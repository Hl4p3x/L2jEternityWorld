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
package l2e.gameserver.network.serverpackets;

import javolution.util.FastList;
import l2e.gameserver.data.xml.EnchantSkillGroupsParser;
import l2e.gameserver.model.L2EnchantSkillGroup.EnchantSkillsHolder;
import l2e.gameserver.model.L2EnchantSkillLearn;

public final class ExEnchantSkillInfo extends L2GameServerPacket
{
	private final FastList<Integer> _routes;
	
	private final int _id;
	private final int _lvl;
	private boolean _maxEnchanted = false;
	
	public ExEnchantSkillInfo(int id, int lvl)
	{
		_routes = new FastList<>();
		_id = id;
		_lvl = lvl;
		
		L2EnchantSkillLearn enchantLearn = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(_id);
		if (enchantLearn != null)
		{
			if (_lvl > 100)
			{
				_maxEnchanted = enchantLearn.isMaxEnchant(_lvl);
				
				EnchantSkillsHolder esd = enchantLearn.getEnchantSkillsHolder(_lvl);
				
				if (esd != null)
				{
					_routes.add(_lvl);
				}
				
				int skillLvL = (_lvl % 100);
				
				for (int route : enchantLearn.getAllRoutes())
				{
					if (((route * 100) + skillLvL) == _lvl)
					{
						continue;
					}
					
					_routes.add((route * 100) + skillLvL);
				}
				
			}
			else
			{
				for (int route : enchantLearn.getAllRoutes())
				{
					_routes.add((route * 100) + 1);
				}
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2a);
		writeD(_id);
		writeD(_lvl);
		writeD(_maxEnchanted ? 0 : 1);
		writeD(_lvl > 100 ? 1 : 0);
		writeD(_routes.size());
		
		for (int level : _routes)
		{
			writeD(level);
		}
	}
}