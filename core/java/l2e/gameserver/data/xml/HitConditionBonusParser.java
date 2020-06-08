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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.actor.L2Character;

public final class HitConditionBonusParser extends DocumentParser
{
	private int frontBonus = 0;
	private int sideBonus = 0;
	private int backBonus = 0;
	private int highBonus = 0;
	private int lowBonus = 0;
	private int darkBonus = 0;
	private int rainBonus = 0;
	
	protected HitConditionBonusParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/stats/hitConditionBonus.xml");
		_log.info(getClass().getSimpleName() + ": Loaded Hit Condition bonuses.");
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": Front bonus: " + frontBonus);
			_log.info(getClass().getSimpleName() + ": Side bonus: " + sideBonus);
			_log.info(getClass().getSimpleName() + ": Back bonus: " + backBonus);
			_log.info(getClass().getSimpleName() + ": High bonus: " + highBonus);
			_log.info(getClass().getSimpleName() + ": Low bonus: " + lowBonus);
			_log.info(getClass().getSimpleName() + ": Dark bonus: " + darkBonus);
			_log.info(getClass().getSimpleName() + ": Rain bonus: " + rainBonus);
		}
	}
	
	@Override
	protected void parseDocument()
	{
		final Node n = getCurrentDocument().getFirstChild();
		NamedNodeMap attrs;
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			attrs = d.getAttributes();
			switch (d.getNodeName())
			{
				case "front":
					frontBonus = parseInt(attrs, "val");
					break;
				case "side":
					sideBonus = parseInt(attrs, "val");
					break;
				case "back":
					backBonus = parseInt(attrs, "val");
					break;
				case "high":
					highBonus = parseInt(attrs, "val");
					break;
				case "low":
					lowBonus = parseInt(attrs, "val");
					break;
				case "dark":
					darkBonus = parseInt(attrs, "val");
					break;
				case "rain":
					rainBonus = parseInt(attrs, "val");
					break;
			}
		}
	}
	
	public double getConditionBonus(L2Character attacker, L2Character target)
	{
		double mod = 100;
		
		if ((attacker.getZ() - target.getZ()) > 50)
		{
			mod += highBonus;
		}
		else if ((attacker.getZ() - target.getZ()) < -50)
		{
			mod += lowBonus;
		}
		
		if (GameTimeController.getInstance().isNight())
		{
			mod += darkBonus;
		}
		
		if (attacker.isBehindTarget())
		{
			mod += backBonus;
		}
		else if (attacker.isInFrontOfTarget())
		{
			mod += frontBonus;
		}
		else
		{
			mod += sideBonus;
		}
		return Math.max(mod / 100, 0);
	}
	
	public static HitConditionBonusParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HitConditionBonusParser _instance = new HitConditionBonusParser();
	}
}