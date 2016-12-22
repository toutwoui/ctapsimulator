package net.stenuit.xavier.hostsimulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import net.stenuit.xavier.hostsimulator.protocol.ParserException;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapMessage;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapParser;
import net.stenuit.xavier.tools.Converter;
import net.stenuit.xavier.tools.Log;

public class ServiceRequest implements Runnable {
	private Socket socket;
	private static final int SO_TIMEOUT=100; // how many milliseconds waiting for data on socket
	private static final int MAX_MSG_LEN=2000; // Maximum message size
	
	public ServiceRequest(Socket connection) {
        this.socket = connection;
    }
	
	@Override
	public void run() {
		try
		{
			/*
			BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			String line;
			while((line=br.readLine())!=null)
			{
				Log.debug(line);
				bw.write(""+line.length());
				bw.flush();
			}*/
			socket.setSoTimeout(SO_TIMEOUT);
			InputStream is=socket.getInputStream();
			OutputStream os=socket.getOutputStream();
			
			byte[] lentbl=new byte[4];
			try
			{
				is.read(new byte[2]); // 0x0A, 0x04
				is.read(lentbl);
				int len=((lentbl[0]&0xFF)<<24)+((lentbl[1]&0xFF)<<16)+((lentbl[2]&0xFF)<<8)+((lentbl[3]&0xFF));
				if(len>MAX_MSG_LEN)throw new ParserException("Message too long : "+len+" bytes");
				
				byte[]rawMsg;
				if(len==0)
				{ // standard message don't have LEN there... 0A0400000000 is read
				  // next tag should be F0
				  if(is.read()!=0xF0) throw new ParserException("Expected F0 after 0A0400000000");
				  byte len1st=(byte)is.read(); // length or 0x8x
				  byte[] f0len;
				  int remaining;
				  if(len1st>0)
				  {
					  f0len=new byte[0]; // length is in f0len
					  remaining=(int)len1st;
				  }
				  else
				  {
					  f0len=new byte[len1st&0x3];
					  is.read(f0len); // read length
					  remaining=0;
					  for(int i=0;i<f0len.length;i++)
					  {
						  remaining=remaining<<8;
						  remaining=remaining+(int)(f0len[i]&0xff);
					  }
				  }
				  Log.debug("remaining bytes : "+remaining);
				  rawMsg=new byte[6+1/*F0*/+1/*82*/+f0len.length/*013C*/+remaining];
				  System.arraycopy(Converter.hex2bin("A00400000000F0"), 0, rawMsg, 0, 7);
				  rawMsg[7]=len1st;
				  System.arraycopy(f0len, 0, rawMsg, 8, f0len.length);
				  is.read(rawMsg, 8+f0len.length, remaining);
				}
				else
				{
					rawMsg=new byte[len+6];
					System.arraycopy(lentbl, 0, rawMsg, 2, 4);
					is.read(rawMsg,6,len);
				}
				
				CtapParser p=new CtapParser();
				CtapMessage msg=(CtapMessage)p.parse(rawMsg);
				Log.debug(msg.dump());
				// System.out.println(msg.dump());
				
				byte[] msgtypeb=Converter.hex2bin(msg.getTag("F0.E1.D0"));
				String msgtype=new String(msgtypeb,"ASCII");
				String d1=msg.getTag("F0.E1.D1");
				switch(msgtype)
				{
					case "ci":
						Log.info("Received C-INQ");
						os.write(RINQ.rinq(d1)); // answers with RINQ
						Log.debug(p.parse(RINQ.rinq()).dump());
						break;
					case "ri":
						Log.info("Received R-INQ");
						break;
					case "ct":
						Log.info("Received C-TRA");
						os.write(RTRA.rtra(d1)); // answers with RTRA
						Log.debug(p.parse(RTRA.rtra(d1)).dump());
						break;
					case "rt":
						Log.info("Received R-TRA");
						break;
					case "cb":
						Log.info("Received C-BAL");
						break;
					case "rb":
						Log.info("Received R-BAL");
						break;
					case "cp":
						Log.info("Received C-PAR");
						break;
					case "rp":
						Log.info("Received R-PAR");
						break;
					case "rX":
						Log.info("Received Rx");
						break;
					default:
						Log.error("Received unknown message "+msgtype);
				}
			}
			catch(SocketTimeoutException e)
			{
				Log.error("Timeout on socket");
			}
			catch(ParserException e)
			{
				Log.error("Problem reading message : "+e.getMessage());
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
