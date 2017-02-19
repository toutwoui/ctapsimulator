package net.stenuit.xavier.hostsimulator.props;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class Merchant {
	private String merchantId;
	private ArrayList<Terminal> terminal;
	
	@XmlElement(required=true)
	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	@XmlElement(required=false)
	public ArrayList<Terminal> getTerminal() {
		return terminal;
	}

	public void setTerminal(ArrayList<Terminal> terminal) {
		this.terminal = terminal;
	}
	
}
