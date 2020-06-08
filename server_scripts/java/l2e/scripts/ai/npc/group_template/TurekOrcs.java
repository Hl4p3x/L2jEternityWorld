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
package l2e.scripts.ai.npc.group_template;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.npc.AbstractNpcAI;

public final class TurekOrcs extends AbstractNpcAI
{
	protected int x;
	protected int y;
	
	private static final int[] MOBS =
	{
		20494,
		20495,
		20497,
		20498,
		20499,
		20500
	};
	
	private TurekOrcs()
	{
		super(TurekOrcs.class.getSimpleName(), "ai");
		registerMobs(MOBS, QuestEventType.ON_ATTACK, QuestEventType.ON_EVENT_RECEIVED, QuestEventType.ON_MOVE_FINISHED);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("checkState") && !npc.isDead() && (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK))
		{
			if ((npc.getCurrentHp() > (npc.getMaxHp() * 0.7)) && (npc.getVariables().getInteger("state") == 2))
			{
				npc.getVariables().set("state", 3);
				((L2Attackable) npc).returnHome();
			}
			else
			{
				npc.getVariables().remove("state");
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (!npc.getVariables().hasVariable("isHit"))
		{
			npc.getVariables().set("isHit", 1);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.5)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.3)) && (attacker.getCurrentHp() > (attacker.getMaxHp() * 0.25)) && (npc.getVariables().getInteger("state") == 0) && (getRandom(100) < 10))
		{
			x = (npc.getX() + getRandom(-400, 400));
			y = (npc.getY() + getRandom(-400, 400));
			broadcastNpcSay(npc, 0, NpcStringId.getNpcStringId(getRandom(1000007, 1000027)));	
			npc.disableCoreAI(true);
			npc.setIsRunning(true);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, npc.getZ(), npc.getHeading()));
			npc.getVariables().set("state", 1);
			npc.getVariables().set("attacker", attacker.getObjectId());
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		if (eventName.equals("WARNING") && !receiver.isDead() && (receiver.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK) && (reference != null) && (reference.getActingPlayer() != null) && !reference.getActingPlayer().isDead())
		{
			receiver.getVariables().set("state", 3);
			receiver.setIsRunning(true);
			((L2Attackable) receiver).addDamageHate(reference.getActingPlayer(), 0, 99999);
			receiver.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, reference.getActingPlayer());
		}	
		return null;
	}
	
	@Override
	public void onMoveFinished(L2Npc npc)
	{
		if (npc.getVariables().getInteger("state") == 1)
		{
			if ((npc.getX() == x) && (npc.getY() == y))
			{
				npc.disableCoreAI(false);
				startQuestTimer("checkState", 15000, npc, null);
				npc.getVariables().set("state", 2);
				npc.broadcastEvent("WARNING", 400, L2World.getInstance().getPlayer(npc.getVariables().getInteger("attacker")));
			}
			else
			{
				x = (npc.getX() + getRandom(-400, 400));
				y = (npc.getY() + getRandom(-400, 400));
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, npc.getZ(), npc.getHeading()));
			}
		}
		else if ((npc.getVariables().getInteger("state") == 3) && npc.staysInSpawnLoc())
		{
			npc.disableCoreAI(false);
			npc.getVariables().remove("state");
		}
	}
	
	public static void main(String[] args)
	{
		new TurekOrcs();
	}
}