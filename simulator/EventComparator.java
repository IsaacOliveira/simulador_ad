package simulator;
//ESTA CLASSE E DE EXTREMA IMPORTANCIA PARA O BOM FUNCIONAMENTO E ORDENACAO DE NOSSO EVENTOS.
//A USAMOS PARA DEFINIR O ATRIBUTO DE COMPARACAO DO EVENTO. NO NOSSO CASO, O HORARIO DE TRATAMENTO DO EVENTO.
import java.util.Comparator;

public class EventComparator implements Comparator<Event>{
@Override
public int compare(Event e1, Event e2) 
{
        if(e1.getTime() > e2.getTime()) 
            return 1;
        else if(e1.getTime() < e2.getTime())
            return -1;
        else
            return 0;   
}
}