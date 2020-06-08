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
package l2e.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.instancemanager.tasks.StartMovingTask;
import l2e.gameserver.model.L2NpcWalkerNode;
import l2e.gameserver.model.L2WalkRoute;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.WalkInfo;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.tasks.npc.walker.ArrivedTask;
import l2e.gameserver.model.holders.NpcRoutesHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.util.Broadcast;

public final class WalkingManager extends DocumentParser
{
	public static final byte REPEAT_GO_BACK = 0;
	public static final byte REPEAT_GO_FIRST = 1;
	public static final byte REPEAT_TELE_FIRST = 2;
	public static final byte REPEAT_RANDOM = 3;
	
	private final Map<String, L2WalkRoute> _routes = new HashMap<>();
	private final Map<Integer, WalkInfo> _activeRoutes = new HashMap<>();
	private final Map<Integer, NpcRoutesHolder> _routesToAttach = new HashMap<>();
	
	protected WalkingManager()
	{
		load();
	}
	
	@Override
	public final void load()
	{
		parseDatapackFile("data/Routes.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _routes.size() + " walking routes.");
	}
	
	@Override
	protected void parseDocument()
	{
		Node n = getCurrentDocument().getFirstChild();
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if (d.getNodeName().equals("route"))
			{
				final String routeName = parseString(d.getAttributes(), "name");
				boolean repeat = parseBoolean(d.getAttributes(), "repeat");
				String repeatStyle = d.getAttributes().getNamedItem("repeatStyle").getNodeValue();
				byte repeatType;
				if (repeatStyle.equalsIgnoreCase("back"))
				{
					repeatType = REPEAT_GO_BACK;
				}
				else if (repeatStyle.equalsIgnoreCase("cycle"))
				{
					repeatType = REPEAT_GO_FIRST;
				}
				else if (repeatStyle.equalsIgnoreCase("conveyor"))
				{
					repeatType = REPEAT_TELE_FIRST;
				}
				else if (repeatStyle.equalsIgnoreCase("random"))
				{
					repeatType = REPEAT_RANDOM;
				}
				else
				{
					repeatType = -1;
				}
				
				final List<L2NpcWalkerNode> list = new ArrayList<>();
				for (Node r = d.getFirstChild(); r != null; r = r.getNextSibling())
				{
					if (r.getNodeName().equals("point"))
					{
						NamedNodeMap attrs = r.getAttributes();
						int x = parseInt(attrs, "X");
						int y = parseInt(attrs, "Y");
						int z = parseInt(attrs, "Z");
						int delay = parseInt(attrs, "delay");
						
						String chatString = null;
						NpcStringId npcString = null;
						Node node = attrs.getNamedItem("string");
						if (node != null)
						{
							chatString = node.getNodeValue();
						}
						else
						{
							node = attrs.getNamedItem("npcString");
							if (node != null)
							{
								npcString = NpcStringId.getNpcStringId(node.getNodeValue());
								if (npcString == null)
								{
									_log.warning(getClass().getSimpleName() + ": Unknown npcstring '" + node.getNodeValue() + ".");
									continue;
								}
							}
							else
							{
								node = attrs.getNamedItem("npcStringId");
								if (node != null)
								{
									npcString = NpcStringId.getNpcStringId(Integer.parseInt(node.getNodeValue()));
									if (npcString == null)
									{
										_log.warning(getClass().getSimpleName() + ": Unknown npcstring '" + node.getNodeValue() + ".");
										continue;
									}
								}
							}
						}
						list.add(new L2NpcWalkerNode(0, npcString, chatString, x, y, z, delay, parseBoolean(attrs, "run")));
					}
					else if (r.getNodeName().equals("target"))
					{
						NamedNodeMap attrs = r.getAttributes();
						try
						{
							int npcId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int x = 0, y = 0, z = 0;
							
							x = Integer.parseInt(attrs.getNamedItem("spawnX").getNodeValue());
							y = Integer.parseInt(attrs.getNamedItem("spawnY").getNodeValue());
							z = Integer.parseInt(attrs.getNamedItem("spawnZ").getNodeValue());
							
							NpcRoutesHolder holder = _routesToAttach.containsKey(npcId) ? _routesToAttach.get(npcId) : new NpcRoutesHolder();
							holder.addRoute(routeName, new Location(x, y, z));
							_routesToAttach.put(npcId, holder);
						}
						catch (Exception e)
						{
							_log.warning("Walking Manager: Error in target definition for route : " + routeName);
						}
					}
				}
				_routes.put(routeName, new L2WalkRoute(routeName, list, repeat, false, repeatType));
			}
		}
	}
	
	public boolean isOnWalk(L2Npc npc)
	{
		L2MonsterInstance monster = null;
		
		if (npc.isMonster())
		{
			if (((L2MonsterInstance) npc).getLeader() == null)
			{
				monster = (L2MonsterInstance) npc;
			}
			else
			{
				monster = ((L2MonsterInstance) npc).getLeader();
			}
		}
		
		if (((monster != null) && !isRegistered(monster)) || !isRegistered(npc))
		{
			return false;
		}
		
		final WalkInfo walk = monster != null ? _activeRoutes.get(monster.getObjectId()) : _activeRoutes.get(npc.getObjectId());
		
		if (walk.isStoppedByAttack() || walk.isSuspended())
		{
			return false;
		}
		return true;
	}
	
	public L2WalkRoute getRoute(String route)
	{
		return _routes.get(route);
	}
	
	public boolean isRegistered(L2Npc npc)
	{
		return _activeRoutes.containsKey(npc.getObjectId());
	}
	
	public String getRouteName(L2Npc npc)
	{
		return _activeRoutes.containsKey(npc.getObjectId()) ? _activeRoutes.get(npc.getObjectId()).getRoute().getName() : "";
	}
	
	public void startMoving(final L2Npc npc, final String routeName)
	{
		if (_routes.containsKey(routeName) && (npc != null) && !npc.isDead())
		{
			if (!_activeRoutes.containsKey(npc.getObjectId()))
			{
				if ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) || (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
				{
					WalkInfo walk = new WalkInfo(routeName);
					
					if (npc.isDebug())
					{
						walk.setLastAction(System.currentTimeMillis());
					}
					
					L2NpcWalkerNode node = walk.getCurrentNode();
					
					if ((npc.getX() == node.getMoveX()) && (npc.getY() == node.getMoveY()))
					{
						walk.calculateNextNode(npc);
						node = walk.getCurrentNode();
					}
					
					if (!npc.isInsideRadius(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 3000, true, false))
					{
						return;
					}
					
					npc.setIsRunning(node.getRunning());
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 0));
					walk.setWalkCheckTask(ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new StartMovingTask(npc, routeName), 60000, 60000));
					npc.getKnownList().startTrackingTask();
					_activeRoutes.put(npc.getObjectId(), walk);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new StartMovingTask(npc, routeName), 60000);
				}
			}
			else
			{
				if (_activeRoutes.containsKey(npc.getObjectId()) && (npc.isEkimusFood()) && ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) || (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)))
				{
					WalkInfo walk = _activeRoutes.get(npc.getObjectId());
					
					if (walk == null)
					{
						return;
					}
					
					if (walk.isBlocked() || walk.isSuspended())
					{
						if (npc.isEkimusFood())
						{
							resumeMoving(npc);
						}
						return;
					}
					
					walk.setBlocked(true);
					L2NpcWalkerNode node = walk.getCurrentNode();
					npc.setIsRunning(node.getRunning());
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 0));
					walk.setBlocked(false);
					walk.setStoppedByAttack(false);
				}
				else if (_activeRoutes.containsKey(npc.getObjectId()) && ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) || (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)))
				{
					WalkInfo walk = _activeRoutes.get(npc.getObjectId());
					
					if (walk == null)
					{
						return;
					}
					
					if (walk.isBlocked() || walk.isSuspended())
					{
						return;
					}
					
					walk.setBlocked(true);
					L2NpcWalkerNode node = walk.getCurrentNode();
					npc.setIsRunning(node.getRunning());
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 0));
					walk.setBlocked(false);
					walk.setStoppedByAttack(false);
				}
			}
		}
	}
	
	public synchronized void cancelMoving(L2Npc npc)
	{
		if (_activeRoutes.containsKey(npc.getObjectId()))
		{
			final WalkInfo walk = _activeRoutes.remove(npc.getObjectId());
			walk.getWalkCheckTask().cancel(true);
			npc.getKnownList().stopTrackingTask();
		}
	}
	
	public void resumeMoving(final L2Npc npc)
	{
		if (!_activeRoutes.containsKey(npc.getObjectId()))
		{
			return;
		}
		
		WalkInfo walk = _activeRoutes.get(npc.getObjectId());
		walk.setSuspended(false);
		walk.setStoppedByAttack(false);
		startMoving(npc, walk.getRoute().getName());
	}
	
	public void stopMoving(L2Npc npc, boolean suspend, boolean stoppedByAttack)
	{
		L2MonsterInstance monster = null;
		
		if (npc.isMonster())
		{
			if (((L2MonsterInstance) npc).getLeader() == null)
			{
				monster = (L2MonsterInstance) npc;
			}
			else
			{
				monster = ((L2MonsterInstance) npc).getLeader();
			}
		}
		
		if (((monster != null) && !isRegistered(monster)) || !isRegistered(npc))
		{
			return;
		}
		
		WalkInfo walk = monster != null ? _activeRoutes.get(monster.getObjectId()) : _activeRoutes.get(npc.getObjectId());
		
		walk.setSuspended(suspend);
		walk.setStoppedByAttack(stoppedByAttack);
		
		if (monster != null)
		{
			monster.stopMove(null);
			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		else
		{
			npc.stopMove(null);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	public void onArrived(final L2Npc npc)
	{
		if (_activeRoutes.containsKey(npc.getObjectId()))
		{
			if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_NODE_ARRIVED) != null)
			{
				for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_NODE_ARRIVED))
				{
					quest.notifyNodeArrived(npc);
				}
			}
			WalkInfo walk = _activeRoutes.get(npc.getObjectId());
			
			if ((walk.getCurrentNodeId() >= 0) && (walk.getCurrentNodeId() < walk.getRoute().getNodesCount()))
			{
				L2NpcWalkerNode node = walk.getRoute().getNodeList().get(walk.getCurrentNodeId());
				if (npc.isInsideRadius(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 10, false, false))
				{
					walk.calculateNextNode(npc);
					int delay = node.getDelay();
					walk.setBlocked(true);
					
					if (node.getNpcString() != null)
					{
						Broadcast.toKnownPlayers(npc, new NpcSay(npc, Say2.NPC_ALL, node.getNpcString()));
					}
					else
					{
						final String text = node.getChatText();
						if ((text != null) && !text.isEmpty())
						{
							Broadcast.toKnownPlayers(npc, new NpcSay(npc, Say2.NPC_ALL, text));
						}
					}
					
					if (npc.isDebug())
					{
						walk.setLastAction(System.currentTimeMillis());
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new ArrivedTask(npc, walk), 100 + (delay * 1000L));
				}
			}
		}
	}
	
	public void onDeath(L2Npc npc)
	{
		cancelMoving(npc);
	}
	
	public void onSpawn(L2Npc npc)
	{
		if (_routesToAttach.containsKey(npc.getId()))
		{
			final String routeName = _routesToAttach.get(npc.getId()).getRouteName(npc);
			if (!routeName.isEmpty())
			{
				startMoving(npc, routeName);
			}
		}
	}
	
	public static final WalkingManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final WalkingManager _instance = new WalkingManager();
	}
}