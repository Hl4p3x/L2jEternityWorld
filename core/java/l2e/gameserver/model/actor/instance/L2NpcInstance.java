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
package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.Map;

import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.status.FolkStatus;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.StringUtil;

public class L2NpcInstance extends L2Npc
{
	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2NpcInstance);
		setIsInvul(false);
	}
	
	@Override
	public FolkStatus getStatus()
	{
		return (FolkStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new FolkStatus(this));
	}
	
	public List<ClassId> getClassesToTeach()
	{
		return getTemplate().getTeachInfo();
	}
	
	public static void showSkillList(L2PcInstance player, L2Npc npc, ClassId classId)
	{
		if (Config.DEBUG)
		{
			_log.fine("SkillList activated on: " + npc.getObjectId());
		}
		
		final int npcId = npc.getTemplate().getId();
		if (npcId == 32611)
		{
			final List<L2SkillLearn> skills = SkillTreesParser.getInstance().getAvailableCollectSkills(player);
			final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.COLLECT);
			
			int counts = 0;
			for (L2SkillLearn s : skills)
			{
				final L2Skill sk = SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
				
				if (sk != null)
				{
					counts++;
					asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), 0, 1);
				}
			}
			
			if (counts == 0)
			{
				final int minLevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, SkillTreesParser.getInstance().getCollectSkillTree());
				if (minLevel > 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
					sm.addNumber(minLevel);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
				}
			}
			else
			{
				player.sendPacket(asl);
			}
			return;
		}
		
		if (!npc.getTemplate().canTeach(classId))
		{
			npc.showNoTeachHtml(player);
			return;
		}
		
		if (((L2NpcInstance) npc).getClassesToTeach().isEmpty())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			final String sb = StringUtil.concat("<html><body>" + "I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:", String.valueOf(npcId), ", Your classId:", String.valueOf(player.getClassId().getId()), "<br>" + "</body></html>");
			html.setHtml(sb);
			player.sendPacket(html);
			return;
		}
		final List<L2SkillLearn> skills = SkillTreesParser.getInstance().getAvailableSkills(player, classId, false, false);
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.CLASS);
		int count = 0;
		player.setLearningClass(classId);
		for (L2SkillLearn s : skills)
		{
			if (SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
			{
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getCalculatedLevelUpSp(player.getClassId(), classId), 0);
				count++;
			}
		}
		
		if (count == 0)
		{
			final Map<Integer, L2SkillLearn> skillTree = SkillTreesParser.getInstance().getCompleteClassSkillTree(classId);
			final int minLevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, skillTree);
			if (minLevel > 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minLevel);
				player.sendPacket(sm);
			}
			else
			{
				if (player.getClassId().level() == 1)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NO_SKILLS_TO_LEARN_RETURN_AFTER_S1_CLASS_CHANGE);
					sm.addNumber(2);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
				}
			}
		}
		else
		{
			player.sendPacket(asl);
		}
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
	}
}