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
package l2e.gameserver.model.quest.AITasks;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;

public final class AggroRangeEnter implements Runnable
{
	private final Quest _quest;
	private final L2Npc _npc;
	private final L2PcInstance _pc;
	private final boolean _isSummon;
	
	public AggroRangeEnter(Quest quest, L2Npc npc, L2PcInstance pc, boolean isSummon)
	{
		_quest = quest;
		_npc = npc;
		_pc = pc;
		_isSummon = isSummon;
	}
	
	@Override
	public void run()
	{
		String res = null;
		try
		{
			res = _quest.onAggroRangeEnter(_npc, _pc, _isSummon);
		}
		catch (Exception e)
		{
			_quest.showError(_pc, e);
		}
		_quest.showResult(_pc, res);
	}
}