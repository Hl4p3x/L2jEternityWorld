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
package l2e.gameserver.model.entity.mods.aio.main;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import l2e.Config;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;

public class Conditions
{
	public static boolean checkPlayerConditions(L2PcInstance player)
	{
		if (player.getPvpFlag() > 0)
		{
			player.sendMessage("Cannot use AIOItem while flagged!");
			return false;
		}
		if ((player.getKarma() > 0) || player.isCursedWeaponEquipped())
		{
			player.sendMessage("Cannot use AIOItem while chaotic!");
			return false;
		}
		if (player.isInOlympiadMode() || TvTEvent.isPlayerParticipant(player.getObjectId()) || TvTRoundEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("Cannot use while in events!");
			return false;
		}
		if (player.isEnchanting())
		{
			player.sendMessage("Cannot use while enchanting!");
			return false;
		}
		if (player.isJailed())
		{
			player.sendMessage("Cannot use while in Jail!");
			return false;
		}
		return true;
	}
	
	public static boolean checkPlayerItemCount(L2PcInstance player, int itemId, int count)
	{
		if ((player.getInventory().getItemByItemId(itemId) == null) || (player.getInventory().getItemByItemId(itemId).getCount() < count))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}
		return true;
	}
	
	public static boolean isValidName(String name)
	{
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e)
		{
			pattern = Pattern.compile(".*");
		}
		return pattern.matcher(name).matches();
	}
	
	public static boolean checkQuests(L2PcInstance player)
	{
		if (player.isNoble())
		{
			return true;
		}
		
		QuestState qs = player.getQuestState("_234_FatesWhisper");
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		qs = player.getQuestState("_235_MimirsElixir");
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		return true;
	}
	
	public final static Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
	{
		final int currentBaseId = player.getBaseClass();
		final ClassId baseCID = ClassId.getClassId(currentBaseId);
		final int baseClassId;
		
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().ordinal();
		}
		else
		{
			baseClassId = currentBaseId;
		}
		
		Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
		
		if ((availSubs != null) && !availSubs.isEmpty())
		{
			for (Iterator<PlayerClass> availSub = availSubs.iterator(); availSub.hasNext();)
			{
				PlayerClass pclass = availSub.next();
				
				if (!checkVillageMaster(pclass))
				{
					availSub.remove();
					continue;
				}
				int availClassId = pclass.ordinal();
				ClassId cid = ClassId.getClassId(availClassId);
				SubClass prevSubClass;
				ClassId subClassId;
				for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
				{
					prevSubClass = subList.next();
					subClassId = ClassId.getClassId(prevSubClass.getClassId());
					
					if (subClassId.equalsOrChildOf(cid))
					{
						availSub.remove();
						break;
					}
				}
			}
		}
		return availSubs;
	}
	
	public final static boolean isValidNewSubClass(L2PcInstance player, int classId)
	{
		if (!checkVillageMaster(classId))
		{
			return false;
		}
		
		final ClassId cid = ClassId.values()[classId];
		SubClass sub;
		ClassId subClassId;
		for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
		{
			sub = subList.next();
			subClassId = ClassId.values()[sub.getClassId()];
			
			if (subClassId.equalsOrChildOf(cid))
			{
				return false;
			}
		}
		final int currentBaseId = player.getBaseClass();
		final ClassId baseCID = ClassId.getClassId(currentBaseId);
		final int baseClassId;
		
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().ordinal();
		}
		else
		{
			baseClassId = currentBaseId;
		}
		
		Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
		if ((availSubs == null) || availSubs.isEmpty())
		{
			return false;
		}
		
		boolean found = false;
		for (PlayerClass pclass : availSubs)
		{
			if (pclass.ordinal() == classId)
			{
				found = true;
				break;
			}
		}
		return found;
	}
	
	public static boolean checkVillageMasterRace(PlayerClass pclass)
	{
		return true;
	}
	
	protected static boolean checkVillageMasterTeachType(PlayerClass pclass)
	{
		return true;
	}
	
	public final static boolean checkVillageMaster(int classId)
	{
		return checkVillageMaster(PlayerClass.values()[classId]);
	}
	
	public final static boolean checkVillageMaster(PlayerClass pclass)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE)
		{
			return true;
		}
		return checkVillageMasterRace(pclass) && checkVillageMasterTeachType(pclass);
	}
	
	public static final Iterator<SubClass> iterSubClasses(L2PcInstance player)
	{
		return player.getSubClasses().values().iterator();
	}
}