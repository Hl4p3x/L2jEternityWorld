/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://eternity-world.ru/>.
 */
package l2e.scripts.quests;

import java.util.Calendar;
import java.util.StringTokenizer;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TerritoryWarManager.TerritoryNPCSpawn;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.TerritoryWard;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.util.Util;
import l2e.util.L2FastMap;

public class TerritoryWarSuperClass extends Quest
{
	private static L2FastMap<Integer, TerritoryWarSuperClass> _forTheSakeScripts = new L2FastMap<>();
	private static L2FastMap<Integer, TerritoryWarSuperClass> _protectTheScripts = new L2FastMap<>();
	private static L2FastMap<Integer, TerritoryWarSuperClass> _killTheScripts = new L2FastMap<>();
	
	public static String qn = "TerritoryWarSuperClass";
	
	public int CATAPULT_ID;
	public int TERRITORY_ID;
	public int[] LEADER_IDS;
	public int[] GUARD_IDS;
	public NpcStringId[] npcString = {};

	public int[] NPC_IDS;

	public int[] CLASS_IDS;
	public int RANDOM_MIN;
	public int RANDOM_MAX;
	
	public void registerKillIds()
	{
		addKillId(CATAPULT_ID);
		for (int mobid : LEADER_IDS)
		{
			addKillId(mobid);
		}
		for (int mobid : GUARD_IDS)
		{
			addKillId(mobid);
		}
	}
	
	public void registerAttackIds()
	{
		for (int mobid : NPC_IDS)
		{
			addAttackId(mobid);
		}
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (Util.contains(targets, npc))
		{
			if (skill.getId() == 845)
			{
				if (TerritoryWarManager.getInstance().getHQForClan(caster.getClan()) != npc)
				{
					return super.onSkillSee(npc, caster, skill, targets, isSummon);
				}
				npc.deleteMe();
				TerritoryWarManager.getInstance().setHQForClan(caster.getClan(), null);
			}
			else if (skill.getId() == 847)
			{
				if (TerritoryWarManager.getInstance().getHQForTerritory(caster.getSiegeSide()) != npc)
				{
					return super.onSkillSee(npc, caster, skill, targets, isSummon);
				}
				TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(caster);
				if (ward == null)
				{
					return super.onSkillSee(npc, caster, skill, targets, isSummon);
				}
				if ((caster.getSiegeSide() - 80) == ward.getOwnerCastleId())
				{
					for (TerritoryNPCSpawn wardSpawn : TerritoryWarManager.getInstance().getTerritory(ward.getOwnerCastleId()).getOwnedWard())
					{
						if (wardSpawn.getId() == ward.getTerritoryId())
						{
							wardSpawn.setNPC(wardSpawn.getNpc().getSpawn().doSpawn());
							ward.unSpawnMe();
							ward.setNpc(wardSpawn.getNpc());
						}
					}
				}
				else
				{
					ward.unSpawnMe();
					ward.setNpc(TerritoryWarManager.getInstance().addTerritoryWard(ward.getTerritoryId(), caster.getSiegeSide() - 80, ward.getOwnerCastleId(), true));
					ward.setOwnerCastleId(caster.getSiegeSide() - 80);
					TerritoryWarManager.getInstance().getTerritory(caster.getSiegeSide() - 80).getQuestDone()[1]++;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	public int getTerritoryIdForThisNPCId(int npcid)
	{
		return 0;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if ((npc.getCurrentHp() == npc.getMaxHp()) && Util.contains(NPC_IDS, npc.getId()))
		{
			int territoryId = getTerritoryIdForThisNPCId(npc.getId());
			if ((territoryId >= 81) && (territoryId <= 89))
			{
				for (L2PcInstance pl : L2World.getInstance().getAllPlayersArray())
				{
					if (pl.getSiegeSide() == territoryId)
					{
						QuestState st = pl.getQuestState(getName());
						if (st == null)
						{
							st = newQuestState(pl);
						}
						if (st.getState() != State.STARTED)
						{
							st.setCond(1);
							st.setState(State.STARTED, false);
						}
					}
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == CATAPULT_ID)
		{
			TerritoryWarManager.getInstance().territoryCatapultDestroyed(TERRITORY_ID - 80);
			TerritoryWarManager.getInstance().giveTWPoint(killer, TERRITORY_ID, 4);
			TerritoryWarManager.getInstance().announceToParticipants(new ExShowScreenMessage(npcString[0], 2, 10000), 135000, 13500);
			handleBecomeMercenaryQuest(killer, true);
		}
		else if (Util.contains(LEADER_IDS, npc.getId()))
		{
			TerritoryWarManager.getInstance().giveTWPoint(killer, TERRITORY_ID, 3);
		}
		
		if ((killer.getSiegeSide() != TERRITORY_ID) && (TerritoryWarManager.getInstance().getTerritory(killer.getSiegeSide() - 80) != null))
		{
			TerritoryWarManager.getInstance().getTerritory(killer.getSiegeSide() - 80).getQuestDone()[0]++;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ((npc != null) || (player != null))
		{
			return null;
		}
		StringTokenizer st = new StringTokenizer(event, " ");
		event = st.nextToken();
		if (event.equalsIgnoreCase("setNextTWDate"))
		{
			Calendar startTWDate = Calendar.getInstance();
			startTWDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			startTWDate.set(Calendar.HOUR_OF_DAY, 20);
			startTWDate.set(Calendar.MINUTE, 0);
			startTWDate.set(Calendar.SECOND, 0);
			if (startTWDate.getTimeInMillis() < System.currentTimeMillis())
			{
				startTWDate.add(Calendar.DAY_OF_MONTH, 7);
			}
			if (!SevenSigns.getInstance().isDateInSealValidPeriod(startTWDate))
			{
				startTWDate.add(Calendar.DAY_OF_MONTH, 7);
			}
			saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
			TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
			_log.info("Next TerritoryWarTime: " + startTWDate.getTime());
		}
		else if (event.equalsIgnoreCase("setTWDate") && st.hasMoreTokens())
		{
			Calendar startTWDate = Calendar.getInstance();
			startTWDate.setTimeInMillis(Long.parseLong(st.nextToken()));
			saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
			TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
		}
		return null;
	}
	
	private void handleKillTheQuest(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		int kill = 1;
		int max = 10;
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (!st.isCompleted())
		{
			if (!st.isStarted())
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.set("kill", "1");
				max = getRandom(RANDOM_MIN, RANDOM_MAX);
				st.set("max", String.valueOf(max));
			}
			else
			{
				kill = st.getInt("kill") + 1;
				max = st.getInt("max");
			}
			if (kill >= max)
			{
				TerritoryWarManager.getInstance().giveTWQuestPoint(player);
				st.addExpAndSp(534000, 51000);
				st.set("doneDate", String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)));
				st.setState(State.COMPLETED);
				st.exitQuest(true);
				player.sendPacket(new ExShowScreenMessage(npcString[1], 2, 10000));
			}
			else
			{
				st.set("kill", String.valueOf(kill));
				
				ExShowScreenMessage message = new ExShowScreenMessage(npcString[0], 2, 10000);
				message.addStringParameter(String.valueOf(max));
				message.addStringParameter(String.valueOf(kill));
				player.sendPacket(message);
				
			}
		}
		else if (st.getInt("doneDate") != Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.set("kill", "1");
			max = getRandom(RANDOM_MIN, RANDOM_MAX);
			st.set("max", String.valueOf(max));
			
			ExShowScreenMessage message = new ExShowScreenMessage(npcString[0], 2, 10000);
			message.addStringParameter(String.valueOf(max));
			message.addStringParameter(String.valueOf(kill));
			player.sendPacket(message);
		}
		else if (player.isGM())
		{
			player.sendMessage("Cleaning " + getName() + " Territory War quest by force!");
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.set("kill", "1");
			max = getRandom(RANDOM_MIN, RANDOM_MAX);
			st.set("max", String.valueOf(max));
			
			ExShowScreenMessage message = new ExShowScreenMessage(npcString[0], 2, 10000);
			message.addStringParameter(String.valueOf(max));
			message.addStringParameter(String.valueOf(kill));
			player.sendPacket(message);
		}
	}
	
	private static void handleBecomeMercenaryQuest(L2PcInstance player, boolean catapult)
	{
		int enemyCount = 10, catapultCount = 1;
		QuestState st = player.getQuestState(_147_PathtoBecominganEliteMercenary.class.getSimpleName());
		if ((st != null) && st.isCompleted())
		{
			st = player.getQuestState(_148_PathtoBecominganExaltedMercenary.class.getSimpleName());
			enemyCount = 30;
			catapultCount = 2;
		}
		
		if ((st != null) && st.isStarted())
		{
			if (catapult)
			{
				if (st.isCond(1) || st.isCond(2))
				{
					int count = st.getInt("catapult");
					count++;
					st.set("catapult", String.valueOf(count));
					if (count >= catapultCount)
					{
						if (st.isCond(1))
						{
							st.setCond(3);
						}
						else
						{
							st.setCond(4);
						}
					}
				}
			}
			else
			{
				if (st.isCond(1) || st.isCond(3))
				{
					int _kills = st.getInt("kills");
					_kills++;
					st.set("kills", String.valueOf(_kills));
					if (_kills >= enemyCount)
					{
						if (st.isCond(1))
						{
							st.setCond(2);
						}
						else
						{
							st.setCond(4);
						}
					}
				}
			}
		}
	}
	
	private void handleStepsForHonor(L2PcInstance player)
	{
		int kills = 0;
		int cond = 0;

		QuestState _sfh = player.getQuestState("_176_StepsForHonor");
		if ((_sfh != null) && (_sfh.getState() == State.STARTED))
		{
			cond = _sfh.getInt("cond");
			if ((cond == 1) || (cond == 3) || (cond == 5) || (cond == 7))
			{
				kills = _sfh.getInt("kills");
				kills++;
				_sfh.set("kills", String.valueOf(kills));
				if ((cond == 1) && (kills >= 9))
				{
					_sfh.set("cond", "2");
					_sfh.set("kills", "0");
				}
				else if ((cond == 3) && (kills >= 18))
				{
					_sfh.set("cond", "4");
					_sfh.set("kills", "0");
				}
				else if ((cond == 5) && (kills >= 27))
				{
					_sfh.set("cond", "6");
					_sfh.set("kills", "0");
				}
				else if ((cond == 7) && (kills >= 36))
				{
					_sfh.set("cond", "8");
					_sfh.unset("kills");
				}
			}
		}
	}
	
	@Override
	public String onDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		if ((killer == victim) || !(victim.isPlayer()) || (victim.getLevel() < 61))
		{
			return "";
		}
		L2PcInstance actingPlayer = killer.getActingPlayer();
		if ((actingPlayer != null) && (qs.getPlayer() != null))
		{
			if (actingPlayer.getParty() != null)
			{
				for (L2PcInstance pl : actingPlayer.getParty().getMembers())
				{
					if ((pl.getSiegeSide() == qs.getPlayer().getSiegeSide()) || (pl.getSiegeSide() == 0) || !Util.checkIfInRange(2000, killer, pl, false))
					{
						continue;
					}
					if (pl == actingPlayer)
					{
						handleStepsForHonor(actingPlayer);
						handleBecomeMercenaryQuest(actingPlayer, false);
					}
					handleKillTheQuest(pl);
				}
			}
			else if ((actingPlayer.getSiegeSide() != qs.getPlayer().getSiegeSide()) && (actingPlayer.getSiegeSide() > 0))
			{
				handleKillTheQuest(actingPlayer);
				handleStepsForHonor(actingPlayer);
				handleBecomeMercenaryQuest(actingPlayer, false);
			}
			TerritoryWarManager.getInstance().giveTWPoint(actingPlayer, qs.getPlayer().getSiegeSide(), 1);
		}
		return "";
	}
	
	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		int territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
		if (territoryId > 0)
		{
			TerritoryWarSuperClass territoryQuest = _forTheSakeScripts.get(territoryId);
			QuestState st = player.getQuestState(territoryQuest.getName());
			if (st == null)
			{
				st = territoryQuest.newQuestState(player);
			}
			st.setState(State.STARTED, false);
			st.setCond(1);
			
			if (player.getLevel() >= 61)
			{
				TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassId().getId());
				if (killthe != null)
				{
					st = player.getQuestState(killthe.getName());
					if (st == null)
					{
						st = killthe.newQuestState(player);
					}
					player.addNotifyQuestOfDeath(st);
				}
				else
				{
					_log.warning("TerritoryWar: Missing Kill the quest for player " + player.getName() + " whose class id: " + player.getClassId().getId());
				}
			}
		}
		return null;
	}
	
	@Override
	public void setOnEnterWorld(boolean val)
	{
		super.setOnEnterWorld(val);
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			if (player.getSiegeSide() > 0)
			{
				TerritoryWarSuperClass territoryQuest = _forTheSakeScripts.get(player.getSiegeSide());
				if (territoryQuest == null)
				{
					continue;
				}

				QuestState st = player.hasQuestState(territoryQuest.getName()) ? player.getQuestState(territoryQuest.getName()) : territoryQuest.newQuestState(player);

				if (val)
				{
					st.setState(State.STARTED, false);
					st.setCond(1);
					if (player.getLevel() >= 61)
					{
						TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassId().getId());
						if (killthe != null)
						{
							st = player.getQuestState(killthe.getName());
							if (st == null)
							{
								st = killthe.newQuestState(player);
							}
							player.addNotifyQuestOfDeath(st);
						}
						else
						{
							_log.warning("TerritoryWar: Missing Kill the quest for player " + player.getName() + " whose class id: " + player.getClassId().getId());
						}
					}
				}
				else
				{
					st.exitQuest(false);
					for (Quest q : _protectTheScripts.values())
					{
						st = player.getQuestState(q.getName());
						if (st != null)
						{
							st.exitQuest(false);
						}
					}

					TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassIndex());
					if (killthe != null)
					{
						st = player.getQuestState(killthe.getName());
						if (st != null)
						{
							player.removeNotifyQuestOfDeath(st);
						}
					}
				}
			}
		}
	}
	
	public TerritoryWarSuperClass(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		if (questId < 0)
		{
			addSkillSeeId(36590);
			
			Calendar startTWDate = Calendar.getInstance();
			if (loadGlobalQuestVar("nextTWStartDate").equalsIgnoreCase(""))
			{
				startTWDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
				startTWDate.set(Calendar.HOUR_OF_DAY, 20);
				startTWDate.set(Calendar.MINUTE, 0);
				startTWDate.set(Calendar.SECOND, 0);
				if (startTWDate.getTimeInMillis() < System.currentTimeMillis())
				{
					startTWDate.add(Calendar.DAY_OF_MONTH, 7);
				}
				if (!SevenSigns.getInstance().isDateInSealValidPeriod(startTWDate))
				{
					startTWDate.add(Calendar.DAY_OF_MONTH, 7);
				}
				saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
			}
			else
			{
				startTWDate.setTimeInMillis(Long.parseLong(loadGlobalQuestVar("nextTWStartDate")));
				if ((startTWDate.getTimeInMillis() < System.currentTimeMillis()) && SevenSigns.getInstance().isSealValidationPeriod() && (SevenSigns.getInstance().getMilliToPeriodChange() > 172800000))
				{
					startTWDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
					startTWDate.set(Calendar.HOUR_OF_DAY, 20);
					startTWDate.set(Calendar.MINUTE, 0);
					startTWDate.set(Calendar.SECOND, 0);
					if (startTWDate.getTimeInMillis() < System.currentTimeMillis())
					{
						startTWDate.add(Calendar.DAY_OF_MONTH, 7);
					}
					if (!SevenSigns.getInstance().isDateInSealValidPeriod(startTWDate))
					{
						startTWDate.add(Calendar.DAY_OF_MONTH, 7);
					}
					saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
				}
			}
			TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
			_log.info("Next TerritoryWarTime: " + startTWDate.getTime());
		}
	}
	
	public static void main(String[] args)
	{
		new TerritoryWarSuperClass(-1, qn, "Territory_War");
		
		TerritoryWarSuperClass gludio = new _717_FortheSakeoftheTerritoryGludio();
		_forTheSakeScripts.put(gludio.TERRITORY_ID, gludio);
		TerritoryWarSuperClass dion = new _718_FortheSakeoftheTerritoryDion();
		_forTheSakeScripts.put(dion.TERRITORY_ID, dion);
		TerritoryWarSuperClass giran = new _719_FortheSakeoftheTerritoryGiran();
		_forTheSakeScripts.put(giran.TERRITORY_ID, giran);
		TerritoryWarSuperClass oren = new _720_FortheSakeoftheTerritoryOren();
		_forTheSakeScripts.put(oren.TERRITORY_ID, oren);
		TerritoryWarSuperClass aden = new _721_FortheSakeoftheTerritoryAden();
		_forTheSakeScripts.put(aden.TERRITORY_ID, aden);
		TerritoryWarSuperClass innadril = new _722_FortheSakeoftheTerritoryInnadril();
		_forTheSakeScripts.put(innadril.TERRITORY_ID, innadril);
		TerritoryWarSuperClass goddard = new _723_FortheSakeoftheTerritoryGoddard();
		_forTheSakeScripts.put(goddard.TERRITORY_ID, goddard);
		TerritoryWarSuperClass rune = new _724_FortheSakeoftheTerritoryRune();
		_forTheSakeScripts.put(rune.TERRITORY_ID, rune);
		TerritoryWarSuperClass schuttgart = new _725_FortheSakeoftheTerritorySchuttgart();
		_forTheSakeScripts.put(schuttgart.TERRITORY_ID, schuttgart);
		TerritoryWarSuperClass catapult = new _729_Protecttheterritorycatapult();
		_protectTheScripts.put(catapult.getId(), catapult);
		TerritoryWarSuperClass military = new _731_ProtecttheMilitaryAssociationLeader();
		_protectTheScripts.put(military.getId(), military);
		TerritoryWarSuperClass religious = new _732_ProtecttheReligiousAssociationLeader();
		_protectTheScripts.put(religious.getId(), religious);
		TerritoryWarSuperClass supplies = new _730_ProtecttheSuppliesSafe();
		_protectTheScripts.put(supplies.getId(), supplies);
		TerritoryWarSuperClass knights = new _734_Piercethroughashield();
		for (int i : knights.CLASS_IDS)
			_killTheScripts.put(i, knights);
		TerritoryWarSuperClass warriors = new _735_Makespearsdull();
		for (int i : warriors.CLASS_IDS)
			_killTheScripts.put(i, warriors);
		TerritoryWarSuperClass wizards = new _736_Weakenmagic();
		for (int i : wizards.CLASS_IDS)
			_killTheScripts.put(i, wizards);
		TerritoryWarSuperClass priests = new _737_DenyBlessings();
		for (int i : priests.CLASS_IDS)
			_killTheScripts.put(i, priests);
		TerritoryWarSuperClass keys = new _738_DestroyKeyTargets();
		for (int i : keys.CLASS_IDS)
			_killTheScripts.put(i, keys);
	}
}