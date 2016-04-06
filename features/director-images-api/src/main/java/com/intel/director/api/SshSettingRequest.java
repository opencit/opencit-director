package com.intel.director.api;

import java.util.ArrayList;
import java.util.List;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.constants.Constants;


public class SshSettingRequest extends AuditFields {
	String ip_address;
	String username;
	String password;
	String name;
	String id;
	String key;
	String image_id;
	String partition;
	String host_type;
	///String policy_name;

	/*public String getPolicy_name() {
		return policy_name;
	}

	public void setPolicy_name(String policy_name) {
		this.policy_name = policy_name;
	}*/

	public SshSettingRequest() {

	}

	public String getIpAddress() {
		return ip_address;
	}

	public void setIpAddress(String ipAddress) {
		this.ip_address = ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public String getImage_id() {
		return image_id;
	}

	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	
	
	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}
	
	

	public String getHost_type() {
		return host_type;
	}

	public void setHost_type(String host_type) {
		this.host_type = host_type;
	}

	public SshSettingResponse validate(String operation) {
		String NAME_REGEX = "[a-zA-Z0-9,;.@ _-]+";
		String FQDN_RFC_ALL="^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)*[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?$";
		String  IPADDR_FQDN_RFC_ALL = "(?:" + RegexPatterns.IPADDRESS + "|" + FQDN_RFC_ALL + ")";
		
		SshSettingResponse sshResponse = new SshSettingResponse();
		if (!ValidationUtil.isValidWithRegex(getIpAddress(),IPADDR_FQDN_RFC_ALL)) {
			sshResponse.setError("No host provided or host is in incorrect format");
		} else if (!ValidationUtil.isValidWithRegex(getUsername(),NAME_REGEX)) {
			sshResponse.setError("No username provided or username is not in correct format");
		} else if (!ValidationUtil.isValidWithRegex(getPassword(),RegexPatterns.PASSWORD)) {
			sshResponse.setError("No password provided or password is in incorrect format");
		}
		
		List<String> supported_host_types = new ArrayList<String>();
		supported_host_types.add(Constants.HOST_TYPE_LINUX);
		supported_host_types.add(Constants.HOST_TYPE_WINDOWS);
		
		if (getHost_type() == null || !supported_host_types.contains(getHost_type())) {
			sshResponse
					.setError("No Host Type is provided or Host Type is in incorrect format");
		}
		
		if ("update".equals(operation)) {
			if (!ValidationUtil.isValidWithRegex(getImage_id(),RegexPatterns.UUID)) {
				sshResponse.setError("No image id provided or image id is not in uuid format");
			}
		}

		return sshResponse;
	}

}
