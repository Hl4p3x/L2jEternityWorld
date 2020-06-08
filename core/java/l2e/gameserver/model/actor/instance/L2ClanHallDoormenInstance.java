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
package l2e.gameserver.model.actor.instance;

import java.util.Arrays;
import java.util.StringTokenizer;

import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Evolve;

public class L2ClanHallDoormenInstance extends L2DoormenInstance
{
	private volatile boolean _init = false;
	private ClanHall _clanHall = null;
	private boolean _hasEvolve = false;
	
	private static final int[] CH_WITH_EVOLVE =
	{
		36,
		37,
		38,
		39,
		40,
		41,
		51,
		52,
		53,
		54,
		55,
		56,
		57
	};
	
	public L2ClanHallDoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2ClanHallDoormenInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (_hasEvolve && command.startsWith("evolve"))
		{
			if (isOwnerClan(player))
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				if (st.countTokens() < 2)
				{
					return;
				}
				
				st.nextToken();
				boolean ok = false;
				switch (Integer.parseInt(st.nextToken()))
				{
					case 1:
						ok = Evolve.doEvolve(player, this, 9882, 10307, 55);
						break;
					case 2:
						ok = Evolve.doEvolve(player, this, 4422, 10308, 55);
						break;
					case 3:
						ok = Evolve.doEvolve(player, this, 4423, 10309, 55);
						break;
					case 4:
						ok = Evolve.doEvolve(player, this, 4424, 10310, 55);
						break;
					case 5:
						ok = Evolve.doEvolve(player, this, 10426, 10611, 70);
						break;
				}
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (ok)
				{
					html.setFile(player.getLang(), "data/html/clanHallDoormen/evolve-ok.htm");
				}
				else
				{
					html.setFile(player.getLang(), "data/html/clanHallDoormen/evolve-no.htm");
				}
				player.sendPacket(html);
				return;
			}
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (getClanHall() != null)
		{
			L2Clan owner = ClanHolder.getInstance().getClan(getClanHall().getOwnerId());
			if (isOwnerClan(player))
			{
				if (_hasEvolve)
				{
					html.setFile(player.getLang(), "data/html/clanHallDoormen/doormen2.htm");
					html.replace("%clanname%", owner.getName());
				}
				else
				{
					html.setFile(player.getLang(), "data/html/clanHallDoormen/doormen1.htm");
					html.replace("%clanname%", owner.getName());
				}
			}
			else
			{
				if ((owner != null) && (owner.getLeader() != null))
				{
					html.setFile(player.getLang(), "data/html/clanHallDoormen/doormen-no.htm");
					html.replace("%leadername%", owner.getLeaderName());
					html.replace("%clanname%", owner.getName());
				}
				else
				{
					html.setFile(player.getLang(), "data/html/clanHallDoormen/emptyowner.htm");
					html.replace("%hallname%", getClanHall().getName());
				}
			}
		}
		else
		{
			return;
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	protected final void openDoors(L2PcInstance player, String command)
	{
		getClanHall().openCloseDoors(true);
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), "data/html/clanHallDoormen/doormen-opened.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	protected final void closeDoors(L2PcInstance player, String command)
	{
		getClanHall().openCloseDoors(false);
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), "data/html/clanHallDoormen/doormen-closed.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private final ClanHall getClanHall()
	{
		if (!_init)
		{
			synchronized (this)
			{
				if (!_init)
				{
					_clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
					if (_clanHall != null)
					{
						_hasEvolve = Arrays.binarySearch(CH_WITH_EVOLVE, _clanHall.getId()) >= 0;
					}
					_init = true;
				}
			}
		}
		return _clanHall;
	}
	
	@Override
	protected final boolean isOwnerClan(L2PcInstance player)
	{
		if ((player.getClan() != null) && (getClanHall() != null))
		{
			if (player.getClanId() == getClanHall().getOwnerId())
			{
				return true;
			}
		}
		return false;
	}
}