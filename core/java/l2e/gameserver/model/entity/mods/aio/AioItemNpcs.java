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
package l2e.gameserver.model.entity.mods.aio;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.AIOItemParser;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.data.xml.HennaParser;
import l2e.gameserver.data.xml.MultiSellParser;
import l2e.gameserver.data.xml.TransformParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.leaderboards.ArenaLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.CraftLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.FishermanLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.TvTLeaderboard;
import l2e.gameserver.model.L2AioTeleport;
import l2e.gameserver.model.L2Augmentation;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.mods.AchievementsManager;
import l2e.gameserver.model.entity.mods.aio.dymanicHtmls.GenerateHtmls;
import l2e.gameserver.model.entity.mods.aio.main.Conditions;
import l2e.gameserver.model.entity.mods.aio.runnable.AioItemDelay;
import l2e.gameserver.model.entity.mods.aio.runnable.TransformFinalizer;
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.items.L2Henna;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import l2e.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2e.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2e.gameserver.network.serverpackets.HennaEquipList;
import l2e.gameserver.network.serverpackets.HennaRemoveList;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PartySmallWindowAll;
import l2e.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.SiegeInfo;
import l2e.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2e.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.network.serverpackets.WareHouseDepositList;
import l2e.gameserver.util.Broadcast;
import l2e.gameserver.util.Util;
import l2e.util.StringUtil;

public class AioItemNpcs
{
	private static int itemIdToGet;
	private static int price;
	private static final Logger _log = Logger.getLogger(AioItemNpcs.class.getName());
	private static final String[] COMMANDS =
	{
		"withdrawp",
		"withdrawsortedp",
		"withdrawc",
		"withdrawsortedc"
	};
	
	private AioItemNpcs()
	{
	}
	
	protected static String getSubClassMenu(Race pRace)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE || (pRace != Race.Kamael))
		{
			return "data/html/AioItemNpcs/subclass/SubClass.htm";
		}
		
		return "data/html/AioItemNpcs/subclass/SubClass_NoOther.htm";
	}
	
	protected static String getSubClassFail()
	{
		return "data/html/AioItemNpcs/subclass/SubClass_Fail.htm";
	}
	
	public static void onBypassFeedback(L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		if (player == null)
		{
			return;
		}
		
		if (!Conditions.checkPlayerConditions(player))
		{
			return;
		}
		
		if (command.startsWith("Chat"))
		{
			if (subCommand[1].isEmpty() || (subCommand[1] == null))
			{
				return;
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(5);
			msg.setFile(player.getLang(), "data/html/AioItemNpcs/" + subCommand[1]);
			player.sendPacket(msg);
		}
		else if (command.toLowerCase().startsWith(COMMANDS[0]))
		{
			if (Config.ENABLE_WAREHOUSESORTING_PRIVATE)
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setFile(player.getLang(), "data/html/AioItemNpcs/warehouse/WhSortedP.htm");
				player.sendPacket(msg);
			}
			else
			{
				GenerateHtmls.showPWithdrawWindow(player, null, (byte) 0);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith(COMMANDS[1]))
		{
			final String param[] = command.split(" ");
			
			if (param.length > 2)
			{
				GenerateHtmls.showPWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				GenerateHtmls.showPWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				GenerateHtmls.showPWithdrawWindow(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith(COMMANDS[2]))
		{
			if (Config.ENABLE_WAREHOUSESORTING_CLAN)
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setFile(player.getLang(), "data/html/AioItemNpcs/warehouse/WhSortedC.htm");
				player.sendPacket(msg);
			}
			else
			{
				GenerateHtmls.showCWithdrawWindow(player, null, (byte) 0);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith(COMMANDS[3]))
		{
			final String param[] = command.split(" ");
			
			if (param.length > 2)
			{
				GenerateHtmls.showCWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				GenerateHtmls.showCWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				GenerateHtmls.showCWithdrawWindow(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
			
			return;
		}
		else if (command.startsWith("ndeposit"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getWarehouse());
			if (player.getWarehouse().getSize() == player.getWareHouseLimit())
			{
				player.sendPacket(SystemMessageId.WAREHOUSE_FULL);
				return;
			}
			player.setIsUsingAioWh(true);
			player.setInventoryBlockingStatus(true);
			player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
		}
		else if (command.startsWith("clandeposit"))
		{
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				return;
			}
			
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getClan().getWarehouse());
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
				return;
			}
			
			player.setIsUsingAioWh(true);
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.setInventoryBlockingStatus(true);
			player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
		}
		
		else if (command.startsWith("showMyAchievements"))
		{
			player.getAchievemntData();
			GenerateHtmls.showMyAchievements(player);
		}
		else if (command.startsWith("showAchievementInfo"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int id = Integer.parseInt(st.nextToken());
				
				GenerateHtmls.showAchievementInfo(id, player);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "AioItemNpcs: Error in Achievement info.", e);
			}
		}
		else if (command.startsWith("achievementGetReward"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int id = Integer.parseInt(st.nextToken());
				
				// if (!AchievementsManager.checkConditions(id, player))
				// {
				// return;
				// }
				
				AchievementsManager.getInstance().rewardForAchievement(id, player);
				player.saveAchievementData(id, 0);
				GenerateHtmls.showMyAchievements(player);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "AioItemNpcs: Error at delivery Achievement reward.", e);
			}
		}
		else if (command.startsWith("showAchievementStats"))
		{
			GenerateHtmls.showAchievementStats(player);
		}
		else if (command.startsWith("showAchievementHelp"))
		{
			GenerateHtmls.showAchievementHelp(player);
		}
		else if (command.startsWith("showAchievementMain"))
		{
			GenerateHtmls.showAchievementMain(player, 0);
		}
		else if (command.startsWith("siege_"))
		{
			int castleId = 0;
			
			if (command.startsWith("siege_gludio"))
			{
				castleId = 1;
			}
			else if (command.startsWith("siege_dion"))
			{
				castleId = 2;
			}
			else if (command.startsWith("siege_giran"))
			{
				castleId = 3;
			}
			else if (command.startsWith("siege_oren"))
			{
				castleId = 4;
			}
			else if (command.startsWith("siege_aden"))
			{
				castleId = 5;
			}
			else if (command.startsWith("siege_innadril"))
			{
				castleId = 6;
			}
			else if (command.startsWith("siege_goddard"))
			{
				castleId = 7;
			}
			else if (command.startsWith("siege_rune"))
			{
				castleId = 8;
			}
			else if (command.startsWith("siege_schuttgart"))
			{
				castleId = 9;
			}
			
			Castle castle = CastleManager.getInstance().getCastleById(castleId);
			if ((castle != null) && (castleId != 0))
			{
				player.sendPacket(new SiegeInfo(castle));
			}
		}
		else if (command.startsWith("Subclass"))
		{
			if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneId.PEACE))
			{
				player.sendMessage("Cannot use while have PvP flag.");
				return;
			}
			
			if (player.isCastingNow() || player.isAllSkillsDisabled())
			{
				player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
				return;
			}
			else if (player.isInCombat())
			{
				player.sendMessage("Sub classes may not be created or changed while being in combat.");
				return;
			}
			else if (OlympiadManager.getInstance().isRegistered(player))
			{
				player.sendMessage("You can not change subclass when registered for Olympiad.");
				return;
			}
			else if (player.isInParty())
			{
				player.sendMessage("Sub classes may not be created or changed while being in party.");
				return;
			}
			else if (player.isCursedWeaponEquipped())
			{
				player.sendMessage("You can`t change Subclass while Cursed weapon equiped!");
				return;
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(5);
			
			if (player.getTransformation() != null)
			{
				html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_NoTransformed.htm");
				player.sendPacket(html);
				return;
			}
			
			if (player.getSummon() != null)
			{
				html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_NoSummon.htm");
				player.sendPacket(html);
				return;
			}
			
			if (!player.isInventoryUnder90(true))
			{
				player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_INVENTORY_FULL);
				return;
			}
			
			if (player.getWeightPenalty() >= 2)
			{
				player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_OVERWEIGHT);
				return;
			}
			
			int cmdChoice = 0;
			int paramOne = 0;
			int paramTwo = 0;
			try
			{
				cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				
				int endIndex = command.indexOf(' ', 11);
				if (endIndex == -1)
				{
					endIndex = command.length();
				}
				
				if (command.length() > 11)
				{
					paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
					if (command.length() > endIndex)
					{
						paramTwo = Integer.parseInt(command.substring(endIndex).trim());
					}
				}
			}
			catch (Exception NumberFormatException)
			{
				_log.warning(AioItemNpcs.class.getName() + ": Wrong numeric values for command " + command);
			}
			
			Set<PlayerClass> subsAvailable = null;
			switch (cmdChoice)
			{
				case 0:
					html.setFile(player.getLang(), getSubClassMenu(player.getRace()));
					break;
				case 1:
					if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
					{
						html.setFile(player.getLang(), getSubClassFail());
						break;
					}
					
					subsAvailable = Conditions.getAvailableSubClasses(player);
					if ((subsAvailable != null) && !subsAvailable.isEmpty())
					{
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_Add.htm");
						final StringBuilder content1 = StringUtil.startAppend(200);
						for (PlayerClass subClass : subsAvailable)
						{
							StringUtil.append(content1, "<a action=\"bypass -h Aioitem_Subclass 4 ", String.valueOf(subClass.ordinal()), "\" msg=\"1268;", ClassListParser.getInstance().getClass(subClass.ordinal()).getClassName(), "\">", ClassListParser.getInstance().getClass(subClass.ordinal()).getClientCode(), "</a><br>");
						}
						html.replace("%list%", content1.toString());
					}
					else
					{
						if ((player.getRace() == Race.Elf) || (player.getRace() == Race.DarkElf))
						{
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_Fail_Elves.htm");
							player.sendPacket(html);
						}
						else if (player.getRace() == Race.Kamael)
						{
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_Fail_Kamael.htm");
							player.sendPacket(html);
						}
						else
						{
							player.sendMessage("There are no sub classes available at this time.");
						}
						
						return;
					}
					break;
				case 2:
					if (player.getSubClasses().isEmpty())
					{
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ChangeNo.htm");
					}
					else
					{
						final StringBuilder content2 = StringUtil.startAppend(200);
						if (Conditions.checkVillageMaster(player.getBaseClass()))
						{
							StringUtil.append(content2, "<a action=\"bypass -h Aioitem_Subclass 5 0\">", ClassListParser.getInstance().getClass(player.getBaseClass()).getClientCode(), "</a><br>");
						}
						
						for (Iterator<SubClass> subList = Conditions.iterSubClasses(player); subList.hasNext();)
						{
							SubClass subClass = subList.next();
							if (Conditions.checkVillageMaster(subClass.getClassDefinition()))
							{
								StringUtil.append(content2, "<a action=\"bypass -h Aioitem_Subclass 5 ", String.valueOf(subClass.getClassIndex()), "\">", ClassListParser.getInstance().getClass(subClass.getClassId()).getClientCode(), "</a><br>");
							}
						}
						
						if (content2.length() > 0)
						{
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_Change.htm");
							html.replace("%list%", content2.toString());
						}
						else
						{
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ChangeNotFound.htm");
						}
					}
					break;
				case 3:
					if ((player.getSubClasses() == null) || player.getSubClasses().isEmpty())
					{
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ModifyEmpty.htm");
						break;
					}
					
					if ((player.getTotalSubClasses() > 3) || (Config.MAX_SUBCLASS > 3))
					{
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ModifyCustom.htm");
						final StringBuilder content3 = StringUtil.startAppend(200);
						int classIndex = 1;
						
						for (Iterator<SubClass> subList = Conditions.iterSubClasses(player); subList.hasNext();)
						{
							SubClass subClass = subList.next();
							
							StringUtil.append(content3, "Sub-class ", String.valueOf(classIndex++), "<br>", "<a action=\"bypass -h Aioitem_Subclass 6 ", String.valueOf(subClass.getClassIndex()), "\">", ClassListParser.getInstance().getClass(subClass.getClassId()).getClientCode(), "</a><br>");
						}
						html.replace("%list%", content3.toString());
					}
					else
					{
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_Modify.htm");
						if (player.getSubClasses().containsKey(1))
						{
							html.replace("%sub1%", ClassListParser.getInstance().getClass(player.getSubClasses().get(1).getClassId()).getClientCode());
						}
						else
						{
							html.replace("<a action=\"bypass -h Aioitem_Subclass 6 1\">%sub1%</a><br>", "");
						}
						
						if (player.getSubClasses().containsKey(2))
						{
							html.replace("%sub2%", ClassListParser.getInstance().getClass(player.getSubClasses().get(2).getClassId()).getClientCode());
						}
						else
						{
							html.replace("<a action=\"bypass -h Aioitem_Subclass 6 2\">%sub2%</a><br>", "");
						}
						
						if (player.getSubClasses().containsKey(3))
						{
							html.replace("%sub3%", ClassListParser.getInstance().getClass(player.getSubClasses().get(3).getClassId()).getClientCode());
						}
						else
						{
							html.replace("<a action=\"bypass -h Aioitem_Subclass 6 3\">%sub3%</a><br>", "");
						}
					}
					break;
				case 4:
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("add subclass"))
					{
						_log.warning(AioItemNpcs.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}
					
					boolean allowAddition = true;
					
					if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
					{
						allowAddition = false;
					}
					
					if (player.getLevel() < 75)
					{
						allowAddition = false;
					}
					
					if (allowAddition)
					{
						if (!player.getSubClasses().isEmpty())
						{
							for (Iterator<SubClass> subList = Conditions.iterSubClasses(player); subList.hasNext();)
							{
								SubClass subClass = subList.next();
								
								if (subClass.getLevel() < 75)
								{
									allowAddition = false;
									break;
								}
							}
						}
					}
					
					if (allowAddition && !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
					{
						allowAddition = Conditions.checkQuests(player);
					}
					
					if (allowAddition && Conditions.isValidNewSubClass(player, paramOne))
					{
						if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1))
						{
							return;
						}
						
						player.setActiveClass(player.getTotalSubClasses());
						
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_AddOk.htm");
						
						player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
					}
					else
					{
						html.setFile(player.getLang(), getSubClassFail());
					}
					break;
				case 5:
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change class"))
					{
						_log.warning(AioItemNpcs.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}
					
					if (player.getClassIndex() == paramOne)
					{
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_Current.htm");
						break;
					}
					
					if (paramOne == 0)
					{
						if (!Conditions.checkVillageMaster(player.getBaseClass()))
						{
							return;
						}
					}
					else
					{
						try
						{
							if (!Conditions.checkVillageMaster(player.getSubClasses().get(paramOne).getClassDefinition()))
							{
								return;
							}
						}
						catch (NullPointerException e)
						{
							return;
						}
					}
					
					player.setActiveClass(paramOne);
					player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED);
					return;
				case 6:
					if ((paramOne < 1) || (paramOne > Config.MAX_SUBCLASS))
					{
						return;
					}
					
					subsAvailable = Conditions.getAvailableSubClasses(player);
					
					if ((subsAvailable == null) || subsAvailable.isEmpty())
					{
						player.sendMessage("There are no sub classes available at this time.");
						return;
					}
					
					final StringBuilder content6 = StringUtil.startAppend(200);
					for (PlayerClass subClass : subsAvailable)
					{
						StringUtil.append(content6, "<a action=\"bypass -h Aioitem_Subclass 7 ", String.valueOf(paramOne), " ", String.valueOf(subClass.ordinal()), "\" msg=\"1445;", "\">", ClassListParser.getInstance().getClass(subClass.ordinal()).getClientCode(), "</a><br>");
					}
					
					switch (paramOne)
					{
						case 1:
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ModifyChoice1.htm");
							break;
						case 2:
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ModifyChoice2.htm");
							break;
						case 3:
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ModifyChoice3.htm");
							break;
						default:
							html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ModifyChoice.htm");
					}
					html.replace("%list%", content6.toString());
					break;
				case 7:
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change class"))
					{
						_log.warning(AioItemNpcs.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}
					
					if (!Conditions.isValidNewSubClass(player, paramTwo))
					{
						return;
					}
					
					if (player.modifySubClass(paramOne, paramTwo))
					{
						player.abortCast();
						player.stopAllEffectsExceptThoseThatLastThroughDeath();
						player.stopAllEffectsNotStayOnSubclassChange();
						player.stopCubics();
						player.setActiveClass(paramOne);
						
						html.setFile(player.getLang(), "data/html/AioItemNpcs/subclass/SubClass_ModifyOk.htm");
						html.replace("%name%", ClassListParser.getInstance().getClass(paramTwo).getClientCode());
						
						player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
					}
					else
					{
						player.setActiveClass(0);
						player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
						return;
					}
					break;
			}
			player.sendPacket(html);
		}
		else if (command.startsWith("createclan"))
		{
			String[] commandStr = command.split(" ");
			String cmdParams = "";
			
			if (commandStr.length >= 2)
			{
				cmdParams = commandStr[1];
			}
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			if (!Conditions.isValidName(cmdParams))
			{
				player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
				return;
			}
			
			ClanHolder.getInstance().createClan(player, cmdParams);
		}
		else if (command.startsWith("clanskills"))
		{
			itemIdToGet = Config.GET_FULL_CLAN_COIN;
			price = Config.GET_FULL_CLAN_PRICE;
			if ((player.getClan() == null) || !player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			player.destroyItemByItemId("Clan donate", itemIdToGet, price, player, true);
			player.getClan().changeLevel(11);
			player.sendMessage("Clan level set to 11");
			player.getClan().addReputationScore(500000, true);
			player.getClan().addNewSkill(SkillHolder.getInstance().getInfo(391, 1));
			int[] squad =
			{
				611,
				612,
				613,
				614,
				615,
				616
			};
			int[] normal =
			{
				370,
				371,
				372,
				373,
				374,
				375,
				376,
				377,
				378,
				379,
				380,
				381,
				382,
				383,
				384,
				385,
				386,
				387,
				388,
				389,
				390
			};
			for (int ids : normal)
			{
				player.getClan().addNewSkill(SkillHolder.getInstance().getInfo(ids, 3));
			}
			for (int ids : squad)
			{
				player.getClan().addNewSkill(SkillHolder.getInstance().getInfo(ids, 3), 0);
			}
			
			player.sendMessage("You have successfully perform this action");
		}
		else if (command.startsWith("changename"))
		{
			try
			{
				itemIdToGet = Config.CHANGE_NAME_COIN;
				price = Config.CHANGE_NAME_PRICE;
				
				String val = command.substring(11);
				if (!Util.isAlphaNumeric(val))
				{
					player.sendMessage("Invalid character name.");
					return;
				}
				
				if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
				{
					return;
				}
				
				if (CharNameHolder.getInstance().getIdByName(val) > 0)
				{
					player.sendMessage("Warning, name " + val + " already exists.");
					return;
				}
				player.destroyItemByItemId("Name Change", itemIdToGet, price, player, true);
				player.setName(val);
				player.getAppearance().setVisibleName(val);
				player.store();
				player.sendMessage("Your name has been changed to " + val);
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (L2PcInstance member : player.getParty().getMembers())
					{
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				player.sendMessage("Player name box cannot be empty.");
			}
		}
		else if (command.startsWith("changeclanname"))
		{
			try
			{
				itemIdToGet = Config.CHANGE_CNAME_COIN;
				price = Config.CHANGE_CNAME_PRICE;
				
				String val = command.substring(15);
				if ((player.getClan() == null) || !player.isClanLeader())
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				if (!Util.isAlphaNumeric(val))
				{
					player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
					return;
				}
				
				if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
				{
					return;
				}
				
				if (ClanHolder.getInstance().getClanByName(val) != null)
				{
					player.sendMessage("Warning, clan name " + val + " already exists.");
					return;
				}
				
				player.destroyItemByItemId("Clan Name Change", itemIdToGet, price, player, true);
				player.getClan().setName(val);
				player.getClan().updateClanNameInDB();
				player.sendMessage("Your clan name has been changed to " + val);
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (L2PcInstance member : player.getParty().getMembers())
					{
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				player.sendMessage("Clan name box cannot be empty.");
			}
		}
		else if (command.startsWith("addaugment"))
		{
			int value = 0;
			int pskill = 0;
			
			String[] stats = command.split(" ");
			
			if (stats[1].equals("STR+1"))
			{
				value = 16341;
			}
			else if (stats[1].equals("INT+1"))
			{
				value = 16343;
			}
			else if (stats[1].equals("MEN+1"))
			{
				value = 16344;
			}
			else if (stats[1].equals("CON+1"))
			{
				value = 16342;
			}
			
			if (stats[2].equals("Heal_Empower"))
			{
				pskill = 16279;
			}
			else if (stats[2].equals("Wild_Magic"))
			{
				pskill = 16336;
			}
			else if (stats[2].equals("Empower"))
			{
				pskill = 16281;
			}
			else if (stats[2].equals("Magic_Barrier"))
			{
				pskill = 16282;
			}
			else if (stats[2].equals("Might"))
			{
				pskill = 16283;
			}
			else if (stats[2].equals("Shield"))
			{
				pskill = 16284;
			}
			else if (stats[2].equals("Duel_Might"))
			{
				pskill = 16285;
			}
			
			pskill = pskill + 8358;
			int armorType = -1;
			armorType = Inventory.PAPERDOLL_RHAND;
			itemIdToGet = Config.AUGMENT_COIN;
			price = Config.AUGMENT_PRICE;
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			L2ItemInstance itemInstance = null;
			L2ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
			if (parmorInstance == null)
			{
				player.sendMessage("Equip the weapon for augmentation.");
				return;
			}
			if (parmorInstance.isAugmented())
			{
				player.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
				return;
			}
			if (parmorInstance.isHeroItem() || (parmorInstance.isShadowItem() || (parmorInstance.isCommonItem() || (parmorInstance.isEtcItem() || (parmorInstance.isTimeLimitedItem())))))
			{
				player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
				return;
			}
			if (parmorInstance.getLocationSlot() == armorType)
			{
				itemInstance = parmorInstance;
			}
			
			if (itemInstance != null)
			{
				player.destroyItemByItemId("augment", itemIdToGet, price, player, true);
				player.getInventory().unEquipItemInSlot(armorType);
				itemInstance.setAugmentation(new L2Augmentation(((pskill << 16) + value)));
				player.getInventory().equipItem(itemInstance);
				player.sendPacket(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
				
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(itemInstance);
				player.sendPacket(iu);
				player.broadcastPacket(new CharInfo(player));
				player.sendPacket(new UserInfo(player));
				player.broadcastPacket(new ExBrExtraUserInfo(player));
			}
		}
		else if (command.startsWith("teleportTo"))
		{
			if (Config.AIO_ENABLE_TP_DELAY && AioItemDelay._delayers.contains(player))
			{
				if (Config.AIO_DELAY_SENDMESSAGE)
				{
					player.sendMessage("In order to use Aio Item teleport function again, you will have to wait " + Config.AIO_DELAY + "!");
				}
				return;
			}
			
			itemIdToGet = Config.AIO_TPCOIN;
			price = Config.AIO_PRICE_PERTP;
			
			if (player.isTransformed())
			{
				if ((player.getTransformationId() == 9) || (player.getTransformationId() == 8))
				{
					player.untransform();
				}
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			try
			{
				L2AioTeleport list = AIOItemParser.getInstance().getTeleportId(Integer.parseInt(subCommand[1]));
				
				if (player.isInsideZone(ZoneId.PEACE) || player.isGM())
				{
					doTeleport(player, list.getLocX(), list.getLocY(), list.getLocZ());
				}
				else
				{
					doTeleportLong(player, list.getLocX(), list.getLocY(), list.getLocZ());
					
					if (Config.AIO_ENABLE_TP_DELAY)
					{
						ThreadPoolManager.getInstance().executeTask(new AioItemDelay(player));
					}
				}
				player.destroyItemByItemId("AIO Teleport", itemIdToGet, price, player, true);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "AioItemNpcs: Teleportation error.", e);
			}
		}
		else if (command.startsWith("rankarenainfo"))
		{
			if (Config.RANK_ARENA_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(5);
				html.setHtml(ArenaLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		
		else if (command.startsWith("rankfishermaninfo"))
		{
			if (Config.RANK_FISHERMAN_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(5);
				html.setHtml(FishermanLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		
		else if (command.startsWith("rankcraftinfo"))
		{
			if (Config.RANK_CRAFT_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(5);
				html.setHtml(CraftLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		
		else if (command.startsWith("ranktvtinfo"))
		{
			if (Config.RANK_TVT_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(5);
				html.setHtml(TvTLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		else if (command.startsWith("changeGender"))
		{
			if (command.startsWith("changeGenderDonate"))
			{
				itemIdToGet = Config.CHANGE_GENDER_DONATE_COIN;
				price = Config.CHANGE_GENDER_DONATE_PRICE;
			}
			else if (command.startsWith("changeGenderNormal"))
			{
				itemIdToGet = Config.CHANGE_GENDER_NORMAL_COIN;
				price = Config.CHANGE_GENDER_NORMAL_PRICE;
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			player.destroyItemByItemId("changeGender", itemIdToGet, price, player, true);
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.sendMessage("Your gender has been changed.");
			player.broadcastUserInfo();
			TransformParser.getInstance().transformPlayer(105, player);
			TransformFinalizer ef = new TransformFinalizer(player);
			player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, 200));
		}
		else if (command.startsWith("showMultiSellWindow"))
		{
			try
			{
				int multi = Integer.valueOf(subCommand[1]);
				player.setIsUsingAioMultisell(true);
				MultiSellParser.getInstance().separateAndSend(multi, player, null, false);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "AioItemNpcs: Error with using MultiSell.", e);
			}
		}
		else if (command.startsWith("TopPvp"))
		{
			GenerateHtmls.showTopPvp(player);
		}
		else if (command.startsWith("TopPk"))
		{
			GenerateHtmls.showTopPk(player);
		}
		else if (command.startsWith("TopClan"))
		{
			GenerateHtmls.showTopClan(player);
		}
		else if (command.startsWith("rbinfo"))
		{
			GenerateHtmls.showRbInfo(player);
		}
		else if (command.startsWith("addAugment"))
		{
			player.sendPacket(new ExShowVariationMakeWindow());
		}
		else if (command.startsWith("delAugment"))
		{
			player.sendPacket(new ExShowVariationCancelWindow());
		}
		else if (command.startsWith("removeAtt"))
		{
			player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
		}
		else if (command.startsWith("drawSymbol"))
		{
			List<L2Henna> tato = HennaParser.getInstance().getHennaList(player.getClassId());
			player.sendPacket(new HennaEquipList(player, tato));
		}
		else if (command.startsWith("removeSymbol"))
		{
			boolean hasHennas = false;
			
			for (int i = 1; i <= 3; i++)
			{
				L2Henna henna = player.getHenna(i);
				
				if (henna != null)
				{
					hasHennas = true;
				}
			}
			if (hasHennas)
			{
				player.sendPacket(new HennaRemoveList(player));
			}
			else
			{
				player.sendMessage("You do not have dyes.");
			}
		}
		else if (command.startsWith("adenaToItem"))
		{
			if (player.getAdena() > Config.AIO_EXCHANGE_PRICE)
			{
				player.destroyItemByItemId("Aio Item", 57, Config.AIO_EXCHANGE_PRICE, player, true);
				player.addItem("Aio Item", Config.AIO_EXCHANGE_ID, 1, player, true);
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			GenerateHtmls.sendPacket(player, "service/exchange.htm");
		}
		else if (command.startsWith("itemToAdena"))
		{
			itemIdToGet = Config.AIO_EXCHANGE_ID;
			price = 1;
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			player.destroyItemByItemId("Aio Item", Config.AIO_EXCHANGE_ID, price, player, true);
			player.addItem("Aio Item", 57, Config.AIO_EXCHANGE_PRICE, player, true);
			
			GenerateHtmls.sendPacket(player, "service/exchange.htm");
		}
	}
	
	private static void doTeleport(L2PcInstance player, int x, int y, int z)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.teleToLocation(x, y, z, true);
	}
	
	public static void doTeleportLong(L2PcInstance player, int x, int y, int z)
	{
		player.abortCast();
		player.abortAttack();
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setTarget(player);
		player.disableAllSkills();
		Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, 1050, 1, 30000, 0), 810000);
		player.sendPacket(new SetupGauge(0, 30000));
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(player, x, y, z), 30000));
		player.forceIsCasting(10 + GameTimeController.getInstance().getGameTicks() + (30000 / GameTimeController.MILLIS_IN_TICK));
	}
	
	private static class Teleport implements Runnable
	{
		private final String _playerName;
		
		private final int _x, _y, _z;
		
		Teleport(L2PcInstance activeChar, int x, int y, int z)
		{
			_playerName = activeChar.getName();
			_x = x;
			_y = y;
			_z = z;
		}
		
		@Override
		public void run()
		{
			try
			{
				L2PcInstance player = L2World.getInstance().getPlayer(_playerName);
				if (player != null)
				{
					player.teleToLocation(_x, _y, _z, true);
					player.setIsCastingNow(false);
					player.enableAllSkills();
				}
			}
			catch (Throwable e)
			{
				
			}
		}
	}
}