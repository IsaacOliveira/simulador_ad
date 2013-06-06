package simulator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

import util.DataProcess;
import util.SampleGenerator;
public class Simulator 
{
	private int FIRST_QUEUE = 1;
	private int SECOND_QUEUE = 2;
	
	
	private boolean deploy= false;

	double lambda = 0.05;
	private final double mi1 = 0.1;
	private final float mi2 = 1;
	private final float dp = 1;

	private final int transientFaseSize = 50;
	private final int numberOfRounds = 20;
	private final int roundSize = 10000;

	//ESCOLHE SE O TIPO DE SERVIÇO VAI SER DETERMINISTICO OU NORMAL
	private String typeOfService = "exponential";

	//ESCOLHE O TIPO DE CASO, SE A FILA 2 VAI SER LCFS OU FCFS
	private final boolean case1 = true;
	
	private final boolean hideTransientFase = false;
	
	//Escolhe a continuidade, para a parte de fase transiente	
	private boolean continuity = false;
	private final ItemQueue queue1;
	private final ItemQueue queue2;
	private Item itemInService = null;
	private final ArrayList<Double> w1 = new ArrayList<Double>();
	private final ArrayList<Double> w2 = new ArrayList<Double>();
	DataProcess dataProcess = new DataProcess();

	//ESTE METODO SIMULATOR CRIA UMA INSTANCIA DO SIMULADOR,COM OS PARAMETROS PERTINENTES A SUA EXECUCAO
	public Simulator()
	{
		this.queue1 = new ItemQueue(FIRST_QUEUE);
		this.queue2 = new ItemQueue(SECOND_QUEUE);
	}
	//AQUI AS FUNCOES QUE RETORNA HORARIO DO NOVO EVENTO QUE SERA GERADOS, SEGUNDO O TIPO DE DISTRIBUICAO 
	//RECEBENDO COMO PARAMETRO O TEMPO CORRENTE E A TAXA DE 
	//CHEGADAS E O DESVIO PADRÃO, E RETORNA O HORARIO DESTE EVENTO ACONTECER NA TIMELINE DO SIMULADOR.

	private double getNextEventTimeExponential(double currentTime, double ratio) 
	{
		return currentTime + SampleGenerator.exponential(ratio);
	}

	private double getNextEventTimeDeterministic(double currentTime, double ratio) 
	{
		return currentTime + 1/ratio;
	}

	private double getNextEventTimeNormal(double currentTime, double ratio, double dp) 
	{
		return currentTime + SampleGenerator.normal(ratio, dp);
	}


	//AQUI E A FUNCAO QUE INICIA A EXECUCAO DO SIMULADOR.
	public void begin() throws IOException
	{
		//NESTE MOMENTO E CRIADA A LISTA DE EVENTOS, QUE E UMA FILA DE PRIORIDADE, ONDE PODEMOS DEFINIR 
		//QUAL O ATRIBUTO DO OBJETO SERÁ USADO PARA ORDENAÇÃO DELES NESTA FILA
		PriorityQueue<Event> eventQueue = new PriorityQueue<Event>(10, new EventComparator());
		RoundConfig currentRound = new RoundConfig(0);
		double currentTime = 0;
		double deltaT0 = 0;
		double deltaT1 = 0;
		double deltaT2 = 0;
		double deltaT3 = 0;
		double hora_entrada_servico1 = 0;
		double hora_entrada_servico2 = 0;
		//AGORA CRIAMOS O PRIMEIRO FREGUÊS, COM SEUS RESPECTIVOS ATRIBUTOS (NUMERO DA FILA E HORÁRIO DE CHEGADA)
		//E O ADICIONAMOS COMO PARÂMETRO PARA CRIAÇÃO DO PRIMEIRO EVENTO
		Item item1 = new Item(0);
		Event firstEvent = new Event(item1, Event.TYPE_ENTER_IN_QUEUE_1, currentTime);
		if(deploy){
			System.out.print(currentTime + ": ");
			System.out.print("Inicializacao do simulador;\n");
		}
		serverStatus();
		//LOGO A SEGUIR ADICIONAMOS O EVENTO NA FILA DE EVENTOS
		eventQueue.add(firstEvent);
		//AQUI O LOOP PRINCIPAL PARA CORRERMOS A LISTA DE EVENTOS PROCURANDO OS	EVENTOS QUE AINDA EXISTEM 
		while (!eventQueue.isEmpty()) 
		{
			Event currentEvent = eventQueue.remove();
			currentTime = currentEvent.getTime();
			//VERIFICA-SE QUAL TIPO DE EVENTO É O PROXIMO. NESTE CASO É A CHEGADA DE UM FREGUES NA FILA 1
			if (currentEvent.getType() == Event.TYPE_ENTER_IN_QUEUE_1) 
			{
				//AQUI PEGAMOS O FREGUES QUE ESTÁ RELACIONADO AO EVENTO	CORRENTE, HORARIO DE CHEGADA, Nq1, Nq2, N2 E NUMERO DA FILA
				Item item = currentEvent.getItem();
				item.setQueueId(FIRST_QUEUE);
				item.setQueue1ArriveTime(currentTime);
				item.setNq1(queue1.size());
				item.setNq2(queue2.size());
				item.setN2(queue2.size());
				//item.setRound(currentRound.getId());
				//SE EXISTE ALGUM FREGUES NO SERVIDOR QUE VEIO DA FILA 1, INCREMENTAMOS O
				//VALOR DE N1
				if (itemInService != null && itemInService.getQueueId() ==	FIRST_QUEUE )
				{
					item.setN1(queue1.size() + 1);
				}
				//ADICIONAMOS O ITEM ASSOCIADO A ESTE EVENTO NA FILA 1
				if (currentRound.getId() != 0 || !hideTransientFase) {
					////AREA ENTRE O MOMENTO ATUAL E O ANTERIOR ////
					deltaT0 = currentTime - queue1.getLastEventTime();
					queue1.setNq(queue1.size() * deltaT0);
					queue1.setN(queue1.size() * deltaT0);
					queue1.setN((queue1.size() + ((itemInService !=	null && itemInService.getQueueId() == FIRST_QUEUE) ? 1 : 0)) * deltaT0);
					/////////////
				}
				queue1.add(item);
				if(deploy){
					System.out.println("=====\n T1:"+ deltaT0 + " ∂= " + currentTime + " - " + queue1.getLastEventTime() + "\n======");
					System.out.println(" >>> " +currentTime + ": Item "	+ item.getId() + " entra na fila 1");
				}
				///
				queue1.setLastEventTime(currentTime);
				///
				serverStatus();
				//SE EXISTE ALGUM FREGUES DA CLASSE 2 EM SERVIÇO,	INCREMENTAMOS O VALOR DE N2
				if (itemInService != null && itemInService.getQueueId() ==	SECOND_QUEUE ) 
				{
					eventQueue.remove(new Event(itemInService, 0, 0));
					item.setN2(queue2.size() + 1);

					if(deploy) {
						System.out.print(currentTime + ": Item "	+ itemInService.getId() + " interrompido!!!!\n");
					}
					// AQUI IMPLEMENTAMOS A PREEMPCAO. TIRAMOS O ITEM QUE ESTA NO SERVICO E O COLOCAMOS DE VOLTA NA
					// FRENTE NA FILA 2. TAMBEM ATUALIZAMOS O TEMPO EM QUE ESTEVE EM SERVICO.
					if (currentRound.getId() != 0 || !hideTransientFase) {
						//// AREA ENTRE O MOMENTO ATUAL E O	ANTERIOR ////
						deltaT2 = currentTime -	queue2.getLastEventTime();
						queue2.setNq(queue2.size() * deltaT2);
						queue2.setN((queue2.size() + 1) * deltaT2);
					}
					if(deploy){
						System.out.println("=====\n T2:"+ deltaT2 + " ∂	= " + currentTime + " - " + queue2.getLastEventTime() + "\n======");
						System.out.println("=====\n T3:"+ deltaT3 + " ∂= " + currentTime + " - " + hora_entrada_servico2 + "\n======");
						System.out.println(currentTime + ": Item " +item.getId() + " entra na fila 1");
						System.out.println(" <<< "+currentTime + ":Item " + itemInService.getId() + " sai do servico 2");
					}
					queue2.setLastEventTime(currentTime);
					itemInService.setQueue2ArriveTime(currentTime);
					if(continuity){
						itemInService.addTimeInService2(currentTime - itemInService.getServico2ArriveTime());
					}
					//ADICIONA NO COMEÇO OU FIM DA FILA DEPENDENDO DO CASO
					if(case1){
						queue2.addOnTop(itemInService);
					} else {
						queue2.add(itemInService);
					}
					itemInService = null;
				}
				// CRIAMOS UM NOVO EVENTO DE CHEGADA
				eventQueue.add(new Event(new Item(item.getId()+1), Event.TYPE_ENTER_IN_QUEUE_1, getNextEventTimeExponential(currentTime, lambda)));
				serverStatus();
			} 
			else 
			{
				// EVENTO DE CHEGADA NO SERVICO 1
				if (currentEvent.getType() == Event.TYPE_ENTER_IN_SERVICE_1) 
				{
					hora_entrada_servico1 = enterInService1(eventQueue,	currentTime, currentEvent);
				}
				else if (currentEvent.getType() == Event.TYPE_ENTER_IN_QUEUE_2) 
				{
					// NO CASO DA CHEGADA NA FILA 2, PRECISAMOS VERIFICAR DE QUAL FILA ESTA VINDO ESTE FREGUES CORRENTE
					Item item = currentEvent.getItem();
					if (item.getQueueId() == FIRST_QUEUE) 
					{
						if(itemInService != null &&	itemInService.getQueueId() == SECOND_QUEUE){
							item.setNq2(queue2.size() + 1);
						}
						if (currentRound.getId() != 0 || !	hideTransientFase) {
							deltaT1 = currentTime -	hora_entrada_servico1;
						}
						queue1.setN(deltaT1);
						if(deploy) {
							System.out.println("=====\n T1:"+ "∂"+ deltaT1 + " = " + currentTime + " - " + hora_entrada_servico1 + "\n======");
						}
						if (currentRound.getId() != 0 || !	hideTransientFase) {
							deltaT2 = currentTime -	queue2.getLastEventTime();
							queue2.setNq(queue2.size() * deltaT2);
							queue2.setN((queue2.size()) * deltaT2);
						}
						if(deploy) {
							System.out.println("====\n T2:"+ "∂" +	deltaT2 + " = " + currentTime + " - " + queue2.getLastEventTime() + "\n======");
							System.out.print(" >>> "+currentTime +": Item " + item.getId() + " sai do servico 1 e ENTRA FILA 2\n");
						}
						queue2.setLastEventTime(currentTime);
						item.setQueueId(SECOND_QUEUE);
						item.setTimeInService1(currentTime -(item.getQueue1ArriveTime() + item.getW1()));
						item.setQueue2FirstArrival(currentTime);
					} 
					else if (item.getQueueId() == SECOND_QUEUE)
					{
						if(deploy) {
							System.out.print(currentTime + ":Item " + item.getId() + " VOLTOU PARA FILA 2\n");
						}
						serverStatus();
						item.addTimeInService2(currentTime - item.getServico2ArriveTime());
						if(deploy) {
							System.out.print("X2 Item " +	item.getId() + ": " + item.getTimeInService2() + "\n ");
						}
					}
					item.setQueue2ArriveTime(currentTime);
					itemInService = null;
					queue2.addOnTop(item);
					serverStatus();
				} 
				//SE O EVENTO FOR ENTRADA NO SERVICO 2, CALCULAMOS O TEMPO EM QUE ESTEVE EM ESPERA - SE FOR O CASO.
				//LOGO APOS, GERAMOS MAIS UM EVENTO, DESTA VEZ DE SAIDA DO SISTEMA DO FREGUES QUE ESTA SENDO SERVIDO.
				else if (Event.TYPE_ENTER_IN_SERVICE_2 == currentEvent.getType()) 
				{
					deltaT2 = enterInService2(eventQueue, currentRound,	currentTime, deltaT2, currentEvent);
				} 
				//CASO SEJA UM EVENTO DE SAIDA DO SISTEMA, TIRAMOS O FREGUES ATUAL DO SERVICO E CALCULAMOS SEU TEMPO
				//DE SERVICO 2. LOGO DEPOIS GRAVAMOS AS ESTATISTICAS PERTINENTES A ESSE CLIENTE.
				else if (Event.TYPE_SYSTEM_EXIT == currentEvent.getType())
				{
					Item item = currentEvent.getItem();
					item.addTimeInService2(currentTime - item.getServico2ArriveTime());
					itemInService = null;
					double T2;
					//double tamFila2 = queue2.size()+1;
					//item.setN2(tamFila2);
					if (currentRound.getId() != 0 || !hideTransientFase) {
						deltaT2 = currentTime -	queue2.getLastEventTime();
						queue2.setNq(queue2.size() * deltaT2);
						queue2.setN((queue2.size() + 1) * deltaT2);
					}
					T2 = currentTime - item.getQueue2FirstArrival();
					if(deploy){
						System.out.println("====\n T2:"+ deltaT2 + " ∂= " + currentTime + " - " + queue2.getLastEventTime() + "\n======");
						System.out.print(" <<< "+ currentTime + ":Item " + item.getId() + " SAI DO SERVICO 2 e sistema T2 " + T2 + "\n");
					}
					queue2.setLastEventTime(currentTime);
					serverStatus();
					if ((currentRound.getId() != 0) || (!hideTransientFase))
					{
						if(deploy) {
							recordSim("===================\nITEM " + String.valueOf(item.getId()),"\nRodada: " +
									String.valueOf(currentRound.getId()), "\nW1: " + String.valueOf(item.getW1()), 
									"\nX1 :" +String.valueOf(item.getTimeInService1()), "\nW2: " +String.valueOf(item.getW2()), 
									"\nX2: " +String.valueOf(item.getTimeInService2()), "\nN1: " +String.valueOf(item.getN1()),
									"\nNq1: " +String.valueOf(item.getNq1()), 
									"\nN2: " +String.valueOf(item.getN2()), "\nNq2: " +String.valueOf(item.getNq2()) +
									"\n====================");
						}
						else{
							/*recordSim(String.valueOf(item.getId()),String.valueOf(currentRound.getId()),String.valueOf(item.getW1()),
								String.valueOf(item.getTimeInService1()),String.valueOf(item.getW2()), 
								String.valueOf(item.getTimeInService2()),String.valueOf(item.getN1()),
								String.valueOf(item.getNq1()), String.valueOf(item.getN2()), String.valueOf(item.getNq2()));*/
						}
						//Seta os valores para fazer os calculos de esperança e variancia

						dataProcess.n1.add(item.getN1());
						dataProcess.nq1.add(item.getNq1());
						dataProcess.n2.add(item.getN2());
						dataProcess.nq2.add(item.getNq2());
						dataProcess.nqTotal.add(item.getNq1()+item.getNq2());

						dataProcess.id.add(item.getId());
						dataProcess.W1.add(item.getW1());
						dataProcess.W2.add(item.getW2());
						dataProcess.X1.add(item.getTimeInService1());
						dataProcess.X2.add(item.getTimeInService2());
						dataProcess.time.add(currentTime);

						dataProcess.total = numberOfRounds*roundSize;
						System.out.println(item.getId());
					}

					// AQUI FICA O TRATAMENTO DA FASE TRANSIENTE, SE APLICAVEL
					currentRound.incRoundItens();
					if (currentRound.getId() == 0) 
					{
						if (currentRound.getRoundItens() >=	transientFaseSize) {
							if (numberOfRounds == 0) break;
							currentRound = new RoundConfig(1);
						}
					} else {
						if (currentRound.getRoundItens() >=	roundSize) {
							if (currentRound.getId() <	numberOfRounds) {
								currentRound = new	RoundConfig(currentRound.getId()+1);
							} else {
								break;
							}
						}
					}
				}
			}
			// NESTA SECAO TRATAMOS OS EVENTOS DE CHEGADA NOS SERVICOS 1 E 2, QUANDO SABEMOS QUE O SERVIDOR ESTA VAZIO
			if (itemInService == null) 
			{
				if (!queue1.isEmpty()) 
				{
					deltaT0 = enterService1EmptyServer(eventQueue, currentTime);
				} 
				else if (!queue2.isEmpty()) 
				{
					deltaT2 = enterService2EmptyServer(eventQueue, currentTime);
				}
			}
		}

		double mediaNq1 = queue1.getNq()/currentTime;
		System.out.println("\nMEDIA Nq1: "+mediaNq1 +"=" + queue1.getNq() + "/"	+ currentTime);
		double mediaN1 = queue1.getN()/currentTime;
		System.out.println("MEDIA N1: "+mediaN1 +"=" + queue1.getN() + "/" + currentTime);
		double mediaNq2 = queue2.getNq()/currentTime;
		System.out.println("MEDIA Nq2: "+mediaNq2 +"=" + queue2.getNq() + "/" +	currentTime);
		double mediaN2 = queue2.getN()/currentTime;
		System.out.println("MEDIA N2: "+mediaN2 +"=" + queue2.getN() + "/" + currentTime);
		System.out.println("TEMPO TOTAL: "+ currentTime);

		//FUNCOES QUE CALCULAM AS METRICAS DE INTERESSE
		dataProcess.instableCalculator();
		dataProcess.calculator();
		dataProcess.transientCalculatorVarTotal();
		dataProcess.transientCalculatorTtotal();
		dataProcess.transientCalculatorT1();
		dataProcess.transientCalculatorT2();

		dataProcess.totalCalculator();
	}

	private double enterInService2(PriorityQueue<Event> eventQueue,	RoundConfig currentRound, double currentTime, double deltaT2, Event currentEvent) {
		Item item = currentEvent.getItem();
		item.addW2(currentTime - item.getQueue2ArriveTime());
		item.setServico2ArriveTime(currentTime);
		w2.add(item.getW2());
		itemInService = item;
		eventQueue.add(new Event(item, 	Event.TYPE_SYSTEM_EXIT, getNextEventTimeExponential(currentTime, mi2)));
		serverStatus();
		if (currentRound.getId() != 0 || !hideTransientFase) {
			deltaT2 = currentTime -	queue2.getLastEventTime();
			queue2.setNq(queue2.size() * deltaT2);
			queue2.setN((queue2.size() + ((itemInService != null && itemInService.getQueueId() == SECOND_QUEUE) ? 1 : 0))*deltaT2);
		}
		if(deploy) {
			System.out.println("====\n T2:"+ deltaT2 + " ∂= " + currentTime + " - " + queue2.getLastEventTime() + "\n======");
			System.out.print(" >>> "+currentTime + ":Item " + item.getId() + " ENTRA NO SERVICO 2\n");
		}
		queue2.setLastEventTime(currentTime);
		return deltaT2;
	}

	private double enterInService1(PriorityQueue<Event> eventQueue,	double currentTime, Event currentEvent) {
		double hora_entrada_servico1;
		Item item = currentEvent.getItem();
		// NESTE MOMENTO ATUALIZAMOS O TEMPO DE ESPERA W1, QUE E O HORARIO ATUAL MENOS O HORARIO DE CHEGADA NA FILA 1
		item.setW1(currentTime - item.getQueue1ArriveTime());
		w1.add(item.getW1());
		itemInService = item;
		if(deploy){
			System.out.print(" >>> " + currentTime + ":Item " + item.getId() + " entra em servico 1\n");
		}
		serverStatus();
//		 CRIAMOS UM NOVO EVENTO DE CHEGADA, DESTE FREGUES NA FILA 2 dependendo do tipo de serviço do servidor 1
		if(typeOfService.equals("deterministic")) {
			eventQueue.add(new Event(item, Event.TYPE_ENTER_IN_QUEUE_2, getNextEventTimeDeterministic(currentTime, mi1))); 
		} else if(typeOfService.equals("normal")) {
			eventQueue.add(new Event(item, Event.TYPE_ENTER_IN_QUEUE_2, getNextEventTimeNormal(currentTime, mi1, dp))); 
		} else if(typeOfService.equals("exponential")) {
			eventQueue.add(new Event(item, Event.TYPE_ENTER_IN_QUEUE_2, getNextEventTimeExponential(currentTime, mi1)));
		}
		hora_entrada_servico1 = currentTime;
		return hora_entrada_servico1;
	}

	private double enterService2EmptyServer(PriorityQueue<Event> eventQueue, double currentTime) {
		double deltaT2;
		///// saida de 2, vejo tamanho da fila e por quanto	tempo esteve com esse tamanho. ///////
		deltaT2 = currentTime - queue2.getLastEventTime();
		queue2.setNq(queue2.size()*deltaT2);
		queue2.setN((queue2.size()+1)*deltaT2);
		Item item = queue2.remove(0);
		eventQueue.add(new Event(item, Event.TYPE_ENTER_IN_SERVICE_2, currentTime));
		if(deploy){
			System.out.println("====\n T2:"+ deltaT2 + " ∂= " + currentTime + " - " + queue2.getLastEventTime() + "\n======");
			System.out.println(" <<< " + currentTime + ":Item " + item.getId() + " sai da fila 2");
		}
		queue2.setLastEventTime(currentTime);
		return deltaT2;
	}

	private double enterService1EmptyServer(PriorityQueue<Event> eventQueue, double currentTime) {
		double deltaT0;
		///// saida de 1, vejo tamanho da fila e por quanto	tempo ficou com esse tamanho. ///////
		deltaT0 = currentTime - queue1.getLastEventTime();
		queue1.setNq(queue1.size()*deltaT0);
		Item item = queue1.remove(0);
		eventQueue.add(new Event(item, Event.TYPE_ENTER_IN_SERVICE_1, currentTime));
		if(deploy){
			System.out.println("====\n T1:"+ deltaT0 + " ∂= " + currentTime + " - " + queue1.getLastEventTime() + "\n======");
			System.out.println(" <<< " + currentTime + ":Item " + item.getId() + " sai da fila 1");
		}
		queue1.setLastEventTime(currentTime);
		return deltaT0;
	}
	// FUNCAO QUE USAMOS PARA GRAVAR OS DADOS DO FREGUES ATENDIDO PELO SISTEMA
	private void recordSim(String... data) throws IOException 
	{
		String record = "";
		for (String dado : data) 
		{
			record += dado + ";";
		}
		record = record.substring(0, record.length()-1);
		System.out.println(record);
	}
	// SE TIVER A OPCAO DE DEPURACAO EM TRUE, CONTROLA A EXIBICAO DOS 
	private void serverStatus(){
		if(deploy){
			System.out.println();
			System.out.print("===\nFILA 2 [ ");
			for(int i = 0; i < queue2.size(); i++){
			}
			System.out.print(" ] \n");
			System.out.print("FILA 1 [ ");
			for(int i = 0; i < queue1.size(); i++){
				System.out.print(queue1.get(i).getId() + ",");
			}
			System.out.print(" ] \n");
			System.out.print("N1: " + queue1.getN() + ", Nq1: "+ queue1.getNq() +"\n");
			System.out.print("N2: " + queue2.getN() + ", Nq2: "+ queue2.getNq() +"\n");
			if(itemInService != null){
				System.out.print("SERVIDOR: (" + itemInService.getId() + ") \n");
			}
			else{
				System.out.print("SERVIDOR: ( ) \n===\n");
			}
		}
	}
}
