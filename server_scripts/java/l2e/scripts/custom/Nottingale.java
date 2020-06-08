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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.RadarControl;

public final class Nottingale extends AbstractNpcAI
{
	private static final int NOTTINGALE = 32627;
	private static final Map<Integer, RadarControl> RADARS = new HashMap<>();

	static
	{
		RADARS.put(2, new RadarControl(0, -184545, 243120, 1581, 2));
		RADARS.put(5, new RadarControl(0, -192361, 254528, 3598, 1));
		RADARS.put(6, new RadarControl(0, -174600, 219711, 4424, 1));
		RADARS.put(7, new RadarControl(0, -181989, 208968, 4424, 1));
		RADARS.put(8, new RadarControl(0, -252898, 235845, 5343, 1));
		RADARS.put(9, new RadarControl(0, -212819, 209813, 4288, 1));
		RADARS.put(10, new RadarControl(0, -246899, 251918, 4352, 1));
	}
	
	private Nottingale(String name, String descr)
	{
		super(name, descr);

		addStartNpc(NOTTINGALE);
		addTalkId(NOTTINGALE);
		addFirstTalkId(NOTTINGALE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "32627-02.htm":
			case "32627-03.htm":
			case "32627-04.htm":
			{
				if (player.getClan() != null)
				{
					if (((player.getClanPrivileges() & L2Clan.CP_CL_SUMMON_AIRSHIP) == L2Clan.CP_CL_SUMMON_AIRSHIP) && AirShipManager.getInstance().hasAirShipLicense(player.getId()) && !AirShipManager.getInstance().hasAirShip(player.getId()))
					{
						htmltext = event;
					}
					else
					{
						final QuestState st = player.getQuestState("_10273_GoodDayToFly");
						if ((st != null) && st.isCompleted())
						{
							htmltext = event;
						}
						else
						{
							player.sendPacket(RADARS.get(2));
							htmltext = "32627-01.htm";
						}
					}
				}
				else
				{
					final QuestState st = player.getQuestState("_10273_GoodDayToFly");
					if ((st != null) && st.isCompleted())
					{
						htmltext = event;
					}
					else
					{
						player.sendPacket(RADARS.get(2));
						htmltext = "32627-01.htm";
					}
				}
				break;
			}
			case "32627-05.htm":
			case "32627-06.htm":
			case "32627-07.htm":
			case "32627-08.htm":
			case "32627-09.htm":
			case "32627-10.htm":
			{
				player.sendPacket(RADARS.get(Integer.valueOf(event.substring(6, 8))));
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Nottingale(Nottingale.class.getSimpleName(), "custom");
	}
}