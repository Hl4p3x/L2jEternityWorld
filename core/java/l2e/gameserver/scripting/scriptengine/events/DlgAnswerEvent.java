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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.scripting.scriptengine.events;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;

public class DlgAnswerEvent implements L2Event
{
	private L2PcInstance _activeChar;
	private int _messageId;
	private int _answer;
	private int _requesterId;
	
	public void setActiveChar(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}
	
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public void setMessageId(int messageId)
	{
		_messageId = messageId;
	}
	
	public int getMessageId()
	{
		return _messageId;
	}
	
	public SystemMessageId getSystemMessageId()
	{
		return SystemMessageId.getSystemMessageId(_messageId);
	}
	
	public void setAnswer(int answer)
	{
		_answer = answer;
	}
	
	public int getAnswer()
	{
		return _answer;
	}
	
	public void setRequesterId(int req)
	{
		_requesterId = req;
	}
	
	public int getRequesterId()
	{
		return _requesterId;
	}
}