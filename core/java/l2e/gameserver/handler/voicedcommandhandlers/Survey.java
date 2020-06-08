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

import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Survey implements IVoicedCommandHandler
{          
    	private static final String[] VOICED_COMMANDS =
	{
		"yes", "no", "survey"
	};

       	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
       	{
    	   	if (command.equalsIgnoreCase("survey"))
    	   	{
    		   	if (Config.ALLOW_SURVEY)
    		   	{
    				if (L2PcInstance._survey_running == true)
    				{
    					activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.RUNNING") + "");
    					ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.RUNNING") + "", 4000);
    					activeChar.sendPacket(message1);
    					return false;
    				}
    				L2PcInstance.CheckSurveyStatus(activeChar);
    				if (L2PcInstance.lastsurvey + L2PcInstance.surveyDelay < System.currentTimeMillis())
    				{
    					NpcHtmlMessage HtmlMessage = new NpcHtmlMessage(1);
    					HtmlMessage.setHtml("<html noscrollbar><head><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.PANEL") + "</title></head><body><center><br><table width=\"290\" cellpadding=\"5\"><tr><td valign=\"top\"><font color=\"3399ff\">"+activeChar.getName()+"</font>, " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.USE_PANEL") + " <font color=\"3399ff\"> " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.START") + " </font><br>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.AVOID") + " <font color=\"3399ff\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.12_HOURS") + "</font></td></tr></table></center><br><font color=\"333333\" align=\"cente\">_______________________________________</font><br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.STATUS") + ":<font color=\"00FF00\"> ON</font></center><br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.MESSAGE") + ":<br><multiedit var=\"mes\" width=200 height=25><br><td align=center><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.START_SURVEY") + "\" action=\"bypass -h surveymessage $mes\" width=120 height=19 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></center></body></html>");
    					activeChar.sendPacket(HtmlMessage);
    				} 
    				else
    				{
    					NpcHtmlMessage HtmlMessage = new NpcHtmlMessage(1);
    					HtmlMessage.setHtml("<html noscrollbar><head><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.PANEL") + "</title></head><body><center><br><table width=\"290\" cellpadding=\"5\"><tr><td valign=\"top\"><font color=\"3399ff\">"+activeChar.getName()+"</font>, " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.USE_PANEL") + " <font color=\"3399ff\"> " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.START") + " </font><br>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.AVOID") + " <font color=\"3399ff\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.12_HOURS") + "</font></td></tr></table></center><br><font color=\"333333\" align=\"cente\">_______________________________________</font><br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.STATUS") + ":<font color=\"FF0000\"> OFF</font></center><br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.MESSAGE") + ":<br><multiedit var=\"mes\" width=200 height=25><br><td align=center><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.START_SURVEY") + "\" action=\"bypass -h surveymessage $mes\" width=120 height=19 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></center></body></html>");
    					activeChar.sendPacket(HtmlMessage);
    				}
			}  	 		   
    			else
	   		{
    				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.DISABLED") + "");
    				ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.DISABLED") + "", 4000);
    				activeChar.sendPacket(message1); 
			}
		}

		if (command.equalsIgnoreCase("yes"))
    		{
    			if (Config.ALLOW_SURVEY)
    			{
    				if (!activeChar.isSurveyer())
    				{
    					activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.RUNNING_SURVEY") + "");
    					return false;
    				}
    				else if (!Config.ALLOW_SURVEY_GM_VOTING)
    				{
    					if(activeChar.isGM())
    					{
    						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.GM") + "");
    						return false;
    					}
    					return false;
    				}
    				ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.SUCCESSFULL") + "", 4000);
    				activeChar.sendPacket(message1);
    				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.SUCCESSFULL") + "");
    				activeChar.setSurveyer(false);
    				L2PcInstance.yes = L2PcInstance.yes + 1;	 			   
    			}
    			else
    			{
    				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.DISABLED") + "");
    				ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.DISABLED") + "", 4000);
    				activeChar.sendPacket(message1); 
    			}
    		}
    		   
    		if (command.equalsIgnoreCase("no"))
		{
    			if (Config.ALLOW_SURVEY)
    			{
    				if (!activeChar.isSurveyer())
    				{
    					activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.RUNNING_SURVEY") + "");
    					return false;
    				}
    				else if (!Config.ALLOW_SURVEY_GM_VOTING)
    				{
    					if(activeChar.isGM())
    					{
    						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.GM") + "");
    						return false;
    					}
    					return false;
    				}
    				ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.SUCCESSFULL") + "", 4000);
    				activeChar.sendPacket(message1);
	    			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.SUCCESSFULL") + "");
	    			activeChar.setSurveyer(false);
	    			L2PcInstance.no = L2PcInstance.no + 1;
    			}   	
    			else
    			{
	    			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.DISABLED") + "");
	    			ExShowScreenMessage message1 = new ExShowScreenMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Survey.DISABLED") + "", 4000);
	    			activeChar.sendPacket(message1); 
	    		}
		}
		return true;
	}
   	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}