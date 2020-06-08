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
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.scripts.events.LastHero;

public class TeleToLeader implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"teletocl"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("teletocl"))
		{
			
			if (activeChar.getClan() == null)
			{
				return false;
			}
			
			L2PcInstance leader;
			leader = (L2PcInstance) L2World.getInstance().findObject(activeChar.getClan().getLeaderId());
			
			if (leader == null)
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_OFFLINE", activeChar.getLang())).toString());
				return false;
			}
			else if (leader.isJailed())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_ISJAILED", activeChar.getLang())).toString());
				return false;
			}
			else if (leader.isInOlympiadMode())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_INOLYPIAD", activeChar.getLang())).toString());
				return false;
			}
			else if (leader.isInDuel())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_INDUEL", activeChar.getLang())).toString());
				return false;
			}
			else if (leader.isFestivalParticipant())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_INFESTIVAL", activeChar.getLang())).toString());
				return false;
			}
			else if (leader.isInParty() && leader.getParty().isInDimensionalRift())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_INRIFT", activeChar.getLang())).toString());
				return false;
			}
			else if (leader.inObserverMode())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_REGTOOLY", activeChar.getLang())).toString());
			}
			else if ((leader.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(leader.getClan()) != null) && CastleManager.getInstance().getCastleByOwner(leader.getClan()).getSiege().getIsInProgress())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_INSIEGE", activeChar.getLang())).toString());
				return false;
			}
			else if (leader.isFightingInEvent() || leader.isFightingInTW() || (TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(leader.getObjectId())) || (TvTRoundEvent.isStarted() && TvTRoundEvent.isPlayerParticipant(leader.getObjectId())) || ((LastHero.CurrentState == LastHero.EventState.Battle) && LastHero.Players.contains(leader.getName())) || leader.getIsInMonsterRush())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.LEADER_INEVENT", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar.isJailed())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_INJAILED", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_INOLIMPIAD", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar.isInDuel())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_INDUEL", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar.inObserverMode())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_REGTOOLY", activeChar.getLang())).toString());
			}
			else if ((activeChar.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null) && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().getIsInProgress())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_INSIEGE", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar.isFestivalParticipant())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_INFESTIVAL", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_INRIGT", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar.isFightingInEvent() || activeChar.isFightingInTW() || (TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(activeChar.getObjectId())) || (TvTRoundEvent.isStarted() && TvTRoundEvent.isPlayerParticipant(activeChar.getObjectId())) || ((LastHero.CurrentState == LastHero.EventState.Battle) && LastHero.Players.contains(activeChar.getName())) || activeChar.getIsInMonsterRush())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_INEVENT", activeChar.getLang())).toString());
				return false;
			}
			else if (activeChar == leader())
			{
				activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_LEADER", activeChar.getLang())).toString());
				return false;
			}
			if (activeChar.getInventory().getItemByItemId(Config.TELETO_LEADER_ID) == null)
			{
				CustomMessage msg = new CustomMessage("TeleToLeader.NOT_ITEMS", activeChar.getLang());
				msg.add(ItemHolder.getInstance().getTemplate(Config.TELETO_LEADER_ID).getName());
				msg.add(Config.TELETO_LEADER_COUNT);
				activeChar.sendMessage(msg.toString());
				return false;
			}
			int leaderx;
			int leadery;
			int leaderz;
			
			leaderx = leader.getX();
			leadery = leader.getY();
			leaderz = leader.getZ();
			
			activeChar.teleToLocation(leaderx, leadery, leaderz);
			activeChar.sendMessage((new CustomMessage("TeleToLeader.YOU_TELETOCL", activeChar.getLang())).toString());
			activeChar.getInventory().destroyItemByItemId("RessSystem", Config.TELETO_LEADER_ID, Config.TELETO_LEADER_COUNT, activeChar, activeChar.getTarget());
			CustomMessage msg = new CustomMessage("TeleToLeader.TAKE_ITEMS", activeChar.getLang());
			msg.add(ItemHolder.getInstance().getTemplate(Config.TELETO_LEADER_ID).getName());
			msg.add(Config.TELETO_LEADER_COUNT);
			activeChar.sendMessage(msg.toString());
		}
		return true;
	}
	
	public L2PcInstance leader()
	{
		return null;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}