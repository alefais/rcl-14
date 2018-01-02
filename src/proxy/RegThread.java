/*
 * @author Alessandra Fais
 */
package proxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import useragent.Message;

/**<i>class RegThread</i>:
 * il suo compito è registrare il proxy sulla registry (mediante connessione UDP) e
 * occuparsi della gestione degli utenti del proxy (aggiungere un nuovo utente OFFLINE e/o
 * rimuovere un proprio utente tornato ONLINE, recapitandogli prima eventuali messaggi
 * che gli sono stati scritti durante la sua assenza).
 * Le notifiche sui cambi di stato di un utente sono ricevute dalla registry (UDP).
 * I messaggi in cache verranno recapitati al legittimo destinatario mediante uso di
 * protocollo TCP.
 * 
 * @author Alessandra Fais
 */
public class RegThread implements Runnable {
	ProxyStatus ps = null;
	
	public RegThread(ProxyStatus ps) {
		this.ps = ps;
	}
	
	public void run() {
		try {
			execute();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	
	private void execute() throws IOException {
		DatagramSocket udpsock = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[1024];
		boolean listen = true;
		String[] data = null;
		InetAddress addr = InetAddress.getByName("172.241.0.1");
		
		//Presentation proxy to registry
		udpsock = new DatagramSocket(57994);
		
		String s = String.format("%s:%d", ps.getIpaddress(), ps.getUaport());
		buf = s.getBytes();
		packet = new DatagramPacket(buf, buf.length, addr, 57984);
		udpsock.send(packet);
		udpsock.close();
		System.out.println("Registred to registry!");
		
		//Update proxy data: logout/login users
		udpsock = new DatagramSocket(ps.getUaport()+1);
		
		System.out.println("Waiting for updates from registry on "+ps.getIpaddress()+", port "+(ps.getUaport()+1)+".");
		
		while(listen) {
			s = null;
			buf = new byte[1024];
			packet = new DatagramPacket(buf, buf.length);
			udpsock.receive(packet);
			s = new String(buf).trim();
			data = s.split(":");

			switch (data[0]) {
				case "addUA":
					ps.setUacounter(Integer.parseInt(data[1]));
					ps.addUAtoProxy(data[2]);
					break;
				case "remUA":
					sendMessageToUA(data[1], data[2]);
					break;
			}
			
			System.out.println("Proxy is managing "+ps.getUacounter()+" users.");
			System.out.print("Users in cache are ");
			for(String t:ps.getUsersInCache())
				System.out.print(t+" ");
			System.out.println("\nMessages in cache are ");
			for(JSONArray t:ps.getUsersMessages())
				for(Object t1:t)
					System.out.println(new Message((JSONObject)t1));
		}
		udpsock.close();
	}
	
	/**<i>method send messageToUA</i>:
	 * apre una connessione TCP appena va a buon fine l'accept, ossia un
	 * utente richiede di connettersi (e si collega con successo)
	 * @param nickname utente che ha fatto login (destinatario dei messaggi)
	 * @param port porta su cui aprire la connessione
	 * @throws NumberFormatException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void sendMessageToUA(String nickname, String port) throws NumberFormatException, UnknownHostException, IOException {
		ServerSocket tcpsock = null;
		Socket conn = null;
		PrintWriter out = null;
		
		tcpsock = new ServerSocket(Integer.parseInt(port));
		conn = tcpsock.accept();
		System.out.println(port);

		out = new PrintWriter(conn.getOutputStream(), true);

		out.println(ps.getMessageList(nickname));
		
		out.close();
		tcpsock.close();
		
		ps.setUacounter((ps.getUacounter())-1);
		ps.remUAfromProxy(nickname);
		
		System.out.println("Messages sent to online user "+nickname);
	}
}
