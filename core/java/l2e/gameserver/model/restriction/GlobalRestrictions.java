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
package l2e.gameserver.model.restriction;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;

import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public final class GlobalRestrictions
{
	private GlobalRestrictions()
	{
	}
	
	protected static enum RestrictionMode implements Comparator<GlobalRestriction>
	{
		isRestricted,
		canInviteToParty,
		canTarget,
		canRequestRevive,
		canTeleport,
		canUseItemHandler,
		canUseItem,
		canStandUp,
		levelChanged,
		playerLoggedIn,
		playerDisconnected,
		playerKilled,
		playerRevived,
		onBypassFeedback,
		onAction,
		fakePvPZone, ;
		private final Method _method;
		
		private RestrictionMode()
		{
			for (Method method : GlobalRestriction.class.getMethods())
			{
				if (name().equals(method.getName()))
				{
					_method = method;
					return;
				}
			}
			throw new InternalError();
		}
		
		private boolean equalsMethod(Method method)
		{
			if (!_method.getName().equals(method.getName()))
			{
				return false;
			}
			if (!_method.getReturnType().equals(method.getReturnType()))
			{
				return false;
			}
			return Arrays.equals(_method.getParameterTypes(), method.getParameterTypes());
		}
		
		protected static final RestrictionMode[] VALUES = RestrictionMode.values();
		
		protected static RestrictionMode parse(Method method)
		{
			for (RestrictionMode mode : VALUES)
			{
				if (mode.equalsMethod(method))
				{
					return mode;
				}
			}
			return null;
		}
		
		@Override
		public int compare(GlobalRestriction o1, GlobalRestriction o2)
		{
			return Double.compare(getPriority(o2), getPriority(o1));
		}
		
		private double getPriority(GlobalRestriction restriction)
		{
			RestrictionPriority a1 = getMatchingMethod(restriction.getClass()).getAnnotation(RestrictionPriority.class);
			if (a1 != null)
			{
				return a1.value();
			}
			RestrictionPriority a2 = restriction.getClass().getAnnotation(RestrictionPriority.class);
			if (a2 != null)
			{
				return a2.value();
			}
			return RestrictionPriority.DEFAULT_PRIORITY;
		}
		
		private Method getMatchingMethod(Class<? extends GlobalRestriction> clazz)
		{
			for (Method method : clazz.getMethods())
			{
				if (equalsMethod(method))
				{
					return method;
				}
			}
			throw new InternalError();
		}
	}
	
	protected static final GlobalRestriction[][] _restrictions = new GlobalRestriction[RestrictionMode.VALUES.length][0];
	
	public synchronized static void activate(GlobalRestriction restriction)
	{
		for (Method method : restriction.getClass().getMethods())
		{
			RestrictionMode mode = RestrictionMode.parse(method);
			if (mode == null)
			{
				continue;
			}
			if (method.getAnnotation(DisabledRestriction.class) != null)
			{
				continue;
			}
			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];
			if (!ArrayUtils.contains(restrictions, restriction))
			{
				restrictions = (GlobalRestriction[]) ArrayUtils.add(restrictions, restriction);
			}
			Arrays.sort(restrictions, mode);
			_restrictions[mode.ordinal()] = restrictions;
		}
	}
	
	public synchronized static void deactivate(GlobalRestriction restriction)
	{
		for (RestrictionMode mode : RestrictionMode.VALUES)
		{
			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];
			for (int index; (index = ArrayUtils.indexOf(restrictions, restriction)) != -1;)
			{
				restrictions = (GlobalRestriction[]) ArrayUtils.remove(restrictions, index);
			}
			_restrictions[mode.ordinal()] = restrictions;
		}
	}
	
	static
	{
		activate(new OlympiadRestriction());
		activate(new JailRestriction());
		activate(new ConfigRestriction());
		activate(new FunEventRestriction());
	}
	
	public static boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		if (activeChar == null)
		{
			return true;
		}
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You are in observer mode!");
			return true;
		}
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.isRestricted.ordinal()])
		{
			if (restriction.isRestricted(activeChar, callingRestriction))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canInviteToParty.ordinal()])
		{
			if (!restriction.canInviteToParty(activeChar, target))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage)
	{
		final L2PcInstance attacker_ = L2Object.getActingPlayer(activeChar);
		final L2PcInstance target_ = L2Object.getActingPlayer(target);
		return canTarget(activeChar, target, sendMessage, attacker_, target_);
	}
	
	private static boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage, L2PcInstance attacker_, L2PcInstance target_)
	{
		if ((target_ != null) && target_.isGM() && !target_.isVisible())
		{
			if ((activeChar != null) && ((attacker_ == null) || !attacker_.isGM()))
			{
				return false;
			}
		}
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canTarget.ordinal()])
		{
			if (!restriction.canTarget(activeChar, target, sendMessage, attacker_, target_))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean canRequestRevive(L2PcInstance activeChar)
	{
		if (activeChar.isPendingRevive())
		{
			return false;
		}
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canRequestRevive.ordinal()])
		{
			if (!restriction.canRequestRevive(activeChar))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean canTeleport(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canTeleport.ordinal()])
		{
			if (!restriction.canTeleport(activeChar))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item)
	{
		final L2PcInstance player = L2Object.getActingPlayer(activeChar);
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canUseItemHandler.ordinal()])
		{
			if (!restriction.canUseItemHandler(clazz, itemId, activeChar, item, player))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean canUseItem(int itemId, L2PcInstance activeChar, L2ItemInstance item)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canUseItem.ordinal()])
		{
			if (!restriction.canUseItem(itemId, activeChar, item))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean canStandUp(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canStandUp.ordinal()])
		{
			if (!restriction.canStandUp(activeChar))
			{
				return false;
			}
		}
		return true;
	}
	
	public static void levelChanged(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.levelChanged.ordinal()])
		{
			restriction.levelChanged(activeChar);
		}
	}
	
	public static void playerLoggedIn(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerLoggedIn.ordinal()])
		{
			restriction.playerLoggedIn(activeChar);
		}
	}
	
	public static void playerDisconnected(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerDisconnected.ordinal()])
		{
			restriction.playerDisconnected(activeChar);
		}
	}
	
	public static boolean playerKilled(L2Character activeChar, L2PcInstance target)
	{
		final L2PcInstance killer = L2Object.getActingPlayer(activeChar);
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerKilled.ordinal()])
		{
			if (!restriction.playerKilled(activeChar, target, killer))
			{
				return false;
			}
		}
		return true;
	}
	
	public static void playerRevived(L2PcInstance player)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerRevived.ordinal()])
		{
			restriction.playerRevived(player);
		}
	}
	
	public static boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.onBypassFeedback.ordinal()])
		{
			if (restriction.onBypassFeedback(npc, activeChar, command))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.onAction.ordinal()])
		{
			if (restriction.onAction(npc, activeChar))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean fakePvPZone(L2PcInstance activeChar, L2PcInstance target)
	{
		if ((activeChar == null) || (target == null))
		{
			return false;
		}
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.fakePvPZone.ordinal()])
		{
			if (restriction.fakePvPZone(activeChar, target))
			{
				return true;
			}
		}
		return false;
	}
}