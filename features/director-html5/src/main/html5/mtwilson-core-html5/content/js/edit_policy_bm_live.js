endpoint = "/v1/images/";
var imageFormats = new Array();
var image_policies = new Array();
showBMLiveMetaData();
function EditBMLiveMetaData() {
	
	this.imageid = current_image_id;
	this.image_name = current_image_name;
	this.display_name = $("#display_name_host_edit").val();
	
}

function editandNext() {
	var self = this;
	$("#host_name_live_edit").val(current_image_name);
	self.editBMLiveMetaData = new EditBMLiveMetaData();
	self.data = {};
	self.data.ip_address = $("#host_ip_edit").val();
	self.data.username = $("#username_for_host_edit").val();
	self.data.password = $("#password_for_host_edit").val();
	self.data.name = $("#host_ip_edit").val();
	self.data.policy_name = $("#display_name_host_edit").val();
	self.data.key = $("#key_edit").val();
	self.data.image_id = current_image_id;
	
	$.ajax({
		type : "PUT",
		url : "/v1/setting/updatehost",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		data : JSON.stringify(data), 
		success : function (data, status, xhr) {
						if (data.status == "Error") {
				$('#error_modal_body_bm_live_1').text(data.details);
				$("#error_modal_bm_live_1").modal({
					backdrop : "static"
				});
				$('body').removeClass("modal-open");
				return;
			}
			;
			self.editBMLiveMetaData.launch_control_policy = "MeasureOnly";
			self.editBMLiveMetaData.isEncrypted = false;
			self.editBMLiveMetaData.display_name = $("#display_name_host_edit").val();

			$.ajax({
				type : "POST",
				url : endpoint + "trustpoliciesmetadata",
				contentType : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				data : ko.toJSON(self.editBMLiveMetaData),
				success : function (data, status, xhr) {
					if (data.status == "Error") {
						$('#error_modal_body_edit_bm_live_1').text(data.details);
						$("#error_modal_edit_bm_live_1").modal({
							backdrop : "static"
						});
						return;
					}
					$.ajax({
						type : "POST",
						url : endpoint + current_image_id + "/mount",
						contentType : "application/json",
						headers : {
							'Accept' : 'application/json'
						},
						data : ko.toJSON(self.editBMLiveMetaData), // $("#loginForm").serialize(),
						success : function (data, status, xhr) {
							if (data.status == "Error") {
								$('#error_modal_body_edit_bm_live_1').text(data.details);
								$("#error_modal_edit_bm_live_1").modal({
									backdrop : "static"
								});
								return;
							}
							nextButtonLiveBM();
						}
					});
				}
			});
			
		}
		
	});
};

function showBMLiveMetaData() {
	
	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/getbmlivemetadata",
		dataType : "json",
		success : function (data, status, xhr) {
			current_display_name = data.ssh_setting_request.display_name;
			console.log(current_display_name);
			$("#display_name_host_edit").val(data.ssh_setting_request.policy_name);
						$("#host_ip_edit").val(data.ssh_setting_request.ip_address);
			$("#username_for_host_edit").val(data.ssh_setting_request.username);
	}
});

};
