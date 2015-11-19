var imageStores = new Array();

var option;

displayBMImageStore();
function displayBMImageStore() {
	
	$
			.ajax({
				type : "GET",
				url : "v1/setting/sshsettings/getdata",
				contentType : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				dataType : "json",
				success : function(data, status, xhr) {

					hosts = data;
					console.log("******************");
					console.log(hosts.length);
					console.log("******************");
					option = "<option value='0'>Select</option>";
					
					for ( var i = 0; i < hosts.length; i++) {
						option += '<option value="' + hosts[i].ip_address+ '">'
								+ hosts[i].ip_address+ '</option>';

					}
					$('#pust_to_host_combo').append(option);
				}
		});
}

function pushPolicy(data) {
			$.ajax({
			type : "GET",
			url : endpoint + current_image_id + "/pushPolicy?host=" + $('#pust_to_host_combo').val(),
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			success : function(data, status, xhr) {
				current_image_action_id = ""; 
				$("#redirect").modal({backdrop: "static"});
			}
		});
}

function createPolicyDraftFromPolicy() {
	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/recreatedraft?action_id="+current_image_action_id,
		success : function(data, status, xhr) {
			console.log("Draft Created Successfully");
			current_image_action_id = "";
		}
	});
	$.ajax({
		type : "POST",
		url : endpoint + current_image_id + "/mount",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		data : ko.toJSON(self.createImageMetaData), // $("#loginForm").serialize(),
		success : function(data, status, xhr) {
			backButtonImagesBM();
		}
	});

	
}