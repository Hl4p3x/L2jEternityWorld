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
package l2e.gameserver.util;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.util.file.filter.ExtFilter;

public final class Util
{
	private static final NumberFormat ADENA_FORMATTER = NumberFormat.getIntegerInstance(Locale.ENGLISH);
	
	public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000);
	}
	
	public static String getRelativePath(File base, File file)
	{
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}
	
	public static double calculateAngleFrom(L2Object obj1, L2Object obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public final static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return angleTarget;
	}
	
	public static final double convertHeadingToDegree(int clientHeading)
	{
		double degree = clientHeading / 182.044444444;
		return degree;
	}
	
	public static final int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
		{
			degree = 360 + degree;
		}
		return (int) (degree * 182.044444444);
	}
	
	public static final int calculateHeadingFrom(L2Object obj1, L2Object obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static final int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	public static final int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	public static double calculateDistance(int x1, int y1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;
		
		if (includeZAxis)
		{
			double dz = z1 - z2;
			return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return 1000000;
		}
		
		return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
	}
	
	public static String capitalizeFirst(String str)
	{
		if ((str == null) || str.isEmpty())
		{
			return str;
		}
		final char[] arr = str.toCharArray();
		final char c = arr[0];
		
		if (Character.isLetter(c))
		{
			arr[0] = Character.toUpperCase(c);
		}
		return new String(arr);
	}
	
	@Deprecated
	public static String capitalizeWords(String str)
	{
		if ((str == null) || str.isEmpty())
		{
			return str;
		}
		
		char[] charArray = str.toCharArray();
		StringBuilder result = new StringBuilder();
		
		charArray[0] = Character.toUpperCase(charArray[0]);
		
		for (int i = 0; i < charArray.length; i++)
		{
			if (Character.isWhitespace(charArray[i]))
			{
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
			}
			
			result.append(charArray[i]);
		}
		
		return result.toString();
	}
	
	public static boolean checkIfInRange(int range, int x, int y, int z, L2Object obj2, boolean includeZAxis)
	{
		if (obj2 == null)
		{
			return false;
		}
		if (range == -1)
		{
			return true;
		}
		int rad = 0;
		if (obj2 instanceof L2Character)
		{
			rad += ((L2Character) obj2).getTemplate()._collisionRadius;
		}
		
		final double dx = x - obj2.getX();
		final double dy = y - obj2.getY();
		
		if (includeZAxis)
		{
			final double dz = z - obj2.getZ();
			final double d = (dx * dx) + (dy * dy) + (dz * dz);
			
			return d <= ((range * range) + (2 * range * rad) + (rad * rad));
		}
		final double d = (dx * dx) + (dy * dy);
		
		return d <= ((range * range) + (2 * range * rad) + (rad * rad));
	}
	
	public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (obj1.getInstanceId() != obj2.getInstanceId())
		{
			return false;
		}
		if (range == -1)
		{
			return true;
		}
		
		int rad = 0;
		if (obj1 instanceof L2Character)
		{
			rad += ((L2Character) obj1).getTemplate().getCollisionRadius();
		}
		if (obj2 instanceof L2Character)
		{
			rad += ((L2Character) obj2).getTemplate().getCollisionRadius();
		}
		
		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();
		double d = (dx * dx) + (dy * dy);
		
		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			d += (dz * dz);
		}
		return d <= ((range * range) + (2 * range * rad) + (rad * rad));
	}
	
	public static boolean checkIfInShortRadius(int radius, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (radius == -1)
		{
			return true; // not limited
		}
		
		int dx = obj1.getX() - obj2.getX();
		int dy = obj1.getY() - obj2.getY();
		
		if (includeZAxis)
		{
			int dz = obj1.getZ() - obj2.getZ();
			return ((dx * dx) + (dy * dy) + (dz * dz)) <= (radius * radius);
		}
		return ((dx * dx) + (dy * dy)) <= (radius * radius);
	}
	
	public static int countWords(String str)
	{
		return str.trim().split("\\s+").length;
	}
	
	public static String implodeString(Iterable<String> strArray, String strDelim)
	{
		final TextBuilder sbString = TextBuilder.newInstance();
		
		for (String strValue : strArray)
		{
			sbString.append(strValue);
			sbString.append(strDelim);
		}
		
		String result = sbString.toString();
		TextBuilder.recycle(sbString);
		return result;
	}
	
	public static <T> String implode(T[] array, String delim)
	{
		String result = "";
		for (T val : array)
		{
			result += val.toString() + delim;
		}
		if (!result.isEmpty())
		{
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	public static float roundTo(float number, int numPlaces)
	{
		if (numPlaces <= 1)
		{
			return Math.round(number);
		}
		
		float exponent = (float) Math.pow(10, numPlaces);
		return Math.round(number * exponent) / exponent;
	}
	
	public static boolean isDigit(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isAlphaNumeric(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isLetterOrDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	public static String formatAdena(long amount)
	{
		synchronized (ADENA_FORMATTER)
		{
			return ADENA_FORMATTER.format(amount);
		}
	}
	
	public static String formatDouble(double val, String format)
	{
		final DecimalFormat formatter = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
		return formatter.format(val);
	}
	
	public static String formatDate(Date date, String format)
	{
		if (date == null)
		{
			return null;
		}
		final DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static File[] getDatapackFiles(String dirname, String extention)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if (!dir.exists())
		{
			return null;
		}
		return dir.listFiles(new ExtFilter(extention));
	}
	
	public static String getDateString(Date date)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date.getTime());
	}
	
	public static String reverseColor(String color)
	{
		if (color.length() != 6)
		{
			return "000000";
		}
		char[] ch1 = color.toCharArray();
		char[] ch2 = new char[6];
		ch2[0] = ch1[4];
		ch2[1] = ch1[5];
		ch2[2] = ch1[2];
		ch2[3] = ch1[3];
		ch2[4] = ch1[0];
		ch2[5] = ch1[1];
		return new String(ch2);
	}
	
	public static int decodeColor(String color)
	{
		return Integer.decode("0x" + reverseColor(color));
	}
	
	public static void sendHtml(L2PcInstance activeChar, String html)
	{
		NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
		npcHtml.setHtml(html);
		activeChar.sendPacket(npcHtml);
	}
	
	public static void sendCBHtml(L2PcInstance activeChar, String html)
	{
		sendCBHtml(activeChar, html, "");
	}
	
	public static void sendCBHtml(L2PcInstance activeChar, String html, String fillMultiEdit)
	{
		if (activeChar == null)
		{
			return;
		}
		
		if (html != null)
		{
			activeChar.clearBypass();
			int len = html.length();
			for (int i = 0; i < len; i++)
			{
				int start = html.indexOf("\"bypass ", i);
				int finish = html.indexOf("\"", start + 1);
				if ((start < 0) || (finish < 0))
				{
					break;
				}
				
				if (html.substring(start + 8, start + 10).equals("-h"))
				{
					start += 11;
				}
				else
				{
					start += 8;
				}
				
				i = finish;
				int finish2 = html.indexOf("$", start);
				if ((finish2 < finish) && (finish2 > 0))
				{
					activeChar.addBypass2(html.substring(start, finish2).trim());
				}
				else
				{
					activeChar.addBypass(html.substring(start, finish).trim());
				}
			}
		}
		
		if (fillMultiEdit != null)
		{
			activeChar.sendPacket(new ShowBoard(html, "1001", activeChar));
			fillMultiEditContent(activeChar, fillMultiEdit);
		}
		else
		{
			activeChar.sendPacket(new ShowBoard(null, "101", activeChar));
			activeChar.sendPacket(new ShowBoard(html, "101", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	public static void fillMultiEditContent(L2PcInstance activeChar, String text)
	{
		text = text.replaceAll("<br>", Config.EOL);
		List<String> arg = new FastList<>();
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add(activeChar.getName());
		arg.add(Integer.toString(activeChar.getObjectId()));
		arg.add(activeChar.getAccountName());
		arg.add("9");
		arg.add(" ");
		arg.add(" ");
		arg.add(text);
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		activeChar.sendPacket(new ShowBoard(arg));
	}
	
	public static int getPlayersCountInRadius(int range, L2Object npc, boolean playable, boolean invisible)
	{
		int count = 0;
		final Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		for (L2Object obj : objs)
		{
			if ((obj != null) && (playable && (obj.isPlayable() || obj.isPet())))
			{
				if (!invisible && obj.isInvisible())
				{
					continue;
				}
				
				final L2Character cha = (L2Character) obj;
				if (((cha.getZ() < (npc.getZ() - 100)) && (cha.getZ() > (npc.getZ() + 100))) || !(GeoClient.getInstance().canSeeTarget(cha, npc.getX(), npc.getY(), npc.getZ(), false)))
				{
					continue;
				}
				
				if (Util.checkIfInRange(range, npc, obj, true) && !cha.isDead())
				{
					count++;
				}
			}
		}
		return count;
	}
	
	public static String getTimeFromMilliseconds(long millisec)
	{
		long seconds = millisec / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		
		seconds -= minutes * 60;
		minutes -= hours * 60;
		hours -= days * 24;
		
		String result = "";
		if (days > 0)
		{
			result += days;
		}
		if (hours > 0)
		{
			result += hours;
		}
		if (minutes > 0)
		{
			result += minutes;
		}
		if ((seconds > 0) && (hours < 1))
		{
			result += seconds;
		}
		return result;
	}
	
	public static String formatTime(int time)
	{
		if (time == 0)
		{
			return "now";
		}
		time = Math.abs(time);
		String ret = "";
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		if (numDays > 0)
		{
			ret += numDays + "d ";
		}
		if (numHours > 0)
		{
			ret += numHours + "h ";
		}
		if (numMins > 0)
		{
			ret += numMins + "m ";
		}
		if (numSeconds > 0)
		{
			ret += numSeconds + "s";
		}
		return ret.trim();
	}
	
	public static int[] getRange(int start, int end)
	{
		if (start > end)
		{
			return null;
		}
		
		int[] range = new int[(end - start) + 1];
		for (int l = 0; start < (end + 1); l++)
		{
			range[l] = start;
			start++;
		}
		return range;
	}
	
	public static long[] getRange(long start, long end)
	{
		if (start > end)
		{
			return null;
		}
		
		long[] range = new long[(int) ((end - start) + 1)];
		for (int l = 0; start < (end + 1); l++)
		{
			range[l] = start;
			start++;
		}
		return range;
	}
	
	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		int next;
		for (int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = next - (a * mval);
		}
		return result;
	}
	
	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		long next;
		for (int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = (int) (next - (a * mval));
		}
		return result;
	}
	
	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if (a.length > m)
		{
			throw new Exception("Overflow");
		}
		
		int result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		
		for (int i = 0; i < m; i++)
		{
			result <<= bits;
			
			if (a.length > i)
			{
				next = a[i];
				
				if ((next >= mval) || (next < 0))
				{
					throw new Exception("Overload, value is out of range");
				}
			}
			else
			{
				next = 0;
			}
			result += next;
		}
		return result;
	}
	
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch (PatternSyntaxException e)
		{
			e.printStackTrace();
		}
		if (pattern == null)
		{
			return false;
		}
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}
	
	public static String replaceRegexp(String source, String template, String replacement)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch (PatternSyntaxException e)
		{
			e.printStackTrace();
		}
		if (pattern != null)
		{
			Matcher regexp = pattern.matcher(source);
			source = regexp.replaceAll(replacement);
		}
		return source;
	}
	
	public static double calcDistance(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return calcDistance(x1 - x2, y1 - y2, z1 - z2);
	}
	
	public static double calcDistance(int dx, int dy, int dz)
	{
		double dist = Math.sqrt((dx * dx) + (dy * dy));
		dist = Math.sqrt((dist * dist) + (dz * dz));
		return dist;
	}
	
	public static double calcDistance(int dx, int dy)
	{
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	public static boolean contains(byte[] array, byte obj)
	{
		Arrays.sort(array);
		int index = Arrays.binarySearch(array, obj);
		if (index >= 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}