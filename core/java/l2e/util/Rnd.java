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
package l2e.util;

import java.security.SecureRandom;
import java.util.Random;

public final class Rnd
{
	protected static final L2Random RND = new L2Random();
	
	protected static final class L2Random extends Random
	{
		private static final long serialVersionUID = 2089256427272977088L;
		
		private static final long multiplier = 0x5DEECE66DL;
		private static final long addend = 0xBL;
		private static final long mask = (1L << 48) - 1;
		private long seed;
		
		@Override
		public synchronized void setSeed(long newSeed)
		{
			seed = (newSeed ^ multiplier) & mask;
			super.setSeed(newSeed);
		}
		
		@Override
		protected int next(int bits)
		{
			long nextseed = seed = ((seed * multiplier) + addend) & mask;
			return (int) (nextseed >>> (48 - bits));
		}
		
		@Override
		public double nextDouble()
		{
			return (double) next(31) / 0x7fffffff;
		}
		
		public double nextDouble(double n)
		{
			return ((double) (next(31) - 1) / 0x7fffffff) * n;
		}
		
		@Override
		public int nextInt(int n)
		{
			return (int) (((double) (next(31) - 1) / 0x7fffffff) * n);
		}
	}
	
	public static final class NonAtomicRandom extends Random
	{
		private static final long serialVersionUID = 1L;
		private volatile long _seed;
		
		public NonAtomicRandom()
		{
			this(++SEED_UNIQUIFIER + System.nanoTime());
		}
		
		public NonAtomicRandom(final long seed)
		{
			setSeed(seed);
		}
		
		@Override
		public final int next(final int bits)
		{
			return (int) ((_seed = ((_seed * MULTIPLIER) + ADDEND) & MASK) >>> (48 - bits));
		}
		
		@Override
		public final void setSeed(final long seed)
		{
			_seed = (seed ^ MULTIPLIER) & MASK;
		}
	}
	
	public static final class RandomContainer
	{
		private final Random _random;
		
		protected RandomContainer(final Random random)
		{
			_random = random;
		}
		
		public final Random directRandom()
		{
			return _random;
		}
		
		public final double get()
		{
			return _random.nextDouble();
		}
		
		public final int get(final int n)
		{
			return (int) (_random.nextDouble() * n);
		}
		
		public final int get(final int min, final int max)
		{
			return min + (int) (_random.nextDouble() * ((max - min) + 1));
		}
		
		public final long get(final long min, final long max)
		{
			return min + (long) (_random.nextDouble() * ((max - min) + 1));
		}
		
		public final boolean nextBoolean()
		{
			return _random.nextBoolean();
		}
		
		public final void nextBytes(final byte[] array)
		{
			_random.nextBytes(array);
		}
		
		public final double nextDouble()
		{
			return _random.nextDouble();
		}
		
		public final float nextFloat()
		{
			return _random.nextFloat();
		}
		
		public final double nextGaussian()
		{
			return _random.nextGaussian();
		}
		
		public final int nextInt()
		{
			return _random.nextInt();
		}
		
		public final long nextLong()
		{
			return _random.nextLong();
		}
	}
	
	public static enum RandomType
	{
		SECURE,
		
		UNSECURE_ATOMIC,
		
		UNSECURE_THREAD_LOCAL,
		
		UNSECURE_VOLATILE
	}
	
	public static final class ThreadLocalRandom extends Random
	{
		private static final class Seed
		{
			long _seed;
			
			Seed(final long seed)
			{
				setSeed(seed);
			}
			
			final int next(final int bits)
			{
				return (int) ((_seed = ((_seed * MULTIPLIER) + ADDEND) & MASK) >>> (48 - bits));
			}
			
			final void setSeed(final long seed)
			{
				_seed = (seed ^ MULTIPLIER) & MASK;
			}
		}
		
		private static final long serialVersionUID = 1L;
		private final ThreadLocal<Seed> _seedLocal;
		
		public ThreadLocalRandom()
		{
			_seedLocal = new ThreadLocal<Seed>()
			{
				@Override
				public final Seed initialValue()
				{
					return new Seed(++SEED_UNIQUIFIER + System.nanoTime());
				}
			};
		}
		
		public ThreadLocalRandom(final long seed)
		{
			_seedLocal = new ThreadLocal<Seed>()
			{
				@Override
				public final Seed initialValue()
				{
					return new Seed(seed);
				}
			};
		}
		
		@Override
		public final int next(final int bits)
		{
			return _seedLocal.get().next(bits);
		}
		
		@Override
		public final void setSeed(final long seed)
		{
			if (_seedLocal != null)
			{
				_seedLocal.get().setSeed(seed);
			}
		}
	}
	
	private static final long ADDEND = 0xBL;
	
	private static final long MASK = (1L << 48) - 1;
	
	private static final long MULTIPLIER = 0x5DEECE66DL;
	
	private static final RandomContainer rnd = newInstance(RandomType.UNSECURE_THREAD_LOCAL);
	
	protected static volatile long SEED_UNIQUIFIER = 8682522807148012L;
	
	public static final Random directRandom()
	{
		return rnd.directRandom();
	}
	
	public static final double get()
	{
		return rnd.nextDouble();
	}
	
	public static final int get(final int n)
	{
		return rnd.get(n);
	}
	
	public static final int get(final int min, final int max)
	{
		return rnd.get(min, max);
	}
	
	public static final long get(final long min, final long max)
	{
		return rnd.get(min, max);
	}
	
	public static final RandomContainer newInstance(final RandomType type)
	{
		switch (type)
		{
			case UNSECURE_ATOMIC:
				return new RandomContainer(new Random());
				
			case UNSECURE_VOLATILE:
				return new RandomContainer(new NonAtomicRandom());
				
			case UNSECURE_THREAD_LOCAL:
				return new RandomContainer(new ThreadLocalRandom());
				
			case SECURE:
				return new RandomContainer(new SecureRandom());
		}
		
		throw new IllegalArgumentException();
	}
	
	public static final boolean nextBoolean()
	{
		return rnd.nextBoolean();
	}
	
	public static final void nextBytes(final byte[] array)
	{
		rnd.nextBytes(array);
	}
	
	public static final double nextDouble()
	{
		return rnd.nextDouble();
	}
	
	public static final float nextFloat()
	{
		return rnd.nextFloat();
	}
	
	public static final double nextGaussian()
	{
		return rnd.nextGaussian();
	}
	
	public static final int nextInt()
	{
		return rnd.nextInt();
	}
	
	public static final int nextInt(final int n)
	{
		return get(n);
	}
	
	public static final long nextLong()
	{
		return rnd.nextLong();
	}
	
	public static final boolean getChance(int n)
	{
		return n > (RND.nextInt() * 100);
	}
	
	public static boolean calcChance(int chance, int maxChance)
	{
		return chance > RND.nextInt(maxChance);
	}
	
	public static boolean calcChance(double chance, int maxChance)
	{
		return chance > RND.nextDouble(maxChance);
	}
	
	public static boolean calcChance(double chance)
	{
		return chance > RND.nextDouble();
	}
	
	public static boolean chance(int chance)
	{
		return (chance >= 1) && ((chance > 99) || ((RND.nextInt(99) + 1) <= chance));
	}
	
	public static boolean chance(double chance)
	{
		return RND.nextDouble() <= (chance / 100.);
	}
}