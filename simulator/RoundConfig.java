package simulator;
// CLASSE QUE USAMOS PARA CONTROLE DAS RODADAS DURANTE A SIMULACAO
public class RoundConfig 
{
	private final int id;
	private int roundItens;
	public RoundConfig(int id) 
	{
		this.id = id;
	}
	public int getId() 
	{
		return id;
	}
	public int getRoundItens() 
	{
		return roundItens;
	}
	public void incRoundItens() 
	{
		this.roundItens++;
	}
}