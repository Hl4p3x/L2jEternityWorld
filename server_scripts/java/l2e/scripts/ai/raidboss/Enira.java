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
package l2e.scripts.ai.raidboss;

import java.util.Calendar;
import java.util.Collection;

import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class Enira extends L2AttackableAIScript
{
	private static final int ENIRA = 25625;

	public Enira(int questId, String name, String descr)
	{
		super(questId, name, descr);

		eniraSpawn();
	}

	private void eniraSpawn()
	{
		Calendar _date = Calendar.getInstance();
		final int newSecond = _date.get(Calendar.SECOND);
		final int newMinute = _date.get(Calendar.MINUTE);
		final int newHour = _date.get(Calendar.HOUR);

		final int targetHour = (((24 - newHour) * 60) * 60) * 1000;
		final int extraMinutesAndSeconds = (((60 - newMinute) * 60) + (60 - newSecond)) * 1000;
		final int timerDuration = targetHour + extraMinutesAndSeconds;

		startQuestTimer("enira_spawn", timerDuration, null, null);
	}

	private L2Npc findTemplate(int npcId)
	{
		L2Npc npc = null;
		final Collection<L2Spawn> spawns = SpawnTable.getInstance().getSpawnTable();
		for (L2Spawn spawn : spawns)
		{
			if ((spawn != null) && (spawn.getId() == npcId))
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("enira_spawn"))
		{
			if (getRandom(100) <= 40)
			{
				L2Npc eniraSpawn = findTemplate(ENIRA);
				if (eniraSpawn == null)
				{
					addSpawn(ENIRA, -181989, 208968, 4030, 0, false, 3600000);
				}
			}

			eniraSpawn();
		}

		return null;
	}

	public static void main(String[] args)
	{
		new Enira(-1, "Enira", "ai");
	}
}
