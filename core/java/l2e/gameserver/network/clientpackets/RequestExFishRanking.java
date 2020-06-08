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
package l2e.gameserver.network.clientpackets;

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.instancemanager.games.FishingChampionship;
import l2e.gameserver.instancemanager.leaderboards.FishermanLeaderboard;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestExFishRanking extends L2GameClientPacket
{
	protected static final Logger _log = Logger.getLogger(RequestExFishRanking.class.getName());
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		FishingChampionship.getInstance().showMidResult(getClient().getActiveChar());
		
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.RANK_FISHERMAN_ENABLED)
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(0);
			htm.setHtml(FishermanLeaderboard.getInstance().showHtm(getClient().getActiveChar().getObjectId()));
			activeChar.sendPacket(htm);
		}
		else
		{
			_log.info("C5: RequestExFishRanking");
		}
	}
}