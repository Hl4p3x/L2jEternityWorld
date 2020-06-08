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
package l2e.gameserver.handler;

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.voicedcommandhandlers.AioItem;
import l2e.gameserver.handler.voicedcommandhandlers.Antibot;
import l2e.gameserver.handler.voicedcommandhandlers.AutoLoot;
import l2e.gameserver.handler.voicedcommandhandlers.Banking;
import l2e.gameserver.handler.voicedcommandhandlers.ChangePassword;
import l2e.gameserver.handler.voicedcommandhandlers.ChatAdmin;
import l2e.gameserver.handler.voicedcommandhandlers.Debug;
import l2e.gameserver.handler.voicedcommandhandlers.ExpGain;
import l2e.gameserver.handler.voicedcommandhandlers.Hellbound;
import l2e.gameserver.handler.voicedcommandhandlers.Lang;
import l2e.gameserver.handler.voicedcommandhandlers.LastHeroCmd;
import l2e.gameserver.handler.voicedcommandhandlers.Menu;
import l2e.gameserver.handler.voicedcommandhandlers.MonsterAss;
import l2e.gameserver.handler.voicedcommandhandlers.Online;
import l2e.gameserver.handler.voicedcommandhandlers.OpenAtod;
import l2e.gameserver.handler.voicedcommandhandlers.Repair;
import l2e.gameserver.handler.voicedcommandhandlers.Security;
import l2e.gameserver.handler.voicedcommandhandlers.StatsVCmd;
import l2e.gameserver.handler.voicedcommandhandlers.Survey;
import l2e.gameserver.handler.voicedcommandhandlers.TeleToLeader;
import l2e.gameserver.handler.voicedcommandhandlers.TvTRoundVoicedInfo;
import l2e.gameserver.handler.voicedcommandhandlers.TvTVoicedInfo;
import l2e.gameserver.handler.voicedcommandhandlers.Vote;
import l2e.gameserver.handler.voicedcommandhandlers.VoteReward;
import l2e.gameserver.handler.voicedcommandhandlers.Wedding;
import l2e.protection.ConfigProtection;
import l2e.protection.Protection;
import gnu.trove.map.hash.TIntObjectHashMap;

public class VoicedCommandHandler
{
	private static Logger _log = Logger.getLogger(VoicedCommandHandler.class.getName());
	
	private final TIntObjectHashMap<IVoicedCommandHandler> _datatable;
	
	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected VoicedCommandHandler()
	{
		_datatable = new TIntObjectHashMap<>();
		
		if (Config.ALLOW_AIO_ITEM_COMMAND)
		{
			registerHandler(new AioItem());
		}
		
		if (Config.ANTIBOT_START_CAPTCHA == 1)
		{
			registerHandler(new Antibot());
		}
		
		if (Config.ALLOW_AUTOLOOT_COMMAND)
		{
			registerHandler(new AutoLoot());
		}
		
		if (Config.BANKING_SYSTEM_ENABLED)
		{
			registerHandler(new Banking());
		}
		
		if (Config.ALLOW_CHANGE_PASSWORD)
		{
			registerHandler(new ChangePassword());
		}
		
		if (Config.CHAT_ADMIN)
		{
			registerHandler(new ChatAdmin());
		}
		
		registerHandler(new Menu());
		
		if (Config.MR_ENABLED)
		{
			registerHandler(new MonsterAss());
		}
		
		registerHandler(new Online());
		registerHandler(new OpenAtod());
		
		if (Config.ALLOW_REPAIR_COMMAND)
		{
			registerHandler(new Repair());
		}
		
		if ((Protection.isProtectionOn()) && (ConfigProtection.PROTECT_ENABLE_HWID_LOCK))
		{
			registerHandler(new Security());
		}
		
		if (Config.DEBUG_VOICE_COMMAND)
		{
			registerHandler(new Debug());
		}
		
		if (Config.ALLOW_EXP_GAIN_COMMAND)
		{
			registerHandler(new ExpGain());
		}
		
		if (Config.HELLBOUND_STATUS)
		{
			registerHandler(new Hellbound());
		}
		
		if (Config.MULTILANG_ENABLE && Config.MULTILANG_VOICED_ALLOW)
		{
			registerHandler(new Lang());
		}
		
		registerHandler(new LastHeroCmd());
		registerHandler(new StatsVCmd());
		registerHandler(new Survey());
		
		if (Config.ALLOW_TELETO_LEADER)
		{
			registerHandler(new TeleToLeader());
		}
		
		if (Config.TVT_ALLOW_VOICED_COMMAND)
		{
			registerHandler(new TvTVoicedInfo());
		}
		
		if (Config.TVT_ROUND_ALLOW_VOICED_COMMAND)
		{
			registerHandler(new TvTRoundVoicedInfo());
		}
		
		if (Config.ALLOW_VOTE_REWARD_SYSTEM)
		{
			registerHandler(new Vote());
		}
		
		registerHandler(new VoteReward());
		
		if (Config.ALLOW_WEDDING)
		{
			registerHandler(new Wedding());
		}
		_log.info("Loaded " + _datatable.size() + " VoicedHandlers");
	}
	
	public void registerHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String id : ids)
		{
			if (Config.DEBUG)
			{
				_log.fine("Adding handler for command " + id);
			}
			_datatable.put(id.hashCode(), handler);
		}
	}
	
	public IVoicedCommandHandler getHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		if (Config.DEBUG)
		{
			_log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));
		}
		return _datatable.get(command.hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}