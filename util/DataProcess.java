package util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataProcess {

	public  int numberOfRounds;
	public  int roundSize;
	
	public List<Double> n1 = new ArrayList<Double>();
	public List<Double> nq1 = new ArrayList<Double>();
	public List<Double> n2 = new ArrayList<Double>();
	public List<Double> nq2 = new ArrayList<Double>();
	public List<Double> nqTotal = new ArrayList<Double>();
	
	public List<Integer> id = new ArrayList<Integer>();

	public List<Double> X1 = new ArrayList<Double>();
	public List<Double> X2 = new ArrayList<Double>();
	public List<Double> W1 = new ArrayList<Double>();
	public List<Double> W2 = new ArrayList<Double>();
	
	public List<Double> T1 = new ArrayList<Double>();
	public List<Double> T2 = new ArrayList<Double>();
	public List<Double> Ttotal = new ArrayList<Double>();
	
	public List<Double> time = new ArrayList<Double>();

	


	double n1avg = 0;
	double n2avg = 0;
	double nq1avg = 0;
	double nq2avg = 0;
	
	double X1avg = 0;
	double X2avg = 0;
	double W1avg = 0;
	double W2avg = 0;
	
	double W1var = 0;
	double W2var = 0;
	double X1var = 0;
	double X2var = 0;
	double W1varVar = 0;
	double W2varVar = 0;
	double X1varVar = 0;
	double X2varVar = 0;
	
	double T1var;
	double T2var;
	double TtotalVar;
	
	double T1avg;
	double T2avg;
	Double TtotalAvg = 0.0;
	double TtotalAvgVar =0;
	double icTtotalSup;
	double icTtotalInf;
	double icVarTotalSup;
	double icVarTotalInf;
	
	double n1avgVar = 0;
	double n2avgVar = 0;
	double nq1avgVar = 0;
	double nq2avgVar = 0;
	
	double X1avgVar = 0;
	double X2avgVar = 0;
	double W1avgVar = 0;
	double W2avgVar = 0;
	
	double varTotal;
	double TtotalVarVar;
	
	public int total;
	
	final double ci = 1.96;
	private double TtotalAvgQuad= 0;
	private double newAvgVar;

	public void instableCalculator() throws FileNotFoundException, IOException {
		Collections.sort(Ttotal);
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("transC.txt"));
		int i = 0;
		for (Double eachT : nqTotal) {
			stream.writeChars(time.get(i)+";"+eachT.toString() +"\n");
			i++;
		}
		stream.close();
	}
	
	public void totalCalculator() throws FileNotFoundException, IOException {
		Collections.sort(Ttotal);
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("cdf8.txt"));
		double i = 0;
		for (Double eachT : Ttotal) {
			stream.writeChars(eachT.toString()+";"+String.format("%.2f", i/total)+"\n");
			i++;
		}
		stream.close();
	}
	
	
	public void transientCalculatorVarTotal() throws FileNotFoundException, IOException {
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("transientVar.txt"));
		int i = 0;
		for (Double eachT : Ttotal) {
			stream.writeChars(time.get(i)+";"+(Math.pow(eachT-TtotalAvg,2) +"\n"));
			i++;
		}
		stream.close();
	}
	
	public void transientCalculatorTtotal() throws FileNotFoundException, IOException {
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("transientTtotal.txt"));
		int i = 0;
		for (Double eachT : Ttotal) {
			stream.writeChars(time.get(i)+";"+eachT.toString() +"\n");
			i++;
		}
		stream.close();
	}
	
	public void transientCalculatorT1() throws FileNotFoundException, IOException {
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("transientT1.txt"));
		int i = 0;
		for (Double eachT : T1) {
			stream.writeChars(id.get(i)+";"+eachT.toString() +"\n");
			i++;
		}
		stream.close();
	}
	
	public void transientCalculatorT2() throws FileNotFoundException, IOException {
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("transientT2.txt"));
		int i = 0;
		for (Double eachT : T2) {
			stream.writeChars(id.get(i)+";"+eachT.toString() +"\n");
			i++;
		}
		stream.close();
	}
	
	public void calculator() {
		for(int i=0;i<total;i++){
			T1.add(i, X1.get(i) + W1.get(i));
			T2.add(i, X2.get(i) + W2.get(i));
			Ttotal.add(i, T1.get(i) + T2.get(i));
			
			n1avg += n1.get(i);
			n2avg += n2.get(i);
			nq1avg += nq1.get(i);
			nq2avg += nq2.get(i);
			
			X1avg += X1.get(i);
			X2avg += X2.get(i);
			W1avg += W1.get(i);
			W2avg += W2.get(i);
			
			TtotalAvg += Ttotal.get(i);
			TtotalAvgQuad += Math.pow(Ttotal.get(i),2);
		}
		
		TtotalAvgQuad /= Ttotal.size();
		TtotalAvg /= Ttotal.size();
		newAvgVar = TtotalAvgQuad - Math.pow(TtotalAvg,2);

		n1avg /= total;
		n2avg /=total;
		nq1avg /= total;
		nq2avg /= total;
		
		X1avg /= total;
		X2avg /= total;
		W1avg /= total;
		W2avg /= total;
		
		T1avg = X1avg + W1avg;
		T2avg = X2avg + W2avg;
		
		for(int i=0;i<total;i++){
			n1avgVar += Math.pow((n1.get(i)-n1avg),2);
			n2avgVar += Math.pow((n2.get(i)-n2avg),2);
			nq1avgVar += Math.pow((nq1.get(i)-nq1avg),2);
			nq2avgVar += Math.pow((nq2.get(i)-nq2avg),2);
			
			X1avgVar += Math.pow((X1.get(i)-X1avg),2);
			X2avgVar += Math.pow((X2.get(i)-X2avg),2);
			W1avgVar += Math.pow((W1.get(i)-W1avg),2);
			W2avgVar += Math.pow((W2.get(i)-W2avg),2);
			TtotalAvgVar += Math.pow(Ttotal.get(i)-TtotalAvg,2);
			
			
		}
		n1avgVar /= total; 
		n2avgVar /=total;  
		nq1avgVar /= total;
		nq2avgVar /= total;
		                    
		X1avgVar /= total; 
		X2avgVar /= total; 
		W1avgVar /= total; 
		W2avgVar /= total; 
		TtotalAvgVar /= total;
		
		for(int i=0;i<total;i++){
			
			W1var = Math.pow((W1.get(i)-W1avg), 2);
			W2var = Math.pow((W2.get(i)-W2avg), 2);
			X1var = Math.pow((X1.get(i)-X1avg), 2);
			X2var = Math.pow((X2.get(i)-X2avg), 2);
			TtotalVar = Math.pow((Ttotal.get(i)-TtotalAvg),2);
			
			W1varVar += Math.pow((W1var-W1avgVar), 2);
			W2varVar += Math.pow((W2var-W2avgVar), 2);		
			X1varVar += Math.pow((X1var-X1avgVar), 2);		
			X2varVar += Math.pow((X2var-X2avgVar), 2);
			TtotalVarVar += Math.pow((TtotalVar-TtotalAvgVar), 2);
		}
		
		W1varVar /= total;
		W2varVar /= total;
		X1varVar /= total;
		X2varVar /= total;
		TtotalVarVar /= total;
		
		varTotal = X1avgVar+X2avgVar+W1avgVar+W2avgVar;
		icTtotalSup = (TtotalAvg) + ci* Math.sqrt(TtotalAvgVar) /Math.sqrt(total);
		icTtotalInf = (TtotalAvg) - ci* Math.sqrt(TtotalAvgVar) /Math.sqrt(total);
		
		
		icVarTotalInf = TtotalAvgVar - ci* Math.sqrt(TtotalVarVar) /Math.sqrt(total);
		icVarTotalSup = TtotalAvgVar + ci* Math.sqrt(TtotalVarVar) /Math.sqrt(total);

			System.out.println("Media Fila de espera total: "+W1avg);
		
		
			System.out.println("Media Ttotal: "+TtotalAvg);
			System.out.println("Lim Inf T: "+ icTtotalInf);
			System.out.println("Lim Sup T : "+ icTtotalSup);

			System.out.println("Var Total Metodo1: "+ TtotalAvgVar);
			System.out.println("Var Total Metodo2: " + newAvgVar);
			System.out.println("Lim Inf Var: "+ icVarTotalInf);
			System.out.println("Lim Sup Var: "+ icVarTotalSup);
			

	}
	
	
}
