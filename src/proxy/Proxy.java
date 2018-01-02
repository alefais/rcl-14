/*
 * @author Alessandra Fais
 */
package proxy;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**<i>class Proxy</i>:
 * permette di attivare un nuovo proxy sul sistema, mantenendo
 * le sue informazioni in una struttura di tipo ProxyStatus
 * 
 * @author Alessandra Fais
 */
public class Proxy {
	
	public static void main(String[] args) {
		Executor pool = Executors.newCachedThreadPool();
		String instructions = "You have to start Proxy in the following mode: java -jar proxy <port> <IPaddress>";
		
		if(args.length<2) {
			System.out.println(instructions);
			return;
		}
		
		int uaport = Integer.parseInt(args[0].trim());
		String ipaddress = args[1].trim();
		
		ProxyStatus ps = new ProxyStatus(uaport, 0, ipaddress, 0);
		
		System.out.println("Proxy activated on "+ps.getIpaddress()+", port "+ps.getUaport()+".");
		System.out.println("It manages "+ps.getUacounter()+" users.");
		
		pool.execute(new RegThread(ps));
		pool.execute(new ComFromUAThread(ps));
	}
}
