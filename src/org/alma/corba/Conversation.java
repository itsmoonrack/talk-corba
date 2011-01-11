package org.alma.corba;

public class Conversation {

	String sonIOR;
	String monIOR;
	Short convNum;
	
	
	public Conversation(String sonIOR, String monIOR, Short numConv) {
		super();
		this.sonIOR = sonIOR;
		this.monIOR = monIOR;
		this.convNum = numConv;
	}

	public String getSonIOR() {
		return sonIOR;
	}

	public String getMonIOR() {
		return monIOR;
	}

	public void setSonIOR(String sonIOR) {
		this.sonIOR = sonIOR;
	}

	public void setMonIOR(String monIOR) {
		this.monIOR = monIOR;
	}

	public Short getConvNum() {
		return convNum;
	}

	public void setConvNum(Short convNum) {
		this.convNum = convNum;
	}

	
}
