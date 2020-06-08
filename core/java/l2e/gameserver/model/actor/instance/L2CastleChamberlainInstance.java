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

import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.TeleLocationParser;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2TeleportLocation;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExShowDominionRegistry;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;
import l2e.util.StringUtil;

public class L2CastleChamberlainInstance extends L2MerchantInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public L2CastleChamberlainInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2CastleChamberlainInstance);
	}
	
	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getId()));
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.getLastFolkNPC().getObjectId() != getObjectId())
		{
			return;
		}
		
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
		{
			return;
		}
		else if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			return;
		}
		else if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken();
			
			String val = "";
			if (st.hasMoreTokens())
			{
				val = st.nextToken();
			}
			
			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_DISMISS))
				{
					return;
				}
				if (siegeBlocksFunction(player))
				{
					return;
				}
				getCastle().banishForeigners();
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-banishafter.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("banish_foreigner_show"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_DISMISS))
				{
					return;
				}
				if (siegeBlocksFunction(player))
				{
					return;
				}
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-banishfore.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list_siege_clans"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
				{
					getCastle().getSiege().listRegisterClan(player);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list_territory_clans"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
				{
					player.sendPacket(new ExShowDominionRegistry(getCastle().getId(), player));
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("receive_report"))
			{
				if (player.isClanLeader())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-report.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					L2Clan clan = ClanHolder.getInstance().getClan(getCastle().getOwnerId());
					html.replace("%clanname%", clan.getName());
					html.replace("%clanleadername%", clan.getLeaderName());
					html.replace("%castlename%", getCastle().getName());
					
					int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
					switch (currentPeriod)
					{
						case SevenSigns.PERIOD_COMP_RECRUITING:
							html.replace("%ss_event%", "Quest Event Initialization");
							break;
						case SevenSigns.PERIOD_COMPETITION:
							html.replace("%ss_event%", "Competition (Quest Event)");
							break;
						case SevenSigns.PERIOD_COMP_RESULTS:
							html.replace("%ss_event%", "Quest Event Results");
							break;
						case SevenSigns.PERIOD_SEAL_VALIDATION:
							html.replace("%ss_event%", "Seal Validation");
							break;
					}
					int sealOwner1 = SevenSigns.getInstance().getSealOwner(1);
					switch (sealOwner1)
					{
						case SevenSigns.CABAL_NULL:
							html.replace("%ss_avarice%", "Not in Possession");
							break;
						case SevenSigns.CABAL_DAWN:
							html.replace("%ss_avarice%", "Lords of Dawn");
							break;
						case SevenSigns.CABAL_DUSK:
							html.replace("%ss_avarice%", "Revolutionaries of Dusk");
							break;
					}
					int sealOwner2 = SevenSigns.getInstance().getSealOwner(2);
					switch (sealOwner2)
					{
						case SevenSigns.CABAL_NULL:
							html.replace("%ss_gnosis%", "Not in Possession");
							break;
						case SevenSigns.CABAL_DAWN:
							html.replace("%ss_gnosis%", "Lords of Dawn");
							break;
						case SevenSigns.CABAL_DUSK:
							html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
							break;
					}
					int sealOwner3 = SevenSigns.getInstance().getSealOwner(3);
					switch (sealOwner3)
					{
						case SevenSigns.CABAL_NULL:
							html.replace("%ss_strife%", "Not in Possession");
							break;
						case SevenSigns.CABAL_DAWN:
							html.replace("%ss_strife%", "Lords of Dawn");
							break;
						case SevenSigns.CABAL_DUSK:
							html.replace("%ss_strife%", "Revolutionaries of Dusk");
							break;
					}
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("items"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS)
				{
					if (val.isEmpty())
					{
						return;
					}
					// player.tempInventoryDisable();
					
					if (Config.DEBUG)
					{
						_log.fine("Showing chamberlain buylist");
					}
					
					showBuyWindow(player, Integer.parseInt(val + "1"));
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage_siege_defender"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
				{
					getCastle().getSiege().listRegisterClan(player);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage_vault"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_TAXES) == L2Clan.CP_CS_TAXES)
				{
					String filename = "data/html/chamberlain/chamberlain-vault.htm";
					long amount = 0;
					if (val.equalsIgnoreCase("deposit"))
					{
						try
						{
							amount = Long.parseLong(st.nextToken());
						}
						catch (NoSuchElementException e)
						{
						}
						if ((amount > 0) && ((getCastle().getTreasury() + amount) < PcInventory.MAX_ADENA))
						{
							if (player.reduceAdena("Castle", amount, this, true))
							{
								getCastle().addToTreasuryNoTax(amount);
							}
							else
							{
								sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
							}
						}
					}
					else if (val.equalsIgnoreCase("withdraw"))
					{
						try
						{
							amount = Long.parseLong(st.nextToken());
						}
						catch (NoSuchElementException e)
						{
						}
						if (amount > 0)
						{
							if (getCastle().getTreasury() < amount)
							{
								filename = "data/html/chamberlain/chamberlain-vault-no.htm";
							}
							else
							{
								if (getCastle().addToTreasuryNoTax((-1) * amount))
								{
									player.addAdena("Castle", amount, this, true);
								}
							}
						}
					}
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					html.replace("%tax_income%", Util.formatAdena(getCastle().getTreasury()));
					html.replace("%withdraw_amount%", Util.formatAdena(amount));
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("operate_door"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
				{
					if (!val.isEmpty())
					{
						boolean open = (Integer.parseInt(val) == 1);
						while (st.hasMoreTokens())
						{
							getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
						}
						
						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						String file = "data/html/chamberlain/doors-close.htm";
						if (open)
						{
							file = "data/html/chamberlain/doors-open.htm";
						}
						html.setFile(player.getLang(), file);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
						return;
					}
					
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/" + getTemplate().getId() + "-d.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("tax_set"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_TAXES) == L2Clan.CP_CS_TAXES)
				{
					if (!val.isEmpty())
					{
						getCastle().setTaxPercent(player, Integer.parseInt(val));
					}
					
					final String msg = StringUtil.concat("<html><body>", getName(), ":<br>" + "Current tax rate: ", String.valueOf(getCastle().getTaxPercent()), "%<br>" + "<table>" + "<tr>" + "<td>Change tax rate to:</td>" + "<td><edit var=\"value\" width=40><br>" + "<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "</tr>" + "</table>" + "</center>" + "</body></html>");
					sendHtmlMessage(player, msg);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-tax.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%tax%", String.valueOf(getCastle().getTaxPercent()));
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage_functions"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-manage.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("products"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-products.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcId%", String.valueOf(getId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("functions"))
			{
				if (val.equalsIgnoreCase("tele"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getCastle().getFunction(Castle.FUNC_TELEPORT) == null)
					{
						html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-nac.htm");
					}
					else
					{
						html.setFile(player.getLang(), "data/html/chamberlain/" + getId() + "-t" + getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("support"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getCastle().getFunction(Castle.FUNC_SUPPORT) == null)
					{
						html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-nac.htm");
					}
					else
					{
						html.setFile(player.getLang(), "data/html/chamberlain/support" + getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() + ".htm");
						html.replace("%mp%", String.valueOf((int) getCurrentMp()));
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("back"))
				{
					showChatWindow(player);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-functions.htm");
					if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
					{
						html.replace("%xp_regen%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl()));
					}
					else
					{
						html.replace("%xp_regen%", "0");
					}
					if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
					{
						html.replace("%hp_regen%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl()));
					}
					else
					{
						html.replace("%hp_regen%", "0");
					}
					if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
					{
						html.replace("%mp_regen%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl()));
					}
					else
					{
						html.replace("%mp_regen%", "0");
					}
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
				{
					if (val.equalsIgnoreCase("recovery"))
					{
						if (st.countTokens() >= 1)
						{
							if (getCastle().getOwnerId() == 0)
							{
								player.sendMessage("This castle have no owner, you cannot change configuration");
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("hp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel.htm");
								html.replace("%apply%", "recovery hp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("mp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel.htm");
								html.replace("%apply%", "recovery mp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("exp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel.htm");
								html.replace("%apply%", "recovery exp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_hp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-apply.htm");
								html.replace("%name%", "Fireplace (HP Recovery Device)");
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 80:
										cost = Config.CS_HPREG1_FEE;
										break;
									case 120:
										cost = Config.CS_HPREG2_FEE;
										break;
									case 180:
										cost = Config.CS_HPREG3_FEE;
										break;
									case 240:
										cost = Config.CS_HPREG4_FEE;
										break;
									default:
										cost = Config.CS_HPREG5_FEE;
										break;
								}
								
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" + String.valueOf(Config.CS_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day</font>)");
								html.replace("%use%", "Provides additional HP recovery for clan members in the castle.<font color=\"00FFFF\">" + String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery hp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_mp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-apply.htm");
								html.replace("%name%", "Carpet (MP Recovery)");
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CS_MPREG1_FEE;
										break;
									case 15:
										cost = Config.CS_MPREG2_FEE;
										break;
									case 30:
										cost = Config.CS_MPREG3_FEE;
										break;
									default:
										cost = Config.CS_MPREG4_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" + String.valueOf(Config.CS_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day</font>)");
								html.replace("%use%", "Provides additional MP recovery for clan members in the castle.<font color=\"00FFFF\">" + String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery mp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_exp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-apply.htm");
								html.replace("%name%", "Chandelier (EXP Recovery Device)");
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 15:
										cost = Config.CS_EXPREG1_FEE;
										break;
									case 25:
										cost = Config.CS_EXPREG2_FEE;
										break;
									case 35:
										cost = Config.CS_EXPREG3_FEE;
										break;
									default:
										cost = Config.CS_EXPREG4_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" + String.valueOf(Config.CS_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day</font>)");
								html.replace("%use%", "Restores the Exp of any clan member who is resurrected in the castle.<font color=\"00FFFF\">" + String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery exp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("hp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										_log.warning("Hp editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getLang(), "data/html/chamberlain/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
											break;
										case 80:
											fee = Config.CS_HPREG1_FEE;
											break;
										case 120:
											fee = Config.CS_HPREG2_FEE;
											break;
										case 180:
											fee = Config.CS_HPREG3_FEE;
											break;
										case 240:
											fee = Config.CS_HPREG4_FEE;
											break;
										default:
											fee = Config.CS_HPREG5_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_HP, percent, fee, Config.CS_HPREG_FEE_RATIO, (getCastle().getFunction(Castle.FUNC_RESTORE_HP) == null)))
									{
										html.setFile(player.getLang(), "data/html/chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("mp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										_log.warning("Mp editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getLang(), "data/html/chamberlain/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
											break;
										case 5:
											fee = Config.CS_MPREG1_FEE;
											break;
										case 15:
											fee = Config.CS_MPREG2_FEE;
											break;
										case 30:
											fee = Config.CS_MPREG3_FEE;
											break;
										default:
											fee = Config.CS_MPREG4_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_MP, percent, fee, Config.CS_MPREG_FEE_RATIO, (getCastle().getFunction(Castle.FUNC_RESTORE_MP) == null)))
									{
										html.setFile(player.getLang(), "data/html/chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("exp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										_log.warning("Exp editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getLang(), "data/html/chamberlain/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
											break;
										case 15:
											fee = Config.CS_EXPREG1_FEE;
											break;
										case 25:
											fee = Config.CS_EXPREG2_FEE;
											break;
										case 35:
											fee = Config.CS_EXPREG3_FEE;
											break;
										default:
											fee = Config.CS_EXPREG4_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_EXP, percent, fee, Config.CS_EXPREG_FEE_RATIO, (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) == null)))
									{
										html.setFile(player.getLang(), "data/html/chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getLang(), "data/html/chamberlain/edit_recovery.htm");
						String hp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
						String exp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
						String mp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
						if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
						{
							html.replace("%hp_recovery%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLease()) + "</font>Adena /" + String.valueOf(Config.CS_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%hp_period%", "Withdraw the fee for the next time at " + format.format(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getEndTime()));
							html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>]" + hp);
						}
						else
						{
							html.replace("%hp_recovery%", "none");
							html.replace("%hp_period%", "none");
							html.replace("%change_hp%", hp);
						}
						if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
						{
							html.replace("%exp_recovery%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLease()) + "</font>Adena /" + String.valueOf(Config.CS_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%exp_period%", "Withdraw the fee for the next time at " + format.format(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getEndTime()));
							html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>]" + exp);
						}
						else
						{
							html.replace("%exp_recovery%", "none");
							html.replace("%exp_period%", "none");
							html.replace("%change_exp%", exp);
						}
						if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
						{
							html.replace("%mp_recovery%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLease()) + "</font>Adena /" + String.valueOf(Config.CS_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%mp_period%", "Withdraw the fee for the next time at " + format.format(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getEndTime()));
							html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>]" + mp);
						}
						else
						{
							html.replace("%mp_recovery%", "none");
							html.replace("%mp_period%", "none");
							html.replace("%change_mp%", mp);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("other"))
					{
						if (st.countTokens() >= 1)
						{
							if (getCastle().getOwnerId() == 0)
							{
								player.sendMessage("This castle have no owner, you cannot change configuration");
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("tele_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel.htm");
								html.replace("%apply%", "other tele 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("support_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel.htm");
								html.replace("%apply%", "other support 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_support"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-apply.htm");
								html.replace("%name%", "Insignia (Supplementary Magic)");
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CS_SUPPORT1_FEE;
										break;
									case 2:
										cost = Config.CS_SUPPORT2_FEE;
										break;
									case 3:
										cost = Config.CS_SUPPORT3_FEE;
										break;
									default:
										cost = Config.CS_SUPPORT4_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" + String.valueOf(Config.CS_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day</font>)");
								html.replace("%use%", "Enables the use of supplementary magic.");
								html.replace("%apply%", "other support " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_tele"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getLang(), "data/html/chamberlain/functions-apply.htm");
								html.replace("%name%", "Mirror (Teleportation Device)");
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CS_TELE1_FEE;
										break;
									default:
										cost = Config.CS_TELE2_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" + String.valueOf(Config.CS_TELE_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day</font>)");
								html.replace("%use%", "Teleports clan members in a castle to the target <font color=\"00FFFF\">Stage " + String.valueOf(stage) + "</font> staging area");
								html.replace("%apply%", "other tele " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("tele"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										_log.warning("Tele editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_TELEPORT) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getLang(), "data/html/chamberlain/functions-used.htm");
											html.replace("%val%", "Stage " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CS_TELE1_FEE;
											break;
										default:
											fee = Config.CS_TELE2_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_TELEPORT, lvl, fee, Config.CS_TELE_FEE_RATIO, (getCastle().getFunction(Castle.FUNC_TELEPORT) == null)))
									{
										html.setFile(player.getLang(), "data/html/chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("support"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										_log.warning("Support editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_SUPPORT) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getLang(), "data/html/chamberlain/functions-used.htm");
											html.replace("%val%", "Stage " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CS_SUPPORT1_FEE;
											break;
										case 2:
											fee = Config.CS_SUPPORT2_FEE;
											break;
										case 3:
											fee = Config.CS_SUPPORT3_FEE;
											break;
										default:
											fee = Config.CS_SUPPORT4_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_SUPPORT, lvl, fee, Config.CS_SUPPORT_FEE_RATIO, (getCastle().getFunction(Castle.FUNC_SUPPORT) == null)))
									{
										html.setFile(player.getLang(), "data/html/chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										sendHtmlMessage(player, html);
									}
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getLang(), "data/html/chamberlain/edit_other.htm");
						String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";
						String support = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]";
						if (getCastle().getFunction(Castle.FUNC_TELEPORT) != null)
						{
							html.replace("%tele%", "Stage " + String.valueOf(getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl()) + "</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_TELEPORT).getLease()) + "</font>Adena /" + String.valueOf(Config.CS_TELE_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%tele_period%", "Withdraw the fee for the next time at " + format.format(getCastle().getFunction(Castle.FUNC_TELEPORT).getEndTime()));
							html.replace("%change_tele%", "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Deactivate</a>]" + tele);
						}
						else
						{
							html.replace("%tele%", "none");
							html.replace("%tele_period%", "none");
							html.replace("%change_tele%", tele);
						}
						if (getCastle().getFunction(Castle.FUNC_SUPPORT) != null)
						{
							html.replace("%support%", "Stage " + String.valueOf(getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl()) + "</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_SUPPORT).getLease()) + "</font>Adena /" + String.valueOf(Config.CS_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%support_period%", "Withdraw the fee for the next time at " + format.format(getCastle().getFunction(Castle.FUNC_SUPPORT).getEndTime()));
							html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>]" + support);
						}
						else
						{
							html.replace("%support%", "none");
							html.replace("%support_period%", "none");
							html.replace("%change_support%", support);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("back"))
					{
						showChatWindow(player);
					}
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getLang(), "data/html/chamberlain/manage.htm");
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("support"))
			{
				setTarget(player);
				L2Skill skill;
				if (val.isEmpty())
				{
					return;
				}
				
				try
				{
					int skill_id = Integer.parseInt(val);
					try
					{
						if (getCastle().getFunction(Castle.FUNC_SUPPORT) == null)
						{
							return;
						}
						if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == 0)
						{
							return;
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						int skill_lvl = 0;
						if (st.countTokens() >= 1)
						{
							skill_lvl = Integer.parseInt(st.nextToken());
						}
						skill = SkillHolder.getInstance().getInfo(skill_id, skill_lvl);
						if (skill.getSkillType() == L2SkillType.SUMMON)
						{
							player.doSimultaneousCast(skill);
						}
						else
						{
							if (!((skill.getMpConsume() + skill.getMpInitialConsume()) > getCurrentMp()))
							{
								this.doCast(skill);
							}
							else
							{
								html.setFile(player.getLang(), "data/html/chamberlain/support-no_mana.htm");
								html.replace("%mp%", String.valueOf((int) getCurrentMp()));
								sendHtmlMessage(player, html);
								return;
							}
						}
						html.setFile(player.getLang(), "data/html/chamberlain/support-done.htm");
						html.replace("%mp%", String.valueOf((int) getCurrentMp()));
						sendHtmlMessage(player, html);
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid skill level, contact your admin!");
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid skill level, contact your admin!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("support_back"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == 0)
				{
					return;
				}
				html.setFile(player.getLang(), "data/html/chamberlain/support" + getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() + ".htm");
				html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
				sendHtmlMessage(player, html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("goto"))
			{
				int whereTo = Integer.parseInt(val);
				doTeleport(player, whereTo);
				return;
			}
			else if (actualCommand.equals("give_crown"))
			{
				if (siegeBlocksFunction(player))
				{
					return;
				}
				
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				
				if (player.isClanLeader())
				{
					if (player.getInventory().getItemByItemId(6841) == null)
					{
						L2ItemInstance crown = player.getInventory().addItem("Castle Crown", 6841, 1, player, this);
						
						SystemMessage ms = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						ms.addItemName(crown);
						player.sendPacket(ms);
						
						html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-gavecrown.htm");
						html.replace("%CharName%", String.valueOf(player.getName()));
						html.replace("%FeudName%", String.valueOf(getCastle().getName()));
					}
					else
					{
						html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-hascrown.htm");
					}
				}
				else
				{
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
				}
				
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manors_cert"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (isMyLord(player) || (validatePrivileges(player, 5) && (validateCondition(player) == COND_OWNER)))
				{
					if (getCastle().getSiege().getIsInProgress())
					{
						html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-busy.htm");
						html.replace("%npcname%", String.valueOf(getName()));
					}
					else
					{
						final int cabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
						if ((cabal == SevenSigns.CABAL_DAWN) && SevenSigns.getInstance().isCompetitionPeriod())
						{
							final int ticketCount = getCastle().getTicketBuyCount();
							if (ticketCount < (Config.SSQ_DAWN_TICKET_QUANTITY / Config.SSQ_DAWN_TICKET_BUNDLE))
							{
								html.setFile(player.getLang(), "data/html/chamberlain/ssq_selldawnticket.htm");
								html.replace("%DawnTicketLeft%", String.valueOf(Config.SSQ_DAWN_TICKET_QUANTITY - (ticketCount * Config.SSQ_DAWN_TICKET_BUNDLE)));
								html.replace("%DawnTicketBundle%", String.valueOf(Config.SSQ_DAWN_TICKET_BUNDLE));
								html.replace("%DawnTicketPrice%", String.valueOf(Config.SSQ_DAWN_TICKET_PRICE * Config.SSQ_DAWN_TICKET_BUNDLE));
							}
							else
							{
								html.setFile(player.getLang(), "data/html/chamberlain/ssq_notenoughticket.htm");
							}
						}
						else
						{
							html.setFile(player.getLang(), "data/html/chamberlain/ssq_notdawnorevent.htm");
						}
					}
				}
				else
				{
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
				}
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("manors_cert_confirm"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (isMyLord(player) || (validatePrivileges(player, 5) && (validateCondition(player) == COND_OWNER)))
				{
					if (getCastle().getSiege().getIsInProgress())
					{
						html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-busy.htm");
						html.replace("%npcname%", String.valueOf(getName()));
					}
					else
					{
						final int cabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
						if ((cabal == SevenSigns.CABAL_DAWN) && SevenSigns.getInstance().isCompetitionPeriod())
						{
							final int ticketCount = getCastle().getTicketBuyCount();
							if (ticketCount < (Config.SSQ_DAWN_TICKET_QUANTITY / Config.SSQ_DAWN_TICKET_BUNDLE))
							{
								final long totalCost = Config.SSQ_DAWN_TICKET_PRICE * Config.SSQ_DAWN_TICKET_BUNDLE;
								if (player.getAdena() >= totalCost)
								{
									player.reduceAdena(actualCommand, totalCost, this, true);
									player.addItem(actualCommand, Config.SSQ_MANORS_AGREEMENT_ID, Config.SSQ_DAWN_TICKET_BUNDLE, this, true);
									getCastle().setTicketBuyCount(ticketCount + 1);
									return;
								}
								html.setFile(player.getLang(), "data/html/chamberlain/chamberlain_noadena.htm");
							}
							else
							{
								html.setFile(player.getLang(), "data/html/chamberlain/ssq_notenoughticket.htm");
							}
						}
						else
						{
							html.setFile(player.getLang(), "data/html/chamberlain/ssq_notdawnorevent.htm");
						}
					}
				}
				else
				{
					html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
				}
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
	}
	
	private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setHtml(htmlMessage);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/chamberlain/chamberlain-no.htm";
		
		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/chamberlain/chamberlain-busy.htm";
			}
			else if (condition == COND_OWNER)
			{
				filename = "data/html/chamberlain/chamberlain.htm";
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void doTeleport(L2PcInstance player, int val)
	{
		if (Config.DEBUG)
		{
			_log.warning("doTeleport(L2PcInstance player, int val) is called");
		}
		L2TeleportLocation list = TeleLocationParser.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.destroyItemByItemId("Teleport", list.getItemId(), list.getPrice(), this, true))
			{
				if (Config.DEBUG)
				{
					_log.warning("Teleporting player " + player.getName() + " for Castle to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				}
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
			}
		}
		else
		{
			_log.warning("No teleport destination with id:" + val);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected int validateCondition(L2PcInstance player)
	{
		if ((getCastle() != null) && (getCastle().getId() > 0))
		{
			if (player.getClan() != null)
			{
				if (getCastle().getZone().isActive())
				{
					return COND_BUSY_BECAUSE_OF_SIEGE;
				}
				else if (getCastle().getOwnerId() == player.getClanId())
				{
					return COND_OWNER;
				}
			}
		}
		return COND_ALL_FALSE;
	}
	
	private boolean validatePrivileges(L2PcInstance player, int privilege)
	{
		if ((player.getClanPrivileges() & privilege) != privilege)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
			player.sendPacket(html);
			return false;
		}
		return true;
	}
	
	private boolean siegeBlocksFunction(L2PcInstance player)
	{
		if (getCastle().getSiege().getIsInProgress())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getLang(), "data/html/chamberlain/chamberlain-busy.htm");
			html.replace("%npcname%", String.valueOf(getName()));
			player.sendPacket(html);
			return true;
		}
		return false;
	}
}