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
package l2e.gameserver.handler.effecthandlers;

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.holders.SummonRequestHolder;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CallPc extends L2Effect
{
	private static int _itemId;
	private static int _itemCount;
	
	public CallPc(Env env, EffectTemplate template)
	{
		super(env, template);
		_itemId = template.getParameters().getInteger("itemId", 0);
		_itemCount = template.getParameters().getInteger("itemCount", 0);
	}

	@Override
	public boolean calcSuccess()
	{
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CALLPC;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() == getEffector())
		{
			return false;
		}
		
		L2PcInstance target = getEffected().getActingPlayer();
		L2PcInstance activeChar = getEffector().getActingPlayer();
		if (checkSummonTargetStatus(target, activeChar))
		{
			if ((_itemId != 0) && (_itemCount != 0))
			{
				if (target.getInventory().getInventoryItemCount(_itemId, 0) < _itemCount)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
					sm.addItemName(_itemId);
					target.sendPacket(sm);
					return false;
				}
				target.getInventory().destroyItemByItemId("Consume", _itemId, _itemCount, activeChar, target);
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(_itemId);
				target.sendPacket(sm);
			}
			
			target.addScript(new SummonRequestHolder(activeChar, getSkill()));
			final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
			confirm.addCharName(activeChar);
			confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			confirm.addTime(30000);
			confirm.addRequesterId(activeChar.getObjectId());
			target.sendPacket(confirm);
			return true;
		}
		return false;
	}
	
	public static boolean checkSummonTargetStatus(L2PcInstance target, L2PcInstance activeChar)
	{
		if (target == activeChar)
		{
			return false;
		}
		
		if (target.isAlikeDead())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInStoreMode())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isRooted() || target.isInCombat())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
			return false;
		}
		
		if (target.isFestivalParticipant() || target.isFlyingMounted() || target.isCombatFlagEquipped() || !TvTEvent.onEscapeUse(target.getObjectId()) || !TvTRoundEvent.onEscapeUse(target.getObjectId()))
		{
			activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		if (target.inObserverMode())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_STATE_FORBIDS_SUMMONING);
			sm.addCharName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || target.isInsideZone(ZoneId.JAIL))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (activeChar.getInstanceId() > 0)
		{
			Instance summonerInstance = InstanceManager.getInstance().getInstance(activeChar.getInstanceId());
			if (!Config.ALLOW_SUMMON_TO_INSTANCE || !summonerInstance.isSummonAllowed())
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}
		}
		
		if (activeChar.isIn7sDungeon())
		{
			int targetCabal = SevenSigns.getInstance().getPlayerCabal(target.getObjectId());
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					return false;
				}
			}
			else if (targetCabal == SevenSigns.CABAL_NULL)
			{
				activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
				return false;
			}
		}
		return true;
	}
}