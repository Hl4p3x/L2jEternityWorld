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
package l2e.scripts.ai.npc.NpcBuffers;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.ZoneId;

public class ChristmasTree extends AbstractNpcAI
{
	private static final int CHRISTMAS_TREE = 13007;
	
	protected ChristmasTree(String name, String descr)
	{
		super(name, descr);

		addFirstTalkId(CHRISTMAS_TREE);
		addSpawnId(CHRISTMAS_TREE);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		addTask(npc);
		return super.onSpawn(npc);
	}
	
	private void addTask(L2Npc npc)
	{
		final SkillsHolder holder = new SkillsHolder(2139, 1);
		ThreadPoolManager.getInstance().scheduleGeneral(new ChristmasTreeAI(npc, holder), 1000);
	}
	
	protected class ChristmasTreeAI implements Runnable
	{
		private final L2Npc _npc;
		private final SkillsHolder _holder;
		
		protected ChristmasTreeAI(L2Npc npc, SkillsHolder holder)
		{
			_npc = npc;
			_holder = holder;
		}
		
		@Override
		public void run()
		{
			if ((_npc == null) || !_npc.isVisible() || (_holder == null) || (_holder.getSkill() == null))
			{
				return;
			}
			
			if (!_npc.isInsideZone(ZoneId.PEACE))
			{
				L2Skill skill = _holder.getSkill();
				
				if (_npc.getSummoner() == null || !_npc.getSummoner().isPlayer())
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
					return;
				}
				
				final L2PcInstance player = _npc.getSummoner().getActingPlayer();
				
				if (!player.isInParty())
				{
					if (player.isInsideRadius(_npc, skill.getAffectRange(), true, true))
					{
						skill.getEffects(_npc, player);
					}
				}
				else
				{
					for (L2PcInstance member : player.getParty().getMembers())
					{
						if ((member != null) && member.isInsideRadius(_npc, skill.getAffectRange(), true, true))
						{
							skill.getEffects(_npc, member);
						}
					}
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
		}
	}
	
	public static void main(String[] args)
	{
		new ChristmasTree(ChristmasTree.class.getSimpleName(), "ai");
	}
}