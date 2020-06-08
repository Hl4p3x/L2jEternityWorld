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

import java.util.Calendar;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.tasks.UpdateSoDStateTask;
import l2e.gameserver.model.quest.Quest;

public final class SoDManager
{
	private static final Logger _log = Logger.getLogger(SoDManager.class.getName());
	
	public static String ENERGY_SEEDS = "EnergySeeds";
	
	private static final byte SOITYPE = 2;
	private static final byte SOATYPE = 3;
	
	private static final byte SODTYPE = 1;
	private int _SoDTiatKilled = 0;
	private int _SoDState = 1;
	private final Calendar _SoDLastStateChangeDate;
	
	protected SoDManager()
	{
		_log.info("Seed of Destruction Manager: Loaded. Current stage is: " + getCurrentStage());
		_SoDLastStateChangeDate = Calendar.getInstance();
		loadData();
		handleSodStages();
	}
	
	private static int getCurrentStage()
	{
		return ServerVariables.getInt("SoDState", 1);
	}
	
	public void saveData(byte seedType)
	{
		switch (seedType)
		{
			case SODTYPE:
				GlobalVariablesManager.getInstance().storeVariable("SoDState", String.valueOf(_SoDState));
				GlobalVariablesManager.getInstance().storeVariable("SoDTiatKilled", String.valueOf(_SoDTiatKilled));
				GlobalVariablesManager.getInstance().storeVariable("SoDLSCDate", String.valueOf(_SoDLastStateChangeDate.getTimeInMillis()));
				break;
			case SOITYPE:
				break;
			case SOATYPE:
				break;
			default:
				_log.warning(getClass().getSimpleName() + ": Unknown SeedType in SaveData: " + seedType);
				break;
		}
	}
	
	public void loadData()
	{
		if (GlobalVariablesManager.getInstance().isVariableStored("SoDState"))
		{
			_SoDState = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDState"));
			_SoDTiatKilled = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDTiatKilled"));
			_SoDLastStateChangeDate.setTimeInMillis(Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("SoDLSCDate")));
		}
		else
		{
			saveData(SODTYPE);
		}
	}
	
	private void handleSodStages()
	{
		switch (_SoDState)
		{
			case 1:
				break;
			case 2:
				long timePast = System.currentTimeMillis() - _SoDLastStateChangeDate.getTimeInMillis();
				if (timePast >= Config.SOD_STAGE_2_LENGTH)
				{
					setSoDState(1, true);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleEffect(new UpdateSoDStateTask(), Config.SOD_STAGE_2_LENGTH - timePast);
				}
				break;
			case 3:
				setSoDState(1, true);
				break;
			default:
				_log.warning(getClass().getSimpleName() + ": Unknown Seed of Destruction state(" + _SoDState + ")! ");
		}
	}
	
	public void updateSodState()
	{
		final Quest quest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
		if (quest == null)
		{
			_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
		}
		else
		{
			quest.notifyEvent("StopSoDAi", null, null);
		}
	}
	
	public void increaseSoDTiatKilled()
	{
		if (_SoDState == 1)
		{
			_SoDTiatKilled++;
			if (_SoDTiatKilled >= Config.SOD_TIAT_KILL_COUNT)
			{
				setSoDState(2, false);
			}
			saveData(SODTYPE);
			Quest esQuest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
			if (esQuest == null)
			{
				_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
			}
			else
			{
				esQuest.notifyEvent("StartSoDAi", null, null);
			}
		}
	}
	
	public int getSoDTiatKilled()
	{
		return _SoDTiatKilled;
	}
	
	public void setSoDState(int value, boolean doSave)
	{
		_log.info(getClass().getSimpleName() + ": New Seed of Destruction state -> " + value + ".");
		_SoDLastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		_SoDState = value;
		
		if (_SoDState == 1)
		{
			_SoDTiatKilled = 0;
		}
		
		handleSodStages();
		
		if (doSave)
		{
			saveData(SODTYPE);
		}
	}
	
	public long getSoDTimeForNextStateChange()
	{
		switch (_SoDState)
		{
			case 1:
				return -1;
			case 2:
				return ((_SoDLastStateChangeDate.getTimeInMillis() + Config.SOD_STAGE_2_LENGTH) - System.currentTimeMillis());
			case 3:
				return -1;
			default:
				return -1;
		}
	}
	
	public Calendar getSoDLastStateChangeDate()
	{
		return _SoDLastStateChangeDate;
	}
	
	public int getSoDState()
	{
		return _SoDState;
	}
	
	public static final SoDManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SoDManager _instance = new SoDManager();
	}
}