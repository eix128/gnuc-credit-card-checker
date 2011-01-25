
package com.gnuc.java.ccc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryScanner
{
	private final Semaphore				sem				= new Semaphore(1);
	private final AtomicInteger			waitingCount	= new AtomicInteger(0);
	private final List<ThreadWithList>	threads			= new ArrayList<ThreadWithList>();
	private final int					installmentSize;
	private final AtomicInteger			producedCount	= new AtomicInteger(0);
	private static class DirInfo
	{
		private final File		dir;
		private final File[]	listing;
		private int				index;
		
		public DirInfo(File dir)
		{
			super();
			this.dir = dir;
			this.listing = dir.listFiles();
			this.index = 0;
		}
		
		public int getIndex()
		{
			return index;
		}
		
		public void setIndex(int index)
		{
			this.index = index;
		}
		
		@SuppressWarnings("unused")
		public File getDir()
		{
			return dir;
		}
		
		public File[] getListing()
		{
			return listing;
		}
	}
	private final BlockingQueue<DirInfo>	workingQueue;
	private final BlockingQueue<File>		producedQueue;
	private final class ThreadWithList extends Thread
	{
		private int	useCount	= 0;
		
		public ThreadWithList()
		{
			super();
		}
		
		@SuppressWarnings("unused")
		public void incrementUseCount()
		{
			useCount++;
		}
		
		@SuppressWarnings("unused")
		public int getUseCount()
		{
			return useCount;
		}
		
		public void run()
		{
			while (true)
			{
				if (DirectoryScanner.this.scan0())
				{
					break;
				}
			}
		}
	}
	
	public DirectoryScanner(BlockingQueue<File> producedQueue, int threads, int installmentSize)
	{
		super();
		this.producedQueue = producedQueue;
		this.installmentSize = installmentSize;
		workingQueue = new LinkedBlockingQueue<DirInfo>();
		for (int i = 0; i < threads; i++)
		{
			ThreadWithList t = new ThreadWithList();
			this.threads.add(t);
		}
	}
	
	public int scan(final File dir) throws InterruptedException, ExecutionException
	{
		sem.acquire();
		workingQueue.add(new DirInfo(dir));
		for (ThreadWithList t : threads)
		{
			t.start();
		}
		sem.acquire();
		return producedCount.get();
	}
	
	private boolean scan0()
	{
		waitingCount.incrementAndGet();
		// Remove the next item from the queue.
		ThreadWithList thread = ((ThreadWithList) Thread.currentThread());
		DirInfo dirInfo = null;
		try
		{
			if (waitingCount.get() == threads.size() && workingQueue.isEmpty())
			{
				sem.release();
				return true;
			}
			dirInfo = workingQueue.take();
		}
		catch (InterruptedException exc)
		{
			Thread.currentThread().interrupt();
			return true;
		}
		finally
		{
			waitingCount.decrementAndGet();
		}
		try
		{
			int index = dirInfo.getIndex();
			File[] listing = dirInfo.getListing();
			int upperBound = Math.min(index + installmentSize, listing.length);
			for (int i = index; i < upperBound; i++)
			{
				if (listing[i].isFile())
				{
					try
					{
						producedQueue.put(listing[i]);
						producedCount.incrementAndGet();
					}
					catch (InterruptedException e)
					{
						Thread.currentThread().interrupt();
						return true;
					}
				}
				if (listing[i].isDirectory())
				{
					DirInfo subdirInfo = new DirInfo(listing[i]);
					try
					{
						workingQueue.put(subdirInfo);
					}
					catch (InterruptedException exc)
					{
						Thread.currentThread().interrupt();
						return true;
					}
				}
			}
			if (upperBound != listing.length)
			{
				dirInfo.setIndex(upperBound);
				workingQueue.add(dirInfo);
			}
			thread.useCount += (upperBound - index);
			return false;
		}
		catch (Exception exp)
		{
			return false;
		}
	}
	
	public void close()
	{
		for (ThreadWithList t : threads)
		{
			if (t.isAlive())
			{
				t.interrupt();
			}
		}
		for (ThreadWithList t : threads)
		{
			try
			{
				t.join();
			}
			catch (InterruptedException exc)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}
		workingQueue.clear();
	}
}
