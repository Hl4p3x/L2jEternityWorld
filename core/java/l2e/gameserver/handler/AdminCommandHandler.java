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
import l2e.gameserver.handler.admincommandhandlers.AdminAdmin;
import l2e.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import l2e.gameserver.handler.admincommandhandlers.AdminBBS;
import l2e.gameserver.handler.admincommandhandlers.AdminBuffs;
import l2e.gameserver.handler.admincommandhandlers.AdminCHSiege;
import l2e.gameserver.handler.admincommandhandlers.AdminCache;
import l2e.gameserver.handler.admincommandhandlers.AdminCamera;
import l2e.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import l2e.gameserver.handler.admincommandhandlers.AdminClan;
import l2e.gameserver.handler.admincommandhandlers.AdminCreateItem;
import l2e.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import l2e.gameserver.handler.admincommandhandlers.AdminDebug;
import l2e.gameserver.handler.admincommandhandlers.AdminDelete;
import l2e.gameserver.handler.admincommandhandlers.AdminDisconnect;
import l2e.gameserver.handler.admincommandhandlers.AdminDoorControl;
import l2e.gameserver.handler.admincommandhandlers.AdminEditChar;
import l2e.gameserver.handler.admincommandhandlers.AdminEditNpc;
import l2e.gameserver.handler.admincommandhandlers.AdminEffects;
import l2e.gameserver.handler.admincommandhandlers.AdminElement;
import l2e.gameserver.handler.admincommandhandlers.AdminEnchant;
import l2e.gameserver.handler.admincommandhandlers.AdminEventEngine;
import l2e.gameserver.handler.admincommandhandlers.AdminEvents;
import l2e.gameserver.handler.admincommandhandlers.AdminExpSp;
import l2e.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import l2e.gameserver.handler.admincommandhandlers.AdminFortSiege;
import l2e.gameserver.handler.admincommandhandlers.AdminGeoEditor;
import l2e.gameserver.handler.admincommandhandlers.AdminGeodata;
import l2e.gameserver.handler.admincommandhandlers.AdminGm;
import l2e.gameserver.handler.admincommandhandlers.AdminGmChat;
import l2e.gameserver.handler.admincommandhandlers.AdminGraciaSeeds;
import l2e.gameserver.handler.admincommandhandlers.AdminHWIDBan;
import l2e.gameserver.handler.admincommandhandlers.AdminHeal;
import l2e.gameserver.handler.admincommandhandlers.AdminHellbound;
import l2e.gameserver.handler.admincommandhandlers.AdminHelpPage;
import l2e.gameserver.handler.admincommandhandlers.AdminInstance;
import l2e.gameserver.handler.admincommandhandlers.AdminInstanceZone;
import l2e.gameserver.handler.admincommandhandlers.AdminInvul;
import l2e.gameserver.handler.admincommandhandlers.AdminKick;
import l2e.gameserver.handler.admincommandhandlers.AdminKill;
import l2e.gameserver.handler.admincommandhandlers.AdminKrateisCube;
import l2e.gameserver.handler.admincommandhandlers.AdminLevel;
import l2e.gameserver.handler.admincommandhandlers.AdminLogin;
import l2e.gameserver.handler.admincommandhandlers.AdminMammon;
import l2e.gameserver.handler.admincommandhandlers.AdminManor;
import l2e.gameserver.handler.admincommandhandlers.AdminMenu;
import l2e.gameserver.handler.admincommandhandlers.AdminMessages;
import l2e.gameserver.handler.admincommandhandlers.AdminMobGroup;
import l2e.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import l2e.gameserver.handler.admincommandhandlers.AdminMonsterRush;
import l2e.gameserver.handler.admincommandhandlers.AdminPForge;
import l2e.gameserver.handler.admincommandhandlers.AdminPcCondOverride;
import l2e.gameserver.handler.admincommandhandlers.AdminPetition;
import l2e.gameserver.handler.admincommandhandlers.AdminPledge;
import l2e.gameserver.handler.admincommandhandlers.AdminPolymorph;
import l2e.gameserver.handler.admincommandhandlers.AdminPremium;
import l2e.gameserver.handler.admincommandhandlers.AdminPunishment;
import l2e.gameserver.handler.admincommandhandlers.AdminQuest;
import l2e.gameserver.handler.admincommandhandlers.AdminRepairChar;
import l2e.gameserver.handler.admincommandhandlers.AdminRes;
import l2e.gameserver.handler.admincommandhandlers.AdminRide;
import l2e.gameserver.handler.admincommandhandlers.AdminShop;
import l2e.gameserver.handler.admincommandhandlers.AdminShowQuests;
import l2e.gameserver.handler.admincommandhandlers.AdminShutdown;
import l2e.gameserver.handler.admincommandhandlers.AdminSiege;
import l2e.gameserver.handler.admincommandhandlers.AdminSkill;
import l2e.gameserver.handler.admincommandhandlers.AdminSpawn;
import l2e.gameserver.handler.admincommandhandlers.AdminSummon;
import l2e.gameserver.handler.admincommandhandlers.AdminTarget;
import l2e.gameserver.handler.admincommandhandlers.AdminTeleport;
import l2e.gameserver.handler.admincommandhandlers.AdminTerritoryWar;
import l2e.gameserver.handler.admincommandhandlers.AdminTest;
import l2e.gameserver.handler.admincommandhandlers.AdminTvTEvent;
import l2e.gameserver.handler.admincommandhandlers.AdminTvTRoundEvent;
import l2e.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import l2e.gameserver.handler.admincommandhandlers.AdminVitality;
import l2e.gameserver.handler.admincommandhandlers.AdminZone;
import l2e.protection.Protection;
import gnu.trove.map.hash.TIntObjectHashMap;

public class AdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());
	
	private final TIntObjectHashMap<IAdminCommandHandler> _datatable;
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected AdminCommandHandler()
	{
		_datatable = new TIntObjectHashMap<>();
		
		registerHandler(new AdminAdmin());
		registerHandler(new AdminAnnouncements());
		registerHandler(new AdminBBS());
		registerHandler(new AdminBuffs());
		registerHandler(new AdminCache());
		registerHandler(new AdminCamera());
		registerHandler(new AdminChangeAccessLevel());
		registerHandler(new AdminCHSiege());
		registerHandler(new AdminClan());
		registerHandler(new AdminCreateItem());
		registerHandler(new AdminCursedWeapons());
		registerHandler(new AdminDebug());
		registerHandler(new AdminDelete());
		registerHandler(new AdminDisconnect());
		registerHandler(new AdminDoorControl());
		registerHandler(new AdminEditChar());
		registerHandler(new AdminEditNpc());
		registerHandler(new AdminEffects());
		registerHandler(new AdminElement());
		registerHandler(new AdminEnchant());
		registerHandler(new AdminEventEngine());
		registerHandler(new AdminEvents());
		registerHandler(new AdminExpSp());
		registerHandler(new AdminFightCalculator());
		registerHandler(new AdminFortSiege());
		registerHandler(new AdminHellbound());
		registerHandler(new AdminGeodata());
		registerHandler(new AdminGeoEditor());
		registerHandler(new AdminGm());
		registerHandler(new AdminGmChat());
		registerHandler(new AdminGraciaSeeds());
		registerHandler(new AdminHeal());
		registerHandler(new AdminHelpPage());
		if (Protection.isProtectionOn())
		{
			registerHandler(new AdminHWIDBan());
		}
		registerHandler(new AdminInstance());
		registerHandler(new AdminInstanceZone());
		registerHandler(new AdminInvul());
		registerHandler(new AdminKick());
		registerHandler(new AdminKill());
		registerHandler(new AdminKrateisCube());
		registerHandler(new AdminLevel());
		registerHandler(new AdminLogin());
		registerHandler(new AdminMammon());
		registerHandler(new AdminManor());
		registerHandler(new AdminMenu());
		registerHandler(new AdminMessages());
		registerHandler(new AdminMobGroup());
		registerHandler(new AdminMonsterRace());
		registerHandler(new AdminMonsterRush());
		registerHandler(new AdminPcCondOverride());
		registerHandler(new AdminPetition());
		registerHandler(new AdminPForge());
		registerHandler(new AdminPledge());
		registerHandler(new AdminPolymorph());
		registerHandler(new AdminPremium());
		registerHandler(new AdminPunishment());
		registerHandler(new AdminQuest());
		registerHandler(new AdminRepairChar());
		registerHandler(new AdminRes());
		registerHandler(new AdminRide());
		registerHandler(new AdminShop());
		registerHandler(new AdminShowQuests());
		registerHandler(new AdminShutdown());
		registerHandler(new AdminSiege());
		registerHandler(new AdminSkill());
		registerHandler(new AdminSpawn());
		registerHandler(new AdminSummon());
		registerHandler(new AdminTarget());
		registerHandler(new AdminTeleport());
		registerHandler(new AdminTerritoryWar());
		registerHandler(new AdminTest());
		registerHandler(new AdminTvTEvent());
		registerHandler(new AdminTvTRoundEvent());
		registerHandler(new AdminUnblockIp());
		registerHandler(new AdminVitality());
		registerHandler(new AdminZone());
		
		_log.info("Loaded " + _datatable.size() + " AdminCommandHandlers");
	}
	
	public void registerHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String id : ids)
		{
			if (Config.DEBUG)
			{
				_log.fine("Adding handler for command " + id);
			}
			_datatable.put(id.hashCode(), handler);
		}
	}
	
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
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
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}