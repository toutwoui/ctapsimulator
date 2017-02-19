package net.stenuit.xavier.hostsimulator.props;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={})
public class Terminal {
	private String terminalId;
	private int lastSequenceNumber=0;
	
	@XmlElement(required=true)
	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	@XmlElement(required=false)
	public int getLastSequenceNumber() {
		return lastSequenceNumber;
	}

	public void setLastSequenceNumber(int lastSequenceNumber) {
		this.lastSequenceNumber = lastSequenceNumber;
	}
	
	
}
