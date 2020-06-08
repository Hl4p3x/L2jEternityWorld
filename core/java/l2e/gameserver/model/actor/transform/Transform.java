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
package l2e.gameserver.model.actor.transform;

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.AdditionalItemHolder;
import l2e.gameserver.model.holders.AdditionalSkillHolder;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.serverpackets.ExBasicActionList;

public final class Transform implements IIdentifiable
{
	private final int _id;
	private final int _displayId;
	private final TransformType _type;
	private final boolean _canSwim;
	private final int _spawnHeight;
	private final boolean _canAttack;
	private final String _name;
	private final String _title;
	
	private TransformTemplate _maleTemplate;
	private TransformTemplate _femaleTemplate;
	
	public Transform(StatsSet set)
	{
		_id = set.getInteger("id");
		_displayId = set.getInteger("displayId", _id);
		_type = set.getEnum("type", TransformType.class, TransformType.COMBAT);
		_canSwim = set.getInteger("can_swim", 0) == 1;
		_canAttack = set.getInteger("normal_attackable", 1) == 1;
		_spawnHeight = set.getInteger("spawn_height", 0);
		_name = set.getString("setName", null);
		_title = set.getString("setTitle", null);
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public TransformType getType()
	{
		return _type;
	}
	
	public boolean canSwim()
	{
		return _canSwim;
	}
	
	public boolean canAttack()
	{
		return _canAttack;
	}
	
	public int getSpawnHeight()
	{
		return _spawnHeight;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public TransformTemplate getTemplate(L2PcInstance player)
	{
		return player != null ? (player.getAppearance().getSex() ? _femaleTemplate : _maleTemplate) : null;
	}
	
	public void setTemplate(boolean male, TransformTemplate template)
	{
		if (male)
		{
			_maleTemplate = template;
		}
		else
		{
			_femaleTemplate = template;
		}
	}
	
	public boolean isStance()
	{
		return _type == TransformType.MODE_CHANGE;
	}
	
	public boolean isCombat()
	{
		return _type == TransformType.COMBAT;
	}
	
	public boolean isNonCombat()
	{
		return _type == TransformType.NON_COMBAT;
	}
	
	public boolean isFlying()
	{
		return _type == TransformType.FLYING;
	}
	
	public boolean isCursed()
	{
		return _type == TransformType.CURSED;
	}
	
	public boolean isRiding()
	{
		return _type == TransformType.RIDING_MODE;
	}
	
	public boolean isPureStats()
	{
		return _type == TransformType.PURE_STAT;
	}
	
	public double getCollisionHeight(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		return template != null ? template.getCollisionHeight() : player.getCollisionHeight();
	}
	
	public double getCollisionRadius(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		return template != null ? template.getCollisionRadius() : player.getCollisionRadius();
	}
	
	public void onTransform(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			if (isFlying())
			{
				player.setIsFlying(true);
			}
			
			if (getName() != null)
			{
				player.getAppearance().setVisibleName(getName());
			}
			if (getTitle() != null)
			{
				player.getAppearance().setVisibleTitle(getTitle());
			}
			
			for (SkillsHolder holder : template.getSkills())
			{
				if (player.getSkillLevel(holder.getSkillId()) < holder.getSkillLvl())
				{
					player.addSkill(holder.getSkill(), false);
				}
				player.addTransformSkill(holder.getSkillId());
			}
			
			for (AdditionalSkillHolder holder : template.getAdditionalSkills())
			{
				if (player.getLevel() >= holder.getMinLevel())
				{
					if (player.getSkillLevel(holder.getSkillId()) < holder.getSkillLvl())
					{
						player.addSkill(holder.getSkill(), false);
					}
					player.addTransformSkill(holder.getSkillId());
				}
			}
			
			for (L2SkillLearn skill : SkillTreesParser.getInstance().getCollectSkillTree().values())
			{
				if (player.getKnownSkill(skill.getSkillId()) != null)
				{
					player.addTransformSkill(skill.getSkillId());
				}
			}
			
			if (!template.getAdditionalItems().isEmpty())
			{
				final List<Integer> allowed = new ArrayList<>();
				final List<Integer> notAllowed = new ArrayList<>();
				for (AdditionalItemHolder holder : template.getAdditionalItems())
				{
					if (holder.isAllowedToUse())
					{
						allowed.add(holder.getId());
					}
					else
					{
						notAllowed.add(holder.getId());
					}
				}
				
				if (!allowed.isEmpty())
				{
					final int[] items = new int[allowed.size()];
					for (int i = 0; i < items.length; i++)
					{
						items[i] = allowed.get(i);
					}
					player.getInventory().setInventoryBlock(items, 1);
				}
				
				if (!notAllowed.isEmpty())
				{
					final int[] items = new int[notAllowed.size()];
					for (int i = 0; i < items.length; i++)
					{
						items[i] = notAllowed.get(i);
					}
					player.getInventory().setInventoryBlock(items, 2);
				}
			}
			
			if (template.hasBasicActionList())
			{
				player.sendPacket(template.getBasicActionList());
			}
		}
	}
	
	public void onUntransform(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			if (isFlying())
			{
				player.setIsFlying(false);
			}
			
			if (getName() != null)
			{
				player.getAppearance().setVisibleName(null);
			}
			if (getTitle() != null)
			{
				player.getAppearance().setVisibleTitle(null);
			}
			
			if (!template.getSkills().isEmpty())
			{
				for (SkillsHolder holder : template.getSkills())
				{
					final L2Skill skill = holder.getSkill();
					if (!SkillTreesParser.getInstance().isSkillAllowed(player, skill))
					{
						player.removeSkill(skill, false, skill.isPassive());
					}
				}
			}
			
			if (!template.getAdditionalSkills().isEmpty())
			{
				for (AdditionalSkillHolder holder : template.getAdditionalSkills())
				{
					final L2Skill skill = holder.getSkill();
					if ((player.getLevel() >= holder.getMinLevel()) && !SkillTreesParser.getInstance().isSkillAllowed(player, skill))
					{
						player.removeSkill(skill, false, skill.isPassive());
					}
				}
			}
			player.removeAllTransformSkills();
			
			if (!template.getAdditionalItems().isEmpty())
			{
				player.getInventory().unblock();
			}
			player.sendPacket(ExBasicActionList.STATIC_PACKET);
		}
	}
	
	public void onLevelUp(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			if (!template.getAdditionalSkills().isEmpty())
			{
				for (AdditionalSkillHolder holder : template.getAdditionalSkills())
				{
					if (player.getLevel() >= holder.getMinLevel())
					{
						if (player.getSkillLevel(holder.getSkillId()) < holder.getSkillLvl())
						{
							player.addSkill(holder.getSkill(), false);
							player.addTransformSkill(holder.getSkillId());
						}
					}
				}
			}
		}
	}
	
	public double getStat(L2PcInstance player, Stats stats)
	{
		double val = 0;
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			val = template.getStats(stats);
			final TransformLevelData data = template.getData(player.getLevel());
			if (data != null)
			{
				val = data.getStats(stats);
			}
		}
		return val;
	}
	
	public int getBaseDefBySlot(L2PcInstance player, int slot)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			return template.getDefense(slot);
		}
		return player.getTemplate().getBaseDefBySlot(slot);
	}
	
	public double getLevelMod(L2PcInstance player)
	{
		double val = -1;
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			final TransformLevelData data = template.getData(player.getLevel());
			if (data != null)
			{
				val = data.getLevelMod();
			}
		}
		return val;
	}
}