/*
 * @author Alessandra Fais
 */
package registry;

import java.io.Serializable;

/**<i>class User</i>:
 * è stata pensata per contenere tutte le informazioni che caratterizzano un utente
 * del servizio: <ul>
 * <li><b>name</b> è il nickname con cui l'utente si è registrato </li>
 * <li><b>status</b> indica se l'utente è ONLINE oppure OFFLINE </li>
 * <li><b>port</b> è la porta che l'utente usa per la comunicazione con altri utenti amici (UDP) </li>
 * <li><b>ipaddress</b> è l'indirizzo IP usato dall'utente </li>
 * <li><b>rmi</b> è la stringa che identifica la URI dell'interfaccia RMI fornita dall'utente </li>
 * <li><b>proxyip</b> è l'indirizzo IP del proxy a cui è stato assegnato il suddetto utente nel
 *           caso abbia effettuato un logout </li>
 * <li><b>proxyport</b> è la porta usata dal proxy per le connessioni verso gli utenti </li>
 * </ul>
 * 
 * @author Alessandra Fais
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name = null;
	private String status = null;
	private int port = 0;
	private String ipaddress = null;
	private String rmi = null;
	private String proxyip = null;
	private int proxyport = 0;
	
	public User(String name, String status, int port, String ipaddr, String rmi, String proxyip, int proxyport) {
		this.name = name;
		this.status = status;
		this.port = port;
		this.ipaddress = ipaddr;
		this.rmi = rmi;
		this.proxyip = proxyip;
		this.proxyport = proxyport;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof User) {
			return ((User) o).name.equals(this.name);
		}
		else if (o instanceof String) {
			return ((String) o).equals(this.name);
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getPort() {
		if(status.equals("ONLINE"))
			return port;
		else
			return proxyport;
	}

	public void setProxyport(int proxyport) {
		this.proxyport = proxyport;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIPaddress() {
		if(status.equals("ONLINE"))
			return ipaddress;
		else
			return proxyip;
	}
	
	public void setProxyIP(String proxyip) {
		this.proxyip = proxyip;
	}

	public void setIPaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	
	public String getRmi() {
		return rmi;
	}

}
