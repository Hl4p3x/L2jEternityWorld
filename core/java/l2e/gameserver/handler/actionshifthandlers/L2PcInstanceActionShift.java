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
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.handler.AdminCommandHandler;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2PcInstanceActionShift implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (activeChar.isGM())
		{
			if (activeChar.getTarget() != target)
			{
				activeChar.setTarget(target);
			}
			
			IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_character_info");
			if (ach != null)
			{
				ach.useAdminCommand("admin_character_info " + target.getName(), activeChar);
			}
		}
		else if (Config.ALT_GAME_VIEWPLAYER)
		{
			activeChar.setTarget(target);
			
			final NpcHtmlMessage selectInfo = new NpcHtmlMessage(0);
			selectInfo.setFile(activeChar.getLang(), "data/html/playerinfo.htm");
			selectInfo.replace("%name%", ((L2PcInstance) target).getName());
			selectInfo.replace("%level%", String.valueOf(((L2PcInstance) target).getLevel()));
			selectInfo.replace("%clan%", String.valueOf(((L2PcInstance) target).getClan() != null ? ((L2PcInstance) target).getClan().getName() : null));
			selectInfo.replace("%xp%", String.valueOf(((L2PcInstance) target).getExp()));
			selectInfo.replace("%sp%", String.valueOf(((L2PcInstance) target).getSp()));
			selectInfo.replace("%class%", ClassListParser.getInstance().getClass(((L2PcInstance) target).getClassId()).getClientCode());
			selectInfo.replace("%classid%", String.valueOf(((L2PcInstance) target).getClassId()));
			selectInfo.replace("%currenthp%", String.valueOf((int) ((L2PcInstance) target).getCurrentHp()));
			selectInfo.replace("%karma%", String.valueOf(((L2PcInstance) target).getKarma()));
			selectInfo.replace("%currentmp%", String.valueOf((int) ((L2PcInstance) target).getCurrentMp()));
			selectInfo.replace("%pvpflag%", String.valueOf(((L2PcInstance) target).getPvpFlag()));
			selectInfo.replace("%currentcp%", String.valueOf((int) ((L2PcInstance) target).getCurrentCp()));
			selectInfo.replace("%pvpkills%", String.valueOf(((L2PcInstance) target).getPvpKills()));
			selectInfo.replace("%pkkills%", String.valueOf(((L2PcInstance) target).getPkKills()));
			selectInfo.replace("%currentload%", String.valueOf(((L2PcInstance) target).getCurrentLoad()));
			selectInfo.replace("%patk%", String.valueOf(((L2PcInstance) target).getPAtk(null)));
			selectInfo.replace("%matk%", String.valueOf(((L2PcInstance) target).getMAtk(null, null)));
			selectInfo.replace("%pdef%", String.valueOf(((L2PcInstance) target).getPDef(null)));
			selectInfo.replace("%mdef%", String.valueOf(((L2PcInstance) target).getMDef(null, null)));
			selectInfo.replace("%accuracy%", String.valueOf(((L2PcInstance) target).getAccuracy()));
			selectInfo.replace("%evasion%", String.valueOf(((L2PcInstance) target).getEvasionRate(null)));
			selectInfo.replace("%critical%", String.valueOf(((L2PcInstance) target).getCriticalHit(null, null)));
			selectInfo.replace("%runspeed%", String.valueOf(((L2PcInstance) target).getRunSpeed()));
			selectInfo.replace("%patkspd%", String.valueOf(((L2PcInstance) target).getPAtkSpd()));
			selectInfo.replace("%matkspd%", String.valueOf(((L2PcInstance) target).getMAtkSpd()));
			selectInfo.replace("%noblesse%", ((L2PcInstance) target).getClassId().isMage() ? "Yes" : "No");
			activeChar.sendPacket(selectInfo);
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2PcInstance;
	}
}