/*
*	@author Alessandra Fais
*/

package registry;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;

import useragent.MyProxyCoordinate;
import exceptions.DuplicateException;
import exceptions.NotFoundException;
import exceptions.PermissionException;
import exceptions.StatusException;

/**<i>interface RegistryIF</i>:
 * contiene le intestazioni delle funzioni (metodi) messe a disposizione di qualsiasi
 * cliente (in questo caso qualsiasi user) si connetta in RMI con la registry;
 * tali metodi permettono la registrazione al servizio GOSSIP, login/logout, la creazione
 * di proprie liste di amici e la gestione dei permessi nei confronti di altri utenti 
 * (è possibile bloccare un utente per esempio).
 *
 * <ul>
 * <li>NOTE:</li>
 * i metodi getter forniti hanno esclusivo scopo di debug: permettono di vedere
 * friend, allowed e stato di un singolo utente (basta specificare il nickname)
 * e/o di tutti gli utenti registrati correntemente al servizio GOSSIP.
 * </ul>
 *    
 * @author Alessandra fais
 */
public interface RegistryIF extends Remote {
	
	public void createUser(String nickname, String port, String IPaddress, String rmi) 
			throws RemoteException, NullPointerException, DuplicateException, NotBoundException;
	
	public void removeUser(String nickname) throws RemoteException, 
			NullPointerException, NotFoundException;
	
	public boolean isUser(String nickname) throws RemoteException, 
			NullPointerException;
	
	public MyProxyCoordinate login(String nickname) throws RemoteException, 
			NullPointerException, NotFoundException, StatusException, MalformedURLException, NotBoundException;
	
	public void logout(String nickname, boolean removeuser) throws RemoteException, 
			NullPointerException, NotFoundException, StatusException, MalformedURLException, NotBoundException;
	
	public void addInContactList(String nickname, String user) throws RemoteException, 
			NullPointerException, DuplicateException, NotFoundException;
	
	public void removeInContactList(String nickname, String user) throws RemoteException, 
			NullPointerException, NotFoundException, MalformedURLException, NotBoundException;
	
	public void addOutContactList(String nickname, String user) throws RemoteException, 
			NullPointerException, DuplicateException, NotFoundException, PermissionException, 
			MalformedURLException, NotBoundException;
	
	public void removeOutContactList(String nickname, String user) throws RemoteException, 
			NullPointerException, NotFoundException, PermissionException, MalformedURLException,
			NotBoundException;
	
	//For debug only
	public String getMyStatus(String nickname) throws RemoteException,
			NullPointerException, NotFoundException;

	public LinkedList<String> getMyAllowed(String nickname) throws RemoteException,
			NullPointerException, NotFoundException;
	
	public LinkedList<String> getMyFriends(String nickname) throws RemoteException,
			NullPointerException, NotFoundException;

	public Map<String, User> getStatus() throws RemoteException;
	
	public Map<String, LinkedList<String>> getAllowed() throws RemoteException;
	
	public Map<String, LinkedList<String>> getFriends() throws RemoteException;
	
}