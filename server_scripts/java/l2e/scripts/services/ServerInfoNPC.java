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
package l2e.scripts.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SpawnsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Based on L2J Eternity-World
 */
public class ServerInfoNPC extends Quest
{
	private final static int NPC = Config.SERVERINFO_NPC_ID;

	private final List<SpawnsHolder> _spawnList = new ArrayList<>();

	public ServerInfoNPC(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(NPC);
		addStartNpc(NPC);
		addTalkId(NPC);
		loadSpawnList();

		if (_spawnList != null)
		{
			for (SpawnsHolder spawn : _spawnList)
			{
				addSpawn(NPC, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0, false);
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		String lang = player.getLang();
		String eventSplit[] = event.split(" ");
		if (eventSplit[0].equalsIgnoreCase("redirect"))
		{
			if (eventSplit[1].equalsIgnoreCase("main"))
			{
				htmltext = pageIndex(player, "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.WELCOME") + " " + player.getName() + ".");
			}
			if (eventSplit[1].equalsIgnoreCase("page"))
			{
				htmltext = pageSub(player, Integer.valueOf(eventSplit[2]));
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext;
		String lang = player.getLang();
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		htmltext = pageIndex(player, "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.WELCOME") + " " + player.getName() + ".");
		return htmltext;
	}

	public String pageIndex(L2PcInstance player, String msg)
	{
		String htmltext = "";
		String lang = player.getLang();
		htmltext += htmlPage(player, "Title");
		htmltext += msg;
		htmltext += "<center><table width=230>";
		if (disablePage(1) == 0)
		{
			htmltext += "<tr><td align=center>" + button("" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.RATE") + "", "redirect page 1 0", 150, 20) + "</td></tr>";
		}
		if (disablePage(2) == 0)
		{
			htmltext += "<tr><td align=center>" + button("" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.CONTACT") + "", "redirect page 2 0", 150, 20) + "</td></tr>";
		}
		if (disablePage(3) == 0)
		{
			htmltext += "<tr><td align=center>" + button("" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.CUSTOM_NPC") + "", "redirect page 3 0", 150, 20) + "</td></tr>";
		}
		if (disablePage(4) == 0)
		{
			htmltext += "<tr><td align=center>" + button("" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.MODS") + "", "redirect page 4 0", 150, 20) + "</td></tr>";
		}
		htmltext += "</table></center>";
		htmltext += htmlPage(player, "Footer");
		return htmltext;
	}

	public String pageSub(L2PcInstance player, int pIndex)
	{
		String htmltext = "", title = "";
		String lang = player.getLang();
		switch (pIndex)
		{
			case 1:
				title = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.SERVER_RATE") + "";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.RATE_XP") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_XP) + "x</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.GROUP_RATE_XP") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_PARTY_XP) + "x</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.RATE_SP") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_SP) + "x</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.GROUP_RATE_SP") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_PARTY_SP) + "x</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.RATE_DROP") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_DROP_ITEMS) + "x</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.RATE_DROP_RB") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_DROP_ITEMS_BY_RAID) + "x</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.RATE_SPOIL") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_DROP_SPOIL) + "x</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.RATE_QUESTS") + " <font color=\"LEVEL\">" + String.valueOf(Config.RATE_QUEST_DROP) + "x</font><br>";
				break;
			case 2:
				title = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.CONTACT_INFO") + "";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.SERVER") + " <font color=\"LEVEL\">" + Config.SERVER_NAME + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.INFO") + " <font color=\"LEVEL\">" + Config.SERVERINFO_NPC_DESCRIPTION + "</font><br>";
				htmltext += "<br>" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.ADMIN") + "<br1>";
				for (String adm : Config.SERVERINFO_NPC_ADM)
				{
					htmltext += "* <font color=\"LEVEL\">" + adm + "</font><br1>";
				}
				htmltext += "<br>" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.GM") + "<br1>";
				for (String gm : Config.SERVERINFO_NPC_GM)
				{
					htmltext += "* <font color=\"LEVEL\">" + gm + "</font><br1>";
				}
				htmltext += "<br>E-mail: <font color=\"LEVEL\">" + Config.SERVERINFO_NPC_EMAIL + "</font><br>";
				if (!Config.SERVERINFO_NPC_PHONE.equalsIgnoreCase("0"))
				{
					htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.PHONE") + " <font color=\"LEVEL\">" + Config.SERVERINFO_NPC_PHONE + "</font><br>";
				}
				break;
			case 3:
				title = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.CUST_NPC") + "";
				for (String npcCustom : Config.SERVERINFO_NPC_CUSTOM)
				{
					htmltext += "* <font color=\"LEVEL\">[" + npcCustom + "]</font><br>";
				}
				break;
			case 4:
				title = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.SERVER_MODS") + "";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.BANK_SYSTEM") + " <font color=\"LEVEL\">" + convBoolean(player, Config.BANKING_SYSTEM_ENABLED) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.VITALITY") + " <font color=\"LEVEL\">" + convBoolean(player, Config.ENABLE_VITALITY) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.CHAMPIONS") + " <font color=\"LEVEL\">" + convBoolean(player, Config.CHAMPION_ENABLE) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.WEDDING") + " <font color=\"LEVEL\">" + convBoolean(player, Config.ALLOW_WEDDING) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.OFFLINE_TRADE") + " <font color=\"LEVEL\">" + convBoolean(player, Config.OFFLINE_TRADE_ENABLE) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.OFFLINE_CRAFT") + " <font color=\"LEVEL\">" + convBoolean(player, Config.OFFLINE_CRAFT_ENABLE) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.TVT") + " <font color=\"LEVEL\">" + convBoolean(player, Config.TVT_EVENT_ENABLED) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.CTF") + " <font color=\"LEVEL\">" + convBoolean(player, Config.CTF_AUTO_MODE) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.BW") + " <font color=\"LEVEL\">" + convBoolean(player, Config.BW_AUTO_MODE) + "</font><br>";
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.TW") + " <font color=\"LEVEL\">" + convBoolean(player, Config.TW_AUTO_MODE) + "</font><br>";
				break;
			default:
				title = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.NOT_FOUND") + "";
				htmltext = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.NOT_FOUND") + "";
				break;
		}
		return showText(player, title, htmltext);
	}

	public int disablePage(int page)
	{
		int p = 0;
		for (String pIndex : Config.SERVERINFO_NPC_DISABLE_PAGE)
		{
			if (pIndex.equalsIgnoreCase(String.valueOf(page)))
			{
				p = 1;
			}
		}
		return p;
	}

	public String convBoolean(L2PcInstance player, Boolean b)
	{
		String lang = player.getLang();
		String text = "<null>";
		if (b)
		{
			text = "<font color=\"00FF00\">" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.ONLINE") + "</font>";
		}
		else
		{
			text = "<font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.OFFLINE") + "</font>";
		}
		return text;
	}

	public String convEnchantMax(L2PcInstance player, int i)
	{
		String lang = player.getLang();
		String text = "<null>";
		if (i == 0)
		{
			text = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.NO_LIMIT") + "";
		}
		else
		{
			text = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.UP_TO") + " +" + String.valueOf(i);
		}
		return text;
	}

	public String htmlPage(L2PcInstance player, String op)
	{
		String lang = player.getLang();
		String texto = "";
		if (op.equals("Title"))
		{
			texto += "<html><body><title>" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.SERVER_INFO") + "</title><center><br>" + "<b><font color=ffcc00>" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.SERVER_INFO") + "</font></b>"
			                + "<br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br></center>";
		}
		else if (op.equals("Footer"))
		{
			texto += "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + "<br><font color=\"303030\">---</font></center></body></html>";
		}
		else
		{
			texto = "" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.NOT_FOUND") + "";
		}
		return texto;
	}

	public String button(String name, String event, int w, int h)
	{
		return "<button value=\"" + name + "\" action=\"bypass -h Quest ServerInfoNPC " + event + "\" " + "width=\"" + Integer.toString(w) + "\" height=\"" + Integer.toString(h) + "\" "
		                + "back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	}

	public String link(String name, String event, String color)
	{
		return "<a action=\"bypass -h Quest ServerInfoNPC " + event + "\">" + "<font color=\"" + color + "\">" + name + "</font></a>";
	}

	public String showText(L2PcInstance player, String title, String text)
	{
		String lang = player.getLang();
		String htmltext = "";
		htmltext += htmlPage(player, "Title");
		htmltext += "<center><font color=\"LEVEL\">" + title + "</font></center><br>";
		htmltext += text + "<br><br>";
		htmltext += "<center>" + button("" + LocalizationStorage.getInstance().getString(lang, "ServerInfoNPC.BACK") + "", "redirect main 1 0", 120, 20) + "</center>";
		htmltext += htmlPage(player, "Footer");
		return htmltext;
	}

	private void loadSpawnList()
	{
		File configFile = new File("data/scripts/services/" + getScriptName() + "/spawnList.xml");
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(configFile);
			Node first = doc.getDocumentElement().getFirstChild();
			for (Node n = first; n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("spawnlist"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("loc"))
						{
							try
							{
								int xPos = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
								int yPos = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
								int zPos = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
								int heading = d.getAttributes().getNamedItem("heading").getNodeValue() != null ? Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue()) : 0;

								if (NpcTable.getInstance().getTemplate(NPC) == null)
								{
									_log.warning(getScriptName() + " script: " + NPC + " is wrong NPC id, NPC was not added in spawnlist");
									continue;
								}

								_spawnList.add(new SpawnsHolder(NPC, new Location(xPos, yPos, zPos, heading)));
							}
							catch (NumberFormatException nfe)
							{
								_log.warning("Wrong number format in config.xml spawnlist block for " + getScriptName() + " script.");
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getScriptName() + " script: error reading " + configFile.getAbsolutePath() + " ! " + e.getMessage(), e);
		}
	}

	public static void main(String[] args)
	{
		new ServerInfoNPC(-1, "ServerInfoNPC", "custom");
	}
}
