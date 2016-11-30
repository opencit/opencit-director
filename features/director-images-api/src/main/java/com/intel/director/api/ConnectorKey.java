package com.intel.director.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConnectorKey {

	public ConnectorKey(int seqNo, String key) {
		super();
		this.seqNo = seqNo;
		this.key = key;
	}

	@JsonIgnore
	private int seqNo;
	private String key;
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	

}
