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

import java.util.List;

import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.instancemanager.games.FishingChampionship;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class L2FishermanInstance extends L2MerchantInstance
{
	public L2FishermanInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		setInstanceType(InstanceType.L2FishermanInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/fisherman/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.equalsIgnoreCase("FishSkillList"))
		{
			showFishSkillList(player);
		}
		else if (command.startsWith("FishingChampionship"))
		{
			showChampScreen(player);
		}
		else if (command.startsWith("FishingReward"))
		{
			FishingChampionship.getInstance().getReward(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public static void showFishSkillList(L2PcInstance player)
	{
		final List<L2SkillLearn> skills = SkillTreesParser.getInstance().getAvailableFishingSkills(player);
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.FISHING);
		
		int count = 0;
		
		for (L2SkillLearn s : skills)
		{
			final L2Skill sk = SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
			
			if (sk == null)
			{
				continue;
			}
			count++;
			asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 1);
		}
		
		if (count == 0)
		{
			final int minlLevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, SkillTreesParser.getInstance().getFishingSkillTree());
			
			if (minlLevel > 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minlLevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
	}
	
	public void showChampScreen(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		String str = "<html><head><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
		str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.GUILD_OF_FISHERS") + ":<br><br>";
		str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.HELLO") + "<br>";
		str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.YOUR_NAME_IN_LIST") + "<br>";
		str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.REMEMBER") + "<br>";
		str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.BE_NOT_UPSET") + "<br>";
		str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.MESSAGE") + " " + FishingChampionship.getInstance().getTimeRemaining() + " " + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.MIN") + "!<br>";
		str = str + "<center><a action=\"bypass -h npc_%objectId%_FishingReward\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.WIN_PRIZE") + "</a><br></center>";
		str = str + "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.FISHERMAN") + "</td><td width=80 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.LENGTH") + "</td></tr></table><table width=280>";
		for (int x = 1; x <= 5; x++)
		{
			str = str + "<tr><td width=70 align=center>" + x + " " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td>";
			str = str + "<td width=110 align=center>" + FishingChampionship.getInstance().getWinnerName(player, x) + "</td>";
			str = str + "<td width=80 align=center>" + FishingChampionship.getInstance().getFishLength(x) + "</td></tr>";
		}
		str = str + "<td width=80 align=center>0</td></tr></table><br>";
		str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZES_LIST") + "<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZE") + "</td><td width=80 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.AMOUNT") + "</td></tr></table><table width=280>";
		str = str + "<tr><td width=70 align=center>1 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>800000</td></tr><tr><td width=70 align=center>2 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>500000</td></tr><tr><td width=70 align=center>3 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>300000</td></tr>";
		str = str + "<tr><td width=70 align=center>4 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>200000</td></tr><tr><td width=70 align=center>5 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>100000</td></tr></table></body></html>";
		html.setHtml(str);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}