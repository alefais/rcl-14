/*
 * @author Alessandra Fais
 */
package useragent;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import registry.User;

/**Interfaccia UserIF:
 * definisce tutte le operazioni che vengono offerte in RMI
 * lato user; queste operazioni sono a tutti gli effetti delle
 * funzioni di callback che permettono agli utenti di ottenere
 * aggiornamenti sullo stato corrente dei loro amici
 * (StatusChange) e sulla lista dei loro amici (UpdateFriendList
 * e UploadFriendList) direttamente dalla registry
 * 
 * @author Alessandra Fais
 */
public interface UserAgentIF extends Remote {

	public void statusChange(User friend) throws RemoteException, NullPointerException;
	public void updateFriendList(User friend, String src) throws RemoteException, NullPointerException;
	public void uploadFriendList(List<User> currentfriends) throws RemoteException, NullPointerException;
	
}
