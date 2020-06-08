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
package l2e.scripts.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter 31.08.2012 Fixed by L2J Eternity-World
 */
public class AngelCat extends Event
{
	private static final String event = "AngelCat";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	
	private final int _angelCat = 4308;
	private final int _gift = 21726;
	private final int _amount = 1;
	
	private static final Location[] _coords =
	{
		new Location(148557, 26745, -2200, 32768),
		new Location(147476, -55385, -2728, 49151),
		new Location(148188, -55849, -2776, 61439),
		new Location(85628, -142429, -1344, 0),
		new Location(86891, -142848, -1336, 26000),
		new Location(43908, -47714, -792, 49999),
		new Location(43104, -48456, -792, 17000),
		new Location(82002, 53771, -1488, 0),
		new Location(81086, 56062, -1552, 32768),
		new Location(81743, 146431, -3528, 32768),
		new Location(82984, 149343, -3464, 44000),
		new Location(82156, 148669, -3464, 0),
		new Location(16117, 142909, -2696, 16000),
		new Location(17307, 145044, -3040, 25000),
		new Location(111057, 218934, -3536, 16384),
		new Location(-13777, 122114, -2984, 16384),
		new Location(-14081, 123829, -3112, 40959),
		new Location(-83131, 150956, -3120, 0),
		new Location(-81006, 149991, -3040, 0),
		new Location(-84034, 243107, -3728, 4096),
		new Location(-84365, 244848, -3728, 57343),
		new Location(9926, 16263, -4568, 62999),
		new Location(11482, 17600, -4584, 46900),
		new Location(-45299, -114093, -240, 16384),
		new Location(-45272, -112700, -240, 0),
		new Location(46869, 50913, -2992, 8192),
		new Location(115100, -178306, -880, 0),
		new Location(147265, 25624, -2008, 16384)
	};
	
	public AngelCat(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_angelCat);
		addFirstTalkId(_angelCat);
		addTalkId(_angelCat);
		
		restoreStatus();
	}
	
	@Override
	public boolean eventStart()
	{
		if (_isactive)
		{
			return false;
		}
		_isactive = true;
		_npclist = new FastList<>();
		
		for (Location loc : _coords)
		{
			recordSpawn(_angelCat, loc, false, 0);
		}

		CustomMessage msg = new CustomMessage("EventAngelCat.START", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(true);
		
		return true;
	}
	
	@Override
	public boolean eventStop()
	{
		if (!_isactive)
		{
			return false;
		}
		_isactive = false;
		
		if (!_npclist.isEmpty())
		{
			for (L2Npc _npc : _npclist)
			{
				if (_npc != null)
				{
					_npc.deleteMe();
				}
			}
		}
		_npclist.clear();
		

		CustomMessage msg = new CustomMessage("EventAngelCat.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String var = GlobalVariablesManager.getInstance().getStoredVariable("AngelCat-" + player.getAccountName());
		if (var == null)
		{
			player.addItem("AngelCat-Gift", _gift, _amount, npc, true);
			GlobalVariablesManager.getInstance().storeVariable("AngelCat-" + player.getAccountName(), String.valueOf(player.getName()));
		}
		else
		{
			player.sendPacket(SystemMessage.getSystemMessage(3289));
		}
		return super.onTalk(npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	private L2Npc recordSpawn(int npcId, Location loc, boolean randomOffSet, long despawnDelay)
	{
		L2Npc _tmp = addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffSet, despawnDelay);
		if (_tmp != null)
		{
			_npclist.add(_tmp);
		}
		return _tmp;
	}
	
	private void restoreStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int status = 0;
			String event_name = event;
			PreparedStatement statement = con.prepareStatement(UPDATE_STATUS);
			statement.setString(1, event_name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				status = rset.getInt("status");
			}
			rset.close();
			statement.close();
			if (status > 0)
			{
				eventStart();
			}
			else
			{
				eventStop();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: Could not restore custom event data info: " + e);
		}
	}
	
	private void updateStatus(boolean newEvent)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			String event_name = event;
			boolean insert = newEvent;
			
			PreparedStatement stmt = con.prepareStatement(insert ? EVENT_INSERT : EVENT_DELETE);
			
			if (newEvent)
			{
				stmt.setString(1, event_name);
				stmt.setInt(2, 1);
				stmt.execute();
				stmt.close();
			}
			else
			{
				stmt.setInt(1, newEvent ? 0 : 0);
				stmt.setString(2, event_name);
				stmt.execute();
				stmt.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: could not update custom event database!");
		}
	}
	
	public static void main(String[] args)
	{
		new AngelCat(-1, "AngelCat", "events");
	}
}