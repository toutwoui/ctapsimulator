package net.stenuit.xavier.hostsimulator.props;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"pan","expirityDate","sequence","available","returnCode"})
public class Card {
	
	private String pan;
	/* 1220 means december 2020 */
	private String expirityDate;
	/* optional -  000 will taken if not defined */
	private String seq;
	/* Available amount, -1 means unlimited */
	private int available;
	/* Optional - if defined, transaction will be refused with this returncode */
	private int returnCode; 
	
	
	@XmlElement(required=true)
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	@XmlElement(required=true)
	public String getExpirityDate() {
		return expirityDate;
	}
	public void setExpirityDate(String expirityDate) {
		this.expirityDate = expirityDate;
	}
	@XmlElement(required=false)
	public String getSequence() {
		return seq;
	}
	public void setSequence(String sequence) {
		this.seq = sequence;
	}
	@XmlElement(required=false)
	public int getAvailable() {
		return available;
	}
	public void setAvailable(int available) {
		this.available = available;
	}
	@XmlElement(required=false)
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	  
}
