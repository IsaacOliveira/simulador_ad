package simulator;
import java.util.ArrayList;
public class ItemQueue extends ArrayList<Item> 
{
	private static final long serialVersionUID = 1L;
	private final int id;
	private double lastEventTime;
	private double N;
	private double Nq;
	
	public ItemQueue(int id) 
	{
		this.id = id;
	}
	public int getId() 
	{
		return id;
	}
	public void addOnTop(Item item) 
	{
		this.add(0, item);
	}
	public double getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(double lastArrivalTime) {
		this.lastEventTime = lastArrivalTime;
	}
	public double getN() {
		return N;
	}
	public void setN(double n) {
		N += n;
	}
	public double getNq() {
		return Nq;
	}
	public void setNq(double nq) {
		Nq += nq;
	}
}