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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2QuestGuardInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;

public final class _727_HopeWithinTheDarkness extends Quest
{
	private class CAUWorld extends InstanceWorld
	{
		public CAUWorld()
		{
		}
		
		public boolean underAttack = false;
		public boolean allMonstersDead = true;
	}
	
	public static class CastleDungeon
	{
		private final int INSTANCEID;
		private final int _wardenId;
		
		public CastleDungeon(int iId, int wardenId)
		{
			INSTANCEID = iId;
			_wardenId = wardenId;
		}
		
		public int getInstanceId()
		{
			return INSTANCEID;
		}
		
		public long getReEnterTime()
		{
			String tmp = GlobalVariablesManager.getInstance().getStoredVariable("Castle_dungeon_" + Integer.toString(_wardenId));
			
			return tmp == null ? 0 : Long.parseLong(tmp);
		}
		
		public void setReEnterTime(long time)
		{
			GlobalVariablesManager.getInstance().storeVariable("Castle_dungeon_" + Integer.toString(_wardenId), Long.toString(time));
		}
	}
	
	private static final String qn = "_727_HopeWithinTheDarkness";
	
	private static final boolean CHECK_FOR_CONTRACT = false;
	private static final long REENTER_INTERVAL = 14400000;
	private static final long INITIAL_SPAWN_DELAY = 120000;
	private static final long WAVE_SPAWN_DELAY = 480000;
	private static final long PRIVATE_SPAWN_DELAY = 180000;
	
	private final TIntObjectHashMap<CastleDungeon> _castleDungeons = new TIntObjectHashMap<>(21);
	
	private static final int KNIGHT_EPALUETTE = 9912;
	
	private static final int NPC_KNIGHT = 36562;
	private static final int NPC_RANGER = 36563;
	private static final int NPC_MAGE = 36564;
	private static final int NPC_WARRIOR = 36565;
	
	private static final int[] BOSSES =
	{
		25653,
		25654,
		25655
	};
	private static final int[] MONSTERS =
	{
		25656,
		25657,
		25658
	};
	
	protected static final int[][] BOSSES_FIRST_WAVE =
	{
		{
			BOSSES[0],
			50943,
			-12224,
			-9321,
			32768
		}
	};
	
	protected static final int[][] BOSSES_SECOND_WAVE =
	{
		{
			BOSSES[1],
			50943,
			-12224,
			-9321,
			32768
		}
	};
	
	protected static final int[][] BOSSES_THIRD_WAVE =
	{
		{
			BOSSES[2],
			50943,
			-12004,
			-9321,
			32768
		},
		{
			BOSSES[2],
			50943,
			-12475,
			-9321,
			32768
		}
	};
	
	protected static final int[][] MONSTERS_FIRST_WAVE =
	{
		{
			MONSTERS[0],
			50343,
			-12552,
			-9388,
			32768
		},
		{
			MONSTERS[0],
			50344,
			-12340,
			-9380,
			32768
		},
		{
			MONSTERS[0],
			50341,
			-12134,
			-9381,
			32768
		},
		{
			MONSTERS[0],
			50342,
			-11917,
			-9389,
			32768
		},
		{
			MONSTERS[0],
			50476,
			-12461,
			-9392,
			32768
		},
		{
			MONSTERS[0],
			50481,
			-12021,
			-9390,
			32768
		},
		{
			MONSTERS[0],
			50605,
			-12407,
			-9392,
			32768
		},
		{
			MONSTERS[0],
			50602,
			-12239,
			-9380,
			32768
		},
		{
			MONSTERS[0],
			50606,
			-12054,
			-9390,
			32768
		}
	};
	
	protected static final int[][] MONSTERS_SECOND_WAVE =
	{
		{
			MONSTERS[1],
			50343,
			-12552,
			-9388,
			32768
		},
		{
			MONSTERS[1],
			50344,
			-12340,
			-9380,
			32768
		},
		{
			MONSTERS[1],
			50341,
			-12134,
			-9381,
			32768
		},
		{
			MONSTERS[1],
			50342,
			-11917,
			-9389,
			32768
		},
		{
			MONSTERS[1],
			50476,
			-12461,
			-9392,
			32768
		},
		{
			MONSTERS[1],
			50481,
			-12021,
			-9390,
			32768
		},
		{
			MONSTERS[1],
			50605,
			-12407,
			-9392,
			32768
		},
		{
			MONSTERS[1],
			50602,
			-12239,
			-9380,
			32768
		},
		{
			MONSTERS[1],
			50606,
			-12054,
			-9390,
			32768
		}
	};
	
	protected static final int[][] MONSTERS_THIRD_WAVE =
	{
		{
			MONSTERS[1],
			50343,
			-12552,
			-9388,
			32768
		},
		{
			MONSTERS[1],
			50344,
			-12340,
			-9380,
			32768
		},
		{
			MONSTERS[1],
			50341,
			-12134,
			-9381,
			32768
		},
		{
			MONSTERS[1],
			50342,
			-11917,
			-9389,
			32768
		},
		{
			MONSTERS[2],
			50476,
			-12461,
			-9392,
			32768
		},
		{
			MONSTERS[2],
			50481,
			-12021,
			-9390,
			32768
		},
		{
			MONSTERS[2],
			50605,
			-12407,
			-9392,
			32768
		},
		{
			MONSTERS[2],
			50602,
			-12239,
			-9380,
			32768
		},
		{
			MONSTERS[2],
			50606,
			-12054,
			-9390,
			32768
		}
	};
	
	protected static final int[][] NPCS =
	{
		{
			NPC_WARRIOR,
			49093,
			-12077,
			-9395,
			0
		},
		{
			NPC_RANGER,
			49094,
			-12238,
			-9386,
			0
		},
		{
			NPC_MAGE,
			49093,
			-12401,
			-9388,
			0
		},
		{
			NPC_KNIGHT,
			49232,
			-12239,
			-9386,
			0
		}
	};
	
	protected static final NpcStringId[] NPC_INJURED_FSTRINGID =
	{
		NpcStringId.YOUR_MIND_IS_GOING_BLANK,
		NpcStringId.YOUR_MIND_IS_GOING_BLANK
	};
	
	protected static final NpcStringId[] NPC_DIE_FSTRINGID =
	{
		NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH,
		NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH,
		NpcStringId.KYAAAK,
		NpcStringId.GASP_HOW_CAN_THIS_BE
	};
	
	protected static final NpcStringId NPC_WIN_FSTRINGID = NpcStringId.YOUVE_DONE_IT_WE_BELIEVED_IN_YOU_WARRIOR_WE_WANT_TO_SHOW_OUR_SINCERITY_THOUGH_IT_IS_SMALL_PLEASE_GIVE_ME_SOME_OF_YOUR_TIME;
	protected static final NpcStringId BOSS_DIE_FSTRINGID = NpcStringId.HOW_DARE_YOU;
	
	protected static final NpcStringId[] BOSS_SPAWN_FSTRINGID =
	{
		NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES,
		NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES,
		NpcStringId.YOULL_FLOUNDER_IN_DELUSION_FOR_THE_REST_OF_YOUR_LIFE
	};
	
	private static Map<Integer, SkillsHolder> NPC_BUFFS = new FastMap<>();
	private static final SkillsHolder RAID_CURSE = new SkillsHolder(5456, 1);
	
	public _727_HopeWithinTheDarkness(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		_castleDungeons.put(36403, new CastleDungeon(101, 36403));
		_castleDungeons.put(36404, new CastleDungeon(102, 36404));
		_castleDungeons.put(36405, new CastleDungeon(103, 36405));
		_castleDungeons.put(36406, new CastleDungeon(104, 36406));
		_castleDungeons.put(36407, new CastleDungeon(105, 36407));
		_castleDungeons.put(36408, new CastleDungeon(106, 36408));
		_castleDungeons.put(36409, new CastleDungeon(107, 36409));
		_castleDungeons.put(36410, new CastleDungeon(108, 36410));
		_castleDungeons.put(36411, new CastleDungeon(109, 36411));
		
		NPC_BUFFS.put(NPC_KNIGHT, new SkillsHolder(5970, 1));
		NPC_BUFFS.put(NPC_RANGER, new SkillsHolder(5971, 1));
		NPC_BUFFS.put(NPC_MAGE, new SkillsHolder(5972, 1));
		NPC_BUFFS.put(NPC_WARRIOR, new SkillsHolder(5973, 1));
		
		for (int i : _castleDungeons.keys())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		for (int i = NPC_KNIGHT; i <= NPC_WARRIOR; i++)
		{
			addSpawnId(i);
			addKillId(i);
			addAttackId(i);
			addTalkId(i);
			addFirstTalkId(i);
		}
		
		for (int i : BOSSES)
		{
			addSpawnId(i);
			addKillId(i);
			addAttackId(i);
		}
		
		for (int i : MONSTERS)
		{
			addKillId(i);
			addAttackId(i);
		}
	}
	
	private String checkEnterConditions(L2PcInstance player, L2Npc npc)
	{
		Castle castle = npc.getCastle();
		CastleDungeon dungeon = _castleDungeons.get(npc.getId());
		boolean haveContract = false;
		
		if ((player == null) || (castle == null) || (dungeon == null))
		{
			return "CastleWarden-03.htm";
		}
		
		if (CHECK_FOR_CONTRACT)
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getContractedCastleId() == castle.getId())
				{
					haveContract = true;
					break;
				}
			}
			
			if (!haveContract)
			{
				return "CastleWarden-13a.htm";
			}
		}
		
		QuestState st = player.getQuestState(qn);
		
		if ((st == null) || (st.getInt("cond") < 1))
		{
			if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getId()))
			{
				return "CastleWarden-08.htm";
			}
			
			if (player.getLevel() >= 80)
			{
				return "CastleWarden-06.htm";
			}
			
			return "CastleWarden-07.htm";
		}
		
		L2Party party = player.getParty();
		
		if (party == null)
		{
			return "CastleWarden-09.htm";
		}
		
		if (party.getLeader() != player)
		{
			return showHtmlFile(player, "CastleWarden-10.htm").replace("%leader%", party.getLeader().getName());
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			st = partyMember.getQuestState(qn);
			
			if ((st == null) || (st.getInt("cond") < 1))
			{
				return showHtmlFile(player, "CastleWarden-12.htm").replace("%player%", partyMember.getName());
			}
			
			if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getId()))
			{
				return showHtmlFile(player, "CastleWarden-11.htm").replace("%player%", partyMember.getName());
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				return showHtmlFile(player, "CastleWarden-17.htm").replace("%player%", partyMember.getName());
			}
		}
		
		if (dungeon.getReEnterTime() > System.currentTimeMillis())
		{
			return "CastleWarden-18.htm";
		}
		return null;
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	protected String enterInstance(L2PcInstance player, String template, int[] coords, CastleDungeon dungeon, String ret)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof CAUWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return null;
			}
			teleportPlayer(player, coords, world.instanceId);
			return null;
		}
		
		if (ret != null)
		{
			return ret;
		}
		
		L2Party party = player.getParty();
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new CAUWorld();
		world.instanceId = instanceId;
		world.templateId = dungeon.getInstanceId();
		world.status = 0;
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTER_INTERVAL);
		InstanceManager.getInstance().addWorld(world);
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs((CAUWorld) world), INITIAL_SPAWN_DELAY);
		
		if (player.getParty() == null)
		{
			return "CastleWarden-09.htm";
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			teleportPlayer(partyMember, coords, instanceId);
			world.allowed.add(partyMember.getObjectId());
			if (partyMember.getQuestState(qn) == null)
			{
				newQuestState(partyMember);
			}
			
			partyMember.getQuestState(qn).set("cond", "2");
		}
		return showHtmlFile(player, "CastleWarden-13.htm").replace("%clan%", player.getClan().getName());
	}
	
	protected class spawnNpcs implements Runnable
	{
		private final CAUWorld _world;
		
		public spawnNpcs(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_world.status == 0)
				{
					for (int[] spawn : NPCS)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					}
					for (int[] spawn : BOSSES_FIRST_WAVE)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs(_world), WAVE_SPAWN_DELAY);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
				}
				else if (_world.status == 1)
				{
					for (int[] spawn : BOSSES_SECOND_WAVE)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs(_world), WAVE_SPAWN_DELAY);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
				}
				else if (_world.status == 2)
				{
					for (int[] spawn : BOSSES_THIRD_WAVE)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
				}
			}
			catch (Exception e)
			{
			}
		}
	}
	
	private class spawnPrivates implements Runnable
	{
		private final CAUWorld _world;
		
		public spawnPrivates(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_world.status == 0)
				{
					for (int[] spawn : MONSTERS_FIRST_WAVE)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					}
					
					_world.underAttack = true;
				}
				if (_world.status == 1)
				{
					for (int[] spawn : MONSTERS_SECOND_WAVE)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					}
				}
				if (_world.status == 2)
				{
					for (int[] spawn : MONSTERS_THIRD_WAVE)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					}
				}
				_world.status++;
			}
			catch (Exception e)
			{
			}
		}
	}
	
	protected class Win implements Runnable
	{
		private final CAUWorld _world;
		private final L2PcInstance _player;
		
		public Win(CAUWorld world, L2PcInstance player)
		{
			_world = world;
			_player = player;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_world.status == 4)
				{
					_world.underAttack = false;
					Instance inst = InstanceManager.getInstance().getInstance(_world.instanceId);
					
					for (L2Npc _npc : inst.getNpcs())
					{
						
						if ((_npc != null) && ((_npc.getId() >= NPC_KNIGHT) && (_npc.getId() <= NPC_WARRIOR)))
						{
							cancelQuestTimer("check_for_foes", _npc, null);
							cancelQuestTimer("buff", _npc, null);
							
							if (_npc.getId() == NPC_KNIGHT)
							{
								_npc.broadcastPacket(new NpcSay(_npc.getObjectId(), Say2.SHOUT, _npc.getId(), NPC_WIN_FSTRINGID));
							}
						}
					}
					
					if (_player != null)
					{
						L2Party party = _player.getParty();
						
						if (party == null)
						{
							rewardPlayer(_player);
						}
						else
						{
							for (L2PcInstance partyMember : party.getMembers())
							{
								if ((partyMember != null) && (partyMember.getInstanceId() == _player.getInstanceId()))
								{
									rewardPlayer(partyMember);
								}
							}
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}
	
	protected void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if ((st != null) && (st.getInt("cond") == 2))
		{
			st.set("cond", "3");
		}
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			int[] tele = new int[3];
			tele[0] = 48163;
			tele[1] = -12195;
			tele[2] = -9140;
			return enterInstance(player, "CastlePailaka.xml", tele, _castleDungeons.get(npc.getId()), checkEnterConditions(player, npc));
		}
		
		else if (event.equalsIgnoreCase("suicide"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			tmpworld.status = 5;
			Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			if (inst != null)
			{
				for (L2Npc _npc : inst.getNpcs())
				{
					if ((_npc != null) && ((_npc.getId() >= NPC_KNIGHT) && (_npc.getId() <= NPC_WARRIOR)))
					{
						cancelQuestTimer("check_for_foes", _npc, null);
						cancelQuestTimer("buff", _npc, null);
						
						if (!_npc.isDead())
						{
							_npc.doDie(null);
						}
					}
				}
				inst.setDuration(5 * 60000);
				inst.setEmptyDestroyTime(0);
			}
			return null;
		}
		
		else if (event.equalsIgnoreCase("buff"))
		{
			Collection<L2PcInstance> players = npc.getKnownList().getKnownPlayers().values();
			for (L2PcInstance pl : players)
			{
				if ((pl != null) && Util.checkIfInRange(75, npc, pl, false) && (NPC_BUFFS.get(npc.getId()) != null))
				{
					npc.setTarget(pl);
					npc.doCast(NPC_BUFFS.get(npc.getId()).getSkill());
				}
			}
			startQuestTimer("buff", 120000, npc, null);
			return null;
		}
		else if (event.equalsIgnoreCase("check_for_foes"))
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				for (L2Character foe : npc.getKnownList().getKnownCharactersInRadius(npc.getAggroRange()))
				{
					if ((foe instanceof L2Attackable) && !(foe instanceof L2QuestGuardInstance))
					{
						((L2QuestGuardInstance) npc).addDamageHate(foe, 0, 999);
						((L2QuestGuardInstance) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, foe, null);
					}
				}
			}
			startQuestTimer("check_for_foes", 5000, npc, null);
			return null;
		}
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("CastleWarden-05.htm"))
		{
			if (cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("CastleWarden-15.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			
			if (world.underAttack)
			{
				return "Victim-02.htm";
			}
			else if (world.status == 4)
			{
				return "Victim-03.htm";
			}
			else
			{
				return "Victim-01.htm";
			}
		}
		return null;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if ((tmpworld != null) && (tmpworld instanceof CAUWorld))
			{
				CAUWorld world = (CAUWorld) tmpworld;
				world.allowed.remove(player.getObjectId());
				int instanceId = npc.getInstanceId();
				Instance instance = InstanceManager.getInstance().getInstance(instanceId);
				teleportPlayer(player, instance.getSpawnsLoc(), 0);
				if (instance.getPlayers().isEmpty())
				{
					InstanceManager.getInstance().destroyInstance(instanceId);
				}
				return null;
			}
		}
		
		if ((player.getClan() == null) || (npc.getCastle() == null) || (player.getClan().getCastleId() != npc.getCastle().getId()))
		{
			return "CastleWarden-03.htm";
		}
		else if (st != null)
		{
			int npcId = npc.getId();
			int cond = 0;
			if (st.getState() == State.CREATED)
			{
				st.set("cond", "0");
			}
			else
			{
				cond = st.getInt("cond");
			}
			if (_castleDungeons.containsKey(npcId) && (cond == 0))
			{
				if (player.getLevel() >= 80)
				{
					htmltext = "CastleWarden-01.htm";
				}
				else
				{
					htmltext = "CastleWarden-04.htm";
					st.exitQuest(true);
				}
			}
			else if (_castleDungeons.containsKey(npcId) && (cond > 0) && (st.getState() == State.STARTED))
			{
				if (cond == 1)
				{
					htmltext = "CastleWarden-15.htm";
				}
				else if (cond == 3)
				{
					st.playSound("ItemSound.quest_finish");
					st.giveItems(KNIGHT_EPALUETTE, 159);
					st.exitQuest(true);
					htmltext = "CastleWarden-16.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			if (npc.getCurrentHp() <= (npc.getMaxHp() * 0.1))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NPC_INJURED_FSTRINGID[1]));
			}
			else if (npc.getCurrentHp() <= (npc.getMaxHp() * 0.4))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NPC_INJURED_FSTRINGID[0]));
			}
			return null;
		}
		
		if (player != null)
		{
			L2Playable attacker = (isSummon ? player.getSummon() : player);
			if ((attacker.getLevel() - npc.getLevel()) >= 9)
			{
				if ((attacker.getBuffCount() > 0) || (attacker.getDanceCount() > 0))
				{
					npc.setTarget(attacker);
					npc.doSimultaneousCast(RAID_CURSE.getSkill());
				}
				else if (player.getParty() != null)
				{
					for (L2PcInstance pmember : player.getParty().getMembers())
					{
						if ((pmember.getBuffCount() > 0) || (pmember.getDanceCount() > 0))
						{
							npc.setTarget(pmember);
							npc.doSimultaneousCast(RAID_CURSE.getSkill());
						}
					}
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NPC_DIE_FSTRINGID[npc.getId() - 36562]));
			
			startQuestTimer("suicide", 1500, npc, null);
			cancelQuestTimer("check_for_foes", npc, null);
			cancelQuestTimer("buff", npc, null);
			return null;
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			if (Util.contains(BOSSES, npc.getId()))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), BOSS_DIE_FSTRINGID));
			}
			
			if ((tmpworld.status == 3) && (Util.contains(BOSSES, npc.getId()) || Util.contains(MONSTERS, npc.getId())))
			{
				world.allMonstersDead = true;
				Instance inst = InstanceManager.getInstance().getInstance(tmpworld.instanceId);
				
				if (inst != null)
				{
					for (L2Npc _npc : inst.getNpcs())
					{
						if ((_npc != null) && !_npc.isDead() && (Util.contains(BOSSES, _npc.getId()) || Util.contains(MONSTERS, _npc.getId())))
						{
							world.allMonstersDead = false;
							break;
						}
						
					}
					
					if (world.allMonstersDead)
					{
						tmpworld.status = 4;
						inst.setDuration(5 * 60000);
						inst.setEmptyDestroyTime(0);
						ThreadPoolManager.getInstance().scheduleGeneral(new Win(world, player), 1500);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			startQuestTimer("buff", 120000, npc, null);
			startQuestTimer("check_for_foes", 120000, npc, null);
			
			if (npc.getId() == NPC_KNIGHT)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.WARRIORS_HAVE_YOU_COME_TO_HELP_THOSE_WHO_ARE_IMPRISONED_HERE));
			}
		}
		else if (Arrays.binarySearch(BOSSES, npc.getId()) >= 0)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), BOSS_SPAWN_FSTRINGID[Arrays.binarySearch(BOSSES, npc.getId())]));
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _727_HopeWithinTheDarkness(727, qn, "");
	}
}