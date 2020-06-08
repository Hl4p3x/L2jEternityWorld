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
package l2e.scripts.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import l2e.L2DatabaseFactory;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class KamaAchievements extends Quest
{
	private static final int Pathfinder = 32484;
	
	public KamaAchievements(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(Pathfinder);
		addTalkId(Pathfinder);
		
		Calendar resetTime = Calendar.getInstance();
		resetTime.set(Calendar.HOUR_OF_DAY, 6);
		resetTime.set(Calendar.MINUTE, 30);
		long resetDelay = resetTime.getTimeInMillis();
		if (resetDelay < System.currentTimeMillis())
		{
			resetDelay += 86400000;
		}
		startQuestTimer("cleanKamalokaResults", resetDelay, null, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cleanKamalokaResults"))
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("DELETE FROM kamaloka_results");
				statement.execute();
			}
			catch (Exception e)
			{
				_log.warning("KamaAchievments: Could not empty kamaloka_results table: " + e);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String SCRIPT_PATH = "data/scripts/custom/KamaAchievements/" + player.getLang() + "/";
		
		if (npc.getId() == Pathfinder)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			if (npc.isInsideRadius(18228, 146030, -3088, 500, true, false))
			{
				html.setFile(SCRIPT_PATH + "dion-list.htm");
				html.replace("%REPLACE%", getRimKamalokaPlayerList(2030, 2535, 3040, 1));
			}
			else if (npc.isInsideRadius(-13948, 123819, -3112, 500, true, false))
			{
				html.setFile(SCRIPT_PATH + "gludio-list.htm");
				html.replace("%REPLACE%", getRimKamalokaPlayerList(2030, 2535, 1, 1));
			}
			else if (npc.isInsideRadius(108384, 221614, -3592, 500, true, false))
			{
				html.setFile(SCRIPT_PATH + "heine-list.htm");
				html.replace("%REPLACE%", getRimKamalokaPlayerList(3040, 3545, 4050, 1));
			}
			else if (npc.isInsideRadius(80960, 56455, -1552, 500, true, false))
			{
				html.setFile(SCRIPT_PATH + "oren-list.htm");
				html.replace("%REPLACE%", getRimKamalokaPlayerList(3545, 4050, 4555, 5060));
			}
			else if (npc.isInsideRadius(42674, -47909, -797, 500, true, false))
			{
				html.setFile(SCRIPT_PATH + "rune-list.htm");
				html.replace("%REPLACE%", getRimKamalokaPlayerList(5565, 6070, 6575, 7080));
			}
			else if (npc.isInsideRadius(85894, -142108, -1336, 500, true, false))
			{
				html.setFile(SCRIPT_PATH + "schuttgart-list.htm");
				html.replace("%REPLACE%", getRimKamalokaPlayerList(4555, 5060, 5565, 6070));
			}
			else
			{
				return null;
			}
			player.sendPacket(html);
		}
		return null;
	}
	
	private String getRimKamalokaPlayerList(int a, int b, int c, int d)
	{
		String list = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT char_name FROM kamaloka_results WHERE Level IN (?, ?, ?, ?) ORDER BY Grade DESC, Count DESC");
			statement.setInt(1, a);
			statement.setInt(2, b);
			statement.setInt(3, c);
			statement.setInt(4, d);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				list = list + "---" + rset.getString("char_name") + "---<br>";
			}
		}
		catch (Exception e)
		{
			_log.warning("KamaAchievments: Could not empty kamaloka_results table: " + e);
		}
		return list;
	}
	
	public static void main(String[] args)
	{
		new KamaAchievements(-1, "KamaAchievements", "custom");
	}
}