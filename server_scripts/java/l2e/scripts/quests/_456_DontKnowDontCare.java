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
package l2e.scripts.quests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.L2CommandChannel;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Attackable.AggroInfo;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.QuestState.QuestType;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.util.Util;

public final class _456_DontKnowDontCare extends Quest
{
	private static final int[] SEPARATED_SOUL =
	{
	                32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891
	};

	private static final int DRAKE_LORD_CORPSE = 32884;
	private static final int BEHEMOTH_LEADER_CORPSE = 32885;
	private static final int DRAGON_BEAST_CORPSE = 32886;

	private static final int DRAKE_LORD_ESSENCE = 17251;
	private static final int BEHEMOTH_LEADER_ESSENCE = 17252;
	private static final int DRAGON_BEAST_ESSENCE = 17253;

	private static final int MIN_PLAYERS = 4;
	private static final int MIN_LEVEL = 80;
	private static final Map<Integer, Integer> MONSTER_NPCS = new HashMap<>();
	private static final Map<Integer, Integer> MONSTER_ESSENCES = new HashMap<>();
	static
	{
		MONSTER_NPCS.put(25725, DRAKE_LORD_CORPSE);
		MONSTER_NPCS.put(25726, BEHEMOTH_LEADER_CORPSE);
		MONSTER_NPCS.put(25727, DRAGON_BEAST_CORPSE);
		MONSTER_ESSENCES.put(DRAKE_LORD_CORPSE, DRAKE_LORD_ESSENCE);
		MONSTER_ESSENCES.put(BEHEMOTH_LEADER_CORPSE, BEHEMOTH_LEADER_ESSENCE);
		MONSTER_ESSENCES.put(DRAGON_BEAST_CORPSE, DRAGON_BEAST_ESSENCE);
	}

	private static final int[] WEAPONS =
	{
	                15558,
	                15559,
	                15560,
	                15561,
	                15562,
	                15563,
	                15564,
	                15565,
	                15566,
	                15567,
	                15568,
	                15569,
	                15570,
	                15571
	};

	private static final int[] ARMOR =
	{
	                15743,
	                15746,
	                15749,
	                15752,
	                15755,
	                15758,
	                15744,
	                15747,
	                15750,
	                15753,
	                15756,
	                15745,
	                15748,
	                15751,
	                15754,
	                15757,
	                15759
	};

	private static final int[] ACCESSORIES =
	{
	                15763,
	                15764,
	                15765
	};

	private static final int[] ATTRIBUTE_CRYSTALS =
	{
	                9552,
	                9553,
	                9554,
	                9555,
	                9556,
	                9557
	};

	private static final int BLESSED_SCROLL_ENCHANT_WEAPON_S = 6577;
	private static final int BLESSED_SCROLL_ENCHANT_ARMOR_S = 6578;
	private static final int SCROLL_ENCHANT_WEAPON_S = 959;
	private static final int GEMSTONE_S = 2134;
	private final Map<Integer, Set<Integer>> allowedPlayerMap = new HashMap<>();

	public _456_DontKnowDontCare()
	{
		super(456, _456_DontKnowDontCare.class.getSimpleName(), "");
		addStartNpc(SEPARATED_SOUL);
		addTalkId(SEPARATED_SOUL);
		addFirstTalkId(DRAKE_LORD_CORPSE, BEHEMOTH_LEADER_CORPSE, DRAGON_BEAST_CORPSE);
		addTalkId(DRAKE_LORD_CORPSE, BEHEMOTH_LEADER_CORPSE, DRAGON_BEAST_CORPSE);
		addKillId(MONSTER_NPCS.keySet());
		registerQuestItems(DRAKE_LORD_ESSENCE, BEHEMOTH_LEADER_ESSENCE, DRAGON_BEAST_ESSENCE);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(getName());
		final Set<Integer> allowedPlayers = allowedPlayerMap.get(npc.getObjectId());

		if ((qs == null) || !qs.isCond(1) || (allowedPlayers == null) || !allowedPlayers.contains(player.getObjectId()))
		{
			return npc.getId() + "-02.htm";
		}

		final int essence = MONSTER_ESSENCES.get(npc.getId());
		final String htmltext;

		if (hasQuestItems(player, essence))
		{
			htmltext = npc.getId() + "-03.htm";
		}
		else
		{
			giveItems(player, essence, 1);
			htmltext = npc.getId() + "-01.htm";

			if (hasQuestItems(player, getRegisteredItemIds()))
			{
				qs.setCond(2, true);
			}
			else
			{
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);

		if (qs == null)
		{
			return htmltext;
		}

		if (Util.contains(SEPARATED_SOUL, npc.getId()))
		{
			switch (qs.getState())
			{
				case State.COMPLETED:
					if (!qs.isNowAvailable())
					{
						htmltext = "32864-02.htm";
						break;
					}
					qs.setState(State.CREATED);
				case State.CREATED:
					htmltext = ((player.getLevel() >= MIN_LEVEL) ? "32864-01.htm" : "32864-03.htm");
					break;
				case State.STARTED:
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = (hasAtLeastOneQuestItem(player, getRegisteredItemIds()) ? "32864-09.htm" : "32864-08.htm");
							break;
						}
						case 2:
						{
							if (hasQuestItems(player, getRegisteredItemIds()))
							{
								rewardPlayer(player, npc);
								qs.exitQuest(QuestType.DAILY, true);
								htmltext = "32864-10.htm";
							}
							break;
						}
					}
					break;
			}
		}
		return htmltext;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs;
		String htmltext = null;

		switch (event)
		{
			case "32864-04.htm":
			case "32864-05.htm":
			case "32864-06.htm":
				qs = player.getQuestState(getName());
				if ((qs != null) && qs.isCreated())
				{
					htmltext = event;
				}
				break;
			case "32864-07.htm":
				qs = player.getQuestState(getName());
				if ((qs != null) && qs.isCreated())
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			case "unspawnRaidCorpse":
				allowedPlayerMap.remove(npc.getObjectId());
				npc.deleteMe();
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (!killer.isInParty() || !killer.getParty().isInCommandChannel())
		{
			return super.onKill(npc, killer, isSummon);
		}

		final L2CommandChannel cc = killer.getParty().getCommandChannel();

		if (cc.getMemberCount() < MIN_PLAYERS)
		{
			return super.onKill(npc, killer, isSummon);
		}

		Map<L2Character, AggroInfo> playerList = ((L2Attackable) npc).getAggroList();
		Set<Integer> allowedPlayers = new HashSet<>();

		for (AggroInfo aggro : playerList.values())
		{
			if ((aggro.getAttacker() == null) || !aggro.getAttacker().isPlayer())
			{
				continue;
			}

			L2PcInstance attacker = aggro.getAttacker().getActingPlayer();

			if (attacker.isInParty() && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().equals(cc) && Util.checkIfInRange(1500, npc, attacker, true))
			{
				allowedPlayers.add(attacker.getObjectId());
			}
		}

		if (!allowedPlayers.isEmpty())
		{
			final L2Npc spawned = addSpawn(MONSTER_NPCS.get(npc.getId()), npc.getLocation(), true, 0);
			allowedPlayerMap.put(spawned.getObjectId(), allowedPlayers);
			startQuestTimer("unspawnRaidCorpse", 120000, spawned, null);
		}
		return super.onKill(npc, killer, isSummon);
	}

	private static void rewardPlayer(L2PcInstance player, L2Npc npc)
	{
		int chance = getRandom(10000);
		final int reward;
		int count = 1;

		if (chance < 170)
		{
			reward = ARMOR[getRandom(ARMOR.length)];
		}
		else if (chance < 200)
		{
			reward = ACCESSORIES[getRandom(ACCESSORIES.length)];
		}
		else if (chance < 270)
		{
			reward = WEAPONS[getRandom(WEAPONS.length)];
		}
		else if (chance < 325)
		{
			reward = BLESSED_SCROLL_ENCHANT_WEAPON_S;
		}
		else if (chance < 425)
		{
			reward = BLESSED_SCROLL_ENCHANT_ARMOR_S;
		}
		else if (chance < 925)
		{
			reward = ATTRIBUTE_CRYSTALS[getRandom(ATTRIBUTE_CRYSTALS.length)];
		}
		else if (chance < 1100)
		{
			reward = SCROLL_ENCHANT_WEAPON_S;
		}
		else
		{
			reward = GEMSTONE_S;
			count = 3;
		}
		giveItems(player, reward, count);
		L2Item item = ItemHolder.getInstance().getTemplate(reward);
		NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.S1_RECEIVED_A_S2_ITEM_AS_A_REWARD_FROM_THE_SEPARATED_SOUL);
		packet.addStringParameter(player.getName());
		packet.addStringParameter(item.getName());
		npc.broadcastPacket(packet);
	}
}
