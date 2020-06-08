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

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.L2SquadTrainer;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.util.Rnd;

public class L2CastleMagicianInstance extends L2NpcInstance implements L2SquadTrainer
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public L2CastleMagicianInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		setInstanceType(InstanceType.L2CastleMagicianInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/castlemagician/magician-no.htm";
		
		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/castlemagician/magician-busy.htm";
			}
			else if (condition == COND_OWNER)
			{
				if (val == 0)
				{
					filename = "data/html/castlemagician/magician.htm";
				}
				else
				{
					filename = "data/html/castlemagician/magician-" + val + ".htm";
				}
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			showChatWindow(player, val);
			return;
		}
		else if (command.startsWith("ExchangeKE"))
		{
			String filename = null;
			int item;
			int i0 = Rnd.get(100);
			if (i0 < 5)
			{
				int i1 = Rnd.get(100);
				if (i1 < 5)
				{
					item = 9931;
				}
				else if (i1 <= 50)
				{
					item = 9932;
				}
				else if (i1 <= 75)
				{
					item = 10416;
				}
				else
				{
					item = 10417;
				}
			}
			else if (i0 <= 15)
			{
				switch (Rnd.get(5))
				{
					case 1:
						item = 9917;
						break;
					case 2:
						item = 9918;
						break;
					case 3:
						item = 9928;
						break;
					case 4:
						item = 9929;
						break;
					default:
						item = 9920;
						
				}
			}
			else if (i0 <= 30)
			{
				switch (Rnd.get(8))
				{
					case 1:
						item = 9916;
						break;
					case 2:
						item = 9916;
						break;
					case 3:
						item = 9924;
						break;
					case 4:
						item = 9925;
						break;
					case 5:
						item = 9926;
						break;
					case 6:
						item = 9927;
						break;
					case 7:
						item = 10518;
						break;
					default:
						item = 10424;
				}
			}
			else
			{
				switch (Rnd.get(46))
				{
					case 0:
						item = 9914;
						break;
					case 1:
						item = 9915;
						break;
					case 2:
						item = 9920;
						break;
					case 3:
						item = 9920;
						break;
					case 4:
						item = 9921;
						break;
					case 5:
						item = 9922;
						break;
					case 6:
						item = 9933;
						break;
					case 7:
						item = 9934;
						break;
					case 8:
						item = 9935;
						break;
					case 9:
						item = 9936;
						break;
					case 10:
						item = 9937;
						break;
					case 11:
						item = 9938;
						break;
					case 12:
						item = 9939;
						break;
					case 13:
						item = 9940;
						break;
					case 14:
						item = 9941;
						break;
					case 15:
						item = 9942;
						break;
					case 16:
						item = 9943;
						break;
					case 17:
						item = 9944;
						break;
					case 18:
						item = 9945;
						break;
					case 19:
						item = 9946;
						break;
					case 20:
						item = 9947;
						break;
					case 21:
						item = 9948;
						break;
					case 22:
						item = 9949;
						break;
					case 23:
						item = 9950;
						break;
					case 24:
						item = 9965;
						break;
					case 25:
						item = 9952;
						break;
					case 26:
						item = 9953;
						break;
					case 27:
						item = 9954;
						break;
					case 28:
						item = 9955;
						break;
					case 29:
						item = 9956;
						break;
					case 30:
						item = 9957;
						break;
					case 31:
						item = 9958;
						break;
					case 32:
						item = 9959;
						break;
					case 33:
						item = 9960;
						break;
					case 34:
						item = 9961;
						break;
					case 35:
						item = 9962;
						break;
					case 36:
						item = 9963;
						break;
					case 37:
						item = 9964;
						break;
					case 38:
						item = 10418;
						break;
					case 39:
						item = 10420;
						break;
					case 40:
						item = 10519;
						break;
					case 41:
						item = 10422;
						break;
					case 42:
						item = 10423;
						break;
					case 43:
						item = 10419;
						break;
					default:
						item = 10421;
				}
			}
			
			if (player.exchangeItemsById("ExchangeKE", this, 9912, 10, item, 1, true))
			{
				filename = "data/html/castlemagician/magician-KE-Exchange.htm";
			}
			else
			{
				filename = "data/html/castlemagician/magician-no-KE.htm";
			}
			showChatWindow(player, filename);
		}
		else if (command.equals("gotoleader"))
		{
			if (player.getClan() != null)
			{
				final L2PcInstance clanLeader = player.getClan().getLeader().getPlayerInstance();
				if (clanLeader == null)
				{
					return;
				}
				
				if (clanLeader.getFirstEffect(L2EffectType.CLAN_GATE) != null)
				{
					if (!validateGateCondition(clanLeader, player))
					{
						return;
					}
					
					player.teleToLocation(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ(), false);
					return;
				}
				showChatWindow(player, "data/html/castlemagician/magician-nogate.htm");
			}
		}
		else if (command.equals("subskills"))
		{
			if (player.isClanLeader())
			{
				final List<L2SkillLearn> skills = SkillTreesParser.getInstance().getAvailableSubPledgeSkills(player.getClan());
				final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.SUBPLEDGE);
				int count = 0;
				
				for (L2SkillLearn s : skills)
				{
					if (SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
					{
						asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
						++count;
					}
				}
				
				if (count == 0)
				{
					player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
				}
				else
				{
					player.sendPacket(asl);
				}
			}
			else
			{
				showChatWindow(player, "data/html/castlemagician/magician-nosquad.htm");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	protected int validateCondition(L2PcInstance player)
	{
		if (player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS))
		{
			return COND_OWNER;
		}
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
	
	private static final boolean validateGateCondition(L2PcInstance clanLeader, L2PcInstance player)
	{
		if (clanLeader.isAlikeDead())
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (clanLeader.isInStoreMode())
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (clanLeader.isRooted() || clanLeader.isInCombat())
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (clanLeader.isInOlympiadMode())
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (clanLeader.isFestivalParticipant())
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (clanLeader.inObserverMode())
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (clanLeader.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (clanLeader.getInstanceId() > 0)
		{
			if (!Config.ALLOW_SUMMON_TO_INSTANCE || InstanceManager.getInstance().getInstance(player.getInstanceId()).isSummonAllowed())
			{
				player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
				return false;
			}
		}
		
		if (player.isIn7sDungeon())
		{
			final int targetCabal = SevenSigns.getInstance().getPlayerCabal(clanLeader.getObjectId());
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
			else
			{
				if (targetCabal == SevenSigns.CABAL_NULL)
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
		}
		
		if (!TvTEvent.onEscapeUse(player.getObjectId()))
		{
			player.sendMessage("You on TvT Event, teleporting disabled.");
			return false;
		}
		
		if (!TvTEvent.onEscapeUse(clanLeader.getObjectId()))
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (!TvTRoundEvent.onEscapeUse(player.getObjectId()))
		{
			player.sendMessage("You on TvTRound Event, teleporting disabled.");
			return false;
		}
		
		if (!TvTRoundEvent.onEscapeUse(clanLeader.getObjectId()))
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		return true;
	}
	
	@Override
	public void showSubUnitSkillList(L2PcInstance player)
	{
		onBypassFeedback(player, "subskills");
	}
}