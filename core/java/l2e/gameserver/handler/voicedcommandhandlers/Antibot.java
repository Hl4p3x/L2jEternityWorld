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
package l2e.gameserver.handler.voicedcommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Level;

import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Util;

public class Antibot implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"antibot"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
		if (command.equalsIgnoreCase("antibot") && (target != null))
		{
			StringTokenizer st = new StringTokenizer(target);
			try
			{
				String newpass = null, repeatnewpass = null;
				if (st.hasMoreTokens())
				{
					newpass = st.nextToken();
				}
				repeatnewpass = activeChar.getCode();
				
				if (!((newpass == null) || (repeatnewpass == null)))
				{
					if (newpass.equals(repeatnewpass))
					{
						npcHtmlMessage.setHtml("<html><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.SYSTEM") + "</title><body><center><font color=\"00FF00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.CORRECT") + "</font><br><br><center><br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.EXIT") + "\" action=\"bypass -h npc_%objectId%_Quest\" width=45 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
						activeChar.sendPacket(npcHtmlMessage);
						activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
						activeChar.setIsInvul(false);
						activeChar.setIsParalyzed(false);
						activeChar.setKills(0);
						activeChar.setCodeRight(true);
						return false;
					}
					
				}
				if (!newpass.equals(repeatnewpass))
				{
					npcHtmlMessage.setHtml("<html><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.SYSTEM") + "</title><body><center><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.INCORRECT") + "</font><br><br><font color=\"66FF00\"><center></font><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.WILL_PUNISH") + "</font><br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.EXIT") + "\" action=\"bypass -h npc_%objectId%_Quest\" width=45 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
					activeChar.sendPacket(npcHtmlMessage);
					if (activeChar.isFlyingMounted())
					{
						activeChar.untransform();
					}
					Util.handleIllegalPlayerAction(activeChar, "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.PUNISH_MSG") + "", Config.ANTIBOT_PUNISH);
					activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
					activeChar.setIsInvul(false);
					activeChar.setIsParalyzed(false);
					activeChar.sendPacket(npcHtmlMessage);
					activeChar.setCodeRight(true);
					return false;
				}
				npcHtmlMessage.setHtml("<html><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.SYSTEM") + "</title><body><center><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.INCORRECT") + "</font><br><br><font color=\"66FF00\"><center></font><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.WILL_PUNISH") + "</font><br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.EXIT") + "\" action=\"bypass -h npc_%objectId%_Quest\" width=45 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
				activeChar.sendPacket(npcHtmlMessage);
				if (activeChar.isFlyingMounted())
				{
					activeChar.untransform();
				}
				Util.handleIllegalPlayerAction(activeChar, "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.PUNISH_MSG") + "", Config.ANTIBOT_PUNISH);
				activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
				activeChar.setIsInvul(false);
				activeChar.setIsParalyzed(false);
				activeChar.sendPacket(npcHtmlMessage);
				activeChar.setCodeRight(true);
				return false;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("A problem occured while adding captcha!");
				_log.log(Level.WARNING, "", e);
			}
		}
		else
		{
			npcHtmlMessage.setHtml("<html><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.SYSTEM") + "</title><body><center><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.INCORRECT") + "</font><br><br><font color=\"66FF00\"><center></font><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.WILL_PUNISH") + "</font><br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.EXIT") + "\" action=\"bypass -h npc_%objectId%_Quest\" width=45 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			if (activeChar.isFlyingMounted())
			{
				activeChar.untransform();
			}
			Util.handleIllegalPlayerAction(activeChar, "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.PUNISH_MSG") + "", Config.ANTIBOT_PUNISH);
			activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
			activeChar.setIsInvul(false);
			activeChar.setIsParalyzed(false);
			activeChar.sendPacket(npcHtmlMessage);
			activeChar.setCodeRight(true);
			return false;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}