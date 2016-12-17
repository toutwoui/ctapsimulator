package net.stenuit.xavier.hostsimulator.protocol;

import java.io.InputStream;
import java.util.Properties;

/**
 * A parser converts a flow of data to a message.
 * 
 * You can build your parser with an array of bytes (if length is know)
 * or with an inputstream (length must be determined by the parser)
 * 
 * the parse method will return an instance of message
 * 
 * @author xs
 *
 */
public abstract class Parser {
	protected Properties properties;
	/**
	 * You can change the behavior of parser by putting properties
	 * @param p properties (implementation dependent)
	 */
	public void setProperties(Properties p)
	{
		this.properties=p;
	}
	
	/** parser generating a message from an array of bytes **/
	public abstract Message parse(byte[] in) throws ParserException; 
	public abstract Message parse(InputStream in) throws ParserException;
}
