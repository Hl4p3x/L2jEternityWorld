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
package l2e.gameserver.network.clientpackets;

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.communitybbs.CommunityBoard;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.handler.AdminCommandHandler;
import l2e.gameserver.handler.BypassHandler;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.handler.VoicedCommandHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.entity.mods.aio.AioItemNpcs;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.scripting.scriptengine.events.RequestBypassToServerEvent;
import l2e.gameserver.scripting.scriptengine.listeners.talk.RequestBypassToServerListener;
import l2e.gameserver.util.GMAudit;
import l2e.gameserver.util.Util;

public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final List<RequestBypassToServerListener> _listeners = new FastList<RequestBypassToServerListener>().shared();
	
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command))
		{
			return;
		}
		
		if (_command.isEmpty())
		{
			_log.info(activeChar.getName() + " send empty requestbypass");
			activeChar.logout();
			return;
		}
		
		try
		{
			if (_command.startsWith("admin_"))
			{
				String command = _command.split(" ")[0];
				
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
				
				if (ach == null)
				{
					if (activeChar.isGM())
					{
						activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");
					}
					_log.warning(activeChar + " requested not registered admin command '" + command + "'");
					return;
				}
				
				if (!AdminParser.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access rights to use this command!");
					_log.warning("Character " + activeChar.getName() + " tried to use admin command " + command + ", without proper access level!");
					return;
				}
				
				if (AdminParser.getInstance().requireConfirm(command))
				{
					activeChar.setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.substring(6) + " ?");
					activeChar.sendPacket(dlg);
				}
				else
				{
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", _command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"));
					}
					
					ach.useAdminCommand(_command, activeChar);
				}
			}
			else if (_command.startsWith("voiced_"))
			{
				String command = _command.split(" ")[0];
				command = command.substring(7);
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler(command);
				if (vch == null)
				{
					return;
				}
				vch.useVoicedCommand(_command.substring(7), activeChar, null);
			}
			else if (_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				activeChar.setIsUsingAioWh(false);
				activeChar.setIsUsingAioMultisell(false);
				
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				if (Util.isDigit(id))
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));
					
					if ((object != null) && object.isNpc() && (endOfId > 0) && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
					{
						((L2Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
				}
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("item_"))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(5, endOfId);
				}
				else
				{
					id = _command.substring(5);
				}
				try
				{
					L2ItemInstance item = activeChar.getInventory().getItemByObjectId(Integer.parseInt(id));
					
					if ((item != null) && (endOfId > 0))
					{
						item.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
					
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe)
				{
					_log.log(Level.WARNING, "NFE for command [" + _command + "]", nfe);
				}
			}
			else if (_command.startsWith("phoenix "))
			{
				Interface.bypass(activeChar.getObjectId(), _command.substring(8));
			}
			else if (_command.startsWith("eventmanager"))
			{
				Interface.bypass(activeChar.getObjectId(), _command.substring(13));
			}
			else if (_command.startsWith("summon_"))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				int endOfId = _command.indexOf('_', 8);
				String id;
				
				if (endOfId > 0)
				{
					id = _command.substring(7, endOfId);
				}
				else
				{
					id = _command.substring(7);
				}
				
				if (Util.isDigit(id))
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));
					
					if ((object instanceof L2MerchantSummonInstance) && (endOfId > 0) && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
					{
						((L2MerchantSummonInstance) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
				}
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("manor_menu_select"))
			{
				final IBypassHandler manor = BypassHandler.getInstance().getHandler("manor_menu_select");
				if (manor != null)
				{
					manor.useBypass(_command, activeChar, null);
				}
			}
			else if (_command.startsWith("_bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_friendlist_0_"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_maillist_0_1_0_"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("Quest "))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
				{
					activeChar.processQuestEvent(p, "");
				}
				else
				{
					activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
				}
			}
			else if (_command.startsWith("_match"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_diary"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("Aioitem"))
			{
				if (Config.ENABLE_AIO_NPCS)
				{
					String sub = _command.substring(8);
					AioItemNpcs.onBypassFeedback(activeChar, sub);
				}
			}
			else if (_command.startsWith("surveymessage"))
			{
				String surveymsg = "";
				StringTokenizer stringTokenizer = new StringTokenizer(_command);
				stringTokenizer.nextToken();
				
				while (stringTokenizer.hasMoreTokens())
				{
					surveymsg = surveymsg + stringTokenizer.nextToken() + " ";
				}
				
				if (surveymsg == "")
				{
					ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.FILL") + "", 4000);
					activeChar.sendPacket(message1);
					return;
				}
				
				if (!((L2PcInstance.lastsurvey + L2PcInstance.surveyDelay) < System.currentTimeMillis()))
				{
					ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.ABLE_START") + "", 4000);
					activeChar.sendPacket(message1);
					return;
				}
				if (surveymsg.length() >= 55)
				{
					ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.MUST_NOT") + "", 4000);
					activeChar.sendPacket(message1);
					return;
				}
				for (L2PcInstance players : L2World.getInstance().getAllPlayersArray())
				{
					players.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.MANAGER") + "", "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.PLAYER") + " " + activeChar.getName() + " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.STARTED") + ""));
					players.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.MANAGER") + "", "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.USE") + " .yes " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.OR") + " .no " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.TO_VOTE") + ""));
					ExShowScreenMessage message1 = new ExShowScreenMessage(activeChar.getName() + ": " + surveymsg + " | .Yes " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.OR") + " .No", 8000);
					players.sendPacket(message1);
					players.sendPacket(new CreatureSay(0, Say2.BATTLEFIELD, activeChar.getName(), surveymsg));
				}
				activeChar.Surveystatus();
				for (L2PcInstance all : L2World.getInstance().getAllPlayersArray())
				{
					all.setSurveyer(true);
				}
				L2PcInstance._survey_running = true;
				
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
							{
								ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.UNTIL") + "", 8000);
								player.sendPacket(message1);
							}
						}
						catch (Exception e)
						{
						}
					}
				}, ((Config.TIME_SURVEY * 60000) - 60000));
				
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							for (L2PcInstance all : L2World.getInstance().getAllPlayersArray())
							{
								all.setSurveyer(false);
							}
							
							for (L2PcInstance players : L2World.getInstance().getAllPlayersArray())
							{
								ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.ENDED") + "", 8000);
								players.sendPacket(message1);
								players.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.RESULTS") + "", L2PcInstance.getYes() + " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.VOTES") + " yes | " + L2PcInstance.getNo() + " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.VOTES") + " no"));
							}
							L2PcInstance._survey_running = false;
							L2PcInstance.midenismos();
						}
						catch (Exception e)
						{
						}
					}
				}, Config.TIME_SURVEY * 60000);
			}
			else if (_command.startsWith("_olympiad?command"))
			{
				int arenaId = Integer.parseInt(_command.split("=")[2]);
				final IBypassHandler handler = BypassHandler.getInstance().getHandler("arenachange");
				if (handler != null)
				{
					handler.useBypass("arenachange " + (arenaId - 1), activeChar, null);
				}
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					handler.useBypass(_command, activeChar, null);
				}
				else
				{
					_log.log(Level.WARNING, getClient() + " sent not handled RequestBypassToServer: [" + _command + "]");
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClient() + " sent bad RequestBypassToServer: \"" + _command + "\"", e);
			if (activeChar.isGM())
			{
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: " + e + "<br1>");
				sb.append("Bypass command: " + _command + "<br1>");
				sb.append("StackTrace:<br1>");
				for (StackTraceElement ste : e.getStackTrace())
				{
					sb.append(ste.toString() + "<br1>");
				}
				sb.append("</body></html>");
				
				NpcHtmlMessage msg = new NpcHtmlMessage(0, 12807);
				msg.setHtml(sb.toString());
				msg.disableValidation();
				activeChar.sendPacket(msg);
			}
		}
		fireBypassListeners();
	}
	
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null)
		{
			return;
		}
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, activeChar.getLocation());
		}
	}
	
	private void fireBypassListeners()
	{
		RequestBypassToServerEvent event = new RequestBypassToServerEvent();
		event.setActiveChar(getActiveChar());
		event.setCommand(_command);
		
		for (RequestBypassToServerListener listener : _listeners)
		{
			listener.onRequestBypassToServer(event);
		}
	}
	
	public static void addBypassListener(RequestBypassToServerListener listener)
	{
		if (!_listeners.contains(listener))
		{
			_listeners.add(listener);
		}
	}
	
	public static void removeBypassListener(RequestBypassToServerListener listener)
	{
		if (_listeners.contains(listener))
		{
			_listeners.remove(listener);
		}
	}
}