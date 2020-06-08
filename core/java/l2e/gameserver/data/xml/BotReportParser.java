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
package l2e.gameserver.data.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import gnu.trove.map.hash.TIntLongHashMap;

public final class BotReportParser
{
	static final Logger _log = Logger.getLogger(BotReportParser.class.getName());
	
	private static final int COLUMN_BOT_ID = 1;
	private static final int COLUMN_REPORTER_ID = 2;
	private static final int COLUMN_REPORT_TIME = 3;
	
	public static final int ATTACK_ACTION_BLOCK_ID = -1;
	public static final int TRADE_ACTION_BLOCK_ID = -2;
	public static final int PARTY_ACTION_BLOCK_ID = -3;
	public static final int ACTION_BLOCK_ID = -4;
	public static final int CHAT_BLOCK_ID = -5;
	
	private static final String SQL_LOAD_REPORTED_CHAR_DATA = "SELECT * FROM bot_report_data";
	private static final String SQL_INSERT_REPORTED_CHAR_DATA = "INSERT INTO bot_report_data VALUES (?,?,?)";
	private static final String SQL_CLEAR_REPORTED_CHAR_DATA = "DELETE FROM bot_report_data";
	
	private TIntLongHashMap _ipRegistry;
	private Map<Integer, ReporterCharData> _charRegistry;
	private Map<Integer, ReportedCharData> _reports;
	private Map<Integer, PunishHolder> _punishments;
	
	BotReportParser()
	{
		if (Config.BOTREPORT_ENABLE)
		{
			_ipRegistry = new TIntLongHashMap();
			_charRegistry = new ConcurrentHashMap<>();
			_reports = new ConcurrentHashMap<>();
			_punishments = new ConcurrentHashMap<>();
			
			try
			{
				File punishments = new File(Config.DATAPACK_ROOT, "data/botreport_punishments.xml");
				if (!punishments.exists())
				{
					throw new FileNotFoundException(punishments.getName());
				}
				
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				parser.parse(punishments, new PunishmentsLoader());
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "BotReportParser: Could not load punishments from /data/botreport_punishments.xml", e);
			}
			
			loadReportedCharData();
			scheduleResetPointTask();
		}
	}
	
	private void loadReportedCharData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement(SQL_LOAD_REPORTED_CHAR_DATA);
			ResultSet rset = st.executeQuery();
			long lastResetTime = 0;
			try
			{
				String[] hour = Config.BOTREPORT_RESETPOINT_HOUR;
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
				c.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
				
				if (System.currentTimeMillis() < c.getTimeInMillis())
				{
					c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - 1);
				}
				
				lastResetTime = c.getTimeInMillis();
			}
			catch (Exception e)
			{
				
			}
			
			while (rset.next())
			{
				int botId = rset.getInt(COLUMN_BOT_ID);
				int reporter = rset.getInt(COLUMN_REPORTER_ID);
				long date = rset.getLong(COLUMN_REPORT_TIME);
				if (_reports.containsKey(botId))
				{
					_reports.get(botId).addReporter(reporter, date);
				}
				else
				{
					ReportedCharData rcd = new ReportedCharData();
					rcd.addReporter(reporter, date);
					_reports.put(rset.getInt(COLUMN_BOT_ID), rcd);
				}
				
				if (date > lastResetTime)
				{
					ReporterCharData rcd = null;
					if ((rcd = _charRegistry.get(reporter)) != null)
					{
						rcd.setPoints(rcd.getPointsLeft() - 1);
					}
					else
					{
						rcd = new ReporterCharData();
						rcd.setPoints(6);
						_charRegistry.put(reporter, rcd);
					}
				}
			}
			
			rset.close();
			st.close();
			
			_log.info("BotReportParser: Loaded " + _reports.size() + " bot reports.");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "BotReportParser: Could not load reported char data!", e);
		}
	}
	
	public void saveReportedCharData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement(SQL_CLEAR_REPORTED_CHAR_DATA);
			st.execute();
			
			st = con.prepareStatement(SQL_INSERT_REPORTED_CHAR_DATA);
			for (Map.Entry<Integer, ReportedCharData> entrySet : _reports.entrySet())
			{
				TIntLongHashMap reportTable = entrySet.getValue()._reporters;
				for (int reporterId : reportTable.keys())
				{
					st.setInt(1, entrySet.getKey());
					st.setInt(2, reporterId);
					st.setLong(3, reportTable.get(reporterId));
					st.execute();
					st.clearParameters();
				}
			}
			st.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "BotReportParser: Could not update reported char data in database!", e);
		}
	}
	
	public boolean reportBot(L2PcInstance reporter)
	{
		L2Object target = reporter.getTarget();
		L2PcInstance bot = null;
		if ((target == null) || ((bot = target.getActingPlayer()) == null) || (target.getObjectId() == reporter.getObjectId()))
		{
			return false;
		}
		
		if (bot.isInsideZone(ZoneId.PEACE) || bot.isInsideZone(ZoneId.PVP))
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHARACTER_IN_PEACE_OR_BATTLE_ZONE);
			return false;
		}
		
		if (bot.isInOlympiadMode())
		{
			reporter.sendPacket(SystemMessageId.TARGET_NOT_REPORT_CANNOT_REPORT_PEACE_PVP_ZONE_OR_OLYMPIAD_OR_CLAN_WAR_ENEMY);
			return false;
		}
		
		if ((bot.getClan() != null) && bot.getClan().isAtWarWith(reporter.getClan()))
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CLAN_WAR_ENEMY);
			return false;
		}
		
		if (bot.getExp() == bot.getStat().getStartingExp())
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHAR_WHO_ACQUIRED_XP);
			return false;
		}
		
		ReportedCharData rcd = _reports.get(bot.getObjectId());
		ReporterCharData rcdRep = _charRegistry.get(reporter.getObjectId());
		final int reporterId = reporter.getObjectId();
		
		synchronized (this)
		{
			if (_reports.containsKey(reporterId))
			{
				reporter.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AND_CANNOT_REPORT);
				return false;
			}
			
			final int ip = hashIp(reporter);
			if (!timeHasPassed(_ipRegistry, ip))
			{
				reporter.sendPacket(SystemMessageId.CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP);
				return false;
			}
			
			if (rcd != null)
			{
				if (rcd.alredyReportedBy(reporterId))
				{
					reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_1);
					return false;
				}
				
				if (!Config.BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS && rcd.reportedBySameClan(reporter.getClan()))
				{
					reporter.sendPacket(SystemMessageId.CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP);
					return false;
				}
			}
			
			if (rcdRep != null)
			{
				if (rcdRep.getPointsLeft() == 0)
				{
					reporter.sendPacket(SystemMessageId.YOU_HAVE_USED_ALL_POINTS_POINTS_ARE_RESET_AT_NOON);
					return false;
				}
				
				long reuse = (System.currentTimeMillis() - rcdRep.getLastReporTime());
				if (reuse < Config.BOTREPORT_REPORT_DELAY)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_REPORT_IN_S1_MINS_YOU_HAVE_S2_POINTS_LEFT);
					sm.addNumber((int) (reuse / 60000));
					sm.addNumber(rcdRep.getPointsLeft());
					reporter.sendPacket(sm);
					return false;
				}
			}
			
			final long curTime = System.currentTimeMillis();
			
			if (rcd == null)
			{
				rcd = new ReportedCharData();
				_reports.put(bot.getObjectId(), rcd);
			}
			rcd.addReporter(reporterId, curTime);
			
			if (rcdRep == null)
			{
				rcdRep = new ReporterCharData();
			}
			rcdRep.registerReport(curTime);
			
			_ipRegistry.put(ip, curTime);
			_charRegistry.put(reporterId, rcdRep);
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_REPORTED_AS_BOT);
		sm.addCharName(bot);
		reporter.sendPacket(sm);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_USED_REPORT_POINT_ON_C1_YOU_HAVE_C2_POINTS_LEFT);
		sm.addCharName(bot);
		sm.addNumber(rcdRep.getPointsLeft());
		reporter.sendPacket(sm);
		
		handleReport(bot, rcd);
		
		return true;
	}
	
	private void handleReport(L2PcInstance bot, final ReportedCharData rcd)
	{
		punishBot(bot, _punishments.get(rcd.getReportCount()));
		
		for (int key : _punishments.keySet())
		{
			if ((key < 0) && (Math.abs(key) <= rcd.getReportCount()))
			{
				punishBot(bot, _punishments.get(key));
			}
		}
	}
	
	private void punishBot(L2PcInstance bot, PunishHolder ph)
	{
		if (ph != null)
		{
			ph._punish.getEffects(bot, bot);
			if (ph._systemMessageId > -1)
			{
				SystemMessageId id = SystemMessageId.getSystemMessageId(ph._systemMessageId);
				if (id != null)
				{
					bot.sendPacket(id);
				}
			}
		}
	}
	
	void addPunishment(int neededReports, int skillId, int skillLevel, int sysMsg)
	{
		L2Skill sk = SkillHolder.getInstance().getInfo(skillId, skillLevel);
		if (sk != null)
		{
			_punishments.put(neededReports, new PunishHolder(sk, sysMsg));
		}
		else
		{
			_log.warning("BotReportParser: Could not add punishment for " + neededReports + " report(s): Skill " + skillId + "-" + skillLevel + " does not exist!");
		}
	}
	
	void resetPointsAndSchedule()
	{
		synchronized (_charRegistry)
		{
			for (ReporterCharData rcd : _charRegistry.values())
			{
				rcd.setPoints(7);
			}
		}
		
		scheduleResetPointTask();
	}
	
	private void scheduleResetPointTask()
	{
		try
		{
			String[] hour = Config.BOTREPORT_RESETPOINT_HOUR;
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
			c.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
			
			if (System.currentTimeMillis() > c.getTimeInMillis())
			{
				c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new ResetPointTask(), c.getTimeInMillis() - System.currentTimeMillis());
		}
		catch (Exception e)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new ResetPointTask(), 24 * 3600 * 1000);
			_log.log(Level.WARNING, "BotReportParser: Could not properly schedule bot report points reset task. Scheduled in 24 hours.", e);
		}
	}
	
	public static BotReportParser getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static int hashIp(L2PcInstance player)
	{
		String con = player.getClient().getConnection().getInetAddress().getHostAddress();
		String[] rawByte = con.split("\\.");
		int[] rawIp = new int[4];
		for (int i = 0; i < 4; i++)
		{
			rawIp[i] = Integer.parseInt(rawByte[i]);
		}
		
		return rawIp[0] | (rawIp[1] << 8) | (rawIp[2] << 16) | (rawIp[3] << 24);
	}
	
	private static boolean timeHasPassed(TIntLongHashMap map, int objectId)
	{
		long time;
		if ((time = map.get(objectId)) != map.getNoEntryValue())
		{
			return (System.currentTimeMillis() - time) > Config.BOTREPORT_REPORT_DELAY;
		}
		
		return true;
	}
	
	private final class ReporterCharData
	{
		private long _lastReport;
		private byte _reportPoints;
		
		ReporterCharData()
		{
			_reportPoints = 7;
			_lastReport = 0;
		}
		
		void registerReport(long time)
		{
			_reportPoints -= 1;
			_lastReport = time;
		}
		
		long getLastReporTime()
		{
			return _lastReport;
		}
		
		byte getPointsLeft()
		{
			return _reportPoints;
		}
		
		void setPoints(int points)
		{
			_reportPoints = (byte) points;
		}
	}
	
	private final class ReportedCharData
	{
		TIntLongHashMap _reporters;
		
		ReportedCharData()
		{
			_reporters = new TIntLongHashMap();
		}
		
		int getReportCount()
		{
			return _reporters.size();
		}
		
		boolean alredyReportedBy(int objectId)
		{
			return _reporters.contains(objectId);
		}
		
		void addReporter(int objectId, long reportTime)
		{
			_reporters.put(objectId, reportTime);
		}
		
		boolean reportedBySameClan(L2Clan clan)
		{
			if (clan == null)
			{
				return false;
			}
			
			for (int reporterId : _reporters.keys())
			{
				if (clan.isMember(reporterId))
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	private final class PunishmentsLoader extends DefaultHandler
	{
		PunishmentsLoader()
		{
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attr)
		{
			if (qName.equals("punishment"))
			{
				int reportCount = -1, skillId = -1, skillLevel = 1, sysMessage = -1;
				try
				{
					reportCount = Integer.parseInt(attr.getValue("neededReportCount"));
					skillId = Integer.parseInt(attr.getValue("skillId"));
					String level = attr.getValue("skillLevel");
					String systemMessageId = attr.getValue("sysMessageId");
					if (level != null)
					{
						skillLevel = Integer.parseInt(level);
					}
					
					if (systemMessageId != null)
					{
						sysMessage = Integer.parseInt(systemMessageId);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				addPunishment(reportCount, skillId, skillLevel, sysMessage);
			}
		}
	}
	
	class PunishHolder
	{
		final L2Skill _punish;
		final int _systemMessageId;
		
		PunishHolder(final L2Skill sk, final int sysMsg)
		{
			_punish = sk;
			_systemMessageId = sysMsg;
		}
	}
	
	class ResetPointTask implements Runnable
	{
		@Override
		public void run()
		{
			resetPointsAndSchedule();
			
		}
	}
	
	private static final class SingletonHolder
	{
		static final BotReportParser INSTANCE = new BotReportParser();
	}
}