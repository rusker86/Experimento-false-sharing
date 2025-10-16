public class CounterWithFalseSharing {
// ambos contadores comparten la misma linea de caché
	public volatile long counter1;
	public volatile long counter2;
}
