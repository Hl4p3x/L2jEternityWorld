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
package l2e.gameserver.network.serverpackets;

import l2e.Config;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2TrapInstance;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.zone.ZoneId;

public abstract class AbstractNpcInfo extends L2GameServerPacket
{
	protected int _x, _y, _z, _heading;
	protected int _idTemplate;
	protected boolean _isAttackable, _isSummoned;
	protected int _mAtkSpd, _pAtkSpd;
	protected float _moveMultiplier;
	
	protected int _runSpd;
	
	protected int _walkSpd;
	
	protected int _rhand, _lhand, _chest, _enchantEffect;
	protected double _collisionHeight, _collisionRadius;
	protected String _name = "";
	protected String _title = "";
	
	public AbstractNpcInfo(L2Character cha)
	{
		_isSummoned = cha.isShowSummonAnimation();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ() + Config.CLIENT_SHIFTZ;
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = Math.round(cha.getWalkSpeed() / _moveMultiplier);
	}
	
	public static class NpcInfo extends AbstractNpcInfo
	{
		private final L2Npc _npc;
		private int _clanCrest = 0;
		private int _allyCrest = 0;
		private int _allyId = 0;
		private int _clanId = 0;
		private int _displayEffect = 0;
		
		public NpcInfo(L2Npc cha, L2Character attacker)
		{
			super(cha);
			_npc = cha;
			_idTemplate = cha.getTemplate().getIdTemplate();
			_rhand = cha.getRightHandItem();
			_lhand = cha.getLeftHandItem();
			_enchantEffect = cha.getEnchantEffect();
			_collisionHeight = cha.getCollisionHeight();
			_collisionRadius = cha.getCollisionRadius();
			_isAttackable = cha.isAutoAttackable(attacker);
			if (cha.getTemplate().isServerSideName())
			{
				_name = cha.getName();
			}
			
			if (_npc.isInvisible())
			{
				_title = "Invisible";
			}
			else if (Config.CHAMPION_ENABLE && cha.isChampion())
			{
				_title = (Config.CHAMP_TITLE);
			}
			else if (cha.getTemplate().isServerSideTitle())
			{
				_title = cha.getTemplate().getTitle();
			}
			else
			{
				_title = cha.getTitle();
			}
			
			if (Config.SHOW_NPC_LVL && (_npc instanceof L2MonsterInstance) && ((L2Attackable) _npc).canShowLevelInTitle())
			{
				String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
				if (_title != null)
				{
					t += " " + _title;
				}
				
				_title = t;
			}
			
			if ((cha instanceof L2NpcInstance) && cha.isInsideZone(ZoneId.TOWN) && (Config.SHOW_CREST_WITHOUT_QUEST || cha.getCastle().getShowNpcCrest()) && (cha.getCastle().getOwnerId() != 0))
			{
				int townId = TownManager.getTown(_x, _y, _z).getTownId();
				if ((townId != 33) && (townId != 22))
				{
					L2Clan clan = ClanHolder.getInstance().getClan(cha.getCastle().getOwnerId());
					_clanCrest = clan.getCrestId();
					_clanId = clan.getId();
					_allyCrest = clan.getAllyCrestId();
					_allyId = clan.getAllyId();
				}
			}
			_displayEffect = cha.getDisplayEffect();
		}
		
		@Override
		protected void writeImpl()
		{
			writeC(0x0c);
			writeD(_npc.getObjectId());
			writeD(_idTemplate + 1000000);
			writeD(_isAttackable ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z + Config.CLIENT_SHIFTZ);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeF(_moveMultiplier);
			writeF(_npc.getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand);
			writeD(_chest);
			writeD(_lhand);
			writeC(1);
			writeC(_npc.isRunning() ? 1 : 0);
			writeC(_npc.isInCombat() ? 1 : 0);
			writeC(_npc.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : 0);
			writeD(-1);
			writeS(_name);
			writeD(-1);
			writeS(_title);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeD(_npc.isInvisible() ? _npc.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _npc.getAbnormalEffect());
			writeD(_clanId);
			writeD(_clanCrest);
			writeD(_allyId);
			writeD(_allyCrest);
			writeC(_npc.isFlying() ? 2 : 0);
			if (Config.CHAMPION_ENABLE)
			{
				writeC(_npc.isChampion() ? Config.CHAMPION_ENABLE_AURA : 0);
			}
			else
			{
				writeC(_npc.getTeam());
			}
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_enchantEffect);
			writeD(_npc.isFlying() ? 1 : 0);
			writeD(0x00);
			writeD(_npc.getColorEffect());
			writeC(_npc.isTargetable() ? 0x01 : 0x00);
			writeC(_npc.isShowName() ? 0x01 : 0x00);
			writeD(_npc.getSpecialEffect());
			writeD(_displayEffect);
		}
	}
	
	public static class TrapInfo extends AbstractNpcInfo
	{
		private final L2TrapInstance _trap;
		
		public TrapInfo(L2TrapInstance cha, L2Character attacker)
		{
			super(cha);
			
			_trap = cha;
			_idTemplate = cha.getTemplate().getIdTemplate();
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = 0;
			_lhand = 0;
			_collisionHeight = _trap.getTemplate().getfCollisionHeight();
			_collisionRadius = _trap.getTemplate().getfCollisionRadius();
			if (cha.getTemplate().isServerSideName())
			{
				_name = cha.getName();
			}
			_title = cha.getOwner() != null ? cha.getOwner().getName() : "";
			_moveMultiplier = cha.getMovementSpeedMultiplier();
			_runSpd = Math.round(cha.getRunSpeed() / _moveMultiplier);
			_walkSpd = Math.round(cha.getWalkSpeed() / _moveMultiplier);
		}
		
		@Override
		protected void writeImpl()
		{
			writeC(0x0c);
			writeD(_trap.getObjectId());
			writeD(_idTemplate + 1000000);
			writeD(_isAttackable ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeF(_moveMultiplier);
			writeF(_trap.getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand);
			writeD(_chest);
			writeD(_lhand);
			writeC(1);
			writeC(1);
			writeC(_trap.isInCombat() ? 1 : 0);
			writeC(_trap.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : 0);
			writeD(-1);
			writeS(_name);
			writeD(-1);
			writeS(_title);
			writeD(0x00);
			
			writeD(_trap.getPvpFlag());
			writeD(_trap.getKarma());
			
			writeD(_trap.isInvisible() ? _trap.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _trap.getAbnormalEffect());
			writeD(0x00);
			writeD(0x00);
			writeD(0000);
			writeD(0000);
			writeC(0000);
			
			writeC(_trap.getTeam());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0);
			writeC(0x01);
			writeC(0x01);
			writeD(0x00);
		}
	}
	
	public static class SummonInfo extends AbstractNpcInfo
	{
		private final L2Summon _summon;
		private int _form = 0;
		private int _val = 0;
		
		public SummonInfo(L2Summon cha, L2Character attacker, int val)
		{
			super(cha);
			_summon = cha;
			_val = val;
			if (_summon.isShowSummonAnimation())
			{
				_val = 2;
			}
			
			int npcId = cha.getTemplate().getId();
			
			if ((npcId == 16041) || (npcId == 16042))
			{
				if (cha.getLevel() > 69)
				{
					_form = 3;
				}
				else if (cha.getLevel() > 64)
				{
					_form = 2;
				}
				else if (cha.getLevel() > 59)
				{
					_form = 1;
				}
			}
			else if ((npcId == 16025) || (npcId == 16037))
			{
				if (cha.getLevel() > 69)
				{
					_form = 3;
				}
				else if (cha.getLevel() > 64)
				{
					_form = 2;
				}
				else if (cha.getLevel() > 59)
				{
					_form = 1;
				}
			}
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = cha.getWeapon();
			_lhand = 0;
			_chest = cha.getArmor();
			_enchantEffect = cha.getTemplate().getEnchantEffect();
			_name = cha.getName();
			_title = cha.getOwner() != null ? ((!cha.getOwner().isOnline()) ? "" : cha.getOwner().getName()) : "";
			_idTemplate = cha.getTemplate().getIdTemplate();
			_collisionHeight = cha.getTemplate().getfCollisionHeight();
			_collisionRadius = cha.getTemplate().getfCollisionRadius();
			_invisible = cha.isInvisible();
			_moveMultiplier = cha.getMovementSpeedMultiplier();
			_runSpd = Math.round(cha.getRunSpeed() / _moveMultiplier);
			_walkSpd = Math.round(cha.getWalkSpeed() / _moveMultiplier);
		}
		
		@Override
		protected void writeImpl()
		{
			boolean gmSeeInvis = false;
			if (_invisible)
			{
				final L2PcInstance activeChar = getClient().getActiveChar();
				if ((activeChar != null) && activeChar.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
				{
					gmSeeInvis = true;
				}
			}
			
			writeC(0x0c);
			writeD(_summon.getObjectId());
			writeD(_idTemplate + 1000000);
			writeD(_isAttackable ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeF(_moveMultiplier);
			writeF(_summon.getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand);
			writeD(_chest);
			writeD(_lhand);
			writeC(0x01);
			writeC(0x01);
			writeC(_summon.isInCombat() ? 1 : 0);
			writeC(_summon.isAlikeDead() ? 1 : 0);
			writeC(_val);
			writeD(-1);
			writeS(_name);
			writeD(-1);
			writeS(_title);
			writeD(0x01);
			
			writeD(_summon.getPvpFlag());
			writeD(_summon.getKarma());
			
			writeD(gmSeeInvis ? _summon.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _summon.getAbnormalEffect());
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeC(0x00);
			
			writeC(_summon.getTeam());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_enchantEffect);
			writeD(0x00);
			writeD(0x00);
			writeD(_form);
			writeC(0x01);
			writeC(0x01);
			writeD(_summon.getSpecialEffect());
		}
	}
}