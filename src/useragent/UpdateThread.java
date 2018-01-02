/*
 * @author Alessandra Fais
 */
package useragent;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import registry.User;

/**<i>class UpdateThread</i>:
 * implementa tutte le funzioni messe a disposizione sull'interfaccia
 * RMI lato utente; questi metodi aggiornano i dati locali dell'utente: <ul>
 * <li><b>friends</b> è la lista degli amici dell'utente (mantenere una lista
 * 					  di strutture di tipo User permette all'utente di avere le 
 * 			   		  informazioni necessarie per poter scrivere a un amico: 
 * 			  		  indirizzo IP e porta)
 * </ul>
 * 
 * @author Alessandra Fais
 */
public class UpdateThread extends UnicastRemoteObject implements Runnable, UserAgentIF {
	private static final long serialVersionUID = 1L;
	String port = null;
	String ipaddress = null;
	String rmi = null;
	private volatile boolean end = false;
	InputThread i = null;
	
	public UpdateThread(String port, String ipaddress, String rmi) throws RemoteException {
		this.port = port;
		this.ipaddress = ipaddress;
		this.rmi = rmi;
	}
	
	public void shutdown(InputThread i) {
		this.i = i;
		end = true;
	}
	
	/**<i>method statusChange</i>
	 * permette di aggiornare lo stato (ONLINE/OFFLINE) degli utenti nella propria lista
	 * di amici in seguito a una loro richiesta di login/logout al sistema
	 * 
	 * @param friend è l'amico che ha effettuato un login/logout
	 */
	@Override
	public void statusChange(User friend) throws RemoteException, NullPointerException {
		String s = "Error while updating status.";
		if(friend==null)
			throw new NullPointerException("You've inserted null data.");
		else {
			s = String.format("User %s is now %s.", friend.getName(), friend.getStatus());

			if(UserAgent.friends.contains(friend))
				UserAgent.friends.set(UserAgent.friends.indexOf(friend), friend);
		}
		System.out.println(s+"\n");
		
		System.out.println("Updated friend list:");
		for(User i: UserAgent.friends)
			System.out.println("+ "+i.getName()+"["+i.getStatus()+"]");
	}

	/**<i>method updateFriendList</i>
	 * permette di aggiornare la lista di amici in locale ogni volta che l'utente aggiunge o
	 * rimuove un amico dai suoi contatti; viene trattata l'intera casistica delle situazioni
	 * possibili: <ol>
	 * <li>utente friend non presente in lista e src "out" significa che la chiamata proviene
	 * 		dalla addOutContactList e l'utente verrà perciò aggiunto alla lista friends </li>
	 * <li>utente friend presente in lista e scr "out" significa che la chiamata proviene
	 * 		dalla removeOutContactList e l'utente verrà perciò rimosso dalla lista friends </li>
	 * <li>src diverso da "out" (viene settato a "in" dalla registry infatti) significa che la 
	 * 		chiamata proviene dalla removeInContactList e l'utente friend verrà perciò rimosso 
	 * 		dalla lista friends poichè gli sono stati levati i permessi di allow (se l'utente 
	 * 		non è presente in friends la lista viene lasciata invariata) </li>
	 * </ol>
	 * 
	 * @param friend l'amico che viene aggiunto/rimosso dalla lista friends
	 * @param src indica la funzione della registry che invoca questo metodo in RMI <ul>
	 * 				<li><b>src.equals("out")</b> sta a significare che il metodo viene chiamato
	 * 											dalla addOutContactList oppure dalla removeOutContactList </li>
	 * 				<li><b>src.equals("in")</b> sta a significare che il metodo viene chiamato
	 * 											dalla removeInContactList </li>
	 * 				</ul>
	 */
	@Override
	public void updateFriendList(User friend, String src) throws RemoteException, NullPointerException {
		if(friend==null)
			throw new NullPointerException("You've inserted null data.");
		else if(!UserAgent.friends.contains(friend) && src.equals("out"))
			UserAgent.friends.add(friend);
		else
			UserAgent.friends.remove(friend);

		System.out.println("Updated friend list:");
		for(User s: UserAgent.friends)
			System.out.println("+ "+s.getName()+"["+s.getStatus()+"]");
	}
	
	/**<i>method uploadFriendList</i>
	 * è la callback chiamata al momento del login dello user in modo da avere la lista degli
	 * amici aggiornata
	 * 
	 * @param currentfriends è la lista corrente degli amici
	 */
	@Override
	public void uploadFriendList(List<User> currentfriends) throws RemoteException, NullPointerException {
		UserAgent.friends = new LinkedList<User>(currentfriends);
				
		System.out.println("Here is your friend list:");
		for(User l: UserAgent.friends)
			System.out.println("+ "+l.getName()+"["+l.getStatus()+"]");
	}

	/** <i>class run</i>:
	 * finchè l'utente non esegue una <i>unregister</i> oppure digita <i>bye</i> allora
	 * il thread continua a mantenere aperta la comunicazione in RMI; quando uno dei
	 * due eventi indicati si verifica, InputThread lo comunica settando la variabile end
	 * a true (chiamata al metodo shutdown di UpdateThread): a quel punto si interrompe 
	 * l'accettazione di qualsiasi chiamata RMI e, ottenuto il riferimento al thread 
	 * InputThread, si segnala la propria terminazione chiamando un suo metodo apposito 
	 * (utTerminated())
	 */
	public void run() {
		try {
			System.setProperty("java.rmi.server.hostname", ipaddress);
			Registry reg = LocateRegistry.createRegistry(Integer.parseInt(port));
			reg.rebind("UserAgent", new UpdateThread(port, ipaddress, rmi));
			System.out.println("UA ready on port "+port+"!");
			while(!end) {}
			super.unexportObject(reg, true);
			i.utTerminated();
		}
		catch(NumberFormatException | RemoteException e) {
			e.printStackTrace();
		}
	}

}
