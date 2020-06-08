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
import l2e.gameserver.data.sql.CharColorHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.L2Object;
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
public class ColorNameNPC extends Quest
{
	private final static int NPC = Config.COLORNAME_NPC_ID;

	private final List<SpawnsHolder> _spawnList = new ArrayList<>();

	public ColorNameNPC(int questId, String name, String descr)
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
		String paramEvent[] = event.split(" ");
		String action = paramEvent[0];
		String value1 = paramEvent[1];
		String value2 = paramEvent[2];
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (action.equalsIgnoreCase("viewColor"))
		{
			htmltext = viewColor(player, Integer.valueOf(value1), player.getName());
		}
		else if (action.equalsIgnoreCase("buyColor"))
		{
			htmltext = buyColor(player, value1, value2, st);
		}
		else if (action.equalsIgnoreCase("page"))
		{
			htmltext = page(player, player.getLevel());
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return page(player, player.getLevel());
	}

	public String page(L2PcInstance player, int lvl)
	{
		String lang = player.getLang();
		String htmltext = "";
		String _days[] = Config.COLORNAME_NPC_SETTINGS.getDays();
		htmltext += htmlPage(player, "Title");
		if (lvl < Config.COLORNAME_NPC_MIN_LEVEL)
		{
			htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.MIN_LVL") + " " + String.valueOf(Config.COLORNAME_NPC_MIN_LEVEL);
		}
		else
		{
			htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.WELCOME") + "<br>" + "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.SEARCH_COLOR") + "" + "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.NEED_ITEM") + "<br>";
			htmltext += "<center>";
			htmltext += "<table width=\"260\" align=\"center\">";
			htmltext += "<tr>";
			htmltext += "<td width=\"165\" align=\"center\">" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COUNT") + "</td>";
			htmltext += "<td width=\"95\" align=\"center\">" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.DAY") + "</td>";
			htmltext += "</tr>";
			for (String _day : _days)
			{
				int days = Integer.valueOf(_day);
				htmltext += "<tr>";
				htmltext += "<td align=\"left\">";
				for (Integer _itemId : Config.COLORNAME_NPC_SETTINGS.getFeeItems(days).keySet())
				{
					int _count = Config.COLORNAME_NPC_SETTINGS.getFeeItems(days).get(_itemId);
					htmltext += "<font color=\"LEVEL\">" + _count + "</font> " + ItemHolder.getInstance().getTemplate(_itemId).getName() + "<br1>";
				}
				htmltext += "<br><br></td>";
				htmltext += "<td align=\"center\">" + button(_day + " " + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.DAYS") + "", "viewColor " + _day + " 0", 80, 30) + "<br><br></td>";
				htmltext += "</tr>";
			}
			htmltext += "</center>";
		}
		htmltext += htmlPage(player, "Footer");
		return htmltext;
	}

	public String viewColor(L2PcInstance player, int d, String name)
	{
		String lang = player.getLang();
		String htmltext = "";
		htmltext += htmlPage(player, "Title");
		htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COLOR_LIST") + "  " + d + " " + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.DAYS") + ".<br1>" + "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COLOR_BUY") + "<br>" + "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.THX") + "<br>";
		htmltext += "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COUNT") + ":</font><br1>";
		for (Integer _itemId : Config.COLORNAME_NPC_SETTINGS.getFeeItems(d).keySet())
		{
			int _count = Config.COLORNAME_NPC_SETTINGS.getFeeItems(d).get(_itemId);
			htmltext += "<font color=\"LEVEL\">" + _count + "</font> " + ItemHolder.getInstance().getTemplate(_itemId).getName() + "<br1>";
		}
		htmltext += "<br><center>";
		htmltext += "<table>";
		htmltext += "<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"250\" height=\"1\"></td></tr>";
		htmltext += "<tr><td>";
		htmltext += "<table>";
		htmltext += "<tr>";
		htmltext += "<td width=\"200\" align=\"center\"> </td>";
		htmltext += "<td width=\"50\" align=\"center\"></td>";
		htmltext += "</tr>";
		for (String element : Config.COLORNAME_NPC_COLORS)
		{
			htmltext += "<tr><td align=\"left\">";
			htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COLOR") + " - " + link(name, "buyColor " + d + " " + element, element) + "<br1>";
			htmltext += "</td>";
			htmltext += "<td align=\"center\">";
			htmltext += button("" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.SELECT") + "", "buyColor " + d + " " + element, 60, 30);
			htmltext += "</td></tr>";
		}
		htmltext += "<tr><td height=\"3\" align=\"center\"> </td><td height=\"3\" align=\"center\"> </td></tr>";
		htmltext += "</table>";
		htmltext += "</td></tr>";
		htmltext += "<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"250\" height=\"1\"></td></tr>";
		htmltext += "</table><br>";
		htmltext += button("" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.BACK") + "", "page 0 0", 60, 30);
		htmltext += "</center>";
		htmltext += htmlPage(player, "Footer");
		return htmltext;
	}

	public String buyColor(L2PcInstance player, String d, String c, QuestState st)
	{
		String lang = player.getLang();
		String htmltext = "";
		htmltext += htmlPage(player, "Title");
		for (Integer _itemId : Config.COLORNAME_NPC_SETTINGS.getFeeItems(Integer.valueOf(d)).keySet())
		{
			Long _count = Long.valueOf(Config.COLORNAME_NPC_SETTINGS.getFeeItems(Integer.valueOf(d)).get(_itemId));
			if (st.getQuestItemsCount(Integer.valueOf(_itemId)) < _count)
			{
				htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.NO_ITEM") + "<br>";
				htmltext += "<br><center>" + button("" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.BACK") + "", "page 0 0", 60, 30) + "</center>";
				htmltext += htmlPage(player, "Footer");
				return htmltext;
			}
		}
		if (player.getKarma() > 0)
		{
			htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.KARMA") + "<br>";
		}
		else if (player.getPvpFlag() != 0)
		{
			htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COMBAT") + "<br>";
		}
		else if (player.isAttackingNow() == true)
		{
			htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.NO_SAY") + "<br>";
		}
		else
		{
			L2Object target = player;
			player.setTarget(player);
			for (Integer _itemId : Config.COLORNAME_NPC_SETTINGS.getFeeItems(Integer.valueOf(d)).keySet())
			{
				Long _count = Long.valueOf(Config.COLORNAME_NPC_SETTINGS.getFeeItems(Integer.valueOf(d)).get(_itemId));
				st.takeItems(_itemId, _count);
			}
			int color = Integer.decode("0x" + c);
			long time = Long.valueOf(d);
			CharColorHolder.getInstance().add((L2PcInstance) target, color, System.currentTimeMillis(), (time * 24 * 60 * 60 * 1000));
			htmltext += "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COLOR_CHANGE") + "<br>" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.COME_STILL") + "<br>";
		}
		htmltext += "<br><center>" + button("" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.BACK") + "", "page 0 0", 60, 30) + "</center>";
		htmltext += htmlPage(player, "Footer");
		return htmltext;
	}

	public String htmlPage(L2PcInstance player, String op)
	{
		String lang = player.getLang();
		String texto = "";
		if (op == "Title")
		{
			texto += "<html><body><title>" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.TITLE") + "</title><center><br>" + "<b><font color=ffcc00>" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.INFO") + "</font></b>" + "<br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br></center>";
		}
		else if (op == "Footer")
		{
			texto += "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + "<br><font color=\"303030\">---</font></center></body></html>";
		}
		else
		{
			texto = "" + LocalizationStorage.getInstance().getString(lang, "ColorNameNPC.NO_SEARCH") + "";
		}
		return texto;
	}

	public String button(String name, String event, int w, int h)
	{
		return "<button value=\"" + name + "\" action=\"bypass -h Quest ColorNameNPC " + event + "\" " + "width=\"" + Integer.toString(w) + "\" height=\"" + Integer.toString(h) + "\" " + "back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	}

	public String link(String name, String event, String color)
	{
		return "<a action=\"bypass -h Quest ColorNameNPC " + event + "\">" + "<font color=\"" + color + "\">" + name + "</font></a>";
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
		new ColorNameNPC(-1, "ColorNameNPC", "custom");
	}
}
