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

import java.io.IOException;
import java.io.InputStream;

public class CloseShieldedInputStream extends InputStream
{
	private InputStream _in = null;

	public CloseShieldedInputStream(InputStream in)
	{
		_in = in;
	}
	
	@Override
	public void close()
	{
		_in = null;
	}
	
	@Override
	public int read() throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.read();
	}
	
	@Override
	public int read(byte b[]) throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.read(b);
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.read(b, off, len);
	}
	
	@Override
	public long skip(long n) throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.skip(n);
	}
	
	@Override
	public synchronized void mark(int readlimit)
	{
		if (_in != null)
		{
			_in.mark(readlimit);
		}
	}
	
	@Override
	public boolean markSupported()
	{
		if (_in == null)
		{
			return false;
		}
		return _in.markSupported();
	}
	
	@Override
	public synchronized void reset() throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		_in.reset();
	}
	
	public InputStream getUnderlyingStream()
	{
		return _in;
	}
}