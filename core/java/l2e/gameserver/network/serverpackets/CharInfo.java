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
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Decoy;
import l2e.gameserver.model.actor.instance.L2CubicInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.itemcontainer.Inventory;

public class CharInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final Inventory _inv;
	private int _objId;
	private int _x, _y, _z, _heading;
	private final int _mAtkSpd, _pAtkSpd;
	
	private final int _runSpd;
	private final int _walkSpd;
	private final float _moveMultiplier, _attackSpeedMultiplier;
	
	private int _vehicleId, _airShipHelm;
	
	public CharInfo(L2PcInstance cha)
	{
		_activeChar = cha;
		_objId = cha.getObjectId();
		_inv = cha.getInventory();
		if ((_activeChar.getVehicle() != null) && (_activeChar.getInVehiclePosition() != null))
		{
			_x = _activeChar.getInVehiclePosition().getX();
			_y = _activeChar.getInVehiclePosition().getY();
			_z = _activeChar.getInVehiclePosition().getZ() + Config.CLIENT_SHIFTZ;
			_vehicleId = _activeChar.getVehicle().getObjectId();
			if (_activeChar.isInAirShip() && _activeChar.getAirShip().isCaptain(_activeChar))
			{
				_airShipHelm = _activeChar.getAirShip().getHelmItemId();
			}
			else
			{
				_airShipHelm = 0;
			}
		}
		else
		{
			_x = _activeChar.getX();
			_y = _activeChar.getY();
			_z = _activeChar.getZ() + Config.CLIENT_SHIFTZ;
			_vehicleId = 0;
			_airShipHelm = 0;
		}
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_moveMultiplier = _activeChar.getMovementSpeedMultiplier();
		_attackSpeedMultiplier = _activeChar.getAttackSpeedMultiplier();
		_runSpd = (int) (_activeChar.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) (_activeChar.getWalkSpeed() / _moveMultiplier);
		_invisible = cha.isInvisible();
	}
	
	public CharInfo(L2Decoy decoy)
	{
		this(decoy.getActingPlayer());
		_vehicleId = 0;
		_airShipHelm = 0;
		_objId = decoy.getObjectId();
		_x = decoy.getX();
		_y = decoy.getY();
		_z = decoy.getZ();
		_heading = decoy.getHeading();
	}
	
	@Override
	protected final void writeImpl()
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
		
		if (_activeChar.getPoly().isMorphed())
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
			
			if (template != null)
			{
				writeC(0x0c);
				writeD(_objId);
				writeD(template.getId() + 1000000);
				writeD(_activeChar.getKarma() > 0 ? 1 : 0);
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
				writeF(_attackSpeedMultiplier);
				writeF(template.getfCollisionRadius());
				writeF(template.getfCollisionHeight());
				writeD(template.getRightHand());
				writeD(0x00);
				writeD(template.getLeftHand());
				writeC(1);
				writeC(_activeChar.isRunning() ? 1 : 0);
				writeC(_activeChar.isInCombat() ? 1 : 0);
				writeC(_activeChar.isAlikeDead() ? 1 : 0);
				writeC(!gmSeeInvis && _invisible ? 1 : 0);
				
				writeD(-1);
				writeS(_activeChar.getAppearance().getVisibleName());
				writeD(-1);
				writeS(gmSeeInvis ? "Invisible" : _activeChar.getAppearance().getVisibleTitle());
				
				writeD(_activeChar.getAppearance().getTitleColor());
				writeD(_activeChar.getPvpFlag());
				writeD(_activeChar.getKarma());
				
				writeD(gmSeeInvis ? (_activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask()) : _activeChar.getAbnormalEffect());
				
				writeD(_activeChar.getClanId());
				writeD(_activeChar.getClanCrestId());
				writeD(_activeChar.getAllyId());
				writeD(_activeChar.getAllyCrestId());
				
				writeC(_activeChar.isFlying() ? 2 : 0);
				writeC(_activeChar.getTeam());
				
				writeF(template.getfCollisionRadius());
				writeF(template.getfCollisionHeight());
				
				writeD(0x00);
				writeD(_activeChar.isFlying() ? 2 : 0);
				
				writeD(0x00);
				
				writeD(0x00);
				writeC(template.getAIDataStatic().isTargetable() ? 0x01 : 0x00);
				writeC(template.getAIDataStatic().showName() ? 0x01 : 0x00);
				writeC(_activeChar.getSpecialEffect());
				writeD(0x00);
			}
			else
			{
				_log.warning("Character " + _activeChar.getName() + " (" + _activeChar.getObjectId() + ") morphed in a Npc (" + _activeChar.getPoly().getPolyId() + ") w/o template.");
			}
		}
		else
		{
			writeC(0x31);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_vehicleId);
			writeD(_objId);
			
			if ((Config.ENABLE_OLY_FEED) && (_activeChar.isInOlympiadMode()))
			{
				writeS("Player");
				writeD(Config.OLY_ANTI_FEED_RACE);
				writeD(Config.OLY_ANTI_FEED_GENDER);
			}
			else
			{
				writeS(_activeChar.getAppearance().getVisibleName());
				writeD(_activeChar.getRace().ordinal());
				writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
			}
			
			if (_activeChar.getClassIndex() == 0)
			{
				writeD(_activeChar.getClassId().getId());
			}
			else
			{
				writeD(_activeChar.getBaseClass());
			}
			
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_UNDER));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_HEAD));
			
			if ((Config.ENABLE_OLY_FEED) && (_activeChar.isInOlympiadMode()))
			{
				writeD(Config.OLY_ANTI_FEED_WEAPON_RIGHT);
				writeD(Config.OLY_ANTI_FEED_WEAPON_LEFT);
			}
			else
			{
				writeD(_airShipHelm == 0 ? _inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_RHAND) : _airShipHelm);
				writeD(_airShipHelm == 0 ? _inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_LHAND) : 0);
			}
			
			if ((Config.ENABLE_OLY_FEED) && (_activeChar.isInOlympiadMode()))
			{
				writeD(Config.OLY_ANTI_FEED_GLOVES);
				writeD(Config.OLY_ANTI_FEED_CHEST);
				writeD(Config.OLY_ANTI_FEED_LEGS);
				writeD(Config.OLY_ANTI_FEED_FEET);
				writeD(Config.OLY_ANTI_FEED_CLOAK);
				writeD(Config.OLY_ANTI_FEED_RIGH_HAND_ARMOR);
				writeD(Config.OLY_ANTI_FEED_HAIR_MISC_1);
				writeD(Config.OLY_ANTI_FEED_HAIR_MISC_2);
			}
			else
			{
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_GLOVES));
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_CHEST));
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_LEGS));
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_FEET));
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_CLOAK));
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_RHAND));
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_HAIR));
				writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_HAIR2));
			}
			
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_RBRACELET));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_LBRACELET));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO1));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO2));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO3));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO4));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO5));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO6));
			writeD(_inv.getPaperdollItemDisplayId(Inventory.PAPERDOLL_BELT));
			
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
			
			writeD(_airShipHelm == 0 ? _inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND) : _airShipHelm);
			writeD(_airShipHelm == 0 ? _inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND) : 0);
			
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_GLOVES));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_CHEST));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LEGS));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_FEET));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_CLOAK));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR2));
			
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RBRACELET));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LBRACELET));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO1));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO2));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO3));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO4));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO5));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO6));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_BELT));
			
			writeD(0x00);
			writeD(0x01);
			
			writeD(_activeChar.getPvpFlag());
			writeD(_activeChar.getKarma());
			
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			
			writeD(0x00);
			
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeF(_activeChar.getMovementSpeedMultiplier());
			writeF(_activeChar.getAttackSpeedMultiplier());
			
			if ((Config.ENABLE_OLY_FEED) && (_activeChar.isInOlympiadMode()))
			{
				writeF(Config.OLY_ANTI_FEED_CLASS_RADIUS);
				writeF(Config.OLY_ANTI_FEED_CLASS_HEIGHT);
			}
			else
			{
				writeF(_activeChar.getCollisionRadius());
				writeF(_activeChar.getCollisionHeight());
			}
			
			writeD(_activeChar.getAppearance().getHairStyle());
			writeD(_activeChar.getAppearance().getHairColor());
			writeD(_activeChar.getAppearance().getFace());
			
			writeS(gmSeeInvis ? "Invisible" : _activeChar.getAppearance().getVisibleTitle());
			
			if (!_activeChar.isCursedWeaponEquipped())
			{
				writeD(_activeChar.getClanId());
				writeD(_activeChar.getClanCrestId());
				writeD(_activeChar.getAllyId());
				writeD(_activeChar.getAllyCrestId());
			}
			else
			{
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
			}
			
			writeC(_activeChar.isSitting() ? 0 : 1);
			writeC(_activeChar.isRunning() ? 1 : 0);
			writeC(_activeChar.isInCombat() ? 1 : 0);
			
			writeC(!_activeChar.isInOlympiadMode() && _activeChar.isAlikeDead() ? 1 : 0);
			
			writeC(!gmSeeInvis && _invisible ? 1 : 0);
			
			writeC(_activeChar.getMountType().ordinal());
			writeC(_activeChar.getPrivateStoreType());
			
			writeH(_activeChar.getCubics().size());
			for (L2CubicInstance c : _activeChar.getCubics())
			{
				writeH(c.getId());
			}
			
			writeC(_activeChar.isInPartyMatchRoom() ? 1 : 0);
			
			writeD(gmSeeInvis ? (_activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask()) : _activeChar.getAbnormalEffect());
			
			writeC(_activeChar.isFlyingMounted() ? 2 : 0);
			
			if ((Config.ENABLE_OLY_FEED) && (_activeChar.isInOlympiadMode()))
			{
				writeH(Config.OLY_ANTI_FEED_PLAYER_HAVE_RECS);
			}
			else
			{
				writeH(_activeChar.getRecomHave());
			}
			
			writeD(_activeChar.getMountNpcId() + 1000000);
			
			writeD(_activeChar.getClassId().getId());
			
			writeD(0x00);
			writeC(_activeChar.isMounted() || (_airShipHelm != 0) ? 0 : _activeChar.getEnchantEffect());
			
			writeC(_activeChar.getTeam());
			
			writeD(_activeChar.getClanCrestLargeId());
			writeC(_activeChar.isNoble() ? 1 : 0);
			writeC(_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA) ? 1 : 0);
			
			writeC(_activeChar.isFishing() ? 1 : 0);
			writeD(_activeChar.getFishx());
			writeD(_activeChar.getFishy());
			writeD(_activeChar.getFishz());
			
			writeD(_activeChar.getAppearance().getNameColor());
			
			writeD(_heading);
			
			writeD(_activeChar.getPledgeClass());
			writeD(_activeChar.getPledgeType());
			
			writeD(_activeChar.getAppearance().getTitleColor());
			
			writeD(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0);
			
			writeD(_activeChar.getClanId() > 0 ? _activeChar.getClan().getReputationScore() : 0);
			
			writeD(_activeChar.getTransformationId());
			writeD(_activeChar.getAgathionId());
			
			writeD(0x01);
			
			writeD(_activeChar.getSpecialEffect());
		}
	}
}