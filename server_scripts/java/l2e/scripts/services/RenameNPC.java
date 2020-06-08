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
import l2e.gameserver.communitybbs.Manager.RegionBBSManager;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SpawnsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.PartySmallWindowAll;
import l2e.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2e.gameserver.util.Util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Based on L2J Eternity-World
 */
public class RenameNPC extends Quest
{
	private final static int NPC = Config.RENAME_NPC_ID;

	private final List<SpawnsHolder> _spawnList = new ArrayList<>();

	public RenameNPC(int questId, String name, String descr)
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
		String lang = player.getLang();
		String htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.NEW_NAME") + "<br1><edit var=\"newname\" width=70 height=10>";
		String eventSplit[] = event.split(" ");
		QuestState st = player.getQuestState(getName());

		if (eventSplit[0].equalsIgnoreCase("rename"))
		{
			player.setTarget(player);
			if (eventSplit.length != 2)
			{
				htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.ENTER_NAME") + "";
			}
			else if (player.getLevel() < Config.RENAME_NPC_MIN_LEVEL)
			{
				htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.MIN_LVL") + " " + String.valueOf(Config.RENAME_NPC_MIN_LEVEL);
			}
			else if (validItemFee(st))
			{
				htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.NO_ITEM") + "";
			}
			else if ((eventSplit[1].length() < 1) || (eventSplit[1].length() > 16))
			{
				htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.MAX_LETTER") + "";
			}
			else if (!Util.isAlphaNumeric(eventSplit[1]))
			{
				htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.LETTER_AND_DIGIT") + "";
			}
			else if (CharNameHolder.getInstance().doesCharNameExist(eventSplit[1]))
			{
				htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.NAME_ALREADY_USE") + "";
			}
			else
			{
				try
				{
					L2World.getInstance().removeFromAllPlayers(player);
					player.setName(eventSplit[1]);
					player.store();
					L2World.getInstance().addToAllPlayers(player);
					htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.RENAME_SUH") + "";
					player.broadcastUserInfo();

					String itemFeeSplit[] = Config.RENAME_NPC_FEE.split("\\;");
					for (String anItemFeeSplit : itemFeeSplit)
					{
						String item[] = anItemFeeSplit.split("\\,");
						st.takeItems(Integer.parseInt(item[0]), Integer.parseInt(item[1]));
					}

					if (player.isInParty())
					{
						player.getParty().broadcastToPartyMembers(player, PartySmallWindowDeleteAll.STATIC_PACKET);
						for (L2PcInstance member : player.getParty().getMembers())
						{
							if (member != player)
							{
								member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
							}
						}
					}
					if (player.getClan() != null)
					{
						player.getClan().broadcastClanStatus();
					}
					RegionBBSManager.getInstance().changeCommunityBoard(player);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					htmltext = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.SERVICE_DISABLE") + "";
				}
			}
			return (page(player, htmltext, 1));
		}
		return (page(player, htmltext, 0));
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String lang = player.getLang();
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		htmltext = page(player, "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.NEW_NAME") + "<br1><edit var=\"newname\" width=70 height=10>", 0);
		return htmltext;
	}

	public String page(L2PcInstance player, String msg, int t)
	{
		String lang = player.getLang();
		String htmltext = "";
		htmltext += htmlPage(player, "Title");
		htmltext += "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.WELCOME") + "<br>" + "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.ENTER_NAME_NOW") + "<br1>";
		String itemFeeSplit[] = Config.RENAME_NPC_FEE.split("\\;");
		for (String anItemFeeSplit : itemFeeSplit)
		{
			String item[] = anItemFeeSplit.split("\\,");
			htmltext += "<font color=\"LEVEL\">" + item[1] + " " + ItemHolder.getInstance().getTemplate(Integer.parseInt(item[0])).getName() + "</font><br1>";
		}
		if (t == 0)
		{
			htmltext += "<br><font color=\"339966\">" + msg + "</font>";
			htmltext += "<br><center>" + button("" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.RENAME") + "", "rename $newname", 100, 23) + "</center>";
		}
		else
		{
			htmltext += "<br><font color=\"FF0000\">" + msg + "</font>";
			htmltext += "<br><center>" + button("" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.BACK") + "", "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.START") + "", 70, 23) + "</center>";
		}
		htmltext += htmlPage(player, "Footer");
		return htmltext;
	}

	public Boolean validItemFee(QuestState st)
	{
		String itemFeeSplit[] = Config.RENAME_NPC_FEE.split("\\;");
		for (String anItemFeeSplit : itemFeeSplit)
		{
			String item[] = anItemFeeSplit.split("\\,");
			if (st.getQuestItemsCount(Integer.parseInt(item[0])) < Integer.parseInt(item[1]))
			{
				return true;
			}
		}
		return false;
	}

	public String htmlPage(L2PcInstance player, String op)
	{
		String lang = player.getLang();
		String texto = "";
		if (op.equals("Title"))
		{
			texto += "<html><body><title>" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.RENAME_MANAGER") + "</title><center><br>" + "<b><font color=ffcc00>" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.INFO") + "</font></b>" + "<br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br></center>";
		}
		else if (op.equals("Footer"))
		{
			texto += "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + "<br><font color=\"303030\">---</font></center></body></html>";
		}
		else
		{
			texto = "" + LocalizationStorage.getInstance().getString(lang, "RenameNPC.NOT_FOUND") + "";
		}
		return texto;
	}

	public String button(String name, String event, int w, int h)
	{
		return "<button value=\"" + name + "\" action=\"bypass -h Quest RenameNPC " + event + "\" " + "width=\"" + Integer.toString(w) + "\" height=\"" + Integer.toString(h) + "\" " + "back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	}

	public String link(String name, String event, String color)
	{
		return "<a action=\"bypass -h Quest RenameNPC " + event + "\">" + "<font color=\"" + color + "\">" + name + "</font></a>";
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
		new RenameNPC(-1, "RenameNPC", "custom");
	}
}
