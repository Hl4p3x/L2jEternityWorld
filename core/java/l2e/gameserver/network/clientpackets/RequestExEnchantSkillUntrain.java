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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.EnchantSkillGroupsParser;
import l2e.gameserver.model.L2EnchantSkillGroup.EnchantSkillsHolder;
import l2e.gameserver.model.L2EnchantSkillLearn;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2e.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import l2e.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;

public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
{
	private static final Logger _logEnchant = Logger.getLogger("enchant");
	
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
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
		
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getClassId().level() < 3)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS);
			return;
		}
		
		if (player.getLevel() < 76)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL);
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT);
			return;
		}
		
		L2EnchantSkillLearn s = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		
		if ((_skillLvl % 100) == 0)
		{
			_skillLvl = s.getBaseLevel();
		}
		
		L2Skill skill = SkillHolder.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null)
		{
			return;
		}
		
		int reqItemId = EnchantSkillGroupsParser.UNTRAIN_ENCHANT_BOOK;
		
		final int beforeUntrainSkillLevel = player.getSkillLevel(_skillId);
		if (((beforeUntrainSkillLevel - 1) != _skillLvl) && (((beforeUntrainSkillLevel % 100) != 1) || (_skillLvl != s.getBaseLevel())))
		{
			return;
		}
		
		EnchantSkillsHolder esd = s.getEnchantSkillsHolder(beforeUntrainSkillLevel);
		
		int requiredSp = esd.getSpCost();
		int requireditems = esd.getAdenaCost();
		
		L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
		if (Config.ES_SP_BOOK_NEEDED)
		{
			if (spb == null)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
		}
		
		if (player.getInventory().getAdena() < requireditems)
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		boolean check = true;
		if (Config.ES_SP_BOOK_NEEDED)
		{
			check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
		}
		
		check &= player.destroyItemByItemId("Consume", PcInventory.ADENA_ID, requireditems, player, true);
		
		if (!check)
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		player.getStat().addSp((int) (requiredSp * 0.8));
		
		if (Config.LOG_SKILL_ENCHANTS)
		{
			LogRecord record = new LogRecord(Level.INFO, "Untrain");
			record.setParameters(new Object[]
			{
				player,
				skill,
				spb
			});
			record.setLoggerName("skill");
			_logEnchant.log(record);
		}
		
		player.addSkill(skill, true);
		player.sendPacket(ExEnchantSkillResult.valueOf(true));
		
		if (Config.DEBUG)
		{
			_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
		}
		
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		
		if (_skillLvl > 100)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		player.sendSkillList();
		final int afterUntrainSkillLevel = player.getSkillLevel(_skillId);
		player.sendPacket(new ExEnchantSkillInfo(_skillId, afterUntrainSkillLevel));
		player.sendPacket(new ExEnchantSkillInfoDetail(2, _skillId, afterUntrainSkillLevel - 1, player));
		player.updateShortCuts(_skillId, afterUntrainSkillLevel);
	}
}