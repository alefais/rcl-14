/*
 * @author Alessandra Fais
 */
package useragent;

import java.io.Serializable;

/**<i>class MyProxyCoordinate</i>:
 * contiene le informazioni che permettono a un utente di stabilire
 * una connessione col proprio proxy (indirizzo IP e porta) al fine
 * di ricevere eventuali messaggi a lui destinati e custoditi dal proxy
 * (TCP) <ul>
 * <li><b>ip</b> è l'indirizzo IP del proxy </li>
 * <li><b>port</b> è la porta usata dal proxy per la connessione TCP </li>
 * </ul>
 * 
 * @author Alessandra Fais
 */
public class MyProxyCoordinate implements Serializable {
	private static final long serialVersionUID = 1L;
	private String ip = null;
	private int port = 0;
	
	public MyProxyCoordinate(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}