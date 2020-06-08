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
package l2e.tools.i18n;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class LanguageControl extends Control
{
	public static final String LANGUAGES_DIRECTORY = "../languages/";
	
	public static final LanguageControl INSTANCE = new LanguageControl();
	
	protected LanguageControl()
	{	
	}
	
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException
	{
		if ((baseName == null) || (locale == null) || (format == null) || (loader == null))
		{
			throw new NullPointerException();
		}
		ResourceBundle bundle = null;
		if (format.equals("java.properties"))
		{
			format = "properties";
			String bundleName = toBundleName(baseName, locale);
			String resourceName = LANGUAGES_DIRECTORY + toResourceName(bundleName, format);
			
			try (FileInputStream fis = new FileInputStream(resourceName); BufferedInputStream bis = new BufferedInputStream(fis))
			{
				bundle = new PropertyResourceBundle(bis);
			}
		}
		return bundle;
	}
}