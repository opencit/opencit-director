function saveMtWilsonSetting() {
	var self = this;
	self.data = {};
	self.data.mtwilson_api_url = $('#mtwilsonapiurl').val();
	self.data.mtwilson_api_username = $('#mtwilsonapiusername').val();
	self.data.mtwilson_api_password = $('#mtwilsonapipassword').val();
	self.data.mtwilson_api_tls_policy_certificate_sha1 = $('#mtwilsonapicertificate').val();
	self.data.mtwilson_server = $('#mtwilsonserverip').val();
	self.data.mtwilson_server_port = $('#mtwilsonserverport').val();
	self.data.mtwilson_username = $('#mtwilsonusername').val();
	self.data.mtwilson_password = $('#mtwilsonpassword').val();
	$.ajax({
		type : 'POST',
		url : '/v1/setting/mtwilson/updateproperties',
		data : JSON.stringify(self.data),
		contentType : "application/json",
		dataType : "json",
		success : function (data) {
			$('#mtwilsonapiurl').val(data.mtwilson_api_url);
			$('#mtwilsonapiusername').val(data.mtwilson_api_username);
			$('#mtwilsonapipassword').val(self.data.mtwilson_api_password );
			$('#mtwilsonapicertificate').val(data.mtwilson_api_tls_policy_certificate_sha1);
			$('#mtwilsonserverip').val(data.mtwilson_server);
			$('#mtwilsonserverport').val(data.mtwilson_server_port);
			$('#mtwilsonusername').val(data.mtwilson_username);
			$('#mtwilsonpassword').val(self.data.mtwilson_password);
			alert("Updated Successfully");
		}
	});
}

function fetchMtwilsonSetting() {
	$.ajax({
		type : 'GET',
		url : '/v1/setting/mtwilson/getproperties',
		contentType : "application/json",
		success : function (data) {
			$('#mtwilsonapiurl').val(data.mtwilson_api_url);
			$('#mtwilsonapiusername').val(data.mtwilson_api_username);
			$('#mtwilsonapipassword').val(data.mtwilson_api_password);
			$('#mtwilsonapicertificate').val(data.mtwilson_api_tls_policy_certificate_sha1);
			$('#mtwilsonserverip').val(data.mtwilson_server);
			$('#mtwilsonserverport').val(data.mtwilson_server_port);
			$('#mtwilsonusername').val(data.mtwilson_username);
			$('#mtwilsonpassword').val(data.mtwilson_password);
		}
	});
}
