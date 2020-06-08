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
package l2e.gameserver.data.xml;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2AioTeleport;
import l2e.gameserver.model.actor.tasks.aio.StateUpdate;
import l2e.gameserver.model.entity.mods.aio.main.PlayersTopData;

public final class AIOItemParser extends DocumentParser
{
	protected static final Logger _log = Logger.getLogger(AIOItemParser.class.getName());
	
	private final Map<Integer, L2AioTeleport> _teleports = new HashMap<>();
	private static FastList<PlayersTopData> _topPvp = new FastList<>();
	private static FastList<PlayersTopData> _topPk = new FastList<>();
	private static FastList<PlayersTopData> _topClan = new FastList<>();
	
	protected AIOItemParser()
	{
		if (Config.ENABLE_AIO_NPCS)
		{
			load();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StateUpdate(), 1000, 3 * 60 * 1000);
		}
	}
	
	@Override
	public void load()
	{
		_teleports.clear();
		parseDatapackFile("data/stats/AioTeleports.xml");
		_log.info(getClass().getSimpleName() + ": Loaded functions successfully.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node list = getCurrentDocument().getFirstChild().getFirstChild(); list != null; list = list.getNextSibling())
		{
			if (list.getNodeName().equalsIgnoreCase("teleport"))
			{
				NamedNodeMap node = list.getAttributes();
				
				L2AioTeleport teleport = new L2AioTeleport();
				teleport.setTeleId(Integer.valueOf(node.getNamedItem("id").getNodeValue()));
				teleport.setLocX(Integer.valueOf(node.getNamedItem("locX").getNodeValue()));
				teleport.setLocY(Integer.valueOf(node.getNamedItem("locY").getNodeValue()));
				teleport.setLocZ(Integer.valueOf(node.getNamedItem("locZ").getNodeValue()));
				_teleports.put(teleport.getTeleId(), teleport);
			}
		}
	}
	
	public void loadTopPvp()
	{
		_topPvp.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT ch.char_name,ch.pvpkills,ch.clanid,cl.clan_name FROM characters ch, clan_data cl WHERE cl.clan_id=ch.clanid ORDER BY ch.pvpkills DESC LIMIT 30"))
		{
			PlayersTopData playerData;
			while (rs.next())
			{
				playerData = new PlayersTopData();
				playerData.setCharName(rs.getString("char_name"));
				playerData.setClanName(rs.getString("clan_name"));
				playerData.setPvp(rs.getInt("pvpkills"));
				_topPvp.add(playerData);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Could`t gather needed info from database for PvP Top:", e);
		}
	}
	
	public void loadTopPk()
	{
		_topPk.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT ch.char_name,ch.pkkills,ch.clanid,cl.clan_name FROM characters ch, clan_data cl WHERE cl.clan_id=ch.clanid ORDER BY ch.pkkills DESC LIMIT 30"))
		{
			PlayersTopData playerData;
			while (rs.next())
			{
				playerData = new PlayersTopData();
				playerData.setCharName(rs.getString("char_name"));
				playerData.setClanName(rs.getString("clan_name"));
				playerData.setPk(rs.getInt("pkkills"));
				_topPk.add(playerData);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Could`t gather needed info from database for Pk Top:", e);
		}
	}
	
	public void loadTopClan()
	{
		_topClan.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT ch.clan_name,ch.clan_level,ch.leader_id,cl.char_name FROM clan_data ch, characters cl WHERE cl.charId=ch.leader_id ORDER BY ch.clan_level DESC LIMIT 30"))
		{
			PlayersTopData playerData;
			while (rs.next())
			{
				playerData = new PlayersTopData();
				playerData.setCharName(rs.getString("char_name"));
				playerData.setClanName(rs.getString("clan_name"));
				playerData.setClanLevel(rs.getInt("clan_level"));
				_topClan.add(playerData);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Could`t gather needed info from database for Clan Top:", e);
		}
	}
	
	public FastList<PlayersTopData> getTopPvp()
	{
		return _topPvp;
	}
	
	public FastList<PlayersTopData> getTopPk()
	{
		return _topPk;
	}
	
	public FastList<PlayersTopData> getTopClan()
	{
		return _topPk;
	}
	
	public Map<Integer, L2AioTeleport> getTeleports()
	{
		return _teleports;
	}
	
	public L2AioTeleport getTeleportId(int id)
	{
		return _teleports.get(id);
	}
	
	public static AIOItemParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AIOItemParser _instance = new AIOItemParser();
	}
}