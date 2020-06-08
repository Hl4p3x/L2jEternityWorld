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
package l2e.gameserver.model.entity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class VoteRewardTopzone
{
	private static String topzoneUrl = Config.TOPZONE_SERVER_LINK;
	private static String page1Url = Config.TOPZONE_FIRST_PAGE_LINK;
	private static int voteRewardVotesDifference = Config.TOPZONE_VOTES_DIFFERENCE;
	private static int firstPageRankNeeded = Config.TOPZONE_FIRST_PAGE_RANK_NEEDED;
	private static int checkTime = 60 * 1000 * Config.TOPZONE_REWARD_CHECK_TIME;
	
	private static int lastVotes = 0;
	private static FastMap<String, Integer> playerIps = new FastMap<>();
	
	public static void updateConfigurations()
	{
		topzoneUrl = Config.TOPZONE_SERVER_LINK;
		page1Url = Config.TOPZONE_FIRST_PAGE_LINK;
		voteRewardVotesDifference = Config.TOPZONE_VOTES_DIFFERENCE;
		firstPageRankNeeded = Config.TOPZONE_FIRST_PAGE_RANK_NEEDED;
		checkTime = 60 * 1000 * Config.TOPZONE_REWARD_CHECK_TIME;
	}
	
	public static void getInstance()
	{
		System.out.println("Vote reward system initialized.");
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (Config.ALLOW_TOPZONE_VOTE_REWARD)
				{
					reward();
				}
				else
				{
					return;
				}
			}
		}, checkTime / 2, checkTime);
	}
	
	protected static void reward()
	{
		int firstPageVotes = getFirstPageRankVotes();
		int currentVotes = getVotes();
		
		if ((firstPageVotes == -1) || (currentVotes == -1))
		{
			if (firstPageVotes == -1)
			{
				System.out.println("There was a problem on getting votes from server with rank " + firstPageRankNeeded + ".");
			}
			if (currentVotes == -1)
			{
				System.out.println("There was a problem on getting server votes.");
			}
			return;
		}
		
		if (lastVotes == 0)
		{
			lastVotes = currentVotes;
			Announcements.getInstance().gameAnnounceToAll("Current vote count is " + currentVotes + ".");
			if (Config.ALLOW_TOPZONE_GAME_SERVER_REPORT)
			{
				System.out.println("Server votes on topzone: " + currentVotes);
				System.out.println("Votes needed for reward: " + ((lastVotes + voteRewardVotesDifference) - currentVotes));
			}
			if ((firstPageVotes - lastVotes) <= 0)
			{
				if (Config.ALLOW_TOPZONE_GAME_SERVER_REPORT)
				{
					System.out.println("Server is on the first page of topzone.");
				}
			}
			else
			{
				if (Config.ALLOW_TOPZONE_GAME_SERVER_REPORT)
				{
					System.out.println("Server votes needed for first page: " + (firstPageVotes - lastVotes));
				}
			}
			return;
		}
		
		if (currentVotes >= (lastVotes + voteRewardVotesDifference))
		{
			L2PcInstance[] pls = L2World.getInstance().getAllPlayersArray();
			if ((firstPageVotes - currentVotes) <= 0)
			{
				if (Config.ALLOW_TOPZONE_GAME_SERVER_REPORT)
				{
					System.out.println("Server votes on topzone: " + currentVotes);
					System.out.println("Server is on the first page of topzone.");
					System.out.println("Votes needed for next reward: " + ((currentVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().gameAnnounceToAll("Current vote count is " + currentVotes + ".");
				for (L2PcInstance p : pls)
				{
					boolean canReward = false;
					String pIp = p.getClient().getConnection().getInetAddress().getHostAddress();
					if (playerIps.containsKey(pIp))
					{
						int count = playerIps.get(pIp);
						if (count < Config.TOPZONE_DUALBOXES_ALLOWED)
						{
							playerIps.remove(pIp);
							playerIps.put(pIp, count + 1);
							canReward = true;
						}
					}
					else
					{
						canReward = true;
						playerIps.put(pIp, 1);
					}
					if ((canReward) && !p.getClient().isDetached())
					{
						for (int i : Config.TOPZONE_BIG_REWARD.keySet())
						{
							p.addItem("Vote reward.", i, Config.TOPZONE_BIG_REWARD.get(i), p, true);
						}
						if ((p.getInventory().getItemByItemId(65499) == null) && (p.getWarehouse().getItemByItemId(65499) == null))
						{
							p.addItem("Vote reward.", 65499, 1, p, true);
						}
					}
					else
					{
						p.sendMessage("Already " + Config.TOPZONE_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();
			}
			else
			{
				if (Config.ALLOW_TOPZONE_GAME_SERVER_REPORT)
				{
					System.out.println("Server votes on topzone: " + currentVotes);
					System.out.println("Server votes needed for first page: " + (firstPageVotes - lastVotes));
					System.out.println("Votes needed for next reward: " + ((currentVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().gameAnnounceToAll("Current vote count is " + currentVotes + ".");
				for (L2PcInstance p : pls)
				{
					boolean canReward = false;
					String pIp = p.getClient().getConnection().getInetAddress().getHostAddress();
					if (playerIps.containsKey(pIp))
					{
						int count = playerIps.get(pIp);
						if (count < Config.TOPZONE_DUALBOXES_ALLOWED)
						{
							playerIps.remove(pIp);
							playerIps.put(pIp, count + 1);
							canReward = true;
						}
					}
					else
					{
						canReward = true;
						playerIps.put(pIp, 1);
					}
					if (canReward)
					{
						for (int i : Config.TOPZONE_SMALL_REWARD.keySet())
						{
							p.addItem("Vote reward.", i, Config.TOPZONE_SMALL_REWARD.get(i), p, true);
						}
						if ((p.getInventory().getItemByItemId(65499) == null) && (p.getWarehouse().getItemByItemId(65499) == null))
						{
							p.addItem("Vote reward.", 65499, 1, p, true);
						}
					}
					else
					{
						p.sendMessage("Already " + Config.TOPZONE_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();
			}
			lastVotes = currentVotes;
		}
		else
		{
			if ((firstPageVotes - currentVotes) <= 0)
			{
				if (Config.ALLOW_TOPZONE_GAME_SERVER_REPORT)
				{
					System.out.println("Server votes on topzone: " + currentVotes);
					System.out.println("Server is on the first page of topzone.");
					System.out.println("Votes needed for next reward: " + ((lastVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().gameAnnounceToAll("Current vote count is " + currentVotes + ".");
			}
			else
			{
				if (Config.ALLOW_TOPZONE_GAME_SERVER_REPORT)
				{
					System.out.println("Server votes on topzone: " + currentVotes);
					System.out.println("Server votes needed for first page: " + (firstPageVotes - lastVotes));
					System.out.println("Votes needed for next reward: " + ((lastVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().gameAnnounceToAll("Current vote count is " + currentVotes + ".");
			}
		}
	}
	
	private static int getFirstPageRankVotes()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		try
		{
			URLConnection con = new URL(page1Url).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			isr = new InputStreamReader(con.getInputStream());
			br = new BufferedReader(isr);
			
			String line;
			int i = 0;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("<td><div align=\"center\">" + firstPageRankNeeded + "</div></td>"))
				{
					i++;
				}
				if (line.contains("<td><div align=\"center\">") && (i == 1))
				{
					i++;
				}
				if (line.contains("<td><div align=\"center\">") && (i == 2))
				{
					i = 0;
					int votes = Integer.valueOf(line.split(">")[2].replace("</div", ""));
					return votes;
				}
			}
			br.close();
			isr.close();
		}
		catch (Exception e)
		{
			System.out.println(e);
			System.out.println("Error while getting server vote count.");
		}
		return -1;
	}
	
	private static int getVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(topzoneUrl);
			con = url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("Votes"))
				{
					String votesLine = inputLine;
					votes = Integer.valueOf(votesLine.split(">")[3].replace("</div", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return votes;
	}
}