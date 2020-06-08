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
package l2e.gameserver.handler.admincommandhandlers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.GMAudit;
import l2e.gameserver.util.Util;

public class AdminPunishment implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminPunishment.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_punishment",
		"admin_punishment_add",
		"admin_punishment_remove"
	};
	
	private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		if (!st.hasMoreTokens())
		{
			return false;
		}
		final String cmd = st.nextToken();
		switch (cmd)
		{
			case "admin_punishment":
			{
				if (!st.hasMoreTokens())
				{
					String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "data/html/admin/punishment.htm");
					if (content != null)
					{
						content = content.replaceAll("%punishments%", Util.implode(PunishmentType.values(), ";"));
						content = content.replaceAll("%affects%", Util.implode(PunishmentAffect.values(), ";"));
						activeChar.sendPacket(new NpcHtmlMessage(5, 5, content));
					}
					else
					{
						_log.log(Level.WARNING, getClass().getSimpleName() + ": data/html/admin/punishment.htm is missing");
					}
				}
				else
				{
					final String subcmd = st.nextToken();
					switch (subcmd)
					{
						case "info":
						{
							String key = st.hasMoreTokens() ? st.nextToken() : null;
							String af = st.hasMoreTokens() ? st.nextToken() : null;
							String name = key;
							
							if ((key == null) || (af == null))
							{
								activeChar.sendMessage("Not enough data specified!");
								break;
							}
							final PunishmentAffect affect = PunishmentAffect.getByName(af);
							if (affect == null)
							{
								activeChar.sendMessage("Incorrect value specified for affect type!");
								break;
							}
							
							if (affect == PunishmentAffect.CHARACTER)
							{
								key = findCharId(key);
							}
							
							String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "data/html/admin/punishment-info.htm");
							if (content != null)
							{
								StringBuilder sb = new StringBuilder();
								for (PunishmentType type : PunishmentType.values())
								{
									if (PunishmentManager.getInstance().hasPunishment(key, affect, type))
									{
										long expiration = PunishmentManager.getInstance().getPunishmentExpiration(key, affect, type);
										String expire = "never";
										
										if (expiration > 0)
										{
											synchronized (DATE_FORMATTER)
											{
												expire = DATE_FORMATTER.format(new Date(expiration));
											}
										}
										sb.append("<tr><td><font color=\"LEVEL\">" + type + "</font></td><td>" + expire + "</td><td><a action=\"bypass -h admin_punishment_remove " + name + " " + affect + " " + type + "\">Remove</a></td></tr>");
									}
								}
								
								content = content.replaceAll("%player_name%", name);
								content = content.replaceAll("%punishments%", sb.toString());
								content = content.replaceAll("%affects%", Util.implode(PunishmentAffect.values(), ";"));
								content = content.replaceAll("%affect_type%", affect.name());
								activeChar.sendPacket(new NpcHtmlMessage(5, 5, content));
							}
							else
							{
								_log.log(Level.WARNING, getClass().getSimpleName() + ": data/html/admin/punishment-info.htm is missing");
							}
							break;
						}
						case "player":
						{
							if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
							{
								activeChar.sendMessage("You must target player!");
								break;
							}
							L2PcInstance target = activeChar.getTarget().getActingPlayer();
							String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "data/html/admin/punishment-player.htm");
							if (content != null)
							{
								content = content.replaceAll("%player_name%", target.getName());
								content = content.replaceAll("%punishments%", Util.implode(PunishmentType.values(), ";"));
								content = content.replaceAll("%acc%", target.getAccountName());
								content = content.replaceAll("%char%", target.getName());
								content = content.replaceAll("%ip%", target.getIPAddress());
								activeChar.sendPacket(new NpcHtmlMessage(5, 5, content));
							}
							else
							{
								_log.log(Level.WARNING, getClass().getSimpleName() + ": data/html/admin/punishment-player.htm is missing");
							}
							break;
						}
					}
				}
				break;
			}
			case "admin_punishment_add":
			{
				String key = st.hasMoreTokens() ? st.nextToken() : null;
				String af = st.hasMoreTokens() ? st.nextToken() : null;
				String t = st.hasMoreTokens() ? st.nextToken() : null;
				String exp = st.hasMoreTokens() ? st.nextToken() : null;
				String reason = st.hasMoreTokens() ? st.nextToken() : null;
				
				if (reason != null)
				{
					while (st.hasMoreTokens())
					{
						reason += " " + st.nextToken();
					}
					if (!reason.isEmpty())
					{
						reason = reason.replaceAll("\\$", "\\\\\\$");
						reason = reason.replaceAll("\r\n", "<br1>");
						reason = reason.replace("<", "&lt;");
						reason = reason.replace(">", "&gt;");
					}
				}
				
				String name = key;
				
				if ((key == null) || (af == null) || (t == null) || (exp == null) || (reason == null))
				{
					activeChar.sendMessage("Please fill all the fields!");
					break;
				}
				if (!Util.isDigit(exp) && !exp.equals("-1"))
				{
					activeChar.sendMessage("Incorrect value specified for expiration time!");
					break;
				}
				
				long expirationTime = Integer.parseInt(exp);
				if (expirationTime > 0)
				{
					expirationTime = System.currentTimeMillis() + (expirationTime * 60 * 1000);
				}
				
				final PunishmentAffect affect = PunishmentAffect.getByName(af);
				final PunishmentType type = PunishmentType.getByName(t);
				if ((affect == null) || (type == null))
				{
					activeChar.sendMessage("Incorrect value specified for affect/punishment type!");
					break;
				}
				
				if (affect == PunishmentAffect.CHARACTER)
				{
					key = findCharId(key);
				}
				else if (affect == PunishmentAffect.IP)
				{
					try
					{
						InetAddress addr = InetAddress.getByName(key);
						if (addr.isLoopbackAddress())
						{
							throw new UnknownHostException("You cannot ban any local address!");
						}
						else if (Config.GAME_SERVER_HOSTS.contains(addr.getHostAddress()))
						{
							throw new UnknownHostException("You cannot ban your gameserver's address!");
						}
					}
					catch (UnknownHostException e)
					{
						activeChar.sendMessage("You've entered an incorrect IP address!");
						activeChar.sendMessage(e.getMessage());
						break;
					}
				}
				
				if (PunishmentManager.getInstance().hasPunishment(key, affect, type))
				{
					activeChar.sendMessage("Target is already affected by that punishment.");
					break;
				}
				
				PunishmentManager.getInstance().startPunishment(new PunishmentTask(key, affect, type, expirationTime, reason, activeChar.getName()));
				activeChar.sendMessage("Punishment " + type.name() + " have been applied to: " + affect + " " + name + "!");
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", cmd, affect.name(), name);
				return useAdminCommand("admin_punishment info " + name + " " + affect.name(), activeChar);
			}
			case "admin_punishment_remove":
			{
				String key = st.hasMoreTokens() ? st.nextToken() : null;
				String af = st.hasMoreTokens() ? st.nextToken() : null;
				String t = st.hasMoreTokens() ? st.nextToken() : null;
				String name = key;
				
				if ((key == null) || (af == null) || (t == null))
				{
					activeChar.sendMessage("Not enough data specified!");
					break;
				}
				
				final PunishmentAffect affect = PunishmentAffect.getByName(af);
				final PunishmentType type = PunishmentType.getByName(t);
				if ((affect == null) || (type == null))
				{
					activeChar.sendMessage("Incorrect value specified for affect/punishment type!");
					break;
				}
				
				if (affect == PunishmentAffect.CHARACTER)
				{
					key = findCharId(key);
				}
				
				if (!PunishmentManager.getInstance().hasPunishment(key, affect, type))
				{
					activeChar.sendMessage("Target is not affected by that punishment!");
					break;
				}
				
				PunishmentManager.getInstance().stopPunishment(key, affect, type);
				activeChar.sendMessage("Punishment " + type.name() + " have been stopped to: " + affect + " " + name + "!");
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", cmd, affect.name(), name);
				return useAdminCommand("admin_punishment info " + name + " " + affect.name(), activeChar);
			}
		}
		return true;
	}
	
	private static final String findCharId(String key)
	{
		int charId = CharNameHolder.getInstance().getIdByName(key);
		if (charId > 0)
		{
			return Integer.toString(charId);
		}
		return key;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}