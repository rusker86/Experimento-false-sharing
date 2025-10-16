public class Main {
	static final long ITER = 100_000_000L;

	static class CounterWithFalseSharing {
		public volatile long c1 = 0L;
		public volatile long c2 = 0L;
	}

	static class CounterWithoutFalseSharing {
		public volatile long c1 = 0L;
		public long p1, p2, p3, p4, p5, p6, p7;
		public volatile long c2 = 0L;
	}

	public static void main(String[] args) throws Exception {
		boolean fs = args.length == 0;
		Object obj = fs ? new CounterWithFalseSharing() : new CounterWithoutFalseSharing();
		Thread t1 = new Thread(() -> {
			for (long i = 0; i < ITER; i++)
				if (fs) ((CounterWithFalseSharing)obj).c1++; else ((CounterWithoutFalseSharing)obj).c1++;
		});
		
		Thread t2 = new Thread(() -> {
			for (long i = 0; i < ITER; i++)
				if (fs) ((CounterWithFalseSharing)obj).c2++; else ((CounterWithoutFalseSharing)obj).c2++;
		});

		long start = System.nanoTime();
		t1.start(); t2.start();
		t1.join(); t2.join();
		long end = System.nanoTime();

		System.out.println("GLOBAL\tTOTAL\t" + ((end - start) / 1_000_000));
	}
}