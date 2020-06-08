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
package l2e.scripts.ai.npc.group_template;

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class NPC_Walk extends L2AttackableAIScript
{
	private final L2Npc wharf_patrol01, wharf_patrol02, wharf_patrol03, wharf_patrol04;
	private static Map<String, Object[]> walks99 = new FastMap<>();
	
	private void load99()
	{
		walks99.put("3262801", new Object[]
		{
			-148230,
			255280,
			-184,
			"3262802"
		});
		walks99.put("3262802", new Object[]
		{
			-148280,
			254820,
			-184,
			"3262803"
		});
		walks99.put("3262803", new Object[]
		{
			-148670,
			254380,
			-184,
			"3262804"
		});
		walks99.put("3262804", new Object[]
		{
			-149230,
			254100,
			-184,
			"3262805"
		});
		walks99.put("3262805", new Object[]
		{
			-148670,
			254380,
			-184,
			"3262806"
		});
		walks99.put("3262806", new Object[]
		{
			-148280,
			254820,
			-184,
			"3262801"
		});
		walks99.put("3262807", new Object[]
		{
			-148270,
			255320,
			-184,
			"3262808"
		});
		walks99.put("3262808", new Object[]
		{
			-148320,
			254860,
			-184,
			"3262809"
		});
		walks99.put("3262809", new Object[]
		{
			-148710,
			254420,
			-184,
			"3262810"
		});
		walks99.put("3262810", new Object[]
		{
			-149270,
			254140,
			-184,
			"3262811"
		});
		walks99.put("3262811", new Object[]
		{
			-148710,
			254420,
			-184,
			"3262812"
		});
		walks99.put("3262812", new Object[]
		{
			-148320,
			254860,
			-184,
			"3262807"
		});
		walks99.put("3262901", new Object[]
		{
			-150500,
			255280,
			-184,
			"3262902"
		});
		walks99.put("3262902", new Object[]
		{
			-150450,
			254820,
			-184,
			"3262903"
		});
		walks99.put("3262903", new Object[]
		{
			-150060,
			254380,
			-184,
			"3262904"
		});
		walks99.put("3262904", new Object[]
		{
			-149500,
			254100,
			-184,
			"3262905"
		});
		walks99.put("3262905", new Object[]
		{
			-150060,
			254380,
			-184,
			"3262906"
		});
		walks99.put("3262906", new Object[]
		{
			-150450,
			254820,
			-184,
			"3262901"
		});
		walks99.put("3262907", new Object[]
		{
			-150460,
			255320,
			-184,
			"3262908"
		});
		walks99.put("3262908", new Object[]
		{
			-150410,
			254860,
			-184,
			"3262909"
		});
		walks99.put("3262909", new Object[]
		{
			-150020,
			254420,
			-184,
			"3262910"
		});
		walks99.put("3262910", new Object[]
		{
			-149460,
			254140,
			-184,
			"3262911"
		});
		walks99.put("3262911", new Object[]
		{
			-150020,
			254420,
			-184,
			"3262912"
		});
		walks99.put("3262912", new Object[]
		{
			-150410,
			254860,
			-184,
			"3262907"
		});
	}
	
	public NPC_Walk(int id, String name, String descr)
	{
		super(id, name, descr);
		
		load99();
		wharf_patrol01 = addSpawn(32628, -148230, 255280, -184, 0, false, 0);
		wharf_patrol02 = addSpawn(32628, -148270, 255320, -184, 0, false, 0);
		wharf_patrol03 = addSpawn(32629, -150500, 255280, -184, 0, false, 0);
		wharf_patrol04 = addSpawn(32629, -150460, 255320, -184, 0, false, 0);
		startQuestTimer("3262801", 5000, wharf_patrol01, null);
		startQuestTimer("3262807", 5000, wharf_patrol02, null);
		startQuestTimer("3262904", 5000, wharf_patrol03, null);
		startQuestTimer("3262910", 5000, wharf_patrol04, null);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (walks99.containsKey(event))
		{
			int x = (Integer) walks99.get(event)[0];
			int y = (Integer) walks99.get(event)[1];
			int z = (Integer) walks99.get(event)[2];
			String nextEvent = (String) walks99.get(event)[3];
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, z, 0));
			if (((npc.getX() - 100) <= x) && ((npc.getX() + 100) >= x) && ((npc.getY() - 100) <= y) && ((npc.getY() + 100) >= y))
			{
				startQuestTimer(nextEvent, 1000, npc, null);
			}
			else
			{
				startQuestTimer(event, 1000, npc, null);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new NPC_Walk(-1, "npc_walk", "ai");
	}
}