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

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;

public class GiveRecommendation extends L2Effect
{
	private final int _amount;
	
	public GiveRecommendation(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_amount = template.getParameters().getInteger("amount", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		L2PcInstance target = getEffected() instanceof L2PcInstance ? (L2PcInstance) getEffected() : null;
		if (target != null)
		{
			int recommendationsGiven = _amount;
			
			if ((target.getRecomHave() + _amount) >= 255)
			{
				recommendationsGiven = 255 - target.getRecomHave();
			}
			
			if (recommendationsGiven > 0)
			{
				target.setRecomHave(target.getRecomHave() + recommendationsGiven);
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
				sm.addNumber(recommendationsGiven);
				target.sendPacket(sm);
				target.sendPacket(new UserInfo(target));
				target.sendPacket(new ExVoteSystemInfo(target));
			}
			else
			{
				L2PcInstance player = getEffector() instanceof L2PcInstance ? (L2PcInstance) getEffector() : null;
				if (player != null)
				{
					player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
		}
		return true;
	}
}