package net.stenuit.xavier.hostsimulator;

import net.stenuit.xavier.tools.Converter;

public class RINQ {
	private static final String msg="0A0400000000"
			+ "F08197"
			+ "E107D0027269D10109E272F1129F1C083233303930343532DF110400800000F532C5020001DF1E0400000002DF1F0400000020DF2001015F2A0209788906363035303132DF270720161222090622DF2E020000F7048A023030FA07DF680400006100FB19EF17DF5F0220099F160F333230303330303030320000000000E318DF815314D2A2B3744F4D08D4A0B5F4460BDBFD09F8BFB927";
	
	public static byte[] rinq()
	{
		return Converter.hex2bin(msg);
	}
	public static synchronized byte[] rinq(String D1)
	{
		byte[] ret=Converter.hex2bin(msg);
		byte[] d1=Converter.hex2bin(D1);
		ret[17]=d1[0];
		return ret;
	}
}
