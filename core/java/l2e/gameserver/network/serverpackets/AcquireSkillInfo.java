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

import java.util.ArrayList;
import java.util.List;

import l2e.Config;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.skills.L2Skill;

public class AcquireSkillInfo extends L2GameServerPacket
{
	private final AcquireSkillType _type;
	private final int _id;
	private final int _level;
	private final int _spCost;
	private final List<Req> _reqs;
	
	private static class Req
	{
		public int itemId;
		public long count;
		public int type;
		public int unk;
		
		public Req(int pType, int pItemId, long itemCount, int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = itemCount;
			unk = pUnk;
		}
	}
	
	public AcquireSkillInfo(AcquireSkillType skillType, L2SkillLearn skillLearn)
	{
		_id = skillLearn.getSkillId();
		_level = skillLearn.getSkillLevel();
		_spCost = skillLearn.getLevelUpSp();
		_type = skillType;
		_reqs = new ArrayList<>();
		if ((skillType != AcquireSkillType.PLEDGE) || Config.LIFE_CRYSTAL_NEEDED)
		{
			for (ItemsHolder item : skillLearn.getRequiredItems())
			{
				if (!Config.DIVINE_SP_BOOK_NEEDED && (_id == L2Skill.SKILL_DIVINE_INSPIRATION))
				{
					continue;
				}
				_reqs.add(new Req(99, item.getId(), item.getCount(), 50));
			}
		}
	}
	
	public AcquireSkillInfo(AcquireSkillType skillType, L2SkillLearn skillLearn, int sp)
	{
		_id = skillLearn.getSkillId();
		_level = skillLearn.getSkillLevel();
		_spCost = sp;
		_type = skillType;
		_reqs = new ArrayList<>();
		for (ItemsHolder item : skillLearn.getRequiredItems())
		{
			_reqs.add(new Req(99, item.getId(), item.getCount(), 50));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x91);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_type.ordinal());
		writeD(_reqs.size());
		for (Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.itemId);
			writeQ(temp.count);
			writeD(temp.unk);
		}
	}
}