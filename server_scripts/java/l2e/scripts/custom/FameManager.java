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
package l2e.scripts.custom;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.UserInfo;

public class FameManager extends AbstractNpcAI
{
	private static final int[] FAME_MANAGER =
	{
		36479,
		36480
	};

	private static final int MIN_LVL = 40;
	private static final int DECREASE_COST = 5000;
	private static final int REPUTATION_COST = 1000;
	private static final int MIN_CLAN_LVL = 5;
	private static final int CLASS_LVL = 2;
	
	private FameManager(String name, String descr)
	{
		super(name, descr);

		addStartNpc(FAME_MANAGER);
		addTalkId(FAME_MANAGER);
		addFirstTalkId(FAME_MANAGER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "36479.htm":
			case "36479-02.htm":
			case "36479-07.htm":
			case "36480.htm":
			case "36480-02.htm":
			case "36480-07.htm":
			{
				htmltext = event;
				break;
			}
			case "decreasePk":
			{
				if (player.getPkKills() > 0)
				{
					if ((player.getFame() >= DECREASE_COST) && (player.getLevel() >= MIN_LVL) && (player.getClassId().level() >= CLASS_LVL))
					{
						player.setFame(player.getFame() - DECREASE_COST);
						player.setPkKills(player.getPkKills() - 1);
						player.sendPacket(new UserInfo(player));
						htmltext = npc.getId() + "-06.htm";
					}
					else
					{
						htmltext = npc.getId() + "-01.htm";
					}
				}
				else
				{
					htmltext = npc.getId() + "-05.htm";
				}
				break;
			}
			case "clanRep":
			{
				if ((player.getClan() != null) && (player.getClan().getLevel() >= MIN_CLAN_LVL))
				{
					if ((player.getFame() >= REPUTATION_COST) && (player.getLevel() >= MIN_LVL) && (player.getClassId().level() >= CLASS_LVL))
					{
						player.setFame(player.getFame() - REPUTATION_COST);
						player.getClan().addReputationScore(50, true);
						player.sendPacket(new UserInfo(player));
						player.sendPacket(SystemMessageId.ACQUIRED_50_CLAN_FAME_POINTS);
						htmltext = npc.getId() + "-04.htm";
					}
					else
					{
						htmltext = npc.getId() + "-01.htm";
					}
				}
				else
				{
					htmltext = npc.getId() + "-03.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return ((player.getFame() > 0) && (player.getLevel() >= MIN_LVL) && (player.getClassId().level() >= CLASS_LVL)) ? npc.getId() + ".htm" : npc.getId() + "-01.htm";
	}
	
	public static void main(String[] args)
	{
		new FameManager(FameManager.class.getSimpleName(), "custom");
	}
}