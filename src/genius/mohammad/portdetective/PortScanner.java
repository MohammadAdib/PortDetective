package genius.mohammad.portdetective;

import java.io.InputStream;
import java.net.Socket;

public class PortScanner {
	int progress = 0;	 
	int currentPort = -2;
	String output = "No open ports found...";
	boolean running = true;
	boolean timedOut = false;
	PortDescriptor pd;
	int realPort;
	
	public PortScanner(InputStream stream) {		
		pd = new PortDescriptor(stream);
	}
	
	public void scan() {
		Runnable r = new Runnable() {
			public void run() {
				running = true;
				String ip = RAM.ip;
				int port1 = RAM.port1;
				int port2 = RAM.port2;
				for(int port = port1; port < port2 + 1; port++) {
					if(!timedOut) {						
						realPort = port;
						boolean open = false;
						try {
							Socket s = new Socket(ip, port);
							open = true;
							s.close();
						} catch (Exception e) { }
						if(open) {
							if(output.equals("No open ports found...")) {
								output = "\nPort " + port + " is open.\n" + pd.describePort(port)+"\n";
							}else{
								output += "\nPort " + port + " is open.\n" + pd.describePort(port)+"\n";
							}
						}
						progress = (int)(((port-port1)/(1.0*(port2-port1)) * 100.0) + 0.5);
						currentPort = port;
					}else{
						break;
					}
				}
				if(!timedOut) {
					currentPort = -1;
				}
				running = false;
			}
		};
		Thread t = new Thread(r);
		t.start();
	}	
	
	public void timeOut() {
		timedOut = true;
		running = false;
		currentPort = -2;
	}
}
