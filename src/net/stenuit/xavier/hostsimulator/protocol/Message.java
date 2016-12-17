package net.stenuit.xavier.hostsimulator.protocol;

/**
 * Represents a parsed message
 * Elements can be read from this message with getter functions
 * (either precise getter functions, or something like getTag())
 * 
 * When building the message, the flag "isComplete" must remain false.
 * After message has been built, the flag must be set to one by calling the "complete()" method
 * When message is complete, it should not be writable
 * 
 * @author xs
 *
 */
public abstract class Message {
	protected boolean isComplete=false;
	long parsedTimeInMillis=0L; // when the last tag was parsed

	public long getParsedTimeInMillis() {
		return parsedTimeInMillis;
	}
	
	public void complete()
	{
		isComplete=true;
		parsedTimeInMillis=System.currentTimeMillis();
	}
	public abstract String dump();
}
