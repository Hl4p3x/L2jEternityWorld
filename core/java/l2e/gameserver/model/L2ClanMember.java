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
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class L2ClanMember
{
	private static final Logger _log = Logger.getLogger(L2ClanMember.class.getName());
	
	private final L2Clan _clan;
	private int _objectId;
	private String _name;
	private String _title;
	private int _powerGrade;
	private int _level;
	private int _classId;
	private boolean _sex;
	private int _raceOrdinal;
	private L2PcInstance _player;
	private int _pledgeType;
	private int _apprentice;
	private int _sponsor;
	
	public L2ClanMember(L2Clan clan, ResultSet clanMember) throws SQLException
	{
		if (clan == null)
		{
			throw new IllegalArgumentException("Cannot create a Clan Member with a null clan.");
		}
		_clan = clan;
		_name = clanMember.getString("char_name");
		_level = clanMember.getInt("level");
		_classId = clanMember.getInt("classid");
		_objectId = clanMember.getInt("charId");
		_pledgeType = clanMember.getInt("subpledge");
		_title = clanMember.getString("title");
		_powerGrade = clanMember.getInt("power_grade");
		_apprentice = clanMember.getInt("apprentice");
		_sponsor = clanMember.getInt("sponsor");
		_sex = clanMember.getInt("sex") != 0;
		_raceOrdinal = clanMember.getInt("race");
	}
	
	public L2ClanMember(L2Clan clan, L2PcInstance player)
	{
		if (clan == null)
		{
			throw new IllegalArgumentException("Cannot create a Clan Member if player has a null clan.");
		}
		_player = player;
		_clan = clan;
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_objectId = player.getObjectId();
		_pledgeType = player.getPledgeType();
		_powerGrade = player.getPowerGrade();
		_title = player.getTitle();
		_sponsor = 0;
		_apprentice = 0;
		_sex = player.getAppearance().getSex();
		_raceOrdinal = player.getRace().ordinal();
	}
	
	public void setPlayerInstance(L2PcInstance player)
	{
		if (player == null && _player != null)
		{
			_name = _player.getName();
			_level = _player.getLevel();
			_classId = _player.getClassId().getId();
			_objectId = _player.getObjectId();
			_powerGrade = _player.getPowerGrade();
			_pledgeType = _player.getPledgeType();
			_title = _player.getTitle();
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
			_sex = _player.getAppearance().getSex();
			_raceOrdinal = _player.getRace().ordinal();
		}
		
		if (player != null)
		{
			_clan.addSkillEffects(player);
			if (_clan.getLevel() > 3 && player.isClanLeader())
				SiegeManager.getInstance().addSiegeSkills(player);
			if (player.isClanLeader())
				_clan.setLeader(this);
		}
		
		_player = player;
	}
	
	public L2PcInstance getPlayerInstance()
	{
		return _player;
	}
	
	public boolean isOnline()
	{
		if (_player == null || !_player.isOnline())
			return false;
		if (_player.getClient() == null)
			return false;
		if (_player.getClient().isDetached())
			return false;
		
		return true;
	}
	
	public int getClassId()
	{
		if (_player != null)
		{
			return _player.getClassId().getId();
		}
		return _classId;
	}
	
	public int getLevel()
	{
		if (_player != null)
		{
			return _player.getLevel();
		}
		return _level;
	}
	
	public String getName()
	{
		if (_player != null)
		{
			return _player.getName();
		}
		return _name;
	}
	
	public int getObjectId()
	{
		if (_player != null)
		{
			return _player.getObjectId();
		}
		return _objectId;
	}
	
	public String getTitle()
	{
		if (_player != null)
		{
			return _player.getTitle();
		}
		return _title;
	}
	
	public int getPledgeType()
	{
		if (_player != null)
		{
			return _player.getPledgeType();
		}
		return _pledgeType;
	}
	
	public void setPledgeType(int pledgeType)
	{
		_pledgeType = pledgeType;
		if(_player != null)
		{
			_player.setPledgeType(pledgeType);
		}
		else
		{
			updatePledgeType();
		}
	}
	
	public void updatePledgeType()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET subpledge=? WHERE charId=?");
			statement.setLong(1, _pledgeType);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not update pledge type: " + e.getMessage(), e);
		}
	}
	
	public int getPowerGrade()
	{
		if(_player != null)
			return _player.getPowerGrade();
		return _powerGrade;
	}
	
	public void setPowerGrade(int powerGrade)
	{
		_powerGrade = powerGrade;
		if(_player != null)
		{
			_player.setPowerGrade(powerGrade);
		}
		else
		{
			updatePowerGrade();
		}
	}
	
	public void updatePowerGrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET power_grade=? WHERE charId=?");
			statement.setLong(1, _powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not update power _grade: " + e.getMessage(), e);
		}
	}
	
	public void setApprenticeAndSponsor(int apprenticeID, int sponsorID)
	{
		_apprentice = apprenticeID;
		_sponsor = sponsorID;
	}
	
	public int getRaceOrdinal()
	{
		if (_player != null)
			return _player.getRace().ordinal();
		return _raceOrdinal;
	}
	
	public boolean getSex()
	{
		if (_player != null)
			return _player.getAppearance().getSex();
		return _sex;
	}
	
	public int getSponsor()
	{
		if (_player != null)
			return _player.getSponsor();
		return _sponsor;
	}
	
	public int getApprentice()
	{
		if (_player != null)
			return _player.getApprentice();
		return _apprentice;
	}
	
	public String getApprenticeOrSponsorName()
	{
		if(_player != null)
		{
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
		}
		
		if(_apprentice != 0)
		{
			L2ClanMember apprentice = _clan.getClanMember(_apprentice);
			if(apprentice != null)
				return apprentice.getName();
			return "Error";
		}
		if(_sponsor != 0)
		{
			L2ClanMember sponsor = _clan.getClanMember(_sponsor);
			if(sponsor != null) 
				return sponsor.getName();
			return "Error";
		}
		return "";
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
	
	public static int calculatePledgeClass(L2PcInstance player)
	{
		int pledgeClass = 0;
		
		if (player == null)
			return pledgeClass;
		
		L2Clan clan = player.getClan();
		if (clan != null)
		{
			switch (clan.getLevel())
			{
				case 4:
					if (player.isClanLeader())
						pledgeClass = 3;
					break;
				case 5:
					if (player.isClanLeader())
						pledgeClass = 4;
					else
						pledgeClass = 2;
					break;
				case 6:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 5;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 4;
										break;
									case -1:
									default:
										pledgeClass = 3;
										break;
								}
							break;
					}
					break;
				case 7:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 3;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 7;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 6;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 5;
										break;
									case -1:
									default:
										pledgeClass = 4;
										break;
								}
							break;
					}
					break;
				case 8:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 4;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 3;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 8;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 7;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 6;
										break;
									case -1:
									default:
										pledgeClass = 5;
										break;
								}
							break;
					}
					break;
				case 9:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 5;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 4;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 9;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 8;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 7;
										break;
									case -1:
									default:
										pledgeClass = 6;
										break;
								}
							break;
					}
					break;
				case 10:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 6;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 5;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 10;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 9;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 8;
										break;
									case -1:
									default:
										pledgeClass = 7;
										break;
								}
							break;
					}
					break;
				case 11:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 7;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 6;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 11;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 10;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 9;
										break;
									case -1:
									default:
										pledgeClass = 8;
										break;
								}
							break;
					}
					break;
				default:
					pledgeClass = 1;
					break;
			}
		}

		if (player.isNoble() && (pledgeClass < 5))
		{
			pledgeClass = 5;
		}
		
		if (player.isHero() && (pledgeClass < 8))
		{
			pledgeClass = 8;
		}
		return pledgeClass;
	}
	
	public void saveApprenticeAndSponsor(int apprentice, int sponsor)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET apprentice=?,sponsor=? WHERE charId=?");
			statement.setInt(1, apprentice);
			statement.setInt(2, sponsor);
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Could not save apprentice/sponsor: " + e.getMessage(), e);
		}
	}
}