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
package l2e.scripts.clanhallsiege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javolution.util.FastList;

import org.apache.commons.lang.ArrayUtils;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2SiegeClan;
import l2e.gameserver.model.L2SiegeClan.SiegeClanType;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ChestInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.model.entity.clanhall.SiegeStatus;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;
import l2e.util.TimeUtils;

/**
 * Created by LordWinter 01.06.2013 Based on L2J Eternity-World
 */
public final class RainbowSpringsSiege extends ClanHallSiegeEngine
{
	private static final String qn = "RainbowSpringsSiege";

	private static final int WAR_DECREES = 8034;
	private static final int RAINBOW_NECTAR = 8030;
	private static final int RAINBOW_MWATER = 8031;
	private static final int RAINBOW_WATER = 8032;
	private static final int RAINBOW_SULFUR = 8033;

	private static final int MESSENGER = 35604;
	private static final int CARETAKER = 35603;
	private static final int CHEST = 35593;
	private static final int ENRAGED_YETI = 35592;

	protected static Map<Integer, Long> _warDecreesCount = new HashMap<>();
	protected static List<L2Clan> _acceptedClans = new ArrayList<>(4);
	protected ArrayList<Integer> _playersOnArena = new ArrayList<>();
	protected final FastList<L2Npc> chests = new FastList<>();

	private static final int ItemA = 8035;
	private static final int ItemB = 8036;
	private static final int ItemC = 8037;
	private static final int ItemD = 8038;
	private static final int ItemE = 8039;
	private static final int ItemF = 8040;
	private static final int ItemG = 8041;
	private static final int ItemH = 8042;
	private static final int ItemI = 8043;
	private static final int ItemK = 8045;
	private static final int ItemL = 8046;
	private static final int ItemN = 8047;
	private static final int ItemO = 8048;
	private static final int ItemP = 8049;
	private static final int ItemR = 8050;
	private static final int ItemS = 8051;
	private static final int ItemT = 8052;
	private static final int ItemU = 8053;
	private static final int ItemW = 8054;
	private static final int ItemY = 8055;

	protected static int _generated;
	protected Future<?> _task = null;
	protected Future<?> _chesttask = null;
	private L2Clan _winner;

	protected static final Word[] WORLD_LIST = new Word[8];

	private static class Word
	{
		private final String _name;
		private final int[][] _items;

		public Word(String name, int[]... items)
		{
			_name = name;
			_items = items;
		}

		public String getName()
		{
			return _name;
		}

		public int[][] getItems()
		{
			return _items;
		}
	}

	private static final int[] GOURDS =
	{
	                35588,
	                35589,
	                35590,
	                35591
	};

	private static L2Spawn[] _gourds = new L2Spawn[4];
	private static L2Npc[] _yetis = new L2Npc[4];
	protected L2Npc _chest1;
	protected L2Npc _chest2;
	protected L2Npc _chest3;
	protected L2Npc _chest4;

	private static final int[] YETIS =
	{
	                35596,
	                35597,
	                35598,
	                35599
	};

	private static final int[][] ARENAS =
	{
	                {
	                                151562,
	                                -127080,
	                                -2214
	                },
	                {
	                                153141,
	                                -125335,
	                                -2214
	                },
	                {
	                                153892,
	                                -127530,
	                                -2214
	                },
	                {
	                                155657,
	                                -125752,
	                                -2214
	                },
	};

	private static final int[][] YETIS_SPAWN =
	{
	                {
	                                151560,
	                                -127075,
	                                -2221
	                },
	                {
	                                153129,
	                                -125337,
	                                -2221
	                },
	                {
	                                153884,
	                                -127534,
	                                -2221
	                },
	                {
	                                156657,
	                                -125753,
	                                -2221
	                },
	};

	protected static final int[][] CHESTS_SPAWN =
	{
	                {
	                                151560,
	                                -127075,
	                                -2221
	                },
	                {
	                                153129,
	                                -125337,
	                                -2221
	                },
	                {
	                                153884,
	                                -127534,
	                                -2221
	                },
	                {
	                                155657,
	                                -125753,
	                                -2221
	                },

	};

	protected final int[] arenaChestsCnt =
	{
	                0,
	                0,
	                0,
	                0
	};

	private static final L2Skill[] DEBUFFS =
	{
		        SkillHolder.getInstance().getInfo(4991, 1)
	};

	static
	{
		WORLD_LIST[0] = new Word("BABYDUCK", new int[]
		{
		                ItemB,
		                2
		}, new int[]
		{
		                ItemA,
		                1
		}, new int[]
		{
		                ItemY,
		                1
		}, new int[]
		{
		                ItemD,
		                1
		}, new int[]
		{
		                ItemU,
		                1
		}, new int[]
		{
		                ItemC,
		                1
		}, new int[]
		{
		                ItemK,
		                1
		});
		WORLD_LIST[1] = new Word("ALBATROS", new int[]
		{
		                ItemA,
		                2
		}, new int[]
		{
		                ItemL,
		                1
		}, new int[]
		{
		                ItemB,
		                1
		}, new int[]
		{
		                ItemT,
		                1
		}, new int[]
		{
		                ItemR,
		                1
		}, new int[]
		{
		                ItemO,
		                1
		}, new int[]
		{
		                ItemS,
		                1
		});
		WORLD_LIST[2] = new Word("PELICAN", new int[]
		{
		                ItemP,
		                1
		}, new int[]
		{
		                ItemE,
		                1
		}, new int[]
		{
		                ItemL,
		                1
		}, new int[]
		{
		                ItemI,
		                1
		}, new int[]
		{
		                ItemC,
		                1
		}, new int[]
		{
		                ItemA,
		                1
		}, new int[]
		{
		                ItemN,
		                1
		});
		WORLD_LIST[3] = new Word("KINGFISHER", new int[]
		{
		                ItemK,
		                1
		}, new int[]
		{
		                ItemI,
		                1
		}, new int[]
		{
		                ItemN,
		                1
		}, new int[]
		{
		                ItemG,
		                1
		}, new int[]
		{
		                ItemF,
		                1
		}, new int[]
		{
		                ItemI,
		                1
		}, new int[]
		{
		                ItemS,
		                1
		}, new int[]
		{
		                ItemH,
		                1
		}, new int[]
		{
		                ItemE,
		                1
		}, new int[]
		{
		                ItemR,
		                1
		});
		WORLD_LIST[4] = new Word("CYGNUS", new int[]
		{
		                ItemC,
		                1
		}, new int[]
		{
		                ItemY,
		                1
		}, new int[]
		{
		                ItemG,
		                1
		}, new int[]
		{
		                ItemN,
		                1
		}, new int[]
		{
		                ItemU,
		                1
		}, new int[]
		{
		                ItemS,
		                1
		});
		WORLD_LIST[5] = new Word("TRITON", new int[]
		{
		                ItemT,
		                2
		}, new int[]
		{
		                ItemR,
		                1
		}, new int[]
		{
		                ItemI,
		                1
		}, new int[]
		{
		                ItemN,
		                1
		});
		WORLD_LIST[6] = new Word("RAINBOW", new int[]
		{
		                ItemR,
		                1
		}, new int[]
		{
		                ItemA,
		                1
		}, new int[]
		{
		                ItemI,
		                1
		}, new int[]
		{
		                ItemN,
		                1
		}, new int[]
		{
		                ItemB,
		                1
		}, new int[]
		{
		                ItemO,
		                1
		}, new int[]
		{
		                ItemW,
		                1
		});
		WORLD_LIST[7] = new Word("SPRING", new int[]
		{
		                ItemS,
		                1
		}, new int[]
		{
		                ItemP,
		                1
		}, new int[]
		{
		                ItemR,
		                1
		}, new int[]
		{
		                ItemI,
		                1
		}, new int[]
		{
		                ItemN,
		                1
		}, new int[]
		{
		                ItemG,
		                1
		});
	}

	public RainbowSpringsSiege(int questId, String name, String descr, int hallId)
	{
		super(questId, name, descr, hallId);

		addFirstTalkId(MESSENGER);
		addFirstTalkId(CARETAKER);
		addFirstTalkId(YETIS);
		addTalkId(MESSENGER);
		addTalkId(CARETAKER);
		addTalkId(YETIS);

		for (int squashes : GOURDS)
		{
			addSpawnId(squashes);
			addKillId(squashes);
		}
		addSpawnId(ENRAGED_YETI);

		addSkillSeeId(YETIS);

		addKillId(CHEST);

		_generated = -1;
		_winner = ClanHolder.getInstance().getClan(_hall.getOwnerId());
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(qn) == null)
		{
			newQuestState(player);
		}

		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		final int npcId = npc.getId();

		if (npcId == MESSENGER)
		{
			final String main = (_hall.getOwnerId() > 0) ? "35604-01.htm" : "35604-00.htm";
			html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/" + main);
			html.replace("%nextSiege%", TimeUtils.toSimpleFormat(_hall.getSiegeDate()));
			if (_hall.getOwnerId() > 0)
			{
				html.replace("%owner%", ClanHolder.getInstance().getClan(_hall.getOwnerId()).getName());
			}
			player.sendPacket(html);
		}
		else if (npcId == CARETAKER)
		{
			final String main = (_hall.isInSiege() || !_hall.isWaitingBattle()) ? "35603-00.htm" : "35603-01.htm";
			html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/" + main);
			player.sendPacket(html);
		}
		else if (Util.contains(YETIS, npcId))
		{
			L2Clan clan = player.getClan();
			if (_acceptedClans.contains(clan))
			{
				int index = _acceptedClans.indexOf(clan);
				if (npcId == YETIS[index])
				{
					if (!player.isClanLeader())
					{
						html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35596-00.htm");
					}
					else
					{
						html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35596-01.htm");
					}
				}
				else
				{
					html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35596-06.htm");
				}
			}
			else
			{
				html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35596-06.htm");
			}
			player.sendPacket(html);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return "";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		L2Clan clan = player.getClan();

		switch (npc.getId())
		{
			case MESSENGER:
				switch (event)
				{
					case "Register":
						if (!player.isClanLeader())
						{
							htmltext = "35604-07.htm";
						}
						else if ((clan.getCastleId() > 0) || (clan.getFortId() > 0) || (clan.getHideoutId() > 0))
						{
							htmltext = "35604-09.htm";
						}
						else if (!_hall.isRegistering())
						{
							htmltext = "35604-11.htm";
						}
						else if (_warDecreesCount.containsKey(clan.getId()))
						{
							htmltext = "35604-10.htm";
						}
						else if (getAttackers().size() >= 4)
						{
							htmltext = "35604-18.htm";
						}
						else if ((clan.getLevel() < 3) || (clan.getMembersCount() < 5))
						{
							htmltext = "35604-08.htm";
						}
						else
						{
							final L2ItemInstance warDecrees = player.getInventory().getItemByItemId(WAR_DECREES);
							if (warDecrees == null)
							{
								htmltext = "35604-05.htm";
							}
							else
							{
								long count = warDecrees.getCount();
								_warDecreesCount.put(clan.getId(), count);
								player.destroyItem("Rainbow Springs Registration", warDecrees, npc, true);
								registerClan(clan, count, true);
								htmltext = "35604-06.htm";
							}
						}
						break;
					case "Cancel":
						if (!player.isClanLeader())
						{
							htmltext = "35604-08.htm";
						}
						else if (!_warDecreesCount.containsKey(clan.getId()))
						{
							htmltext = "35604-12.htm";
						}
						else if (!_hall.isRegistering())
						{
							htmltext = "35604-13.htm";
						}
						else
						{
							registerClan(clan, 0, false);
							htmltext = "35604-17.htm";
						}
						break;
					case "Unregister":
						if (_hall.isRegistering())
						{
							if (_warDecreesCount.containsKey(clan.getId()))
							{
								player.addItem("Rainbow Spring unregister", WAR_DECREES, _warDecreesCount.get(clan.getId()) / 2, npc, true);
								_warDecreesCount.remove(clan.getId());
								htmltext = "35604-14.htm";
							}
							else
							{
								htmltext = "35604-16.htm";
							}
						}
						else if (_hall.isWaitingBattle())
						{
							_acceptedClans.remove(clan);
							htmltext = "35604-16.htm";
						}
						break;
				}
				break;
			case CARETAKER:
				switch (event)
				{
					case "GoToArena":
						final L2Party party = player.getParty();
						if (clan == null)
						{
							htmltext = "35603-07.htm";
						}
						else if (!player.isClanLeader())
						{
							htmltext = "35603-02.htm";
						}
						else if (!player.isInParty())
						{
							htmltext = "35603-03.htm";
						}
						else if (party.getLeaderObjectId() != player.getObjectId())
						{
							htmltext = "35603-04.htm";
						}
						else
						{
							final int clanId = player.getId();
							boolean nonClanMemberInParty = false;
							for (L2PcInstance member : party.getMembers())
							{
								if (member.getId() != clanId)
								{
									nonClanMemberInParty = true;
									break;
								}
							}

							if (nonClanMemberInParty)
							{
								htmltext = "35603-05.htm";
							}
							else if (party.getMemberCount() < 5)
							{
								htmltext = "35603-06.htm";
							}
							if ((clan.getCastleId() > 0) || (clan.getFortId() > 0) || (clan.getHideoutId() > 0))
							{
								htmltext = "35603-08.htm";
							}
							else if (clan.getLevel() < Config.CHS_CLAN_MINLEVEL)
							{
								htmltext = "35603-09.htm";
							}
							else if (!_acceptedClans.contains(clan))
							{
								htmltext = "35603-10.htm";
							}
							else
							{
								portToArena(player, _acceptedClans.indexOf(clan));
							}
							return null;
						}
						break;
				}
				break;
		}

		if (event.startsWith("getItem"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			boolean has = true;
			if (_generated == -1)
			{
				has = false;
			}
			else
			{
				Word word = WORLD_LIST[_generated];

				if (_generated == 0)
				{
					if ((player.getInventory().getInventoryItemCount(ItemB, -1) >= 2) && (player.getInventory().getInventoryItemCount(ItemA, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemY, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemD, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemU, -1) >= 1)
					                && (player.getInventory().getInventoryItemCount(ItemC, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemK, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (_generated == 1)
				{
					if ((player.getInventory().getInventoryItemCount(ItemA, -1) >= 2) && (player.getInventory().getInventoryItemCount(ItemL, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemB, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemT, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemR, -1) >= 1)
					                && (player.getInventory().getInventoryItemCount(ItemO, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemS, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (_generated == 2)
				{
					if ((player.getInventory().getInventoryItemCount(ItemP, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemE, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemL, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemI, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemC, -1) >= 1)
					                && (player.getInventory().getInventoryItemCount(ItemA, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemN, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (_generated == 3)
				{
					if ((player.getInventory().getInventoryItemCount(ItemK, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemI, -1) >= 2) && (player.getInventory().getInventoryItemCount(ItemN, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemG, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemF, -1) >= 1)
					                && (player.getInventory().getInventoryItemCount(ItemS, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemH, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemE, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemR, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (_generated == 4)
				{
					if ((player.getInventory().getInventoryItemCount(ItemC, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemY, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemG, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemN, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemU, -1) >= 1)
					                && (player.getInventory().getInventoryItemCount(ItemS, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (_generated == 5)
				{
					if ((player.getInventory().getInventoryItemCount(ItemT, -1) >= 2) && (player.getInventory().getInventoryItemCount(ItemR, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemI, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemO, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemN, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (_generated == 6)
				{
					if ((player.getInventory().getInventoryItemCount(ItemR, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemA, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemI, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemN, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemB, -1) >= 1)
					                && (player.getInventory().getInventoryItemCount(ItemO, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemW, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (_generated == 7)
				{
					if ((player.getInventory().getInventoryItemCount(ItemS, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemP, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemR, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemI, -1) >= 1) && (player.getInventory().getInventoryItemCount(ItemN, -1) >= 1)
					                && (player.getInventory().getInventoryItemCount(ItemG, -1) >= 1))
					{
						has = true;
					}
					else
					{
						has = false;
					}
				}

				if (has)
				{
					for (int[] itemInfo : word.getItems())
					{
						player.destroyItemByItemId("Rainbow Item", itemInfo[0], itemInfo[1], player, true);
					}

					int rnd = Rnd.get(100);
					if ((_generated >= 0) && (_generated <= 5))
					{
						if (rnd < 70)
						{
							addItem(player, RAINBOW_NECTAR);
						}
						else if (rnd < 80)
						{
							addItem(player, RAINBOW_MWATER);
						}
						else if (rnd < 90)
						{
							addItem(player, RAINBOW_WATER);
						}
						else
						{
							addItem(player, RAINBOW_SULFUR);
						}
					}
					else
					{
						if (rnd < 10)
						{
							addItem(player, RAINBOW_NECTAR);
						}
						else if (rnd < 40)
						{
							addItem(player, RAINBOW_MWATER);
						}
						else if (rnd < 70)
						{
							addItem(player, RAINBOW_WATER);
						}
						else
						{
							addItem(player, RAINBOW_SULFUR);
						}
					}
				}

				if (!has)
				{
					html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35596-02.htm");
				}
				else
				{
					html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35596-04.htm");
				}
				player.sendPacket(html);
			}
			return null;
		}
		else if (event.startsWith("seeItem"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35596-05.htm");
			if (_generated == -1)
			{
				html.replace("%word%", "<fstring>" + NpcStringId.UNDECIDED + "</fstring>");
			}
			else
			{
				html.replace("%word%", WORLD_LIST[_generated].getName());
			}
			player.sendPacket(html);
			return null;
		}
		return htmltext;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		final L2Clan clan = caster.getClan();

		if ((clan == null) || !_acceptedClans.contains(clan))
		{
			return null;
		}

		int index = _acceptedClans.indexOf(clan);
		int warIndex = Integer.MIN_VALUE;

		if (Util.contains(targets, npc))
		{
			if (npc.isInsideRadius(caster, 60, false, false))
			{
				switch (skill.getId())
				{
					case 2240:
						if (getRandom(100) < 10)
						{
							addSpawn(ENRAGED_YETI, caster.getX() + 10, caster.getY() + 10, caster.getZ(), 0, false, 0, false);
						}
						reduceGourdHp(index, caster);
						break;
					case 2241:
						warIndex = rndEx(_acceptedClans.size(), index);
						if (warIndex == Integer.MIN_VALUE)
						{
							return null;
						}
						increaseGourdHp(warIndex);
						break;
					case 2242:
						warIndex = rndEx(_acceptedClans.size(), index);
						if (warIndex == Integer.MIN_VALUE)
						{
							return null;
						}
						moveGourds(warIndex);
						break;
					case 2243:
						warIndex = rndEx(_acceptedClans.size(), index);
						if (warIndex == Integer.MIN_VALUE)
						{
							return null;
						}
						castDebuffsOnEnemies(caster, warIndex);
						break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, final L2PcInstance killer, boolean isSummon)
	{
		final L2Clan clan = killer.getClan();
		int index = _acceptedClans.indexOf(clan);

		if ((clan == null) || !_acceptedClans.contains(clan))
		{
			return null;
		}

		if (npc.getId() == CHEST)
		{
			chestDie(npc);
			if (chests.contains(npc))
			{
				chests.remove(npc);
			}

			int chance = Rnd.get(100);
			if (chance <= 5)
			{
				((L2ChestInstance) npc).dropItem(killer, ItemA, 1);
			}
			else if ((chance > 5) && (chance <= 10))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemB, 1);
			}
			else if ((chance > 10) && (chance <= 15))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemC, 1);
			}
			else if ((chance > 15) && (chance <= 20))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemD, 1);
			}
			else if ((chance > 20) && (chance <= 25))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemE, 1);
			}
			else if ((chance > 25) && (chance <= 30))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemF, 1);
			}
			else if ((chance > 30) && (chance <= 35))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemG, 1);
			}
			else if ((chance > 35) && (chance <= 40))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemH, 1);
			}
			else if ((chance > 40) && (chance <= 45))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemI, 1);
			}
			else if ((chance > 45) && (chance <= 50))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemK, 1);
			}
			else if ((chance > 50) && (chance <= 55))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemL, 1);
			}
			else if ((chance > 55) && (chance <= 60))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemN, 1);
			}
			else if ((chance > 60) && (chance <= 65))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemO, 1);
			}
			else if ((chance > 65) && (chance <= 70))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemP, 1);
			}
			else if ((chance > 70) && (chance <= 75))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemR, 1);
			}
			else if ((chance > 75) && (chance <= 80))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemS, 1);
			}
			else if ((chance > 80) && (chance <= 85))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemT, 1);
			}
			else if ((chance > 85) && (chance <= 90))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemU, 1);
			}
			else if ((chance > 90) && (chance <= 95))
			{
				((L2ChestInstance) npc).dropItem(killer, ItemW, 1);
			}
			else if (chance > 95)
			{
				((L2ChestInstance) npc).dropItem(killer, ItemY, 1);
			}
		}

		if (npc.getId() == GOURDS[index])
		{
			_missionAccomplished = true;
			_winner = ClanHolder.getInstance().getClan(clan.getId());

			synchronized (this)
			{
				cancelSiegeTask();
				endSiege();

				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						for (int id : _playersOnArena)
						{
							L2PcInstance pl = L2World.getInstance().findPlayer(id);
							if (pl != null)
							{
								pl.teleToLocation(TeleportWhereType.TOWN);
							}
						}
						_playersOnArena = new ArrayList<>();
					}
				}, 120 * 1000);
			}
		}
		return null;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (npc.getId() == ENRAGED_YETI)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.SHOUT, npc.getName(), NpcStringId.OOOH_WHO_POURED_NECTAR_ON_MY_HEAD_WHILE_I_WAS_SLEEPING));
		}

		if (ArrayUtils.contains(GOURDS, npc.getId()))
		{
			npc.setIsParalyzed(true);
		}
		return super.onSpawn(npc);
	}

	@Override
	public void startSiege()
	{
		if (_acceptedClans.size() < 2)
		{
			onSiegeEnds();
			_acceptedClans.clear();
			_hall.updateNextSiege();
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getName());
			Announcements.getInstance().announceToAll(sm);
			return;
		}
		spawnGourds();
		spawnYetis();
	}

	@Override
	public void prepareOwner()
	{
		if (_hall.getOwnerId() > 0)
		{
			registerClan(ClanHolder.getInstance().getClan(_hall.getOwnerId()), 10000, true);
		}
		_hall.banishForeigners();
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(_hall.getName());
		Announcements.getInstance().announceToAll(msg);
		_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), 3600000);
	}

	@Override
	public void endSiege()
	{
		if (_hall.getOwnerId() > 0)
		{
			L2Clan clan = ClanHolder.getInstance().getClan(_hall.getOwnerId());
			clan.setHideoutId(0);
			_hall.free();
		}
		super.endSiege();
	}

	@Override
	public void onSiegeEnds()
	{
		unSpawnGourds();
		unSpawnYetis();
		unSpawnChests();
		clearTables();
	}

	protected void portToArena(L2PcInstance leader, int arena)
	{
		if ((arena < 0) || (arena > 3))
		{
			_log.warning(qn + ": Wrong arena id passed: " + arena);
			return;
		}
		for (L2PcInstance pc : leader.getParty().getMembers())
		{
			if (pc != null)
			{
				pc.stopAllEffects();
				if (pc.hasSummon())
				{
					pc.getSummon().unSummon(pc);
				}
				_playersOnArena.add(pc.getObjectId());
				pc.teleToLocation(ARENAS[arena][0], ARENAS[arena][1], ARENAS[arena][2]);
			}
		}
	}

	protected void spawnYetis()
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			if (_yetis[i] == null)
			{
				try
				{
					_yetis[i] = addSpawn(YETIS[i], YETIS_SPAWN[i][0], YETIS_SPAWN[i][1], YETIS_SPAWN[i][2], 0, false, 0, false);
					_yetis[i].setHeading(1);
					_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GenerateTask(_yetis[i]), 10000, 300000);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	protected void spawnGourds()
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			if (_gourds[i] == null)
			{
				try
				{
					_gourds[i] = new L2Spawn(NpcTable.getInstance().getTemplate(GOURDS[i]));
					_gourds[i].setX(ARENAS[i][0] + 150);
					_gourds[i].setY(ARENAS[i][1] + 150);
					_gourds[i].setZ(ARENAS[i][2]);
					_gourds[i].setHeading(1);
					_gourds[i].setAmount(1);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			SpawnTable.getInstance().addNewSpawn(_gourds[i], false);
			_gourds[i].init();
		}
		_chesttask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ChestsSpawn(), 5000, 5000);
	}

	protected void unSpawnYetis()
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			_yetis[i].deleteMe();
			if (_task != null)
			{
				_task.cancel(false);
				_task = null;
			}
		}
	}

	protected void unSpawnGourds()
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			_gourds[i].getLastSpawn().deleteMe();
			SpawnTable.getInstance().deleteSpawn(_gourds[i], false);
		}
	}

	protected void unSpawnChests()
	{
		if (!chests.isEmpty())
		{
			for (L2Npc chest : chests)
			{
				if (chest != null)
				{
					chest.deleteMe();
					if (_chesttask != null)
					{
						_chesttask.cancel(false);
						_chesttask = null;
					}
				}
			}
		}
	}

	private static void moveGourds(int index)
	{
		L2Spawn[] tempArray = _gourds;
		for (int i = 0; i < index; i++)
		{
			L2Spawn oldSpawn = _gourds[(index - 1) - i];
			L2Spawn curSpawn = tempArray[i];

			_gourds[(index - 1) - i] = curSpawn;

			int newX = oldSpawn.getX();
			int newY = oldSpawn.getY();
			int newZ = oldSpawn.getZ();

			curSpawn.getLastSpawn().teleToLocation(newX, newY, newZ);
		}
	}

	private static void reduceGourdHp(int index, L2PcInstance player)
	{
		L2Spawn gourd = _gourds[index];
		gourd.getLastSpawn().reduceCurrentHp(1000, player, null);
	}

	private static void increaseGourdHp(int index)
	{
		L2Spawn gourd = _gourds[index];
		L2Npc gourdNpc = gourd.getLastSpawn();
		gourdNpc.setCurrentHp(gourdNpc.getCurrentHp() + 1000);
	}

	private void castDebuffsOnEnemies(L2PcInstance player, int myArena)
	{
		if (_acceptedClans.contains(player.getClan()))
		{
			int index = _acceptedClans.indexOf(player.getClan());

			if (_playersOnArena.contains(player.getObjectId()))
			{
				for (L2PcInstance pl : player.getParty().getMembers())
				{
					if (index != myArena)
					{
						continue;
					}

					if (pl != null)
					{
						for (L2Skill sk : DEBUFFS)
						{
							sk.getEffects(pl, pl);
						}
					}
				}
			}
		}
	}

	private void registerClan(L2Clan clan, long count, boolean register)
	{
		if (register)
		{
			L2SiegeClan sc = new L2SiegeClan(clan.getId(), SiegeClanType.ATTACKER);
			getAttackers().put(clan.getId(), sc);

			int spotLeft = 4;

			for (int i = 0; i < spotLeft; i++)
			{
				long counter = 0;
				L2Clan fightclan = null;
				for (int clanId : _warDecreesCount.keySet())
				{
					L2Clan actingClan = ClanHolder.getInstance().getClan(clanId);
					if ((actingClan == null) || (actingClan.getDissolvingExpiryTime() > 0))
					{
						_warDecreesCount.remove(clanId);
						continue;
					}

					final long counts = _warDecreesCount.get(clanId);
					if (counts > counter)
					{
						counter = counts;
						fightclan = actingClan;
					}
				}
				if ((fightclan != null) && (_acceptedClans.size() < 4))
				{
					_acceptedClans.add(clan);
				}
			}
			updateAttacker(clan.getId(), count, false);
		}
		else
		{
			updateAttacker(clan.getId(), 0, true);
		}
	}

	private static void updateAttacker(int clanId, long count, boolean remove)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			if (remove)
			{
				statement = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list WHERE clanId = ?");
				statement.setInt(1, clanId);
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO rainbowsprings_attacker_list VALUES (?,?)");
				statement.setInt(1, clanId);
				statement.setLong(2, count);
			}
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public final void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM rainbowsprings_attacker_list");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int clanId = rset.getInt("clanId");
				long count = rset.getLong("war_decrees_count");
				_warDecreesCount.put(clanId, count);

				for (int clan : _warDecreesCount.keySet())
				{
					L2Clan loadClan = ClanHolder.getInstance().getClan(clan);
					_acceptedClans.add(loadClan);
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(qn + ".loadAttackers()->" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void clearTables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stat1 = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list");
			stat1.execute();
			stat1.close();
		}
		catch (Exception e)
		{
			_log.warning(qn + ".clearTables()->" + e.getMessage());
		}
	}

	protected final class ChestsSpawn implements Runnable
	{
		@Override
		public void run()
		{
			for (int i = 0; i < _acceptedClans.size(); i++)
			{
				if (arenaChestsCnt[i] < 4)
				{
					L2Npc chest = addSpawn(CHEST, CHESTS_SPAWN[i][0] + getRandom(-400, 400), CHESTS_SPAWN[i][1] + getRandom(-400, 400), CHESTS_SPAWN[i][2], 0, false, 0, false);
					if (chest != null)
					{
						chests.add(chest);
					}
					arenaChestsCnt[i]++;
				}

				if (arenaChestsCnt[i] < 4)
				{
					L2Npc chest = addSpawn(CHEST, CHESTS_SPAWN[i][0] + getRandom(-400, 400), CHESTS_SPAWN[i][1] + getRandom(-400, 400), CHESTS_SPAWN[i][2], 0, false, 0, false);
					if (chest != null)
					{
						chests.add(chest);
					}
					arenaChestsCnt[i]++;
				}

				if (arenaChestsCnt[i] < 4)
				{
					L2Npc chest = addSpawn(CHEST, CHESTS_SPAWN[i][0] + getRandom(-400, 400), CHESTS_SPAWN[i][1] + getRandom(-400, 400), CHESTS_SPAWN[i][2], 0, false, 0, false);
					if (chest != null)
					{
						chests.add(chest);
					}
					arenaChestsCnt[i]++;
				}

				if (arenaChestsCnt[i] < 4)
				{
					L2Npc chest = addSpawn(CHEST, CHESTS_SPAWN[i][0] + getRandom(-400, 400), CHESTS_SPAWN[i][1] + getRandom(-400, 400), CHESTS_SPAWN[i][2], 0, false, 0, false);
					if (chest != null)
					{
						chests.add(chest);
					}
					arenaChestsCnt[i]++;
				}
			}
		}
	}

	private void addItem(L2PcInstance player, int itemId)
	{
		player.getInventory().addItem("Rainbow Item", itemId, 1, player, null);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
		sm.addItemName(itemId);
		player.sendPacket(sm);
	}

	protected final class GenerateTask implements Runnable
	{
		protected final L2Npc _npc;

		protected GenerateTask(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			_generated = getRandom(WORLD_LIST.length);
			Word word = WORLD_LIST[_generated];

			ExShowScreenMessage msg = new ExShowScreenMessage(word.getName(), 5000);
			int region = MapRegionManager.getInstance().getMapRegionLocId(_npc.getX(), _npc.getY());
			for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
			{
				if (region == MapRegionManager.getInstance().getMapRegionLocId(player.getX(), player.getY()))
				{
					if (Util.checkIfInRange(750, _npc, player, false))
					{
						player.sendPacket(msg);
					}
				}
			}
		}
	}

	protected void chestDie(L2Npc npc)
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			arenaChestsCnt[i]--;
		}
	}

	private int rndEx(int size, int ex)
	{
		int rnd = Integer.MIN_VALUE;
		for (int i = 0; i < Byte.MAX_VALUE; i++)
		{
			rnd = Rnd.get(size);
			if (rnd != ex)
			{
				break;
			}
		}
		return rnd;
	}

	@Override
	public L2Clan getWinner()
	{
		return _winner;
	}

	public static void main(String[] args)
	{
		new RainbowSpringsSiege(-1, qn, "clanhallsiege", RAINBOW_SPRINGS);
	}
}
