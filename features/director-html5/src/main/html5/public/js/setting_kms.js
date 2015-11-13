function saveKMSSetting() {
	var self = this;
	self.data = {};
	self.data.kms_endpoint_url = $('#KMS_IP').val();
	self.data.kms_login_basic_username = $('#KMS_USERNAME').val();
//	self.data.kms_login_basic_password = $('#KMS_PASSWORD').val();
	self.data.kms_tls_policy_certificate_sha1 = $('#KMS_RSAKEY').val();
	$.ajax({
		type : 'POST',
		url : '/v1/setting/kms/updateproperties',
		data : JSON.stringify(self.data),
		contentType : "application/json",
		dataType : "json",
		success : function (data) {
			$('#KMS_IP').val(data.kms_endpoint_url);
			$('#KMS_USERNAME').val(data.kms_login_basic_username);
//			$('#KMS_PASSWORD').val(data.kms_login_basic_password);
			$('#KMS_RSAKEY').val(data.kms_tls_policy_certificate_sha1);
		}
	});
}

function fetchKMSSetting() {
	$.ajax({
		type : 'GET',
		url : '/v1/setting/kms/getproperties',
		contentType : "application/json",
		success : function (data) {
			$('#KMS_IP').val(data.kms_endpoint_url);
			$('#KMS_USERNAME').val(data.kms_login_basic_username);
//			$('#KMS_PASSWORD').val(data.kms_login_basic_password);
			$('#KMS_RSAKEY').val(data.kms_tls_policy_certificate_sha1);
		}
	});
}
