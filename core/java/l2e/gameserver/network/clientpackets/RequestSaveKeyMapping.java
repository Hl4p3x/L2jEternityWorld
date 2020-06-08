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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.Config;
import l2e.gameserver.data.xml.UIParser;
import l2e.gameserver.model.ActionKey;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.L2GameClient.GameClientState;

public class RequestSaveKeyMapping extends L2GameClientPacket
{
	private int _tabNum;
	private final Map<Integer, List<ActionKey>> _keyMap = new HashMap<>();
	private final Map<Integer, List<Integer>> _catMap = new HashMap<>();
	
	@Override
	protected void readImpl()
	{
		int category = 0;
		
		readD();
		readD();
		_tabNum = readD();
		for (int i = 0; i < _tabNum; i++)
		{
			int cmd1Size = readC();
			for (int j = 0; j < cmd1Size; j++)
			{
				UIParser.addCategory(_catMap, category, readC());
			}
			category++;
			
			int cmd2Size = readC();
			for (int j = 0; j < cmd2Size; j++)
			{
				UIParser.addCategory(_catMap, category, readC());
			}
			category++;
			
			int cmdSize = readD();
			for (int j = 0; j < cmdSize; j++)
			{
				int cmd = readD();
				int key = readD();
				int tgKey1 = readD();
				int tgKey2 = readD();
				int show = readD();
				UIParser.addKey(_keyMap, i, new ActionKey(i, cmd, key, tgKey1, tgKey2, show));
			}
		}
		readD();
		readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if (!Config.STORE_UI_SETTINGS || (player == null) || (getClient().getState() != GameClientState.IN_GAME))
		{
			return;
		}
		player.getUISettings().storeAll(_catMap, _keyMap);
	}
}