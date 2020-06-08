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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.scripting.scriptengine.impl;

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.scripting.scriptengine.events.AddToInventoryEvent;
import l2e.gameserver.scripting.scriptengine.events.AttackEvent;
import l2e.gameserver.scripting.scriptengine.events.AugmentEvent;
import l2e.gameserver.scripting.scriptengine.events.ChatEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanCreationEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanJoinEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanLeaderChangeEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanLeaveEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanLevelUpEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanWarEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanWarehouseAddItemEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanWarehouseDeleteItemEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanWarehouseTransferEvent;
import l2e.gameserver.scripting.scriptengine.events.DeathEvent;
import l2e.gameserver.scripting.scriptengine.events.DlgAnswerEvent;
import l2e.gameserver.scripting.scriptengine.events.EquipmentEvent;
import l2e.gameserver.scripting.scriptengine.events.FortSiegeEvent;
import l2e.gameserver.scripting.scriptengine.events.HennaEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemCreateEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemDestroyEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemPickupEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemTransferEvent;
import l2e.gameserver.scripting.scriptengine.events.PlayerEvent;
import l2e.gameserver.scripting.scriptengine.events.PlayerLevelChangeEvent;
import l2e.gameserver.scripting.scriptengine.events.ProfessionChangeEvent;
import l2e.gameserver.scripting.scriptengine.events.RequestBypassToServerEvent;
import l2e.gameserver.scripting.scriptengine.events.SiegeEvent;
import l2e.gameserver.scripting.scriptengine.events.SkillUseEvent;
import l2e.gameserver.scripting.scriptengine.events.TransformEvent;
import l2e.gameserver.scripting.scriptengine.events.TvtKillEvent;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;
import l2e.gameserver.scripting.scriptengine.listeners.character.AttackListener;
import l2e.gameserver.scripting.scriptengine.listeners.character.DeathListener;
import l2e.gameserver.scripting.scriptengine.listeners.character.SkillUseListener;
import l2e.gameserver.scripting.scriptengine.listeners.clan.ClanCreationListener;
import l2e.gameserver.scripting.scriptengine.listeners.clan.ClanMembershipListener;
import l2e.gameserver.scripting.scriptengine.listeners.clan.ClanWarListener;
import l2e.gameserver.scripting.scriptengine.listeners.clan.ClanWarehouseListener;
import l2e.gameserver.scripting.scriptengine.listeners.events.FortSiegeListener;
import l2e.gameserver.scripting.scriptengine.listeners.events.SiegeListener;
import l2e.gameserver.scripting.scriptengine.listeners.events.TvTListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.AugmentListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.DropListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.EquipmentListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.HennaListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.ItemTracker;
import l2e.gameserver.scripting.scriptengine.listeners.player.NewItemListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerDespawnListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerLevelListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerSpawnListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.ProfessionChangeListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.TransformListener;
import l2e.gameserver.scripting.scriptengine.listeners.talk.ChatFilterListener;
import l2e.gameserver.scripting.scriptengine.listeners.talk.ChatListener;
import l2e.gameserver.scripting.scriptengine.listeners.talk.DlgAnswerListener;
import l2e.gameserver.scripting.scriptengine.listeners.talk.RequestBypassToServerListener;

public abstract class L2Script extends Quest
{
	private final List<L2JListener> _listeners = new ArrayList<>();
	
	public L2Script(String name, String descr)
	{
		super(-1, name, descr);
	}
	
	public L2Script(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
	
	@Override
	public boolean unload()
	{
		for (L2JListener listener : _listeners)
		{
			listener.unregister();
		}
		_listeners.clear();
		return super.unload();
	}
	
	private void removeListeners(List<L2JListener> removeList)
	{
		for (L2JListener listener : removeList)
		{
			listener.unregister();
			_listeners.remove(listener);
		}
	}

	public void addDeathNotify(final L2Character character)
	{
		DeathListener listener = new DeathListener(character)
		{
			@Override
			public boolean onDeath(L2Character attacker, L2Character target)
			{
				final DeathEvent event = new DeathEvent();
				event.setKiller(attacker);
				event.setVictim(target);
				return L2Script.this.onDeath(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeDeathNotify(L2Character character)
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof DeathListener)
			{
				if (((DeathListener) listener).getCharacter() == character)
				{
					removeList.add(listener);
				}
			}
		}
		removeListeners(removeList);
	}
	
	public void addLoginLogoutNotify()
	{
		PlayerSpawnListener spawn = new PlayerSpawnListener()
		{
			@Override
			public void onPlayerLogin(L2PcInstance player)
			{
				L2Script.this.onPlayerLogin(player);
			}
		};
		PlayerDespawnListener despawn = new PlayerDespawnListener()
		{
			@Override
			public void onPlayerLogout(L2PcInstance player)
			{
				L2Script.this.onPlayerLogout(player);
			}
		};
		_listeners.add(spawn);
		_listeners.add(despawn);
	}
	
	public void removeLoginLogoutNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if ((listener instanceof PlayerSpawnListener) || (listener instanceof PlayerDespawnListener))
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addAttackNotify(final L2Character character)
	{
		AttackListener listener = new AttackListener(character)
		{
			@Override
			public boolean onAttack(L2Character attacker, L2Character target)
			{
				final AttackEvent event = new AttackEvent();
				event.setAttacker(attacker);
				event.setTarget(target);
				return L2Script.this.onAttack(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeAttackNotify(L2Character character)
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if ((listener instanceof AttackListener) && (((AttackListener) listener).getCharacter() == character))
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addSkillUseNotify(L2Character character)
	{
		SkillUseListener listener = new SkillUseListener(character)
		{
			@Override
			public boolean onSkillUse(L2Character caster, L2Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
			{
				final SkillUseEvent event = new SkillUseEvent();
				event.setCaster(caster);
				event.setSkill(skill);
				event.setTarget(target);
				event.setTargets(targets);
				return L2Script.this.onSkillUse(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeSkillUseNotify(L2Character character)
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if ((listener instanceof SkillUseListener) && (((SkillUseListener) listener).getCharacter() == character))
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addClanCreationLevelUpNotify()
	{
		ClanCreationListener listener = new ClanCreationListener()
		{
			@Override
			public void onClanCreate(ClanCreationEvent event)
			{
				onClanCreated(event);
			}
			
			@Override
			public boolean onClanLevelUp(ClanLevelUpEvent event)
			{
				return onClanLeveledUp(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeClanCreationLevelUpNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof ClanCreationListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addClanJoinLeaveNotify()
	{
		ClanMembershipListener listener = new ClanMembershipListener()
		{
			@Override
			public boolean onJoin(ClanJoinEvent event)
			{
				return onClanJoin(event);
			}
			
			@Override
			public boolean onLeaderChange(ClanLeaderChangeEvent event)
			{
				return onClanLeaderChange(event);
			}
			
			@Override
			public boolean onLeave(ClanLeaveEvent event)
			{
				return onClanLeave(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeClanJoinLeaveNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof ClanMembershipListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addClanWarehouseNotify(L2Clan clan)
	{
		if (clan != null)
		{
			ClanWarehouseListener listener = new ClanWarehouseListener(clan)
			{
				@Override
				public boolean onAddItem(ClanWarehouseAddItemEvent event)
				{
					return onClanWarehouseAddItem(event);
				}
				
				@Override
				public boolean onDeleteItem(ClanWarehouseDeleteItemEvent event)
				{
					return onClanWarehouseDeleteItem(event);
				}
				
				@Override
				public boolean onTransferItem(ClanWarehouseTransferEvent event)
				{
					return onClanWarehouseTransferItem(event);
				}
			};
			_listeners.add(listener);
		}
	}
	
	public void removeClanWarehouseNotify(L2Clan clan)
	{
		if (clan != null)
		{
			List<L2JListener> removeList = new ArrayList<>();
			for (L2JListener listener : _listeners)
			{
				if ((listener instanceof ClanWarehouseListener) && (((ClanWarehouseListener) listener).getWarehouse() == clan.getWarehouse()))
				{
					removeList.add(listener);
				}
			}
			removeListeners(removeList);
		}
	}
	
	public void addClanWarNotify()
	{
		ClanWarListener listener = new ClanWarListener()
		{
			@Override
			public boolean onWarStart(ClanWarEvent event)
			{
				event.setStage(EventStage.START);
				return onClanWarEvent(event);
			}
			
			@Override
			public boolean onWarEnd(ClanWarEvent event)
			{
				event.setStage(EventStage.END);
				return onClanWarEvent(event);
			}
		};
		_listeners.add(listener);
	}

	public void removeClanWarNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof ClanWarListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}

	public void addFortSiegeNotify()
	{
		FortSiegeListener listener = new FortSiegeListener()
		{
			@Override
			public boolean onStart(FortSiegeEvent event)
			{
				event.setStage(EventStage.START);
				return onFortSiegeEvent(event);
			}
			
			@Override
			public void onEnd(FortSiegeEvent event)
			{
				event.setStage(EventStage.END);
				onFortSiegeEvent(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeFortSiegeNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof FortSiegeListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}

	public void addSiegeNotify()
	{
		SiegeListener listener = new SiegeListener()
		{
			@Override
			public boolean onStart(SiegeEvent event)
			{
				event.setStage(EventStage.START);
				return onSiegeEvent(event);
			}
			
			@Override
			public void onEnd(SiegeEvent event)
			{
				event.setStage(EventStage.END);
				onSiegeEvent(event);
			}
			
			@Override
			public void onControlChange(SiegeEvent event)
			{
				onCastleControlChange(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeSiegeNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof SiegeListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addTvTNotify()
	{
		TvTListener listener = new TvTListener()
		{
			@Override
			public void onBegin()
			{
				onTvtEvent(EventStage.START);
			}
			
			@Override
			public void onKill(TvtKillEvent event)
			{
				onTvtKill(event);
			}
			
			@Override
			public void onEnd()
			{
				onTvtEvent(EventStage.END);
			}
			
			@Override
			public void onRegistrationStart()
			{
				onTvtEvent(EventStage.REGISTRATION_BEGIN);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeTvtNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof TvTListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addItemAugmentNotify()
	{
		AugmentListener listener = new AugmentListener()
		{
			@Override
			public boolean onAugment(AugmentEvent event)
			{
				return onItemAugment(event);
			}
			
			@Override
			public boolean onRemoveAugment(AugmentEvent event)
			{
				return onItemAugment(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeItemAugmentNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof AugmentListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addItemDropPickupNotify()
	{
		DropListener listener = new DropListener()
		{
			
			@Override
			public boolean onDrop(ItemDropEvent event)
			{
				return onItemDrop(event);
			}
			
			@Override
			public boolean onPickup(ItemPickupEvent event)
			{
				return onItemPickup(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeItemDropPickupNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof DropListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addPlayerLevelNotify(L2PcInstance player)
	{
		PlayerLevelListener listener = new PlayerLevelListener(player)
		{
			@Override
			public boolean onLevelChange(L2Playable playable, byte levels)
			{
				final PlayerLevelChangeEvent event = new PlayerLevelChangeEvent();
				event.setPlayer(playable.getActingPlayer());
				event.setOldLevel(playable.getLevel());
				event.setNewLevel(playable.getLevel() + levels);
				return L2Script.this.onLevelChange(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removePlayerLevelNotify(L2PcInstance player)
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if ((listener instanceof PlayerLevelListener) && (listener.getPlayer() == player))
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addProfessionChangeNotify(L2PcInstance player)
	{
		ProfessionChangeListener listener = new ProfessionChangeListener(player)
		{
			@Override
			public void professionChanged(ProfessionChangeEvent event)
			{
				onProfessionChange(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeProfessionChangeNotify(L2PcInstance player)
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if ((listener instanceof ProfessionChangeListener) && (listener.getPlayer() == player))
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addEquipmentNotify(L2PcInstance player)
	{
		EquipmentListener listener = new EquipmentListener(player)
		{
			@Override
			public boolean onEquip(EquipmentEvent event)
			{
				return onItemEquip(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeEquipmentNotify(L2PcInstance player)
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if ((listener instanceof EquipmentListener) && (((EquipmentListener) listener).getPlayer() == player))
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addHennaNotify()
	{
		HennaListener listener = new HennaListener()
		{
			@Override
			public boolean onAddHenna(HennaEvent event)
			{
				return onHennaModify(event);
			}
			
			@Override
			public boolean onRemoveHenna(HennaEvent event)
			{
				return onHennaModify(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removeHennaNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof HennaListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addItemTracker(final List<Integer> itemIds)
	{
		if (itemIds != null)
		{
			ItemTracker listener = new ItemTracker(itemIds)
			{
				@Override
				public void onDrop(ItemDropEvent event)
				{
					onItemTrackerEvent(event);
				}
				
				@Override
				public void onAddToInventory(AddToInventoryEvent event)
				{
					onItemTrackerEvent(event);
				}
				
				@Override
				public void onDestroy(ItemDestroyEvent event)
				{
					onItemTrackerEvent(event);
				}
				
				@Override
				public void onTransfer(ItemTransferEvent event)
				{
					onItemTrackerEvent(event);
				}
			};
			_listeners.add(listener);
		}
	}
	
	public void removeItemTrackers()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof ItemTracker)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addNewItemNotify(List<Integer> itemIds)
	{
		if (itemIds != null)
		{
			NewItemListener listener = new NewItemListener(itemIds)
			{
				
				@Override
				public boolean onCreate(ItemCreateEvent event)
				{
					return onItemCreate(event);
				}
			};
			_listeners.add(listener);
		}
	}
	
	public void removeNewItemNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof NewItemListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addTransformNotify(final L2PcInstance player)
	{
		if (player != null)
		{
			TransformListener listener = new TransformListener(player)
			{
				@Override
				public boolean onTransform(TransformEvent event)
				{
					event.setTransforming(true);
					return onPlayerTransform(event);
				}
				
				@Override
				public boolean onUntransform(TransformEvent event)
				{
					return onPlayerTransform(event);
				}
			};
			_listeners.add(listener);
		}
	}
	
	public void removeTransformNotify(L2PcInstance player)
	{
		if (player != null)
		{
			List<L2JListener> removeList = new ArrayList<>();
			for (L2JListener listener : _listeners)
			{
				if ((listener instanceof TransformListener) && (listener.getPlayer() == player))
				{
					removeList.add(listener);
				}
			}
			removeListeners(removeList);
		}
	}
	
	public void addPlayerChatFilter()
	{
		ChatFilterListener listener = new ChatFilterListener()
		{
			@Override
			public String onTalk(ChatEvent event)
			{
				return filterChat(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removePlayerChatFilter()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof ChatFilterListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addPlayerTalkNotify()
	{
		ChatListener listener = new ChatListener()
		{
			@Override
			public void onTalk(ChatEvent event)
			{
				onPlayerTalk(event);
			}
		};
		_listeners.add(listener);
	}
	
	public void removePlayerTalkNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof ChatListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}

	public void addDlgAnswerNotify(L2PcInstance player)
	{
		DlgAnswerListener dlgAnswer = new DlgAnswerListener(player)
		{
			@Override
			public boolean onDlgAnswer(L2PcInstance player, int messageId, int answer, int requesterId)
			{
				final DlgAnswerEvent event = new DlgAnswerEvent();
				event.setActiveChar(player);
				event.setMessageId(messageId);
				event.setAnswer(answer);
				event.setRequesterId(requesterId);
				return L2Script.this.onDlgAnswer(event);
			}
		};
		
		_listeners.add(dlgAnswer);
	}
	
	public void removeDlgAnswerNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof DlgAnswerListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void removeDlgAnswerNotify(L2PcInstance player)
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if ((listener instanceof DlgAnswerListener) && (((DlgAnswerListener) listener).getPlayer() == player))
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addRequestBypassToServerNotify()
	{
		RequestBypassToServerListener bypass = new RequestBypassToServerListener()
		{
			@Override
			public void onRequestBypassToServer(RequestBypassToServerEvent event)
			{
				L2Script.this.onRequestBypassToServer(event);
			}
		};
		_listeners.add(bypass);
	}
	
	public void removeRequestBypassToServerNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof DlgAnswerListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void addPlayerNotify()
	{
		PlayerListener bypass = new PlayerListener()
		{	
			@Override
			public void onCharCreate(PlayerEvent event)
			{
				L2Script.this.onCharCreate(event);
				
			}
			
			@Override
			public void onCharDelete(PlayerEvent event)
			{
				L2Script.this.onCharDelete(event);
			}
			
			@Override
			public void onCharRestore(PlayerEvent event)
			{
				L2Script.this.onCharRestore(event);
			}
			
			@Override
			public void onCharSelect(PlayerEvent event)
			{
				L2Script.this.onCharSelect(event);
			}
		};
		_listeners.add(bypass);
	}
	
	public void removePlayerNotify()
	{
		List<L2JListener> removeList = new ArrayList<>();
		for (L2JListener listener : _listeners)
		{
			if (listener instanceof PlayerListener)
			{
				removeList.add(listener);
			}
		}
		removeListeners(removeList);
	}
	
	public void onPlayerLogin(L2PcInstance player)
	{		
	}
	
	public void onPlayerLogout(L2PcInstance player)
	{	
	}
	
	public boolean onAttack(AttackEvent event)
	{
		return true;
	}
	
	public boolean onDeath(DeathEvent event)
	{
		return true;
	}
	
	public boolean onSkillUse(SkillUseEvent event)
	{
		return true;
	}
	
	public void onClanCreated(ClanCreationEvent event)
	{
		
	}
	
	public boolean onClanLeveledUp(ClanLevelUpEvent event)
	{
		return true;
	}

	public boolean onClanJoin(ClanJoinEvent event)
	{
		return true;
	}
	
	public boolean onClanLeave(ClanLeaveEvent event)
	{
		return true;
	}
	
	public boolean onClanLeaderChange(ClanLeaderChangeEvent event)
	{
		return true;
	}
	
	public boolean onClanWarehouseAddItem(ClanWarehouseAddItemEvent event)
	{
		return true;
	}
	
	public boolean onClanWarehouseDeleteItem(ClanWarehouseDeleteItemEvent event)
	{
		return true;
	}
	
	public boolean onClanWarehouseTransferItem(ClanWarehouseTransferEvent event)
	{
		return true;
	}
	
	public boolean onClanWarEvent(ClanWarEvent event)
	{
		return true;
	}
	
	public boolean onFortSiegeEvent(FortSiegeEvent event)
	{
		return true;
	}
	
	public boolean onSiegeEvent(SiegeEvent event)
	{
		return true;
	}
	
	public void onCastleControlChange(SiegeEvent event)
	{
		
	}

	public void onTvtEvent(EventStage stage)
	{
		
	}
	
	public void onTvtKill(TvtKillEvent event)
	{
		
	}
	
	public boolean onItemAugment(AugmentEvent event)
	{
		return true;
	}
	
	public boolean onItemDrop(ItemDropEvent event)
	{
		return true;
	}
	
	public boolean onItemPickup(ItemPickupEvent event)
	{
		return true;
	}
	
	public boolean onItemEquip(EquipmentEvent event)
	{
		return true;
	}
	
	public boolean onLevelChange(PlayerLevelChangeEvent event)
	{
		return true;
	}
	
	public void onProfessionChange(ProfessionChangeEvent event)
	{
		
	}
	
	public boolean onHennaModify(HennaEvent event)
	{
		return true;
	}
	
	public void onItemTrackerEvent(L2Event event)
	{
		
	}
	
	public boolean onItemCreate(ItemCreateEvent event)
	{
		return true;
	}

	public boolean onPlayerTransform(TransformEvent event)
	{
		return true;
	}
	
	public String filterChat(ChatEvent event)
	{
		return "";
	}
	
	public void onPlayerTalk(ChatEvent event)
	{	
	}

	public boolean onDlgAnswer(DlgAnswerEvent event)
	{
		return true;
	}
	
	protected void onRequestBypassToServer(RequestBypassToServerEvent event)
	{		
	}
	
	protected void onCharSelect(PlayerEvent event)
	{		
	}
	
	protected void onCharCreate(PlayerEvent event)
	{		
	}
	
	protected void onCharDelete(PlayerEvent event)
	{		
	}
	
	protected void onCharRestore(PlayerEvent event)
	{		
	}
	
	public enum EventStage
	{
		START,
		END,
		EVENT_STOPPED,
		REGISTRATION_BEGIN,
		CONTROL_CHANGE
	}
	
	public enum ItemTrackerEvent
	{
		DROP,
		ADD_TO_INVENTORY,
		DESTROY,
		TRANSFER
	}
}