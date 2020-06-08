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

import l2e.gameserver.data.xml.EnchantSkillGroupsParser;
import l2e.gameserver.model.L2EnchantSkillLearn;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;

public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
	private int _type;
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		int reqSkillLvl = -2;
		
		if ((_type == 0) || (_type == 1))
		{
			reqSkillLvl = _skillLvl - 1;
		}
		else if (_type == 2)
		{
			reqSkillLvl = _skillLvl + 1;
		}
		else if (_type == 3)
		{
			reqSkillLvl = _skillLvl;
		}
		
		int playerSkillLvl = activeChar.getSkillLevel(_skillId);
		
		if (playerSkillLvl == -1)
		{
			return;
		}
		
		if ((reqSkillLvl % 100) == 0)
		{
			L2EnchantSkillLearn esl = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(_skillId);
			if (esl != null)
			{
				if (playerSkillLvl != esl.getBaseLevel())
				{
					return;
				}
			}
			else
			{
				return;
			}
		}
		else if (playerSkillLvl != reqSkillLvl)
		{
			if ((_type == 3) && ((playerSkillLvl % 100) != (_skillLvl % 100)))
			{
				return;
			}
		}
		
		ExEnchantSkillInfoDetail esd = new ExEnchantSkillInfoDetail(_type, _skillId, _skillLvl, activeChar);
		activeChar.sendPacket(esd);
	}
}