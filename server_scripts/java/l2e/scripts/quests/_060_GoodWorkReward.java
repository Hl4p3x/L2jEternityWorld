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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class _060_GoodWorkReward extends Quest
{
	private static final String qn = "_060_GoodWorkReward";

	// NPCs
	private final static int DAEGER = 31435;
	private final static int HELVETIA = 30081;
	private final static int BLACK_MARKETEER_OF_MAMMON = 31092;
	private final static int MARK = 32487;

	// Mobs
	private final static int PURSUER = 27340;

	// Items
	private final static int ADENA = 57;
	private final static int BLOODY_CLOTH_FRAGMENT = 10867;
	private final static int HELVETIA_ANTIDOTE = 10868;
	public L2Npc Pursuer;

	private final int[] TALK_NPC =
	{
	                DAEGER,
	                HELVETIA,
	                BLACK_MARKETEER_OF_MAMMON,
	                MARK
	};

	private static final HashMap<String, String> user_variables = new HashMap<>();

	public _060_GoodWorkReward(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(DAEGER);
		for (int npcId : TALK_NPC)
		{
			addTalkId(npcId);
		}
		addKillId(PURSUER);

		questItemIds = new int[]
		{
		                BLOODY_CLOTH_FRAGMENT,
		                HELVETIA_ANTIDOTE
		};
	}

	private void spawnPursuer(QuestState st)
	{
		Pursuer = st.addSpawn(PURSUER, 72590, 148100, -3312, getRandom(0, 20), true, 1800000);
		Pursuer.broadcastPacket(new NpcSay(Pursuer.getObjectId(), 0, Pursuer.getId(), NpcStringId.S1_I_MUST_KILL_YOU_BLAME_YOUR_OWN_CURIOSITY));
		Pursuer.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer());
	}

	private void despawnPursuer(QuestState st)
	{
		if (Pursuer != null)
		{
			Pursuer.deleteMe();
		}
		Pursuer = null;
	}

	protected L2Npc IsPursuerSpawned(int npcId)
	{
		Collection<L2Spawn> spawnTable = SpawnTable.getInstance().getSpawnTable();
		L2Spawn tSpawn = null;
		for (Iterator<L2Spawn> i = spawnTable.iterator(); i.hasNext();)
		{
			tSpawn = i.next();
			if (tSpawn.getTemplate()._npcId == npcId)
			{
				return tSpawn.getLastSpawn();
			}
		}
		return null;
	}

	private static final FastMap<Integer, String> profs = new FastMap<>();
	private static final FastMap<String, int[]> classes = new FastMap<>();

	static
	{
		profs.put(1, "<a action=\"bypass -h Quest " + qn + " classes-WL\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.3") + "</a><br>" + "<a action=\"bypass -h Quest " + qn + " classes-GL\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.2") + "</a>");
		profs.put(4, "<a action=\"bypass -h Quest " + qn + " classes-PA\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.5") + "</a><br" + "><a action=\"bypass -h Quest " + qn + " classes-DA\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.6") + "</a>");
		profs.put(7, "<a action=\"bypass -h Quest " + qn + " classes-TH\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.8") + "</a><br>" + "<a action=\"bypass -h Quest " + qn + " classes-HK\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.9") + "</a>");
		profs.put(11, "<a action=\"bypass -h Quest " + qn + " classes-SC\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.12") + "</a><br>" + "<a action=\"bypass -h Quest " + qn + " classes-NM\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.13") + "</a><br>" + "<a action=\"bypass -h Quest " + qn + " classes-WA\">"
		                + LocalizationStorage.getInstance().getString(getLang(), "ClassName.14") + "</a>");
		profs.put(15, "<a action=\"bypass -h Quest " + qn + " classes-BS\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.16") + "</a><br>" + "<a action=\"bypass -h Quest " + qn + " classes-PP\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.17") + "</a>");
		profs.put(19, "<a action=\"bypass -h Quest " + qn + " classes-TK\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.20") + "</a>" + "<br><a action=\"bypass -h Quest " + qn + " classes-SS\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.21") + "</a>");
		profs.put(22, "<a action=\"bypass -h Quest " + qn + " classes-PW\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.23") + "</a>" + "<br><a action=\"bypass -h Quest " + qn + " classes-SR\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.24") + "</a>");
		profs.put(26, "<a action=\"bypass -h Quest " + qn + " classes-SP\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.27") + "</a>" + "<br><a action=\"bypass -h Quest " + qn + " classes-ES\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.28") + "</a>");
		profs.put(29, "<a action=\"bypass -h Quest " + qn + " classes-EE\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.30") + "</a>");
		profs.put(32, "<a action=\"bypass -h Quest " + qn + " classes-SK\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.33") + "</a>" + "<br><a action=\"bypass -h Quest " + qn + " classes-BD\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.34") + "</a>");
		profs.put(35, "<a action=\"bypass -h Quest " + qn + " classes-AW\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.36") + "</a>" + "<br><a action=\"bypass -h Quest " + qn + " classes-PR\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.37") + "</a>");
		profs.put(39, "<a action=\"bypass -h Quest " + qn + " classes-SH\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.40") + "</a>" + "<br><a action=\"bypass -h Quest " + qn + " classes-PS\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.41") + "</a>");
		profs.put(42, "<a action=\"bypass -h Quest " + qn + " classes-SE\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.43") + "</a>");
		profs.put(45, "<a action=\"bypass -h Quest " + qn + " classes-DT\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.46") + "</a>");
		profs.put(47, "<a action=\"bypass -h Quest " + qn + " classes-TR\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.48") + "</a>");
		profs.put(50, "<a action=\"bypass -h Quest " + qn + " classes-OL\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.51") + "</a><br>" + "<a action=\"bypass -h Quest " + qn + " classes-WC\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.52") + "</a>");
		profs.put(54, "<a action=\"bypass -h Quest " + qn + " classes-BH\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.55") + "</a>");
		profs.put(56, "<a action=\"bypass -h Quest " + qn + " classes-WS\">" + LocalizationStorage.getInstance().getString(getLang(), "ClassName.57") + "</a>");

		classes.put("AW", new int[]
		{
		                2673,
		                3172,
		                2809
		});
		classes.put("BD", new int[]
		{
		                2627,
		                3172,
		                2762
		});
		classes.put("BH", new int[]
		{
		                2809,
		                3119,
		                3238
		});
		classes.put("BS", new int[]
		{
		                2721,
		                2734,
		                2820
		});
		classes.put("DA", new int[]
		{
		                2633,
		                2734,
		                3307
		});
		classes.put("DT", new int[]
		{
		                2627,
		                3203,
		                3276
		});
		classes.put("EE", new int[]
		{
		                2721,
		                3140,
		                2820
		});
		classes.put("ES", new int[]
		{
		                2674,
		                3140,
		                3336
		});
		classes.put("GL", new int[]
		{
		                2627,
		                2734,
		                2762
		});
		classes.put("HK", new int[]
		{
		                2673,
		                2734,
		                3293
		});
		classes.put("NM", new int[]
		{
		                2674,
		                2734,
		                3307
		});
		classes.put("OL", new int[]
		{
		                2721,
		                3203,
		                3390
		});
		classes.put("PA", new int[]
		{
		                2633,
		                2734,
		                2820
		});
		classes.put("PP", new int[]
		{
		                2721,
		                2734,
		                2821
		});
		classes.put("PR", new int[]
		{
		                2673,
		                3172,
		                3293
		});
		classes.put("PS", new int[]
		{
		                2674,
		                3172,
		                3336
		});
		classes.put("PW", new int[]
		{
		                2673,
		                3140,
		                2809
		});
		classes.put("SC", new int[]
		{
		                2674,
		                2734,
		                2840
		});
		classes.put("SE", new int[]
		{
		                2721,
		                3172,
		                2821
		});
		classes.put("SH", new int[]
		{
		                2674,
		                3172,
		                2840
		});
		classes.put("SK", new int[]
		{
		                2633,
		                3172,
		                3307
		});
		classes.put("SP", new int[]
		{
		                2674,
		                3140,
		                2840
		});
		classes.put("SR", new int[]
		{
		                2673,
		                3140,
		                3293
		});
		classes.put("SS", new int[]
		{
		                2627,
		                3140,
		                2762
		});
		classes.put("TH", new int[]
		{
		                2673,
		                2734,
		                2809
		});
		classes.put("TK", new int[]
		{
		                2633,
		                3140,
		                2820
		});
		classes.put("TR", new int[]
		{
		                2627,
		                3203,
		                2762
		});
		classes.put("WA", new int[]
		{
		                2674,
		                2734,
		                3336
		});
		classes.put("WC", new int[]
		{
		                2721,
		                3203,
		                2879
		});
		classes.put("WL", new int[]
		{
		                2627,
		                2734,
		                3276
		});
		classes.put("WS", new int[]
		{
		                2867,
		                3119,
		                3238
		});
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

		final int cond = st.getInt("cond");

		if (event.equalsIgnoreCase("31435-03.htm"))
		{
			if (st.getState() == State.CREATED)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("32487-02.htm"))
		{
			if (cond == 1)
			{
				despawnPursuer(st);
				spawnPursuer(st);
				Pursuer.setRunning();
				((L2Attackable) Pursuer).addDamageHate(player, 0, 999);
				Pursuer.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		else if (event.equalsIgnoreCase("31435-05.htm"))
		{
			if (cond == 3)
			{
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30081-03.htm"))
		{
			if (cond == 4)
			{
				if (st.getQuestItemsCount(BLOODY_CLOTH_FRAGMENT) < 1)
				{
					return "30081-03a.htm";
				}
				st.takeItems(BLOODY_CLOTH_FRAGMENT, -1);
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30081-05.htm"))
		{
			if (cond == 5)
			{
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30081-08.htm"))
		{
			if ((cond == 5) || (cond == 6))
			{
				if (st.getQuestItemsCount(ADENA) < 3000000)
				{
					st.set("cond", "6");
					st.playSound("ItemSound.quest_middle");
					return "30081-07.htm";
				}
				st.takeItems(ADENA, 3000000);
				st.giveItems(HELVETIA_ANTIDOTE, 1);
				st.set("cond", "7");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("32487-06.htm"))
		{
			if (cond == 7)
			{
				if (st.getQuestItemsCount(HELVETIA_ANTIDOTE) < 1)
				{
					return "32487-06a.htm";
				}
				st.takeItems(HELVETIA_ANTIDOTE, -1);
				st.set("cond", "8");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31435-08.htm"))
		{
			if (cond == 8)
			{
				st.set("cond", "9");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31092-05.htm"))
		{
			if ((cond == 10) && profs.containsKey(player.getClassId().getId()))
			{
				return htmltext = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "060quest.CHANGE_CLASS") + "<br>" + profs.get(player.getClassId().getId()) + "</body></html>";
			}
		}
		else if (event.startsWith("classes-"))
		{
			if (cond == 10)
			{
				String occupation = event.replaceAll("classes-", "");
				int[] _classes = classes.get(occupation);
				if (_classes == null)
				{
					return "Error id: " + occupation;
				}

				int adena = 0;
				for (int mark : _classes)
				{
					if (st.getQuestItemsCount(mark) > 0)
					{
						adena = 1;
					}
					else
					{
						st.giveItems(mark, 1);
					}
				}

				if (adena > 0)
				{
					st.giveItems(ADENA, adena * 1000000);
				}
				st.unset("cond");
				st.setState(State.COMPLETED);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				return "31092-06.htm";
			}
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

		final int cond = st.getInt("cond");
		final int npcId = npc.getId();
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == DAEGER)
				{
					if ((player.getLevel() < 39) || (player.getRace() == Race.Kamael) || (player.getClassId().level() != 1))
					{
						st.exitQuest(true);
						htmltext = "31435-00.htm";
					}
					else
					{
						htmltext = "31435-01.htm";
					}
				}
				break;
			case State.STARTED:
				switch (npcId)
				{
					case DAEGER:
						switch (cond)
						{
							case 1:
							case 2:
								htmltext = "31435-03.htm";
								break;
							case 3:
								htmltext = "31435-04.htm";
								break;
							case 4:
							case 5:
							case 6:
							case 7:
								htmltext = "31435-06.htm";
								break;
							case 8:
								htmltext = "31435-07.htm";
								break;
							case 9:
								st.set("cond", "10");
								st.playSound("ItemSound.quest_middle");
								htmltext = "31435-09.htm";
								break;
							default:
								if (cond > 9)
								{
									htmltext = "31435-10.htm";
								}
								break;
						}
						break;
					case HELVETIA:
						switch (cond)
						{
							case 4:
								htmltext = "30081-01.htm";
								break;
							case 5:
								htmltext = "30081-04.htm";
								break;
							case 6:
								htmltext = "30081-06.htm";
								break;
							default:
								if (cond > 6)
								{
									htmltext = "30081-09.htm";
								}
								break;
						}
						break;
					case BLACK_MARKETEER_OF_MAMMON:
						switch (cond)
						{
							case 10:
								htmltext = "31092-01.htm";
								break;
						}
						break;
					case MARK:
						switch (cond)
						{
							case 1:
								htmltext = "32487-01.htm";
								break;
							case 2:
								st.set("cond", "3");
								st.playSound("ItemSound.quest_middle");
								htmltext = "32487-03.htm";
								break;
							case 3:
								htmltext = "32487-04.htm";
								break;
							case 7:
								htmltext = "32487-05.htm";
								break;
							default:
								if (cond > 7)
								{
									htmltext = "32487-06.htm";
								}
								break;
						}
						break;
				}

				break;
		}

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}

		if ((npc.getId() == PURSUER) && (st.getInt("cond") == 1))
		{
			if (getRandom(100) < 50)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOU_ARE_STRONG_THIS_WAS_A_MISTAKE));
				return null;
			}
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOU_HAVE_GOOD_LUCK_I_SHALL_RETURN));
			st.set("cond", "2");
			st.giveItems(BLOODY_CLOTH_FRAGMENT, 1);
			st.playSound("ItemSound.quest_middle");
		}

		return null;
	}

	private static String getLang()
	{
		return Config.MULTILANG_ENABLE && (getVar("lang@") != null) ? getVar("lang@") : "en";
	}

	private static String getVar(String name)
	{
		return user_variables.get(name);
	}

	public static void main(String[] args)
	{
		new _060_GoodWorkReward(60, qn, "");
	}
}
