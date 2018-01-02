/*
 * @author Alessandra Fais
 */
package proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import useragent.Message;

/**<i>class ComFromUAThread</i>:
 * ha il compito di ricevere messaggi provenienti da utenti loggati al sistema e 
 * destinati a uno degli utenti attualmente OFFLINE gestiti dal suddetto proxy;
 * una volta ricevuti, i messaggi saranno salvati in una HashMap dal proxy
 * stesso mantenendo l'associazione con l'utente destinatario.
 * 
 * @author Alessandra Fais
 */
public class ComFromUAThread implements Runnable {
	ProxyStatus ps = null;
	
	public ComFromUAThread(ProxyStatus ps) {
		this.ps = ps;
	}
	
	public void run() {
		try {
			execute();
		}
		catch(ParseException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void execute() throws IOException, ParseException {
		DatagramSocket sock = null;
		DatagramPacket packet = null;
		byte[] buf = null;
		String s = null;
		boolean listen = true;
		JSONObject obj = null;
		Message msg = null;
	
		sock = new DatagramSocket(ps.getUaport());

		System.out.println("Connection opened on "+ps.getIpaddress()+", port "+ps.getUaport()+": listening...");
		
		while(listen) {
			buf = new byte[1024];
			packet = new DatagramPacket(buf, buf.length);
			sock.receive(packet);
			
			s = new String(buf).trim();
			obj = (JSONObject) new JSONParser().parse(s);
			msg = new Message(obj);
			
			System.out.println("Received message:\n"+msg);
			
			ps.addMsgToCache(msg.getReceiver(), msg);
			
			System.out.println("\nMessages in cache are ");
			for(JSONArray t:ps.getUsersMessages())
				for(Object t1:t)
					System.out.println(new Message((JSONObject)t1));
		}
		
		sock.close();
	}
}
