package util;
import java.util.Random;

public class SampleGenerator {

	private final static long SEED = System.currentTimeMillis();
	private final static Random RANDOM = new Random(SEED);
	
	/**
	 * Gera um nœmero da distribuicao exponencial
	 * 
	 * @param taxa
	 * @return
	 * @throws TaxaZeroException
	 */
	public static double exponential(double ratio) {
		if (ratio == 0) {
			return 0;
		}
		
		// usado para "simular" o caso da taxa = infinito
		if (ratio < 0) {
			return 0;
		}
		
		final double u = RANDOM.nextDouble();
//		System.out.print(-Math.log(u) / ratio);
		return -Math.log(u) / ratio;
		
	}
	
	public static double normal(double ratio, double dp) {
		double va;
		
		//IGNORA SE O VALOR FOR NEGATIVO
		do{
			va = dp*RANDOM.nextGaussian() + 1/ratio;
		}while(va<0);
		
		return va;
	}
	
	
}
