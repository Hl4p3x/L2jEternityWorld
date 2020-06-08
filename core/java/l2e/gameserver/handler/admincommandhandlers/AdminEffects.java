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

import java.util.Collection;
import java.util.StringTokenizer;

import l2e.Config;
import l2e.gameserver.communitybbs.Manager.RegionBBSManager;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ChestInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.Earthquake;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ExRedSky;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SSQInfo;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SunRise;
import l2e.gameserver.network.serverpackets.SunSet;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.util.Broadcast;

public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_invis",
		"admin_invisible",
		"admin_setinvis",
		"admin_vis",
		"admin_visible",
		"admin_invis_menu",
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_bighead",
		"admin_shrinkhead",
		"admin_gmspeed",
		"admin_gmspeed_menu",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_polyself",
		"admin_unpolyself",
		"admin_polyself_menu",
		"admin_unpolyself_menu",
		"admin_clearteams",
		"admin_setteam_close",
		"admin_setteam",
		"admin_social",
		"admin_effect",
		"admin_social_menu",
		"admin_special",
		"admin_special_menu",
		"admin_effect_menu",
		"admin_abnormal",
		"admin_abnormal_menu",
		"admin_play_sounds",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu",
		"admin_set_displayeffect",
		"admin_set_displayeffect_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
		
		if (command.equals("admin_invis_menu"))
		{
			if (!activeChar.isInvisible())
			{
				activeChar.setInvisible(true);
				activeChar.broadcastUserInfo();
				activeChar.decayMe();
				activeChar.spawnMe();
			}
			else
			{
				activeChar.setInvisible(false);
				activeChar.broadcastUserInfo();
			}
			RegionBBSManager.getInstance().changeCommunityBoard(activeChar);
			command = "";
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_invis"))
		{
			activeChar.setInvisible(false);
			activeChar.broadcastUserInfo();
			activeChar.decayMe();
			activeChar.spawnMe();
			RegionBBSManager.getInstance().changeCommunityBoard(activeChar);
		}
		else if (command.startsWith("admin_setinvis"))
		{
			if ((activeChar.getTarget() == null) || !activeChar.getTarget().isCharacter())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			final L2Character target = (L2Character) activeChar.getTarget();
			target.setInvisible(!target.isInvisible());
			activeChar.sendMessage("You've made " + target.getName() + " " + (target.isInvisible() ? "invisible" : "visible") + ".");
		}
		else if (command.startsWith("admin_vis"))
		{
			activeChar.setInvisible(false);
			activeChar.broadcastUserInfo();
			RegionBBSManager.getInstance().changeCommunityBoard(activeChar);
		}
		else if (command.startsWith("admin_earthquake"))
		{
			try
			{
				String val1 = st.nextToken();
				int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				int duration = Integer.parseInt(val2);
				Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				activeChar.broadcastPacket(eq);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //earthquake <intensity> <duration>");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				int duration = Integer.parseInt(st.nextToken());
				adminAtmosphere(type, state, duration, activeChar);
			}
			catch (Exception ex)
			{
				activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red> <duration>");
			}
		}
		else if (command.equals("admin_play_sounds"))
		{
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/songs/songs.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_play_sounds"))
		{
			try
			{
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/songs/songs" + command.substring(18) + ".htm");
				activeChar.sendPacket(adminhtm);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sounds <pagenumber>");
			}
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sound <soundname>");
			}
		}
		else if (command.equals("admin_para_all"))
		{
			try
			{
				Collection<L2PcInstance> plrs = activeChar.getKnownList().getKnownPlayers().values();
				for (L2PcInstance player : plrs)
				{
					if (!player.isGM())
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_1);
						player.setIsParalyzed(true);
						player.startParalyze();
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.equals("admin_unpara_all"))
		{
			try
			{
				Collection<L2PcInstance> plrs = activeChar.getKnownList().getKnownPlayers().values();
				for (L2PcInstance player : plrs)
				{
					player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					player.setIsParalyzed(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_para"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch (Exception e)
			{
			}
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					if (type.equals("1"))
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_1);
					}
					else
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_2);
					}
					player.setIsParalyzed(true);
					player.startParalyze();
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unpara"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch (Exception e)
			{
			}
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					if (type.equals("1"))
					{
						player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					}
					else
					{
						player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
					}
					player.setIsParalyzed(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_bighead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_shrinkhead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_gmspeed"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				activeChar.stopSkillEffects(7029);
				if ((val >= 1) && (val <= 4))
				{
					L2Skill gmSpeedSkill = SkillHolder.getInstance().getInfo(7029, val);
					activeChar.doSimultaneousCast(gmSpeedSkill);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //gmspeed <value> (0=off...4=max)");
			}
			if (command.contains("_menu"))
			{
				command = "";
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
				activeChar.sendPacket(adminhtm);
			}
		}
		else if (command.startsWith("admin_polyself"))
		{
			try
			{
				String id = st.nextToken();
				activeChar.getPoly().setPolyInfo("npc", id);
				activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
				CharInfo info1 = new CharInfo(activeChar);
				activeChar.broadcastPacket(info1);
				UserInfo info2 = new UserInfo(activeChar);
				activeChar.sendPacket(info2);
				activeChar.broadcastPacket(new ExBrExtraUserInfo(activeChar));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //polyself <npcId>");
			}
		}
		else if (command.startsWith("admin_unpolyself"))
		{
			activeChar.getPoly().setPolyInfo(null, "1");
			activeChar.decayMe();
			activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			CharInfo info1 = new CharInfo(activeChar);
			activeChar.broadcastPacket(info1);
			UserInfo info2 = new UserInfo(activeChar);
			activeChar.sendPacket(info2);
			activeChar.broadcastPacket(new ExBrExtraUserInfo(activeChar));
		}
		else if (command.equals("admin_clearteams"))
		{
			try
			{
				Collection<L2PcInstance> plrs = activeChar.getKnownList().getKnownPlayers().values();
				for (L2PcInstance player : plrs)
				{
					player.setTeam(0);
					player.broadcastUserInfo();
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_setteam_close"))
		{
			try
			{
				String val = st.nextToken();
				int radius = 400;
				if (st.hasMoreTokens())
				{
					radius = Integer.parseInt(st.nextToken());
				}
				int teamVal = Integer.parseInt(val);
				Collection<L2Character> plrs = activeChar.getKnownList().getKnownCharactersInRadius(radius);
				
				for (L2Character player : plrs)
				{
					player.setTeam(teamVal);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam_close <teamId>");
			}
		}
		else if (command.startsWith("admin_setteam"))
		{
			try
			{
				String val = st.nextToken();
				int teamVal = Integer.parseInt(val);
				L2Character target = null;
				if (activeChar.getTarget() instanceof L2Character)
				{
					target = (L2Character) activeChar.getTarget();
				}
				else
				{
					return false;
				}
				target.setTeam(teamVal);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam <teamId>");
			}
		}
		else if (command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					int social = Integer.parseInt(st.nextToken());
					target = st.nextToken();
					if (target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performSocial(social, player, activeChar))
							{
								activeChar.sendMessage(player.getName() + " was affected by your request.");
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
								for (L2Object object : objs)
								{
									if (activeChar.isInsideRadius(object, radius, false, false))
									{
										performSocial(social, object, activeChar);
									}
								}
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Incorrect parameter");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int social = Integer.parseInt(st.nextToken());
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performSocial(social, obj, activeChar))
					{
						activeChar.sendMessage(obj.getName() + " was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_abnormal"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					String parm = st.nextToken();
					int abnormal = Integer.decode("0x" + parm);
					target = st.nextToken();
					if (target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performAbnormal(abnormal, player))
							{
								activeChar.sendMessage(player.getName() + "'s abnormal status was affected by your request.");
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
								
								for (L2Object object : objs)
								{
									if (activeChar.isInsideRadius(object, radius, false, false))
									{
										performAbnormal(abnormal, object);
									}
								}
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int abnormal = Integer.decode("0x" + st.nextToken());
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performAbnormal(abnormal, obj))
					{
						activeChar.sendMessage(obj.getName() + "'s abnormal status was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //abnormal <abnormal_mask> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_special"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					String parm = st.nextToken();
					int special = Integer.decode("0x" + parm);
					target = st.nextToken();
					if (target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performSpecial(special, player))
							{
								activeChar.sendMessage(player.getName() + "'s special status was affected by your request.");
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
								for (L2Object object : objs)
								{
									if (activeChar.isInsideRadius(object, radius, false, false))
									{
										performSpecial(special, object);
									}
								}
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //special <hex_special_mask> [player|radius]");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int special = Integer.decode("0x" + st.nextToken());
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performSpecial(special, obj))
					{
						activeChar.sendMessage(obj.getName() + "'s special status was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //special <special_mask> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();
				int level = 1, hittime = 1;
				int skill = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens())
				{
					hittime = Integer.parseInt(st.nextToken());
				}
				if (obj == null)
				{
					obj = activeChar;
				}
				if (!(obj instanceof L2Character))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else
				{
					L2Character target = (L2Character) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
				}
				
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		}
		else if (command.startsWith("admin_set_displayeffect"))
		{
			L2Object target = activeChar.getTarget();
			if (!(target instanceof L2Npc))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			L2Npc npc = (L2Npc) target;
			try
			{
				String type = st.nextToken();
				int diplayeffect = Integer.parseInt(type);
				npc.setDisplayEffect(diplayeffect);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //set_displayeffect <id>");
			}
		}
		if (command.contains("menu"))
		{
			showMainPage(activeChar, command);
		}
		return true;
	}
	
	private boolean performAbnormal(int action, L2Object target)
	{
		if (target instanceof L2Character)
		{
			L2Character character = (L2Character) target;
			if ((character.getAbnormalEffect() & action) == action)
			{
				character.stopAbnormalEffect(action);
			}
			else
			{
				character.startAbnormalEffect(action);
			}
			return true;
		}
		return false;
	}
	
	private boolean performSpecial(int action, L2Object target)
	{
		if (target instanceof L2PcInstance)
		{
			L2Character character = (L2Character) target;
			if ((character.getSpecialEffect() & action) == action)
			{
				character.stopSpecialEffect(action);
			}
			else
			{
				character.startSpecialEffect(action);
			}
			return true;
		}
		return false;
	}
	
	private boolean performSocial(int action, L2Object target, L2PcInstance activeChar)
	{
		try
		{
			if (target instanceof L2Character)
			{
				if (target instanceof L2ChestInstance)
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if ((target instanceof L2Npc) && ((action < 1) || (action > 3)))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if ((target instanceof L2PcInstance) && ((action < 2) || ((action > 18) && (action != SocialAction.LEVEL_UP))))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				L2Character character = (L2Character) target;
				character.broadcastPacket(new SocialAction(character.getObjectId(), action));
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
		}
		return true;
	}
	
	private void adminAtmosphere(String type, String state, int duration, L2PcInstance activeChar)
	{
		L2GameServerPacket packet = null;
		
		if (type.equals("signsky"))
		{
			if (state.equals("dawn"))
			{
				packet = new SSQInfo(2);
			}
			else if (state.equals("dusk"))
			{
				packet = new SSQInfo(1);
			}
		}
		else if (type.equals("sky"))
		{
			if (state.equals("night"))
			{
				packet = SunSet.STATIC_PACKET;
			}
			else if (state.equals("day"))
			{
				packet = SunRise.STATIC_PACKET;
			}
			else if (state.equals("red"))
			{
				if (duration != 0)
				{
					packet = new ExRedSky(duration);
				}
				else
				{
					packet = new ExRedSky(10);
				}
			}
		}
		else
		{
			activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red> <duration>");
		}
		if (packet != null)
		{
			Broadcast.toAllOnlinePlayers(packet);
		}
	}
	
	private void playAdminSound(L2PcInstance activeChar, String sound)
	{
		PlaySound _snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
		activeChar.sendPacket(_snd);
		activeChar.broadcastPacket(_snd);
		activeChar.sendMessage("Playing " + sound + ".");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(L2PcInstance activeChar, String command)
	{
		NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
		adminhtm.setFile(activeChar.getLang(), "data/html/admin/effects_menu.htm");
		activeChar.sendPacket(adminhtm);
		
		if (command.contains("abnormal"))
		{
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/abnormal.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.contains("special"))
		{
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/special.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.contains("social"))
		{
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/social.htm");
			activeChar.sendPacket(adminhtm);
		}
	}
}