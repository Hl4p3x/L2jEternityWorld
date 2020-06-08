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
package l2e.gameserver.communitybbs.Manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.text.TextBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.CharSchemesHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.SchemesParser;
import l2e.gameserver.data.xml.SchemesParser.SkillInfo;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Util;

/**
 * Rework by LordWinter 04.07.2013 Fixed by L2J Eternity-World
 */
public class BuffBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(BuffBBSManager.class.getName());
	
	private final Map<Integer, ArrayList<L2Skill>> _buffs = new HashMap<>();
	
	private static BuffBBSManager _instance = new BuffBBSManager();
	
	public BuffBBSManager()
	{
		load();
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	public static BuffBBSManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new BuffBBSManager();
		}
		return _instance;
	}
	
	protected void load()
	{
		_buffs.clear();
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT, "data/communityBuffer.xml");
			if (!file.exists())
			{
				_log.warning(getClass().getSimpleName() + ": Couldn't find data/" + file.getName());
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			NamedNodeMap attrs;
			String group;
			int id, groupId, level;
			
			for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			{
				if ("list".equalsIgnoreCase(list.getNodeName()))
				{
					for (Node groups = list.getFirstChild(); groups != null; groups = groups.getNextSibling())
					{
						if ("groups".equalsIgnoreCase(groups.getNodeName()))
						{
							attrs = groups.getAttributes();
							group = attrs.getNamedItem("name").getNodeValue();
							groupId = Integer.parseInt(attrs.getNamedItem("groupId").getNodeValue());
							
							final ArrayList<L2Skill> groupSkills = new ArrayList<>();
							for (Node skills = groups.getFirstChild(); skills != null; skills = skills.getNextSibling())
							{
								if ("skill".equalsIgnoreCase(skills.getNodeName()))
								{
									attrs = skills.getAttributes();
									id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
									level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
									
									L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
									if (skill == null)
									{
										_log.warning(getClass().getSimpleName() + ": Can't find skill id: " + id + " level: " + level + " from " + group + " group in communityBuffer.xml");
										continue;
									}
									groupSkills.add(skill);
								}
							}
							_buffs.put(groupId, groupSkills);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": Error while loading buffs: " + e);
		}
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		final String[] commands = command.split("_");
		if (commands.length == 0)
		{
			return;
		}
		
		boolean petbuff = false;
		
		if (!(commands[2].startsWith("buff")))
		{
			return;
		}
		
		if ((commands[5] != null) && commands[5].startsWith(" " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Buffer.SELECT_PLAYER") + ""))
		{
			petbuff = false;
		}
		if ((commands[5] != null) && commands[5].startsWith(" " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Buffer.SELECT_PET") + ""))
		{
			petbuff = true;
		}
		
		if (commands[3].equalsIgnoreCase("skill"))
		{
			int skill_id = Integer.parseInt(commands[4]);
			
			int skilllevel = SkillHolder.getInstance().getMaxLevel(skill_id);
			L2Skill skill = SkillHolder.getInstance().getInfo(skill_id, skilllevel);
			
			doBuff(activeChar, skill, petbuff);
		}
		
		if (commands[3].equalsIgnoreCase("groupbuffs"))
		{
			final int group = Integer.parseInt(commands[4]);
			
			groupBuffs(activeChar, group, petbuff);
		}
		
		if (commands[3].equalsIgnoreCase("function"))
		{
			String function = commands[4];
			
			if (function.startsWith("cancel"))
			{
				cancel(activeChar, petbuff);
			}
			else if (function.startsWith("regmp"))
			{
				regMp(activeChar, petbuff);
			}
			else if (function.startsWith("reghp"))
			{
				regHp(activeChar, petbuff);
			}
			else if (function.startsWith("regcp"))
			{
				regCp(activeChar, petbuff);
			}
		}
		
		if (commands[3].equalsIgnoreCase("buffpage"))
		{
			String pagecommand = commands[4];
			if (pagecommand.startsWith("page"))
			{
				String currentCommand = commands[5];
				
				if (currentCommand.startsWith("1"))
				{
					sendHtm(activeChar, "data/html/CommunityBoard/3.htm");
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.BUFFPAGE1") + "");
				}
				else if (currentCommand.startsWith("2"))
				{
					sendHtm(activeChar, "data/html/CommunityBoard/37.htm");
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.BUFFPAGE2") + "");
				}
				else if (currentCommand.startsWith("3"))
				{
					sendHtm(activeChar, "data/html/CommunityBoard/38.htm");
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.BUFFPAGE3") + "");
				}
				else if (currentCommand.startsWith("4"))
				{
					sendHtm(activeChar, "data/html/CommunityBoard/39.htm");
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.BUFFPAGE4") + "");
				}
				else if (currentCommand.startsWith("5"))
				{
					sendHtm(activeChar, "data/html/CommunityBoard/40.htm");
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.BUFFPAGE5") + "");
				}
			}
		}
		
		if (commands[3].equalsIgnoreCase("schemes"))
		{
			String lang = activeChar.getLang();
			
			String feeName = "None";
			L2Item feeItem = ItemHolder.getInstance().getTemplate(Config.BUFF_ID_ITEM);
			if (feeItem != null)
			{
				feeName = feeItem.getName();
			}
			
			String listcommand = commands[4];
			if (listcommand.startsWith("list"))
			{
				String currentCommand = commands[5];
				
				if (currentCommand.startsWith("mainmenu"))
				{
					showBufferHomeWindow(activeChar);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.SCHEMEMAIN") + "");
				}
				else if (currentCommand.startsWith("generalpage"))
				{
					showBufferHomeWindow(activeChar);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.SCHEMEMAIN") + "");
				}
				else if (currentCommand.startsWith("support"))
				{
					String targettype = commands[6];
					showGiveBuffsWindow(activeChar, targettype);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.GIVEBUFFS") + "");
				}
				else if (currentCommand.startsWith("givebuffs"))
				{
					String targettype = commands[6];
					String scheme_key = commands[7];
					int cost = Integer.parseInt(commands[8]);
					int minLvl = Integer.parseInt(commands[9]);
					
					if (activeChar.getLevel() < minLvl)
					{
						CustomMessage msg = new CustomMessage("Buffer.LOW_LVL", lang);
						msg.add(minLvl);
						activeChar.sendMessage(msg.toString());
					}
					
					else if ((cost == 0) || takeFee(activeChar, Config.BUFF_ID_ITEM, cost))
					{
						L2Playable target = activeChar;
						if (targettype.equalsIgnoreCase("pet") && (activeChar).hasSummon())
						{
							target = activeChar.getSummon();
						}
						
						for (L2Skill sk : CharSchemesHolder.getInstance().getScheme(activeChar.getObjectId(), scheme_key))
						{
							sk.getEffects(activeChar, target);
						}
					}
					else
					{
						CustomMessage msg = new CustomMessage("Buffer.NOT_ENOUGH_ITEMS", lang);
						msg.add(feeName);
						activeChar.sendMessage(msg.toString());
					}
					showGiveBuffsWindow(activeChar, targettype);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.GIVEBUFFS") + "");
				}
				else if (currentCommand.startsWith("editscheme"))
				{
					String skill_group = "unselected";
					String scheme_key = "unselected";
					try
					{
						skill_group = commands[6];
						scheme_key = commands[7];
					}
					catch (Exception e)
					{
					}
					showEditSchemeWindow(activeChar, skill_group, scheme_key, lang);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.EDITEBUFFS") + "");
				}
				else if (currentCommand.startsWith("skill"))
				{
					String skill_group = commands[6];
					String scheme_key = commands[7];
					int skill_id = Integer.parseInt(commands[8]);
					int level = Integer.parseInt(commands[9]);
					
					if (currentCommand.startsWith("skillselect") && !scheme_key.equalsIgnoreCase("unselected"))
					{
						if (!SchemesParser.getInstance().buffsContainsSkill(skill_id, level))
						{
							_log.warning("Player " + activeChar.getName() + " try to cheat whith Community Buffer bypass!");
							Util.handleIllegalPlayerAction(activeChar, "You try to cheat whith Community Buffer and you will be Punished. Have a Nice Day!", Config.BUFFER_PUNISH);
							showEditSchemeWindow(activeChar, skill_group, scheme_key, lang);
							return;
						}
						if (CharSchemesHolder.getInstance().getScheme(activeChar.getObjectId(), scheme_key).size() < Config.BUFF_MAX_SKILLS)
						{
							CharSchemesHolder.getInstance().getScheme(activeChar.getObjectId(), scheme_key).add(SkillHolder.getInstance().getInfo(skill_id, level));
						}
						else
						{
							activeChar.sendMessage(LocalizationStorage.getInstance().getString(lang, "Buffer.SCHEME_FULL"));
						}
					}
					else if (currentCommand.startsWith("skillunselect"))
					{
						CharSchemesHolder.getInstance().getScheme(activeChar.getObjectId(), scheme_key).remove(SkillHolder.getInstance().getInfo(skill_id, level));
					}
					showEditSchemeWindow(activeChar, skill_group, scheme_key, lang);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.EDITEBUFFS") + "");
				}
				else if (currentCommand.startsWith("manageschemes"))
				{
					showManageSchemeWindow(activeChar);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MANAGESCHEME") + "");
				}
				else if (currentCommand.startsWith("create"))
				{
					String name = null;
					try
					{
						name = commands[6];
					}
					catch (Exception e)
					{
					}
					
					if (name == null)
					{
						activeChar.sendMessage(LocalizationStorage.getInstance().getString(lang, "Buffer.SCHEME_ERROR_NO_NAME"));
					}
					else if (name.length() > 14)
					{
						activeChar.sendMessage(LocalizationStorage.getInstance().getString(lang, "Buffer.SCHEME_ERROR_WRONG_NAME"));
					}
					else if ((CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()) != null) && (CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()).size() == Config.BUFF_MAX_SCHEMES))
					{
						activeChar.sendMessage(LocalizationStorage.getInstance().getString(lang, "Buffer.SCHEME_ERROR_FULL"));
					}
					else if ((CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()) != null) && CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()).containsKey(name))
					{
						activeChar.sendMessage(LocalizationStorage.getInstance().getString(lang, "Buffer.SCHEME_ERROR_ALREADY_EXIST"));
					}
					else
					{
						if (CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()) == null)
						{
							CharSchemesHolder.getInstance().getSchemesTable().put(activeChar.getObjectId(), new HashMap<String, ArrayList<L2Skill>>(Config.BUFF_MAX_SCHEMES + 1));
						}
						CharSchemesHolder.getInstance().setScheme(activeChar.getObjectId(), name.trim(), new ArrayList<L2Skill>(Config.BUFF_MAX_SKILLS + 1));
					}
					showManageSchemeWindow(activeChar);
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MANAGESCHEME") + "");
				}
				else if (currentCommand.startsWith("deletescheme"))
				{
					String name = commands[6];
					if ((CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()) != null) && CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()).containsKey(name))
					{
						CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()).remove(name);
						showManageSchemeWindow(activeChar);
					}
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MANAGESCHEME") + "");
				}
				else if (currentCommand.startsWith("clearscheme"))
				{
					String name = commands[6];
					if ((CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()) != null) && CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()).containsKey(name))
					{
						CharSchemesHolder.getInstance().getAllSchemes(activeChar.getObjectId()).get(name).clear();
						showManageSchemeWindow(activeChar);
					}
					activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MANAGESCHEME") + "");
				}
			}
		}
	}
	
	private void groupBuffs(L2PcInstance activeChar, int group_id, boolean petbuff)
	{
		final ArrayList<L2Skill> skills = _buffs.get(group_id);
		if (skills != null)
		{
			final int price = skills.size() * Config.BUFF_AMOUNT;
			
			if (activeChar.destroyItemByItemId(null, Config.BUFF_ID_ITEM, price, activeChar, true))
			{
				startBuffGroup(activeChar, skills, petbuff);
			}
		}
	}
	
	private static final void startBuffGroup(L2PcInstance activeChar, final List<L2Skill> skills, boolean petbuff)
	{
		if ((activeChar == null) || (skills == null) || skills.isEmpty())
		{
			return;
		}
		
		for (L2Skill sk : skills)
		{
			if (SchemesParser.getInstance().buffsIdContainsSkill(sk.getId()))
			{
				if (!petbuff)
				{
					sk.getEffects(activeChar, activeChar);
				}
				else
				{
					sk.getEffects(activeChar, activeChar.getSummon());
				}
			}
			else
			{
				_log.warning("Player " + activeChar.getName() + " try to cheat whith Community Buffer bypass!");
				Util.handleIllegalPlayerAction(activeChar, "You try to cheat whith Community Buffer and you will be Punished. Have a Nice Day!", Config.BUFFER_PUNISH);
			}
		}
	}
	
	private void doBuff(L2PcInstance activeChar, L2Skill skill, boolean petbuff)
	{
		if (skill == null)
		{
			return;
		}
		
		if (!SchemesParser.getInstance().buffsIdContainsSkill(skill.getId()))
		{
			_log.warning("Player " + activeChar.getName() + " try to cheat whith Community Buffer bypass!");
			Util.handleIllegalPlayerAction(activeChar, "You try to cheat whith Community Buffer and you will be Punished. Have a Nice Day!", Config.BUFFER_PUNISH);
			return;
		}
		
		if (activeChar.destroyItemByItemId(null, Config.BUFF_ID_ITEM, Config.BUFF_AMOUNT, activeChar, true))
		{
			if (!petbuff)
			{
				skill.getEffects(activeChar, activeChar);
			}
			else
			{
				skill.getEffects(activeChar, activeChar.getSummon());
			}
		}
	}
	
	private void cancel(L2PcInstance activeChar, boolean petbuff)
	{
		if (activeChar.destroyItemByItemId(null, Config.BUFF_ID_ITEM, Config.CANCEL_BUFF_AMOUNT, activeChar, true))
		{
			if (!petbuff)
			{
				activeChar.stopAllEffects();
			}
			else
			{
				activeChar.getSummon().stopAllEffects();
			}
		}
	}
	
	private void regMp(L2PcInstance activeChar, boolean petbuff)
	{
		if (activeChar.destroyItemByItemId(null, Config.BUFF_ID_ITEM, Config.MP_BUFF_AMOUNT, activeChar, true))
		{
			if (!petbuff)
			{
				activeChar.setCurrentMp(activeChar.getMaxMp());
			}
			else
			{
				activeChar.getSummon().setCurrentMp(activeChar.getSummon().getMaxMp());
			}
		}
	}
	
	private void regHp(L2PcInstance activeChar, boolean petbuff)
	{
		if (activeChar.destroyItemByItemId(null, Config.BUFF_ID_ITEM, Config.HP_BUFF_AMOUNT, activeChar, true))
		{
			if (!petbuff)
			{
				activeChar.setCurrentHp(activeChar.getMaxHp());
			}
			else
			{
				activeChar.getSummon().setCurrentHp(activeChar.getSummon().getMaxHp());
			}
		}
	}
	
	private void regCp(L2PcInstance activeChar, boolean petbuff)
	{
		if (activeChar.destroyItemByItemId(null, Config.BUFF_ID_ITEM, Config.CP_BUFF_AMOUNT, activeChar, true))
		{
			if (!petbuff)
			{
				activeChar.setCurrentCp(activeChar.getMaxCp());
			}
			else
			{
				activeChar.getSummon().setCurrentCp(activeChar.getSummon().getMaxCp());
			}
		}
	}
	
	private void showBufferHomeWindow(L2PcInstance player)
	{
		String lang = player.getLang();
		TextBuilder html = new TextBuilder();
		
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(lang, "Buffer.BUFF_MODE_SCHEME") + ":</font>");
		html.append("<br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "Buffer.BUFF_SELF") + "\" action=\"bypass -h _bbs_buff_schemes_list_support_player\" width=240 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "Buffer.BUFF_PET") + "\" action=\"bypass -h _bbs_buff_schemes_list_support_pet\" width=240 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "Buffer.SCHEME_EDIT") + "\" action=\"bypass -h _bbs_buff_schemes_list_manageschemes\" width=240 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(player.getLang(), "data/html/CommunityBoard/1.htm");
		adminReply.replace("%scheme%", html.toString());
		separateAndSend(adminReply.getHtm(), player);
	}
	
	private void showGiveBuffsWindow(L2PcInstance player, String targettype)
	{
		String lang = player.getLang();
		TextBuilder html = new TextBuilder();
		html.append("" + LocalizationStorage.getInstance().getString(lang, "Buffer.INFO_GIVE_BUFFS") + "");
		html.append("<br>");
		
		HashMap<String, ArrayList<L2Skill>> skillMap = CharSchemesHolder.getInstance().getAllSchemes(player.getObjectId());
		
		if ((skillMap == null) || skillMap.isEmpty())
		{
			html.append("" + LocalizationStorage.getInstance().getString(lang, "Buffer.INFO_NO_SCHEMES") + "");
		}
		else
		{
			int cost;
			int minLvl;
			html.append("<table>");
			for (String key : skillMap.keySet())
			{
				cost = getFee(skillMap.get(key));
				minLvl = getMinLvl(skillMap.get(key));
				html.append("<tr><td width=\"90\" align=\"left\"><a action=\"bypass -h _bbs_buff_schemes_list_givebuffs_" + targettype + "_" + key + "_" + cost + "_" + minLvl + "\">" + key + "</a></td><td width=\"90\" align=\"left\">" + LocalizationStorage.getInstance().getString(lang, "Buffer.STRING_FEE") + ": " + cost + "</td></tr>");
			}
			html.append("</table>");
			html.append("<br><br>");
			html.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "Buffer.STRING_BACK") + " action=\"bypass -h _bbs_buff_schemes_list_generalpage\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(player.getLang(), "data/html/CommunityBoard/1.htm");
		adminReply.replace("%scheme%", html.toString());
		separateAndSend(adminReply.getHtm(), player);
	}
	
	private void showManageSchemeWindow(L2PcInstance player)
	{
		String lang = player.getLang();
		TextBuilder html = new TextBuilder();
		
		if ((CharSchemesHolder.getInstance().getAllSchemes(player.getObjectId()) == null) || CharSchemesHolder.getInstance().getAllSchemes(player.getObjectId()).isEmpty())
		{
			html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(lang, "Buffer.INFO_NO_SCHEMES") + "</font><br>");
			html.append("<br>");
		}
		else
		{
			html.append("" + LocalizationStorage.getInstance().getString(lang, "Buffer.INFO_CREATE_SCHEME") + "");
			html.append("<br>");
			html.append("<table width=\"400\">");
			
			HashMap<String, ArrayList<L2Skill>> skillMap = CharSchemesHolder.getInstance().getAllSchemes(player.getObjectId());
			for (String key : skillMap.keySet())
			{
				html.append("<tr><td width=\"140\">" + key + " (" + CharSchemesHolder.getInstance().getScheme(player.getObjectId(), key).size() + " " + LocalizationStorage.getInstance().getString(lang, "Buffer.BUFFS") + ")</td>");
				html.append("<td width=\"60\"><button value=" + LocalizationStorage.getInstance().getString(lang, "Buffer.EDIT") + " action=\"bypass -h _bbs_buff_schemes_list_editscheme_unselected_" + key + "\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				html.append("<td width=\"60\"><button value=" + LocalizationStorage.getInstance().getString(lang, "Buffer.CLEAR") + " action=\"bypass -h _bbs_buff_schemes_list_clearscheme_" + key + "\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				html.append("<td width=\"60\"><button value=" + LocalizationStorage.getInstance().getString(lang, "Buffer.DROP") + " action=\"bypass -h _bbs_buff_schemes_list_deletescheme_" + key + "\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
		}
		html.append("<br><table width=240>");
		html.append("<tr><td><edit var=\"name\" width=120 height=15></td><td><button value=" + LocalizationStorage.getInstance().getString(lang, "Buffer.CREATE") + " action=\"bypass -h _bbs_buff_schemes_list_create_ $name\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		html.append("</table>");
		html.append("<br><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(lang, "Buffer.MAX_SCHEMES") + ": " + Config.BUFF_MAX_SCHEMES + "</font>");
		html.append("<br><br>");
		html.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "Buffer.STRING_BACK") + " action=\"bypass -h _bbs_buff_schemes_list_generalpage\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(player.getLang(), "data/html/CommunityBoard/1.htm");
		adminReply.replace("%scheme%", html.toString());
		separateAndSend(adminReply.getHtm(), player);
	}
	
	private void showEditSchemeWindow(L2PcInstance player, String skill_group, String scheme_key, String lang)
	{
		NpcHtmlMessage _html = new NpcHtmlMessage(5);
		_html.setFile(player.getLang(), "data/html/CommunityBoard/2.htm");
		_html.replace("%typesframe%", getTypesFrame(scheme_key));
		_html.replace("%schemelistframe%", getPlayerSchemeListFrame(player, skill_group, scheme_key, lang));
		_html.replace("%skilllistframe%", getGroupSkillListFrame(player, skill_group, scheme_key, lang));
		_html.replace("%myschemeframe%", getPlayerSkillListFrame(player, skill_group, scheme_key, lang));
		separateAndSend(_html.getHtm(), player);
	}
	
	private String getPlayerSchemeListFrame(L2PcInstance player, String skill_group, String scheme_key, String lang)
	{
		HashMap<String, ArrayList<L2Skill>> skillMap = CharSchemesHolder.getInstance().getAllSchemes(player.getObjectId());
		if ((skillMap == null) || skillMap.isEmpty())
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.CREATE_SCHEME");
		}
		
		if (skill_group == null)
		{
			skill_group = "def";
		}
		
		if (scheme_key == null)
		{
			scheme_key = "def";
		}
		
		String text = "<table>";
		int count = 0;
		
		for (String key : skillMap.keySet())
		{
			if (count == 0)
			{
				text += "<tr>";
			}
			text += "<td width=\"90\"><center><a action=\"bypass -h _bbs_buff_schemes_list_editschemes_" + skill_group + "_" + key + "\">" + key + "</a></center></td>";
			if (count == 5)
			{
				text += "</tr>";
				count = 0;
			}
			count++;
		}
		
		if (!text.endsWith("</tr>"))
		{
			text += "</tr>";
		}
		text += "</table>";
		
		return text;
	}
	
	private String getGroupSkillListFrame(L2PcInstance player, String skill_group, String scheme_key, String lang)
	{
		if ((skill_group == null) || skill_group.equals("unselected"))
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.SELECT_SKILL_GROUP");
		}
		else if ((scheme_key == null) || scheme_key.equals("unselected"))
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.SELECT_SCHEME");
		}
		
		String text = "<table>";
		String icon;
		int count = 0;
		HashMap<String, ArrayList<L2Skill>> skillMap = CharSchemesHolder.getInstance().getAllSchemes(player.getObjectId());
		for (SkillInfo info : SchemesParser.getInstance().getBuffInfoByGroup(skill_group))
		{
			String skiil_id = Integer.toString(info._id);
			
			if ((skillMap != null) && !skillMap.isEmpty() && CharSchemesHolder.getInstance().getSchemeContainsSkill(player.getObjectId(), scheme_key, info._id))
			{
				continue;
			}
			
			if (count == 0)
			{
				text += "<tr>";
			}
			
			if (skiil_id.length() == 3)
			{
				icon = 0 + skiil_id;
			}
			else
			{
				if ((info._id == 4700) || (info._id == 4699))
				{
					icon = "1331";
				}
				else if ((info._id == 4702) || (info._id == 4703))
				{
					icon = "1332";
				}
				else
				{
					icon = skiil_id;
				}
			}
			
			text += "<td><center><img src=icon.skill" + icon + " width=32 height=32><br1><button action=\"bypass -h _bbs_buff_schemes_list_skillselect_" + skill_group + "_" + scheme_key + "_" + info._id + "_" + info._lvl + "\" value=\"" + info._name + "\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td>";
			
			if (count == 5)
			{
				text += "</tr>";
				count = -1;
			}
			count++;
		}
		if (!text.endsWith("</tr>"))
		{
			text += "</tr>";
		}
		text += "</table>";
		
		return text;
	}
	
	private String getPlayerSkillListFrame(L2PcInstance player, String skill_group, String scheme_key, String lang)
	{
		if ((skill_group == null) || skill_group.equals("unselected"))
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.SELECT_SKILL_GROUP");
		}
		else if ((scheme_key == null) || scheme_key.equals("unselected"))
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.SELECT_SCHEME");
		}
		
		HashMap<String, ArrayList<L2Skill>> skillMap = CharSchemesHolder.getInstance().getAllSchemes(player.getObjectId());
		
		if (skillMap == null)
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.SELECT_SCHEME");
		}
		if (skillMap.isEmpty())
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.EMPTY_SCHEME");
		}
		
		String text = "<center>" + LocalizationStorage.getInstance().getString(lang, "Buffer.STRING_SCHEME") + ": " + scheme_key + "</center><br>";
		text += "<table>";
		String icon;
		int count = 0;
		
		for (L2Skill sk : CharSchemesHolder.getInstance().getScheme(player.getObjectId(), scheme_key))
		{
			String skiil_id = Integer.toString(sk.getId());
			
			if (count == 0)
			{
				text += "<tr>";
			}
			
			if (skiil_id.length() == 3)
			{
				icon = 0 + skiil_id;
			}
			else
			{
				if ((sk.getId() == 4700) || (sk.getId() == 4699))
				{
					icon = "1331";
				}
				else if ((sk.getId() == 4702) || (sk.getId() == 4703))
				{
					icon = "1332";
				}
				else
				{
					icon = skiil_id;
				}
			}
			
			text += "<td><center><img src=icon.skill" + icon + " width=32 height=32><br1><button action=\"bypass -h _bbs_buff_schemes_list_skillunselect_" + skill_group + "_" + scheme_key + "_" + sk.getId() + "_" + sk.getLevel() + "\" value=\"" + sk.getName() + "\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td>";
			if (count == 5)
			{
				text += "</tr>";
				count = -1;
			}
			count++;
		}
		if (!text.endsWith("<tr>"))
		{
			text += "<tr>";
		}
		text += "</table>";
		
		return text;
	}
	
	private String getTypesFrame(String scheme_key)
	{
		String text = "<table>";
		int count = 0;
		
		if (scheme_key == null)
		{
			scheme_key = "unselected";
		}
		
		for (String skillGroup : SchemesParser.getInstance().getBuffGroups())
		{
			if (count == 0)
			{
				text += "<tr>";
			}
			
			text += "<td><center><button action=\"bypass -h _bbs_buff_schemes_list_editscheme_" + skillGroup + "_" + scheme_key + "\" value=\"" + skillGroup + "\" width=90 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td>";
			if (count == 7)
			{
				text += "</tr>";
				count = -1;
			}
			count++;
		}
		if (!text.endsWith("</tr>"))
		{
			text += "</tr>";
		}
		text += "</table>";
		
		return text;
	}
	
	private int getFee(ArrayList<L2Skill> list)
	{
		int fee = 0;
		if (Config.BUFF_STATIC_BUFF_COST >= 0)
		{
			return (list.size() * Config.BUFF_STATIC_BUFF_COST);
		}
		
		for (L2Skill sk : list)
		{
			fee += SchemesParser.getInstance().getSkillFee(sk.getId());
		}
		return fee;
	}
	
	private int getMinLvl(ArrayList<L2Skill> list)
	{
		int minLvl = 0;
		for (L2Skill sk : list)
		{
			int newLvl = SchemesParser.getInstance().getSkillMinLvl(sk.getId());
			if (newLvl > minLvl)
			{
				minLvl = newLvl;
			}
		}
		return minLvl;
	}
	
	protected String getCustomTargetFrame(String targettype, String lang)
	{
		String forMe = LocalizationStorage.getInstance().getString(lang, "Buffer.STRING_BUFF_FOR_ME");
		String forPet = LocalizationStorage.getInstance().getString(lang, "Buffer.STRING_BUFF_FOR_PET");
		String text = "<table><tr>";
		if (targettype.equalsIgnoreCase("char"))
		{
			text += "<td width=\"90\">[" + forMe + "]</td>";
		}
		else
		{
			text += "<td width=\"90\"><a action=\"bypass -h _bbs_buff_schemes_list_custombuffs_char\">" + forMe + "</a></td>";
		}
		if (targettype.equalsIgnoreCase("pet"))
		{
			text += "<td width=\"90\">[" + forPet + "]</td>";
		}
		else
		{
			text += "<td width=\"90\"><a action=\"bypass -h _bbs_buff_schemes_list_custombuffs_pet\">" + forPet + "</a></td>";
		}
		text += "</tr></table>";
		
		return text;
	}
	
	protected String getCustomSkillListFrame(String targettype, String skill_group, String lang)
	{
		if ((skill_group == null) || skill_group.equals("unselected"))
		{
			return LocalizationStorage.getInstance().getString(lang, "Buffer.SELECT_SKILL_GROUP");
		}
		
		String text = "<table>";
		int count = 0;
		for (SkillInfo sk : SchemesParser.getInstance().getBuffInfoByGroup(skill_group))
		{
			if (count == 0)
			{
				text += "<tr>";
			}
			text += "<td><button action=\"bypass -h _bbs_buff_schemes_list_givecustombuffs_" + targettype + "_" + sk._id + "_" + sk._lvl + "_" + sk._cost + "_" + sk._minLvl + "_" + skill_group + "\" value=\"" + sk._name + "\" width=\"130\" height=\"20\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
			if (count == 1)
			{
				text += "</tr>";
				count = -1;
			}
			count++;
		}
		if (!text.endsWith("</tr>"))
		{
			text += "</tr>";
		}
		text += "</table>";
		
		return text;
	}
	
	private boolean takeFee(L2PcInstance player, int feeId, int feeAmount)
	{
		if (feeId != 0)
		{
			L2ItemInstance itemInstance = player.getInventory().getItemByItemId(feeId);
			if ((itemInstance == null) || (!itemInstance.isStackable() && (player.getInventory().getInventoryItemCount(feeId, -1) < feeAmount)))
			{
				player.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
				return false;
			}
			if (itemInstance.isStackable())
			{
				if (!player.destroyItemByItemId("Npc Buffer", feeId, feeAmount, player.getTarget(), true))
				{
					player.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return false;
				}
			}
			else
			{
				for (int i = 0; i < feeAmount; ++i)
				{
					player.destroyItemByItemId("Npc Buffer", feeId, 1, player.getTarget(), true);
				}
			}
		}
		return true;
	}
	
	private boolean sendHtm(L2PcInstance player, String path)
	{
		String oriPath = path;
		if ((player.getLang() != null) && !player.getLang().equalsIgnoreCase("en"))
		{
			if (path.contains("html/"))
			{
				path = path.replace("html/", "html-" + player.getLang() + "/");
			}
		}
		String content = HtmCache.getInstance().getHtm(path);
		if ((content == null) && !oriPath.equals(path))
		{
			content = HtmCache.getInstance().getHtm(oriPath);
		}
		if (content == null)
		{
			return false;
		}
		
		separateAndSend(content, player);
		return true;
	}
	
	@Override
	public void parsewrite(String s, String s1, String s2, String s3, String s4, L2PcInstance l2pcinstance)
	{
	}
}