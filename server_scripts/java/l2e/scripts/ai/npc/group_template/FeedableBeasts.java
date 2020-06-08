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

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2TamedBeastInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;
import l2e.scripts.quests._020_BringUpWithLove;

public class FeedableBeasts extends AbstractNpcAI
{
	private static final int GOLDEN_SPICE = 6643;
	private static final int CRYSTAL_SPICE = 6644;
	private static final int SKILL_GOLDEN_SPICE = 2188;
	private static final int SKILL_CRYSTAL_SPICE = 2189;
	private static final int[] TAMED_BEASTS =
	{
	                16013, 16014, 16015, 16016, 16017, 16018
	};
	private static final int FOODSKILLDIFF = GOLDEN_SPICE - SKILL_GOLDEN_SPICE;

	private static final int[] FEEDABLE_BEASTS =
	{
	                21451, 21452, 21453, 21454, 21455, 21456, 21457, 21458, 21459, 21460, 21461, 21462, 21463,
	                21464, 21465, 21466, 21467, 21468, 21469, 21470, 21471, 21472, 21473, 21474, 21475, 21476,
	                21477, 21478, 21479, 21480, 21481, 21482, 21483, 21484, 21485, 21486, 21487, 21488, 21489,
	                21490, 21491, 21492, 21493, 21494, 21495, 21496, 21497, 21498, 21499, 21500, 21501, 21502,
	                21503, 21504, 21505, 21506, 21507, 21824, 21825, 21826, 21827, 21828, 21829, 16013, 16014,
	                16015, 16016, 16017, 16018
	};

	private static final Map<Integer, Integer> MAD_COW_POLYMORPH = new FastMap<>();

	static
	{
		MAD_COW_POLYMORPH.put(21824, 21468);
		MAD_COW_POLYMORPH.put(21825, 21469);
		MAD_COW_POLYMORPH.put(21826, 21487);
		MAD_COW_POLYMORPH.put(21827, 21488);
		MAD_COW_POLYMORPH.put(21828, 21506);
		MAD_COW_POLYMORPH.put(21829, 21507);
	}

	private static final NpcStringId[][] TEXT =
	{
	                {
	                                NpcStringId.WHAT_DID_YOU_JUST_DO_TO_ME,
	                                NpcStringId.ARE_YOU_TRYING_TO_TAME_ME_DONT_DO_THAT,
	                                NpcStringId.DONT_GIVE_SUCH_A_THING_YOU_CAN_ENDANGER_YOURSELF,
	                                NpcStringId.YUCK_WHAT_IS_THIS_IT_TASTES_TERRIBLE,
	                                NpcStringId.IM_HUNGRY_GIVE_ME_A_LITTLE_MORE_PLEASE,
	                                NpcStringId.WHAT_IS_THIS_IS_THIS_EDIBLE,
	                                NpcStringId.DONT_WORRY_ABOUT_ME,
	                                NpcStringId.THANK_YOU_THAT_WAS_DELICIOUS,
	                                NpcStringId.I_THINK_I_AM_STARTING_TO_LIKE_YOU,
	                                NpcStringId.EEEEEK_EEEEEK
	                },
	                {
	                                NpcStringId.DONT_KEEP_TRYING_TO_TAME_ME_I_DONT_WANT_TO_BE_TAMED,
	                                NpcStringId.IT_IS_JUST_FOOD_TO_ME_ALTHOUGH_IT_MAY_ALSO_BE_YOUR_HAND,
	                                NpcStringId.IF_I_KEEP_EATING_LIKE_THIS_WONT_I_BECOME_FAT_CHOMP_CHOMP,
	                                NpcStringId.WHY_DO_YOU_KEEP_FEEDING_ME,
	                                NpcStringId.DONT_TRUST_ME_IM_AFRAID_I_MAY_BETRAY_YOU_LATER
	                },
	                {
	                                NpcStringId.GRRRRR,
	                                NpcStringId.YOU_BROUGHT_THIS_UPON_YOURSELF,
	                                NpcStringId.I_FEEL_STRANGE_I_KEEP_HAVING_THESE_EVIL_THOUGHTS,
	                                NpcStringId.ALAS_SO_THIS_IS_HOW_IT_ALL_ENDS,
	                                NpcStringId.I_DONT_FEEL_SO_GOOD_OH_MY_MIND_IS_VERY_TROUBLED
	                }
	};

	private static final NpcStringId[] TAMED_TEXT =
	{
	                NpcStringId.S1_SO_WHAT_DO_YOU_THINK_IT_IS_LIKE_TO_BE_TAMED,
	                NpcStringId.S1_WHENEVER_I_SEE_SPICE_I_THINK_I_WILL_MISS_YOUR_HAND_THAT_USED_TO_FEED_IT_TO_ME,
	                NpcStringId.S1_DONT_GO_TO_THE_VILLAGE_I_DONT_HAVE_THE_STRENGTH_TO_FOLLOW_YOU,
	                NpcStringId.THANK_YOU_FOR_TRUSTING_ME_S1_I_HOPE_I_WILL_BE_HELPFUL_TO_YOU,
	                NpcStringId.S1_WILL_I_BE_ABLE_TO_HELP_YOU,
	                NpcStringId.I_GUESS_ITS_JUST_MY_ANIMAL_MAGNETISM,
	                NpcStringId.TOO_MUCH_SPICY_FOOD_MAKES_ME_SWEAT_LIKE_A_BEAST,
	                NpcStringId.ANIMALS_NEED_LOVE_TOO
	};

	private static Map<Integer, Integer> _FeedInfo = new FastMap<>();
	private static Map<Integer, GrowthCapableMob> _GrowthCapableMobs = new FastMap<>();

	private static class GrowthCapableMob
	{
		private final int _growthLevel;
		private final int _chance;

		private final Map<Integer, int[][]> _spiceToMob = new FastMap<>();

		public GrowthCapableMob(int growthLevel, int chance)
		{
			_growthLevel = growthLevel;
			_chance = chance;
		}

		public void addMobs(int spice, int[][] Mobs)
		{
			_spiceToMob.put(spice, Mobs);
		}

		public Integer getMob(int spice, int mobType, int classType)
		{
			if (_spiceToMob.containsKey(spice))
			{
				return _spiceToMob.get(spice)[mobType][classType];
			}
			return null;
		}

		public Integer getRandomMob(int spice)
		{
			int[][] temp;
			temp = _spiceToMob.get(spice);
			int rand = getRandom(temp[0].length);
			return temp[0][rand];
		}

		public Integer getChance()
		{
			return _chance;
		}

		public Integer getGrowthLevel()
		{
			return _growthLevel;
		}
	}

	private FeedableBeasts()
	{
		super(FeedableBeasts.class.getSimpleName(), "ai");

		registerMobs(FEEDABLE_BEASTS, QuestEventType.ON_KILL, QuestEventType.ON_SKILL_SEE);

		GrowthCapableMob temp;

		final int[][] Kookabura_0_Gold =
		{
			{
			                21452, 21453, 21454, 21455
			}
		};
		final int[][] Kookabura_0_Crystal =
		{
			{
			                21456, 21457, 21458, 21459
			}
		};
		final int[][] Kookabura_1_Gold_1 =
		{
			{
			                21460, 21462
			}
		};
		final int[][] Kookabura_1_Gold_2 =
		{
			{
			                21461, 21463
			}
		};
		final int[][] Kookabura_1_Crystal_1 =
		{
			{
			                21464, 21466
			}
		};
		final int[][] Kookabura_1_Crystal_2 =
		{
			{
			                21465, 21467
			}
		};
		final int[][] Kookabura_2_1 =
		{
		                {
		                                21468, 21824
		                },
		                {
		                                16017, 16018
		                }
		};
		final int[][] Kookabura_2_2 =
		{
		                {
		                                21469, 21825
		                },
		                {
		                                16017, 16018
		                }
		};

		final int[][] Buffalo_0_Gold =
		{
			{
			                21471, 21472, 21473, 21474
			}
		};
		final int[][] Buffalo_0_Crystal =
		{
			{
			                21475, 21476, 21477, 21478
			}
		};
		final int[][] Buffalo_1_Gold_1 =
		{
			{
			                21479, 21481
			}
		};
		final int[][] Buffalo_1_Gold_2 =
		{
			{
			                21481, 21482
			}
		};
		final int[][] Buffalo_1_Crystal_1 =
		{
			{
			                21483, 21485
			}
		};
		final int[][] Buffalo_1_Crystal_2 =
		{
			{
			                21484, 21486
			}
		};
		final int[][] Buffalo_2_1 =
		{
		                {
		                                21487, 21826
		                },
		                {
		                                16013, 16014
		                }
		};
		final int[][] Buffalo_2_2 =
		{
		                {
		                                21488, 21827
		                },
		                {
		                                16013, 16014
		                }
		};

		final int[][] Cougar_0_Gold =
		{
			{
			                21490, 21491, 21492, 21493
			}
		};
		final int[][] Cougar_0_Crystal =
		{
			{
			                21494, 21495, 21496, 21497
			}
		};
		final int[][] Cougar_1_Gold_1 =
		{
			{
			                21498, 21500
			}
		};
		final int[][] Cougar_1_Gold_2 =
		{
			{
			                21499, 21501
			}
		};
		final int[][] Cougar_1_Crystal_1 =
		{
			{
			                21502, 21504
			}
		};
		final int[][] Cougar_1_Crystal_2 =
		{
			{
			                21503, 21505
			}
		};
		final int[][] Cougar_2_1 =
		{
		                {
		                                21506, 21828
		                },
		                {
		                                16015, 16016
		                }
		};
		final int[][] Cougar_2_2 =
		{
		                {
		                                21507, 21829
		                },
		                {
		                                16015, 16016
		                }
		};

		temp = new GrowthCapableMob(0, 100);
		temp.addMobs(GOLDEN_SPICE, Kookabura_0_Gold);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_0_Crystal);
		_GrowthCapableMobs.put(21451, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Kookabura_1_Gold_1);
		_GrowthCapableMobs.put(21452, temp);
		_GrowthCapableMobs.put(21454, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Kookabura_1_Gold_2);
		_GrowthCapableMobs.put(21453, temp);
		_GrowthCapableMobs.put(21455, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_1_Crystal_1);
		_GrowthCapableMobs.put(21456, temp);
		_GrowthCapableMobs.put(21458, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_1_Crystal_2);
		_GrowthCapableMobs.put(21457, temp);
		_GrowthCapableMobs.put(21459, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Kookabura_2_1);
		_GrowthCapableMobs.put(21460, temp);
		_GrowthCapableMobs.put(21462, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Kookabura_2_2);
		_GrowthCapableMobs.put(21461, temp);
		_GrowthCapableMobs.put(21463, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_2_1);
		_GrowthCapableMobs.put(21464, temp);
		_GrowthCapableMobs.put(21466, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_2_2);
		_GrowthCapableMobs.put(21465, temp);
		_GrowthCapableMobs.put(21467, temp);

		temp = new GrowthCapableMob(0, 100);
		temp.addMobs(GOLDEN_SPICE, Buffalo_0_Gold);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_0_Crystal);
		_GrowthCapableMobs.put(21470, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Buffalo_1_Gold_1);
		_GrowthCapableMobs.put(21471, temp);
		_GrowthCapableMobs.put(21473, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Buffalo_1_Gold_2);
		_GrowthCapableMobs.put(21472, temp);
		_GrowthCapableMobs.put(21474, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_1_Crystal_1);
		_GrowthCapableMobs.put(21475, temp);
		_GrowthCapableMobs.put(21477, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_1_Crystal_2);
		_GrowthCapableMobs.put(21476, temp);
		_GrowthCapableMobs.put(21478, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Buffalo_2_1);
		_GrowthCapableMobs.put(21479, temp);
		_GrowthCapableMobs.put(21481, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Buffalo_2_2);
		_GrowthCapableMobs.put(21480, temp);
		_GrowthCapableMobs.put(21482, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_2_1);
		_GrowthCapableMobs.put(21483, temp);
		_GrowthCapableMobs.put(21485, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_2_2);
		_GrowthCapableMobs.put(21484, temp);
		_GrowthCapableMobs.put(21486, temp);

		temp = new GrowthCapableMob(0, 100);
		temp.addMobs(GOLDEN_SPICE, Cougar_0_Gold);
		temp.addMobs(CRYSTAL_SPICE, Cougar_0_Crystal);
		_GrowthCapableMobs.put(21489, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Cougar_1_Gold_1);
		_GrowthCapableMobs.put(21490, temp);
		_GrowthCapableMobs.put(21492, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Cougar_1_Gold_2);
		_GrowthCapableMobs.put(21491, temp);
		_GrowthCapableMobs.put(21493, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Cougar_1_Crystal_1);
		_GrowthCapableMobs.put(21494, temp);
		_GrowthCapableMobs.put(21496, temp);

		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Cougar_1_Crystal_2);
		_GrowthCapableMobs.put(21495, temp);
		_GrowthCapableMobs.put(21497, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Cougar_2_1);
		_GrowthCapableMobs.put(21498, temp);
		_GrowthCapableMobs.put(21500, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Cougar_2_2);
		_GrowthCapableMobs.put(21499, temp);
		_GrowthCapableMobs.put(21501, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Cougar_2_1);
		_GrowthCapableMobs.put(21502, temp);
		_GrowthCapableMobs.put(21504, temp);

		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Cougar_2_2);
		_GrowthCapableMobs.put(21503, temp);
		_GrowthCapableMobs.put(21505, temp);
	}

	public void spawnNext(L2Npc npc, int growthLevel, L2PcInstance player, int food)
	{
		int npcId = npc.getId();
		int nextNpcId = 0;

		if (growthLevel == 2)
		{
			if (getRandom(2) == 0)
			{
				if (player.getClassId().isMage())
				{
					nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 1, 1);
				}
				else
				{
					nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 1, 0);
				}
			}
			else
			{
				if (getRandom(5) == 0)
				{
					nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 0, 1);
				}
				else
				{
					nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 0, 0);
				}
			}
		}
		else
		{
			nextNpcId = _GrowthCapableMobs.get(npcId).getRandomMob(food);
		}

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
			if ((player.getTrainedBeasts() != null) && !player.getTrainedBeasts().isEmpty())
			{
				for (L2TamedBeastInstance oldTrained : player.getTrainedBeasts())
				{
					oldTrained.deleteMe();
				}
			}

			L2NpcTemplate template = NpcTable.getInstance().getTemplate(nextNpcId);
			L2TamedBeastInstance nextNpc = new L2TamedBeastInstance(IdFactory.getInstance().getNextId(), template, player, food - FOODSKILLDIFF, npc.getX(), npc.getY(), npc.getZ());
			nextNpc.setRunning();

			_020_BringUpWithLove.checkJewelOfInnocence(player);

			if (getRandom(20) == 0)
			{
				NpcStringId message = NpcStringId.getNpcStringId(getRandom(2024, 2029));
				NpcSay packet = new NpcSay(nextNpc, 0, message);
				if (message.getParamCount() > 0)
				{
					packet.addStringParameter(player.getName());
				}
				npc.broadcastPacket(packet);
			}
		}
		else
		{
			L2Attackable nextNpc = (L2Attackable) addSpawn(nextNpcId, npc);

			if (MAD_COW_POLYMORPH.containsKey(nextNpcId))
			{
				this.startQuestTimer("polymorph Mad Cow", 10000, nextNpc, player);
			}
			_FeedInfo.put(nextNpc.getObjectId(), player.getObjectId());
			nextNpc.setRunning();
			nextNpc.addDamageHate(player, 0, 99999);
			nextNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("polymorph Mad Cow") && (npc != null) && (player != null))
		{
			if (MAD_COW_POLYMORPH.containsKey(npc.getId()))
			{
				if (_FeedInfo.get(npc.getObjectId()) == player.getObjectId())
				{
					_FeedInfo.remove(npc.getObjectId());
				}
				npc.deleteMe();

				L2Attackable nextNpc = (L2Attackable) addSpawn(MAD_COW_POLYMORPH.get(npc.getId()), npc);

				_FeedInfo.put(nextNpc.getObjectId(), player.getObjectId());
				nextNpc.setRunning();
				nextNpc.addDamageHate(player, 0, 99999);
				nextNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return super.onAdvEvent(event, npc, player);
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

		if (!Util.contains(FEEDABLE_BEASTS, npcId) || ((skillId != SKILL_GOLDEN_SPICE) && (skillId != SKILL_CRYSTAL_SPICE)))
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

		int food = 0;
		if (skillId == SKILL_GOLDEN_SPICE)
		{
			food = GOLDEN_SPICE;
		}
		else if (skillId == SKILL_CRYSTAL_SPICE)
		{
			food = CRYSTAL_SPICE;
		}
		npc.broadcastSocialAction(2);

		if (_GrowthCapableMobs.containsKey(npcId))
		{
			if (_GrowthCapableMobs.get(npcId).getMob(food, 0, 0) == null)
			{
				return super.onSkillSee(npc, caster, skill, targets, isSummon);
			}

			if (getRandom(20) == 0)
			{
				NpcStringId message = TEXT[growthLevel][getRandom(TEXT[growthLevel].length)];
				NpcSay packet = new NpcSay(npc, 0, message);
				if (message.getParamCount() > 0)
				{
					packet.addStringParameter(caster.getName());
				}
				npc.broadcastPacket(packet);
			}

			if ((growthLevel > 0) && (_FeedInfo.get(objectId) != caster.getObjectId()))
			{
				return super.onSkillSee(npc, caster, skill, targets, isSummon);
			}

			if (getRandom(100) < _GrowthCapableMobs.get(npcId).getChance())
			{
				spawnNext(npc, growthLevel, caster, food);
			}
		}
		else if (Util.contains(TAMED_BEASTS, npcId) && (npc instanceof L2TamedBeastInstance))
		{
			L2TamedBeastInstance beast = ((L2TamedBeastInstance) npc);
			if (skillId == beast.getFoodType())
			{
				beast.onReceiveFood();
				NpcStringId message = TAMED_TEXT[getRandom(TAMED_TEXT.length)];
				NpcSay packet = new NpcSay(npc, 0, message);
				if (message.getParamCount() > 0)
				{
					packet.addStringParameter(caster.getName());
				}
				beast.broadcastPacket(packet);
			}
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

	public static void main(String[] args)
	{
		new FeedableBeasts();
	}
}
