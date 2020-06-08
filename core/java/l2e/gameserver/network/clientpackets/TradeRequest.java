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
package l2e.gameserver.network.clientpackets;

import l2e.Config;
import l2e.gameserver.data.xml.BotReportParser;
import l2e.gameserver.model.BlockList;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SendTradeRequest;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public final class TradeRequest extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your current Access Level.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Effect ef = null;
		if (((ef = player.getFirstEffect(L2EffectType.ACTION_BLOCK)) != null) && !ef.checkCondition(BotReportParser.TRADE_ACTION_BLOCK_ID))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object target = L2World.getInstance().findObject(_objectId);
		
		if ((target == null) || !player.getKnownList().knowsObject(target) || ((target.getInstanceId() != player.getInstanceId()) && (player.getInstanceId() != -1)))
		{
			return;
		}
		
		if (target.getObjectId() == player.getObjectId())
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		if (!target.isPlayer())
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2PcInstance partner = target.getActingPlayer();
		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("A user currently participating in the Olympiad cannot accept or request a trade.");
			return;
		}
		
		if (((ef = partner.getFirstEffect(L2EffectType.ACTION_BLOCK)) != null) && !ef.checkCondition(BotReportParser.TRADE_ACTION_BLOCK_ID))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_REPORTED_AND_IS_BEING_INVESTIGATED);
			sm.addCharName(partner);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0))
		{
			player.sendMessage("You cannot trade while you are in a chaotic state.");
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (partner.getKarma() > 0))
		{
			player.sendMessage("You cannot request a trade while your target is in a chaotic state.");
			return;
		}
		
		if (Config.JAIL_DISABLE_TRANSACTION && (player.isJailed() || partner.isJailed()))
		{
			player.sendMessage("You cannot trade while you are in in Jail.");
			return;
		}
		
		if ((player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE) || (partner.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE))
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				_log.fine("Already trading with someone else.");
			}
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		SystemMessage sm;
		if (partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				_log.info("Transaction already in progress.");
			}
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			return;
		}
		
		if (partner.getTradeRefusal())
		{
			player.sendMessage("That person is in trade refusal mode.");
			return;
		}
		
		if (BlockList.isBlocked(partner, player))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (Util.calculateDistance(player, partner, true) > 150)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}
		
		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		sm = SystemMessage.getSystemMessage(SystemMessageId.REQUEST_C1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}
}