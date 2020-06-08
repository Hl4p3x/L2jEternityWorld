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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;

/**
 * Rework by LordWinter 31.05.2013 Based on L2J Eternity-World
 */
public final class DevastatedCastleSiege extends ClanHallSiegeEngine
{
	private static final String qn = "DevastatedCastleSiege";

	private static final int GUSTAV = 35410;
	private static final int MIKHAIL = 35409;
	private static final int DIETRICH = 35408;
	private static final double GUSTAV_TRIGGER_HP = NpcTable.getInstance().getTemplate(GUSTAV).getBaseHpMax() / 12;

	private static Map<Integer, Integer> _damageToGustav = new HashMap<>();

	public DevastatedCastleSiege(int questId, String name, String descr, int hallId)
	{
		super(questId, name, descr, hallId);
		addKillId(GUSTAV);

		addSpawnId(MIKHAIL);
		addSpawnId(DIETRICH);

		addAttackId(GUSTAV);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == MIKHAIL)
		{
			broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId.GLORY_TO_ADEN_THE_KINGDOM_OF_THE_LION_GLORY_TO_SIR_GUSTAV_OUR_IMMORTAL_LORD);
		}
		else if (npc.getId() == DIETRICH)
		{
			broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId.SOLDIERS_OF_GUSTAV_GO_FORTH_AND_DESTROY_THE_INVADERS);
		}
		return null;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (!_hall.isInSiege())
		{
			return null;
		}

		synchronized (this)
		{
			final L2Clan clan = attacker.getClan();

			if ((clan != null) && checkIsAttacker(clan))
			{
				final int id = clan.getId();
				if (_damageToGustav.containsKey(id))
				{
					int newDamage = _damageToGustav.get(id);
					newDamage += damage;
					_damageToGustav.put(id, newDamage);
				}
				else
				{
					_damageToGustav.put(id, damage);
				}
			}

			if ((npc.getCurrentHp() < GUSTAV_TRIGGER_HP) && (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST))
			{
				broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.THIS_IS_UNBELIEVABLE_HAVE_I_REALLY_BEEN_DEFEATED_I_SHALL_RETURN_AND_TAKE_YOUR_HEAD);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, SkillHolder.getInstance().getInfo(4235, 1), npc);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (!_hall.isInSiege())
		{
			return null;
		}

		_missionAccomplished = true;

		if (npc.getId() == GUSTAV)
		{
			synchronized (this)
			{
				cancelSiegeTask();
				endSiege();
			}
		}

		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public L2Clan getWinner()
	{
		int counter = 0;
		int damagest = 0;
		for (Entry<Integer, Integer> e : _damageToGustav.entrySet())
		{
			final int damage = e.getValue();
			if (damage > counter)
			{
				counter = damage;
				damagest = e.getKey();
			}
		}
		return ClanHolder.getInstance().getClan(damagest);
	}

	public static void main(String[] args)
	{
		new DevastatedCastleSiege(-1, qn, "clanhallsiege", DEVASTATED_CASTLE);
	}
}
