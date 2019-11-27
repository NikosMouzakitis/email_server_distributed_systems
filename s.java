import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.HashMap;


class ServerThread extends Thread
{
	Socket s;
	public static ArrayList ulist, ilist, plist;
	private static Object mutex;
	public static int connected = 0;
	public int delindx;
	public static HashMap<String, String> mails;	

	public ServerThread(Socket s)
	{
		this.s = s;
		System.out.println("Accepted Connection");
	}
	
	//prints the current file list for debug.
	public static void printUlist()
	{
		for(int i = 0; i < ulist.size(); i++)
			System.out.println("IP: "+ ilist.get(i)+ " Port: "+ plist.get(i) + " user: "+ ulist.get(i));
	}

	public void run()
	{

		int incomingPort, passSignin = 0;
		String incomingIP;	

		incomingPort = s.getPort();
		incomingIP = s.getInetAddress().toString();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter pw = new PrintWriter(s.getOutputStream(), true);

			String un = "";

			while (true)
			{
				String message = br.readLine();
				// client abnormal termination.
				if(message == null) {
					System.out.println("Client terminates connection");
					break;	
				}
		
				// checking if signed up.
					
				if( passSignin == 0) {

					char[] tmp = message.toCharArray();

					if( message.startsWith("Signin ")) {

						synchronized(mutex) {
							String toAddShared = message.substring(7);
							ulist.add(toAddShared);	
							un = toAddShared;
							ilist.add(incomingIP);	
							plist.add(incomingPort);	
							connected += 1;
							String okrp = "Ok\n";
							pw.println(okrp);
							pw.flush();
							passSignin = 1;
						}

						
					} else if( message.startsWith("mail ") && message.length() > 5) {
						/// Reception on Server-to-Server.
						//// message from another server to forward an email to one of it's users.
						String toUser, fromUser, tt, plaintext;
						tt = message.substring(5);
						toUser = tt.split(" ")[0];
						fromUser = tt.split("-")[1].split(" ")[0];
						//mail user from-user text
						plaintext = tt.split("-")[1].substring(fromUser.length()+1);

						if(ulist.contains(toUser)) {

							pw.println("Ok");
							pw.flush();	
							// do the appropriate handlings HashMap etc.

							if(mails.containsKey(toUser))
								mails.put(toUser, mails.get(toUser) + fromUser + ":" + plaintext + " ");	
							else
								mails.put(toUser, fromUser + ":" + plaintext);
						} else {
							System.out.println("Not having recipient as a client: ");
							continue;
						}	
					}	

				}else{	       
				
					if( message.startsWith("mail ") ) { // to contact another email Server.(Server- to Server).Received from connected client.
						/// Send on Server-to-Server.
						String serport, serip, userto, plaintext;
						String tt;
						tt = message.substring(5);
						userto = tt.split("@")[0];
						serip = tt.split("@")[1].split(":")[0];	
						serport = tt.split("@")[1].split(":")[1].split(" ")[0];
						plaintext = tt.substring(userto.length() + serip.length() + serport.length() + 3);
						String cmdts = "mail " + userto+" from-" + un + " " + plaintext;	
						Socket pps = new Socket(serip, Integer.parseInt(serport));
						PrintWriter pwpp = new PrintWriter(pps.getOutputStream());
						BufferedReader brpp = new BufferedReader(new InputStreamReader(pps.getInputStream()));
						pwpp.println(cmdts);
						pwpp.flush();
						
						pw.println("ok");
						pw.flush();

						String respSS = brpp.readLine();

						if(respSS.equals("Ok"))
							System.out.println("Mail Server transfer succeed.");

						try {
							pps.close();
						} catch(IOException e) {
							System.out.println("Failed to close S-S socket");
						}


					} else if( message.startsWith("Signout")) {

						synchronized(mutex) {
							//get index of the particular client to remove from all lists.	
							for(int i = 0; i < connected; i++)
								if( (int) plist.get(i) == incomingPort)
									delindx = i;	
							
							plist.remove(delindx);
							ilist.remove(delindx);
							ulist.remove(delindx);
							connected -= 1;				
						}					

					} else if( message.equals("mail") ) { // from client to read emails
						String mailreply;						
						if( mails.containsKey(un) == false)
							mailreply = "no messages";
						else	
							mailreply = mails.get(un);	
						pw.println(mailreply);
						pw.flush();
					}
				}
				printUlist();	
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[])
	{
		
		ulist = new ArrayList();
		ilist = new ArrayList();
		plist = new ArrayList();
		mails = new HashMap<String, String>();
		int serverport;	
	
		serverport = Integer.parseInt(args[0]);	
			
		ServerSocket serverSocket = null;
		mutex = new Object();

		try {

			serverSocket = new ServerSocket(serverport);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		while (true)
		{
			Socket clientSocket = null;

			try {
				//returns a new socket(client socket) and is passed to the thread as an argument..
				clientSocket = serverSocket.accept();
				new ServerThread(clientSocket).start();

			} catch (IOException e)	{
				e.printStackTrace();
			}
		}
	}
}
