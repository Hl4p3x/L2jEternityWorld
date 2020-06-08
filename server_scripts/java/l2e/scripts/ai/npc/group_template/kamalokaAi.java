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
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class kamalokaAi extends L2AttackableAIScript
{
	private static int Chants_Spawn = 20;
	
	private static final Map<Integer, Integer> KAMALOKASPAWNS = new FastMap<>();
	static
	{
		KAMALOKASPAWNS.put(22452, 22453);
		KAMALOKASPAWNS.put(22455, 22456);
		KAMALOKASPAWNS.put(22458, 22459);
		KAMALOKASPAWNS.put(22461, 22462);
		KAMALOKASPAWNS.put(22464, 22465);
		KAMALOKASPAWNS.put(22467, 22468);
		KAMALOKASPAWNS.put(22470, 22471);
		KAMALOKASPAWNS.put(22473, 22474);
		KAMALOKASPAWNS.put(22476, 22477);
		KAMALOKASPAWNS.put(22479, 22480);
		KAMALOKASPAWNS.put(22482, 22483);
	}
	
	public kamalokaAi(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		int[] temp =
		{
			22452,
			22455,
			22458,
			22461,
			22464,
			22467,
			22470,
			22473,
			22476,
			22479,
			22482
		};
		this.registerMobs(temp, QuestEventType.ON_KILL);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();
		
		if (KAMALOKASPAWNS.containsKey(npcId))
		{
			if (getRandom(100) < Chants_Spawn)
			{
				L2Attackable newNpc = null;
				if (getRandom(100) < 60)
				{
					newNpc = (L2Attackable) addSpawn(KAMALOKASPAWNS.get(npcId), npc);
					newNpc.setTitle("Doppler");
				}
				else
				{
					newNpc = (L2Attackable) addSpawn(((KAMALOKASPAWNS.get(npcId)) + 1), npc);
					newNpc.setTitle("Void");
				}
				
				newNpc.setInstanceId(npc.getInstanceId());
				newNpc.setRunning();
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new kamalokaAi(-1, "kamalokaAi", "ai");
	}
}