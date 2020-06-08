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
package l2e.gameserver.handler.admincommandhandlers;

import l2e.gameserver.MonsterRace;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.MonRaceInfo;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class AdminMonsterRace implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mons"
	};
	
	protected static int state = -1;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equalsIgnoreCase("admin_mons"))
		{
			handleSendPacket(activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleSendPacket(L2PcInstance activeChar)
	{
		int[][] codes =
		{
			{ -1, 0 },
			{ 0, 15322 },
			{ 13765, -1 },
			{ -1, 0 }
		};
		MonsterRace race = MonsterRace.getInstance();
		
		if (state == -1)
		{
			state++;
			race.newRace();
			race.newSpeeds();
			MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
		}
		else if (state == 0)
		{
			state++;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_RACE_START);
			sm.addNumber(0);
			activeChar.sendPacket(sm);
			PlaySound SRace = new PlaySound(1, "S_Race", 0, 0, 0, 0, 0);
			activeChar.sendPacket(SRace);
			activeChar.broadcastPacket(SRace);
			PlaySound SRace2 = new PlaySound(0, "ItemSound2.race_start", 1, 121209259, 12125, 182487, -3559);
			activeChar.sendPacket(SRace2);
			activeChar.broadcastPacket(SRace2);
			MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			
			ThreadPoolManager.getInstance().scheduleGeneral(new RunRace(codes, activeChar), 5000);
		}
		
	}
	
	class RunRace implements Runnable
	{
		
		private int[][] codes;
		private L2PcInstance activeChar;
		
		public RunRace(int[][] pCodes, L2PcInstance pActiveChar)
		{
			codes = pCodes;
			activeChar = pActiveChar;
		}
		
		@Override
		public void run()
		{	
			MonRaceInfo spk = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			ThreadPoolManager.getInstance().scheduleGeneral(new RunEnd(activeChar), 30000);
		}
	}
	
	private static class RunEnd implements Runnable
	{
		private L2PcInstance activeChar;
		
		public RunEnd(L2PcInstance pActiveChar)
		{
			activeChar = pActiveChar;
		}
		
		@Override
		public void run()
		{
			DeleteObject obj = null;
			for (int i = 0; i < 8; i++)
			{
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				activeChar.sendPacket(obj);
				activeChar.broadcastPacket(obj);
			}
			state = -1;
		}
	}
}