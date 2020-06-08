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
package l2e.gameserver.handler.actionshifthandlers;

import l2e.Config;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.util.Util;

public class L2NpcActionShift implements IActionHandler
{
	private static final int[] CHESTS =
	{
		18265,
		18266,
		18267,
		18268,
		18269,
		18270,
		18271,
		18272,
		18273,
		18274,
		18275,
		18276,
		18277,
		18278,
		18279,
		18280,
		18281,
		18282,
		18283,
		18284,
		18285,
		18286
	};
	
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (Util.contains(CHESTS, ((L2Npc) target).getId()))
		{
			return false;
		}
		
		if (activeChar.getAccessLevel().isGm())
		{
			activeChar.setTarget(target);
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(activeChar.getLang(), "data/html/admin/npcinfo.htm");
			
			html.replace("%objid%", String.valueOf(target.getObjectId()));
			html.replace("%class%", target.getClass().getSimpleName());
			html.replace("%id%", String.valueOf(((L2Npc) target).getTemplate().getId()));
			html.replace("%lvl%", String.valueOf(((L2Npc) target).getTemplate().getLevel()));
			html.replace("%name%", String.valueOf(((L2Npc) target).getTemplate().getName()));
			html.replace("%tmplid%", String.valueOf(((L2Npc) target).getTemplate().getId()));
			html.replace("%aggro%", String.valueOf((target instanceof L2Attackable) ? ((L2Attackable) target).getAggroRange() : 0));
			html.replace("%hp%", String.valueOf((int) ((L2Character) target).getCurrentHp()));
			html.replace("%hpmax%", String.valueOf(((L2Character) target).getMaxHp()));
			html.replace("%mp%", String.valueOf((int) ((L2Character) target).getCurrentMp()));
			html.replace("%mpmax%", String.valueOf(((L2Character) target).getMaxMp()));
			
			html.replace("%patk%", String.valueOf(((L2Character) target).getPAtk(null)));
			html.replace("%matk%", String.valueOf(((L2Character) target).getMAtk(null, null)));
			html.replace("%pdef%", String.valueOf(((L2Character) target).getPDef(null)));
			html.replace("%mdef%", String.valueOf(((L2Character) target).getMDef(null, null)));
			html.replace("%accu%", String.valueOf(((L2Character) target).getAccuracy()));
			html.replace("%evas%", String.valueOf(((L2Character) target).getEvasionRate(null)));
			html.replace("%crit%", String.valueOf(((L2Character) target).getCriticalHit(null, null)));
			html.replace("%rspd%", String.valueOf(((L2Character) target).getRunSpeed()));
			html.replace("%aspd%", String.valueOf(((L2Character) target).getPAtkSpd()));
			html.replace("%cspd%", String.valueOf(((L2Character) target).getMAtkSpd()));
			html.replace("%atkType%", String.valueOf(((L2Character) target).getTemplate().getBaseAttackType()));
			html.replace("%atkRng%", String.valueOf(((L2Character) target).getTemplate().getBaseAttackRange()));
			html.replace("%str%", String.valueOf(((L2Character) target).getSTR()));
			html.replace("%dex%", String.valueOf(((L2Character) target).getDEX()));
			html.replace("%con%", String.valueOf(((L2Character) target).getCON()));
			html.replace("%int%", String.valueOf(((L2Character) target).getINT()));
			html.replace("%wit%", String.valueOf(((L2Character) target).getWIT()));
			html.replace("%men%", String.valueOf(((L2Character) target).getMEN()));
			html.replace("%loc%", String.valueOf(target.getX() + " " + target.getY() + " " + target.getZ()));
			html.replace("%heading%", String.valueOf(((L2Character) target).getHeading()));
			html.replace("%collision_radius%", String.valueOf(((L2Character) target).getTemplate().getfCollisionRadius()));
			html.replace("%collision_height%", String.valueOf(((L2Character) target).getTemplate().getfCollisionHeight()));
			html.replace("%dist%", String.valueOf((int) Math.sqrt(activeChar.getDistanceSq(target))));
			
			byte attackAttribute = ((L2Character) target).getAttackElement();
			html.replace("%ele_atk%", Elementals.getElementName(attackAttribute));
			html.replace("%ele_atk_value%", String.valueOf(((L2Character) target).getAttackElementValue(attackAttribute)));
			html.replace("%ele_dfire%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.FIRE)));
			html.replace("%ele_dwater%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.WATER)));
			html.replace("%ele_dwind%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.WIND)));
			html.replace("%ele_dearth%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.EARTH)));
			html.replace("%ele_dholy%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.HOLY)));
			html.replace("%ele_ddark%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.DARK)));
			
			if (((L2Npc) target).getSpawn() != null)
			{
				html.replace("%spawn%", ((L2Npc) target).getSpawn().getX() + " " + ((L2Npc) target).getSpawn().getY() + " " + ((L2Npc) target).getSpawn().getZ());
				html.replace("%loc2d%", String.valueOf((int) Math.sqrt(((L2Character) target).getPlanDistanceSq(((L2Npc) target).getSpawn().getX(), ((L2Npc) target).getSpawn().getY()))));
				html.replace("%loc3d%", String.valueOf((int) Math.sqrt(((L2Character) target).getDistanceSq(((L2Npc) target).getSpawn().getX(), ((L2Npc) target).getSpawn().getY(), ((L2Npc) target).getSpawn().getZ()))));
				if (((L2Npc) target).getSpawn().getRespawnMinDelay() == 0)
				{
					html.replace("%resp%", "None");
				}
				else if (((L2Npc) target).getSpawn().hasRespawnRandom())
				{
					html.replace("%resp%", String.valueOf(((L2Npc) target).getSpawn().getRespawnMinDelay() / 1000) + "-" + String.valueOf((((L2Npc) target).getSpawn().getRespawnMaxDelay() / 1000) + " sec"));
				}
				else
				{
					html.replace("%resp%", String.valueOf(((L2Npc) target).getSpawn().getRespawnMinDelay() / 1000) + " sec");
				}
			}
			else
			{
				html.replace("%spawn%", "<font color=FF0000>null</font>");
				html.replace("%loc2d%", "<font color=FF0000>--</font>");
				html.replace("%loc3d%", "<font color=FF0000>--</font>");
				html.replace("%resp%", "<font color=FF0000>--</font>");
			}
			
			if (((L2Npc) target).hasAI())
			{
				html.replace("%ai_intention%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Intention:</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getAI().getIntention().name()) + "</td></tr></table></td></tr>");
				html.replace("%ai%", "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>AI</font></td><td align=right width=170>" + ((L2Npc) target).getAI().getClass().getSimpleName() + "</td></tr></table></td></tr>");
				html.replace("%ai_type%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>AIType</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getAiType()) + "</td></tr></table></td></tr>");
				html.replace("%ai_clan%", "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>Clan & Range:</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getClan()) + " " + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getClanRange()) + "</td></tr></table></td></tr>");
				html.replace("%ai_enemy_clan%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Enemy & Range:</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getEnemyClan()) + " " + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getEnemyRange()) + "</td></tr></table></td></tr>");
			}
			else
			{
				html.replace("%ai_intention%", "");
				html.replace("%ai%", "");
				html.replace("%ai_type%", "");
				html.replace("%ai_clan%", "");
				html.replace("%ai_enemy_clan%", "");
			}
			
			final String routeName = WalkingManager.getInstance().getRouteName((L2Npc) target);
			if (!routeName.isEmpty())
			{
				html.replace("%route%", "<tr><td><table width=270 border=0><tr><td width=100><font color=LEVEL>Route:</font></td><td align=right width=170>" + routeName + "</td></tr></table></td></tr>");
			}
			else
			{
				html.replace("%route%", "");
			}
			activeChar.sendPacket(html);
		}
		else if (Config.ALT_GAME_VIEWNPC)
		{
			activeChar.setTarget(target);
			
			if (target.isAutoAttackable(activeChar))
			{
				StatusUpdate su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_HP, (int) ((L2Character) target).getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, ((L2Character) target).getMaxHp());
				activeChar.sendPacket(su);
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(activeChar.getLang(), "data/html/mobinfo.htm");
			
			html.replace("%objid%", String.valueOf(target.getObjectId()));
			html.replace("%class%", target.getClass().getSimpleName());
			html.replace("%id%", String.valueOf(((L2Npc) target).getTemplate()._npcId));
			html.replace("%lvl%", String.valueOf(((L2Npc) target).getTemplate()._level));
			html.replace("%name%", String.valueOf(((L2Npc) target).getTemplate()._name));
			html.replace("%tmplid%", String.valueOf(((L2Npc) target).getTemplate()._npcId));
			html.replace("%aggro%", String.valueOf((target instanceof L2Attackable) ? ((L2Attackable) target).getAggroRange() : 0));
			html.replace("%hp%", String.valueOf((int) ((L2Character) target).getCurrentHp()));
			html.replace("%hpmax%", String.valueOf(((L2Character) target).getMaxHp()));
			html.replace("%mp%", String.valueOf((int) ((L2Character) target).getCurrentMp()));
			html.replace("%mpmax%", String.valueOf(((L2Character) target).getMaxMp()));
			
			html.replace("%patk%", String.valueOf(((L2Character) target).getPAtk(null)));
			html.replace("%matk%", String.valueOf(((L2Character) target).getMAtk(null, null)));
			html.replace("%pdef%", String.valueOf(((L2Character) target).getPDef(null)));
			html.replace("%mdef%", String.valueOf(((L2Character) target).getMDef(null, null)));
			html.replace("%accu%", String.valueOf(((L2Character) target).getAccuracy()));
			html.replace("%evas%", String.valueOf(((L2Character) target).getEvasionRate(null)));
			html.replace("%crit%", String.valueOf(((L2Character) target).getCriticalHit(null, null)));
			html.replace("%rspd%", String.valueOf(((L2Character) target).getRunSpeed()));
			html.replace("%aspd%", String.valueOf(((L2Character) target).getPAtkSpd()));
			html.replace("%cspd%", String.valueOf(((L2Character) target).getMAtkSpd()));
			html.replace("%str%", String.valueOf(((L2Character) target).getSTR()));
			html.replace("%dex%", String.valueOf(((L2Character) target).getDEX()));
			html.replace("%con%", String.valueOf(((L2Character) target).getCON()));
			html.replace("%int%", String.valueOf(((L2Character) target).getINT()));
			html.replace("%wit%", String.valueOf(((L2Character) target).getWIT()));
			html.replace("%men%", String.valueOf(((L2Character) target).getMEN()));
			html.replace("%loc%", String.valueOf(target.getX() + " " + target.getY() + " " + target.getZ()));
			html.replace("%dist%", String.valueOf((int) Math.sqrt(activeChar.getDistanceSq(target))));
			
			byte attackAttribute = ((L2Character) target).getAttackElement();
			html.replace("%ele_atk%", Elementals.getElementName(attackAttribute));
			html.replace("%ele_atk_value%", String.valueOf(((L2Character) target).getAttackElementValue(attackAttribute)));
			html.replace("%ele_dfire%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.FIRE)));
			html.replace("%ele_dwater%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.WATER)));
			html.replace("%ele_dwind%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.WIND)));
			html.replace("%ele_dearth%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.EARTH)));
			html.replace("%ele_dholy%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.HOLY)));
			html.replace("%ele_ddark%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.DARK)));
			
			if (((L2Npc) target).getSpawn() != null)
			{
				html.replace("%spawn%", ((L2Npc) target).getSpawn().getX() + " " + ((L2Npc) target).getSpawn().getY() + " " + ((L2Npc) target).getSpawn().getZ());
				html.replace("%loc2d%", String.valueOf((int) Math.sqrt(((L2Character) target).getPlanDistanceSq(((L2Npc) target).getSpawn().getX(), ((L2Npc) target).getSpawn().getY()))));
				html.replace("%loc3d%", String.valueOf((int) Math.sqrt(((L2Character) target).getDistanceSq(((L2Npc) target).getSpawn().getX(), ((L2Npc) target).getSpawn().getY(), ((L2Npc) target).getSpawn().getZ()))));
				html.replace("%resp%", String.valueOf(((L2Npc) target).getSpawn().getRespawnDelay() / 1000));
			}
			else
			{
				html.replace("%spawn%", "<font color=FF0000>null</font>");
				html.replace("%loc2d%", "<font color=FF0000>--</font>");
				html.replace("%loc3d%", "<font color=FF0000>--</font>");
				html.replace("%resp%", "<font color=FF0000>--</font>");
			}
			
			if (((L2Npc) target).hasAI())
			{
				html.replace("%ai_intention%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Intention:</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getAI().getIntention().name()) + "</td></tr></table></td></tr>");
				html.replace("%ai%", "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>AI</font></td><td align=right width=170>" + ((L2Npc) target).getAI().getClass().getSimpleName() + "</td></tr></table></td></tr>");
				html.replace("%ai_type%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>AIType</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getAiType()) + "</td></tr></table></td></tr>");
				html.replace("%ai_clan%", "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>Clan & Range:</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getClan()) + " " + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getClanRange()) + "</td></tr></table></td></tr>");
				html.replace("%ai_enemy_clan%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Enemy & Range:</font></td><td align=right width=170>" + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getEnemyClan()) + " " + String.valueOf(((L2Npc) target).getTemplate().getAIDataStatic().getEnemyRange()) + "</td></tr></table></td></tr>");
			}
			else
			{
				html.replace("%ai_intention%", "");
				html.replace("%ai%", "");
				html.replace("%ai_type%", "");
				html.replace("%ai_clan%", "");
				html.replace("%ai_enemy_clan%", "");
			}
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2Npc;
	}
}