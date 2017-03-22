package net.stenuit.xavier.hostsimulator;

import net.stenuit.xavier.tools.Converter;

public class RX {
private static final String msg="0A0400000000F028E10AD0027258D10100D20100E214F10B9F1C083030303030303030F505DF2D020000E304CA020000";
	
	public static synchronized byte[] rx()
	{
		return Converter.hex2bin(msg);
	}
	public static synchronized byte[] rx(String host_inc_code)
	{
		byte[] ret=Converter.hex2bin(msg);
		byte[] d1=Converter.hex2bin(host_inc_code);
		ret[40]=d1[0];
		ret[41]=d1[1];
		return ret;
	}
}
