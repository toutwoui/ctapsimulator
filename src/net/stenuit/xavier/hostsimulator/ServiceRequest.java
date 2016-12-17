package net.stenuit.xavier.hostsimulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceRequest implements Runnable {
	private Socket socket;
	private Logger logger;
	
	public ServiceRequest(Socket connection) {
        this.socket = connection;
        this.logger=Logger.getLogger("TCPServer");
    }
	
	@Override
	public void run() {
		try
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			String line;
			while((line=br.readLine())!=null)
			{
				logger.log(Level.INFO, line);
				bw.write(""+line.length());
				bw.flush();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			// the client closed
		}
		try{socket.close();}catch(IOException ioe){};
	}



}
