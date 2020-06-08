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
package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.cache.CrestCache;
import l2e.gameserver.communitybbs.BB.Forum;
import l2e.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TerritoryWarManager.Territory;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.itemcontainer.ClanWarehouse;
import l2e.gameserver.model.itemcontainer.ItemContainer;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ExSubPledgeSkillAdd;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.PledgeSkillList;
import l2e.gameserver.network.serverpackets.PledgeSkillList.SubPledgeSkill;
import l2e.gameserver.network.serverpackets.PledgeSkillListAdd;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.scripting.scriptengine.events.ClanCreationEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanJoinEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanLeaderChangeEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanLeaveEvent;
import l2e.gameserver.scripting.scriptengine.events.ClanLevelUpEvent;
import l2e.gameserver.scripting.scriptengine.listeners.clan.ClanCreationListener;
import l2e.gameserver.scripting.scriptengine.listeners.clan.ClanMembershipListener;
import l2e.gameserver.util.Util;

public class L2Clan implements IIdentifiable
{
	private static final Logger _log = Logger.getLogger(L2Clan.class.getName());
	
	private static final String INSERT_CLAN_DATA = "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,blood_alliance_count,blood_oath_count,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,new_leader_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String SELECT_CLAN_DATA = "SELECT * FROM clan_data where clan_id=?";
	
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	
	public static final int CP_NOTHING = 0;
	public static final int CP_CL_JOIN_CLAN = 2;
	public static final int CP_CL_GIVE_TITLE = 4;
	public static final int CP_CL_VIEW_WAREHOUSE = 8;
	public static final int CP_CL_MANAGE_RANKS = 16;
	public static final int CP_CL_PLEDGE_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	public static final int CP_CL_REGISTER_CREST = 128;
	public static final int CP_CL_APPRENTICE = 256;
	public static final int CP_CL_TROOPS_FAME = 512;
	public static final int CP_CL_SUMMON_AIRSHIP = 1024;
	public static final int CP_CH_OPEN_DOOR = 2048;
	public static final int CP_CH_OTHER_RIGHTS = 4096;
	public static final int CP_CH_AUCTION = 8192;
	public static final int CP_CH_DISMISS = 16384;
	public static final int CP_CH_SET_FUNCTIONS = 32768;
	public static final int CP_CS_OPEN_DOOR = 65536;
	public static final int CP_CS_MANOR_ADMIN = 131072;
	public static final int CP_CS_MANAGE_SIEGE = 262144;
	public static final int CP_CS_USE_FUNCTIONS = 524288;
	public static final int CP_CS_DISMISS = 1048576;
	public static final int CP_CS_TAXES = 2097152;
	public static final int CP_CS_MERCENARIES = 4194304;
	public static final int CP_CS_SET_FUNCTIONS = 8388608;
	public static final int CP_ALL = 16777214;
	
	public static final int SUBUNIT_ACADEMY = -1;
	public static final int SUBUNIT_ROYAL1 = 100;
	public static final int SUBUNIT_ROYAL2 = 200;
	public static final int SUBUNIT_KNIGHT1 = 1001;
	public static final int SUBUNIT_KNIGHT2 = 1002;
	public static final int SUBUNIT_KNIGHT3 = 2001;
	public static final int SUBUNIT_KNIGHT4 = 2002;
	
	private static List<ClanCreationListener> clanCreationListeners = new FastList<ClanCreationListener>().shared();
	private static List<ClanMembershipListener> clanMembershipListeners = new FastList<ClanMembershipListener>().shared();
	
	private String _name;
	private int _clanId;
	private L2ClanMember _leader;
	private final Map<Integer, L2ClanMember> _members = new FastMap<>();
	
	private String _allyName;
	private int _allyId;
	private int _level;
	private int _castleId;
	private int _fortId;
	private int _hideoutId;
	private int _hiredGuards;
	private int _crestId;
	private int _crestLargeId;
	private int _allyCrestId;
	private int _auctionBiddedAt = 0;
	private long _allyPenaltyExpiryTime;
	private int _allyPenaltyType;
	private long _charPenaltyExpiryTime;
	private long _dissolvingExpiryTime;
	private int _bloodAllianceCount;
	private int _bloodOathCount;
	
	private final ItemContainer _warehouse = new ClanWarehouse(this);
	private final List<Integer> _atWarWith = new FastList<>();
	private final List<Integer> _atWarAttackers = new FastList<>();
	
	private Forum _forum;
	
	private final Map<Integer, L2Skill> _skills = new FastMap<>();
	private final Map<Integer, RankPrivs> _privs = new FastMap<>();
	private final Map<Integer, SubPledge> _subPledges = new FastMap<>();
	private final Map<Integer, L2Skill> _subPledgeSkills = new FastMap<>();
	
	private int _reputationScore = 0;
	private int _rank = 0;
	
	private String _notice;
	private boolean _noticeEnabled = false;
	private static final int MAX_NOTICE_LENGTH = 8192;
	private int _newLeaderId;
	
	private AtomicInteger _siegeKills;
	private AtomicInteger _siegeDeaths;
	
	public L2Clan(int clanId)
	{
		_clanId = clanId;
		initializePrivs();
		restore();
		getWarehouse().restore();
	}
	
	public L2Clan(int clanId, String clanName)
	{
		_clanId = clanId;
		_name = clanName;
		initializePrivs();
		fireClanCreationListeners();
	}
	
	@Override
	public int getId()
	{
		return _clanId;
	}
	
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	public int getLeaderId()
	{
		return (_leader != null ? _leader.getObjectId() : 0);
	}
	
	public L2ClanMember getLeader()
	{
		return _leader;
	}
	
	public void setLeader(L2ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getObjectId(), leader);
	}
	
	public void setNewLeader(L2ClanMember member)
	{
		final L2PcInstance newLeader = member.getPlayerInstance();
		final L2ClanMember exMember = getLeader();
		final L2PcInstance exLeader = exMember.getPlayerInstance();
		
		if (!fireClanLeaderChangeListeners(newLeader, exLeader))
		{
			return;
		}
		
		if (exLeader != null)
		{
			if (exLeader.isFlying())
			{
				exLeader.dismount();
			}
			
			if (getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
			{
				SiegeManager.getInstance().removeSiegeSkills(exLeader);
			}
			exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
			exLeader.broadcastUserInfo();
			
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?"))
			{
				statement.setInt(1, L2Clan.CP_NOTHING);
				statement.setInt(2, getLeaderId());
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Couldn't update clan privs for old clan leader", e);
			}
		}
		
		setLeader(member);
		if (getNewLeaderId() != 0)
		{
			setNewLeaderId(0, true);
		}
		updateClanInDB();
		
		if (exLeader != null)
		{
			exLeader.setPledgeClass(L2ClanMember.calculatePledgeClass(exLeader));
			exLeader.broadcastUserInfo();
			exLeader.checkItemRestriction();
		}
		
		if (newLeader != null)
		{
			newLeader.setPledgeClass(L2ClanMember.calculatePledgeClass(newLeader));
			newLeader.setClanPrivileges(L2Clan.CP_ALL);
			
			if (getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
			{
				SiegeManager.getInstance().addSiegeSkills(newLeader);
			}
			newLeader.broadcastUserInfo();
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?"))
			{
				statement.setInt(1, L2Clan.CP_ALL);
				statement.setInt(2, getLeaderId());
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Couldn't update clan privs for new clan leader", e);
			}
		}
		
		broadcastClanStatus();
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1).addString(member.getName()));
		_log.log(Level.INFO, "Leader of Clan: " + getName() + " changed to: " + member.getName() + " ex leader: " + exMember.getName());
	}
	
	public String getLeaderName()
	{
		if (_leader == null)
		{
			_log.warning(L2Clan.class.getName() + ": Clan " + getName() + " without clan leader!");
			return "";
		}
		return _leader.getName();
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	private void addClanMember(L2ClanMember member)
	{
		_members.put(member.getObjectId(), member);
	}
	
	public void addClanMember(L2PcInstance player)
	{
		if (!fireClanJoinListeners(player))
		{
			return;
		}
		
		final L2ClanMember member = new L2ClanMember(this, player);
		addClanMember(member);
		member.setPlayerInstance(player);
		player.setClan(this);
		player.setPledgeClass(L2ClanMember.calculatePledgeClass(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new PledgeSkillList(this));
		addSkillEffects(player);
	}
	
	public void updateClanMember(L2PcInstance player)
	{
		final L2ClanMember member = new L2ClanMember(player.getClan(), player);
		if (player.isClanLeader())
		{
			setLeader(member);
		}
		addClanMember(member);
	}
	
	public L2ClanMember getClanMember(String name)
	{
		for (L2ClanMember temp : _members.values())
		{
			if (temp.getName().equals(name))
			{
				return temp;
			}
		}
		return null;
	}
	
	public L2ClanMember getClanMember(int objectId)
	{
		return _members.get(objectId);
	}
	
	public void removeClanMember(int objectId, long clanJoinExpiryTime)
	{
		if (!fireClanLeaveListeners(objectId))
		{
			return;
		}
		
		final L2ClanMember exMember = _members.remove(objectId);
		if (exMember == null)
		{
			_log.warning("Member Object ID: " + objectId + " not found in clan while trying to remove");
			return;
		}
		final int leadssubpledge = getLeaderSubPledge(objectId);
		if (leadssubpledge != 0)
		{
			getSubPledge(leadssubpledge).setLeaderId(0);
			updateSubPledgeInDB(leadssubpledge);
		}
		
		if (exMember.getApprentice() != 0)
		{
			final L2ClanMember apprentice = getClanMember(exMember.getApprentice());
			if (apprentice != null)
			{
				if (apprentice.getPlayerInstance() != null)
				{
					apprentice.getPlayerInstance().setSponsor(0);
				}
				else
				{
					apprentice.setApprenticeAndSponsor(0, 0);
				}
				
				apprentice.saveApprenticeAndSponsor(0, 0);
			}
		}
		
		if (exMember.getSponsor() != 0)
		{
			final L2ClanMember sponsor = getClanMember(exMember.getSponsor());
			if (sponsor != null)
			{
				if (sponsor.getPlayerInstance() != null)
				{
					sponsor.getPlayerInstance().setApprentice(0);
				}
				else
				{
					sponsor.setApprenticeAndSponsor(0, 0);
				}
				
				sponsor.saveApprenticeAndSponsor(0, 0);
			}
		}
		exMember.saveApprenticeAndSponsor(0, 0);
		if (Config.REMOVE_CASTLE_CIRCLETS)
		{
			CastleManager.getInstance().removeCirclet(exMember, getCastleId());
		}
		
		if (exMember.isOnline())
		{
			L2PcInstance player = exMember.getPlayerInstance();
			if (!player.isNoble())
			{
				player.setTitle("");
			}
			player.setApprentice(0);
			player.setSponsor(0);
			
			if (player.isClanLeader())
			{
				SiegeManager.getInstance().removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + (Config.ALT_CLAN_CREATE_DAYS * 86400000L));
			}
			removeSkillEffects(player);
			
			if (player.getClan().getCastleId() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
			}
			if (player.getClan().getFortId() > 0)
			{
				FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
			}
			player.sendSkillList();
			
			player.setClan(null);
			
			if (exMember.getPledgeType() != -1)
			{
				player.setClanJoinExpiryTime(clanJoinExpiryTime);
			}
			
			player.setPledgeClass(L2ClanMember.calculatePledgeClass(player));
			player.broadcastUserInfo();
			player.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
		}
		else
		{
			removeMemberInDatabase(exMember, clanJoinExpiryTime, getLeaderId() == objectId ? System.currentTimeMillis() + (Config.ALT_CLAN_CREATE_DAYS * 86400000L) : 0);
		}
	}
	
	public L2ClanMember[] getMembers()
	{
		return _members.values().toArray(new L2ClanMember[_members.size()]);
	}
	
	public int getMembersCount()
	{
		return _members.size();
	}
	
	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;
		for (L2ClanMember temp : _members.values())
		{
			if (temp.getPledgeType() == subpl)
			{
				result++;
			}
		}
		return result;
	}
	
	public int getMaxNrOfMembers(int pledgeType)
	{
		int limit = 0;
		
		switch (pledgeType)
		{
			case 0:
				switch (getLevel())
				{
					case 3:
						limit = 30;
						break;
					case 2:
						limit = 20;
						break;
					case 1:
						limit = 15;
						break;
					case 0:
						limit = 10;
						break;
					default:
						limit = 40;
						break;
				}
				break;
			case -1:
				limit = 20;
				break;
			case 100:
			case 200:
				switch (getLevel())
				{
					case 11:
						limit = 30;
						break;
					default:
						limit = 20;
						break;
				}
				break;
			case 1001:
			case 1002:
			case 2001:
			case 2002:
				switch (getLevel())
				{
					case 9:
					case 10:
					case 11:
						limit = 25;
						break;
					default:
						limit = 10;
						break;
				}
				break;
			default:
				break;
		}
		
		return limit;
	}
	
	public FastList<L2PcInstance> getOnlineMembers(int exclude)
	{
		final FastList<L2PcInstance> onlineMembers = new FastList<>();
		for (L2ClanMember temp : _members.values())
		{
			if ((temp != null) && temp.isOnline() && (temp.getObjectId() != exclude))
			{
				onlineMembers.add(temp.getPlayerInstance());
			}
		}
		return onlineMembers;
	}
	
	public int getOnlineMembersCount()
	{
		int count = 0;
		for (L2ClanMember temp : _members.values())
		{
			if ((temp == null) || !temp.isOnline())
			{
				continue;
			}
			count++;
		}
		return count;
	}
	
	public int getAllyId()
	{
		return _allyId;
	}
	
	public String getAllyName()
	{
		return _allyName;
	}
	
	public void setAllyCrestId(int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}
	
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
		if ((_level >= 2) && (_forum == null) && (Config.COMMUNITY_TYPE > 0))
		{
			final Forum forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");
			if (forum != null)
			{
				_forum = forum.getChildByName(_name);
				if (_forum == null)
				{
					_forum = ForumsBBSManager.getInstance().createNewForum(_name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), Forum.CLAN, Forum.CLANMEMBERONLY, getId());
				}
			}
		}
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
	
	public int getFortId()
	{
		return _fortId;
	}
	
	public int getHideoutId()
	{
		return _hideoutId;
	}
	
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}
	
	public int getCrestId()
	{
		return _crestId;
	}
	
	public void setCrestLargeId(int crestLargeId)
	{
		_crestLargeId = crestLargeId;
	}
	
	public int getCrestLargeId()
	{
		return _crestLargeId;
	}
	
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	
	public void setCastleId(int castleId)
	{
		_castleId = castleId;
	}
	
	public void setFortId(int fortId)
	{
		_fortId = fortId;
	}
	
	public void setHideoutId(int hideoutId)
	{
		_hideoutId = hideoutId;
	}
	
	public boolean isMember(int id)
	{
		return (id == 0 ? false : _members.containsKey(id));
	}
	
	public int getBloodAllianceCount()
	{
		return _bloodAllianceCount;
	}
	
	public void increaseBloodAllianceCount()
	{
		_bloodAllianceCount += SiegeManager.getInstance().getBloodAllianceReward();
		updateBloodAllianceCountInDB();
	}
	
	public void resetBloodAllianceCount()
	{
		_bloodAllianceCount = 0;
		updateBloodAllianceCountInDB();
	}
	
	public void updateBloodAllianceCountInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET blood_alliance_count=? WHERE clan_id=?"))
		{
			statement.setInt(1, getBloodAllianceCount());
			statement.setInt(2, getId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + e.getMessage(), e);
		}
	}
	
	public int getBloodOathCount()
	{
		return _bloodOathCount;
	}
	
	public void increaseBloodOathCount()
	{
		_bloodOathCount += Config.FS_BLOOD_OATH_COUNT;
		updateBloodOathCountInDB();
	}
	
	public void resetBloodOathCount()
	{
		_bloodOathCount = 0;
		updateBloodOathCountInDB();
	}
	
	public void updateBloodOathCountInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET blood_oath_count=? WHERE clan_id=?"))
		{
			ps.setInt(1, getBloodOathCount());
			ps.setInt(2, getId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + e.getMessage(), e);
		}
	}
	
	public void updateClanScoreInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?"))
		{
			statement.setInt(1, getReputationScore());
			statement.setInt(2, getId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on updateClanScoreInDb(): " + e.getMessage(), e);
		}
	}
	
	public void updateClanNameInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_name=? WHERE clan_id=?");
			statement.setString(1, getName());
			statement.setInt(2, getId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error saving clan name: " + e.getMessage(), e);
		}
	}
	
	public void updateClanInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=?,new_leader_id=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setString(3, getAllyName());
			statement.setInt(4, getReputationScore());
			statement.setLong(5, getAllyPenaltyExpiryTime());
			statement.setInt(6, getAllyPenaltyType());
			statement.setLong(7, getCharPenaltyExpiryTime());
			statement.setLong(8, getDissolvingExpiryTime());
			statement.setInt(9, getNewLeaderId());
			statement.setInt(10, getId());
			statement.execute();
			statement.close();
			if (Config.DEBUG)
			{
				_log.fine("New clan leader saved in db: " + getId());
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error saving clan: " + e.getMessage(), e);
		}
	}
	
	public void store()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_CLAN_DATA))
		{
			ps.setInt(1, getId());
			ps.setString(2, getName());
			ps.setInt(3, getLevel());
			ps.setInt(4, getCastleId());
			ps.setInt(5, getBloodAllianceCount());
			ps.setInt(6, getBloodOathCount());
			ps.setInt(7, getAllyId());
			ps.setString(8, getAllyName());
			ps.setInt(9, getLeaderId());
			ps.setInt(10, getCrestId());
			ps.setInt(11, getCrestLargeId());
			ps.setInt(12, getAllyCrestId());
			ps.setInt(13, getNewLeaderId());
			ps.execute();
			if (Config.DEBUG)
			{
				_log.fine("New clan saved in db: " + getId());
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error saving new clan: " + e.getMessage(), e);
		}
	}
	
	private void removeMemberInDatabase(L2ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?");
			statement.setString(1, "");
			statement.setLong(2, clanJoinExpiryTime);
			statement.setLong(3, clanCreateExpiryTime);
			statement.setInt(4, member.getObjectId());
			statement.execute();
			statement.close();
			if (Config.DEBUG)
			{
				_log.fine("clan member removed in db: " + getId());
			}
			
			statement = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error removing clan member: " + e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unused")
	private void updateWarsInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_wars SET wantspeace1=? WHERE clan1=?");
			statement.setInt(1, 0);
			statement.setInt(2, 0);
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error updating clan wars data: " + e.getMessage(), e);
		}
	}
	
	private void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_DATA))
		{
			statement.setInt(1, getId());
			try (ResultSet clanData = statement.executeQuery())
			{
				if (clanData.next())
				{
					setName(clanData.getString("clan_name"));
					setLevel(clanData.getInt("clan_level"));
					setCastleId(clanData.getInt("hasCastle"));
					_bloodAllianceCount = clanData.getInt("blood_alliance_count");
					_bloodOathCount = clanData.getInt("blood_oath_count");
					setAllyId(clanData.getInt("ally_id"));
					setAllyName(clanData.getString("ally_name"));
					setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
					if (getAllyPenaltyExpiryTime() < System.currentTimeMillis())
					{
						setAllyPenaltyExpiryTime(0, 0);
					}
					setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
					if ((getCharPenaltyExpiryTime() + (Config.ALT_CLAN_JOIN_DAYS * 86400000L)) < System.currentTimeMillis())
					{
						setCharPenaltyExpiryTime(0);
					}
					setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));
					
					setCrestId(clanData.getInt("crest_id"));
					setCrestLargeId(clanData.getInt("crest_large_id"));
					setAllyCrestId(clanData.getInt("ally_crest_id"));
					
					setReputationScore(clanData.getInt("reputation_score"), false);
					setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
					setNewLeaderId(clanData.getInt("new_leader_id"), false);
					
					final int leaderId = (clanData.getInt("leader_id"));
					
					statement.clearParameters();
					
					try (PreparedStatement select = con.prepareStatement("SELECT char_name,level,classid,charId,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?"))
					{
						select.setInt(1, getId());
						try (ResultSet clanMember = select.executeQuery())
						{
							L2ClanMember member = null;
							while (clanMember.next())
							{
								member = new L2ClanMember(this, clanMember);
								if (member.getObjectId() == leaderId)
								{
									setLeader(member);
								}
								else
								{
									addClanMember(member);
								}
							}
						}
					}
				}
			}
			
			if (Config.DEBUG && (getName() != null))
			{
				_log.info("Restored clan data for \"" + getName() + "\" from database.");
			}
			
			restoreSubPledges();
			restoreRankPrivs();
			restoreSkills();
			restoreNotice();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error restoring clan data: " + e.getMessage(), e);
		}
	}
	
	private void restoreNotice()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT enabled,notice FROM clan_notices WHERE clan_id=?"))
		{
			statement.setInt(1, getId());
			try (ResultSet noticeData = statement.executeQuery())
			{
				while (noticeData.next())
				{
					_noticeEnabled = noticeData.getBoolean("enabled");
					_notice = noticeData.getString("notice");
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error restoring clan notice: " + e.getMessage(), e);
		}
	}
	
	private void storeNotice(String notice, boolean enabled)
	{
		if (notice == null)
		{
			notice = "";
		}
		
		if (notice.length() > MAX_NOTICE_LENGTH)
		{
			notice = notice.substring(0, MAX_NOTICE_LENGTH - 1);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_notices (clan_id,notice,enabled) values (?,?,?) ON DUPLICATE KEY UPDATE notice=?,enabled=?"))
		{
			statement.setInt(1, getId());
			statement.setString(2, notice);
			if (enabled)
			{
				statement.setString(3, "true");
			}
			else
			{
				statement.setString(3, "false");
			}
			statement.setString(4, notice);
			if (enabled)
			{
				statement.setString(5, "true");
			}
			else
			{
				statement.setString(5, "false");
			}
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error could not store clan notice: " + e.getMessage(), e);
		}
		
		_notice = notice;
		_noticeEnabled = enabled;
	}
	
	public void setNoticeEnabled(boolean enabled)
	{
		storeNotice(_notice, enabled);
	}
	
	public void setNotice(String notice)
	{
		storeNotice(notice, _noticeEnabled);
	}
	
	public boolean isNoticeEnabled()
	{
		return _noticeEnabled;
	}
	
	public String getNotice()
	{
		if (_notice == null)
		{
			return "";
		}
		return _notice;
	}
	
	private void restoreSkills()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level,sub_pledge_id FROM clan_skills WHERE clan_id=?"))
		{
			statement.setInt(1, getId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("skill_id");
					int level = rset.getInt("skill_level");
					L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
					int subType = rset.getInt("sub_pledge_id");
					
					if (subType == -2)
					{
						_skills.put(skill.getId(), skill);
					}
					else if (subType == 0)
					{
						_subPledgeSkills.put(skill.getId(), skill);
					}
					else
					{
						SubPledge subunit = _subPledges.get(subType);
						if (subunit != null)
						{
							subunit.addNewSkill(skill);
						}
						else
						{
							_log.info("Missing subpledge " + subType + " for clan " + this + ", skill skipped.");
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error restoring clan skills: " + e.getMessage(), e);
		}
	}
	
	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
		{
			return new L2Skill[0];
		}
		
		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}
	
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			oldSkill = _skills.put(newSkill.getId(), newSkill);
		}
		
		return oldSkill;
	}
	
	public L2Skill addNewSkill(L2Skill newSkill)
	{
		return addNewSkill(newSkill, -2);
	}
	
	public L2Skill addNewSkill(L2Skill newSkill, int subType)
	{
		L2Skill oldSkill = null;
		if (newSkill != null)
		{
			if (subType == -2)
			{
				oldSkill = _skills.put(newSkill.getId(), newSkill);
			}
			else if (subType == 0)
			{
				oldSkill = _subPledgeSkills.put(newSkill.getId(), newSkill);
			}
			else
			{
				SubPledge subunit = getSubPledge(subType);
				if (subunit != null)
				{
					oldSkill = subunit.addNewSkill(newSkill);
				}
				else
				{
					_log.log(Level.WARNING, "Subpledge " + subType + " does not exist for clan " + this);
					return oldSkill;
				}
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				if (oldSkill != null)
				{
					try (PreparedStatement statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?"))
					{
						statement.setInt(1, newSkill.getLevel());
						statement.setInt(2, oldSkill.getId());
						statement.setInt(3, getId());
						statement.execute();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name,sub_pledge_id) VALUES (?,?,?,?,?)"))
					{
						statement.setInt(1, getId());
						statement.setInt(2, newSkill.getId());
						statement.setInt(3, newSkill.getLevel());
						statement.setString(4, newSkill.getName());
						statement.setInt(5, subType);
						statement.execute();
					}
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Error could not store clan skills: " + e.getMessage(), e);
			}
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(newSkill.getId());
			
			for (L2ClanMember temp : _members.values())
			{
				if ((temp != null) && (temp.getPlayerInstance() != null) && temp.isOnline())
				{
					if (subType == -2)
					{
						if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
						{
							temp.getPlayerInstance().addSkill(newSkill, false);
							temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
							temp.getPlayerInstance().sendPacket(sm);
							temp.getPlayerInstance().sendSkillList();
						}
					}
					else
					{
						if (temp.getPledgeType() == subType)
						{
							temp.getPlayerInstance().addSkill(newSkill, false);
							temp.getPlayerInstance().sendPacket(new ExSubPledgeSkillAdd(subType, newSkill.getId(), newSkill.getLevel()));
							temp.getPlayerInstance().sendPacket(sm);
							temp.getPlayerInstance().sendSkillList();
						}
					}
				}
			}
		}
		return oldSkill;
	}
	
	public void addSkillEffects()
	{
		for (L2Skill skill : _skills.values())
		{
			for (L2ClanMember temp : _members.values())
			{
				try
				{
					if ((temp != null) && temp.isOnline())
					{
						if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
						{
							temp.getPlayerInstance().addSkill(skill, false);
						}
					}
				}
				catch (NullPointerException e)
				{
					_log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
	}
	
	public void addSkillEffects(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		for (L2Skill skill : _skills.values())
		{
			if (skill.getMinPledgeClass() <= player.getPledgeClass())
			{
				player.addSkill(skill, false);
			}
		}
		
		if (player.getPledgeType() == 0)
		{
			for (L2Skill skill : _subPledgeSkills.values())
			{
				player.addSkill(skill, false);
			}
		}
		else
		{
			SubPledge subunit = getSubPledge(player.getPledgeType());
			if (subunit == null)
			{
				return;
			}
			for (L2Skill skill : subunit.getSkills())
			{
				player.addSkill(skill, false);
			}
		}
		
		if (_reputationScore < 0)
		{
			skillsStatus(player, true);
		}
	}
	
	public void removeSkillEffects(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		for (L2Skill skill : _skills.values())
		{
			player.removeSkill(skill, false);
		}
		
		if (player.getPledgeType() == 0)
		{
			for (L2Skill skill : _subPledgeSkills.values())
			{
				player.removeSkill(skill, false);
			}
		}
		else
		{
			SubPledge subunit = getSubPledge(player.getPledgeType());
			if (subunit == null)
			{
				return;
			}
			for (L2Skill skill : subunit.getSkills())
			{
				player.removeSkill(skill, false);
			}
		}
	}
	
	public void skillsStatus(L2PcInstance player, boolean disable)
	{
		if (player == null)
		{
			return;
		}
		
		for (L2Skill skill : _skills.values())
		{
			if (disable)
			{
				player.disableSkill(skill, -1);
			}
			else
			{
				player.enableSkill(skill);
			}
		}
		
		if (player.getPledgeType() == 0)
		{
			for (L2Skill skill : _subPledgeSkills.values())
			{
				if (disable)
				{
					player.disableSkill(skill, -1);
				}
				else
				{
					player.enableSkill(skill);
				}
			}
		}
		else
		{
			final SubPledge subunit = getSubPledge(player.getPledgeType());
			if (subunit != null)
			{
				for (L2Skill skill : subunit.getSkills())
				{
					if (disable)
					{
						player.disableSkill(skill, -1);
					}
					else
					{
						player.enableSkill(skill);
					}
				}
			}
		}
	}
	
	public void broadcastToOnlineAllyMembers(L2GameServerPacket packet)
	{
		for (L2Clan clan : ClanHolder.getInstance().getClanAllies(getAllyId()))
		{
			clan.broadcastToOnlineMembers(packet);
		}
	}
	
	public void broadcastToOnlineMembers(L2GameServerPacket packet)
	{
		for (L2ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline())
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public void broadcastCSToOnlineMembers(CreatureSay packet, L2PcInstance broadcaster)
	{
		for (L2ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline() && !BlockList.isBlocked(member.getPlayerInstance(), broadcaster))
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, L2PcInstance player)
	{
		for (L2ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline() && (member.getPlayerInstance() != player))
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return getName() + "[" + getId() + "]";
	}
	
	public ItemContainer getWarehouse()
	{
		return _warehouse;
	}
	
	public boolean isAtWarWith(Integer id)
	{
		if (!_atWarWith.isEmpty())
		{
			if (_atWarWith.contains(id))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isAtWarWith(L2Clan clan)
	{
		if (clan == null)
		{
			return false;
		}
		if (!_atWarWith.isEmpty())
		{
			if (_atWarWith.contains(clan.getId()))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isAtWarAttacker(Integer id)
	{
		if ((_atWarAttackers != null) && !_atWarAttackers.isEmpty())
		{
			if (_atWarAttackers.contains(id))
			{
				return true;
			}
		}
		return false;
	}
	
	public void setEnemyClan(L2Clan clan)
	{
		Integer id = clan.getId();
		_atWarWith.add(id);
	}
	
	public void setEnemyClan(Integer clan)
	{
		_atWarWith.add(clan);
	}
	
	public void setAttackerClan(L2Clan clan)
	{
		Integer id = clan.getId();
		_atWarAttackers.add(id);
	}
	
	public void setAttackerClan(Integer clan)
	{
		_atWarAttackers.add(clan);
	}
	
	public void deleteEnemyClan(L2Clan clan)
	{
		Integer id = clan.getId();
		_atWarWith.remove(id);
	}
	
	public void deleteAttackerClan(L2Clan clan)
	{
		Integer id = clan.getId();
		_atWarAttackers.remove(id);
	}
	
	public int getHiredGuards()
	{
		return _hiredGuards;
	}
	
	public void incrementHiredGuards()
	{
		_hiredGuards++;
	}
	
	public boolean isAtWar()
	{
		if ((_atWarWith != null) && !_atWarWith.isEmpty())
		{
			return true;
		}
		return false;
	}
	
	public List<Integer> getWarList()
	{
		return _atWarWith;
	}
	
	public List<Integer> getAttackerList()
	{
		return _atWarAttackers;
	}
	
	public void broadcastClanStatus()
	{
		for (L2PcInstance member : getOnlineMembers(0))
		{
			member.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
			member.sendPacket(new PledgeShowMemberListAll(this, member));
		}
	}
	
	public static class SubPledge
	{
		public final int _id;
		public String _subPledgeName;
		private int _leaderId;
		public final Map<Integer, L2Skill> _subPledgeSkills = new FastMap<>();
		
		public SubPledge(int id, String name, int leaderId)
		{
			_id = id;
			_subPledgeName = name;
			_leaderId = leaderId;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getName()
		{
			return _subPledgeName;
		}
		
		public void setName(String name)
		{
			_subPledgeName = name;
		}
		
		public int getLeaderId()
		{
			return _leaderId;
		}
		
		public void setLeaderId(int leaderId)
		{
			_leaderId = leaderId;
		}
		
		public L2Skill addNewSkill(L2Skill skill)
		{
			return _subPledgeSkills.put(skill.getId(), skill);
		}
		
		public Collection<L2Skill> getSkills()
		{
			return _subPledgeSkills.values();
		}
	}
	
	public static class RankPrivs
	{
		private final int _rankId;
		private final int _party;
		private int _rankPrivs;
		
		public RankPrivs(int rank, int party, int privs)
		{
			_rankId = rank;
			_party = party;
			_rankPrivs = privs;
		}
		
		public int getRank()
		{
			return _rankId;
		}
		
		public int getParty()
		{
			return _party;
		}
		
		public int getPrivs()
		{
			return _rankPrivs;
		}
		
		public void setPrivs(int privs)
		{
			_rankPrivs = privs;
		}
	}
	
	private void restoreSubPledges()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?"))
		{
			statement.setInt(1, getId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("sub_pledge_id");
					String name = rset.getString("name");
					int leaderId = rset.getInt("leader_id");
					SubPledge pledge = new SubPledge(id, name, leaderId);
					_subPledges.put(id, pledge);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore clan sub-units: " + e.getMessage(), e);
		}
	}
	
	public final SubPledge getSubPledge(int pledgeType)
	{
		if (_subPledges == null)
		{
			return null;
		}
		
		return _subPledges.get(pledgeType);
	}
	
	public final SubPledge getSubPledge(String pledgeName)
	{
		if (_subPledges == null)
		{
			return null;
		}
		
		for (SubPledge sp : _subPledges.values())
		{
			if (sp.getName().equalsIgnoreCase(pledgeName))
			{
				return sp;
			}
		}
		return null;
	}
	
	public final SubPledge[] getAllSubPledges()
	{
		if (_subPledges == null)
		{
			return new SubPledge[0];
		}
		
		return _subPledges.values().toArray(new SubPledge[_subPledges.values().size()]);
	}
	
	public SubPledge createSubPledge(L2PcInstance player, int pledgeType, int leaderId, String subPledgeName)
	{
		SubPledge subPledge = null;
		pledgeType = getAvailablePledgeTypes(pledgeType);
		if (pledgeType == 0)
		{
			if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendMessage("You can't create any more sub-units of this type");
			}
			return null;
		}
		if (_leader.getObjectId() == leaderId)
		{
			player.sendMessage("Leader is not correct");
			return null;
		}
		
		if ((pledgeType != -1) && (((getReputationScore() < Config.ROYAL_GUARD_COST) && (pledgeType < L2Clan.SUBUNIT_KNIGHT1)) || ((getReputationScore() < Config.KNIGHT_UNIT_COST) && (pledgeType > L2Clan.SUBUNIT_ROYAL2))))
		{
			player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return null;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)"))
		{
			statement.setInt(1, getId());
			statement.setInt(2, pledgeType);
			statement.setString(3, subPledgeName);
			if (pledgeType != -1)
			{
				statement.setInt(4, leaderId);
			}
			else
			{
				statement.setInt(4, 0);
			}
			statement.execute();
			
			subPledge = new SubPledge(pledgeType, subPledgeName, leaderId);
			_subPledges.put(pledgeType, subPledge);
			
			if (pledgeType != -1)
			{
				if (pledgeType < L2Clan.SUBUNIT_KNIGHT1)
				{
					setReputationScore(getReputationScore() - Config.ROYAL_GUARD_COST, true);
				}
				else
				{
					setReputationScore(getReputationScore() - Config.KNIGHT_UNIT_COST, true);
				}
			}
			
			if (Config.DEBUG)
			{
				_log.fine("New sub_clan saved in db: " + getId() + "; " + pledgeType);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error saving sub clan data: " + e.getMessage(), e);
		}
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(_leader.getClan()));
		broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge, _leader.getClan()));
		return subPledge;
	}
	
	public int getAvailablePledgeTypes(int pledgeType)
	{
		if (_subPledges.get(pledgeType) != null)
		{
			switch (pledgeType)
			{
				case SUBUNIT_ACADEMY:
					return 0;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return 0;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return 0;
			}
		}
		return pledgeType;
	}
	
	public void updateSubPledgeInDB(int pledgeType)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?"))
		{
			statement.setInt(1, getSubPledge(pledgeType).getLeaderId());
			statement.setString(2, getSubPledge(pledgeType).getName());
			statement.setInt(3, getId());
			statement.setInt(4, pledgeType);
			statement.execute();
			if (Config.DEBUG)
			{
				_log.fine("Subpledge updated in db: " + getId());
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error updating subpledge: " + e.getMessage(), e);
		}
	}
	
	private void restoreRankPrivs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT privs,rank,party FROM clan_privs WHERE clan_id=?"))
		{
			statement.setInt(1, getId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int rank = rset.getInt("rank");
					int privileges = rset.getInt("privs");
					if (rank == -1)
					{
						continue;
					}
					
					_privs.get(rank).setPrivs(privileges);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error restoring clan privs by rank: " + e.getMessage(), e);
		}
	}
	
	public void initializePrivs()
	{
		RankPrivs privs;
		for (int i = 1; i < 10; i++)
		{
			privs = new RankPrivs(i, 0, CP_NOTHING);
			_privs.put(i, privs);
		}
	}
	
	public int getRankPrivs(int rank)
	{
		if (_privs.get(rank) != null)
		{
			return _privs.get(rank).getPrivs();
		}
		return CP_NOTHING;
	}
	
	public void setRankPrivs(int rank, int privs)
	{
		if (_privs.get(rank) != null)
		{
			_privs.get(rank).setPrivs(privs);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE privs = ?"))
			{
				statement.setInt(1, getId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.setInt(5, privs);
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not store clan privs for rank: " + e.getMessage(), e);
			}
			
			for (L2ClanMember cm : getMembers())
			{
				if (cm.isOnline())
				{
					if (cm.getPowerGrade() == rank)
					{
						if (cm.getPlayerInstance() != null)
						{
							cm.getPlayerInstance().setClanPrivileges(privs);
							cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
							cm.getPlayerInstance().sendPacket(new ExBrExtraUserInfo(cm.getPlayerInstance()));
						}
					}
				}
			}
			broadcastClanStatus();
		}
		else
		{
			_privs.put(rank, new RankPrivs(rank, 0, privs));
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?)"))
			{
				statement.setInt(1, getId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not create new rank and store clan privs for rank: " + e.getMessage(), e);
			}
		}
	}
	
	public final RankPrivs[] getAllRankPrivs()
	{
		if (_privs == null)
		{
			return new RankPrivs[0];
		}
		
		return _privs.values().toArray(new RankPrivs[_privs.values().size()]);
	}
	
	public int getLeaderSubPledge(int leaderId)
	{
		int id = 0;
		for (SubPledge sp : _subPledges.values())
		{
			if (sp.getLeaderId() == 0)
			{
				continue;
			}
			if (sp.getLeaderId() == leaderId)
			{
				id = sp.getId();
			}
		}
		return id;
	}
	
	public synchronized void addReputationScore(int value, boolean save)
	{
		setReputationScore(getReputationScore() + value, save);
	}
	
	public synchronized void takeReputationScore(int value, boolean save)
	{
		setReputationScore(getReputationScore() - value, save);
	}
	
	private void setReputationScore(int value, boolean save)
	{
		if ((_reputationScore >= 0) && (value < 0))
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));
			for (L2ClanMember member : _members.values())
			{
				if (member.isOnline() && (member.getPlayerInstance() != null))
				{
					skillsStatus(member.getPlayerInstance(), true);
				}
			}
		}
		else if ((_reputationScore < 0) && (value >= 0))
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));
			for (L2ClanMember member : _members.values())
			{
				if (member.isOnline() && (member.getPlayerInstance() != null))
				{
					skillsStatus(member.getPlayerInstance(), false);
				}
			}
		}
		
		_reputationScore = value;
		if (_reputationScore > 100000000)
		{
			_reputationScore = 100000000;
		}
		if (_reputationScore < -100000000)
		{
			_reputationScore = -100000000;
		}
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		if (save)
		{
			updateClanScoreInDB();
		}
	}
	
	public int getReputationScore()
	{
		return _reputationScore;
	}
	
	public void setRank(int rank)
	{
		_rank = rank;
	}
	
	public int getRank()
	{
		return _rank;
	}
	
	public int getAuctionBiddedAt()
	{
		return _auctionBiddedAt;
	}
	
	public void setAuctionBiddedAt(int id, boolean storeInDb)
	{
		_auctionBiddedAt = id;
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?"))
			{
				statement.setInt(1, id);
				statement.setInt(2, getId());
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not store auction for clan: " + e.getMessage(), e);
			}
		}
	}
	
	public boolean checkClanJoinCondition(L2PcInstance activeChar, L2PcInstance target, int pledgeType)
	{
		if (activeChar == null)
		{
			return false;
		}
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) != L2Clan.CP_CL_JOIN_CLAN)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if (getCharPenaltyExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
			return false;
		}
		if (target.getClanId() != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			return false;
		}
		if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			return false;
		}
		if (((target.getLevel() > 40) || (target.getClassId().level() >= 2)) && (pledgeType == -1))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			activeChar.sendPacket(SystemMessageId.ACADEMY_REQUIREMENTS);
			return false;
		}
		if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
		{
			if (pledgeType == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL);
				sm.addString(getName());
				activeChar.sendPacket(sm);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL);
			}
			return false;
		}
		return true;
	}
	
	public boolean checkAllyJoinCondition(L2PcInstance activeChar, L2PcInstance target)
	{
		if (activeChar == null)
		{
			return false;
		}
		if ((activeChar.getAllyId() == 0) || !activeChar.isClanLeader() || (activeChar.getClanId() != activeChar.getAllyId()))
		{
			activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return false;
		}
		L2Clan leaderClan = activeChar.getClan();
		if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (leaderClan.getAllyPenaltyType() == PENALTY_TYPE_DISMISS_CLAN)
			{
				activeChar.sendPacket(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY);
				return false;
			}
		}
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if (target.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			return false;
		}
		if (!target.isClanLeader())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		L2Clan targetClan = target.getClan();
		if (target.getAllyId() != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
			sm.addString(targetClan.getName());
			sm.addString(targetClan.getAllyName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_LEAVED)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				sm.addString(target.getClan().getName());
				sm.addString(target.getClan().getAllyName());
				activeChar.sendPacket(sm);
				sm = null;
				return false;
			}
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_DISMISSED)
			{
				activeChar.sendPacket(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				return false;
			}
		}
		if (activeChar.isInsideZone(ZoneId.SIEGE) && target.isInsideZone(ZoneId.SIEGE))
		{
			activeChar.sendPacket(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE);
			return false;
		}
		if (leaderClan.isAtWarWith(targetClan.getId()))
		{
			activeChar.sendPacket(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE);
			return false;
		}
		
		if (ClanHolder.getInstance().getClanAllies(activeChar.getAllyId()).size() >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
			return false;
		}
		
		return true;
	}
	
	public long getAllyPenaltyExpiryTime()
	{
		return _allyPenaltyExpiryTime;
	}
	
	public int getAllyPenaltyType()
	{
		return _allyPenaltyType;
	}
	
	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
	{
		_allyPenaltyExpiryTime = expiryTime;
		_allyPenaltyType = penaltyType;
	}
	
	public long getCharPenaltyExpiryTime()
	{
		return _charPenaltyExpiryTime;
	}
	
	public void setCharPenaltyExpiryTime(long time)
	{
		_charPenaltyExpiryTime = time;
	}
	
	public long getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}
	
	public void setDissolvingExpiryTime(long time)
	{
		_dissolvingExpiryTime = time;
	}
	
	public void createAlly(L2PcInstance player, String allyName)
	{
		if (null == player)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine(player.getObjectId() + "(" + player.getName() + ") requested ally creation from ");
		}
		
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
			return;
		}
		if (getAllyId() != 0)
		{
			player.sendPacket(SystemMessageId.ALREADY_JOINED_ALLIANCE);
			return;
		}
		if (getLevel() < 5)
		{
			player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return;
		}
		if (getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (getAllyPenaltyType() == L2Clan.PENALTY_TYPE_DISSOLVE_ALLY)
			{
				player.sendPacket(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION);
				return;
			}
		}
		if (getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING);
			return;
		}
		if (!Util.isAlphaNumeric(allyName))
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
			return;
		}
		if ((allyName.length() > 16) || (allyName.length() < 2))
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH);
			return;
		}
		if (ClanHolder.getInstance().isAllyExists(allyName))
		{
			player.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS);
			return;
		}
		
		setAllyId(getId());
		setAllyName(allyName.trim());
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();
		
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		player.sendMessage("Alliance " + allyName + " has been created.");
	}
	
	public void dissolveAlly(L2PcInstance player)
	{
		if (getAllyId() == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		if (!player.isClanLeader() || (getId() != getAllyId()))
		{
			player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		if (player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE);
			return;
		}
		
		broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED));
		
		long currentTime = System.currentTimeMillis();
		for (L2Clan clan : ClanHolder.getInstance().getClanAllies(getAllyId()))
		{
			if (clan.getId() != getId())
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.setAllyPenaltyExpiryTime(0, 0);
				clan.updateClanInDB();
			}
		}
		
		setAllyId(0);
		setAllyName(null);
		changeAllyCrest(0, false);
		setAllyPenaltyExpiryTime(currentTime + (Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L), L2Clan.PENALTY_TYPE_DISSOLVE_ALLY);
		updateClanInDB();
		
		player.deathPenalty(false, false, false);
	}
	
	public boolean levelUpClan(L2PcInstance player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		if (System.currentTimeMillis() < getDissolvingExpiryTime())
		{
			player.sendPacket(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS);
			return false;
		}
		
		boolean increaseClanLevel = false;
		
		if (!fireClanLevelUpListeners())
		{
			return false;
		}
		
		switch (getLevel())
		{
			case 0:
			{
				if ((player.getSp() >= 20000) && (player.getAdena() >= 650000))
				{
					if (player.reduceAdena("ClanLvl", 650000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 20000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(20000);
						player.sendPacket(sp);
						sp = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 1:
			{
				if ((player.getSp() >= 100000) && (player.getAdena() >= 2500000))
				{
					if (player.reduceAdena("ClanLvl", 2500000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 100000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(100000);
						player.sendPacket(sp);
						sp = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 2:
			{
				if ((player.getSp() >= 350000) && (player.getInventory().getItemByItemId(1419) != null))
				{
					if (player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 350000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(350000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(1419);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 3:
			{
				if ((player.getSp() >= 1000000) && (player.getInventory().getItemByItemId(3874) != null))
				{
					if (player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 1000000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(1000000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(3874);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 4:
			{
				if ((player.getSp() >= 2500000) && (player.getInventory().getItemByItemId(3870) != null))
				{
					if (player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 2500000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(2500000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(3870);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 5:
				if ((getReputationScore() >= Config.CLAN_LEVEL_6_COST) && (getMembersCount() >= Config.CLAN_LEVEL_6_REQUIREMENT))
				{
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_6_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_6_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				break;
			
			case 6:
				if ((getReputationScore() >= Config.CLAN_LEVEL_7_COST) && (getMembersCount() >= Config.CLAN_LEVEL_7_REQUIREMENT))
				{
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_7_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_7_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				break;
			case 7:
				if ((getReputationScore() >= Config.CLAN_LEVEL_8_COST) && (getMembersCount() >= Config.CLAN_LEVEL_8_REQUIREMENT))
				{
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_8_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_8_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				break;
			case 8:
				if ((getReputationScore() >= Config.CLAN_LEVEL_9_COST) && (player.getInventory().getItemByItemId(9910) != null) && (getMembersCount() >= Config.CLAN_LEVEL_9_REQUIREMENT))
				{
					if (player.destroyItemByItemId("ClanLvl", 9910, 150, player.getTarget(), false))
					{
						setReputationScore(getReputationScore() - Config.CLAN_LEVEL_9_COST, true);
						SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						cr.addNumber(Config.CLAN_LEVEL_9_COST);
						player.sendPacket(cr);
						cr = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(9910);
						sm.addItemNumber(150);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				break;
			case 9:
				if ((getReputationScore() >= Config.CLAN_LEVEL_10_COST) && (player.getInventory().getItemByItemId(9911) != null) && (getMembersCount() >= Config.CLAN_LEVEL_10_REQUIREMENT))
				{
					if (player.destroyItemByItemId("ClanLvl", 9911, 5, player.getTarget(), false))
					{
						setReputationScore(getReputationScore() - Config.CLAN_LEVEL_10_COST, true);
						SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						cr.addNumber(Config.CLAN_LEVEL_10_COST);
						player.sendPacket(cr);
						cr = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(9911);
						sm.addItemNumber(5);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				break;
			case 10:
				boolean hasTerritory = false;
				for (Territory terr : TerritoryWarManager.getInstance().getAllTerritories())
				{
					if (terr.getOwnerClan().getId() == getId())
					{
						hasTerritory = true;
						break;
					}
				}
				if (hasTerritory && (getReputationScore() >= Config.CLAN_LEVEL_11_COST) && (getMembersCount() >= Config.CLAN_LEVEL_11_REQUIREMENT))
				{
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_11_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_11_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				break;
			default:
				return false;
		}
		
		if (!increaseClanLevel)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
			return false;
		}
		
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		ItemList il = new ItemList(player, false);
		player.sendPacket(il);
		
		changeLevel(getLevel() + 1);
		return true;
	}
	
	public void changeLevel(int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?"))
		{
			statement.setInt(1, level);
			statement.setInt(2, getId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not increase clan level:" + e.getMessage(), e);
		}
		
		setLevel(level);
		
		if (getLeader().isOnline())
		{
			L2PcInstance leader = getLeader().getPlayerInstance();
			if (4 < level)
			{
				SiegeManager.getInstance().addSiegeSkills(leader);
				leader.sendPacket(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
			}
			else if (5 > level)
			{
				SiegeManager.getInstance().removeSiegeSkills(leader);
			}
		}
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}
	
	public void changeClanCrest(int crestId)
	{
		if (getCrestId() != 0)
		{
			CrestCache.getInstance().removePledgeCrest(getCrestId());
		}
		
		setCrestId(crestId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?"))
		{
			statement.setInt(1, crestId);
			statement.setInt(2, getId());
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Could not update crest for clan " + getName() + " [" + getId() + "] : " + e.getMessage(), e);
		}
		
		for (L2PcInstance member : getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}
	
	public void changeAllyCrest(int crestId, boolean onlyThisClan)
	{
		String sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?";
		int allyId = getId();
		if (!onlyThisClan)
		{
			if (getAllyCrestId() != 0)
			{
				CrestCache.getInstance().removeAllyCrest(getAllyCrestId());
			}
			sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?";
			allyId = getAllyId();
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sqlStatement))
		{
			statement.setInt(1, crestId);
			statement.setInt(2, allyId);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Could not update ally crest for ally/clan id " + allyId + " : " + e.getMessage(), e);
		}
		
		if (onlyThisClan)
		{
			setAllyCrestId(crestId);
			for (L2PcInstance member : getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}
		}
		else
		{
			for (L2Clan clan : ClanHolder.getInstance().getClanAllies(getAllyId()))
			{
				clan.setAllyCrestId(crestId);
				for (L2PcInstance member : clan.getOnlineMembers(0))
				{
					member.broadcastUserInfo();
				}
			}
		}
	}
	
	public void changeLargeCrest(int crestId)
	{
		if (getCrestLargeId() != 0)
		{
			CrestCache.getInstance().removePledgeCrestLarge(getCrestLargeId());
		}
		
		setCrestLargeId(crestId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?"))
		{
			statement.setInt(1, crestId);
			statement.setInt(2, getId());
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Could not update large crest for clan " + getName() + " [" + getId() + "] : " + e.getMessage(), e);
		}
		
		for (L2PcInstance member : getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}
	
	public boolean isLearnableSubSkill(int skillId, int skillLevel)
	{
		L2Skill current = _subPledgeSkills.get(skillId);
		
		if ((current != null) && ((current.getLevel() + 1) == skillLevel))
		{
			return true;
		}
		
		if ((current == null) && (skillLevel == 1))
		{
			return true;
		}
		
		for (SubPledge subunit : _subPledges.values())
		{
			if (subunit._id == -1)
			{
				continue;
			}
			current = subunit._subPledgeSkills.get(skillId);
			
			if ((current != null) && ((current.getLevel() + 1) == skillLevel))
			{
				return true;
			}
			
			if ((current == null) && (skillLevel == 1))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isLearnableSubPledgeSkill(L2Skill skill, int subType)
	{
		if (subType == -1)
		{
			return false;
		}
		
		int id = skill.getId();
		L2Skill current;
		if (subType == 0)
		{
			current = _subPledgeSkills.get(id);
		}
		else
		{
			current = _subPledges.get(subType)._subPledgeSkills.get(id);
		}
		
		if ((current != null) && ((current.getLevel() + 1) == skill.getLevel()))
		{
			return true;
		}
		
		if ((current == null) && (skill.getLevel() == 1))
		{
			return true;
		}
		
		return false;
	}
	
	public SubPledgeSkill[] getAllSubSkills()
	{
		FastList<SubPledgeSkill> list = FastList.newInstance();
		for (L2Skill skill : _subPledgeSkills.values())
		{
			list.add(new SubPledgeSkill(0, skill.getId(), skill.getLevel()));
		}
		for (SubPledge subunit : _subPledges.values())
		{
			for (L2Skill skill : subunit.getSkills())
			{
				list.add(new SubPledgeSkill(subunit._id, skill.getId(), skill.getLevel()));
			}
		}
		SubPledgeSkill[] result = list.toArray(new SubPledgeSkill[list.size()]);
		FastList.recycle(list);
		return result;
	}
	
	public void setNewLeaderId(int objectId, boolean storeInDb)
	{
		_newLeaderId = objectId;
		if (storeInDb)
		{
			updateClanInDB();
		}
	}
	
	public int getNewLeaderId()
	{
		return _newLeaderId;
	}
	
	public L2PcInstance getNewLeader()
	{
		return L2World.getInstance().getPlayer(_newLeaderId);
	}
	
	public String getNewLeaderName()
	{
		return CharNameHolder.getInstance().getNameById(_newLeaderId);
	}
	
	public int getSiegeKills()
	{
		return _siegeKills != null ? _siegeKills.get() : 0;
	}
	
	public int getSiegeDeaths()
	{
		return _siegeDeaths != null ? _siegeDeaths.get() : 0;
	}
	
	public int addSiegeKill()
	{
		if (_siegeKills == null)
		{
			synchronized (this)
			{
				if (_siegeKills == null)
				{
					_siegeKills = new AtomicInteger();
				}
			}
		}
		return _siegeKills.incrementAndGet();
	}
	
	public int addSiegeDeath()
	{
		if (_siegeDeaths == null)
		{
			synchronized (this)
			{
				if (_siegeDeaths == null)
				{
					_siegeDeaths = new AtomicInteger();
				}
			}
		}
		return _siegeDeaths.incrementAndGet();
	}
	
	public void clearSiegeKills()
	{
		if (_siegeKills != null)
		{
			_siegeKills.set(0);
		}
	}
	
	public void clearSiegeDeaths()
	{
		if (_siegeDeaths != null)
		{
			_siegeDeaths.set(0);
		}
	}
	
	private void fireClanCreationListeners()
	{
		if (!clanCreationListeners.isEmpty())
		{
			ClanCreationEvent event = new ClanCreationEvent();
			event.setClan(this);
			for (ClanCreationListener listener : clanCreationListeners)
			{
				listener.onClanCreate(event);
			}
		}
	}
	
	private boolean fireClanLeaderChangeListeners(L2PcInstance newLeader, L2PcInstance exLeader)
	{
		if (!clanMembershipListeners.isEmpty() && (newLeader != null) && (exLeader != null))
		{
			ClanLeaderChangeEvent event = new ClanLeaderChangeEvent();
			event.setClan(this);
			event.setNewLeader(newLeader);
			event.setOldLeader(exLeader);
			for (ClanMembershipListener listener : clanMembershipListeners)
			{
				if (!listener.onLeaderChange(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean fireClanJoinListeners(L2PcInstance player)
	{
		if (!clanMembershipListeners.isEmpty() && (player != null))
		{
			ClanJoinEvent event = new ClanJoinEvent();
			event.setClan(this);
			event.setPlayer(player);
			for (ClanMembershipListener listener : clanMembershipListeners)
			{
				if (!listener.onJoin(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean fireClanLeaveListeners(int objectId)
	{
		if (!clanMembershipListeners.isEmpty())
		{
			ClanLeaveEvent event = new ClanLeaveEvent();
			event.setPlayerId(objectId);
			event.setClan(this);
			for (ClanMembershipListener listener : clanMembershipListeners)
			{
				if (!listener.onLeave(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean fireClanLevelUpListeners()
	{
		if (!clanCreationListeners.isEmpty())
		{
			ClanLevelUpEvent event = new ClanLevelUpEvent();
			event.setClan(this);
			event.setOldLevel(_level);
			for (ClanCreationListener listener : clanCreationListeners)
			{
				if (!listener.onClanLevelUp(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public static void addClanCreationListener(ClanCreationListener listener)
	{
		if (!clanCreationListeners.contains(listener))
		{
			clanCreationListeners.add(listener);
		}
	}
	
	public static void removeClanCreationListener(ClanCreationListener listener)
	{
		clanCreationListeners.remove(listener);
	}
	
	public static void addClanMembershipListener(ClanMembershipListener listener)
	{
		if (!clanMembershipListeners.contains(listener))
		{
			clanMembershipListeners.add(listener);
		}
	}
	
	public static void removeClanMembershipListener(ClanMembershipListener listener)
	{
		clanMembershipListeners.remove(listener);
	}
}