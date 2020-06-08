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

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.util.Rnd;

/**
 * Created by LordWinter 15.06.2013 Based on L2J Eternity-World
 */
public class _233_TestOfWarspirit extends Quest
{
	private static final String qn = "_233_TestOfWarspirit";
	
	private static int Somak = 30510;
	private static int Vivyan = 30030;
	private static int Sarien = 30436;
	private static int Racoy = 30507;
	private static int Manakia = 30515;
	private static int Orim = 30630;
	private static int Ancestor_Martankus = 30649;
	private static int Pekiron = 30682;
	
	private static int Porta = 20213;
	private static int Excuro = 20214;
	private static int Mordeo = 20215;
	private static int Noble_Ant = 20089;
	private static int Noble_Ant_Leader = 20090;
	private static int Leto_Lizardman_Shaman = 20581;
	private static int Leto_Lizardman_Overlord = 20582;
	private static int Medusa = 20158;
	private static int Stenoa_Gorgon_Queen = 27108;
	private static int Tamlin_Orc = 20601;
	private static int Tamlin_Orc_Archer = 20602;
	
	private static int MARK_OF_WARSPIRIT = 2879;
	
	private static int VENDETTA_TOTEM = 2880;
	private static int TAMLIN_ORC_HEAD = 2881;
	private static int WARSPIRIT_TOTEM = 2882;
	private static int ORIMS_CONTRACT = 2883;
	private static int PORTAS_EYE = 2884;
	private static int EXCUROS_SCALE = 2885;
	private static int MORDEOS_TALON = 2886;
	private static int BRAKIS_REMAINS1 = 2887;
	private static int PEKIRONS_TOTEM = 2888;
	private static int TONARS_SKULL = 2889;
	private static int TONARS_RIB_BONE = 2890;
	private static int TONARS_SPINE = 2891;
	private static int TONARS_ARM_BONE = 2892;
	private static int TONARS_THIGH_BONE = 2893;
	private static int TONARS_REMAINS1 = 2894;
	private static int MANAKIAS_TOTEM = 2895;
	private static int HERMODTS_SKULL = 2896;
	private static int HERMODTS_RIB_BONE = 2897;
	private static int HERMODTS_SPINE = 2898;
	private static int HERMODTS_ARM_BONE = 2899;
	private static int HERMODTS_THIGH_BONE = 2900;
	private static int HERMODTS_REMAINS1 = 2901;
	private static int RACOYS_TOTEM = 2902;
	private static int VIVIANTES_LETTER = 2903;
	private static int INSECT_DIAGRAM_BOOK = 2904;
	private static int KIRUNAS_SKULL = 2905;
	private static int KIRUNAS_RIB_BONE = 2906;
	private static int KIRUNAS_SPINE = 2907;
	private static int KIRUNAS_ARM_BONE = 2908;
	private static int KIRUNAS_THIGH_BONE = 2909;
	private static int KIRUNAS_REMAINS1 = 2910;
	private static int BRAKIS_REMAINS2 = 2911;
	private static int TONARS_REMAINS2 = 2912;
	private static int HERMODTS_REMAINS2 = 2913;
	private static int KIRUNAS_REMAINS2 = 2914;
	
	private static int[] Noble_Ant_Drops =
	{
		KIRUNAS_THIGH_BONE,
		KIRUNAS_ARM_BONE,
		KIRUNAS_SPINE,
		KIRUNAS_RIB_BONE,
		KIRUNAS_SKULL
	};
	private static int[] Leto_Lizardman_Drops =
	{
		TONARS_SKULL,
		TONARS_RIB_BONE,
		TONARS_SPINE,
		TONARS_ARM_BONE,
		TONARS_THIGH_BONE
	};
	private static int[] Medusa_Drops =
	{
		HERMODTS_RIB_BONE,
		HERMODTS_SPINE,
		HERMODTS_THIGH_BONE,
		HERMODTS_ARM_BONE
	};
	
	public _233_TestOfWarspirit(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Somak);
		addTalkId(Somak);
		addTalkId(Vivyan);
		addTalkId(Sarien);
		addTalkId(Racoy);
		addTalkId(Manakia);
		addTalkId(Orim);
		addTalkId(Ancestor_Martankus);
		addTalkId(Pekiron);
		
		addKillId(Porta);
		addKillId(Excuro);
		addKillId(Mordeo);
		addKillId(Noble_Ant);
		addKillId(Noble_Ant_Leader);
		addKillId(Leto_Lizardman_Shaman);
		addKillId(Leto_Lizardman_Overlord);
		addKillId(Medusa);
		addKillId(Stenoa_Gorgon_Queen);
		addKillId(Tamlin_Orc);
		addKillId(Tamlin_Orc_Archer);
		
		questItemIds = new int[]
		{
			VENDETTA_TOTEM,
			TAMLIN_ORC_HEAD,
			WARSPIRIT_TOTEM,
			ORIMS_CONTRACT,
			PORTAS_EYE,
			EXCUROS_SCALE,
			MORDEOS_TALON,
			BRAKIS_REMAINS1,
			PEKIRONS_TOTEM,
			TONARS_SKULL,
			TONARS_RIB_BONE,
			TONARS_SPINE,
			TONARS_ARM_BONE,
			TONARS_THIGH_BONE,
			TONARS_REMAINS1,
			MANAKIAS_TOTEM,
			HERMODTS_SKULL,
			HERMODTS_RIB_BONE,
			HERMODTS_SPINE,
			HERMODTS_ARM_BONE,
			HERMODTS_THIGH_BONE,
			HERMODTS_REMAINS1,
			RACOYS_TOTEM,
			VIVIANTES_LETTER,
			INSECT_DIAGRAM_BOOK,
			KIRUNAS_SKULL,
			KIRUNAS_RIB_BONE,
			KIRUNAS_SPINE,
			KIRUNAS_ARM_BONE,
			KIRUNAS_THIGH_BONE,
			KIRUNAS_REMAINS1,
			BRAKIS_REMAINS2,
			TONARS_REMAINS2,
			HERMODTS_REMAINS2,
			KIRUNAS_REMAINS2
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("1"))
		{
			htmltext = "30510-05.htm";
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30630_1"))
		{
			htmltext = "30630-02.htm";
		}
		else if (event.equalsIgnoreCase("30630_2"))
		{
			htmltext = "30630-03.htm";
		}
		else if (event.equalsIgnoreCase("30630_3"))
		{
			htmltext = "30630-04.htm";
			st.giveItems(ORIMS_CONTRACT, 1);
		}
		else if (event.equalsIgnoreCase("30682_1"))
		{
			htmltext = "30682-02.htm";
			st.giveItems(PEKIRONS_TOTEM, 1);
		}
		else if (event.equalsIgnoreCase("30515_1"))
		{
			htmltext = "30515-02.htm";
			st.giveItems(MANAKIAS_TOTEM, 1);
		}
		else if (event.equalsIgnoreCase("30507_1"))
		{
			htmltext = "30507-02.htm";
			st.giveItems(RACOYS_TOTEM, 1);
		}
		else if (event.equalsIgnoreCase("30030_1"))
		{
			htmltext = "30030-02.htm";
		}
		else if (event.equalsIgnoreCase("30030_2"))
		{
			htmltext = "30030-03.htm";
		}
		else if (event.equalsIgnoreCase("30030_3"))
		{
			htmltext = "30030-04.htm";
			st.giveItems(VIVIANTES_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30649_1"))
		{
			htmltext = "30649-02.htm";
		}
		else if (event.equalsIgnoreCase("30649_2"))
		{
			htmltext = "30649-03.htm";
			st.takeItems(WARSPIRIT_TOTEM, -1);
			st.takeItems(BRAKIS_REMAINS2, -1);
			st.takeItems(HERMODTS_REMAINS2, -1);
			st.takeItems(KIRUNAS_REMAINS2, -1);
			st.takeItems(TAMLIN_ORC_HEAD, -1);
			st.takeItems(TONARS_REMAINS2, -1);
			st.giveItems(MARK_OF_WARSPIRIT, 1);
			st.addExpAndSp(894888, 61408);
			st.giveItems(57, 161806);
			st.giveItems(7562, 92);
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.exitQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		
		if (npcId == Somak)
		{
			if (player.getClassId().getId() == 0x32)
			{
				if (player.getLevel() > 38)
				{
					htmltext = "30510-04.htm";
				}
				else
				{
					htmltext = "30510-03.htm";
					st.exitQuest(true);
				}
			}
			else if (player.getRace().ordinal() == 3)
			{
				htmltext = "30510-02.htm";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "30510-01.htm";
				st.exitQuest(true);
			}
		}
		
		else if (npcId == Somak)
		{
			if (st.getQuestItemsCount(VENDETTA_TOTEM) > 0)
			{
				if (st.getQuestItemsCount(TAMLIN_ORC_HEAD) < 13)
				{
					htmltext = "30510-08.htm";
				}
				else
				{
					st.takeItems(VENDETTA_TOTEM, -1);
					st.giveItems(WARSPIRIT_TOTEM, 1);
					st.giveItems(BRAKIS_REMAINS2, 1);
					st.giveItems(HERMODTS_REMAINS2, 1);
					st.giveItems(KIRUNAS_REMAINS2, 1);
					st.giveItems(TONARS_REMAINS2, 1);
					st.playSound("Itemsound.quest_middle");
					htmltext = "30510-09.htm";
				}
			}
			if (st.getQuestItemsCount(WARSPIRIT_TOTEM) > 0)
			{
				htmltext = "30510-10.htm";
			}
			else if ((st.getQuestItemsCount(BRAKIS_REMAINS1) == 0) || (st.getQuestItemsCount(HERMODTS_REMAINS1) == 0) || (st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0) || (st.getQuestItemsCount(TONARS_REMAINS1) == 0))
			{
				htmltext = "30510-06.htm";
			}
			else
			{
				st.takeItems(BRAKIS_REMAINS1, -1);
				st.takeItems(HERMODTS_REMAINS1, -1);
				st.takeItems(KIRUNAS_REMAINS1, -1);
				st.takeItems(TONARS_REMAINS1, -1);
				st.giveItems(VENDETTA_TOTEM, 1);
				st.playSound("Itemsound.quest_middle");
				htmltext = "30510-07.htm";
			}
		}
		
		if (npcId == Orim)
		{
			if (st.getQuestItemsCount(ORIMS_CONTRACT) > 0)
			{
				if ((st.getQuestItemsCount(PORTAS_EYE) < 10) || (st.getQuestItemsCount(EXCUROS_SCALE) < 10) || (st.getQuestItemsCount(MORDEOS_TALON) < 10))
				{
					htmltext = "30630-05.htm";
				}
				else
				{
					st.takeItems(ORIMS_CONTRACT, -1);
					st.takeItems(PORTAS_EYE, -1);
					st.takeItems(EXCUROS_SCALE, -1);
					st.takeItems(MORDEOS_TALON, -1);
					st.giveItems(BRAKIS_REMAINS1, 1);
					st.playSound("Itemsound.quest_middle");
					htmltext = "30630-06.htm";
				}
			}
			else if ((st.getQuestItemsCount(BRAKIS_REMAINS1) == 0) && (st.getQuestItemsCount(BRAKIS_REMAINS2) == 0) && (st.getQuestItemsCount(VENDETTA_TOTEM) == 0))
			{
				htmltext = "30630-01.htm";
			}
			else
			{
				htmltext = "30630-07.htm";
			}
		}
		
		if (npcId == Pekiron)
		{
			if (st.getQuestItemsCount(PEKIRONS_TOTEM) > 0)
			{
				for (int drop_id : Leto_Lizardman_Drops)
				{
					if (st.getQuestItemsCount(drop_id) == 0)
					{
						htmltext = "30682-03.htm";
					}
				}
				st.takeItems(PEKIRONS_TOTEM, -1);
				for (int drop_id : Leto_Lizardman_Drops)
				{
					if (st.getQuestItemsCount(drop_id) == 0)
					{
						st.takeItems(drop_id, -1);
					}
				}
				st.giveItems(TONARS_REMAINS1, 1);
				st.playSound("Itemsound.quest_middle");
				htmltext = "30682-04.htm";
			}
			else if ((st.getQuestItemsCount(TONARS_REMAINS1) == 0) && (st.getQuestItemsCount(TONARS_REMAINS2) == 0) && (st.getQuestItemsCount(VENDETTA_TOTEM) == 0))
			{
				htmltext = "30682-01.htm";
			}
			else
			{
				htmltext = "30682-05.htm";
			}
		}
		
		if (npcId == Manakia)
		{
			if (st.getQuestItemsCount(MANAKIAS_TOTEM) > 0)
			{
				if (st.getQuestItemsCount(HERMODTS_SKULL) == 0)
				{
					htmltext = "30515-03.htm";
				}
				for (int drop_id : Medusa_Drops)
				{
					if (st.getQuestItemsCount(drop_id) == 0)
					{
						htmltext = "30515-03.htm";
					}
				}
				st.takeItems(MANAKIAS_TOTEM, -1);
				st.takeItems(HERMODTS_SKULL, -1);
				for (int drop_id : Medusa_Drops)
				{
					if (st.getQuestItemsCount(drop_id) == 0)
					{
						st.takeItems(drop_id, -1);
					}
				}
				st.giveItems(HERMODTS_REMAINS1, 1);
				st.playSound("Itemsound.quest_middle");
				htmltext = "30515-04.htm";
			}
			else if ((st.getQuestItemsCount(HERMODTS_REMAINS1) == 0) && (st.getQuestItemsCount(HERMODTS_REMAINS2) == 0) && (st.getQuestItemsCount(VENDETTA_TOTEM) == 0))
			{
				htmltext = "30515-01.htm";
			}
			else if ((st.getQuestItemsCount(RACOYS_TOTEM) == 0) && ((st.getQuestItemsCount(KIRUNAS_REMAINS2) > 0) || (st.getQuestItemsCount(WARSPIRIT_TOTEM) > 0) || (st.getQuestItemsCount(BRAKIS_REMAINS2) > 0) || (st.getQuestItemsCount(HERMODTS_REMAINS2) > 0) || (st.getQuestItemsCount(TAMLIN_ORC_HEAD) > 0) || (st.getQuestItemsCount(TONARS_REMAINS2) > 0)))
			{
				htmltext = "30515-05.htm";
			}
		}
		
		if (npcId == Racoy)
		{
			if (st.getQuestItemsCount(RACOYS_TOTEM) > 0)
			{
				if (st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) == 0)
				{
					htmltext = st.getQuestItemsCount(VIVIANTES_LETTER) == 0 ? "30507-03.htm" : "30507-04.htm";
				}
				if (st.getQuestItemsCount(VIVIANTES_LETTER) == 0)
				{
					for (int drop_id : Noble_Ant_Drops)
					{
						if (st.getQuestItemsCount(drop_id) == 0)
						{
							htmltext = "30507-05.htm";
						}
					}
					st.takeItems(RACOYS_TOTEM, -1);
					st.takeItems(INSECT_DIAGRAM_BOOK, -1);
					for (int drop_id : Noble_Ant_Drops)
					{
						if (st.getQuestItemsCount(drop_id) == 0)
						{
							st.takeItems(drop_id, -1);
						}
					}
					st.giveItems(KIRUNAS_REMAINS1, 1);
					st.playSound("Itemsound.quest_middle");
					htmltext = "30507-06.htm";
				}
			}
			else
			{
				if ((st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0) && (st.getQuestItemsCount(KIRUNAS_REMAINS2) == 0) && (st.getQuestItemsCount(VENDETTA_TOTEM) == 0))
				{
					htmltext = "30507-01.htm";
				}
				else
				{
					htmltext = "30507-07.htm";
				}
			}
		}
		
		if (npcId == Vivyan)
		{
			if (st.getQuestItemsCount(RACOYS_TOTEM) > 0)
			{
				if (st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) == 0)
				{
					htmltext = st.getQuestItemsCount(VIVIANTES_LETTER) == 0 ? "30030-01.htm" : "30030-05.htm";
				}
				else if (st.getQuestItemsCount(VIVIANTES_LETTER) == 0)
				{
					htmltext = "30030-06.htm";
				}
			}
			else if ((st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0) && (st.getQuestItemsCount(KIRUNAS_REMAINS2) == 0) && (st.getQuestItemsCount(VENDETTA_TOTEM) == 0))
			{
				htmltext = "30030-07.htm";
			}
		}
		
		if (npcId == Sarien)
		{
			if (st.getQuestItemsCount(RACOYS_TOTEM) > 0)
			{
				if ((st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) == 0) && (st.getQuestItemsCount(VIVIANTES_LETTER) > 0))
				{
					st.takeItems(VIVIANTES_LETTER, -1);
					st.giveItems(INSECT_DIAGRAM_BOOK, 1);
					st.playSound("Itemsound.quest_middle");
					htmltext = "30436-01.htm";
				}
				else if ((st.getQuestItemsCount(VIVIANTES_LETTER) == 0) && (st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) > 0))
				{
					htmltext = "30436-02.htm";
				}
			}
			else if ((st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0) && (st.getQuestItemsCount(KIRUNAS_REMAINS2) == 0) && (st.getQuestItemsCount(VENDETTA_TOTEM) == 0))
			{
				htmltext = "30436-03.htm";
			}
		}
		
		if ((npcId == Ancestor_Martankus) && (st.getQuestItemsCount(WARSPIRIT_TOTEM) > 0))
		{
			htmltext = "30649-01.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		
		if ((npcId == Porta) && (st.getQuestItemsCount(ORIMS_CONTRACT) > 0) && (st.getQuestItemsCount(PORTAS_EYE) < 10))
		{
			st.giveItems(PORTAS_EYE, 1);
			st.playSound(st.getQuestItemsCount(PORTAS_EYE) == 10 ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
		}
		else if ((npcId == Excuro) && (st.getQuestItemsCount(ORIMS_CONTRACT) > 0) && (st.getQuestItemsCount(EXCUROS_SCALE) < 10))
		{
			st.giveItems(EXCUROS_SCALE, 1);
			st.playSound(st.getQuestItemsCount(EXCUROS_SCALE) == 10 ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
		}
		else if ((npcId == Mordeo) && (st.getQuestItemsCount(ORIMS_CONTRACT) > 0) && (st.getQuestItemsCount(MORDEOS_TALON) < 10))
		{
			st.giveItems(MORDEOS_TALON, 1);
			st.playSound(st.getQuestItemsCount(MORDEOS_TALON) == 10 ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
		}
		else if (((npcId == Noble_Ant) || (npcId == Noble_Ant_Leader)) && (st.getQuestItemsCount(RACOYS_TOTEM) > 0))
		{
			List<Integer> drops = new ArrayList<>();
			for (int drop_id : Noble_Ant_Drops)
			{
				if (st.getQuestItemsCount(drop_id) == 0)
				{
					drops.add(drop_id);
				}
			}
			if ((drops.size() > 0) && Rnd.chance(30))
			{
				int drop_id = drops.get(Rnd.get(drops.size()));
				st.giveItems(drop_id, 1);
				st.playSound(drops.size() == 1 ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
			}
			drops.clear();
			drops = null;
		}
		else if (((npcId == Leto_Lizardman_Shaman) || (npcId == Leto_Lizardman_Overlord)) && (st.getQuestItemsCount(PEKIRONS_TOTEM) > 0))
		{
			List<Integer> drops = new ArrayList<>();
			for (int drop_id : Leto_Lizardman_Drops)
			{
				if (st.getQuestItemsCount(drop_id) == 0)
				{
					drops.add(drop_id);
				}
			}
			if ((drops.size() > 0) && Rnd.chance(25))
			{
				int drop_id = drops.get(Rnd.get(drops.size()));
				st.giveItems(drop_id, 1);
				st.playSound(drops.size() == 1 ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
			}
			drops.clear();
			drops = null;
		}
		else if ((npcId == Medusa) && (st.getQuestItemsCount(MANAKIAS_TOTEM) > 0))
		{
			List<Integer> drops = new ArrayList<>();
			for (int drop_id : Medusa_Drops)
			{
				if (st.getQuestItemsCount(drop_id) == 0)
				{
					drops.add(drop_id);
				}
			}
			if ((drops.size() > 0) && Rnd.chance(30))
			{
				int drop_id = drops.get(Rnd.get(drops.size()));
				st.giveItems(drop_id, 1);
				st.playSound((drops.size() == 1) && (st.getQuestItemsCount(HERMODTS_SKULL) > 0) ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
			}
			drops.clear();
			drops = null;
		}
		else if ((npcId == Stenoa_Gorgon_Queen) && (st.getQuestItemsCount(MANAKIAS_TOTEM) > 0) && (st.getQuestItemsCount(HERMODTS_SKULL) == 0) && Rnd.chance(30))
		{
			st.giveItems(HERMODTS_SKULL, 1);
			boolean _allset = true;
			for (int drop_id : Medusa_Drops)
			{
				if (st.getQuestItemsCount(drop_id) == 0)
				{
					_allset = false;
					break;
				}
			}
			st.playSound(_allset ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
		}
		else if (((npcId == Tamlin_Orc) || (npcId == Tamlin_Orc_Archer)) && (st.getQuestItemsCount(VENDETTA_TOTEM) > 0) && (st.getQuestItemsCount(TAMLIN_ORC_HEAD) < 13))
		{
			if (Rnd.chance(npcId == Tamlin_Orc ? 30 : 50))
			{
				st.giveItems(TAMLIN_ORC_HEAD, 1);
				st.playSound(st.getQuestItemsCount(TAMLIN_ORC_HEAD) == 13 ? "Itemsound.quest_middle" : "Itemsound.quest_itemget");
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new _233_TestOfWarspirit(233, qn, "");
	}
}