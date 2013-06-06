package simulator; 
// CLASSE MODELO DOS EVENTOS. TEM COMO ATRIBUTOS OS TIPOS DE EVENTO QUE O PROGRAMA SIMULA. 
// OS METODOS DESTA CLASSE, SE DESTINAM A ATUALIZAR OU RETORNAR VALORES DO ATRIBUTO DO EVENTO EM QUESTAO. 
// EXCETO O ULTIMO QUE COMPARA OBJETOS DESTA CLASSE, PARA CONTROLE, SE NECESSARIO. 
public class Event 
{ 
	public static final int TYPE_ENTER_IN_QUEUE_1 = 0; 
	public static final int TYPE_ENTER_IN_SERVICE_1 = 1; 
	public static final int TYPE_ENTER_IN_QUEUE_2 = 2; 
	public static final int TYPE_ENTER_IN_SERVICE_2 = 3; 
	public static final int TYPE_SYSTEM_EXIT = 4; 
	private final Item item; 
	private final double time; 
	private final int type; 

	public Event(Item item, int type, double time) 
	{ 
		this.item = item; 
		this.type = type; 
		this.time = time; 
	} 
	public Item getItem() 
	{ 
		return item; 
	} 
	public double getTime() 
	{ 
		return time; 
	} 
	public int getType() 
	{ 
		return type; 
	} 
	public boolean equals(Object object) 
	{ 
		if (object == null) 
			return this == null; 
		if (!(object instanceof Event)) 
			return false; 
		Event event = (Event) object; 
		return event.getItem().getId() == this.getItem().getId(); 
	} 
}