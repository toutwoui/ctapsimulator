package net.stenuit.xavier.hostsimulator;

import java.io.FileOutputStream;

/**
 * Just writing down a CTAP message
 * @author xs
 *
 */
public class RawMsgWriter {
	
	public static void main(String[] args) throws Exception
	{
		FileOutputStream os;
		os=new FileOutputStream("/tmp/cinq.raw");
		os.write(CINQ.cinq());
		os.flush();
		os.close();
		
		os=new FileOutputStream("/tmp/ctra.raw");
		os.write(CTRA.ctra());
		os.flush();
		os.close();
		
	}
}
