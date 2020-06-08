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
package l2e.gameserver.model.actor.tasks.player;

import l2e.Config;
import l2e.gameserver.instancemanager.VoteRewardManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Created by LordWinter 01.16.2013 Based on L2J Eternity-World
 */
public class VoteRewardTask implements Runnable
{
	L2PcInstance voter = null;
	
	public VoteRewardTask(L2PcInstance player)
	{
		voter = player;
	}
	
	@Override
	public void run()
	{
		if ((voter != null) && (voter.getClient() != null))
		{
			voter.setVoting(false);
			
			if (VoteRewardManager.getInstance().getLastTimeVoted(voter) == 0)
			{
				voter.sendMessage("You have not voted on all links. Vote and try again.");
			}
			else if (VoteRewardManager.getInstance().getLastTimeVoted(voter) > (VoteRewardManager.getInstance().getLastTimeRewarded(voter) + 43200000))
			{
				VoteRewardManager.getInstance().updateLastTimeRewarded(voter);
				
				String ip = voter.getClient().getConnection().getInetAddress().getHostAddress();
				voter.sendMessage("Thanks for your support!");
				
				voter.addAdena("Vote Reward", Config.VOTE_REWARD_ADENA_AMOUNT, null, true);
				voter.sendMessage("Vote has been rewarded to everyone connected from the same network as you!");
				
				for (L2PcInstance sameIp : L2World.getInstance().getAllPlayersArray())
				{
					if ((sameIp != null) && (sameIp.getClient() != null) && (sameIp.getClient().getConnection() != null) && (sameIp.getClient().getConnection().getInetAddress() != null) && (sameIp.getClient().getConnection().getInetAddress().getHostAddress() != null))
					{
						if (sameIp.getClient().getConnection().getInetAddress().getHostAddress().equals(ip))
						{
							sameIp.addItem("Vote Reward", Config.VOTE_REWARD_SECOND_ID, Config.VOTE_REWARD_SECOND_COUNT, sameIp, true);
						}
					}
				}
			}
			else
			{
				voter.sendMessage("You have already been rewarded less than 12 hours ago, or you didn't vote correctly. Try again later.");
			}
		}
	}
}