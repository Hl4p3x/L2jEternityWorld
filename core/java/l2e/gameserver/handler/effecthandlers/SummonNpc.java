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
package l2e.gameserver.handler.effecthandlers;

import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ChronoMonsterInstance;
import l2e.gameserver.model.actor.instance.L2DecoyInstance;
import l2e.gameserver.model.actor.instance.L2EffectPointInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import l2e.util.Rnd;

public class SummonNpc extends L2Effect
{
	private final int _despawnDelay;
	private final int _npcId;
	private final int _npcCount;
	private final boolean _randomOffset;
	private final boolean _isSummonSpawn;
	
	public SummonNpc(Env env, EffectTemplate template)
	{
		super(env, template);
		_despawnDelay = template.getParameters().getInteger("despawnDelay", 20000);
		_npcId = template.getParameters().getInteger("npcId", 0);
		_npcCount = template.getParameters().getInteger("npcCount", 1);
		_randomOffset = template.getParameters().getBool("randomOffset", false);
		_isSummonSpawn = template.getParameters().getBool("isSummonSpawn", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer() || getEffected().isAlikeDead() || getEffected().getActingPlayer().inObserverMode())
		{
			return false;
		}
		
		if ((_npcId <= 0) || (_npcCount <= 0))
		{
			_log.warning(SummonNpc.class.getSimpleName() + ": Invalid NPC Id or count skill Id: " + getSkill().getId());
			return false;
		}
		
		final L2PcInstance player = getEffected().getActingPlayer();
		if (player.isMounted())
		{
			return false;
		}
		
		final L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(_npcId);
		if (npcTemplate == null)
		{
			_log.warning(SummonNpc.class.getSimpleName() + ": Spawn of the nonexisting NPC Id: " + _npcId + ", skill Id:" + getSkill().getId());
			return false;
		}
		
		switch (npcTemplate.getType())
		{
			case "L2Decoy":
			{
				final L2DecoyInstance decoy = new L2DecoyInstance(IdFactory.getInstance().getNextId(), npcTemplate, player, _despawnDelay);
				decoy.setCurrentHp(decoy.getMaxHp());
				decoy.setCurrentMp(decoy.getMaxMp());
				decoy.setHeading(player.getHeading());
				decoy.setInstanceId(player.getInstanceId());
				decoy.spawnMe(player.getX(), player.getY(), player.getZ());
				player.setDecoy(decoy);
				break;
			}
			case "L2EffectPoint":
			{
				final L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), npcTemplate, player);
				effectPoint.setCurrentHp(effectPoint.getMaxHp());
				effectPoint.setCurrentMp(effectPoint.getMaxMp());
				int x = player.getX();
				int y = player.getY();
				int z = player.getZ();
				
				if (getSkill().getTargetType() == L2TargetType.GROUND)
				{
					final Location wordPosition = player.getActingPlayer().getCurrentSkillWorldPosition();
					if (wordPosition != null)
					{
						x = wordPosition.getX();
						y = wordPosition.getY();
						z = wordPosition.getZ();
					}
				}
				getSkill().getEffects(player, effectPoint);
				effectPoint.setIsInvul(true);
				effectPoint.spawnMe(x, y, z);
				break;
			}
			default:
			{
				L2Spawn spawn;
				try
				{
					spawn = new L2Spawn(npcTemplate);
				}
				catch (Exception e)
				{
					_log.warning(SummonNpc.class.getSimpleName() + ": " + e.getMessage());
					return false;
				}
				
				spawn.setInstanceId(player.getInstanceId());
				spawn.setHeading(-1);
				
				if (_randomOffset)
				{
					spawn.setX(player.getX() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20)));
					spawn.setY(player.getY() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20)));
				}
				else
				{
					spawn.setX(player.getX());
					spawn.setY(player.getY());
				}
				spawn.setZ(player.getZ() + 20);
				spawn.stopRespawn();
				
				final L2Npc npc = spawn.doSpawn(_isSummonSpawn);
				npc.setName(npcTemplate.getName());
				npc.setTitle(npcTemplate.getName());
				npc.setSummoner(player);
				if (_despawnDelay > 0)
				{
					npc.scheduleDespawn(_despawnDelay);
				}
				if (npc instanceof L2ChronoMonsterInstance)
				{
					((L2ChronoMonsterInstance) npc).setOwner(player);
					npc.setTitle(player.getName());
					npc.broadcastPacket(new NpcInfo(npc, null));
				}
				npc.setIsRunning(false);
			}
		}
		return true;
	}
}