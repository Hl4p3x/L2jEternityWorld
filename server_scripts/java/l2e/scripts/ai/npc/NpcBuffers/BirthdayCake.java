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

public class BirthdayCake extends AbstractNpcAI
{
	private static final int BIRTHDAY_CAKE_24 = 106;
	private static final int BIRTHDAY_CAKE = 139;
	
	protected BirthdayCake(String name, String descr)
	{
		super(name, descr);

		addFirstTalkId(BIRTHDAY_CAKE, BIRTHDAY_CAKE_24);
		addSpawnId(BIRTHDAY_CAKE, BIRTHDAY_CAKE_24);
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
		final SkillsHolder holder;
		switch (npc.getId())
		{
			case BIRTHDAY_CAKE:
			{
				holder = new SkillsHolder(22035, 1);
				break;
			}
			case BIRTHDAY_CAKE_24:
			{
				holder = new SkillsHolder(22250, 1);
				break;
			}
			default:
			{
				return;
			}
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new BirthdayCakeAI(npc, holder), 1000);
	}
	
	protected class BirthdayCakeAI implements Runnable
	{
		private final L2Npc _npc;
		private final SkillsHolder _holder;
		
		protected BirthdayCakeAI(L2Npc npc, SkillsHolder holder)
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
				switch (_npc.getId())
				{
					case BIRTHDAY_CAKE:
					{
						for (L2PcInstance player : _npc.getKnownList().getKnownPlayersInRadius(skill.getAffectRange()))
						{
							skill.getEffects(_npc, player);
						}
						break;
					}
					case BIRTHDAY_CAKE_24:
					{
						final L2PcInstance player = _npc.getSummoner().getActingPlayer();
						if (player == null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
							return;
						}
						
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
						break;
					}
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
		}
	}
	
	public static void main(String[] args)
	{
		new BirthdayCake(BirthdayCake.class.getSimpleName(), "ai/npc");
	}
}