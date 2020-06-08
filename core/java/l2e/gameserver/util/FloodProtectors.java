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

import l2e.Config;
import l2e.gameserver.network.L2GameClient;

public final class FloodProtectors
{
	private final FloodProtectorAction _useItem;
	private final FloodProtectorAction _rollDice;
	private final FloodProtectorAction _firework;
	private final FloodProtectorAction _itemPetSummon;
	private final FloodProtectorAction _heroVoice;
	private final FloodProtectorAction _globalChat;
	private final FloodProtectorAction _subclass;
	private final FloodProtectorAction _dropItem;
	private final FloodProtectorAction _serverBypass;
	private final FloodProtectorAction _multiSell;
	private final FloodProtectorAction _transaction;
	private final FloodProtectorAction _manufacture;
	private final FloodProtectorAction _manor;
	private final FloodProtectorAction _sendMail;
	private final FloodProtectorAction _characterSelect;
	private final FloodProtectorAction _itemAuction;
	
	public FloodProtectors(final L2GameClient client)
	{
		super();

		_useItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_USE_ITEM);
		_rollDice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ROLL_DICE);
		_firework = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_FIREWORK);
		_itemPetSummon = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_PET_SUMMON);
		_heroVoice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_HERO_VOICE);
		_globalChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_GLOBAL_CHAT);
		_subclass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SUBCLASS);
		_dropItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_DROP_ITEM);
		_serverBypass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SERVER_BYPASS);
		_multiSell = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MULTISELL);
		_transaction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_TRANSACTION);
		_manufacture = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANUFACTURE);
		_manor = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANOR);
		_sendMail = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SENDMAIL);
		_characterSelect = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_CHARACTER_SELECT);
		_itemAuction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_AUCTION);
	}
	
	public FloodProtectorAction getUseItem()
	{
		return _useItem;
	}
	
	public FloodProtectorAction getRollDice()
	{
		return _rollDice;
	}
	
	public FloodProtectorAction getFirework()
	{
		return _firework;
	}
	
	public FloodProtectorAction getItemPetSummon()
	{
		return _itemPetSummon;
	}
	
	public FloodProtectorAction getHeroVoice()
	{
		return _heroVoice;
	}
	
	public FloodProtectorAction getGlobalChat()
	{
		return _globalChat;
	}
	
	public FloodProtectorAction getSubclass()
	{
		return _subclass;
	}
	
	public FloodProtectorAction getDropItem()
	{
		return _dropItem;
	}
	
	public FloodProtectorAction getServerBypass()
	{
		return _serverBypass;
	}
	
	public FloodProtectorAction getMultiSell()
	{
		return _multiSell;
	}
	
	public FloodProtectorAction getTransaction()
	{
		return _transaction;
	}
	
	public FloodProtectorAction getManufacture()
	{
		return _manufacture;
	}
	
	public FloodProtectorAction getManor()
	{
		return _manor;
	}
	
	public FloodProtectorAction getSendMail()
	{
		return _sendMail;
	}
	
	public FloodProtectorAction getCharacterSelect()
	{
		return _characterSelect;
	}
	
	public FloodProtectorAction getItemAuction()
	{
		return _itemAuction;
	}
}