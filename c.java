import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

// client program
public class Client
{
	
	public Client(String serverIP, int sport) {
		this.serverIP = serverIP;
		this.sport = sport;
	}

	public void initialize() {
		
		System.out.println("ConTo: "+ this.serverIP + " LclPort: " + sport);

		socket = null;
		pw = null;
		br = null;
		// the bri is for user input, br is used for sending requests throught socket.
		bri = new BufferedReader(new InputStreamReader(System.in));

		try {
			socket = new Socket(serverIP, sport);
			pw = new PrintWriter(socket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void signIN() {
		signin = "Signin "+myname;
		// sends the message over the socket.
		pw.println(signin);
		pw.flush();
		System.out.println("Signin..");

		//wait for the Ok reply.
		String okrep = "";
		try {
			okrep = br.readLine();
		} catch(IOException e) {
			e.printStackTrace();
		}

		if(okrep.equals("Ok")) {
			System.out.println("Signin success.");
		} else {
			System.out.println("Failed..");
			System.exit(-1);
		}
	}

	public void prompts() {
		System.out.println("Enter <mail> to check your emails");
		System.out.println("Enter <send> to send email");
		System.out.println("Enter <signout> to exit");
	}

	public void searchMode() {
		try{
			br.readLine();
		} catch(IOException e) {
			e.printStackTrace();
		}

		while (true)
		{
			srep = "";
			prompts();

			try {
				data = bri.readLine();       //reads line.

				//checking if this was the signout command
				if(data.equals("signout")) {
					dt2 = "signout";
					System.out.println(dt2);
					//send signout	
					pw.println(dt2);
					pw.flush();
					break;	
				}
				// code to get your email.	
				if(data.startsWith("mail") && data.length() == 4) {
					System.out.println("Check your emails");
					//send mail	
					pw.println("mail");
					pw.flush();
					
					data = br.readLine();
					
					System.out.println("---EMAILS---\n");
					System.out.println(data);
					System.out.println("------------\n");
					
				}		
				// command to send email.	
				if(data.startsWith("send") && (data.length() == 4)) {
					String receiverName;	
					String receiverPort;		
					String email;
					String cmdToServer;
					System.out.println("Enter receivers name: ");
					data = bri.readLine();       //reads line.
					receiverName = data;
					System.out.println("Enter server's port : ");
					data = bri.readLine();	
					receiverPort = data;
					System.out.println("Write email for : " + receiverName);
					data = bri.readLine();	
					email = data;
					cmdToServer = "mail "+receiverName+"@"+serverIP+":"+receiverPort+" "+email;	
					System.out.println(cmdToServer);	
					//send message
					pw.println(cmdToServer);
					pw.flush();
					String dump = br.readLine();

				}
			
			} catch (IOException e)	{
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args)
	{

		if (args.length != 3) {
			System.out.println("Error: you forgot <server IP> <port>");
			return;
		}

		serverIP = args[0];
		sport = Integer.decode(args[1]);
		myname = args[2];
		
		c = new Client(serverIP, sport);
		c.initialize();  //initializing the connection.
		c.signIN();	// signin
		c.searchMode();	//functionality of search and getting Server's replies.

	}

	private static   Client c;	
	private  static String str, data, dt2, serverIP, srep, listOfFiles, signin, myname;
	private  static int sport, ttt;
	private  Socket socket;
        public static  PrintWriter pw;
	private static String freply;
	private  BufferedReader br, bri;
	private  StringTokenizer stoken;
}
