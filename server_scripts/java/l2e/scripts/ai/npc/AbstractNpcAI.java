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
package l2e.scripts.ai.npc;

import java.util.logging.Logger;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.scripting.scriptengine.impl.L2Script;
import l2e.gameserver.util.Broadcast;

public abstract class AbstractNpcAI extends L2Script
{
	public Logger _log = Logger.getLogger(getClass().getSimpleName());

	public AbstractNpcAI(String name, String descr)
	{
		super(-1, name, descr);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}

	public void registerMobs(int... mobs)
	{
		addAttackId(mobs);
		addKillId(mobs);
		addSpawnId(mobs);
		addSpellFinishedId(mobs);
		addSkillSeeId(mobs);
		addAggroRangeEnterId(mobs);
		addFactionCallId(mobs);
	}
	
	public void registerMobs(int[] mobs, QuestEventType... types)
	{
		for (QuestEventType type : types)
		{
			addEventId(type, mobs);
		}
	}
	
	public void registerMobs(Iterable<Integer> mobs, QuestEventType... types)
	{
		for (int id : mobs)
		{
			for (QuestEventType type : types)
			{
				addEventId(type, id);
			}
		}
	}

	protected void broadcastNpcSay(L2Npc npc, int type, NpcStringId stringId, String... parameters)
	{
		final NpcSay say = new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), stringId);
		if (parameters != null)
		{
			for (String parameter : parameters)
			{
				say.addStringParameter(parameter);
			}
		}
		Broadcast.toKnownPlayers(npc, say);
	}
	
	protected void broadcastNpcSay(L2Npc npc, int type, String text)
	{
		Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), text));
	}
	
	protected void broadcastNpcSay(L2Npc npc, int type, NpcStringId stringId)
	{
		Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), stringId));
	}
	
	protected void broadcastNpcSay(L2Npc npc, int type, String text, int radius)
	{
		Broadcast.toKnownPlayersInRadius(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), text), radius);
	}
	
	protected void broadcastNpcSay(L2Npc npc, int type, NpcStringId stringId, int radius)
	{
		Broadcast.toKnownPlayersInRadius(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), stringId), radius);
	}
	
	protected void broadcastSocialAction(L2Character character, int actionId)
	{
		Broadcast.toSelfAndKnownPlayers(character, new SocialAction(character.getObjectId(), actionId));
	}
	
	protected void broadcastSocialAction(L2Character character, int actionId, int radius)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(character, new SocialAction(character.getObjectId(), actionId), radius);
	}
	
	protected void attackPlayer(L2Attackable npc, L2Playable playable)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, playable);
	}

	public static void main(String[] args)
	{
	}
}