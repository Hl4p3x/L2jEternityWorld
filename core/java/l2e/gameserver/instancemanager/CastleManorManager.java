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
package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.xml.ManorParser;
import l2e.gameserver.model.CropProcure;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.SeedProduction;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.itemcontainer.ClanWarehouse;
import l2e.gameserver.model.itemcontainer.ItemContainer;
import l2e.gameserver.network.SystemMessageId;
import l2e.util.Rnd;

public final class CastleManorManager
{
	protected static final Logger _log = Logger.getLogger(CastleManorManager.class.getName());
	
	public static final int PERIOD_CURRENT = 0;
	public static final int PERIOD_NEXT = 1;
	
	private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
	private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
	
	private static final int NEXT_PERIOD_APPROVE = Config.ALT_MANOR_APPROVE_TIME;
	private static final int NEXT_PERIOD_APPROVE_MIN = Config.ALT_MANOR_APPROVE_MIN;
	private static final int MANOR_REFRESH = Config.ALT_MANOR_REFRESH_TIME;
	private static final int MANOR_REFRESH_MIN = Config.ALT_MANOR_REFRESH_MIN;
	protected static final long MAINTENANCE_PERIOD = Config.ALT_MANOR_MAINTENANCE_PERIOD;
	
	private Calendar _manorRefresh;
	private Calendar _periodApprove;
	
	private boolean _underMaintenance;
	private boolean _disabled;
	
	protected ScheduledFuture<?> _scheduledManorRefresh;
	protected ScheduledFuture<?> _scheduledMaintenanceEnd;
	protected ScheduledFuture<?> _scheduledNextPeriodapprove;
	
	public static final CastleManorManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected CastleManorManager()
	{
		load();
		init();
		_underMaintenance = false;
		_disabled = !Config.ALLOW_MANOR;
		
		boolean isApproved;
		if (_periodApprove.getTimeInMillis() > _manorRefresh.getTimeInMillis())
		{
			isApproved = (_manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis());
		}
		else
		{
			isApproved = ((_periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()));
		}
		
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			c.setNextPeriodApproved(isApproved);
		}
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statementProduction = con.prepareStatement(CASTLE_MANOR_LOAD_PRODUCTION);
			PreparedStatement statementProcure = con.prepareStatement(CASTLE_MANOR_LOAD_PROCURE);
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				FastList<SeedProduction> production = new FastList<>();
				FastList<SeedProduction> productionNext = new FastList<>();
				FastList<CropProcure> procure = new FastList<>();
				FastList<CropProcure> procureNext = new FastList<>();
				
				statementProduction.setInt(1, castle.getId());
				ResultSet rs = statementProduction.executeQuery();
				statementProduction.clearParameters();
				while (rs.next())
				{
					int seedId = rs.getInt("seed_id");
					int canProduce = rs.getInt("can_produce");
					int startProduce = rs.getInt("start_produce");
					int price = rs.getInt("seed_price");
					int period = rs.getInt("period");
					if (period == PERIOD_CURRENT)
					{
						production.add(new SeedProduction(seedId, canProduce, price, startProduce));
					}
					else
					{
						productionNext.add(new SeedProduction(seedId, canProduce, price, startProduce));
					}
				}
				rs.close();
				
				castle.setSeedProduction(production, PERIOD_CURRENT);
				castle.setSeedProduction(productionNext, PERIOD_NEXT);
				
				statementProcure.setInt(1, castle.getId());
				rs = statementProcure.executeQuery();
				statementProcure.clearParameters();
				while (rs.next())
				{
					int cropId = rs.getInt("crop_id");
					int canBuy = rs.getInt("can_buy");
					int startBuy = rs.getInt("start_buy");
					int rewardType = rs.getInt("reward_type");
					int price = rs.getInt("price");
					int period = rs.getInt("period");
					if (period == PERIOD_CURRENT)
					{
						procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
					}
					else
					{
						procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
					}
				}
				rs.close();
				
				castle.setCropProcure(procure, PERIOD_CURRENT);
				castle.setCropProcure(procureNext, PERIOD_NEXT);
				
				if (!procure.isEmpty() || !procureNext.isEmpty() || !production.isEmpty() || !productionNext.isEmpty())
				{
					_log.info(getClass().getSimpleName() + ": " + castle.getName() + ": Data loaded");
				}
			}
			statementProduction.close();
			statementProcure.close();
		}
		catch (Exception e)
		{
			_log.info("Error restoring manor data: " + e.getMessage());
		}
	}
	
	private void init()
	{
		_manorRefresh = Calendar.getInstance();
		_manorRefresh.set(Calendar.HOUR_OF_DAY, MANOR_REFRESH);
		_manorRefresh.set(Calendar.MINUTE, MANOR_REFRESH_MIN);
		
		_periodApprove = Calendar.getInstance();
		_periodApprove.set(Calendar.HOUR_OF_DAY, NEXT_PERIOD_APPROVE);
		_periodApprove.set(Calendar.MINUTE, NEXT_PERIOD_APPROVE_MIN);
		
		updateManorRefresh();
		updatePeriodApprove();
	}
	
	public void updateManorRefresh()
	{
		_log.info("Manor System: Manor refresh updated");
		
		_scheduledManorRefresh = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (!isDisabled())
				{
					setUnderMaintenance(true);
					_log.info("Manor System: Under maintenance mode started");
					
					_scheduledMaintenanceEnd = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							_log.info("Manor System: Next period started");
							setNextPeriod();
							try
							{
								save();
							}
							catch (Exception e)
							{
								_log.log(Level.WARNING, "Manor System: Failed to save manor data: " + e.getMessage(), e);
							}
							setUnderMaintenance(false);
						}
					}, MAINTENANCE_PERIOD);
				}
				updateManorRefresh();
			}
		}, getMillisToManorRefresh());
	}
	
	public void updatePeriodApprove()
	{
		_log.info("Manor System: Manor period approve updated");
		
		_scheduledNextPeriodapprove = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (!isDisabled())
				{
					approveNextPeriod();
					_log.info("Manor System: Next period approved");
				}
				updatePeriodApprove();
			}
		}, getMillisToNextPeriodApprove());
	}
	
	public long getMillisToManorRefresh()
	{
		if ((_manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) < 120000)
		{
			setNewManorRefresh();
		}
		
		_log.info("Manor System: New Schedule for manor refresh @ " + _manorRefresh.getTime());
		
		return (_manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	public void setNewManorRefresh()
	{
		_manorRefresh = Calendar.getInstance();
		_manorRefresh.set(Calendar.HOUR_OF_DAY, MANOR_REFRESH);
		_manorRefresh.set(Calendar.MINUTE, MANOR_REFRESH_MIN);
		_manorRefresh.set(Calendar.SECOND, 0);
		_manorRefresh.add(Calendar.HOUR_OF_DAY, 24);
	}
	
	public long getMillisToNextPeriodApprove()
	{
		if ((_periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) < 120000)
		{
			setNewPeriodApprove();
		}
		
		_log.info("Manor System: New Schedule for period approve @ " + _periodApprove.getTime());
		
		return (_periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	public void setNewPeriodApprove()
	{
		_periodApprove = Calendar.getInstance();
		_periodApprove.set(Calendar.HOUR_OF_DAY, NEXT_PERIOD_APPROVE);
		_periodApprove.set(Calendar.MINUTE, NEXT_PERIOD_APPROVE_MIN);
		_periodApprove.set(Calendar.SECOND, 0);
		_periodApprove.add(Calendar.HOUR_OF_DAY, 24);
	}
	
	public void setNextPeriod()
	{
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			if (c.getOwnerId() <= 0)
			{
				continue;
			}
			L2Clan clan = ClanHolder.getInstance().getClan(c.getOwnerId());
			if (clan == null)
			{
				continue;
			}
			
			ItemContainer cwh = clan.getWarehouse();
			if (!(cwh instanceof ClanWarehouse))
			{
				_log.info("Can't get clan warehouse for clan " + ClanHolder.getInstance().getClan(c.getOwnerId()));
				continue;
			}
			
			for (CropProcure crop : c.getCropProcure(PERIOD_CURRENT))
			{
				if (crop.getStartAmount() == 0)
				{
					continue;
				}
				
				if ((crop.getStartAmount() - crop.getAmount()) > 0)
				{
					long count = crop.getStartAmount() - crop.getAmount();
					count = (count * 90) / 100;
					if (count < 1)
					{
						if (Rnd.nextInt(99) < 90)
						{
							count = 1;
						}
					}
					if (count > 0)
					{
						cwh.addItem("Manor", ManorParser.getInstance().getMatureCrop(crop.getId()), count, null, null);
					}
				}
				
				if (crop.getAmount() > 0)
				{
					c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice());
				}
			}
			
			c.setSeedProduction(c.getSeedProduction(PERIOD_NEXT), PERIOD_CURRENT);
			c.setCropProcure(c.getCropProcure(PERIOD_NEXT), PERIOD_CURRENT);
			
			if (c.getTreasury() < c.getManorCost(PERIOD_CURRENT))
			{
				c.setSeedProduction(getNewSeedsList(c.getId()), PERIOD_NEXT);
				c.setCropProcure(getNewCropsList(c.getId()), PERIOD_NEXT);
			}
			else
			{
				FastList<SeedProduction> production = new FastList<>();
				for (SeedProduction s : c.getSeedProduction(PERIOD_CURRENT))
				{
					s.setCanProduce(s.getStartProduce());
					production.add(s);
				}
				c.setSeedProduction(production, PERIOD_NEXT);
				
				FastList<CropProcure> procure = new FastList<>();
				for (CropProcure cr : c.getCropProcure(PERIOD_CURRENT))
				{
					cr.setAmount(cr.getStartAmount());
					procure.add(cr);
				}
				c.setCropProcure(procure, PERIOD_NEXT);
			}
			
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				c.saveCropData();
				c.saveSeedData();
			}
			L2PcInstance clanLeader = null;
			clanLeader = L2World.getInstance().getPlayer(clan.getLeader().getName());
			if (clanLeader != null)
			{
				clanLeader.sendPacket(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
			}
			
			c.setNextPeriodApproved(false);
		}
	}
	
	public void approveNextPeriod()
	{
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			boolean notFunc = false;
			
			if (c.getOwnerId() <= 0)
			{
				c.setCropProcure(new FastList<CropProcure>(), PERIOD_NEXT);
				c.setSeedProduction(new FastList<SeedProduction>(), PERIOD_NEXT);
			}
			else if (c.getTreasury() < c.getManorCost(PERIOD_NEXT))
			{
				notFunc = true;
				_log.info("Manor for castle " + c.getName() + " disabled, not enough adena in treasury: " + c.getTreasury() + ", " + c.getManorCost(PERIOD_NEXT) + " required.");
				c.setSeedProduction(getNewSeedsList(c.getId()), PERIOD_NEXT);
				c.setCropProcure(getNewCropsList(c.getId()), PERIOD_NEXT);
			}
			else
			{
				ItemContainer cwh = ClanHolder.getInstance().getClan(c.getOwnerId()).getWarehouse();
				if (!(cwh instanceof ClanWarehouse))
				{
					_log.info("Can't get clan warehouse for clan " + ClanHolder.getInstance().getClan(c.getOwnerId()));
					continue;
				}
				int slots = 0;
				for (CropProcure crop : c.getCropProcure(PERIOD_NEXT))
				{
					if (crop.getStartAmount() > 0)
					{
						if (cwh.getItemByItemId(ManorParser.getInstance().getMatureCrop(crop.getId())) == null)
						{
							slots++;
						}
					}
				}
				if (!cwh.validateCapacity(slots))
				{
					notFunc = true;
					_log.info("Manor for castle " + c.getName() + " disabled, not enough free slots in clan warehouse: " + (Config.WAREHOUSE_SLOTS_CLAN - cwh.getSize()) + ", but " + slots + " required.");
					c.setSeedProduction(getNewSeedsList(c.getId()), PERIOD_NEXT);
					c.setCropProcure(getNewCropsList(c.getId()), PERIOD_NEXT);
				}
			}
			c.setNextPeriodApproved(true);
			c.addToTreasuryNoTax((-1) * c.getManorCost(PERIOD_NEXT));
			
			if (notFunc)
			{
				L2Clan clan = ClanHolder.getInstance().getClan(c.getOwnerId());
				L2PcInstance clanLeader = null;
				if (clan != null)
				{
					clanLeader = L2World.getInstance().getPlayer(clan.getLeaderId());
				}
				if (clanLeader != null)
				{
					clanLeader.sendPacket(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
				}
			}
		}
	}
	
	private List<SeedProduction> getNewSeedsList(int castleId)
	{
		List<SeedProduction> seeds = new ArrayList<>();
		List<Integer> seedsIds = ManorParser.getInstance().getSeedsForCastle(castleId);
		for (int sd : seedsIds)
		{
			seeds.add(new SeedProduction(sd));
		}
		return seeds;
	}
	
	private List<CropProcure> getNewCropsList(int castleId)
	{
		List<CropProcure> crops = new ArrayList<>();
		List<Integer> cropsIds = ManorParser.getInstance().getCropsForCastle(castleId);
		for (int cr : cropsIds)
		{
			crops.add(new CropProcure(cr));
		}
		return crops;
	}
	
	public boolean isUnderMaintenance()
	{
		return _underMaintenance;
	}
	
	public void setUnderMaintenance(boolean mode)
	{
		_underMaintenance = mode;
	}
	
	public boolean isDisabled()
	{
		return _disabled;
	}
	
	public void setDisabled(boolean mode)
	{
		_disabled = mode;
	}
	
	public SeedProduction getNewSeedProduction(int id, long amount, long price, long sales)
	{
		return new SeedProduction(id, amount, price, sales);
	}
	
	public CropProcure getNewCropProcure(int id, long amount, int type, long price, long buy)
	{
		return new CropProcure(id, amount, type, buy, price);
	}
	
	public void save()
	{
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			c.saveSeedData();
			c.saveCropData();
		}
	}
	
	private static class SingletonHolder
	{
		protected static final CastleManorManager _instance = new CastleManorManager();
	}
}