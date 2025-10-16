public class CounterWithoutFalseSharing {
	public volatile long counter1;	// Contador 1 en linea de caché 1
	private long p1, p2, p3, p4, p5, p6, p7;		// Padding de 56 Bytes en linea de caché 1
	public volatile long counter2;	// Contador 2 en linea de caché 2
}
