/*
 * @author Alessandra Fais
 */
package useragent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**<i>class ReceiveUDPThread</i>:
 * apre una connessione UDP e sta in attesa di messaggi
 * provenienti da altri utenti (friends)
 * <ul>
 * <li><b>port</b> è la porta su cui viene aperta la connessione </li>
 * </ul>
 * 
 * @author Alessandra Fais
 */
public class ReceiveUDPThread implements Runnable {
	int port = 0;
	InputThread i = null;
	private volatile boolean end = false;

	public ReceiveUDPThread(String udpport) {
		this.port = Integer.parseInt(udpport);
	}
	
	public void shutdown(InputThread i) {
		this.i = i;
		end = true;
	}
	
	public void run() {
		try {
			execute();
		}
		catch(SocketException e) {
			System.err.println("Unable to open socket on listen.");
		}
		catch(ParseException e) {
			System.err.println("Unable to parse.");
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}

	/** <i>class execute</i>:
	 * finchè l'utente non esegue una <i>unregister</i> oppure digita <i>bye</i> allora
	 * il thread continua a mantenere aperta la connessione UDP in attesa di messaggi; 
	 * quando uno dei due eventi indicati si verifica, InputThread lo comunica settando 
	 * la variabile end a true (chiamata al metodo shutdown di ReceiveUDPThread) e 
	 * inviando un messaggio UDP apposito: a quel punto si interrompe la connessione 
	 * (si chiude il canale di ascolto per nuovi messaggi), quindi, ottenuto il riferimento
	 * al thread InputThread, si segnala la propria terminazione chiamando un suo metodo 
	 * apposito (rudpTerminated())
	 */
	private void execute() throws IOException, ParseException {
		DatagramSocket sock = null;
		DatagramPacket packet = null;
		byte[] buf = null;
		String s = null;
		JSONObject obj = null;
		Message msg = null;
		
		sock = new DatagramSocket(port);
		
		while(!end) {
			buf = new byte[1024];
			packet = new DatagramPacket(buf, buf.length);
			sock.receive(packet);
			s = new String(buf).trim();
			if(!s.equals("BYE")) {
				obj = (JSONObject) new JSONParser().parse(s);
				msg = new Message(obj);

				System.out.println("Sender: "+msg.getSender());
				System.out.println("Message: "+msg.getBody());
			}
		}
		sock.close();
		i.rudpTerminated();
	}
	
}
