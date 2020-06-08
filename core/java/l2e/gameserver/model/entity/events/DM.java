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
package l2e.gameserver.model.entity.events;

import l2e.Config;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2Party.messageType;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.util.Rnd;

public class DM extends FunEvent
{
	@Override
	public void loadConfig()
	{
		EVENT_ID = 4;
		EVENT_NAME = "DM";
		EVENT_AUTO_MODE = Config.DM_AUTO_MODE;
		EVENT_INTERVAL = Config.DM_EVENT_INTERVAL;
		EVENT_NPC_LOC = (new int[]
		{
			Config.DM_NPC_X,
			Config.DM_NPC_Y,
			Config.DM_NPC_Z
		});
		EVENT_NPC_LOC_NAME = Config.DM_NPC_LOC_NAME;
		EVENT_TEAMS_TYPE = Config.DM_EVEN_TEAMS;
		EVENT_PLAYER_LEVEL_MIN = Config.DM_PLAYER_LEVEL_MIN;
		EVENT_PLAYER_LEVEL_MAX = Config.DM_PLAYER_LEVEL_MAX;
		EVENT_COUNTDOWN_TIME = Config.DM_COUNTDOWN_TIME;
		EVENT_MIN_PLAYERS = Config.DM_MIN_PLAYERS;
		EVENT_DOORS_TO_CLOSE = Config.DM_DOORS_TO_CLOSE;
		EVENT_DOORS_TO_OPEN = Config.DM_DOORS_TO_OPEN;
	}
	
	@Override
	public void abortEvent()
	{
		if (_state == State.INACTIVE)
		{
			return;
		}
		
		if (_state == State.PARTICIPATING)
		{
			unspawnManager();
		}
		else if (_state == State.STARTING)
		{
			teleportPlayersBack();
		}
		else if (_state == State.FIGHTING)
		{
			endFight();
			removeDoors();
			teleportPlayersBack();
		}
		_state = State.INACTIVE;
		clearData();
		autoStart();
	}
	
	private void loadData()
	{
	}
	
	private void clearData()
	{
		if (_sheduleNext != null)
		{
			_sheduleNext.cancel(false);
			_sheduleNext = null;
		}
		_players.clear();
		_teams.clear();
	}
	
	private void startFight()
	{
		if (Config.EVENT_INSTANCE)
		{
			_instanceId = InstanceManager.getInstance().createInstance();
		}
		
		for (L2PcInstance player : getAllPlayers())
		{
			Team team = _teams.get(player._eventTeamId);
			
			if (Config.DM_ON_START_UNSUMMON_PET)
			{
				if (player.hasSummon() && (player.getSummon() instanceof L2PetInstance))
				{
					player.getSummon().unSummon(player);
				}
			}
			
			if (Config.DM_ON_START_REMOVE_ALL_EFFECTS)
			{
				player.stopAllEffects();
				if (player.hasSummon())
				{
					player.getSummon().stopAllEffects();
				}
			}
			
			if (player.getParty() != null)
			{
				player.getParty().removePartyMember(player, messageType.Expelled);
			}
			
			if (player.isMounted())
			{
				player.dismount();
			}
			
			player.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "FunEvent.STRING_KILLS") + ": " + player._eventCountKills);
			player.broadcastTitleInfo();
			player.broadcastUserInfo();
			player.setInstanceId(_instanceId);
			player.teleToLocation(team._teamX, team._teamY, team._teamZ);
			player._eventTeleported = true;
		}
	}
	
	private void endFight()
	{
		L2PcInstance winner = null;
		int count = 0;
		for (L2PcInstance player : getAllPlayers())
		{
			if (!player.isOnline())
			{
				continue;
			}
			count++;
			if (!player.isDead())
			{
				winner = player;
			}
		}
		
		if (count == 1)
		{
			winner.sendMessage(LocalizationStorage.getInstance().getString(winner.getLang(), "DM.YOU_WIN"));
			for (String reward : Config.DM_REWARD)
			{
				String[] rew = reward.split(":");
				winner.addItem("DM Event", Integer.parseInt(rew[0]), Integer.parseInt(rew[1]), null, true);
			}
			CustomMessage msg = new CustomMessage("DM.HE_WIN", true);
			msg.add(winner.getName());
			msg.add(_teams.get(winner._eventTeamId)._teamKills);
			AnnounceToPlayers(true, msg);
		}
		else
		{
			AnnounceToPlayers(true, new CustomMessage("DM.DRAW", true));
		}
	}
	
	private void teleportPlayersBack()
	{
		kickPlayersFromEvent();
		InstanceManager.getInstance().destroyInstance(_instanceId);
	}
	
	@Override
	protected void StartNext()
	{
		long delay = 0;
		StatsSet set;
		
		if (_state == State.WAITING)
		{
			delay = Config.DM_COUNTDOWN_TIME * 60000;
			_state = State.PARTICIPATING;
			loadData();
			spawnManager();
			countdown();
			sendConfirmDialog();
		}
		else if (_state == State.PARTICIPATING)
		{
			delay = 20000;
			unspawnManager();
			if (checkPlayersCount())
			{
				teleportPlayers();
			}
			else
			{
				abortEvent();
				return;
			}
			_state = State.STARTING;
		}
		else if (_state == State.STARTING)
		{
			set = new StatsSet();
			
			delay = Config.DM_FIGHT_TIME * 60000;
			_state = State.FIGHTING;
			startFight();
			makeDoors(set);
		}
		else if (_state == State.FIGHTING)
		{
			endFight();
			removeDoors();
			teleportPlayersBack();
			clearData();
			_state = State.INACTIVE;
			autoStart();
			return;
		}
		sheduleNext(delay);
	}
	
	@Override
	public void onPlayerLogin(final L2PcInstance player)
	{
		if (_players.containsKey(player.getObjectId()))
		{
			if (_state == State.STARTING)
			{
				L2PcInstance member = _players.get(player.getObjectId());
				player._eventName = member._eventName;
				player._eventTeamId = member._eventTeamId;
				player._eventOriginalTitle = member._eventOriginalTitle;
				player._eventOriginalNameColor = member._eventOriginalNameColor;
				player._eventOriginalKarma = member._eventOriginalKarma;
				player.getAppearance().setNameColor(member.getAppearance().getNameColor());
				player.setKarma(0);
				player.broadcastUserInfo();
				_players.put(player.getObjectId(), player);
			}
			else if (_state == State.FIGHTING)
			{
				kickPlayerFromEvent(player);
			}
		}
	}
	
	@Override
	public boolean onPlayerDie(final L2PcInstance player, L2PcInstance killer)
	{
		_teams.get(killer._eventTeamId)._teamKills++;
		
		killer._eventCountKills++;
		killer.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "FunEvent.STRING_KILLS") + ": " + killer._eventCountKills);
		killer.broadcastTitleInfo();
		killer.broadcastUserInfo();
		
		player.getStatus().setCurrentHp(player.getMaxHp());
		player.getStatus().setCurrentMp(player.getMaxMp());
		player.getStatus().setCurrentCp(player.getMaxCp());
		player.broadcastStatusUpdate();
		
		player.sendMessage(LocalizationStorage.getInstance().getString(player.getLang(), "DM.YOU_LOSE"));
		kickPlayerFromEvent(player);
		
		int count = 0;
		for (L2PcInstance plr : getAllPlayers())
		{
			if (!plr.isOnline())
			{
				continue;
			}
			count++;
		}
		if (count < 2)
		{
			abortEvent();
		}
		
		return false;
	}
	
	@Override
	protected void teleportPlayers()
	{
		if (EVENT_TEAMS_TYPE.equals("SHUFFLE"))
		{
			int index = 0;
			for (L2PcInstance player : getAllPlayers())
			{
				if (!player.isOnline())
				{
					_players.remove(player.getObjectId());
					continue;
				}
				index++;
				player._eventName = EVENT_NAME;
				player._eventTeamId = index;
				int offset = 500;
				Team team = new Team();
				team._teamId = index;
				team._teamX = Config.DM_START_LOC_X + Rnd.get(-offset, offset);
				team._teamY = Config.DM_START_LOC_Y + Rnd.get(-offset, offset);
				team._teamZ = Config.DM_START_LOC_Z;
				_teams.put(index, team);
			}
		}
		
		for (L2PcInstance player : getAllPlayers())
		{
			updatePlayerInfo(player);
		}
		
		CustomMessage msg = new CustomMessage("FunEvent.PREPARE_ARENA", true);
		msg.add(EVENT_NAME);
		AnnounceToPlayers(false, msg);
	}
	
	@Override
	public NpcHtmlMessage getChatWindow(L2PcInstance player)
	{
		if (_state != State.PARTICIPATING)
		{
			return null;
		}
		
		@SuppressWarnings("unused")
		String teamsInfo;
		teamsInfo = "";
		String countDownTimer = "";
		int timeLeft = getStartNextTime();
		String lang = player.getLang();
		
		if (timeLeft > 60)
		{
			countDownTimer = (timeLeft / 60) + " " + LocalizationStorage.getInstance().getString(lang, "Time.MIN");
		}
		else
		{
			countDownTimer = timeLeft + " " + LocalizationStorage.getInstance().getString(lang, "Time.SEC");
		}
		
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
		
		if (!_players.containsKey(player.getObjectId()))
		{
			String joiningButtons = "<center><button value=\"" + LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_REGISTER") + "\" action=\"bypass -h npc_%objectId%_join 0\" width=200 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>";
			
			npcHtmlMessage.setFile(lang, "data/html/addon/EventManager/DM-joining.htm");
			npcHtmlMessage.replace("%eventName%", EVENT_NAME);
			npcHtmlMessage.replace("%playerLevels%", EVENT_PLAYER_LEVEL_MIN + "-" + EVENT_PLAYER_LEVEL_MAX);
			npcHtmlMessage.replace("%playersCount%", _players.size());
			npcHtmlMessage.replace("%playersCountMin%", EVENT_MIN_PLAYERS);
			npcHtmlMessage.replace("%?ountdownTime%", countDownTimer);
			npcHtmlMessage.replace("%joiningButtons%", joiningButtons);
		}
		else
		{
			npcHtmlMessage.setFile(lang, "data/html/addon/EventManager/DM-joined.htm");
			npcHtmlMessage.replace("%eventName%", EVENT_NAME);
			npcHtmlMessage.replace("%playersCount%", _players.size());
			npcHtmlMessage.replace("%playersCountMin%", EVENT_MIN_PLAYERS);
			npcHtmlMessage.replace("%?ountdownTime%", countDownTimer);
		}
		return npcHtmlMessage;
	}
}