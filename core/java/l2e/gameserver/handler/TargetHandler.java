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
package l2e.gameserver.handler;

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2e.gameserver.handler.targethandlers.*;
import l2e.gameserver.model.skills.targets.L2TargetType;

public class TargetHandler
{
	private static final Logger _log = Logger.getLogger(TargetHandler.class.getName());

	private final Map<Enum<L2TargetType>, ITargetTypeHandler> _datatable;
	
	public static TargetHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected TargetHandler()
	{
		_datatable = new FastMap<>();

		registerHandler(new Area());
		registerHandler(new AreaCorpseMob());
		registerHandler(new AreaFriendly());
		registerHandler(new AreaSummon());
		registerHandler(new Aura());
		registerHandler(new AuraCorpseMob());
		registerHandler(new BehindArea());
		registerHandler(new BehindAura());
		registerHandler(new Clan());
		registerHandler(new ClanMember());
		registerHandler(new CorpseClan());
		registerHandler(new CorpseMob());
		registerHandler(new CorpsePet());
		registerHandler(new CorpsePlayer());
		registerHandler(new EnemySummon());
		registerHandler(new FlagPole());
		registerHandler(new FrontArea());
		registerHandler(new FrontAura());
		registerHandler(new Ground());
		registerHandler(new Holy());
		registerHandler(new One());
		registerHandler(new OwnerPet());
		registerHandler(new Party());
		registerHandler(new PartyClan());
		registerHandler(new PartyMember());
		registerHandler(new PartyNotMe());
		registerHandler(new PartyOther());
		registerHandler(new Pet());
		registerHandler(new Self());
		registerHandler(new Servitor());
		registerHandler(new Summon());
		registerHandler(new Unlockable());

		_log.info("Loaded " + _datatable.size() + " TargetHandlers");
	}
	
	public void registerHandler(ITargetTypeHandler handler)
	{
		_datatable.put(handler.getTargetType(), handler);
	}
	
	public ITargetTypeHandler getHandler(Enum<L2TargetType> targetType)
	{
		return _datatable.get(targetType);
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final TargetHandler _instance = new TargetHandler();
	}
}