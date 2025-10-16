public class App {
	public static void main(String[] args) {
		CounterWithFalseSharing falseSharing = new CounterWithFalseSharing();
		CounterWithoutFalseSharing withoutFalseSharing = new CounterWithoutFalseSharing();


		final long ITERATIONS = 1_000_000_000;

		// Hilo 1. Accede a contador 1 con false sharing
		Thread th1 = new Thread(() -> {
			long startTime = System.nanoTime();
			for (int i = 0; i < ITERATIONS; i++) falseSharing.counter1++;
			long endTime= System.nanoTime();

			System.out.println("Time thread 1 (Counter 1 with false sharing): " + (endTime - startTime) / 1_000_000+ " ms");
		});

		// Hilo 2. Accede a contador 2 con false sharing
		Thread th2 = new Thread(() -> {
			long startTime = System.nanoTime();
			for (int i = 0; i < ITERATIONS; i++) falseSharing.counter2++;
			long endTime= System.nanoTime();

			System.out.println("Time thread 2 (Counter 2 with false sharing): " + (endTime - startTime) / 1_000_000+ " ms");
		});

		// Hilo 3. Accede a contador 2 con false sharing (Ayudará a ver mejor el efecto cuando un sistema escala los hilos)
		Thread th3 = new Thread(() -> {
			long startTime = System.nanoTime();
			for (int i = 0; i < ITERATIONS; i++) falseSharing.counter2++;
			long endTime= System.nanoTime();

			System.out.println("Time thread 3 (Counter 2 with false sharing): " + (endTime - startTime) / 1_000_000+ " ms");
		});

		// Hilo 4. Accede a contador 1 sin false sharing
		Thread th4 = new Thread(() -> {
			long startTime = System.nanoTime();
			for (int i = 0; i < ITERATIONS; i++) withoutFalseSharing.counter1++;
			long endTime= System.nanoTime();

			System.out.println("Time thread 4 (Counter 1 without false sharing): " + (endTime - startTime) / 1_000_000+ " ms");
		});

		// Hilo 5. Accede a contador 2 sin false sharing
		Thread th5 = new Thread(() -> {
			long startTime = System.nanoTime();
			for (int i = 0; i < ITERATIONS; i++) withoutFalseSharing.counter2++;
			long endTime= System.nanoTime();

			System.out.println("Time thread 5 (Counter 2 without false sharing): " + (endTime - startTime) / 1_000_000+ " ms");
		});

		// Hilo 6. Accede a contador 2 sin false sharing (Ayudará a ver otro efecto conocido como true sharing. Ver README para más información)
		Thread th6 = new Thread(() -> {
			long startTime = System.nanoTime();
			for (int i = 0; i < ITERATIONS; i++) withoutFalseSharing.counter2++;
			long endTime= System.nanoTime();

			System.out.println("Time thread 6 (Counter 2 without false sharing): " + (endTime - startTime) / 1_000_000+ " ms");
		});


		// Hilos que manejan la clase con false sharing
		th1.start();
		th2.start();
		//Descomentar este hilo para ver los efectos del false sharing cuando el número de hilos escala
//		th3.start();

		try {
			th1.join();
			th2.join();

//		Descomentar si se va a usar el hilo 3
//		th3.join();		
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}


		// Hilos que manejan la clase sin false sharing
		th4.start();
		th5.start();
		// Descomentar este hilo para ver los efectos del true sharing
//		th6.start();

		try {
			th4.join();
			th5.join();

//		Descomentar si se va a usar el hilo 6
//		th6.join();		
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
	}
}
