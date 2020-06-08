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
package l2e.scripts.clanhallsiege;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Util;

/**
 * Rework by LordWinter 31.05.2013 Based on L2J Eternity-World
 */
public final class FortressOfResistanceSiege extends ClanHallSiegeEngine
{
	private static final String qn = "FortressOfResistanceSiege";

	private final int MESSENGER = 35382;
	private final int BLOODY_LORD_NURKA = 35375;

	private final Location[] NURKA_COORDS =
	{
	                new Location(45109, 112124, -1900),
	                new Location(47653, 110816, -2110),
	                new Location(47247, 109396, -2000)
	};

	private L2Spawn _nurka;
	private final Map<Integer, Long> _damageToNurka = new HashMap<>();

	public FortressOfResistanceSiege(int questId, String name, String descr, final int hallId)
	{
		super(questId, name, descr, hallId);

		addFirstTalkId(MESSENGER);
		addKillId(BLOODY_LORD_NURKA);
		addAttackId(BLOODY_LORD_NURKA);

		try
		{
			_nurka = new L2Spawn(NpcTable.getInstance().getTemplate(BLOODY_LORD_NURKA));
			_nurka.setAmount(1);
			_nurka.setRespawnDelay(10800);
			_nurka.setLocation(NURKA_COORDS[0]);
		}
		catch (Exception e)
		{
			_log.warning(getName() + ": Couldnt set the Bloody Lord Nurka spawn");
			e.printStackTrace();
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == MESSENGER)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile(player.getLang(), "data/html/default/35382.htm");
			html.replace("%nextSiege%", Util.formatDate(_hall.getSiegeDate().getTime(), "yyyy-MM-dd HH:mm:ss"));
			player.sendPacket(html);
			return null;
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if (!_hall.isInSiege())
		{
			return null;
		}

		int clanId = player.getId();
		if (clanId > 0)
		{
			long clanDmg = (_damageToNurka.containsKey(clanId)) ? _damageToNurka.get(clanId) + damage : damage;
			_damageToNurka.put(clanId, clanDmg);
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (!_hall.isInSiege())
		{
			return null;
		}

		_missionAccomplished = true;

		synchronized (this)
		{
			npc.getSpawn().stopRespawn();
			npc.deleteMe();
			cancelSiegeTask();
			endSiege();
		}
		return null;
	}

	@Override
	public L2Clan getWinner()
	{
		int winnerId = 0;
		long counter = 0;
		for (Entry<Integer, Long> e : _damageToNurka.entrySet())
		{
			long dam = e.getValue();
			if (dam > counter)
			{
				winnerId = e.getKey();
				counter = dam;
			}
		}
		return ClanHolder.getInstance().getClan(winnerId);
	}

	@Override
	public void onSiegeStarts()
	{
		_nurka.init();
	}

	public static void main(String[] args)
	{
		new FortressOfResistanceSiege(-1, qn, "clanhallsiege", FORTRESS_RESSISTANCE);
	}
}
