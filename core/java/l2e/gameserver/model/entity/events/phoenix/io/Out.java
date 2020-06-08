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
package l2e.gameserver.model.entity.events.phoenix.io;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.handler.AdminCommandHandler;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.handler.SkillHandler;
import l2e.gameserver.handler.VoicedCommandHandler;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.ManagerNpc;
import l2e.gameserver.model.entity.events.phoenix.container.EventContainer;
import l2e.gameserver.model.entity.events.phoenix.container.PlayerContainer;
import l2e.gameserver.model.entity.events.phoenix.function.Vote;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Broadcast;
import l2e.util.Rnd;

public class Out
{
	protected static class BombHandler implements ISkillHandler
	{
		private final L2SkillType[] SKILL_IDS =
		{
			L2SkillType.BOMB
		};
		
		@Override
		public L2SkillType[] getSkillIds()
		{
			return SKILL_IDS;
		}
		
		@Override
		public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
		{
			if (!(activeChar.isPlayer()))
			{
				return;
			}
			
			PlayerContainer.getInstance().getPlayer(activeChar.getObjectId()).getEvent().dropBomb(PlayerContainer.getInstance().getPlayer(((L2PcInstance) activeChar).getObjectId()));
		}
	}
	
	protected static class CaptureHandler implements ISkillHandler
	{
		private final L2SkillType[] SKILL_IDS =
		{
			L2SkillType.CAPTURE
		};
		
		@Override
		public L2SkillType[] getSkillIds()
		{
			return SKILL_IDS;
		}
		
		@Override
		public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
		{
			if (!(activeChar.isPlayer()))
			{
				return;
			}
			
			if (!(targets[0] instanceof L2NpcInstance))
			{
				return;
			}
			L2PcInstance player = (L2PcInstance) activeChar;
			L2NpcInstance target = (L2NpcInstance) targets[0];
			PlayerContainer.getInstance().getPlayer(activeChar.getObjectId()).getEvent().useCapture(PlayerContainer.getInstance().getPlayer(player.getObjectId()), target.getObjectId());
		}
	}
	
	protected static class ReloadHandler implements IAdminCommandHandler
	{
		private final String[] ADMIN_COMMANDS =
		{
			"admin_reload_event_Configuration"
		};
		
		@Override
		public String[] getAdminCommandList()
		{
			return ADMIN_COMMANDS;
		}
		
		@Override
		public boolean useAdminCommand(String command, L2PcInstance activeChar)
		{
			if (command.startsWith("admin_reload_event_Configuration"))
			{
				Configuration.getInstance().load();
			}
			return true;
		}
	}
	
	protected static class KickHandler implements IAdminCommandHandler
	{
		private final String[] ADMIN_COMMANDS =
		{
			"admin_eventkick"
		};
		
		@Override
		public String[] getAdminCommandList()
		{
			return ADMIN_COMMANDS;
		}
		
		@Override
		public boolean useAdminCommand(String command, L2PcInstance activeChar)
		{
			if (command.startsWith("admin_eventkick "))
			{
				EventPlayer p = PlayerContainer.getInstance().getPlayerByName(command.substring(16));
				if (p != null)
				{
					p.getEvent().onLogout(p);
				}
			}
			return true;
		}
	}
	
	protected static class CreateEventHandler implements IAdminCommandHandler
	{
		private final String[] ADMIN_COMMANDS =
		{
			"admin_createevent"
		};
		
		@Override
		public String[] getAdminCommandList()
		{
			return ADMIN_COMMANDS;
		}
		
		@Override
		public boolean useAdminCommand(String command, L2PcInstance activeChar)
		{
			if (command.startsWith("admin_createevent "))
			{
				EventContainer.getInstance().createEvent(Integer.parseInt(command.substring(18)));
			}
			return true;
		}
	}
	
	protected static class VoicedHandler implements IVoicedCommandHandler
	{
		private static final String[] _voicedCommands =
		{
			"event",
			"popup"
		};
		
		@Override
		public String[] getVoicedCommandList()
		{
			return _voicedCommands;
		}
		
		@Override
		public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
		{
			if (command.equalsIgnoreCase("event"))
			{
				ManagerNpc.getInstance().showMain(activeChar.getObjectId());
			}
			if (command.equalsIgnoreCase("popup"))
			{
				if (Configuration.getInstance().getBoolean(0, "voteEnabled"))
				{
					Vote.getInstance().switchPopup(activeChar.getObjectId());
				}
			}
			return true;
		}
	}
	
	public static void broadcastCreatureSay(String message)
	{
		Broadcast.toAllOnlinePlayers(new CreatureSay(0, 18, "", message));
	}
	
	public static void createInstance(int id)
	{
		InstanceManager.getInstance().createInstance(id);
	}
	
	public static void createParty2(FastList<EventPlayer> players)
	{
		L2Party party = null;
		party = new L2Party(players.get(0).getOwner(), 1);
		
		for (EventPlayer player : players.subList(1, players.size()))
		{
			player.joinParty(party);
		}
	}
	
	public static int getClassIndex(int player)
	{
		return getPlayerById(player).getClassIndex();
	}
	
	public static L2PcInstance getPlayerById(int id)
	{
		return L2World.getInstance().getPlayer(id);
	}
	
	public static String getSkillName(int skill)
	{
		return SkillHolder.getInstance().getInfo(skill, 1).getName();
	}
	
	public static void html(Integer player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html);
		getPlayerById(player).sendPacket(msg);
	}
	
	public static boolean isPotion(int item)
	{
		if (ItemHolder.getInstance().getTemplate(item).getItemType() == L2EtcItemType.POTION)
		{
			return true;
		}
		return false;
	}
	
	public static boolean isRestrictedSkill(int skill)
	{
		if (SkillHolder.getInstance().getInfo(skill, 1).getSkillType() == L2SkillType.RESURRECT)
		{
			return true;
		}
		
		if (SkillHolder.getInstance().getInfo(skill, 1).hasEffectType(L2EffectType.TELEPORT))
		{
			return true;
		}
		
		if (SkillHolder.getInstance().getInfo(skill, 1).hasEffectType(L2EffectType.CALLPC))
		{
			return true;
		}
		return false;
	}
	
	public static boolean isScroll(int item)
	{
		if (ItemHolder.getInstance().getTemplate(item).getItemType() == L2EtcItemType.SCROLL)
		{
			return true;
		}
		return false;
		
	}
	
	public static int random(int max)
	{
		return Rnd.get(max);
	}
	
	public static void registerHandlers()
	{
		SkillHandler.getInstance().registerHandler(new BombHandler());
		SkillHandler.getInstance().registerHandler(new CaptureHandler());
		
		AdminCommandHandler.getInstance().registerHandler(new ReloadHandler());
		AdminCommandHandler.getInstance().registerHandler(new KickHandler());
		AdminCommandHandler.getInstance().registerHandler(new CreateEventHandler());
		VoicedCommandHandler.getInstance().registerHandler(new VoicedHandler());
	}
	
	public static void sendMessage(int player, String message)
	{
		getPlayerById(player).sendMessage(message);
	}
	
	public static void setPvPInstance(int id)
	{
		InstanceManager.getInstance().getInstance(id).setPvPInstance(true);
	}
	
	public static void startFlameEffect(Integer npc)
	{
		((L2Npc) L2World.getInstance().findObject(npc)).startAbnormalEffect(AbnormalEffect.FLAME);
	}
	
	public static void tpmPurge()
	{
		ThreadPoolManager.getInstance().purge();
	}
	
	public static ScheduledFuture<?> tpmScheduleGeneral(Runnable task, int time)
	{
		return ThreadPoolManager.getInstance().scheduleGeneral(task, time);
	}
	
	public static void tpmScheduleGeneralAtFixedRate(Runnable task, int first, int delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(task, first, delay);
	}
	
	public static Collection<Integer> getEveryPlayer()
	{
		List<Integer> l = new LinkedList<>();
		for (Integer p : L2World.getInstance().getAllPlayers().keys())
		{
			l.add(p);
		}
		return l;
	}
}