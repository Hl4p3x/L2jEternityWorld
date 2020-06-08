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
package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.ai.L2AttackableAI;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.knownlist.GuardKnownList;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.util.Rnd;

public class L2GuardInstance extends L2Attackable
{
	private static Logger _log = Logger.getLogger(L2GuardInstance.class.getName());
	
	private static final int RETURN_INTERVAL = 60000;
	
	private Future<?> _returnTask;
	
	public class ReturnTask implements Runnable
	{
		@Override
		public void run()
		{
			if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				returnHome();
			}
		}
	}
	
	public L2GuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		setInstanceType(InstanceType.L2GuardInstance);
	}
	
	@Override
	public final GuardKnownList getKnownList()
	{
		return (GuardKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new GuardKnownList(this));
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}
	
	@Override
	public void returnHome()
	{
		if (!isInsideRadius(getSpawn().getX(), getSpawn().getY(), 150, false))
		{
			clearAggroList();
			
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLocation());
		}
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
		
		if ((_returnTask == null) && !isWalker() && !isRunner() && !isSpecialCamera() && !isEkimusFood())
		{
			_returnTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), RETURN_INTERVAL, RETURN_INTERVAL + Rnd.nextInt(60000));
		}
		
		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if ((region != null) && (!region.isActive()))
		{
			((L2AttackableAI) getAI()).stopAITask();
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "data/html/guard/" + pom + ".htm";
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		if (getObjectId() != player.getTargetId())
		{
			player.setTarget(this);
		}
		else if (interact)
		{
			if (containsTarget(player))
			{
				if (Config.DEBUG)
				{
					_log.fine(player.getObjectId() + ": Attacked guard " + getObjectId());
				}
				
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				if (!canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					broadcastPacket(new SocialAction(getObjectId(), Rnd.nextInt(8)));
					player.setLastFolkNPC(this);

					List<Quest> qlsa = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					List<Quest> qlst = getTemplate().getEventQuests(QuestEventType.ON_FIRST_TALK);
					
					if ((qlsa != null) && !qlsa.isEmpty())
					{
						player.setLastQuestNpcObject(getObjectId());
					}
					
					if ((qlst != null) && (qlst.size() == 1))
					{
						qlst.get(0).notifyFirstTalk(this, player);
					}
					else
					{
						showChatWindow(player, 0);
					}
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}