/*
 * @author Alessandra Fais
 */
package registry;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;

import exceptions.*;

public class Test {
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
		RegistryIF r = (RegistryIF) Naming.lookup("rmi://172.241.0.1:58643/Registry");
		Map<String, User> m = null;
		
		String[] users = {"pippo", "pappo", "pluto", "marco", "ciccio", "ale", "leo"};
		int i;

		for(i=0; i<users.length; i++)
			r.createUser(users[i], "1234", "172.241.0.100", "rmi://localhost:1234/Registry");

		//TEST: REMOVE USERS
		m = r.getStatus();
		for(i=0; i<users.length; i++) {
			System.out.println("Name: "+m.get(users[i]).getName());
			System.out.println("Status: "+m.get(users[i]).getStatus());
		}
		System.out.println("1. OK -> Sono presenti tutti gli utenti registrati e sono tutti OFFLINE");
	
		try {
			r.removeUser("pippo");
			r.removeUser("ale");
			r.removeUser("pippo");
		}
		catch(NotFoundException e) {
			System.err.println("2. OK -> User pippo not found");
		}
		
		try {
			r.removeUser(null);
		}
		catch(NullPointerException e) {
			System.err.println("3. OK -> Invalid user: null");
		}

		r.createUser(users[0], "1234", "172.241.0.100", "rmi://localhost:1234/Registry");
		r.createUser(users[5], "1234", "172.241.0.100", "rmi://localhost:1234/Registry");
		m = r.getStatus();
	
		//TEST: DUPLICATE USER (INSERTION)
		try {
			r.createUser("leo", "1234", "172.241.0.1", "rmi://localhost:1234/Registry");
		}
		catch(DuplicateException e) {
			System.err.println("4. OK -> User leo is already registred");
		}
		
		if(!r.isUser("platone"))
			System.out.println("5. OK");
		else
			System.out.println("NO");
		if(r.isUser("marco"))
			System.out.println("6. OK");
		else
			System.out.println("NO");
		
		//TEST: USERS STRUCTURES
		m = r.getStatus();
		for(i=0; i<users.length; i++) {
			System.out.println("Name: "+m.get(users[i]).getName());
			System.out.println("Status: "+m.get(users[i]).getStatus());
			System.out.println("Port: "+m.get(users[i]).getPort());
			System.out.println("IPaddress: "+m.get(users[i]).getIPaddress());
			System.out.println("RMI: "+m.get(users[i]).getRmi());
		}

		/*Per poter testare la parte seguente è necessario commentare nei
		 * metodi della registry la parte relativa alle callback e ai proxy;
		 * tale test è stato realizzato infatti come prova delle funzionalità
		 * pure della registry, in modo sequenziale, prima dell'implementazione
		 * delle callback e della gestione dei proxy
		 */
		
		//TEST: LOGIN-LOGOUT
		try {
			r.login("pippo");
			r.login("pippo");
		}
		catch(StatusException e) {
			System.err.println("7. OK -> pippo already logged in");
		}

		try {
			r.logout("pappo", false);
		}
		catch(StatusException e) {
			System.err.println("8. OK -> pappo already logged out");
		}
		
		try {
			r.login("ale");
			r.login("marco");
			r.login("leo");
		}
		catch(StatusException e) {
			System.out.println("NO");
		}

		//TEST: MANAGE ALLOWED AND FRIEND
		r.addInContactList("ale", "marco");
		r.addInContactList("ale", "leo");
		r.addInContactList("marco", "pappo");
		r.addInContactList("leo", "pippo");
		r.addInContactList("ale", "pippo");
		r.addInContactList("leo", "ale");

		System.out.println("ale_in: marco, leo, pippo");
		System.out.println(r.getMyAllowed("ale"));
		System.out.println("marco_in: pappo");
		System.out.println(r.getMyAllowed("marco"));
		System.out.println("leo_in: pippo, ale");
		System.out.println(r.getMyAllowed("leo"));

		r.addOutContactList("marco", "ale");
		r.addOutContactList("leo", "ale");
		r.addOutContactList("pippo", "ale");
		
		System.out.println("ale was friended by: marco, leo, pippo");
		System.out.println(r.getMyFriends("ale"));

		try {
			r.addInContactList("ale", "marco");
		}
		catch(DuplicateException e) {
			System.err.println("9. OK -> marco already in!");
		}
		
		try {
			r.addOutContactList("ale", "leo");
		}
		catch(PermissionException e) {
			System.err.println("10. OK -> leo doesn't allow ale!");
		}
		
		r.removeInContactList("ale", "pippo");
		
		try {
			r.removeInContactList("ale", "pippo");			
		}
		catch(NotFoundException e) {
			System.err.println("11. OK -> ale already disallowed pippo!");
		}
		
		r.removeOutContactList("ale", "leo");

	}
	
}
