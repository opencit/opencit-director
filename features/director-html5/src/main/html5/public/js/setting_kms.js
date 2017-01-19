function saveKMSSetting() {
	var self = this;
	self.data = {};
	self.data.kms_endpoint_url = $('#KMS_IP').val();
	self.data.kms_login_basic_username = $('#KMS_USERNAME').val();
	self.data.kms_login_basic_password = $('#KMS_PASSWORD').val();
	self.data.kms_tls_policy_certificate_sha256 = $('#KMS_RSAKEY').val();
	$.ajax({
		type : 'POST',
		url : '/v1/setting/kms',
		data : JSON.stringify(self.data),
		contentType : "application/json",
		dataType : "json",
		success : function(data) {
			$("#kms_config_error").html(
					"<font color=\"green\">Configuration saved</font>");
		}
	});
}

function validateKMSSetting() {
	var self = this;
	self.data = {};
	self.data.url = $('#KMS_IP').val();
	self.data.user = $('#KMS_USERNAME').val();
	self.data.password = $('#KMS_PASSWORD').val();
	self.data.sha256 = $('#KMS_RSAKEY').val();

	$.ajax({
		type : 'POST',
		url : '/v1/setting/kms/validate',
		data : JSON.stringify(self.data),
		contentType : "application/json",
		dataType : "json",
		success : function(data) {
			if (data.status == "success") {
				$("#kms_config_error").html(
						"<font color=\"green\">Valid configuration</font>");
			} else if (data.status == "Error") {
				$("#kms_config_error").html(data.error);
			}

		},
		error : function(jqXHR, textStatus, errorThrown) {
			$("#kms_config_error").html("Cannot validate KMS configuration");
		}
	});
}

function fetchKMSSetting() {
	$("#kms_config_error").html("");
	$.ajax({
		type : 'GET',
		url : '/v1/setting/kms',
		contentType : "application/json",
		success : function(data) {
			$('#KMS_IP').val(data.kms_endpoint_url);
			$('#KMS_USERNAME').val(data.kms_login_basic_username);
			// $('#KMS_PASSWORD').val(data.kms_login_basic_password);
			$('#KMS_RSAKEY').val(data.kms_tls_policy_certificate_sha256);
		}
	});
}
