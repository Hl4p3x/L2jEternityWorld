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
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SpawnsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Based on L2J Eternity-World
 */
public class Delevel extends Quest
{
	private final static int NPC = Config.DELEVEL_NPC_ID;
	private final static int ADENA = Config.DELEVEL_ITEM_ID;
	private final static int COST1 = Config.DELEVEL_LVL_PRICE;
	private final static int COST2 = Config.DELEVEL_VITALITY_PRICE;
	private final static int MINLVL = 40;
	private final static int KARMA = 0;

	private final List<SpawnsHolder> _spawnList = new ArrayList<>();

	public Delevel(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
		loadSpawnList();

		if (Config.DELEVEL_NPC_ENABLE)
		{
			if (_spawnList != null)
			{
				for (SpawnsHolder spawn : _spawnList)
				{
					addSpawn(NPC, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0, false);
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = event;

		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		String lang = player.getLang();

		final int PRICE1 = COST1 * player.getLevel();
		final int PRICE2 = COST2 * player.getVitalityLevel();
		final int VITALITY = player.getVitalityPoints();

		if (event.equalsIgnoreCase("talk"))
		{
			html.setFile("data/scripts/custom/Delevel/" + lang + "/Done.htm");
			html.replace("%MINLVL%", String.valueOf(MINLVL));
			html.replace("%KARMA%", String.valueOf(KARMA));
			html.replace("%LEVEL%", String.valueOf(player.getLevel()));
			html.replace("%PRICE1%", String.valueOf(PRICE1));
			html.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
			html.replace("%PRICE2%", String.valueOf(PRICE2));
			player.sendPacket(html);
			return null;
		}
		else if (event.equalsIgnoreCase("level"))
		{
			if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.KARMA_ERROR") + ""));
			}
			else if (player.getLevel() < MINLVL)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.SORRY") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.LOW_LEVEL") + ""));
			}
			else if (st.getQuestItemsCount(ADENA) < PRICE1)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.SORRY") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.NO_ADENA") + ""));
			}
			else
			{
				st.takeItems(ADENA, PRICE1);
				player.removeExpAndSp((player.getExp() - ExperienceParser.getInstance().getExpForLevel(player.getStat().getLevel() - 1)), 0);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.CONGRATULATE") + " " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.LVL_UP") + ""));
				html.setFile("data/scripts/custom/Delevel/" + lang + "/Done.htm");
				html.replace("%MINLVL%", String.valueOf(MINLVL));
				html.replace("%KARMA%", String.valueOf(KARMA));
				html.replace("%LEVEL%", String.valueOf(player.getLevel()));
				html.replace("%PRICE1%", String.valueOf(COST1 * player.getLevel()));
				html.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
				html.replace("%PRICE2%", String.valueOf(COST2 * player.getVitalityLevel()));
				player.sendPacket(html);
				return null;
			}
		}

		else if (event.equalsIgnoreCase("vitality"))
		{
			if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.KARMA_ERROR") + ""));
				return "";
			}
			else if (player.getLevel() < MINLVL)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.SORRY") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.LOW_LEVEL") + ""));
				return "";
			}
			else if (st.getQuestItemsCount(ADENA) < PRICE2)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.SORRY") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.NO_ADENA") + ""));
				return "";
			}
			else if (VITALITY < 240)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.SORRY") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.VITALITI_ERROR") + ""));
				return "";
			}
			else if (VITALITY < 2000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(1, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.CONGRATULATE") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.VITALITI_UP") + ""));
			}
			else if (VITALITY < 13000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(241, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.CONGRATULATE") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.VITALITI_UP") + ""));
			}
			else if (VITALITY < 17000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(2001, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.CONGRATULATE") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.VITALITI_UP") + ""));
			}
			else if (VITALITY > 17000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(13001, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "" + LocalizationStorage.getInstance().getString(lang, "Delevel.NAME") + "", "" + LocalizationStorage.getInstance().getString(lang, "Delevel.CONGRATULATE") + ", " + player.getName() + ", " + LocalizationStorage.getInstance().getString(lang, "Delevel.VITALITI_UP") + ""));
			}
			html.setFile("data/scripts/custom/Delevel/" + lang + "/Done.htm");
			html.replace("%MINLVL%", String.valueOf(MINLVL));
			html.replace("%KARMA%", String.valueOf(KARMA));
			html.replace("%LEVEL%", String.valueOf(player.getLevel()));
			html.replace("%PRICE1%", String.valueOf(COST1 * player.getLevel()));
			html.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
			html.replace("%PRICE2%", String.valueOf(COST2 * player.getVitalityLevel()));
			player.sendPacket(html);
			return null;
		}
		st.exitQuest(true);
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		String html = null;
		String lang = player.getLang();
		NpcHtmlMessage _html = new NpcHtmlMessage(npc.getObjectId());

		if (player.getLevel() < MINLVL)
		{
			_html.setFile("data/scripts/custom/Delevel/" + lang + "/Low_level.htm");
			_html.replace("%MINLVL%", String.valueOf(MINLVL));
			player.sendPacket(_html);
		}
		else
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
			html = "Welcome.htm";
		}
		return html;
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
		new Delevel(-1, Delevel.class.getSimpleName(), "custom");
	}
}
