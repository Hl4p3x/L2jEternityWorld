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
package l2e.gameserver.customs;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomMessage
{
	private static final Logger _log = Logger.getLogger(CustomMessage.class.getName());
	
	private int _index = 0;
	private String _message;
	private String _messageName;
	private boolean _isStoreType;
	private ArrayList<String> _args;
	
	public CustomMessage(String unicName, String lang)
	{
		_message = LocalizationStorage.getInstance().getString(lang, unicName);
		if (_message == null)
		{
			_log.log(Level.SEVERE, "CustomMessage[getString()]: message named \"" + unicName + "\" not found!");
			_message = "";
		}
	}
	
	public CustomMessage(String unicName, boolean isStoreType)
	{
		_messageName = unicName;
		_isStoreType = isStoreType;
	}
	
	public void add(Object l)
	{
		if (_isStoreType)
		{
			getStoredArgs().add(String.valueOf(l));
		}
		else
		{
			_message = _message.replace(String.format("{%d}", _index), String.valueOf(l));
			_index++;
		}
	}
	
	@Override
	public String toString()
	{
		if (_isStoreType)
		{
			return toString("en");
		}
		return _message;
	}
	
	public String toString(String lang)
	{
		if (!_isStoreType)
		{
			return "";
		}
		_message = LocalizationStorage.getInstance().getString(lang, _messageName);
		if (_message == null)
		{
			_log.log(Level.SEVERE, "CustomMessage[getString()]: message named \"" + _messageName + "\" not found!");
			return "";
		}
		for (String arg : getStoredArgs())
		{
			_message = _message.replace(String.format("{%d}", _index), arg);
			_index++;
		}
		_index = 0;
		return _message;
	}
	
	private ArrayList<String> getStoredArgs()
	{
		if (_args == null)
		{
			_args = new ArrayList<>();
		}
		return _args;
	}
}