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
package l2e.gameserver.util;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.RelationChanged;
import gnu.trove.procedure.TObjectProcedure;

public final class Broadcast
{
	private static Logger _log = Logger.getLogger(Broadcast.class.getName());
	
	public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player.getTarget() != character)
			{
				continue;
			}
			
			player.sendPacket(mov);
		}
		
	}
	
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player == null)
			{
				continue;
			}
			try
			{
				player.sendPacket(mov);
				if ((mov instanceof CharInfo) && (character.isPlayer()))
				{
					int relation = ((L2PcInstance) character).getRelation(player);
					Integer oldrelation = character.getKnownList().getKnownRelations().get(player.getObjectId());
					if ((oldrelation != null) && (oldrelation != relation))
					{
						player.sendPacket(new RelationChanged((L2PcInstance) character, relation, character.isAutoAttackable(player)));
						if (character.hasSummon())
						{
							player.sendPacket(new RelationChanged(character.getSummon(), relation, character.isAutoAttackable(player)));
						}
					}
				}
			}
			catch (NullPointerException e)
			{
				_log.log(Level.WARNING, e.getMessage(), e);
			}
		}
		
	}
	
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (character.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (character.isPlayer())
		{
			character.sendPacket(mov);
		}
		
		toKnownPlayers(character, mov);
	}
	
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 600;
		}
		
		if (character.isPlayer())
		{
			character.sendPacket(mov);
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if ((player != null) && Util.checkIfInRange(radius, character, player, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		L2World.getInstance().forEachPlayer(new ForEachPlayerBroadcast(mov));
	}
	
	public static void announceToOnlinePlayers(String text, boolean isCritical)
	{
		CreatureSay cs;
		
		if (isCritical)
		{
			cs = new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "", text);
		}
		else
		{
			cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		}
		
		toAllOnlinePlayers(cs);
	}
	
	public static void toPlayersInInstance(L2GameServerPacket mov, int instanceId)
	{
		L2World.getInstance().forEachPlayer(new ForEachPlayerInInstanceBroadcast(mov, instanceId));
	}
	
	private static final class ForEachPlayerBroadcast implements TObjectProcedure<L2PcInstance>
	{
		L2GameServerPacket _packet;
		
		protected ForEachPlayerBroadcast(L2GameServerPacket packet)
		{
			_packet = packet;
		}
		
		@Override
		public final boolean execute(final L2PcInstance onlinePlayer)
		{
			if ((onlinePlayer != null) && onlinePlayer.isOnline())
			{
				onlinePlayer.sendPacket(_packet);
			}
			return true;
		}
	}
	
	private static final class ForEachPlayerInInstanceBroadcast implements TObjectProcedure<L2PcInstance>
	{
		L2GameServerPacket _packet;
		int _instanceId;
		
		protected ForEachPlayerInInstanceBroadcast(L2GameServerPacket packet, int instanceId)
		{
			_packet = packet;
			_instanceId = instanceId;
		}
		
		@Override
		public final boolean execute(final L2PcInstance onlinePlayer)
		{
			if ((onlinePlayer != null) && onlinePlayer.isOnline() && (onlinePlayer.getInstanceId() == _instanceId))
			{
				onlinePlayer.sendPacket(_packet);
			}
			return true;
		}
	}
}