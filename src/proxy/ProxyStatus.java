/*
 * @author Alessandra Fais
 */
package proxy;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import useragent.Message;

/** <i>class ProxyStatus</i>:
 * 	è stata pensata per contenere tutte le informazioni riguardanti lo stato corrente
 *  di un proxy: <ul>
 *  <li><b>uaport</b> è la porta usata per la comunicazione verso gli user </li>
 *  <li><b>proxyport</b> è la porta usata dal proxy al momento della sua registrazione alla registry </li>
 *  <li><b>ipaddress</b> è l'indirizzo IP usato dal proxy </li>
 *  <li><b>uacounter</b> è il contatore degli utenti offline correntemente gestiti dal proxy </li>
 *  <li><b>cache</b> è una HashMap privata che contiene i messaggi destinati all'utente X offline (X sarà la chiave) </li>
 *  </ul>
 *  
 *  @author Alessandra Fais
 */
public class ProxyStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	private int uaport = 0;
	private int proxyport = 0;
	private String ipaddress = null;
	private int uacounter = 0;
	private Map<String, JSONArray> cache = null;
	
	public ProxyStatus(int uaport, int proxyport, String ipaddress, int counter) {
		this.uaport = uaport;
		this.proxyport = proxyport;
		this.ipaddress = ipaddress;
		this.uacounter = counter;
		cache = new HashMap<String, JSONArray>();
	}

	public int getUacounter() {
		return uacounter;
	}

	public void setUacounter(int uacounter) {
		this.uacounter = uacounter;
	}

	public int getUaport() {
		return uaport;
	}

	public int getProxyPort() {
		return proxyport;
	}
	
	public String getIpaddress() {
		return ipaddress;
	}

	public void addUAtoProxy(String ua) {
		if(!cache.containsKey(ua))
			cache.put(ua, new JSONArray());
	}

	public void remUAfromProxy(String ua) {
		if(cache.containsKey(ua))
			cache.remove(ua);
	}
	
	@SuppressWarnings("unchecked")
	public void addMsgToCache(String ua, Message msg) {
		cache.get(ua).add(msg.toJson());
	}
	
	public Collection<JSONArray> getUsersMessages() {
		return cache.values();
	}

	public Set<String> getUsersInCache() {
		return cache.keySet();
	}
	
	public JSONArray getMessageList(String usernickname) {
		return cache.get(usernickname);
	}
}
