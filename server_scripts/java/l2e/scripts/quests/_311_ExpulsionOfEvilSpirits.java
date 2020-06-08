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

import java.util.HashMap;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.position.CharPosition;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 14.03.2011 Based on L2J Eternity-World
 */
public class _311_ExpulsionOfEvilSpirits extends Quest
{
	private static final String qn = "_311_ExpulsionOfEvilSpirits";
	
	private static final int CHAIREN = 32655;
	
	private static final int RAGNA_ORCS_AMULET = 14882; // Ragna Orc's Amulet
	private static final int SOUL_CORE = 14881; // Soul Core Containing Evil Spirit
	private static final int SOUL_PENDANT = 14848; // Protection Soul's Pendant
	
	private static boolean SPAWN_VARANGKA = false;
	
	private static final HashMap<L2Npc, L2Npc> PAGES = new HashMap<>();
	private static final HashMap<L2Npc, L2Npc> RETAINERS = new HashMap<>();
	
	private static final int SHAMAN_VARANGKA = 18808;
	private static final int VARANGKA_RETAINER = 18809;
	private static final int VARANGKA_PAGE = 18810;
	private static final int VARANGKA_GUARDIAN = 22700;
	private static final int GUARDIAN_OF_THE_ALTAR = 18811;
	
	private static L2Npc ALTAR;
	private static L2Npc VARANGKA;
	
	private static final Location ALTAR_LOCATION = new Location(74239, -101912, -967, 29865);
	
	private static final int[] MOBS = new int[]
	{
		22691, // Ragna Orc (Spirit Infested)
		22692, // Ragna Orc Warrior (Spirit Infested)
		22693, // Ragna Orc Hero (Spirit Infested)
		22694, // Ragna Orc Commander (Spirit Infested)
		22695, // Ragna Orc Healer (Spirit Infested)
		22696, // Ragna Orc Shaman (Spirit Infested)
		22697, // Ragna Orc Seer (Spirit Infested)
		22698, // Ragna Orc Archer (Spirit Infested)
		22699, // Ragna Orc Sniper (Spirit Infested)
		22700, // Varangka's Guardian
		22701, // Varangka's Dre Vanul
		22702
	// Varangka's Destroyer
	};
	private static HashMap<Integer, Integer> Itens = new HashMap<>();
	
	private _311_ExpulsionOfEvilSpirits(int questId, String name, String descr)
	{
		super(questId, name, descr);
		questItemIds = new int[]
		{
			SOUL_CORE,
			RAGNA_ORCS_AMULET
		};
		addStartNpc(CHAIREN);
		addTalkId(CHAIREN);
		for (int mob : MOBS)
		{
			addKillId(mob);
		}
		
		ALTAR = addSpawn(GUARDIAN_OF_THE_ALTAR, ALTAR_LOCATION.getX(), ALTAR_LOCATION.getY(), ALTAR_LOCATION.getZ(), ALTAR_LOCATION.getHeading(), false, 0);
		ALTAR.getSpawn().startRespawn();
		ALTAR.getSpawn().setRespawnDelay(1800);
		ALTAR.setIsImmobilized(true);
		ALTAR.setIsMortal(false);
		
		addAttackId(GUARDIAN_OF_THE_ALTAR);
		addAttackId(SHAMAN_VARANGKA);
		addKillId(SHAMAN_VARANGKA);
		addKillId(VARANGKA_PAGE);
		addKillId(VARANGKA_RETAINER);
	}
	
	private String onExchangeRequest(int itemID, QuestState qs)
	{
		qs.takeItems(RAGNA_ORCS_AMULET, Itens.get(itemID));
		if (itemID < 9514)
		{
			qs.giveItems(itemID, (long) Config.RATE_QUEST_REWARD_RECIPE);
		}
		else if (itemID < 9628)
		{
			qs.giveItems(itemID, (long) Config.RATE_QUEST_REWARD_SCROLL); // Giant's Codex
		}
		else if (itemID < 9631)
		{
			qs.giveItems(itemID, (long) Config.RATE_QUEST_REWARD_MATERIAL);
		}
		qs.playSound("ItemSound.quest_finish");
		return "32655-13ok.htm";
	}
	
	private L2Npc addMinion(int npcID, int x, int y, int z, int heading)
	{
		L2Npc varangkaMinion = addSpawn(npcID, x, y, z, heading, false, 0);
		varangkaMinion.setRunning();
		((L2Attackable) varangkaMinion).addDamageHate(VARANGKA.getTarget().getActingPlayer(), 1, 99999);
		varangkaMinion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, VARANGKA.getTarget().getActingPlayer());
		return varangkaMinion;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		QuestState qs = attacker.getQuestState(qn);
		if (qs == null)
		{
			return "";
		}
		
		int npcID = npc.getId();
		if ((npcID == GUARDIAN_OF_THE_ALTAR) && (qs.getQuestItemsCount(SOUL_PENDANT) > 0) && !SPAWN_VARANGKA)
		{
			if (getRandom(100) < 10)
			{
				SPAWN_VARANGKA = true;
				qs.takeItems(SOUL_PENDANT, 1);
				
				VARANGKA = addSpawn(SHAMAN_VARANGKA, 74945, -101924, -967, ALTAR_LOCATION.getHeading(), false, 0);
				((L2Attackable) VARANGKA).addDamageHate(attacker, 1, 99999);
				VARANGKA.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
				
				addMinion(VARANGKA_PAGE, 74722, -101651, -967, ALTAR_LOCATION.getHeading());
				addMinion(VARANGKA_RETAINER, 74659, -101618, -967, ALTAR_LOCATION.getHeading());
			}
		}
		
		if (npcID == SHAMAN_VARANGKA)
		{
			if (getRandom(100) < 15)
			{
				if (getRandom(100) < 50)
				{
					CharPosition position = npc.getPosition();
					L2Npc page = addMinion(VARANGKA_PAGE, position.getX() + 80, position.getY(), position.getZ(), position.getHeading());
					L2Npc retainer = addMinion(VARANGKA_RETAINER, position.getX() - 80, position.getY(), position.getZ(), position.getHeading());
					PAGES.put(page, retainer);
					RETAINERS.put(retainer, page);
				}
				
				int newX = getRandom(600);
				int newY = 600 - newX;
				newX = getRandom(100) < 50 ? -newX : +newX;
				newY = getRandom(100) < 50 ? -newY : +newY;
				
				npc.teleToLocation(ALTAR_LOCATION.getX() + newX, ALTAR_LOCATION.getY() + newY, ALTAR_LOCATION.getZ(), ALTAR_LOCATION.getHeading(), false);
				((L2Attackable) VARANGKA).addDamageHate(attacker, 1, 99999);
				VARANGKA.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
		}
		return "";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState qs = player.getQuestState(qn);
		if (qs == null)
		{
			return htmltext;
		}
		
		int itemID = 0;
		try
		{
			itemID = Integer.parseInt(event);
		}
		catch (Exception e)
		{
			itemID = 0;
		}
		
		if (event.equalsIgnoreCase("32655-yes.htm"))
		{
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (Itens.containsKey(itemID))
		{
			long count = qs.getQuestItemsCount(RAGNA_ORCS_AMULET);
			if (count >= Itens.get(itemID))
			{
				htmltext = this.onExchangeRequest(itemID, qs);
			}
			else
			{
				htmltext = "32655-13no.htm";
			}
		}
		else if (event.equalsIgnoreCase("32655-14.htm"))
		{
			long count = qs.getQuestItemsCount(SOUL_CORE);
			if (count >= 10)
			{
				qs.takeItems(SOUL_CORE, 10);
				qs.giveItems(SOUL_PENDANT, 1);
				qs.playSound("ItemSound.quest_finish");
			}
			else
			{
				htmltext = "32655-14no.htm";
			}
		}
		else if (event.equalsIgnoreCase("32655-quit.htm"))
		{
			qs.unset("cond");
			qs.exitQuest(true);
			qs.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState qs = player.getQuestState(qn);
		if (qs == null)
		{
			return null;
		}
		if (qs.getState() != State.STARTED)
		{
			return null;
		}
		
		int npcID = npc.getId();
		boolean cond = qs.getInt("cond") != 0;
		
		boolean contains = false;
		for (int mobID : MOBS)
		{
			if (mobID == npcID)
			{
				contains = true;
				break;
			}
		}
		
		float questRate;
		float chance;
		long numItems;
		if (cond && contains)
		{
			questRate = Config.RATE_QUEST_DROP * 20;
			chance = questRate % 100;
			numItems = (long) ((questRate - chance) / 100);
			
			if (getRandom(100) < chance)
			{
				numItems += 1;
			}
			if (numItems > 0)
			{
				qs.giveItems(RAGNA_ORCS_AMULET, numItems);
				qs.playSound("ItemSound.quest_itemget");
			}
			
			questRate = Config.RATE_QUEST_DROP;
			chance = questRate % 100;
			numItems = (long) ((questRate - chance) / 100);
			if (getRandom(100) < chance)
			{
				numItems += 1;
			}
			if (numItems > 0)
			{
				qs.giveItems(SOUL_CORE, numItems);
				qs.playSound("ItemSound.quest_itemget");
			}
		}
		else if (npcID == SHAMAN_VARANGKA)
		{
			ALTAR.setIsMortal(true);
			ALTAR.doDie(player);
			ALTAR.setIsMortal(false);
			SPAWN_VARANGKA = false;
		}
		else if (npcID == VARANGKA_PAGE)
		{
			if (PAGES.containsKey(npc) && PAGES.get(npc).isDead())
			{
				RETAINERS.remove(PAGES.get(npc));
				PAGES.remove(npc);
				addSpawn(VARANGKA_GUARDIAN, VARANGKA.getX(), VARANGKA.getY(), VARANGKA.getZ(), VARANGKA.getHeading(), false, 0).setTarget(player);
			}
		}
		else if (npcID == VARANGKA_RETAINER)
		{
			if (RETAINERS.containsKey(npc) && RETAINERS.get(npc).isDead())
			{
				PAGES.remove(RETAINERS.get(npc));
				RETAINERS.remove(npc);
				addSpawn(VARANGKA_GUARDIAN, VARANGKA.getX(), VARANGKA.getY(), VARANGKA.getZ(), VARANGKA.getHeading(), false, 0).setTarget(player);
			}
		}
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		
		if (qs == null)
		{
			return htmltext;
		}
		
		int npcID = npc.getId();
		boolean cond = qs.getInt("cond") != 0;
		
		if (npcID == CHAIREN)
		{
			if (!cond)
			{
				if (player.getLevel() >= 80)
				{
					htmltext = "32655-01.htm";
				}
				else
				{
					htmltext = "32655-lvl.htm";
					qs.exitQuest(true);
				}
			}
			else if (qs.getState() == State.STARTED)
			{
				if ((qs.getQuestItemsCount(SOUL_CORE) > 0) || (qs.getQuestItemsCount(RAGNA_ORCS_AMULET) > 0))
				{
					htmltext = "32655-12.htm";
				}
				else
				{
					htmltext = "32655-10.htm";
				}
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		Itens.put(9482, 488); // Recipe: Sealed Dynasty Breast Plate
		Itens.put(9483, 305); // Recipe: Sealed Dynasty Gaiter
		Itens.put(9484, 183); // Recipe: Sealed Dynasty Helmet
		Itens.put(9485, 122); // Recipe: Sealed Dynasty Gauntlet
		Itens.put(9486, 122); // Recipe: Sealed Dynasty Boots
		Itens.put(9487, 366); // Recipe: Sealed Dynasty Leather Armor
		Itens.put(9488, 229); // Recipe: Sealed Dynasty Leather Leggings
		Itens.put(9489, 183); // Recipe: Sealed Dynasty Leather Helmet
		Itens.put(9490, 122); // Recipe: Sealed Dynasty Leather Gloves
		Itens.put(9491, 122); // Recipe: Sealed Dynasty Leather Boots
		Itens.put(9497, 129); // Recipe: Sealed Dynasty Shield
		
		Itens.put(9625, 667); // Giant's Codex - Oblivion
		Itens.put(9626, 1000); // Giant's Codex - Discipline
		Itens.put(9628, 24); // Leonard
		Itens.put(9629, 48); // Adamantine
		Itens.put(9630, 36); // Orichalcum
		
		new _311_ExpulsionOfEvilSpirits(311, qn, "");
	}
}