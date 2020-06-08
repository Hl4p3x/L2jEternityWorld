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
package l2e.scripts.village_master;

import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2VillageMasterInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.ArrayUtils;

/**
 * Created by LordWinter 27.02.2011 Fixed by L2J Eternity-World
 */
public class SubClassCertification extends Quest
{
	private static final String qn = "SubClassCertification";

	private static final int[] NPC =
	{
	                30026, 30031, 30037, 30066, 30070, 30109, 30115, 30120, 30154, 30174, 30175, 30176, 30187,
	                30191, 30195, 30288, 30289, 30290, 30297, 30358, 30373, 30462, 30474, 30498, 30499, 30500,
	                30503, 30504, 30505, 30508, 30511, 30512, 30513, 30520, 30525, 30565, 30594, 30595, 30676,
	                30677, 30681, 30685, 30687, 30689, 30694, 30699, 30704, 30845, 30847, 30849, 30854, 30857,
	                30862, 30865, 30894, 30897, 30900, 30905, 30910, 30913, 31269, 31272, 31276, 31279, 31285,
	                31288, 31314, 31317, 31321, 31324, 31326, 31328, 31331, 31334, 31336, 31755, 31958, 31961,
	                31965, 31968, 31974, 31977, 31996, 32092, 32093, 32094, 32095, 32096, 32097, 32098, 32145,
	                32146, 32147, 32150, 32153, 32154, 32157, 32158, 32160, 32171, 32193, 32199, 32202, 32213,
	                32214, 32221, 32222, 32229, 32230, 32233, 32234
	};

	private static final int[] WARRIORCLASSES =
	{
	                3, 88, 2, 89, 46, 48, 113, 114, 55, 117, 56, 118, 127, 131, 128, 129, 132, 133
	};

	private static final int[] ROGUECLASSES =
	{
	                9, 92, 24, 102, 37, 109, 130, 134, 8, 93, 23, 101, 36, 108
	};

	private static final int[] KNIGHTCLASSES =
	{
	                5, 90, 6, 91, 20, 99, 33, 106
	};

	private static final int[] SUMMONERCLASSES =
	{
	                14, 96, 28, 104, 41, 111
	};

	private static final int[] WIZARDCLASSES =
	{
	                12, 94, 13, 95, 27, 103, 40, 110
	};

	private static final int[] HEALERCLASSES =
	{
	                16, 97, 30, 105, 43, 112
	};

	private static final int[] ENCHANTERCLASSES =
	{
	                17, 98, 21, 100, 34, 107, 51, 115, 52, 116, 135, 136
	};

	private static final int COMMONITEM = 10280;
	private static final int ENHANCEDITEM = 10612;

	private static final int[] CLASSITEMS =
	{
	                10281,
	                10282,
	                10283,
	                10287,
	                10284,
	                10286,
	                10285
	};
	private static final int[] TRANSFORMITEMS =
	{
	                10289,
	                10288,
	                10290,
	                10293,
	                10292,
	                10294,
	                10291
	};

	public SubClassCertification(int id, String name, String descr)
	{
		super(id, name, descr);

		for (int Id : NPC)
		{
			addStartNpc(Id);
			addTalkId(Id);
		}
	}

	private int getClassIndex(L2PcInstance player)
	{
		if (ArrayUtils.isIntInArray(player.getClassId().getId(), WARRIORCLASSES))
		{
			return 0;
		}
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), KNIGHTCLASSES))
		{
			return 1;
		}
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), ROGUECLASSES))
		{
			return 2;
		}
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), ENCHANTERCLASSES))
		{
			return 3;
		}
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), WIZARDCLASSES))
		{
			return 4;
		}
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), SUMMONERCLASSES))
		{
			return 5;
		}
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), HEALERCLASSES))
		{
			return 6;
		}
		return -1;
	}

	private void getCertified(L2PcInstance player, int itemId, String var)
	{
		QuestState st = player.getQuestState(qn);
		String qvar = st.getGlobalQuestVar(var);
		if (!qvar.equals("") && !qvar.equals("0"))
		{
			return;
		}
		L2ItemInstance item = player.getInventory().addItem("Quest", itemId, 1, player, player.getTarget());
		st.saveGlobalQuestVar(var, "" + item.getObjectId());
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(item));
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = event;

		if (event.equals("GetCertified"))
		{
			if (player.isSubClassActive())
			{
				if (((L2VillageMasterInstance) npc).checkVillageMaster(player.getActiveClass()))
				{
					if (player.getLevel() >= 65)
					{
						return "CertificationList.htm";
					}
					return "9002-08.htm";
				}
				return "9002-04.htm";
			}
			return "9002-03.htm";
		}
		else if (event.equals("Obtain65"))
		{
			String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.TRY_TO_RECEIVE") + " %level% " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL")
			                + " %class%, %skilltype%. " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.REMEMBER") + "<br><a action=\"bypass -h Quest SubClassCertification %event%\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.RECEIVE_CERT")
			                + "</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_RECEIVE_CERT") + "</a></body></html>";

			htmltext = html.replace("%level%", "65").replace("%class%", ClassListParser.getInstance().getClass(player.getActiveClass()).getEscapedClientCode()).replace("%skilltype%", "common skill").replace("%event%", "lvl65Emergent");
		}
		else if (event.equals("Obtain70"))
		{
			String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.TRY_TO_RECEIVE") + " %level% " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL")
			                + " %class%, %skilltype%. " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.REMEMBER") + "<br><a action=\"bypass -h Quest SubClassCertification %event%\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.RECEIVE_CERT")
			                + "</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_RECEIVE_CERT") + "</a></body></html>";

			htmltext = html.replace("%level%", "70").replace("%class%", ClassListParser.getInstance().getClass(player.getActiveClass()).getEscapedClientCode()).replace("%skilltype%", "common skill").replace("%event%", "lvl70Emergent");
		}
		else if (event.equals("Obtain75"))
		{
			String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.TRY_TO_RECEIVE") + " %level% " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL")
			                + " %class%, %skilltype%. " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.REMEMBER") + "<br><a action=\"bypass -h Quest SubClassCertification %event1%\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.RECEIVE_CERT_SPECIAL_SKILLS")
			                + "</a><br><a action=\"bypass -h Quest SubClassCertification %event2%\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.RECEIVE_CERT_MASTER_SKILLS") + "</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">"
			                + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_RECEIVE_CERT") + "</a></body></html>";

			htmltext = html.replace("%level%", "75").replace("%class%", ClassListParser.getInstance().getClass(player.getActiveClass()).getEscapedClientCode()).replace("%skilltype%", "common skill or special skill")
			                .replace("%event1%", "lvl75Class").replace("%event2%", "lvl75Master");
		}
		else if ("Obtain80".equals(event))
		{
			String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.TRY_TO_RECEIVE") + " %level% " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL")
			                + " %class%, %skilltype%. " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.REMEMBER") + "<br><a action=\"bypass -h Quest SubClassCertification %event%\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.RECEIVE_CERT")
			                + "</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_RECEIVE_CERT") + "</a></body></html>";

			htmltext = html.replace("%level%", "80").replace("%class%", ClassListParser.getInstance().getClass(player.getActiveClass()).getEscapedClientCode()).replace("%skilltype%", "transformation skill").replace("%event%", "lvl80Class");
		}
		else if (event.startsWith("lvl"))
		{
			int level = Integer.parseInt(event.substring(3, 5));
			String type = event.substring(5);
			String prefix = "-" + player.getClassIndex();

			if (type.equals("Emergent"))
			{
				String isAvailable65 = st.getGlobalQuestVar("EmergentAbility65" + prefix);
				String isAvailable70 = st.getGlobalQuestVar("EmergentAbility70" + prefix);

				if (event.equals("lvl65Emergent"))
				{
					if (isAvailable65.equals("") || isAvailable65.equals("0"))
					{
						if (player.getLevel() > 64)
						{
							getCertified(player, COMMONITEM, "EmergentAbility" + level + prefix);
							return "9002-07.htm";
						}
						String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_READY_RECEIVE") + " %level% "
						                + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL") + ". " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.COME_BACK_LATER") + ".</body></html>";
						htmltext = html.replace("%level%", (event.substring(3, 5)));
						return htmltext;
					}
					return "9002-06.htm";
				}
				else if (event.equals("lvl70Emergent"))
				{
					if (isAvailable70.equals("") || isAvailable70.equals("0"))
					{
						if (player.getLevel() > 69)
						{
							getCertified(player, COMMONITEM, "EmergentAbility" + level + prefix);
							return "9002-07.htm";
						}
						String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_READY_RECEIVE") + " %level% "
						                + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL") + ". " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.COME_BACK_LATER") + ".</body></html>";
						htmltext = html.replace("%level%", (event.substring(3, 5)));
						return htmltext;
					}
					return "9002-06.htm";
				}
			}
			else if (type.equals("Master"))
			{
				String isAvailable = st.getGlobalQuestVar("ClassAbility75" + prefix);
				if (isAvailable.equals("") || isAvailable.equals("0"))
				{
					if (player.getLevel() > 74)
					{
						getCertified(player, ENHANCEDITEM, "ClassAbility" + level + prefix);
						return "9002-07.htm";
					}
					String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_READY_RECEIVE") + " %level% "
					                + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL") + ". " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.COME_BACK_LATER") + ".</body></html>";
					htmltext = html.replace("%level%", (event.substring(3, 5)));
					return htmltext;
				}
				return "9002-06.htm";
			}
			else if (type.equals("Class"))
			{
				if (level == 75)
				{
					String isAvailable = st.getGlobalQuestVar("ClassAbility75" + prefix);
					if (isAvailable.equals("") || isAvailable.equals("0"))
					{
						if (player.getLevel() > 74)
						{
							getCertified(player, CLASSITEMS[getClassIndex(player)], "ClassAbility" + level + prefix);
							return "9002-07.htm";
						}
						String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_READY_RECEIVE") + " %level% "
						                + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL") + ". " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.COME_BACK_LATER") + ".</body></html>";
						htmltext = html.replace("%level%", (event.substring(3, 5)));
						return htmltext;
					}
					return "9002-06.htm";
				}
				else if (level == 80)
				{
					String isAvailable = st.getGlobalQuestVar("ClassAbility80" + prefix);
					if (isAvailable.equals("") || isAvailable.equals("0"))
					{
						if (player.getLevel() > 79)
						{
							getCertified(player, TRANSFORMITEMS[getClassIndex(player)], "ClassAbility" + level + prefix);
							return "9002-07.htm";
						}
						String html = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_OF_SKILLS") + ":<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.NOT_READY_RECEIVE") + " %level% "
						                + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.CERT_LEVEL") + ". " + LocalizationStorage.getInstance().getString(player.getLang(), "SubClassCertification.COME_BACK_LATER") + ".</body></html>";
						htmltext = html.replace("%level%", (event.substring(3, 5)));
						return htmltext;
					}
					return "9002-06.htm";
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		st.set("cond", "0");
		st.setState(State.STARTED);
		return "9002-01.htm";
	}

	public static void main(String[] args)
	{
		new SubClassCertification(-1, qn, "village_master");
	}
}
