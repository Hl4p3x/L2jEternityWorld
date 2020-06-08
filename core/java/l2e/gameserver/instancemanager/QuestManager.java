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
package l2e.gameserver.instancemanager;

import java.util.Map;
import java.util.logging.Logger;

import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.scripting.L2ScriptEngineManager;
import l2e.gameserver.scripting.ScriptManager;
import l2e.util.L2FastMap;

public class QuestManager extends ScriptManager<Quest>
{
	protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());
	
	private final Map<String, Quest> _quests = new L2FastMap<>(true);
	
	protected QuestManager()
	{
	}
	
	public final boolean reload(String questFolder)
	{
		Quest q = getQuest(questFolder);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	public final boolean reload(int questId)
	{
		Quest q = getQuest(questId);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	public final void reloadAllQuests()
	{
		_log.info("Reloading Server Scripts");
		
		for (Quest quest : _quests.values())
		{
			if (quest != null)
			{
				quest.unload(false);
			}
		}
		_quests.clear();
		L2ScriptEngineManager.getInstance().executeScriptList();
		QuestManager.getInstance().report();
	}
	
	public final void report()
	{
		_log.info("Loaded: " + _quests.size() + " quests");
	}
	
	public final void save()
	{
		for (Quest q : _quests.values())
		{
			q.saveGlobalData();
		}
	}
	
	public final Quest getQuest(String name)
	{
		return _quests.get(name);
	}
	
	public final Quest getQuest(int questId)
	{
		for (Quest q : _quests.values())
		{
			if (q.getId() == questId)
			{
				return q;
			}
		}
		return null;
	}
	
	public final void addQuest(Quest newQuest)
	{
		if (newQuest == null)
		{
			throw new IllegalArgumentException("Quest argument cannot be null");
		}
		Quest old = _quests.get(newQuest.getName());
		
		if (old != null)
		{
			old.unload();
			_log.info("Replaced: (" + old.getName() + ") with a new version (" + newQuest.getName() + ")");
			
		}
		_quests.put(newQuest.getName(), newQuest);
	}
	
	public final boolean removeQuest(Quest q)
	{
		return _quests.remove(q.getName()) != null;
	}
	
	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return _quests.values();
	}
	
	@Override
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
	
	@Override
	public String getScriptManagerName()
	{
		return getClass().getSimpleName();
	}
	
	public static final QuestManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestManager _instance = new QuestManager();
	}
}