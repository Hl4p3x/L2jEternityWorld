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
package l2e.geoserver.util;

import java.io.File;

public final class Util
{
	public static int getAvailableProcessors()
	{
		Runtime rt = Runtime.getRuntime();
		return rt.availableProcessors();
	}
	
	public static String getOSName()
	{
		return System.getProperty("os.name");
	}
	
	public static String getOSVersion()
	{
		return System.getProperty("os.version");
	}
	
	public static String getOSArch()
	{
		return System.getProperty("os.arch");
	}
	
	public static String[] getMemUsage()
	{
		double maxMem = (Runtime.getRuntime().maxMemory() / 1024);
		double allocatedMem = (Runtime.getRuntime().totalMemory() / 1024);
		double nonAllocatedMem = maxMem - allocatedMem;
		double cachedMem = (Runtime.getRuntime().freeMemory() / 1024);
		double usedMem = allocatedMem - cachedMem;
		double useableMem = maxMem - usedMem;
		return new String[]
		{
			" - AllowedMemory: " + ((int) (maxMem)) + " KB",
			"Allocated: " + ((int) (allocatedMem)) + " KB (" + (((double) (Math.round((allocatedMem / maxMem) * 1000000))) / 10000) + "%)",
			"Non-Allocated: " + ((int) (nonAllocatedMem)) + " KB (" + (((double) (Math.round((nonAllocatedMem / maxMem) * 1000000))) / 10000) + "%)",
			"- AllocatedMemory: " + ((int) (allocatedMem)) + " KB",
			"Used: " + ((int) (usedMem)) + " KB (" + (((double) (Math.round((usedMem / maxMem) * 1000000))) / 10000) + "%)",
			"Unused (cached): " + ((int) (cachedMem)) + " KB (" + (((double) (Math.round((cachedMem / maxMem) * 1000000))) / 10000) + "%)",
			"- UseableMemory: " + ((int) (useableMem)) + " KB (" + (((double) (Math.round((useableMem / maxMem) * 1000000))) / 10000) + "%)"
		};
	}
	
	public static String getRelativePath(File base, File file)
	{
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}
	
	public static void printSection(String s)
	{
		int maxlength = 79;
		s = "-[ " + s + " ]";
		int slen = s.length();
		if (slen > maxlength)
		{
			System.out.println(s);
			return;
		}
		int i;
		for (i = 0; i < (maxlength - slen); i++)
		{
			s = "=" + s;
		}
		System.out.println(s);
	}
}