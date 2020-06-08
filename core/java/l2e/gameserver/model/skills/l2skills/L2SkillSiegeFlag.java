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
package l2e.gameserver.model.skills.l2skills;

import java.util.logging.Level;

import l2e.Config;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class L2SkillSiegeFlag extends L2Skill
{
	private final boolean _isAdvanced;
	private final boolean _isOutpost;
	
	public L2SkillSiegeFlag(StatsSet set)
	{
		super(set);
		_isAdvanced = set.getBool("isAdvanced", false);
		_isOutpost = set.getBool("isOutpost", false);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		L2PcInstance player = activeChar.getActingPlayer();
		
		if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId()))
		{
			return;
		}
		
		if (!checkIfOkToPlaceFlag(player, true, _isOutpost))
		{
			return;
		}
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			try
			{
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate((_isOutpost ? 36590 : 35062)), _isAdvanced, _isOutpost);
				flag.setTitle(player.getClan().getName());
				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
				if (_isOutpost)
				{
					TerritoryWarManager.getInstance().setHQForClan(player.getClan(), flag);
				}
				else
				{
					TerritoryWarManager.getInstance().addClanFlag(player.getClan(), flag);
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Error placing flag: " + e);
				_log.log(Level.WARNING, "Error placing flag: " + e.getMessage(), e);
			}
			return;
		}
		
		try
		{
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062), _isAdvanced, false);
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			Castle castle = CastleManager.getInstance().getCastle(activeChar);
			Fort fort = FortManager.getInstance().getFort(activeChar);
			SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
			if (castle != null)
			{
				castle.getSiege().getFlag(player.getClan()).add(flag);
			}
			else if (fort != null)
			{
				fort.getSiege().getFlag(player.getClan()).add(flag);
			}
			else
			{
				hall.getSiege().getFlag(player.getClan()).add(flag);
			}
			
		}
		catch (Exception e)
		{
			player.sendMessage("Error placing flag:" + e);
			_log.log(Level.WARNING, "Error placing flag: " + e.getMessage(), e);
		}
	}
	
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly, boolean isOutPost)
	{
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			return checkIfOkToPlaceHQ(activeChar, isCheckOnly, isOutPost);
		}
		else if (isOutPost)
		{
			return false;
		}
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);
		SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
		
		if ((castle == null) && (fort == null) && (hall == null))
		{
			return false;
		}
		if (castle != null)
		{
			return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
		}
		else if (fort != null)
		{
			return checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly);
		}
		return checkIfOkToPlaceFlag(activeChar, hall, isCheckOnly);
	}
	
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (!activeChar.isPlayer())
		{
			return false;
		}
		
		String text = "";
		L2PcInstance player = activeChar.getActingPlayer();
		
		if ((castle == null) || (castle.getId() <= 0))
		{
			text = "You must be on castle ground to place a flag.";
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			text = "You can only place a flag during a siege.";
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
		{
			text = "You must be an attacker to place a flag.";
		}
		else if (!player.isClanLeader())
		{
			text = "You must be a clan leader to place a flag.";
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
		{
			text = "You have already placed the maximum number of flags possible.";
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}
	
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Fort fort, boolean isCheckOnly)
	{
		if (!activeChar.isPlayer())
		{
			return false;
		}
		
		String text = "";
		L2PcInstance player = activeChar.getActingPlayer();
		
		if ((fort == null) || (fort.getId() <= 0))
		{
			text = "You must be on fort ground to place a flag.";
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			text = "You can only place a flag during a siege.";
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			text = "You must be an attacker to place a flag.";
		}
		else if (!player.isClanLeader())
		{
			text = "You must be a clan leader to place a flag.";
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount())
		{
			text = "You have already placed the maximum number of flags possible.";
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}
	
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, SiegableHall hall, boolean isCheckOnly)
	{
		if (!activeChar.isPlayer())
		{
			return false;
		}
		
		String text = "";
		L2PcInstance player = activeChar.getActingPlayer();
		final int hallId = hall.getId();
		
		if (hallId <= 0)
		{
			text = "You must be on Siegable clan hall ground to place a flag.";
		}
		else if (!hall.isInSiege())
		{
			text = "You can only place a flag during a siege.";
		}
		else if ((player.getClan() == null) || !player.isClanLeader())
		{
			text = "You must be a clan leader to place a flag.";
		}
		else if (!hall.isRegistered(player.getClan()))
		{
			text = "You must be an attacker to place a flag.";
		}
		else if (hall.getSiege().getAttackerClan(player.getClan()).getNumFlags() > Config.CHS_MAX_FLAGS_PER_CLAN)
		{
			text = "You have already placed the maximum number of flags possible.";
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		}
		else if (!hall.getSiege().canPlantFlag())
		{
			text = "You cannot place a flag on this siege.";
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}
	
	public static boolean checkIfOkToPlaceHQ(L2Character activeChar, boolean isCheckOnly, boolean isOutPost)
	{
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);
		
		if ((castle == null) && (fort == null))
		{
			return false;
		}
		
		String text = "";
		L2PcInstance player = activeChar.getActingPlayer();
		
		if (((fort != null) && (fort.getId() == 0)) || ((castle != null) && (castle.getId() == 0)))
		{
			text = "You must be on fort or castle ground to construct an outpost or flag.";
		}
		else if (((fort != null) && !fort.getZone().isActive()) || ((castle != null) && !castle.getZone().isActive()))
		{
			text = "You can only construct an outpost or flag on siege field.";
		}
		else if (!player.isClanLeader())
		{
			text = "You must be a clan leader to construct an outpost or flag.";
		}
		else if ((TerritoryWarManager.getInstance().getHQForClan(player.getClan()) != null) && isOutPost)
		{
			player.sendPacket(SystemMessageId.NOT_ANOTHER_HEADQUARTERS);
		}
		else if ((TerritoryWarManager.getInstance().getFlagForClan(player.getClan()) != null) && !isOutPost)
		{
			player.sendPacket(SystemMessageId.A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED);
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}
}