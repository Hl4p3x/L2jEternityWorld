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

import java.util.Arrays;

import l2e.Config;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.ai.L2SummonAI;
import l2e.gameserver.ai.NextAction;
import l2e.gameserver.ai.NextAction.NextActionCallback;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SummonSkillsHolder;
import l2e.gameserver.data.xml.BotReportParser;
import l2e.gameserver.data.xml.PetsParser;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2BabyPetInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2e.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ChairSit;
import l2e.gameserver.network.serverpackets.ExAskCoupleAction;
import l2e.gameserver.network.serverpackets.ExBasicActionList;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.RecipeShopManageList;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import l2e.util.Rnd;

public final class RequestActionUse extends L2GameClientPacket
{
	private static final int SIN_EATER_ID = 12564;
	private static final int SWITCH_STANCE_ID = 6054;
	
	private static final NpcStringId[] NPC_STRINGS =
	{
		NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH,
		NpcStringId.HEY_WHAT_DO_YOU_EXPECT_OF_ME,
		NpcStringId.UGGGGGH_PUSH_ITS_NOT_COMING_OUT,
		NpcStringId.AH_I_MISSED_THE_MARK
	};
	
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = (readD() == 1);
		_shiftPressed = (readC() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((activeChar.isFakeDeath() && (_actionId != 0)) || activeChar.isDead() || activeChar.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Effect ef = null;
		if (((ef = activeChar.getFirstEffect(L2EffectType.ACTION_BLOCK)) != null) && !ef.checkCondition(_actionId))
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isTransformed())
		{
			int[] allowedActions = activeChar.isTransformed() ? ExBasicActionList.ACTIONS_ON_TRANSFORM : ExBasicActionList.DEFAULT_ACTION_LIST;
			if (!(Arrays.binarySearch(allowedActions, _actionId) >= 0))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				_log.warning("Player " + activeChar + " used action which he does not have! Id = " + _actionId + " transform: " + activeChar.getTransformation());
				return;
			}
		}
		
		final L2Summon summon = activeChar.getSummon();
		final L2Object target = activeChar.getTarget();
		
		switch (_actionId)
		{
			case 0:
				if (activeChar.isSitting() || !activeChar.isMoving() || activeChar.isFakeDeath())
				{
					useSit(activeChar, target);
				}
				else
				{
					final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, new NextActionCallback()
					{
						@Override
						public void doWork()
						{
							useSit(activeChar, target);
						}
					});
					
					activeChar.getAI().setNextAction(nextAction);
				}
				break;
			case 1:
				if (activeChar.isRunning())
				{
					activeChar.setWalking();
				}
				else
				{
					activeChar.setRunning();
				}
				break;
			case 10:
				activeChar.tryOpenPrivateSellStore(false);
				break;
			case 15:
				if (validateSummon(summon, true))
				{
					((L2SummonAI) summon.getAI()).notifyFollowStatusChange();
				}
				break;
			case 16:
				if (validateSummon(summon, true))
				{
					if (summon.canAttack(_ctrlPressed))
					{
						summon.doAttack();
					}
				}
				break;
			case 17:
				if (validateSummon(summon, true))
				{
					summon.cancelAction();
				}
				break;
			case 19:
				if (validateSummon(summon, true))
				{
					if (summon.isDead())
					{
						sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
						break;
					}
					
					if (summon.isAttackingNow() || summon.isInCombat() || summon.isMovementDisabled())
					{
						sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
						break;
					}
					
					if (summon.isHungry())
					{
						if (!((L2PetInstance) summon).getPetData().getFood().isEmpty())
						{
							sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
						}
						else
						{
							sendPacket(SystemMessageId.THE_HELPER_PET_CANNOT_BE_RETURNED);
						}
						break;
					}
					summon.unSummon(activeChar);
				}
				break;
			case 21:
				if (validateSummon(summon, false))
				{
					((L2SummonAI) summon.getAI()).notifyFollowStatusChange();
				}
				break;
			case 22:
				if (validateSummon(summon, false))
				{
					if (summon.canAttack(_ctrlPressed))
					{
						summon.doAttack();
					}
				}
				break;
			case 23:
				if (validateSummon(summon, false))
				{
					summon.cancelAction();
				}
				break;
			case 28:
				activeChar.tryOpenPrivateBuyStore();
				break;
			case 32:
				useSkill(4230, false);
				break;
			case 36:
				useSkill(4259, false);
				break;
			case 37:
				if (activeChar.isAlikeDead())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
				{
					activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}
				sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 38:
				activeChar.mountPlayer(summon);
				break;
			case 39:
				useSkill(4138, false);
				break;
			case 41:
				if (validateSummon(summon, false))
				{
					if ((target != null) && (target.isDoor() || (target instanceof L2SiegeFlagInstance)))
					{
						useSkill(4230, false);
					}
					else
					{
						sendPacket(SystemMessageId.INCORRECT_TARGET);
					}
				}
				break;
			case 42:
				useSkill(4378, activeChar, false);
				break;
			case 43:
				useSkill(4137, false);
				break;
			case 44:
				useSkill(4139, false);
				break;
			case 45:
				useSkill(4025, activeChar, false);
				break;
			case 46:
				useSkill(4261, false);
				break;
			case 47:
				useSkill(4260, false);
				break;
			case 48:
				useSkill(4068, false);
				break;
			case 51:
				if (activeChar.isAlikeDead())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != 0)
				{
					activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}
				sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			case 52:
				if (validateSummon(summon, false))
				{
					if (summon.isAttackingNow() || summon.isInCombat())
					{
						sendPacket(SystemMessageId.SERVITOR_NOT_RETURN_IN_BATTLE);
						break;
					}
					summon.unSummon(activeChar);
				}
				break;
			case 53:
				if (validateSummon(summon, false))
				{
					if ((target != null) && (summon != target) && !summon.isMovementDisabled())
					{
						summon.setFollowStatus(false);
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, target.getLocation());
					}
				}
				break;
			case 54:
				if (validateSummon(summon, true))
				{
					if ((target != null) && (summon != target) && !summon.isMovementDisabled())
					{
						summon.setFollowStatus(false);
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, target.getLocation());
					}
				}
				break;
			case 61:
				activeChar.tryOpenPrivateSellStore(true);
				break;
			case 65:
				if (Config.BOTREPORT_ENABLE)
				{
					BotReportParser.getInstance().reportBot(activeChar);
				}
				else
				{
					activeChar.sendMessage("This action is disable.");
				}
				break;
			case 67:
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().setCaptain(activeChar))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 68:
				if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
				{
					if (activeChar.getAirShip().setCaptain(null))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 69:
				AirShipManager.getInstance().sendAirShipTeleportList(activeChar);
				break;
			case 70:
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().isCaptain(activeChar))
					{
						if (activeChar.getAirShip().setCaptain(null))
						{
							activeChar.broadcastUserInfo();
						}
					}
					else if (activeChar.getAirShip().isInDock())
					{
						activeChar.getAirShip().oustPlayer(activeChar);
					}
				}
				break;
			case 71:
			case 72:
			case 73:
				useCoupleSocial(_actionId - 55);
				break;
			case 1000:
				if ((target != null) && target.isDoor())
				{
					useSkill(4079, false);
				}
				break;
			case 1001:
				if (validateSummon(summon, true) && (summon.getId() == SIN_EATER_ID))
				{
					summon.broadcastPacket(new NpcSay(summon.getObjectId(), Say2.NPC_ALL, summon.getId(), NPC_STRINGS[Rnd.get(NPC_STRINGS.length)]));
				}
				break;
			case 1003:
				useSkill(4710, true);
				break;
			case 1004:
				useSkill(4711, activeChar, true);
				break;
			case 1005:
				useSkill(4712, true);
				break;
			case 1006:
				useSkill(4713, activeChar, true);
				break;
			case 1007:
				useSkill(4699, activeChar, false);
				break;
			case 1008:
				useSkill(4700, activeChar, false);
				break;
			case 1009:
				useSkill(4701, false);
				break;
			case 1010:
				useSkill(4702, activeChar, false);
				break;
			case 1011:
				useSkill(4703, activeChar, false);
				break;
			case 1012:
				useSkill(4704, false);
				break;
			case 1013:
				useSkill(4705, false);
				break;
			case 1014:
				useSkill(4706, false);
				break;
			case 1015:
				useSkill(4707, false);
				break;
			case 1016:
				useSkill(4709, false);
				break;
			case 1017:
				useSkill(4708, false);
				break;
			case 1031:
				useSkill(5135, false);
				break;
			case 1032:
				useSkill(5136, false);
				break;
			case 1033:
				useSkill(5137, false);
				break;
			case 1034:
				useSkill(5138, false);
				break;
			case 1035:
				useSkill(5139, false);
				break;
			case 1036:
				useSkill(5142, false);
				break;
			case 1037:
				useSkill(5141, false);
				break;
			case 1038:
				useSkill(5140, false);
				break;
			case 1039:
				if ((target != null) && target.isDoor())
				{
					useSkill(5110, false);
				}
				break;
			case 1040:
				if ((target != null) && target.isDoor())
				{
					useSkill(5111, false);
				}
				break;
			case 1041:
				useSkill(5442, true);
				break;
			case 1042:
				useSkill(5444, true);
				break;
			case 1043:
				useSkill(5443, true);
				break;
			case 1044:
				useSkill(5445, true);
				break;
			case 1045:
				useSkill(5584, true);
				break;
			case 1046:
				useSkill(5585, true);
				break;
			case 1047:
				useSkill(5580, false);
				break;
			case 1048:
				useSkill(5581, false);
				break;
			case 1049:
				useSkill(5582, false);
				break;
			case 1050:
				useSkill(5583, false);
				break;
			case 1051:
				useSkill(5638, false);
				break;
			case 1052:
				useSkill(5639, false);
				break;
			case 1053:
				useSkill(5640, false);
				break;
			case 1054:
				useSkill(5643, false);
				break;
			case 1055:
				useSkill(5647, false);
				break;
			case 1056:
				useSkill(5648, false);
				break;
			case 1057:
				useSkill(5646, false);
				break;
			case 1058:
				useSkill(5652, false);
				break;
			case 1059:
				useSkill(5653, false);
				break;
			case 1060:
				useSkill(5654, false);
				break;
			case 1061:
				useSkill(5745, true);
				break;
			case 1062:
				useSkill(5746, true);
				break;
			case 1063:
				useSkill(5747, true);
				break;
			case 1064:
				useSkill(5748, true);
				break;
			case 1065:
				useSkill(5753, true);
				break;
			case 1066:
				useSkill(5749, true);
				break;
			case 1067:
				useSkill(5750, true);
				break;
			case 1068:
				useSkill(5751, true);
				break;
			case 1069:
				useSkill(5752, true);
				break;
			case 1070:
				useSkill(5771, true);
				break;
			case 1071:
				useSkill(5761, true);
				break;
			case 1072:
				useSkill(6046, true);
				break;
			case 1073:
				useSkill(6047, true);
				break;
			case 1074:
				useSkill(6048, true);
				break;
			case 1075:
				useSkill(6049, true);
				break;
			case 1076:
				useSkill(6050, true);
				break;
			case 1077:
				useSkill(6051, true);
				break;
			case 1078:
				useSkill(6052, true);
				break;
			case 1079:
				useSkill(6053, true);
				break;
			case 1080:
				useSkill(6041, false);
				break;
			case 1081:
				useSkill(6042, false);
				break;
			case 1082:
				useSkill(6043, false);
				break;
			case 1083:
				useSkill(6044, false);
				break;
			case 1084:
				if (summon instanceof L2BabyPetInstance)
				{
					useSkill(6054, true);
				}
				break;
			case 1086:
				useSkill(6094, false);
				break;
			case 1087:
				useSkill(6095, false);
				break;
			case 1088:
				useSkill(6096, false);
				break;
			case 1089:
				useSkill(6199, true);
				break;
			case 1090:
				useSkill(6205, true);
				break;
			case 1091:
				useSkill(6206, true);
				break;
			case 1092:
				useSkill(6207, true);
				break;
			case 1093:
				useSkill(6618, true);
				break;
			case 1094:
				useSkill(6681, true);
				break;
			case 1095:
				useSkill(6619, true);
				break;
			case 1096:
				useSkill(6682, true);
				break;
			case 1097:
				useSkill(6683, true);
				break;
			case 1098:
				useSkill(6684, true);
				break;
			case 5000:
				useSkill(23155, true);
				break;
			case 5001:
				useSkill(23167, true);
				break;
			case 5002:
				useSkill(23168, true);
				break;
			case 5003:
				useSkill(5749, true);
				break;
			case 5004:
				useSkill(5750, true);
				break;
			case 5005:
				useSkill(5751, true);
				break;
			case 5006:
				useSkill(5771, true);
				break;
			case 5007:
				useSkill(6046, true);
				break;
			case 5008:
				useSkill(6047, true);
				break;
			case 5009:
				useSkill(6048, true);
				break;
			case 5010:
				useSkill(6049, true);
				break;
			case 5011:
				useSkill(6050, true);
				break;
			case 5012:
				useSkill(6051, true);
				break;
			case 5013:
				useSkill(6052, true);
				break;
			case 5014:
				useSkill(6053, true);
				break;
			case 5015:
				useSkill(6054, true);
				break;
			case 12:
				tryBroadcastSocial(2);
				break;
			case 13:
				tryBroadcastSocial(3);
				break;
			case 14:
				tryBroadcastSocial(4);
				break;
			case 24:
				tryBroadcastSocial(6);
				break;
			case 25:
				tryBroadcastSocial(5);
				break;
			case 26:
				tryBroadcastSocial(7);
				break;
			case 29:
				tryBroadcastSocial(8);
				break;
			case 30:
				tryBroadcastSocial(9);
				break;
			case 31:
				tryBroadcastSocial(10);
				break;
			case 33:
				tryBroadcastSocial(11);
				break;
			case 34:
				tryBroadcastSocial(12);
				break;
			case 35:
				tryBroadcastSocial(13);
				break;
			case 62:
				tryBroadcastSocial(14);
				break;
			case 66:
				tryBroadcastSocial(15);
				break;
			default:
				_log.warning(activeChar.getName() + ": unhandled action type " + _actionId);
				break;
		}
	}
	
	protected boolean useSit(L2PcInstance activeChar, L2Object target)
	{
		if (activeChar.getMountType() != MountType.NONE)
		{
			return false;
		}
		
		if (!activeChar.isSitting() && (target instanceof L2StaticObjectInstance) && (((L2StaticObjectInstance) target).getType() == 1) && activeChar.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
		{
			final ChairSit cs = new ChairSit(activeChar, ((L2StaticObjectInstance) target).getId());
			sendPacket(cs);
			activeChar.sitDown();
			activeChar.broadcastPacket(cs);
			return true;
		}
		
		if (activeChar.isFakeDeath())
		{
			activeChar.stopEffects(L2EffectType.FAKE_DEATH);
		}
		else if (activeChar.isSitting())
		{
			activeChar.standUp();
		}
		else
		{
			activeChar.sitDown();
		}
		return true;
	}
	
	private void useSkill(int skillId, L2Object target, boolean pet)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2Summon summon = activeChar.getSummon();
		if (!validateSummon(summon, pet))
		{
			return;
		}
		
		if (summon instanceof L2BabyPetInstance)
		{
			if (!((L2BabyPetInstance) summon).isInSupportMode())
			{
				sendPacket(SystemMessageId.PET_AUXILIARY_MODE_CANNOT_USE_SKILLS);
				return;
			}
		}
		
		int lvl = 0;
		if (summon.isPet())
		{
			if ((summon.getLevel() - activeChar.getLevel()) > 20)
			{
				sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
				return;
			}
			lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
		}
		else
		{
			lvl = SummonSkillsHolder.getInstance().getAvailableLevel(summon, skillId);
		}
		
		if (lvl > 0)
		{
			summon.setTarget(target);
			summon.useMagic(SkillHolder.getInstance().getInfo(skillId, lvl), _ctrlPressed, _shiftPressed);
		}
		
		if (skillId == SWITCH_STANCE_ID)
		{
			summon.switchMode();
		}
	}
	
	private void useSkill(int skillId, boolean pet)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		useSkill(skillId, activeChar.getTarget(), pet);
	}
	
	private boolean validateSummon(L2Summon summon, boolean checkPet)
	{
		if ((summon != null) && ((checkPet && summon.isPet()) || summon.isServitor()))
		{
			if (summon.isBetrayed())
			{
				sendPacket(SystemMessageId.PET_REFUSING_ORDER);
				return false;
			}
			return true;
		}
		
		if (checkPet)
		{
			sendPacket(SystemMessageId.DONT_HAVE_PET);
		}
		else
		{
			sendPacket(SystemMessageId.DONT_HAVE_SERVITOR);
		}
		return false;
	}
	
	private void tryBroadcastSocial(int id)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.isFishing())
		{
			sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (activeChar.canMakeSocialAction())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), id));
		}
	}
	
	private void useCoupleSocial(final int id)
	{
		final L2PcInstance requester = getActiveChar();
		if (requester == null)
		{
			return;
		}
		
		final L2Object target = requester.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final int distance = (int) Math.sqrt(requester.getPlanDistanceSq(target));
		if ((distance > 125) || (distance < 15) || (requester.getObjectId() == target.getObjectId()))
		{
			sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
			return;
		}
		
		SystemMessage sm;
		if (requester.isInStoreMode() || requester.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInCombat() || requester.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(requester))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isFishing())
		{
			sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (requester.getKarma() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
		}
		
		if (requester.isMounted() || requester.isFlyingMounted() || requester.isInBoat() || requester.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		final L2PcInstance partner = target.getActingPlayer();
		if (partner.isInStoreMode() || partner.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInCombat() || partner.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(partner))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.getMultiSociaAction() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isFishing())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.getKarma() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isMounted() || partner.isFlyingMounted() || partner.isInBoat() || partner.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isTeleporting())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (requester.isAllSkillsDisabled() || partner.isAllSkillsDisabled())
		{
			sendPacket(SystemMessageId.COUPLE_ACTION_CANCELED);
			return;
		}
		
		requester.setMultiSocialAction(id, partner.getObjectId());
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_COUPLE_ACTION_C1);
		sm.addPcName(partner);
		sendPacket(sm);
		
		if ((requester.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE) || (partner.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE))
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, new NextActionCallback()
			{
				@Override
				public void doWork()
				{
					partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
				}
			});
			requester.getAI().setNextAction(nextAction);
			return;
		}
		
		if (requester.isCastingNow() || requester.isCastingSimultaneouslyNow())
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, new NextActionCallback()
			{
				@Override
				public void doWork()
				{
					partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
				}
			});
			requester.getAI().setNextAction(nextAction);
			return;
		}
		partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return (_actionId != 10) && (_actionId != 28);
	}
}