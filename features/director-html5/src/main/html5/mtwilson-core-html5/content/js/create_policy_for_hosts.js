var imageFormats = new Array();
var image_policies = new Array();
var endpoint = "/v1/images/";

function CreateBMLiveMetaData(data) {
	
	this.imageid = current_image_id;
	this.image_name = $("#image_name").val();
	this.display_name = $("#display_name_host").val();
	
}

function addhostandnext() {
	var self = this;
	$("#image_name").val(current_image_name);
	
	self.createBMLiveMetaData = new CreateBMLiveMetaData({});
	self.data = {};
	self.data.ip_address = $("#host_ip").val();
	self.data.username = $("#username_for_host").val();
	self.data.password = $("#password_for_host").val();
	self.data.name = $("#host_ip").val();
	self.data.policy_name = $("#display_name_host").val();
	self.data.key = $("#key").val();
	
	$.ajax({
		type : "POST",
		url : "/v1/setting/addHost",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		data : JSON.stringify(data), 
		success : function (data, status, xhr) {
			if (data.status == "Error" || data.ssh_setting_request.image_id == null) {
				$('#error_modal_body_bm_live_1').text(data.details);
				$("#error_modal_bm_live_1").modal({
					backdrop : "static"
				});
				$('body').removeClass("modal-open");
				return;
			}
			current_image_id = data.ssh_setting_request.image_id;
			self.createBMLiveMetaData.launch_control_policy = "MeasureOnly";
			self.createBMLiveMetaData.encrypted = false;
			self.createBMLiveMetaData.display_name = $("#display_name_host").val();
self.createBMLiveMetaData.imageid = current_image_id ;
			$.ajax({
				type : "POST",
				url : endpoint + "trustpoliciesmetadata",
				contentType : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				data : ko.toJSON(self.createBMLiveMetaData), // $("#loginForm").serialize(),
				success : function (data, status, xhr) {
					if (data.status == "Error") {
						$('#error_modal_body_bm_live_1').text(data.details);
						$("#error_modal_bm_live_1").modal({
							backdrop : "static"
						});
						$('body').removeClass("modal-open");
						return;
					}
					$.ajax({
						type : "POST",
						url : endpoint + current_image_id
						+ "/mount",
						contentType : "application/json",
						headers : {
							'Accept' : 'application/json'
						},
						data : ko.toJSON(self.createBMLiveMetaData), // $("#loginForm").serialize(),
						success : function (data, status, xhr) {
							if (data.status == "Error") {
								$('#error_modal_body_bm_live_1')
								.text(data.details);
								$("#error_modal_bm_live_1").modal({
									backdrop : "static"
								});
								$('body').removeClass("modal-open");
								return;
							}
							$.ajax({
								type : "GET",
								url : endpoint + current_image_id + "/importpolicytemplate",
								success : function (data) {
									if (data.status == "Error") {
										$('#error_modal_body_bm_live_1').text(data.details);
										$("#error_modal_bm_live_1")
										.modal({
											backdrop : "static"
										});
										$('body').removeClass("modal-open");
										return;
									}
									nextButtonLiveBM();
								}
							});
						}
					});
				}
			});
		}
	});
}