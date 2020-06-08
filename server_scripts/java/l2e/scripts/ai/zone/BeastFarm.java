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
package l2e.scripts.ai.zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2TamedBeastInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;
import l2e.scripts.quests._020_BringUpWithLove;
import l2e.scripts.quests._655_AGrandPlanForTamingWildBeasts;

public class BeastFarm extends AbstractNpcAI
{
	private static final int GOLDEN_SPICE = 15474;
	private static final int CRYSTAL_SPICE = 15475;
	private static final int SKILL_GOLDEN_SPICE = 9049;
	private static final int SKILL_CRYSTAL_SPICE = 9050;
	private static final int SKILL_BLESSED_GOLDEN_SPICE = 9051;
	private static final int SKILL_BLESSED_CRYSTAL_SPICE = 9052;
	private static final int SKILL_SGRADE_GOLDEN_SPICE = 9053;
	private static final int SKILL_SGRADE_CRYSTAL_SPICE = 9054;
	private static final int[] TAMED_BEASTS =
	{
	                18869, 18870, 18871, 18872
	};
	private static final int TAME_CHANCE = 20;
	protected static final int[] SPECIAL_SPICE_CHANCES =
	{
	                33, 75
	};

	private static final int[] FEEDABLE_BEASTS =
	{
	                18873, 18874, 18875, 18876, 18877, 18878, 18879, 18880, 18881, 18882, 18883, 18884, 18885, 18886, 18887, 18888, 18889, 18890, 18891, 18892, 18893, 18894, 18895, 18896, 18897, 18898, 18899, 18900
	};

	private static Map<Integer, Integer> _FeedInfo = new FastMap<>();
	private static Map<Integer, GrowthCapableMob> _GrowthCapableMobs = new FastMap<>();
	private static List<TamedBeast> TAMED_BEAST_DATA = new ArrayList<>();

	private static class GrowthCapableMob
	{
		private final int _chance;
		private final int _growthLevel;
		private final int _tameNpcId;
		private final Map<Integer, Integer> _skillSuccessNpcIdList = new FastMap<>();

		public GrowthCapableMob(int chance, int growthLevel, int tameNpcId)
		{
			_chance = chance;
			_growthLevel = growthLevel;
			_tameNpcId = tameNpcId;
		}

		public void addNpcIdForSkillId(int skillId, int npcId)
		{
			_skillSuccessNpcIdList.put(skillId, npcId);
		}

		public int getGrowthLevel()
		{
			return _growthLevel;
		}

		public int getLeveledNpcId(int skillId)
		{
			if (!_skillSuccessNpcIdList.containsKey(skillId))
			{
				return -1;
			}
			else if ((skillId == SKILL_BLESSED_GOLDEN_SPICE) || (skillId == SKILL_BLESSED_CRYSTAL_SPICE) || (skillId == SKILL_SGRADE_GOLDEN_SPICE) || (skillId == SKILL_SGRADE_CRYSTAL_SPICE))
			{
				if (getRandom(100) < SPECIAL_SPICE_CHANCES[0])
				{
					if (getRandom(100) < SPECIAL_SPICE_CHANCES[1])
					{
						return _skillSuccessNpcIdList.get(skillId);
					}
					else if ((skillId == SKILL_BLESSED_GOLDEN_SPICE) || (skillId == SKILL_SGRADE_GOLDEN_SPICE))
					{
						return _skillSuccessNpcIdList.get(SKILL_GOLDEN_SPICE);
					}
					else
					{
						return _skillSuccessNpcIdList.get(SKILL_CRYSTAL_SPICE);
					}
				}
				return -1;
			}
			else if ((_growthLevel == 2) && (getRandom(100) < TAME_CHANCE))
			{
				return _tameNpcId;
			}
			else if (getRandom(100) < _chance)
			{
				return _skillSuccessNpcIdList.get(skillId);
			}
			else
			{
				return -1;
			}
		}
	}

	private BeastFarm()
	{
		super(BeastFarm.class.getSimpleName(), "ai");
		registerMobs(FEEDABLE_BEASTS, QuestEventType.ON_KILL, QuestEventType.ON_SKILL_SEE);

		GrowthCapableMob temp;

		temp = new GrowthCapableMob(100, 0, 18869);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18874);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18875);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18869);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18869);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18878);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18879);
		_GrowthCapableMobs.put(18873, temp);

		temp = new GrowthCapableMob(40, 1, 18869);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18876);
		_GrowthCapableMobs.put(18874, temp);

		temp = new GrowthCapableMob(40, 1, 18869);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18877);
		_GrowthCapableMobs.put(18875, temp);

		temp = new GrowthCapableMob(25, 2, 18869);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18878);
		_GrowthCapableMobs.put(18876, temp);

		temp = new GrowthCapableMob(25, 2, 18869);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18879);
		_GrowthCapableMobs.put(18877, temp);

		temp = new GrowthCapableMob(100, 0, 18870);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18881);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18882);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18870);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18870);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18885);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18886);
		_GrowthCapableMobs.put(18880, temp);

		temp = new GrowthCapableMob(40, 1, 18870);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18883);
		_GrowthCapableMobs.put(18881, temp);

		temp = new GrowthCapableMob(40, 1, 18870);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18884);
		_GrowthCapableMobs.put(18882, temp);

		temp = new GrowthCapableMob(25, 2, 18870);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18885);
		_GrowthCapableMobs.put(18883, temp);

		temp = new GrowthCapableMob(25, 2, 18870);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18886);
		_GrowthCapableMobs.put(18884, temp);

		temp = new GrowthCapableMob(100, 0, 18871);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18888);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18889);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18871);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18871);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18892);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18893);
		_GrowthCapableMobs.put(18887, temp);

		temp = new GrowthCapableMob(40, 1, 18871);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18890);
		_GrowthCapableMobs.put(18888, temp);

		temp = new GrowthCapableMob(40, 1, 18871);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18891);
		_GrowthCapableMobs.put(18889, temp);

		temp = new GrowthCapableMob(25, 2, 18871);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18892);
		_GrowthCapableMobs.put(18890, temp);

		temp = new GrowthCapableMob(25, 2, 18871);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18893);
		_GrowthCapableMobs.put(18891, temp);

		temp = new GrowthCapableMob(100, 0, 18872);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18895);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18896);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18872);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18872);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18899);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18900);
		_GrowthCapableMobs.put(18894, temp);

		temp = new GrowthCapableMob(40, 1, 18872);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18897);
		_GrowthCapableMobs.put(18895, temp);

		temp = new GrowthCapableMob(40, 1, 18872);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18898);
		_GrowthCapableMobs.put(18896, temp);

		temp = new GrowthCapableMob(25, 2, 18872);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18899);
		_GrowthCapableMobs.put(18897, temp);

		temp = new GrowthCapableMob(25, 2, 18872);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18900);
		_GrowthCapableMobs.put(18898, temp);

		TAMED_BEAST_DATA.add(new TamedBeast("%name% of Focus", new SkillsHolder(6432, 1), new SkillsHolder(6668, 1)));
		TAMED_BEAST_DATA.add(new TamedBeast("%name% of Guiding", new SkillsHolder(6433, 1), new SkillsHolder(6670, 1)));
		TAMED_BEAST_DATA.add(new TamedBeast("%name% of Swifth", new SkillsHolder(6434, 1), new SkillsHolder(6667, 1)));
		TAMED_BEAST_DATA.add(new TamedBeast("Berserker %name%", new SkillsHolder(6671, 1)));
		TAMED_BEAST_DATA.add(new TamedBeast("%name% of Protect", new SkillsHolder(6669, 1), new SkillsHolder(6672, 1)));
		TAMED_BEAST_DATA.add(new TamedBeast("%name% of Vigor", new SkillsHolder(6431, 1), new SkillsHolder(6666, 1)));
	}

	public void spawnNext(L2Npc npc, L2PcInstance player, int nextNpcId, int food)
	{
		if (_FeedInfo.containsKey(npc.getObjectId()))
		{
			if (_FeedInfo.get(npc.getObjectId()) == player.getObjectId())
			{
				_FeedInfo.remove(npc.getObjectId());
			}
		}
		npc.deleteMe();

		if (Util.contains(TAMED_BEASTS, nextNpcId))
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(nextNpcId);
			L2TamedBeastInstance nextNpc = new L2TamedBeastInstance(IdFactory.getInstance().getNextId(), template, player, food, npc.getX(), npc.getY(), npc.getZ(), true);

			TamedBeast beast = TAMED_BEAST_DATA.get(getRandom(TAMED_BEAST_DATA.size()));
			String name = beast.getName();
			switch (nextNpcId)
			{
				case 18869:
					name = name.replace("%name%", "Alpine Kookaburra");
					break;
				case 18870:
					name = name.replace("%name%", "Alpine Cougar");
					break;
				case 18871:
					name = name.replace("%name%", "Alpine Buffalo");
					break;
				case 18872:
					name = name.replace("%name%", "Alpine Grendel");
					break;
			}
			nextNpc.setName(name);
			nextNpc.broadcastPacket(new AbstractNpcInfo.NpcInfo(nextNpc, player));
			nextNpc.setRunning();

			SkillHolder st = SkillHolder.getInstance();
			for (SkillsHolder sh : beast.getSkills())
			{
				nextNpc.addBeastSkill(st.getInfo(sh.getSkillId(), sh.getSkillLvl()));
			}
			_020_BringUpWithLove.checkJewelOfInnocence(player);
			_655_AGrandPlanForTamingWildBeasts.checkCrystalofPurity(player);
		}
		else
		{
			L2Attackable nextNpc = (L2Attackable) addSpawn(nextNpcId, npc);

			_FeedInfo.put(nextNpc.getObjectId(), player.getObjectId());
			nextNpc.setRunning();
			nextNpc.addDamageHate(player, 0, 99999);
			nextNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			player.setTarget(nextNpc);
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (!Util.contains(targets, npc))
		{
			return super.onSkillSee(npc, caster, skill, targets, isSummon);
		}
		int npcId = npc.getId();
		int skillId = skill.getId();

		if (!Util.contains(FEEDABLE_BEASTS, npcId) || ((skillId != SKILL_GOLDEN_SPICE) && (skillId != SKILL_CRYSTAL_SPICE) && (skillId != SKILL_BLESSED_GOLDEN_SPICE) && (skillId != SKILL_BLESSED_CRYSTAL_SPICE) && (skillId != SKILL_SGRADE_GOLDEN_SPICE) && (skillId != SKILL_SGRADE_CRYSTAL_SPICE)))
		{
			return super.onSkillSee(npc, caster, skill, targets, isSummon);
		}

		int objectId = npc.getObjectId();
		int growthLevel = 3;
		if (_GrowthCapableMobs.containsKey(npcId))
		{
			growthLevel = _GrowthCapableMobs.get(npcId).getGrowthLevel();
		}

		if ((growthLevel == 0) && _FeedInfo.containsKey(objectId))
		{
			return super.onSkillSee(npc, caster, skill, targets, isSummon);
		}

		_FeedInfo.put(objectId, caster.getObjectId());
		npc.broadcastSocialAction(2);

		int food = 0;
		if ((skillId == SKILL_GOLDEN_SPICE) || (skillId == SKILL_BLESSED_GOLDEN_SPICE))
		{
			food = GOLDEN_SPICE;
		}
		else if ((skillId == SKILL_CRYSTAL_SPICE) || (skillId == SKILL_BLESSED_CRYSTAL_SPICE))
		{
			food = CRYSTAL_SPICE;
		}

		if (_GrowthCapableMobs.containsKey(npcId))
		{
			int newNpcId = _GrowthCapableMobs.get(npcId).getLeveledNpcId(skillId);
			if (newNpcId == -1)
			{
				if (growthLevel == 0)
				{
					_FeedInfo.remove(objectId);
					npc.setRunning();
					((L2Attackable) npc).addDamageHate(caster, 0, 1);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
				}
				return super.onSkillSee(npc, caster, skill, targets, isSummon);
			}
			else if ((growthLevel > 0) && (_FeedInfo.get(objectId) != caster.getObjectId()))
			{
				return super.onSkillSee(npc, caster, skill, targets, isSummon);
			}
			spawnNext(npc, caster, newNpcId, food);
		}
		else
		{
			caster.sendMessage("The beast spit out the feed instead of eating it.");
			((L2Attackable) npc).dropItem(caster, food, 1);
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (_FeedInfo.containsKey(npc.getObjectId()))
		{
			_FeedInfo.remove(npc.getObjectId());
		}
		return super.onKill(npc, killer, isSummon);
	}

	private static class TamedBeast
	{
		private final String name;
		private final SkillsHolder[] sh;

		public TamedBeast(String beastName, SkillsHolder... holders)
		{
			name = beastName;
			sh = holders;
		}

		public String getName()
		{
			return name;
		}

		public SkillsHolder[] getSkills()
		{
			return sh;
		}
	}

	public static void main(String[] args)
	{
		new BeastFarm();
	}
}
