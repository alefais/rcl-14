/*
 * @author Alessandra Fais
 */
package useragent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import registry.RegistryIF;
import registry.User;
import exceptions.DuplicateException;
import exceptions.NotFoundException;
import exceptions.PermissionException;
import exceptions.StatusException;

/**<i>class InputThread:</i>
 * si occupa di ricevere l'input dall'utente reale, richiedendo poi
 * l'esecuzione delle azioni inserite dall'utente alla registry;
 * di sua competenza è anche la gestione dell'invio di messaggi a altri
 * utenti (UDP) e della ricezione di messaggi arretrati provenienti dal proxy (TCP)
 * 
 * @author Alessandra Fais
 */
public class InputThread implements Runnable {
	String port = null;
	String ipaddress = null;
	String rmi = null;
	UpdateThread u = null;
	ReceiveUDPThread r = null;
	private volatile boolean utEnd = false;
	private volatile boolean rudpEnd = false;
	
	/**<i>constructor InputThread</i>:
	 * oltre a indirizzo e porte per gestire le comunicazioni riceve i riferimenti ai due thread 
	 * di cui dovrà controllare il flusso di esecuzione
	 * 
	 * @param port porta usata per comunicazioni UDP
	 * @param iPaddress indirizzo IP
	 * @param rmi URI dell'interfaccia RMI fornita dall'utente
	 * @param u riferimento al thread UpdateThread
	 * @param r riferimento al thread ReceiveUDPThread
	 * @throws RemoteException
	 */
	public InputThread(String port, String iPaddress, String rmi, UpdateThread u, ReceiveUDPThread r) throws RemoteException {
		this.port = port;
		this.ipaddress = iPaddress;
		this.rmi = rmi;
		this.r = r;
		this.u = u;
	}
	
	public void utTerminated() {
		utEnd = true;
	}
	
	public void rudpTerminated() {
		rudpEnd = true;
	}
	
	public void run() {
		try {
			execute();
		}
		catch (NotBoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**<i>method sendKill</i>:
	 * invia al thread ReceiveUDPThread un messaggio contenente la stringa BYE (interpretato 
	 * come messaggio speciale che conclude la ricezione di ulteriori comunicazioni dall'esterno) 
	 * e setta le variabili booleane che comandano il flusso dei due thread UpdateThread e 
	 * ReceiveUDPThread in modo da interrompere la loro esecuzione (ciò avviene mediante chiamata
	 * ai metodi shutdown di entrambi i thread: questi permettono anche di passare il riferimento
	 * al thread corrente InputThread in modo che i due thread possano chiamare su di esso
	 * rispettivamente i metodi utTerminated e rudpTerminated per segnalare la loro terminazione)
	 */
	private void sendKill() {
		try(DatagramSocket s = new DatagramSocket()) {
			byte[] buf = new String("BYE").getBytes();
			DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByName(ipaddress), Integer.parseInt(port));
			s.send(p);
			u.shutdown(this);
			r.shutdown(this);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void execute() throws NotBoundException, IOException {
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String input = null;
		String request = null;
		String nickname = null;
		String instructions = "You have to indicate the type of operation and your nickname. Choose one of the following actions and enjoy!\n";
		String	operations = "Operations:\n+ register [R]\n+ unregister [UR]\n+ login [LI]\n+ logout [LO]\n+ send [S]\n+ allow [A]\n+ disallow [D]\n+ friend [F]\n+ unfriend [UF]\n+ exit from gossip [BYE]";
		RegistryIF registry = (RegistryIF) Naming.lookup("rmi://172.241.0.1:58643/Registry");
		boolean end = false;
		System.out.println(instructions);

		//Lettura da stdin e parsing degli argomenti
		while(!end) {
			System.out.println("Insert operation\n"+operations);
			
			if((input = stdIn.readLine())!=null)
				request = input.trim();
			
			if(!request.equalsIgnoreCase("S") && !request.equalsIgnoreCase("BYE")) {
				System.out.println("Insert nickname");
				if((input = stdIn.readLine())!=null)
					nickname = input.trim();
			}
						
			try {
				switch(request.toUpperCase()) {
					case "R":
						registry.createUser(nickname, port, ipaddress, rmi);
						System.out.println("Succesfully registred, "+nickname+"!\n");
						break;
					case "UR":
						if(UserAgent.me!=null) {
							this.sendKill();
							end = true;
							registry.removeUser(nickname);
							UserAgent.me = null;
							System.out.println("Succesfully removed, "+nickname+"!\nSession closed.");
							System.out.println("Bye bye! Thank you for using GOSSIP ;)");
							while(!utEnd && !rudpEnd) {}
							System.exit(0);
						}
						else
							System.out.println("You are not logged in!\n");
						break;
					case "LI":
						MyProxyCoordinate pc = registry.login(nickname);
						UserAgent.me = nickname;
						System.out.println("Succesfully logged in, "+UserAgent.me+"!");
						missedMessage(pc);
						break;
					case "LO":
						registry.logout(nickname, false);
						System.out.println("Succesfully logged out, "+UserAgent.me+"!\n");
						UserAgent.me = null;
						break;
					case "S":
						if(UserAgent.me!=null) {
							String receiver = null;
							receiver = sendMessage(stdIn, UserAgent.me);
							if(receiver!=null)
								System.out.println("Well done "+UserAgent.me+"! Message succesfully sent to "+receiver+".");
						}
						else
							System.out.println("You are not logged in!\n");
						break;
					case "A":
						if(UserAgent.me!=null) {
							registry.addInContactList(UserAgent.me, nickname);
							System.out.println(nickname+" succesfully allowed, "+UserAgent.me+"!\n");
						}
						else
							System.out.println("You are not logged in!\n");
						break;
					case "D":
						if(UserAgent.me!=null) {
							registry.removeInContactList(UserAgent.me, nickname);
							System.out.println(nickname+" succesfully disallowed, "+UserAgent.me+"!\n");					
						}
						else
							System.out.println("You are not logged in!\n");
						break;
					case "F":
						if(UserAgent.me!=null) {
							registry.addOutContactList(UserAgent.me, nickname);
							System.out.println(nickname+" succesfully friended, "+UserAgent.me+"!\n");
						}
						else
							System.out.println("You are not logged in!\n");
						break;
					case "UF":
						if(UserAgent.me!=null) {
							registry.removeOutContactList(UserAgent.me, nickname);
							System.out.println(nickname+" succesfully unfriended, "+UserAgent.me+"!\n");
						}
						else
							System.out.println("You are not logged in!\n");
						break;
					case "BYE":
						this.sendKill();
						end = true;
						if(UserAgent.me!=null) {
							registry.logout(UserAgent.me, false);
							UserAgent.me = null;
						}
						System.out.println("Bye bye! Thank you for using GOSSIP ;)");
						while(!utEnd && !rudpEnd) {}
						System.exit(0);
						break;
					default:
						System.out.println("No match for the operation inserted.\n");
						break;
				}
			}
			catch(NullPointerException e) {
				System.err.println("Invalid nickname: user null.\n");
				System.err.println(e.getMessage());
			}
			catch(DuplicateException e) {
				System.err.println("Invalid nickname: user already present.\n");
				System.err.println(e.getMessage());
			}
			catch(NotFoundException e) {
				System.err.println("Invalid nickname: user not found.\n");
				System.err.println(e.getMessage());
			}
			catch(PermissionException e) {
				System.err.println("Not enough permissions to add friend.\n");
				System.err.println(e.getMessage());
			}
			catch(StatusException e) {
				System.err.println("Invalid change status request.\n");
				System.err.println(e.getMessage());
			} 
			catch(NumberFormatException e) {
				System.err.println("Port conversion to int failed.\n");
			} 
			catch(ParseException e) {
				System.err.println("JSONObject parsing failed.\n");
			}
			catch(IllegalArgumentException e) {
				System.err.println("The selected friend is not available anymore.");
			}
		}
		System.out.println("wow such exit");
	}

	/**<i>method sendMessage</i>:
	 * prende in input il testo del messaggio e il destinatario (controllo sul fatto che il
	 * destinatario inserito sia nella lista di amici del mittente);
	 * successivamente viene creato il pacchetto e spedito mediante protocollo UDP
	 * (è totalmente trasparente all'utente il fatto che il messaggio venga inviato direttamente
	 * al destinatario -caso dest ONLINE- oppure al proxy per poi essere consegnato al destinatario
	 * in un secondo momento -caso dest OFFLINE)
	 * @param stdIn è un BufferedReader da standard input
	 * @param sender è il mittente del messaggio
	 * @return <b>receiver</b> è il destinatario del messaggio
	 * @throws IOException
	 * @throws NotFoundException
	 */
	private String sendMessage(BufferedReader stdIn, String sender) throws IOException, NotFoundException,
																			IllegalArgumentException {
		String input = null;
		String receiver = null;
		User myrec = null;
		String message = null;
		boolean correct = false;
		DatagramSocket udpsock = new DatagramSocket();
		DatagramPacket packet = null;
		byte[] buf = new byte[1024];
		JSONObject obj = null;
		InetAddress addr = null;
		int port = 0;

		System.out.println("Insert receiver: choose one from your friend list.");
		for(User i: UserAgent.friends)
			System.out.println("+ "+i.getName()+"["+i.getStatus()+"]");
		
		if((input = stdIn.readLine())!=null) {
			receiver = input.trim();
			
			for(User u: UserAgent.friends) {
				if(u.equals(receiver)) {
					correct = true;
					myrec = u;
		
					System.out.println("Insert message");
					if((input = stdIn.readLine())!=null)
						message = input.trim();
				}
			}
		}
		
		if(correct) {
			addr = InetAddress.getByName(myrec.getIPaddress());
			port = myrec.getPort();
	
			Message msg = new Message(sender, myrec.getName(), message);
			obj = msg.toJson();
			
			buf = obj.toString().getBytes();
			packet = new DatagramPacket(buf, buf.length, addr, port);
			udpsock.send(packet);
			
			System.out.println("Sending message to "+msg.getReceiver()+"...");
		}

		udpsock.close();
		
		if(!correct)
			throw new NotFoundException("You can send message to a friend only!");
		
		return receiver;
	}

	/**<i>method missedMessage</i>:
	 * si occupa di attivare una connessione TCP col proxy e di ricevere eventuali messaggi
	 * arrivati durante l'assenza dell'utente da GOSSIP;
	 * @param pc indirizzo IP e porta del proxy che ha preso in carico l'utente mentre esso
	 * 			   era OFFLINE e ha memorizzato i messaggi a lui inviati (se presenti)
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws ParseException
	 */
	private void missedMessage(MyProxyCoordinate pc) throws NumberFormatException, IOException, ParseException {
		if(pc.getIp()!=null && pc.getPort()!=-1) {
			Socket tcpsock = null;
			BufferedReader in = null;
			String data = null;
			JSONArray obj = null;
			Message msg = null;
			
			tcpsock = new Socket(pc.getIp(), pc.getPort());
		
			in = new BufferedReader(new InputStreamReader(tcpsock.getInputStream()));
			
			System.out.println("Retrieving updates from server...");
			
			data = in.readLine();
			obj = (JSONArray) new JSONParser().parse(data);
			for(Object o:obj) {
				msg = new Message((JSONObject) o);
				System.out.println("Sender: "+msg.getSender());
				System.out.println("Message: "+msg.getBody());
			}
				
			in.close();
			tcpsock.close();
		}
		else
			System.out.println("No messages for you!");
		System.out.println();
	}

}
