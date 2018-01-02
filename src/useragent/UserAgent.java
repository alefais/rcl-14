/*
 * @author Alessandra Fais
 */
package useragent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import registry.User;

/**<i>class UserAgent</i>:
 * permette di avviare una nuova sessione lato utente e 
 * acquisisce informazioni come l'indirizzo IP da
 * utilizzare, la porta dedicata all'RMI e la porta 
 * dedicata alla connessione (UDP) con altri utenti
 * 
 * @author Alessandra Fais
 */
public class UserAgent extends UnicastRemoteObject {

	public static final long serialVersionUID = 1L;
	static List<User> friends = new LinkedList<User>();
	static String me = null;
	
	public UserAgent() throws RemoteException {
		super();
	}
	
	/**<i>class main</i>:
	 * avvia tre thread e permette di controllare il loro flusso di esecuzione
	 * interrompendolo nel momento in cui l'utente esprime la volontà di abbandonare la
	 * sessione digitando BYE (è corretto in questo caso interrompere la possibiltà di inserire
	 * comandi - InputThread - e di ricevere direttamente messaggi - ReceiveUDPThread) oppure
	 * di eliminare il proprio user dal servizio GOSSIP;
	 * il meccanismo utilizzato permette una comunicazione tra thread mediante chiamata da
	 * dentro il thread principale (InputThread) di metodi degli altri due thread che permettono
	 * la loro interruzione attraverso l'utilizzo di una variabile booleana di tipo <b>volatile</b>
	 * (i vantaggi sono molteplici: la variabile può essere modificata da diversi thread, si
	 * comporta come se fosse racchiusa in un blocco synchronized - comportamento thread safe 
	 * senza bisogno di lock espliciti e evitando qualsiasi rischio di deadlock);
	 * 
	 * @param args
	 * @throws RemoteException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws RemoteException, InterruptedException {
		String instructionsA = "You have to start UserAgent in the following mode: java -jar gossip <udp-port> <rmi-port> <IPaddress>";
		if(args.length<3) {
			System.out.println(instructionsA);
			return;
		}
		String udpport = args[0].trim();
		String rmiport = args[1].trim();
		String ipaddress = args[2].trim();
		String rmi = "rmi://"+ipaddress+":"+rmiport+"/UserAgent";
		
		UpdateThread u = new UpdateThread(rmiport, ipaddress, rmi);
		ReceiveUDPThread r = new ReceiveUDPThread(udpport);
		InputThread i = new InputThread(udpport, ipaddress, rmi, u, r);
		
		new Thread(u).start();
		new Thread(r).start();
		new Thread(i).start();
	}

}
