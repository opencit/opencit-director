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
	
	
	self.data.mtwilson_password = $.trim(self.data.mtwilson_password);
	self.data.mtwilson_api_password = $.trim(self.data.mtwilson_api_password);
	
	if(self.data.mtwilson_api_password == '') {
		alert('Please provide valid api password');
		return false;
	}
	if (self.data.mtwilson_password == '' ){
		alert('Please provide valid password');
		return false;
	} 
	$.ajax({
		type : 'POST',
		url : '/v1/setting/mtwilson',
		data : JSON.stringify(self.data),
		contentType : "application/json",
		success : function (data) {
			if (data.indexOf("Error: ") >= 0){
				alert(data);
				return false;
			}

			$("#mtw_config_error").html("<font color=\"green\">Configuration saved</font>");
		}
	});
}

function fetchMtwilsonSetting() {
	$("#mtw_config_error").html("");
	$.ajax({
		type : 'GET',
		url : '/v1/setting/mtwilson',
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

function validateMTWSetting() {
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
	
	
	self.data.mtwilson_password = $.trim(self.data.mtwilson_password);
	self.data.mtwilson_api_password = $.trim(self.data.mtwilson_api_password);
	
	if(self.data.mtwilson_api_password == '') {
		alert('Please provide valid api password');
		return false;
	}
	if (self.data.mtwilson_password == '' ){
		alert('Please provide valid password');
		return false;
	} 
	$('#validateMTWBtn').prop('disabled', true);
	$.ajax({
		type : 'POST',
		data : JSON.stringify(self.data),
		url : '/v1/setting/mtwilson/validate',
		contentType : "application/json",
		dataType : "json",
		success : function (data) {
		$('#validateMTWBtn').prop('disabled', false);
		if (data.status == "success") {
				$("#mtw_config_error").html("<font color=\"green\">Valid configuration</font>");
			} else if (data.status == "Error" ) {		
	                        $("#mtw_config_error").html(data.error);
			} else if (xhr.status != 200 ) {
                                $("#mtw_config_error").html("Invalid configuration");
                        }		
		}, 
		error : function(jqXHR, textStatus, errorThrown) {
			$('#validateMTWBtn').prop('disabled', false);
			$("#mtw_config_error").html("Cannot validate Attestation Service configuration");
                }
	});
}

