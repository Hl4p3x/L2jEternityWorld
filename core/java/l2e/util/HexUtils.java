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
package l2e.util;

import java.util.Arrays;

public class HexUtils
{
	private static final char[] _NIBBLE_CHAR_LOOKUP =
	{
		'0',
		'1',
		'2',
		'3',
		'4',
		'5',
		'6',
		'7',
		'8',
		'9',
		'A',
		'B',
		'C',
		'D',
		'E',
		'F'
	};
	private static final char[] _NEW_LINE_CHARS = System.getProperty("line.separator").toCharArray();
	
	public static char[] b2HexChars(final byte data)
	{
		return b2HexChars(data, null, 0);
	}
	
	public static char[] b2HexChars(final byte data, char[] dstHexChars, int dstOffset)
	{
		if (dstHexChars == null)
		{
			dstHexChars = new char[2];
			dstOffset = 0;
		}
		dstHexChars[dstOffset] = _NIBBLE_CHAR_LOOKUP[(data & 0xF0) >> 4];
		dstHexChars[dstOffset + 1] = _NIBBLE_CHAR_LOOKUP[data & 0x0F];
		
		return dstHexChars;
	}
	
	public static char[] int2HexChars(final int data)
	{
		return int2HexChars(data, new char[8], 0);
	}

	public static char[] int2HexChars(final int data, char[] dstHexChars, int dstOffset)
	{
		if (dstHexChars == null)
		{
			dstHexChars = new char[8];
			dstOffset = 0;
		}
		
		b2HexChars((byte) ((data & 0xFF000000) >> 24), dstHexChars, dstOffset);
		b2HexChars((byte) ((data & 0x00FF0000) >> 16), dstHexChars, dstOffset + 2);
		b2HexChars((byte) ((data & 0x0000FF00) >> 8), dstHexChars, dstOffset + 4);
		b2HexChars((byte) (data & 0x000000FF), dstHexChars, dstOffset + 6);
		return dstHexChars;
	}
	
	public static char[] bArr2HexChars(final byte[] data, final int offset, final int len)
	{
		return bArr2HexChars(data, offset, len, null, 0);
	}
	
	public static char[] bArr2HexChars(final byte[] data, final int offset, final int len, char[] dstHexChars, int dstOffset)
	{
		if (dstHexChars == null)
		{
			dstHexChars = new char[len * 2];
			dstOffset = 0;
		}
		
		for (int dataIdx = offset, charsIdx = dstOffset; dataIdx < (len + offset); ++dataIdx, ++charsIdx)
		{
			dstHexChars[charsIdx] = _NIBBLE_CHAR_LOOKUP[(data[dataIdx] & 0xF0) >> 4];
			dstHexChars[++charsIdx] = _NIBBLE_CHAR_LOOKUP[data[dataIdx] & 0x0F];
		}
		return dstHexChars;
	}
	
	public static char[] bArr2AsciiChars(byte[] data, final int offset, final int len)
	{
		return bArr2AsciiChars(data, offset, len, new char[len], 0);
	}
	
	public static char[] bArr2AsciiChars(byte[] data, final int offset, final int len, char[] dstAsciiChars, int dstOffset)
	{
		if (dstAsciiChars == null)
		{
			dstAsciiChars = new char[len];
			dstOffset = 0;
		}
		
		for (int dataIdx = offset, charsIdx = dstOffset; dataIdx < (len + offset); ++dataIdx, ++charsIdx)
		{
			if ((data[dataIdx] > 0x1f) && (data[dataIdx] < 0x80))
			{
				dstAsciiChars[charsIdx] = (char) data[dataIdx];
			}
			else
			{
				dstAsciiChars[charsIdx] = '.';
			}
		}
		return dstAsciiChars;
	}
	
	private static final int _HEX_ED_BPL = 16;
	private static final int _HEX_ED_CPB = 2;
	
	public static char[] bArr2HexEdChars(byte[] data, int len)
	{
		final int lineLength = 9 + (_HEX_ED_BPL * _HEX_ED_CPB) + 1 + _HEX_ED_BPL + _NEW_LINE_CHARS.length;
		final int lenBplMod = len % _HEX_ED_BPL;
		int numLines;
		char[] textData;

		if (lenBplMod == 0)
		{
			numLines = len / _HEX_ED_BPL;
			textData = new char[(lineLength * numLines) - _NEW_LINE_CHARS.length];
		}
		else
		{
			numLines = (len / _HEX_ED_BPL) + 1;
			textData = new char[(lineLength * numLines) - (_HEX_ED_BPL - (lenBplMod)) - _NEW_LINE_CHARS.length];
		}

		int dataOffset;
		int dataLen;
		int lineStart;
		int lineHexDataStart;
		int lineAsciiDataStart;
		for (int i = 0; i < numLines; ++i)
		{
			dataOffset = i * _HEX_ED_BPL;
			dataLen = Math.min(len - dataOffset, _HEX_ED_BPL);
			lineStart = i * lineLength;
			lineHexDataStart = lineStart + 9;
			lineAsciiDataStart = lineHexDataStart + (_HEX_ED_BPL * _HEX_ED_CPB) + 1;
			
			int2HexChars(dataOffset, textData, lineStart);
			textData[lineHexDataStart - 1] = ' ';
			bArr2HexChars(data, dataOffset, dataLen, textData, lineHexDataStart);
			bArr2AsciiChars(data, dataOffset, dataLen, textData, lineAsciiDataStart);
			
			if (i < (numLines - 1))
			{
				textData[lineAsciiDataStart - 1] = ' ';
				System.arraycopy(_NEW_LINE_CHARS, 0, textData, lineAsciiDataStart + _HEX_ED_BPL, _NEW_LINE_CHARS.length);
			}
			else if (dataLen < _HEX_ED_BPL)
			{
				int lineHexDataEnd = lineHexDataStart + (dataLen * _HEX_ED_CPB);
				Arrays.fill(textData, lineHexDataEnd, lineHexDataEnd + ((_HEX_ED_BPL - dataLen) * _HEX_ED_CPB) + 1, ' ');
			}
			else
			{
				textData[lineAsciiDataStart - 1] = ' ';
			}
		}
		return textData;
	}
}