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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import l2e.Config;
import l2e.L2DatabaseFactory;
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

public class PremiumNpc extends Quest
{
	private final int PremiumNpcId = 70011;
	private final int ConsumableItemId = Config.PREMIUM_ID;
	private final int Count = Config.PREMIUM_COUNT;
	protected static int PremiumService;

	private final List<SpawnsHolder> _spawnList = new ArrayList<>();

	public PremiumNpc(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(PremiumNpcId);
		addFirstTalkId(PremiumNpcId);
		addTalkId(PremiumNpcId);
		loadSpawnList();

		if (Config.USE_PREMIUMSERVICE)
		{
			if (_spawnList != null)
			{
				for (SpawnsHolder spawn : _spawnList)
				{
					addSpawn(PremiumNpcId, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0, false);
				}
			}
		}

	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{

		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		htmltext = event;

		if (event.equalsIgnoreCase("getPremium"))
		{
			htmltext = "getpremium.htm";
			return htmltext;
		}

		if (event.equalsIgnoreCase("what"))
		{
			htmltext = "aboutpremium.htm";
			return htmltext;
		}

		if (event.equalsIgnoreCase("back"))
		{
			htmltext = "start.htm";
			return htmltext;
		}

		if (event.equalsIgnoreCase("premium1"))
		{
			getPS(player);
			if (player.getPremiumService() == 1)
			{
				htmltext = "ladylike-no.htm";
				return htmltext;
			}
			else if (st.getQuestItemsCount(ConsumableItemId) >= (Count * 1))
			{
				st.takeItems(ConsumableItemId, Count * 1);
				addPremiumServices(1, player);
				htmltext = "congratulations1.htm";
				return htmltext;
			}
			else
			{
				htmltext = "sorry.htm";
			}
		}

		if (event.equalsIgnoreCase("premium3"))
		{
			getPS(player);
			if (player.getPremiumService() == 1)
			{
				htmltext = "ladylike-no.htm";
				return htmltext;
			}
			else if (st.getQuestItemsCount(ConsumableItemId) >= (Count * 3))
			{
				st.takeItems(ConsumableItemId, Count * 3);
				addPremiumServices(3, player);
				htmltext = "congratulations3.htm";
				return htmltext;
			}
			else
			{
				htmltext = "sorry.htm";
			}
		}

		if (event.equalsIgnoreCase("premium7"))
		{
			getPS(player);
			if (player.getPremiumService() == 1)
			{
				htmltext = "ladylike-no.htm";
				return htmltext;
			}
			else if (st.getQuestItemsCount(ConsumableItemId) >= (Count * 7))
			{
				st.takeItems(ConsumableItemId, Count * 7);
				addPremiumServices(7, player);
				htmltext = "congratulations7.htm";
			}
			else
			{
				htmltext = "sorry.htm";
			}
		}

		if (event.equalsIgnoreCase("premium14"))
		{
			getPS(player);
			if (player.getPremiumService() == 1)
			{
				htmltext = "ladylike-no.htm";
				return htmltext;
			}
			else if (st.getQuestItemsCount(ConsumableItemId) >= (Count * 14))
			{
				st.takeItems(ConsumableItemId, Count * 14);
				addPremiumServices(14, player);
				htmltext = "congratulations14.htm";
			}
			else
			{
				htmltext = "sorry.htm";
			}
		}

		if (event.equalsIgnoreCase("premium30"))
		{
			getPS(player);
			if (player.getPremiumService() == 1)
			{
				htmltext = "ladylike-no.htm";
				return htmltext;
			}
			else if (st.getQuestItemsCount(ConsumableItemId) >= (Count * 30))
			{
				st.takeItems(ConsumableItemId, Count * 30);
				addPremiumServices(30, player);
				htmltext = "congratulations30.htm";
			}
			else
			{
				htmltext = "sorry.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		htmltext = "start.htm";
		return htmltext;
	}

	public static void getPS(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT premium_service FROM character_premium WHERE account_name=?");
			statement.setString(1, player.getAccountName());
			final ResultSet chars = statement.executeQuery();
			PremiumService = chars.getInt("premium_service");
			chars.close();
			statement.close();
		}
		catch (final Exception e)
		{
		}
	}

	public static void addPremiumServices(int Days, L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.DAY_OF_YEAR, Days);

			final PreparedStatement statement = con.prepareStatement("UPDATE character_premium SET premium_service=?,enddate=? WHERE account_name=?");
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (final SQLException e)
		{
			_log.info("EventPremiumNpc:  Could not increase data");
		}
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

								if (NpcTable.getInstance().getTemplate(PremiumNpcId) == null)
								{
									_log.warning(getScriptName() + " script: " + PremiumNpcId + " is wrong NPC id, NPC was not added in spawnlist");
									continue;
								}

								_spawnList.add(new SpawnsHolder(PremiumNpcId, new Location(xPos, yPos, zPos, heading)));
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
		new PremiumNpc(-1, "PremiumNpc", "custom");
	}
}
