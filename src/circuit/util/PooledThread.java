package circuit.util;

public class PooledThread extends Thread {
	public static IDAssigner threadID = new IDAssigner();
	public ThreadPool pool;

	public PooledThread(ThreadPool pool) {
		super(pool, "PooledThread-" + threadID.nextID());
		this.pool = pool;
	}

	public void run() {
		while (!isInterrupted()) {
			Runnable task = null;
			try {
				task = pool.getTask();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			if (task == null) return;
			try {
				task.run();
			} catch (Throwable t) {
				pool.uncaughtException(this, t);
			}
		}
	}
}