package circuit.util;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool extends ThreadGroup {
	public static IDAssigner poolID = new IDAssigner(1);
	public boolean alive = false;
	public List<Runnable> taskQueue;
	public int id;

	public ThreadPool(int numThreads) {
		super("ThreadPool-" + poolID.nextID());
		this.id = poolID.getCurrentID();
		setDaemon(true);
		taskQueue = new LinkedList<>();
		alive = true;

		for (int i = 0; i < numThreads; i++) {
			new PooledThread(this).start();
		}
	}

	public synchronized void runTask(Runnable task) {
		if (!alive) throw new IllegalStateException("ThreadPool-" + id + " is dead!");
		if (task != null) {
			taskQueue.add(task);
			notify();
		}
	}

	public synchronized void close() {
		if (!alive) return;
		alive = false;
		taskQueue.clear();
		interrupt();
	}

	public void join() {
		synchronized (this) {
			alive = false;
			notifyAll();
		}

		Thread[] threads = new Thread[activeCount()];
		int count = enumerate(threads);

		for (int i = 0; i < count; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	public synchronized Runnable getTask() throws InterruptedException {
		while (taskQueue.size() == 0) {
			if (!alive) return null;
			wait();
		}
		return taskQueue.remove(0);
	}
}