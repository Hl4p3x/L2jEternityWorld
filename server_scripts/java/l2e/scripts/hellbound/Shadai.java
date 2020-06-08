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
package l2e.scripts.hellbound;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.quest.Quest;

public class Shadai extends Quest
{
	private static final int SHADAI = 32347;
	
	private static final int[] DAY_COORDS =
	{
		16882,
		238952,
		9776
	};
	private static final int[] NIGHT_COORDS =
	{
		9064,
		253037,
		-1928
	};
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ValidatePosition(npc), 60000, 60000);
		}
		
		return super.onSpawn(npc);
	}
	
	protected static void validatePosition(L2Npc npc)
	{
		int[] coords = DAY_COORDS;
		boolean mustRevalidate = false;
		if ((npc.getX() != NIGHT_COORDS[0]) && GameTimeController.getInstance().isNight())
		{
			coords = NIGHT_COORDS;
			mustRevalidate = true;
		}
		else if ((npc.getX() != DAY_COORDS[0]) && !GameTimeController.getInstance().isNight())
		{
			mustRevalidate = true;
		}
		
		if (mustRevalidate)
		{
			npc.getSpawn().setX(coords[0]);
			npc.getSpawn().setY(coords[1]);
			npc.getSpawn().setZ(coords[2]);
			npc.teleToLocation(coords[0], coords[1], coords[2]);
		}
	}
	
	private static class ValidatePosition implements Runnable
	{
		private final L2Npc _npc;
		
		public ValidatePosition(L2Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			validatePosition(_npc);
		}
	}
	
	public Shadai(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addSpawnId(SHADAI);
	}
	
	public static void main(String[] args)
	{
		new Shadai(-1, "Shadai", "hellbound");
	}
}