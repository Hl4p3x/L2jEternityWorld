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

import java.util.logging.Logger;

import l2e.gameserver.handler.skillhandlers.BallistaBomb;
import l2e.gameserver.handler.skillhandlers.Blow;
import l2e.gameserver.handler.skillhandlers.Continuous;
import l2e.gameserver.handler.skillhandlers.CpDamPercent;
import l2e.gameserver.handler.skillhandlers.DeluxeKey;
import l2e.gameserver.handler.skillhandlers.Detection;
import l2e.gameserver.handler.skillhandlers.Disablers;
import l2e.gameserver.handler.skillhandlers.Dummy;
import l2e.gameserver.handler.skillhandlers.Fishing;
import l2e.gameserver.handler.skillhandlers.FishingSkill;
import l2e.gameserver.handler.skillhandlers.GetPlayer;
import l2e.gameserver.handler.skillhandlers.Manadam;
import l2e.gameserver.handler.skillhandlers.Mdam;
import l2e.gameserver.handler.skillhandlers.NornilsPower;
import l2e.gameserver.handler.skillhandlers.Pdam;
import l2e.gameserver.handler.skillhandlers.Resurrect;
import l2e.gameserver.handler.skillhandlers.Sow;
import l2e.gameserver.handler.skillhandlers.TakeFort;
import l2e.gameserver.handler.skillhandlers.Trap;
import l2e.gameserver.handler.skillhandlers.Unlock;
import l2e.gameserver.model.skills.L2SkillType;
import gnu.trove.map.hash.TIntObjectHashMap;

public class SkillHandler
{
	private static Logger _log = Logger.getLogger(SkillHandler.class.getName());
	
	private final TIntObjectHashMap<ISkillHandler> _datatable;
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected SkillHandler()
	{
		_datatable = new TIntObjectHashMap<>();
		
		registerHandler(new BallistaBomb());
		registerHandler(new Blow());
		registerHandler(new Continuous());
		registerHandler(new CpDamPercent());
		registerHandler(new DeluxeKey());
		registerHandler(new Detection());
		registerHandler(new Disablers());
		registerHandler(new Dummy());
		registerHandler(new Fishing());
		registerHandler(new FishingSkill());
		registerHandler(new GetPlayer());
		registerHandler(new Manadam());
		registerHandler(new Mdam());
		registerHandler(new NornilsPower());
		registerHandler(new Pdam());
		registerHandler(new Resurrect());
		registerHandler(new Sow());
		registerHandler(new TakeFort());
		registerHandler(new Trap());
		registerHandler(new Unlock());
		
		_log.info("Loaded " + _datatable.size() + " SkillHandlers");
	}
	
	public void registerHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.put(t.ordinal(), handler);
		}
	}
	
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _datatable.get(skillType.ordinal());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}