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
package l2e.gameserver.model.actor.instance;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.events.Hitman;
import l2e.gameserver.model.entity.events.PlayerToAssasinate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2HitmanInstance extends L2Npc
{
	private static Integer maxPerPage = Config.HITMAN_MAX_PER_PAGE;
	private final DecimalFormat f = new DecimalFormat(",##0,000");
	
	public L2HitmanInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String currentcommand = st.nextToken();
		
		try
		{
			if (currentcommand.startsWith("showList"))
			{
				int p = Integer.parseInt(st.nextToken());
				parseWindow(player, showListWindow(player, p));
			}
			else if (currentcommand.startsWith("showInfo"))
			{
				int playerId = Integer.parseInt(st.nextToken());
				int p = Integer.parseInt(st.nextToken());
				parseWindow(player, showInfoWindow(player, playerId, p));
			}
			else if (currentcommand.startsWith("showAddList"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				String filename = "data/html/default/51-1.htm";
				html.setFile(player.getLang(), filename);
				parseWindow(player, html);
			}
			else if (currentcommand.startsWith("addList"))
			{
				String name = st.nextToken();
				Long bounty = Long.parseLong(st.nextToken());
				Integer itemId = Hitman.getInstance().getCurrencyId(st.nextToken());
				if (bounty <= 0)
				{
					bounty = 1L;
				}
				Hitman.getInstance().putHitOn(player, name, bounty, itemId);
			}
			else if (currentcommand.startsWith("removeList"))
			{
				String name = st.nextToken();
				Hitman.getInstance().cancelAssasination(name, player);
				showChatWindow(player, 0);
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
		catch (Exception e)
		{
			player.sendMessage((new CustomMessage("Hitman.MAKE_SURE", player.getLang())).toString());
		}
	}
	
	public void parseWindow(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npc_name%", getName());
		html.replace("%player_name%", player.getName());
		player.sendPacket(html);
	}
	
	public NpcHtmlMessage showAddList(L2PcInstance player, String list)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		TextBuilder content = new TextBuilder("");
		content.append("<html>");
		content.append("<body>");
		content.append("<center>");
		content.append("<img src=\"L2Font-e.mini_logo-e\" width=\"245\" height=\"80\">");
		content.append("<img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
		content.append("<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.ORDER_TARGET") + "<br1>");
		content.append("<table width=\"256\">");
		content.append("<tr>");
		content.append("<td width=\"256\" align=\"center\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.NAME") + "<br1>");
		content.append("<edit var=\"name\" width=\"150\" height=\"15\">");
		content.append("</td>");
		content.append("</tr>");
		content.append("<tr>");
		content.append("<td wi dth=\"256\" align=\"center\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.CURRENCY") + "<br1>");
		content.append("<combobox width=\"180\" var=\"currency\" list=\"Adena;Coin_of_Luck;Golden_Apiga\">");
		content.append("</td>");
		content.append("</tr>");
		content.append("<tr>");
		content.append("<td width=\"256\" align=\"center\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.AMOUNT") + "<br1>");
		content.append("<edit var=\"bounty\" width=\"150\" height=\"15\">");
		content.append("</td>");
		content.append("</tr>");
		content.append("</table>");
		content.append("<br>");
		content.append("<button value=" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.ADD") + " action=\"bypass -h npc_%objectId%_addList $name $bounty $currency\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"95\" height=\"21\">");
		content.append("<br>" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.IF_DEL_TARGET") + "<br1>");
		content.append("<table width=\"240\">");
		content.append("<tr>");
		content.append("<td width=\"60\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.NAME") + ":</td>");
		content.append("<td><edit var=\"remname\" width=\"110\" height=\"15\"></td>");
		content.append("<td><button value=" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.DELETE") + " action=\"bypass -h npc_%objectId%_removeList $remname\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"60\" height=\"21\"></td>");
		content.append("</tr>");
		content.append("</table>");
		content.append("<br>");
		content.append("<br>");
		content.append("<button value=" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.BACK") + " action=\"bypass -h npc_%objectId%_Chat 0\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"80\" height=\"21\"><br1>");
		content.append("<img src=\"L2UI_CH3.herotower_deco\" widt h=\"256\" height=\"32\">");
		content.append("<img src=\"l2ui.bbs_lineage2\" height=\"16\" width=\"80\">");
		content.append("</center>");
		content.append("</body>");
		content.append("</html>");
		
		html.setHtml(content.toString());
		return html;
	}
	
	private String generateButtonPage(int page, int select)
	{
		String text = "";
		
		if (page == 1)
		{
			return text;
		}
		
		text += "<table><tr>";
		for (int i = 1; i <= page; i++)
		{
			String v = (i == select ? String.valueOf(i) + "*" : String.valueOf(i));
			text += "<td><button value=\"P" + v + "\"" + "action=\"bypass -h npc_%objectId%_showList " + i + "\" back=\"L2UI_CT1.Button_DF_Down\"" + "fore=\"L2UI_CT1.Button_DF\" width=35 height=21></td>";
			text += ((i % 8) == 0 ? "</tr><tr>" : "");
		}
		text += "</tr></table>";
		return text;
	}
	
	public NpcHtmlMessage showListWindow(L2PcInstance player, int p)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		TextBuilder content = new TextBuilder("<html><body><center>");
		
		content.append("<img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
		content.append("<br>");
		
		content.append("<table>");
		content.append("<tr><td align=\"center\"><font color=AAAAAA>" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.AGENCY") + "</font></td></tr>");
		content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
		content.append("<tr><td align=\"center\">");
		FastList<PlayerToAssasinate> list = new FastList<>();
		list.addAll(Hitman.getInstance().getTargetsOnline().values());
		
		if (list.size() > 0)
		{
			int countPag = (int) Math.ceil((double) list.size() / (double) maxPerPage);
			int startRow = (maxPerPage * (p - 1));
			int stopRow = startRow + maxPerPage;
			int countReg = 0;
			String pages = generateButtonPage(countPag, p);
			
			content.append(pages);
			content.append("<table bgcolor=\"000000\">");
			content.append("<tr><td width=\"60\" align=\"center\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.TARGET") + "</td>");
			content.append("<td width=\"125\" align=\"center\"><font color=\"F2FEBF\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.REWARD") + "</font></td>");
			content.append("<td width=\"115\" align=\"center\"><font color=\"00CC00\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.CURRENCY") + "</font></td></tr>");
			
			for (PlayerToAssasinate pta : list)
			{
				if (pta == null)
				{
					break;
				}
				
				if (countReg >= stopRow)
				{
					break;
				}
				
				if ((countReg >= startRow) && (countReg < stopRow))
				{
					content.append("<tr><td align=\"center\">" + pta.getName() + "</td>");
					content.append("<td align=\"center\">" + (pta.getBounty() > 999 ? f.format(pta.getBounty()) : pta.getBounty()) + "</td>");
					content.append("<td align=\"center\">" + pta.getItemName() + "</td></tr>");
				}
				
				countReg++;
			}
			content.append("<tr><td height=\"3\"> </td><td height=\"3\"> </td><td height=\"3\"> </td></tr>");
			content.append("</table><br1>");
			
			content.append(pages);
		}
		else
		{
			content.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.NO_TARGET") + "");
		}
		
		content.append("</td></tr>");
		content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
		content.append("</table>");
		
		content.append("<button value=" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.BACK") + " action=\"bypass -h npc_%objectId%_Chat 0\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"55\" height=\"21\">");
		content.append("<br><font color=\"cc9900\"><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"></font><br1>");
		content.append("</center></body></html>");
		html.setHtml(content.toString());
		
		return html;
	}
	
	public NpcHtmlMessage showInfoWindow(L2PcInstance player, int objectId, int p)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		PlayerToAssasinate pta = Hitman.getInstance().getTargets().get(objectId);
		L2PcInstance target = L2World.getInstance().getPlayer(pta.getName());
		TextBuilder content = new TextBuilder("<html><body><center>");
		
		content.append("<img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
		content.append("<table>");
		content.append("<tr><td align=\"center\"><font color=\"AAAAAA\">" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.TARGET") + ": " + pta.getName() + "</font></td></tr>");
		content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
		content.append("<tr><td align=\"center\">");
		
		if (target != null)
		{
			content.append("<table bgcolor=\"000000\"><tr><td>");
			content.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.INFO") + ".<br>");
			content.append("<br><br>");
			content.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.TARGET") + ": <font color=\"D74B18\">" + pta.getName() + "</font><br1>");
			content.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.REWARD") + " <font color=\"D74B18\">" + (pta.getBounty() > 999 ? f.format(pta.getBounty()) : pta.getBounty()) + " " + pta.getItemName() + "</font><br1>");
			content.append("</td></tr></table>");
		}
		else
		{
			content.append("Player went offline.");
		}
		
		content.append("</td></tr>");
		content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
		content.append("</table>");
		content.append("<button value=" + LocalizationStorage.getInstance().getString(player.getLang(), "Hitman.BACK") + " action=\"bypass -h npc_%objectId%_showList " + p + "\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"100\" height=\"21\">");
		content.append("<br><font color=\"cc9900\"><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"></font><br1>");
		content.append("</center></body></html>");
		html.setHtml(content.toString());
		
		return html;
	}
}