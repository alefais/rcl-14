/*
 * @author Alessandra Fais
 */
package useragent;

import org.json.simple.JSONObject;

/**<i>class Message</i>:
 * la sua funzione è incapsulare un messaggio contenendo le
 * informazioni sul mittente e sul destinatario, oltre che
 * il testo (body) del messaggio stesso;
 * il formato dei messaggi viene così unificato (e reso
 * velocemente convertibile in JSON) facilitando gli scambi
 * 
 * @author Alessandra Fais
 */
public class Message {

	private String sender = null;
	private String receiver = null;
	private String body = null;
	
	public Message(String sender, String receiver, String body) {
		this.sender = sender;
		this.receiver = receiver;
		this.body = body;
	}
	
	public Message(JSONObject obj) {
		sender = (String) obj.get("Sender");
		receiver = (String) obj.get("Receiver");
		body = (String) obj.get("Message");
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		obj.put("Sender", sender);
		obj.put("Receiver", receiver);
		obj.put("Message", body);
		return obj;
	}
	
	public String toString() {
		String s = null;
		s = String.format("Sender: %s%nReceiver: %s%nMessage: %s%n", 
				sender, receiver, body);
		return s;
	}

	public String getSender() {
		return sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getBody() {
		return body;
	}
	
}
