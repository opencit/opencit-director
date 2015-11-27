var imageFormats = new Array();
var image_policies = new Array();
var endpoint = "/v1/images/";

function CreateBMLiveMetaData(data) {
	
	this.imageid = current_image_id;
	this.image_name = $("#image_name").val();
	this.display_name = $("#display_name_host").val();
	
}

function show_error_in_bmlivemodal(message){
	
	$('#error_modal_body_bm_live_1').text(message);
	$("#error_modal_bm_live_1").modal({
		backdrop : "static"
	});
	$('body').removeClass("modal-open");
	
	
	
}


function addhostandnext() {
	var self = this;
	$("#image_name").val(current_image_name);
	self.createBMLiveMetaData = new CreateBMLiveMetaData({});
	self.data = {};
	
	
	
	if(!$.trim($("#display_name_host").val())==false){
		self.data.policy_name = $("#display_name_host").val();
		}else{
		show_error_in_bmlivemodal("Policy name is mandatory");
		return;
	}
	
	
	
	
	if(!$.trim($("#host_ip").val())==false){
		self.data.ip_address = $("#host_ip").val();	
		}else{
		show_error_in_bmlivemodal("Host Ip/Name is mandatory");
		return;
	}
	
	
	if(!$.trim($("#username_for_host").val())==false){
		self.data.username = $("#username_for_host").val();
		}else{
		show_error_in_bmlivemodal("Username is mandatory");
		return;
	}
	
	
	
	if(!$.trim($("#password_for_host").val())==false){
		self.data.password = $("#password_for_host").val();
		}else{
		show_error_in_bmlivemodal("Password is mandatory");
		return;
	}
	
	self.data.image_id=current_image_id;
	
	self.data.name = $("#host_ip").val();
	
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
				show_error_in_bmlivemodal(data.details);
				return;
			}
			
			current_image_id = data.ssh_setting_request.image_id;
			self.createBMLiveMetaData.launch_control_policy = "MeasureOnly";
			self.createBMLiveMetaData.encrypted = false;
			self.createBMLiveMetaData.display_name = $("#display_name_host").val();
			self.createBMLiveMetaData.imageid = current_image_id ;
			$.ajax({
				type : "POST",
				url : "/v1/trust-policy-drafts",
				contentType : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				data : ko.toJSON(self.createBMLiveMetaData), // $("#loginForm").serialize(),
				success : function (data, status, xhr) {
					if (data.status == "Error") {
						show_error_in_bmlivemodal(data.details);
						return;
					}
					current_trust_policy_draft_id=data.id;	
					var mountimage = {
						"id" : current_image_id
					}
					$.ajax({
						type : "POST",
						url : "/v1/rpc/mount-image",
						contentType : "application/json",
						headers : {
							'Accept' : 'application/json'
						},
						data : JSON.stringify(mountimage), // $("#loginForm").serialize(),
						success : function (data, status, xhr) {
							if (data.status == "Error") {
								show_error_in_bmlivemodal(data.details);
								return;
							}
							$.ajax({
								type : "POST",
								url : "/v1/rpc/policy-templates/" + current_image_id + "/apply",
								success : function (data) {
									if (data.status == "Error") {
										show_error_in_bmlivemodal(data.details);
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