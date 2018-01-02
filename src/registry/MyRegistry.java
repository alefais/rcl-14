/*
 * 	@author Alessandra Fais
*/
package registry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import proxy.ProxyStatus;
import useragent.MyProxyCoordinate;
import useragent.UserAgentIF;
import exceptions.DuplicateException;
import exceptions.NotFoundException;
import exceptions.PermissionException;
import exceptions.StatusException;

/**<i>class MyRegistry</i>:
 * implementa tutti i metodi definiti nell'interfaccia RegistryIF e si occupa della
 * gestione delle strutture dati relative agli utenti e ai proxy: <ul>
 * 
 * <li>UTENTI</li>
 * Tutte le strutture dati sono HashMap. <ul>
 * <li>statusMap contiene le associazioni nickname utente - struttura User (informazioni dell'utente)
 * 			   per ogni utente registrato al servizio </li>
 * <li>inContacts mantiene per ogni utente la lista aggiornata di coloro che lui ha autorizzato (allowed) </li>
 * <li>outContacts mantiene per ogni utente la lista di coloro che lo hanno inserito nella propria
 * 				 lista di amici (questa struttura è stata pensata per agevolare la fase di aggiornamento
 * 				 dei singoli utenti sui cambi di stato ecc. dei loro amici) </li>
 * <li>outContactsForUA mantiene per ogni utente la lista aggiornata dei suoi amici (friend) </li>
 * </ul>
 * 
 * <li>PROXY</li>
 * Un'unica struttura dati, una lista. <ul>
 * <li>proxylist contiene l'elenco aggiornato dei proxy connessi al sistema (sono accessibili tutte le
 * 			   informazioni caratterizzanti i proxy mediante l'accesso ai campi delle strutture ProxyStatus) </li>
 * </ul>
 * </ul>
 * 
 * @author Alessandra Fais
 */
public class MyRegistry extends UnicastRemoteObject implements RegistryIF {
	
	public static final long serialVersionUID = 1L;
	private Map<String, User> statusMap = new ConcurrentHashMap<String, User>();
	private Map<String, LinkedList<String>> inContacts = new ConcurrentHashMap<String, LinkedList<String>>();
	private Map<String, LinkedList<String>> outContacts = new ConcurrentHashMap<String, LinkedList<String>>();
	private Map<String, LinkedList<User>> outContactsForUA = new ConcurrentHashMap<String, LinkedList<User>>();
	UserAgentIF registry = null;
	private static LinkedList<ProxyStatus> proxylist = new LinkedList<ProxyStatus>();
		
	public MyRegistry() throws RemoteException {
		super();
	}

	public static void main(String[] args) throws RemoteException {
		System.setProperty("java.rmi.server.hostname", "172.241.0.1");
		Registry reg = LocateRegistry.createRegistry(58643);
		reg.rebind("Registry", new MyRegistry());

		System.out.println("Registry ready on 172.241.0.1, port 58643.");
		
		new Thread(new ManageRegProxy(proxylist)).start();
	}
	
	/**<i>method balanceProxies</i>:
	 * trova il proxy con carico di utenti minore e lo candida per ricevere
	 * il nuovo utente che ha fatto logout
	 * @param uaname nickname dell'utente che ha fatto logout
	 * @return <b>ps</b> di tipo ProxyStatus; se non c'è nessun proxy disponibile
	 * 			    ritorna null, altrimenti ritorna la struttura ProxyStatus
	 * 				del proxy cui verrà assegnato l'utente uaname OFFLINE
	 */
	private synchronized ProxyStatus balanceProxies(String uaname) {
		int min, idx;
		ProxyStatus ps = null;
		if(!proxylist.isEmpty()) {
			min = proxylist.get(0).getUacounter();
			idx = 0;
			for (int i = 1; i < proxylist.size(); i++) {
				if (proxylist.get(i).getUacounter() < min) {
					min = proxylist.get(i).getUacounter();
					idx = i;
				}
			}
			ps = proxylist.get(idx);
			ps.setUacounter(min+1);
			ps.addUAtoProxy(uaname);
			proxylist.set(idx, ps);
		}
		if(ps!=null) {
			System.out.println("A valid proxy was found.");
			System.out.println("Proxy is working on "+ps.getIpaddress()+", port "+ps.getUaport()+".");
			System.out.println("Proxy is managing "+ps.getUacounter()+" users.");
			System.out.print("Users in cache are ");
			for(String t:ps.getUsersInCache())
				System.out.print(t+" ");
			System.out.println();
		}		
		return ps;
	}
	
	/**<i>method addRemUAonProxies</i>:
	 * se ps è un proxy valido (non null), ossia è un proxy che può prendere in carico l'utente disconnesso
	 * (caso logout user) oppure è il proxy che gestiva l'utente disconnesso (caso login user), allora
	 * esso viene notificato del cambiamento di stato dello user nickname e si occuperà di fare gli aggiornamenti
	 * necessari (la registry contatta il proxy mediante protocollo UDP)
	 * @param ps struttura ProxyStatus del proxy (null se il proxy non è valido)
	 * @param nickname nome dell'utente che ha fatto login/logout
	 * @param src stringa che indica se l'operazione effettuata dall'utente è stata un login o un logout
	 * @throws IOException
	 */
	private void addRemUAonProxies(ProxyStatus ps, String nickname, String src) throws IOException {
		if(ps!=null) {
			DatagramSocket sock = null;
			DatagramPacket packet = null;
			byte[] buf = new byte[1024];
			
			sock = new DatagramSocket(57985);
			
			InetAddress addr = InetAddress.getByName(ps.getIpaddress());
			if(src.equals("LOGOUT"))
				buf = new String("addUA:"+ps.getUacounter()+":"+nickname).getBytes();
			if(src.equals("LOGIN")) {
				buf = new String("remUA:"+nickname+":"+statusMap.get(nickname).getPort()).getBytes();				
				ps.setUacounter((ps.getUacounter())-1);
			}
			
			packet = new DatagramPacket(buf, buf.length, addr, (ps.getUaport()+1));
			
			sock.send(packet);
			sock.close();
		
			System.out.println("Proxy is working on "+ps.getIpaddress()+", port "+ps.getUaport()+".");
			System.out.println("Proxy is managing "+ps.getUacounter()+" users.");
			System.out.print("Users in cache are ");
			for(String t:ps.getUsersInCache())
				System.out.print(t+" ");
			System.out.println();
		}
		else
			System.out.println("No proxy available!");
	}
	
	@Override
	public void createUser(String nickname, String port, String ipaddress, String rmi) throws RemoteException,
			NullPointerException, DuplicateException, NotBoundException {
		if(nickname==null || port==null || ipaddress==null || rmi==null || nickname=="")
			throw new NullPointerException("You've inserted null data.");
		else if(isUser(nickname))
			throw new DuplicateException("The inserted nickname already exists.");
		else {
			statusMap.put(nickname, new User(nickname, "OFFLINE", Integer.parseInt(port), ipaddress ,rmi, null, -1));
			inContacts.put(nickname, new LinkedList<String>());
			outContacts.put(nickname, new LinkedList<String>());
			outContactsForUA.put(nickname, new LinkedList<User>());

			System.out.println("User "+nickname+" added!");
		}
	}

	@Override
	public void removeUser(String nickname) throws RemoteException,
			NullPointerException, NotFoundException {
		if(nickname==null)
			throw new NullPointerException("You've inserted null data.");
		else if(!isUser(nickname))
			throw new NotFoundException("Unable to find the user with the inserted nickname.");
		else {
			
			try {
				logout(nickname, true);
			} catch (StatusException | MalformedURLException | NotBoundException e) {
				e.printStackTrace();
			}
			
			statusMap.remove(nickname);
			inContacts.remove(nickname);
			outContacts.remove(nickname);
			outContactsForUA.remove(nickname);
			
			System.out.println("User "+nickname+" removed!");
		}
	}

	@Override
	public boolean isUser(String nickname) throws RemoteException,
			NullPointerException {
		if(nickname==null)
			throw new NullPointerException("You've inserted null data.");
		else if(statusMap.containsKey(nickname))
			return true;
		else
			return false;
	}

	/**<i>method login</i>:
	 * se l'utente nickname è correttamente registrato e OFFLINE allora <ol>
	 * <li>Si cerca tra i proxy (memorizzati nella lista proxylist) quello che lo aveva in gestione
	 *   (per far ciò nella struttura dell'utente vengono memorizzati al logout l'indirizzo IP del proxy
	 *   e la porta: quando l'utente effettua il login questi campi prendono valori non significativi,
	 *   rispettivamente null per l'IP e -1 per la porta - null e -1 saranno i valori presenti anche al primo
	 *   login dopo la registrazione) </li>
	 * <li>Si salvano l'indirizzo IP e la porta del proxy in un record MyProxyCoordinate: tale coppia verrà ritornata
	 *   all'utente che la potrà utilizzare per aprire una connessione verso il suo proxy e ricevere 
	 *   eventuali messaggi a lui recapitati durante la sua assenza (TCP) </li>
	 * <li>Si effettua la rimozione dell'utente dal proxy </li>
	 * <li>Si aggiorna lo stato dell'utente </li>
	 * <li>Si notifica a chi ha l'utente come amico il suddetto cambiamento di stato </li>
	 * </ol>
	 * 
	 * @param nickname l'utente che richiede l'operazione di login
	 * @return <b>pc</b> struttura di tipo MyProxyCoordinate
	 */
	@Override
	public MyProxyCoordinate login(String nickname) throws RemoteException,
			NullPointerException, NotFoundException, StatusException, MalformedURLException, NotBoundException {
		if(nickname==null)
			throw new NullPointerException("You've inserted null data.");
		else if(!isUser(nickname))
			throw new NotFoundException("Unable to find the user with the inserted nickname.");
		else {
			if(statusMap.get(nickname).getStatus().equals("OFFLINE")) {
				
				ProxyStatus ps = null;
				synchronized(proxylist) {
					for(ProxyStatus p:proxylist) {
						if(p.getIpaddress().equals(statusMap.get(nickname).getIPaddress()) &&
								p.getUaport()==statusMap.get(nickname).getPort())
							ps = p;
					}
				}
				
				MyProxyCoordinate pc = new MyProxyCoordinate(statusMap.get(nickname).getIPaddress(), statusMap.get(nickname).getPort());
				
				try {
					addRemUAonProxies(ps, nickname, "LOGIN");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				statusMap.get(nickname).setStatus("ONLINE");
				statusMap.get(nickname).setProxyIP(null);
				statusMap.get(nickname).setProxyport(-1);
				registry = (UserAgentIF) Naming.lookup(statusMap.get(nickname).getRmi());
				registry.uploadFriendList(outContactsForUA.get(nickname));
				
				for(String u: outContacts.get(nickname)) {
					if(statusMap.get(u)!=null && statusMap.get(u).getStatus().equals("ONLINE")) {
						registry = (UserAgentIF) Naming.lookup(statusMap.get(u).getRmi());
						registry.statusChange(statusMap.get(nickname));
					}
				}
				
				System.out.println("User "+nickname+" logged in!");
				
				return pc;
			}
			else
				throw new StatusException("User is already logged in.");
		}
	}

	@Override
	public void logout(String nickname, boolean removeuser) throws RemoteException,
			NullPointerException, NotFoundException, StatusException, MalformedURLException, NotBoundException {
		if(nickname==null)
			throw new NullPointerException("You've inserted null data.");
		else if(!isUser(nickname))
			throw new NotFoundException("Unable to find the user with the inserted nickname.");
		else {
			if(statusMap.get(nickname).getStatus().equals("ONLINE")) {
				statusMap.get(nickname).setStatus("OFFLINE");
				
				if(!removeuser) {
					ProxyStatus ps = null;
					try {
						ps = balanceProxies(nickname);
						addRemUAonProxies(ps, nickname, "LOGOUT");
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				
					statusMap.get(nickname).setProxyIP(ps.getIpaddress());
					statusMap.get(nickname).setProxyport(ps.getUaport());
				}
				
				for(String u: outContacts.get(nickname)) {
					if(statusMap.get(u)!=null && statusMap.get(u).getStatus().equals("ONLINE")) {
						registry = (UserAgentIF) Naming.lookup(statusMap.get(u).getRmi());
						registry.statusChange(statusMap.get(nickname));
					}
				}
				
				System.out.println("User "+nickname+" logged out!");
			}
			else
				throw new StatusException("User is already logged out.");
		}
	}

	@Override
	public void addInContactList(String nickname, String user) throws RemoteException,
			NullPointerException, DuplicateException, NotFoundException {
		if(nickname==null || user==null)
			throw new NullPointerException("You've inserted null data.");
		else if(isUser(nickname) && isUser(user) && !nickname.equals(user)) {
			if(inContacts.get(nickname).contains(user))
				throw new DuplicateException("You've already allowed the user with the inserted nickname.");
			else
				inContacts.get(nickname).push(user);
		}
		else
			throw new NotFoundException("Unable to find the user with the inserted nickname.");		
	}

	@Override
	public void removeInContactList(String nickname, String user) throws RemoteException,
			NullPointerException, NotFoundException, MalformedURLException, NotBoundException {
		if(nickname==null || user==null)
			throw new NullPointerException("You've inserted null data.");
		else if(isUser(nickname) && isUser(user)) {
			if(!inContacts.get(nickname).contains(user))
				throw new NotFoundException("You haven't allowed the user with the inserted nickname yet.");
			else {
				registry = (UserAgentIF) Naming.lookup(statusMap.get(nickname).getRmi());
				registry.updateFriendList(statusMap.get(user), "in");
				if(statusMap.get(user).getStatus().equals("ONLINE")) {
					registry = (UserAgentIF) Naming.lookup(statusMap.get(user).getRmi());
					registry.updateFriendList(statusMap.get(nickname), "in");
				}
				inContacts.get(nickname).remove(user);
				outContacts.get(nickname).remove(user);
				outContacts.get(user).remove(nickname);
				outContactsForUA.get(user).remove(nickname);
				outContactsForUA.get(nickname).remove(user);
			}
		}
		else
			throw new NotFoundException("Unable to find the user with the inserted nickname.");
	}

	/**<i>method addOutContactList</i>:
	 * viene costruita una lista per gli aggiornamenti di stato (per ogni user tale lista contiene gli utenti 
	 * che lo hanno aggiunto come amico);
	 * la spiegazione dettagliata delle strutture dati si trova nella parte introduttiva
	 */
	@Override
	public void addOutContactList(String nickname, String user) throws RemoteException,
			NullPointerException, DuplicateException, NotFoundException, PermissionException, MalformedURLException, NotBoundException {
		if(nickname==null || user==null)
			throw new NullPointerException("You've inserted null data.");
		else if(isUser(nickname) && isUser(user) && !nickname.equals(user)) {
			if(inContacts.get(user).contains(nickname)) {
				if(outContacts.get(user).contains(nickname))
					throw new DuplicateException("You've already friended the user with the inserted nickname.");
				else {
					registry = (UserAgentIF) Naming.lookup(statusMap.get(nickname).getRmi());
					registry.updateFriendList(statusMap.get(user), "out");
					registry.statusChange(statusMap.get(user));
					outContacts.get(user).push(nickname);
					outContactsForUA.get(nickname).push(statusMap.get(user));
				}
			}
			else
				throw new PermissionException("You are not allowed to friend the user with the inserted nickname.");
		}
		else
			throw new NotFoundException("Unable to find the user with the inserted nickname.");		
		
	}

	@Override
	public void removeOutContactList(String nickname, String user) throws RemoteException,
			NullPointerException, NotFoundException, MalformedURLException, NotBoundException {
		if(nickname==null || user==null)
			throw new NullPointerException("You've inserted null data.");
		else if(isUser(nickname) && isUser(user)) {
			if(!outContacts.get(user).contains(nickname))
				throw new NotFoundException("You haven't friended the user with the inserted nickname yet.");
			else {
				registry = (UserAgentIF) Naming.lookup(statusMap.get(nickname).getRmi());
				registry.updateFriendList(statusMap.get(user), "out");
				outContacts.get(user).remove(nickname);
				outContactsForUA.get(nickname).remove(user);
			}
		}
		else
			throw new NotFoundException("Unable to find the user with the inserted nickname.");		
	}

	//For debug only
	@Override
	public String getMyStatus(String nickname) throws RemoteException,
			NullPointerException, NotFoundException {
		if(nickname==null)
			throw new NullPointerException();
		else if(!isUser(nickname))
			throw new NotFoundException();
		else
			return statusMap.get(nickname).getStatus();
	}

	@Override
	public LinkedList<String> getMyAllowed(String nickname)
			throws RemoteException, NullPointerException, NotFoundException {
		if(nickname==null)
			throw new NullPointerException();
		else if(!isUser(nickname))
			throw new NotFoundException();
		else
			return inContacts.get(nickname);
	}

	@Override
	public LinkedList<String> getMyFriends(String nickname)
			throws RemoteException, NullPointerException, NotFoundException {
		if(nickname==null)
			throw new NullPointerException();
		else if(!isUser(nickname))
			throw new NotFoundException();
		else
			return outContacts.get(nickname);
	}

	@Override
	public Map<String, User> getStatus() throws RemoteException {
		return new ConcurrentHashMap<String, User>(statusMap);
	}
	
	@Override
	public Map<String, LinkedList<String>> getAllowed() throws RemoteException {
		return new ConcurrentHashMap<String, LinkedList<String>>(inContacts);
	}
	
	@Override
	public Map<String, LinkedList<String>> getFriends() throws RemoteException {
		return new ConcurrentHashMap<String, LinkedList<String>>(outContacts);
	}
	
}
