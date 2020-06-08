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
package l2e.gameserver.network.clientpackets;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.position.PcPosition;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		L2Skill skill = activeChar.getKnownSkill(_magicId);
		if (skill == null)
		{
			skill = activeChar.getCustomSkill(_magicId);
			if (skill == null)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				_log.warning("Skill Id " + _magicId + " not found in player!");
				return;
			}
		}
		
		if (activeChar.isPlayable() && activeChar.isInAirShip())
		{
			activeChar.sendPacket(SystemMessageId.ACTION_PROHIBITED_WHILE_MOUNTED_OR_ON_AN_AIRSHIP);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((activeChar.isTransformed() || activeChar.isInStance()) && !activeChar.hasTransformSkill(skill.getId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0) && skill.hasEffectType(L2EffectType.TELEPORT))
		{
			return;
		}
		
		if (skill.isToggle() && activeChar.isMounted())
		{
			return;
		}
		
		if (((skill.getSkillType() == L2SkillType.BUFF) && (skill.getTargetType() == L2TargetType.SELF)) && (!activeChar.isInAirShip() || !activeChar.isInBoat()))
		{
			final PcPosition charPos = activeChar.getPosition();
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(charPos.getX(), charPos.getY(), charPos.getZ(), charPos.getHeading()));
		}
		activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
	}
}