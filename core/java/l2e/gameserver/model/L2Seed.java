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
package l2e.gameserver.model;

import l2e.Config;

public class L2Seed
{
	private final int _seedId;
	private final int _cropId;
	private final int _level;
	private final int _matureId;
	private final int _reward1;
	private final int _reward2;
	private final int _castleId;
	private final boolean _isAlternative;
	private final int _limitSeeds;
	private final int _limitCrops;
	
	public L2Seed(StatsSet set)
	{
		_cropId = set.getInteger("id");
		_seedId = set.getInteger("seedId");
		_level = set.getInteger("level");
		_matureId = set.getInteger("mature_Id");
		_reward1 = set.getInteger("reward1");
		_reward2 = set.getInteger("reward2");
		_castleId = set.getInteger("castleId");
		_isAlternative = set.getBool("alternative");
		_limitCrops = set.getInteger("limit_crops");
		_limitSeeds = set.getInteger("limit_seed");
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
	
	public int getSeedId()
	{
		return _seedId;
	}
	
	public int getCropId()
	{
		return _cropId;
	}
	
	public int getMatureId()
	{
		return _matureId;
	}
	
	public int getReward(int type)
	{
		return (type == 1 ? _reward1 : _reward2);
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public boolean isAlternative()
	{
		return _isAlternative;
	}
	
	public int getSeedLimit()
	{
		return _limitSeeds * Config.RATE_DROP_MANOR;
	}
	
	public int getCropLimit()
	{
		return _limitCrops * Config.RATE_DROP_MANOR;
	}
	
	@Override
	public String toString()
	{
		return "SeedData [_id=" + _seedId + ", _level=" + _level + ", _crop=" + _cropId + ", _mature=" + _matureId + ", _type1=" + _reward1 + ", _type2=" + _reward2 + ", _manorId=" + _castleId + ", _isAlternative=" + _isAlternative + ", _limitSeeds=" + _limitSeeds + ", _limitCrops=" + _limitCrops + "]";
	}
}