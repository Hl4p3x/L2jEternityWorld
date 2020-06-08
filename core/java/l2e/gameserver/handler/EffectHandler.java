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
package l2e.gameserver.handler;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.gameserver.handler.effecthandlers.*;
import l2e.gameserver.model.effects.L2Effect;

public final class EffectHandler
{
	private static final Logger _log = Logger.getLogger(EffectHandler.class.getName());
	private final FastMap<Integer, Class<? extends L2Effect>> _handlers;
	
	private static final Class<?> _loadInstances = EffectHandler.class;
	
	private static final Class<?>[] _effects =
	{
		AbortCast.class,
		Backstab.class,
		Betray.class,
		BigHead.class,
		BlockAction.class,
		BlockChat.class,
		BlockParty.class,
		BlockResurrection.class,
		Bluff.class,
		Buff.class,
		CallParty.class,
		CallPc.class,
		Cancel.class,
		CancelAll.class,
		ChameleonRest.class,
		ChanceSkillTrigger.class,
		ChangeFace.class,
		ChangeHairColor.class,
		ChangeHairStyle.class,
		CharmOfCourage.class,
		CharmOfLuck.class,
		ClanGate.class,
		Confusion.class,
		ConsumeBody.class,
		ConvertItem.class,
		CpHeal.class,
		CpHealOverTime.class,
		CpHealPercent.class,
		CpDamPercent.class,
		CubicMastery.class,
		DamOverTime.class,
		DamOverTimePercent.class,
		Debuff.class,
		Disarm.class,
		EnemyCharge.class,
		EnlargeAbnormalSlot.class,
		Escape.class,
		FakeDeath.class,
		Fear.class,
		Flag.class,
		FocusEnergy.class,
		FocusMaxEnergy.class,
		FocusSouls.class,
		Fusion.class,
		GiveRecommendation.class,
		GiveSp.class,
		Grow.class,
		Harvesting.class,
		HealOverTime.class,
		HealPercent.class,
		Heal.class,
		Hide.class,
		HolythingPossess.class,
		HpByLevel.class,
		ImmobileBuff.class,
		ImmobilePetBuff.class,
		Invincible.class,
		Lucky.class,
		ManaDamOverTime.class,
		ManaHeal.class,
		ManaHealByLevel.class,
		ManaHealOverTime.class,
		ManaHealPercent.class,
		MpByLevel.class,
		MpConsumePerLevel.class,
		Mute.class,
		NoblesseBless.class,
		OpenCommonRecipeBook.class,
		OpenDwarfRecipeBook.class,
		Paralyze.class,
		Petrification.class,
		PhoenixBless.class,
		PhysicalAttackMute.class,
		PhysicalMute.class,
		ProtectionBlessing.class,
		RebalanceHP.class,
		RecoBonus.class,
		RandomizeHate.class,
		Recovery.class,
		RefuelAirship.class,
		Relax.class,
		Restoration.class,
		RestorationRandom.class,
		Root.class,
		ServitorShare.class,
		SetSkill.class,
		Signet.class,
		SignetAntiSummon.class,
		SignetMDam.class,
		SignetNoise.class,
		SilentMove.class,
		Sleep.class,
		SoulEating.class,
		Spoil.class,
		StealBuffs.class,
		Stun.class,
		SummonAgathion.class,
		SummonCubic.class,
		SummonNpc.class,
		SummonPet.class,
		SummonTrap.class,
		Sweeper.class,
		TargetCancel.class,
		TargetMe.class,
		Teleport.class,
		TeleportToTarget.class,
		ThrowUp.class,
		TransferDamage.class,
		TransferHate.class,
		Transformation.class,
		TransformationDispel.class,
		UnsummonAgathion.class,
		Warp.class,
		VitalityPointUp.class,
	};
	
	protected EffectHandler()
	{
		_handlers = new FastMap<>();
	}
	
	public void registerHandler(String name, Class<? extends L2Effect> func)
	{
		_handlers.put(name.hashCode(), func);
	}
	
	public final Class<? extends L2Effect> getHandler(String name)
	{
		return _handlers.get(name.hashCode());
	}
	
	public int size()
	{
		return _handlers.size();
	}
	
	public void executeScript()
	{
		Object loadInstance = null;
		Method method = null;
		
		try
		{
			method = _loadInstances.getMethod("getInstance");
			loadInstance = method.invoke(_loadInstances);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed invoking getInstance method for handler: " + _loadInstances.getSimpleName(), e);
			return;
		}
		
		method = null;
		
		for (Class<?> c : _effects)
		{
			try
			{
				if (c == null)
				{
					continue;
				}
				
				if (method == null)
				{
					method = loadInstance.getClass().getMethod("registerHandler", String.class, Class.class);
				}
				
				method.invoke(loadInstance, c.getSimpleName(), c);
				
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Failed loading effect handler: " + c.getSimpleName(), e);
				continue;
			}
		}
		
		try
		{
			method = loadInstance.getClass().getMethod("size");
			Object returnVal = method.invoke(loadInstance);
			_log.log(Level.INFO, "SkillTreesParser: Loaded " + returnVal + " Effect Templates.");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed invoking size method for handler: " + loadInstance.getClass().getSimpleName(), e);
		}
	}
	
	private static final class SingletonHolder
	{
		protected static final EffectHandler _instance = new EffectHandler();
	}
	
	public static EffectHandler getInstance()
	{
		return SingletonHolder._instance;
	}
}