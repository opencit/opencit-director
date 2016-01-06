package com.intel.director.api;

public class SshSettingResponse extends GenericResponse {
	SshSettingRequest sshSettingRequest;
	public SshSettingRequest getSshSettingRequest() {
		return sshSettingRequest;
	}

	public void setSshSettingRequest(SshSettingRequest sshSettingRequest) {
		this.sshSettingRequest = sshSettingRequest;
	}
}
