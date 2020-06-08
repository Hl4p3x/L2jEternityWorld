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
package l2e.scripts.custom;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class ArenaManager extends AbstractNpcAI
{
	private static final int[] ARENA_MANAGER =
	{
		31226,
		31225
	};

	private static final SkillsHolder[] BUFFS =
	{
		new SkillsHolder(6805, 1),
		new SkillsHolder(6806, 1),
		new SkillsHolder(6807, 1),
		new SkillsHolder(6808, 1),
		new SkillsHolder(6804, 1),
		new SkillsHolder(6812, 1)
	};

	private static final SkillsHolder CP_RECOVERY = new SkillsHolder(4380, 1);
	private static final SkillsHolder HP_RECOVERY = new SkillsHolder(6817, 1);

	private static final int CP_COST = 1000;
	private static final int HP_COST = 1000;
	private static final int BUFF_COST = 2000;
	
	private ArenaManager()
	{
		super(ArenaManager.class.getSimpleName(), "custom");

		addStartNpc(ARENA_MANAGER);
		addTalkId(ARENA_MANAGER);
		addFirstTalkId(ARENA_MANAGER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "CPrecovery":
			{
				if (player.getAdena() >= CP_COST)
				{
					takeItems(player, PcInventory.ADENA_ID, CP_COST);
					startQuestTimer("CPrecovery_delay", 2000, npc, player);
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				}
				break;
			}
			case "CPrecovery_delay":
			{
				if ((player != null) && !player.isInsideZone(ZoneId.PVP))
				{
					npc.setTarget(player);
					npc.doCast(CP_RECOVERY.getSkill());
				}
				break;
			}
			case "HPrecovery":
			{
				if (player.getAdena() >= HP_COST)
				{
					takeItems(player, PcInventory.ADENA_ID, HP_COST);
					startQuestTimer("HPrecovery_delay", 2000, npc, player);
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				}
				break;
			}
			case "HPrecovery_delay":
			{
				if ((player != null) && !player.isInsideZone(ZoneId.PVP))
				{
					npc.setTarget(player);
					npc.doCast(HP_RECOVERY.getSkill());
				}
				break;
			}
			case "Buff":
			{
				if (player.getAdena() >= BUFF_COST)
				{
					takeItems(player, PcInventory.ADENA_ID, BUFF_COST);
					npc.setTarget(player);
					for (SkillsHolder skill : BUFFS)
					{
						npc.doCast(skill.getSkill());
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				}
				break;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new ArenaManager();
	}
}