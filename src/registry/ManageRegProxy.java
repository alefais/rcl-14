/*
 * @author Alessandra Fais
 */
package registry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

import proxy.ProxyStatus;

/**<i>class ManageRegProxy</i>:
 * è un thread che esegue parallelamente alle funzioni della registry;
 * il suo compito è mantenere aperta una connessione UDP in attesa di 
 * registrazioni di nuovi proxy
 * 
 * @author Alessandra Fais
 */
public class ManageRegProxy implements Runnable {
	LinkedList<ProxyStatus> proxylist = null;
	
	public ManageRegProxy(LinkedList<ProxyStatus> proxylist) {
		this.proxylist = proxylist;
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
		DatagramSocket sock = null;
		DatagramPacket packet = null;
		byte[] buf = null;
		String s = null;
		String[] data = null;
		boolean listen = true;
		
		sock = new DatagramSocket(57984);
		
		System.out.println("Thread for proxies registration is active on 172.241.0.1, port 57984");		
		
		while(listen) {
			buf = new byte[1024];
			packet = new DatagramPacket(buf, buf.length);
			sock.receive(packet);
			s = new String(packet.getData()).trim();
			int proxyport = packet.getPort();
			System.out.println(s);
			
			data = s.split(":");

			synchronized(proxylist) {
				proxylist.addLast(new ProxyStatus(Integer.parseInt(data[1]), proxyport, data[0], 0));
			}
			data = null;
		}		
		sock.close();
	}
}
