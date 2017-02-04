package net.stenuit.xavier.hostsimulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

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
				  // parse length
				  // either one-byte - if len<=127
				  // or two bytes - if len <=255
				  // or 3 bytes - if len <= 65535
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
				  // now, we now how many bytes to read, and the values of message :
				  // A00400000000F0LL[LL][LL]{bytes}
				  // So let's read the message
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
				
				// parses the input message to a CtapMessage structure
				CtapParser p=new CtapParser();
				CtapMessage msg=(CtapMessage)p.parse(rawMsg);
				Log.debug(msg.dump());
				
				
				byte[] msgtypeb=Converter.hex2bin(msg.getTag("F0.E1.D0"));
				String msgtype=new String(msgtypeb,"ASCII");
				// tags to be echoed
				String d1=msg.getTag("F0.E1.D1");
				String df1e=msg.getTag("F0.E2.F5.DF1E");
				String df20=msg.getTag("F0.E2.F5.DF20");
				String t9f1c=msg.getTag("F0.E2.F1.9F1C");
				String df68=msg.getTag("F0.E2.FA.DF68");
				
				CtapMessage retmsg;
				switch(msgtype)
				{
					case "ci":
						Log.info("Received C-INQ");
						// builds a RINQ message
						retmsg=(CtapMessage)p.parse(RINQ.rinq(d1));
						// adapts message with mandatory echoed tags
						if(df1e!=null)retmsg.setTag("F0.E2.F5.DF1E", df1e);
						if(df20!=null)retmsg.setTag("F0.E2.F5.DF20", df20);
						if(t9f1c!=null)retmsg.setTag("F0.E2.F1.9F1C", t9f1c);
						if(df68!=null)retmsg.setTag("F0.E2.FA.DF68", df68);
						// provides a random authorization code
						retmsg.setTag("F0.E2.F5.89", randomAuthCode());
						// computes checksum, and fills appropriate message
						retmsg.setTag("F0.E3.DF8153", retmsg.checksum());
						
						os.write(Converter.hex2bin(retmsg.rawDump()));
						Log.debug("Returned R-INQ: \n"+retmsg.dump());
						break;
					case "ri":
						Log.info("Received R-INQ");
						break;
					case "ct":
						Log.info("Received C-TRA");
						// builds a sandard RTRA message
						retmsg=(CtapMessage)p.parse(RTRA.rtra(d1));
						// sets the mandatory echoed tags
						if(df1e!=null)retmsg.setTag("F0.E2.F5.DF1E", df1e);
						if(df20!=null)retmsg.setTag("F0.E2.F5.DF20", df20);
						if(t9f1c!=null)retmsg.setTag("F0.E2.F1.9F1C", t9f1c);
						if(df68!=null)retmsg.setTag("F0.E2.FA.DF68", df68);
						// computes and sets checksum
						retmsg.setTag("F0.E3.DF8153", retmsg.checksum());
						os.write(Converter.hex2bin(retmsg.rawDump()));
						Log.debug("Returned R-TRA : \n"+retmsg.dump());
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

	/* REturns a 6 digit random number ASCII code (0x30,0x34,0x32,....)
	 * - used for random authcode
	 */
	private String randomAuthCode() {
		int r=new Random().nextInt(1000000);
		
		String ret="";
		for(int i=0;i<6;i++)
		{
			ret=ret+"3"+r%10;
			r=r/10;
		}
		return ret;
	}



}
