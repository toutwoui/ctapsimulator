package net.stenuit.xavier.hostsimulator;

import net.stenuit.xavier.tools.Converter;

public class RTRA {

	private static final String msg="0A0400000000"
			+ "F059"
			+ "E107D0027274D10110E234F10B9F1C083233303930343532F51CDF1E0400000002DF1F0400000020DF200101DF270720161222091001FA07DF680400006100E318DF8153149E6AA156DF6A1502DC12DE7F78F1A2691E04E518";
	
	public static byte[] rtra()
	{
		return Converter.hex2bin(msg);
	}
	public static synchronized byte[] rtra(String D1)
	{
		byte[] ret=Converter.hex2bin(msg);
		byte[] d1=Converter.hex2bin(D1);
		ret[16]=d1[0];
		return ret;
	}
}
