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
package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.L2CharacterAI;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.knownlist.StaticObjectKnownList;
import l2e.gameserver.model.actor.stat.StaticObjStat;
import l2e.gameserver.model.actor.status.StaticObjStatus;
import l2e.gameserver.model.actor.templates.L2CharTemplate;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.ShowTownMap;
import l2e.gameserver.network.serverpackets.StaticObject;

public final class L2StaticObjectInstance extends L2Character
{
	public static final int INTERACTION_DISTANCE = 150;
	
	private final int _staticObjectId;
	private int _meshIndex = 0;
	private int _type = -1;
	private ShowTownMap _map;
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		@Override
		public L2StaticObjectInstance getActor()
		{
			return L2StaticObjectInstance.this;
		}
		
		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}
		
		@Override
		public void moveTo(int x, int y, int z)
		{
		}
		
		@Override
		public void stopMove(Location loc)
		{
		}
		
		@Override
		public void doAttack(L2Character target)
		{
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
		}
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		return null;
	}
	
	@Override
	public int getId()
	{
		return _staticObjectId;
	}
	
	public L2StaticObjectInstance(int objectId, L2CharTemplate template, int staticId)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2StaticObjectInstance);
		_staticObjectId = staticId;
	}
	
	@Override
	public final StaticObjectKnownList getKnownList()
	{
		return (StaticObjectKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new StaticObjectKnownList(this));
	}
	
	@Override
	public final StaticObjStat getStat()
	{
		return (StaticObjStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new StaticObjStat(this));
	}
	
	@Override
	public final StaticObjStatus getStatus()
	{
		return (StaticObjStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new StaticObjStatus(this));
	}
	
	public int getType()
	{
		return _type;
	}
	
	public void setType(int type)
	{
		_type = type;
	}
	
	public void setMap(String texture, int x, int y)
	{
		_map = new ShowTownMap("town_map." + texture, x, y);
	}
	
	public ShowTownMap getMap()
	{
		return _map;
	}
	
	@Override
	public final int getLevel()
	{
		return 1;
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
		this.broadcastPacket(new StaticObject(this));
	}
	
	public int getMeshIndex()
	{
		return _meshIndex;
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new StaticObject(this));
	}
}