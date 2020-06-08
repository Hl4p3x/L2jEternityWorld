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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Base64
{
	private static final Logger _log = Logger.getLogger(Base64.class.getName());
	
	public static final int NO_OPTIONS = 0;
	
	public static final int ENCODE = 1;
	
	public static final int DECODE = 0;
	
	public static final int GZIP = 2;
	
	public static final int DONT_BREAK_LINES = 8;
	
	private static final int MAX_LINE_LENGTH = 76;
	
	private static final byte EQUALS_SIGN = (byte) '=';
	
	private static final byte NEW_LINE = (byte) '\n';
	
	private static final Charset PREFERRED_ENCODING = StandardCharsets.UTF_8;

	private static final byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(PREFERRED_ENCODING);
	
	static final byte[] DECODABET =
	{ 
		-9, -9, -9, -9, -9, -9, -9, -9, -9,
		-5, -5,
		-9, -9,
		-5,
		-9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
		-9, -9, -9, -9, -9,
		-5,
		-9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
		62,
		-9, -9, -9,
		63,
		52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
		-9, -9, -9,
		-1,
		-9, -9, -9,
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
		14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
		-9, -9, -9, -9, -9, -9,
		26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
		39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
		-9, -9, -9, -9
	};
	
	private static final byte WHITE_SPACE_ENC = -5;
	private static final byte EQUALS_SIGN_ENC = -1;
	
	private Base64()
	{
	}
	
	static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes)
	{
		encode3to4(threeBytes, 0, numSigBytes, b4, 0);
		return b4;
	}
	
	static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset)
	{

		int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
		| (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
		| (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);
		
		switch (numSigBytes)
		{
			case 3:
				destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
				destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
				destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
				return destination;
				
			case 2:
				destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
				destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
				destination[destOffset + 3] = EQUALS_SIGN;
				return destination;
				
			case 1:
				destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
				destination[destOffset + 2] = EQUALS_SIGN;
				destination[destOffset + 3] = EQUALS_SIGN;
				return destination;
				
			default:
				return destination;
		}
	}
	
	public static String encodeObject(Serializable serializableObject)
	{
		return encodeObject(serializableObject, NO_OPTIONS);
	}
	
	public static String encodeObject(Serializable serializableObject, int options)
	{
		int gzip = (options & GZIP);
		int dontBreakLines = (options & DONT_BREAK_LINES);
		byte[] value = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Base64.OutputStream b64os = new Base64.OutputStream(baos, ENCODE | dontBreakLines);
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(b64os);
			FilterOutputStream os = (gzip == GZIP) ? gzipOutputStream : b64os;
			ObjectOutputStream oos = new ObjectOutputStream(os))
		{
			oos.writeObject(serializableObject);
			value = baos.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return value != null ? new String(value, PREFERRED_ENCODING) : null;
	}
	
	public static String encodeBytes(byte[] source)
	{
		return encodeBytes(source, 0, source.length, NO_OPTIONS);
	}
	
	public static String encodeBytes(byte[] source, int options)
	{
		return encodeBytes(source, 0, source.length, options);
	}
	
	public static String encodeBytes(byte[] source, int off, int len)
	{
		return encodeBytes(source, off, len, NO_OPTIONS);
	}
	
	public static String encodeBytes(byte[] source, int off, int len, int options)
	{
		int dontBreakLines = (options & DONT_BREAK_LINES);
		int gzip = (options & GZIP);
		
		if (gzip == GZIP)
		{
			byte[] value = null;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Base64.OutputStream b64os = new Base64.OutputStream(baos, ENCODE | dontBreakLines);
				GZIPOutputStream gzos = new GZIPOutputStream(b64os))
			{
				gzos.write(source, off, len);
				value = baos.toByteArray();
			}
			catch (IOException e)
			{
				_log.warning("Base64: " + e.getMessage());
				return null;
			}

			if (value != null)
			{
				return new String(value, PREFERRED_ENCODING);
			}
		}
		boolean breakLines = dontBreakLines == 0;
		
		int len43 = len * 4 / 3;
		byte[] outBuff = new byte[(len43)
		                          + ((len % 3) > 0 ? 4 : 0)
		                          + (breakLines ? (len43 / MAX_LINE_LENGTH) : 0)];
		int d = 0;
		int e = 0;
		int len2 = len - 2;
		int lineLength = 0;
		for (; d < len2; d += 3, e += 4)
		{
			encode3to4(source, d + off, 3, outBuff, e);
			
			lineLength += 4;
			if (breakLines && lineLength == MAX_LINE_LENGTH)
			{
				outBuff[e + 4] = NEW_LINE;
				e++;
				lineLength = 0;
			}
		}
		
		if (d < len)
		{
			encode3to4(source, d + off, len - d, outBuff, e);
			e += 4;
		}
		return new String(outBuff, 0, e, PREFERRED_ENCODING);
	}
	
	static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset)
	{
		if (source[srcOffset + 2] == EQUALS_SIGN)
		{
			int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
			| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12);
			
			destination[destOffset] = (byte) (outBuff >>> 16);
			return 1;
		}
		
		else if (source[srcOffset + 3] == EQUALS_SIGN)
		{
			int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
			| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
			| ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6);
			
			destination[destOffset] = (byte) (outBuff >>> 16);
			destination[destOffset + 1] = (byte) (outBuff >>> 8);
			return 2;
		}
		
		else
		{
			try
			{
				int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
				| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
				| ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6)
				| ((DECODABET[source[srcOffset + 3]] & 0xFF));
				
				destination[destOffset] = (byte) (outBuff >> 16);
				destination[destOffset + 1] = (byte) (outBuff >> 8);
				destination[destOffset + 2] = (byte) (outBuff);
				
				return 3;
			}
			catch (Exception e)
			{
				System.out.println(StringUtil.concat(String.valueOf(source[srcOffset]), ": ", String.valueOf(DECODABET[source[srcOffset]])));
				System.out.println(StringUtil.concat(String.valueOf(source[srcOffset + 1]), ": ", String.valueOf(DECODABET[source[srcOffset + 1]])));
				System.out.println(StringUtil.concat(String.valueOf(source[srcOffset + 2]), ": ", String.valueOf(DECODABET[source[srcOffset + 2]])));
				System.out.println(StringUtil.concat(String.valueOf(source[srcOffset + 3]), ": ", String.valueOf(DECODABET[source[srcOffset + 3]])));
				return -1;
			}
		}
	}
	
	public static byte[] decode(byte[] source, int off, int len)
	{
		int len34 = len * 3 / 4;
		byte[] outBuff = new byte[len34];
		int outBuffPosn = 0;
		
		byte[] b4 = new byte[4];
		int b4Posn = 0;
		int i = 0;
		byte sbiCrop = 0;
		byte sbiDecode = 0;
		for (i = off; i < off + len; i++)
		{
			sbiCrop = (byte) (source[i] & 0x7f);
			sbiDecode = DECODABET[sbiCrop];
			
			if (sbiDecode >= WHITE_SPACE_ENC)
			{
				if (sbiDecode >= EQUALS_SIGN_ENC)
				{
					b4[b4Posn++] = sbiCrop;
					if (b4Posn > 3)
					{
						outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
						b4Posn = 0;

						if (sbiCrop == EQUALS_SIGN)
							break;
					}	
				}	
			}
			else
			{
				System.err.println(StringUtil.concat("Bad Base64 input character at ", String.valueOf(i), ": ", String.valueOf(source[i]), "(decimal)"));
				return null;
			}
		}
		byte[] out = new byte[outBuffPosn];
		System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
		return out;
	}
	
	public static byte[] decode(String s)
	{
		byte[] bytes = s.getBytes(PREFERRED_ENCODING);

		bytes = decode(bytes, 0, bytes.length);
		
		if ((bytes != null) && (bytes.length >= 2))
		{
			final int head = (bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
			if (bytes.length >= 4 && GZIPInputStream.GZIP_MAGIC == head)
			{
				byte[] buffer = new byte[2048];
				int length = 0;
				try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					GZIPInputStream gzis = new GZIPInputStream(bais);
					ByteArrayOutputStream baos = new ByteArrayOutputStream())
				{
					while ((length = gzis.read(buffer)) >= 0)
					{
						baos.write(buffer, 0, length);
					}
					bytes = baos.toByteArray();
					
				}
				catch (IOException e)
				{
				}
			}
		}
		return bytes;
	}
	
	public static Object decodeToObject(String encodedObject)
	{
		byte[] objBytes = decode(encodedObject);
		Object obj = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
			ObjectInputStream ois = new ObjectInputStream(bais))
		{
			obj = ois.readObject();
		}
		catch (IOException e)
		{
			_log.warning("Base64: " + e.getMessage());
		}
		catch (ClassNotFoundException e)
		{
			_log.warning("Base64: " + e.getMessage());
		}
		return obj;
	}
	
	public static class InputStream extends FilterInputStream
	{
		private boolean encode;
		private int position;
		private byte[] buffer;
		private int bufferLength;
		private int numSigBytes;
		private int lineLength;
		private boolean breakLines;
		
		public InputStream(java.io.InputStream pIn)
		{
			this(pIn, DECODE);
		}
		
		public InputStream(java.io.InputStream pIn, int options)
		{
			super(pIn);
			breakLines = (options & DONT_BREAK_LINES) != DONT_BREAK_LINES;
			encode = (options & ENCODE) == ENCODE;
			bufferLength = encode ? 4 : 3;
			buffer = new byte[bufferLength];
			position = -1;
			lineLength = 0;
		}
		
		@Override
		public int read() throws IOException
		{
			if (position < 0)
			{
				if (encode)
				{
					byte[] b3 = new byte[3];
					int numBinaryBytes = 0;
					for (int i = 0; i < 3; i++)
					{
						try
						{
							int b = in.read();

							if (b >= 0)
							{
								b3[i] = (byte) b;
								numBinaryBytes++;
							}	
						}
						catch (IOException e)
						{
							if (i == 0)
								throw e;
							
						}
					}
					
					if (numBinaryBytes > 0)
					{
						encode3to4(b3, 0, numBinaryBytes, buffer, 0);
						position = 0;
						numSigBytes = 4;
					}
					else
					{
						return -1;
					}
				}
				else
				{
					byte[] b4 = new byte[4];
					int i = 0;
					for (i = 0; i < 4; i++)
					{
						int b = 0;
						do
						{
							b = in.read();
						}
						while (b >= 0 && DECODABET[b & 0x7f] <= WHITE_SPACE_ENC);
						
						if (b < 0)
							break;
						
						b4[i] = (byte) b;
					}
					
					if (i == 4)
					{
						numSigBytes = decode4to3(b4, 0, buffer, 0);
						position = 0;
					}
					else if (i == 0)
					{
						return -1;
					}
					else
					{
						throw new IOException("Improperly padded Base64 input.");
					}	
				}
			}
			
			if (position >= 0)
			{
				if (position >= numSigBytes)
					return -1;
				
				if (encode && breakLines && lineLength >= MAX_LINE_LENGTH)
				{
					lineLength = 0;
					return NEW_LINE;
				}
				lineLength++;
				
				int b = buffer[position++];
				
				if (position >= bufferLength)
					position = -1;
				
				return b & 0xFF;
			}
			throw new IOException("Error in Base64 code reading stream.");
		}
		
		@Override
		public int read(byte[] dest, int off, int len) throws IOException
		{
			int i;
			int b;
			for (i = 0; i < len; i++)
			{
				b = read();

				if (b >= 0)
					dest[off + i] = (byte) b;
				else if (i == 0)
					return -1;
				else
					break;
			}
			return i;
		}	
	}
	
	public static class OutputStream extends FilterOutputStream
	{
		private boolean encode;
		private int position;
		private byte[] buffer;
		private int bufferLength;
		private int lineLength;
		private boolean breakLines;
		private byte[] b4;
		private boolean suspendEncoding;
		
		public OutputStream(java.io.OutputStream pOut)
		{
			this(pOut, ENCODE);
		}
		
		public OutputStream(java.io.OutputStream pOut, int options)
		{
			super(pOut);
			breakLines = (options & DONT_BREAK_LINES) != DONT_BREAK_LINES;
			encode = (options & ENCODE) == ENCODE;
			bufferLength = encode ? 3 : 4;
			buffer = new byte[bufferLength];
			position = 0;
			lineLength = 0;
			suspendEncoding = false;
			b4 = new byte[4];
		}
		
		@Override
		public void write(int theByte) throws IOException
		{
			if (suspendEncoding)
			{
				super.out.write(theByte);
				return;
			}

			if (encode)
			{
				buffer[position++] = (byte) theByte;
				if (position >= bufferLength)
				{
					out.write(encode3to4(b4, buffer, bufferLength));
					
					lineLength += 4;
					if (breakLines && lineLength >= MAX_LINE_LENGTH)
					{
						out.write(NEW_LINE);
						lineLength = 0;
					}
					
					position = 0;
				}
			}
			else
			{
				if (DECODABET[theByte & 0x7f] > WHITE_SPACE_ENC)
				{
					buffer[position++] = (byte) theByte;
					if (position >= bufferLength)
					{
						int len = Base64.decode4to3(buffer, 0, b4, 0);
						out.write(b4, 0, len);
						position = 0;
					}
				}
				else if (DECODABET[theByte & 0x7f] != WHITE_SPACE_ENC)
				{
					throw new IOException("Invalid character in Base64 data.");
				}
			}
		}
		
		@Override
		public void write(byte[] theBytes, int off, int len) throws IOException
		{
			if (suspendEncoding)
			{
				super.out.write(theBytes, off, len);
				return;
			}
			
			for (int i = 0; i < len; i++)
			{
				write(theBytes[off + i]);
			}	
		}
		
		public void flushBase64() throws IOException
		{
			if (position > 0)
			{
				if (encode)
				{
					out.write(encode3to4(b4, buffer, position));
					position = 0;
				}
				else
				{
					throw new IOException("Base64 input not properly padded.");
				}
			}	
		}
		
		@Override
		public void close() throws IOException
		{
			flushBase64();
			
			super.close();
			
			buffer = null;
			out = null;
		}
		
		public void suspendEncoding() throws IOException
		{
			flushBase64();
			suspendEncoding = true;
		}
		
		public void resumeEncoding()
		{
			suspendEncoding = false;
		}	
	}	
}