package net.stenuit.xavier.hostsimulator.props;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// You shall generate XSD for properties with the following command :
// /opt/jdk1.8.0_101/bin/schemagen -cp ../bin/ net/stenuit/xavier/hostsimulator/props/HostSimulator.java
// @XmlAccessorType(XmlAccessType.FIELD) --> xsd would be based on fields instead of getters
@XmlRootElement(name="HostSimulator")
public class HostSimulator {
	
	private ArrayList<Merchant> merchants;
	private ArrayList<Card>cards;
	private String updateFrequency;
	private String connectionData;
	private String currencyProfile;
	private String allowedTerminalMode;
	private String optionalDataElementSupport;
	private String allowedServices;
	private String allowedCardEntryModes;
	private String cardBrandRiskManagement;
	private String cardholderVerificationModes;
	private String pinLengthType;
	
	@XmlElement(required=true,name="merchant")
	public ArrayList<Merchant> getMerchants() {
		return merchants;
	}
	public void setMerchants(ArrayList<Merchant> merchants) {
		this.merchants = merchants;
	}	
	
	@XmlElement(required=true,name="card")
	public ArrayList<Card> getCards() {
		return cards;
	}
	public void setCards(ArrayList<Card> cards) {
		this.cards = cards;
	}
	
	@XmlElement(required=false,name="updateFrequency")
	public String getUpdateFrequency() {
		return updateFrequency;
	}
	public void setUpdateFrequency(String updateFrequency) {
		this.updateFrequency = updateFrequency;
	}
	@XmlElement(required=false,name="connectionData")
	public String getConnectionData() {
		return connectionData;
	}
	public void setConnectionData(String connectionData) {
		this.connectionData = connectionData;
	}
	
	@XmlElement(required=false,name="currencyProfile")
	public String getCurrencyProfile() {
		return currencyProfile;
	}
	public void setCurrencyProfile(String currencyProfile) {
		this.currencyProfile = currencyProfile;
	}
	
	@XmlElement(required=true,name="allowedTerminalMode")
	public String getAllowedTerminalMode() {
		return allowedTerminalMode;
	}
	public void setAllowedTerminalMode(String allowedTerminalMode) {
		this.allowedTerminalMode = allowedTerminalMode;
	}
	
	@XmlElement(required=true,name="optionalDataElementSupport")
	public String getOptionalDataElementSupport() {
		return optionalDataElementSupport;
	}
	public void setOptionalDataElementSupport(String optionalDataElementSupport) {
		this.optionalDataElementSupport = optionalDataElementSupport;
	}
	@XmlElement(required=true)
	public String getAllowedServices() {
		return allowedServices;
	}
	public void setAllowedServices(String allowedServices) {
		this.allowedServices = allowedServices;
	}
	@XmlElement(required=true)
	public String getAllowedCardEntryModes() {
		return allowedCardEntryModes;
	}
	
	public void setAllowedCardEntryModes(String allowedCardEntryModes) {
		this.allowedCardEntryModes = allowedCardEntryModes;
	}
	@XmlElement(required=true)
	public String getCardBrandRiskManagement() {
		return cardBrandRiskManagement;
	}
	public void setCardBrandRiskManagement(String cardBrandRiskManagement) {
		this.cardBrandRiskManagement = cardBrandRiskManagement;
	}
	@XmlElement(required=true)
	public String getCardholderVerificationModes() {
		return cardholderVerificationModes;
	}
	public void setCardholderVerificationModes(String cardholderVerificationModes) {
		this.cardholderVerificationModes = cardholderVerificationModes;
	}
	@XmlElement(required=true)
	public String getPinLengthType() {
		return pinLengthType;
	}
	public void setPinLengthType(String pinLengthType) {
		this.pinLengthType = pinLengthType;
	}
	
}
