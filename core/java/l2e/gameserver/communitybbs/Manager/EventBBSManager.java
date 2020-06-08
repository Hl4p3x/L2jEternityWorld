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
package l2e.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.scripts.events.LastHero;

/**
 * Created by LordWinter 05.03.2013 Fixed by L2J Eternity-World
 */
public class EventBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(EventBBSManager.class.getName());
	
	protected EventBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsevent"))
		{
			updateHtm(activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.EVENTS") + "");
			
		}
		else if (command.startsWith("_bbsevent;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String event;
			event = st.nextToken();
			
			if (event.equalsIgnoreCase("tvt_join"))
			{
				if (!TvTEvent.isParticipating())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_PARTICIPATION", activeChar.getLang())).toString());
					return;
				}
				if (activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_CURSED_WEAPON", activeChar.getLang())).toString());
					return;
				}
				else if (OlympiadManager.getInstance().isRegistered(activeChar))
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_OLYMPIAD_MODE", activeChar.getLang())).toString());
					return;
				}
				else if (activeChar.getKarma() > 0)
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_GET_KARMA", activeChar.getLang())).toString());
					return;
				}
				else if ((activeChar.getLevel() < Config.TVT_EVENT_MIN_LVL) || (activeChar.getLevel() > Config.TVT_EVENT_MAX_LVL))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_BAD_LVL", activeChar.getLang());
					msg.add(String.valueOf(Config.TVT_EVENT_MIN_LVL));
					msg.add(String.valueOf(Config.TVT_EVENT_MAX_LVL));
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if ((TvTEvent._teams[0].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS) && (TvTEvent._teams[1].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_FULL_PLAYERS", activeChar.getLang());
					msg.add(String.valueOf(Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS));
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if ((Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.TVT_ID, activeChar, Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_PARTICIPANTS_PER_IP", activeChar.getLang());
					msg.add(String.valueOf(AntiFeedManager.getInstance().getLimit(activeChar, Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP)));
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if (TvTEvent.needParticipationFee() && !TvTEvent.hasParticipationFee(activeChar))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_NEED_FEE", activeChar.getLang());
					msg.add(TvTEvent.getParticipationFee());
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if (TvTEvent.isStarted())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_IS_STARTED", activeChar.getLang())).toString());
					return;
				}
				else if (TvTEvent.addParticipant(activeChar))
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_REGISTER", activeChar.getLang())).toString());
				}
				else
				{
					return;
				}
				updateHtm(activeChar);
			}
			else if (event.equalsIgnoreCase("tvt_leave"))
			{
				if (!TvTEvent.isParticipating())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_PARTICIPATION", activeChar.getLang())).toString());
					return;
				}
				
				TvTEvent.removeParticipant(activeChar.getObjectId());
				if (Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP > 0)
				{
					AntiFeedManager.getInstance().removePlayer(AntiFeedManager.TVT_ID, activeChar);
				}
				updateHtm(activeChar);
			}
			else if (event.equalsIgnoreCase("tvtround_join"))
			{
				if (!TvTRoundEvent.isParticipating())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_ROUND_PARTICIPATION", activeChar.getLang())).toString());
					return;
				}
				if (activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_ROUND_CURSED_WEAPON", activeChar.getLang())).toString());
					return;
				}
				else if (OlympiadManager.getInstance().isRegistered(activeChar))
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_ROUND_OLYMPIAD_MODE", activeChar.getLang())).toString());
					return;
				}
				else if (activeChar.getKarma() > 0)
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_ROUND_GET_KARMA", activeChar.getLang())).toString());
					return;
				}
				else if ((activeChar.getLevel() < Config.TVT_ROUND_EVENT_MIN_LVL) || (activeChar.getLevel() > Config.TVT_ROUND_EVENT_MAX_LVL))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_ROUND_BAD_LVL", activeChar.getLang());
					msg.add(String.valueOf(Config.TVT_ROUND_EVENT_MIN_LVL));
					msg.add(String.valueOf(Config.TVT_ROUND_EVENT_MAX_LVL));
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if ((TvTRoundEvent._teams[0].getParticipatedPlayerCount() == Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS) && (TvTRoundEvent._teams[1].getParticipatedPlayerCount() == Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_ROUND_FULL_PLAYERS", activeChar.getLang());
					msg.add(String.valueOf(Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS));
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if ((Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.TVT_ID, activeChar, Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_ROUND_PARTICIPANTS_PER_IP", activeChar.getLang());
					msg.add(String.valueOf(AntiFeedManager.getInstance().getLimit(activeChar, Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP)));
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if (TvTRoundEvent.needParticipationFee() && !TvTRoundEvent.hasParticipationFee(activeChar))
				{
					CustomMessage msg = new CustomMessage("EventBBS.TVT_ROUND_NEED_FEE", activeChar.getLang());
					msg.add(TvTRoundEvent.getParticipationFee());
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if (TvTRoundEvent.isStarted())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_ROUND_IS_STARTED", activeChar.getLang())).toString());
					return;
				}
				else if (TvTRoundEvent.addParticipant(activeChar))
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_ROUND_REGISTER", activeChar.getLang())).toString());
				}
				else
				{
					return;
				}
				updateHtm(activeChar);
			}
			else if (event.equalsIgnoreCase("tvtround_leave"))
			{
				if (!TvTRoundEvent.isParticipating())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.TVT_ROUND_PARTICIPATION", activeChar.getLang())).toString());
					return;
				}
				
				TvTRoundEvent.removeParticipant(activeChar.getObjectId());
				if (Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP > 0)
				{
					AntiFeedManager.getInstance().removePlayer(AntiFeedManager.TVT_ID, activeChar);
				}
				updateHtm(activeChar);
			}
			else if (event.equalsIgnoreCase("lasthero_join"))
			{
				if (LastHero.CurrentState != LastHero.EventState.Registration)
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_NO_REG", activeChar.getLang())).toString());
					return;
				}
				else if ((Config.LH_MAX_PARTICIPANTS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.LAST_HERO_ID, activeChar, Config.LH_MAX_PARTICIPANTS_PER_IP))
				{
					CustomMessage msg = new CustomMessage("EventBBS.LAST_HERO_PARTICIPANTS_PER_IP", activeChar.getLang());
					msg.add(String.valueOf(AntiFeedManager.getInstance().getLimit(activeChar, Config.LH_MAX_PARTICIPANTS_PER_IP)));
					activeChar.sendMessage(msg.toString());
					return;
				}
				else if (activeChar.isInOlympiadMode())
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_OLYMPIAD_MODE", activeChar.getLang())).toString());
					return;
				}
				else if (activeChar.getLevel() < Config.LH_MIN_LEVEL)
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_LOW_LVL", activeChar.getLang())).toString());
					return;
				}
				else if (LastHero.Players.size() > Config.LH_MAX_PATRICIPATE_COUNT)
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_MAX_PLAYERS", activeChar.getLang())).toString());
					return;
				}
				else if (LastHero.Players.contains(activeChar.getName()))
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_REGISTERED", activeChar.getLang())).toString());
					return;
				}
				LastHero.Players.add(activeChar.getName());
				activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_REG", activeChar.getLang())).toString());
				updateHtm(activeChar);
			}
			else if (event.equalsIgnoreCase("lasthero_leave"))
			{
				if (LastHero.CurrentState != LastHero.EventState.Registration)
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_NO_REG", activeChar.getLang())).toString());
					return;
				}
				else if (!LastHero.Players.contains(activeChar.getName()))
				{
					activeChar.sendMessage((new CustomMessage("EventBBS.LAST_HERO_NOT_REG", activeChar.getLang())).toString());
					return;
				}
				
				LastHero.Players.remove(activeChar.getName());
				if (Config.LH_MAX_PARTICIPANTS_PER_IP > 0)
				{
					AntiFeedManager.getInstance().removePlayer(AntiFeedManager.LAST_HERO_ID, activeChar);
				}
				updateHtm(activeChar);
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private void updateHtm(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/43.htm");
		adminReply.replace("%tvt%", TvTstatus(activeChar));
		adminReply.replace("%tvtround%", TvTRoundstatus(activeChar));
		adminReply.replace("%lasthero%", LastHerostatus(activeChar));
		separateAndSend(adminReply.getHtm(), activeChar);
	}
	
	private String TvTstatus(L2PcInstance activeChar)
	{
		final boolean isTVTParticipant = TvTEvent.isPlayerParticipant(activeChar.getObjectId());
		String status;
		
		if (!isTVTParticipant)
		{
			status = "<font color=\"ff0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "EventBBS.UNREGISTER") + "</font>";
		}
		else
		{
			status = "<font color=\"0bd043\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "EventBBS.REGISTER") + "</font>";
		}
		return status;
	}
	
	private String TvTRoundstatus(L2PcInstance activeChar)
	{
		final boolean isTVTRoundParticipant = TvTRoundEvent.isPlayerParticipant(activeChar.getObjectId());
		String status;
		
		if (!isTVTRoundParticipant)
		{
			status = "<font color=\"ff0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "EventBBS.UNREGISTER") + "</font>";
		}
		else
		{
			status = "<font color=\"0bd043\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "EventBBS.REGISTER") + "</font>";
		}
		return status;
	}
	
	private String LastHerostatus(L2PcInstance activeChar)
	{
		String status;
		
		if (!LastHero.Players.contains(activeChar.getName()))
		{
			status = "<font color=\"ff0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "EventBBS.UNREGISTER") + "</font>";
		}
		else
		{
			status = "<font color=\"0bd043\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "EventBBS.REGISTER") + "</font>";
		}
		return status;
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static EventBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventBBSManager _instance = new EventBBSManager();
	}
}