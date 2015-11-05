var imageFormats = new Array();
var image_policies = new Array();
var endpoint = "/v1/images/";


fetchImageLaunchPolicies();


function CreateBMLiveMetaData(data) {
	
	this.imageid = current_image_id;
	this.image_name = $("#image_name").val();
	this.display_name = $("#display_name_host").val();
	
}

function CreateBMLiveViewModel() {
	var self = this;
	$("#image_name").val(current_image_name);
	
	self.createBMLiveMetaData = new CreateBMLiveMetaData({});
	
	self.createBMLive = function (loginFormElement) {
		
		self.createBMLiveMetaData.launch_control_policy = "MeasureOnly";
		self.createBMLiveMetaData.encrypted = false;
		self.createBMLiveMetaData.display_name = $("#display_name_host").val();
		
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
	
};

function fetchImageLaunchPolicies() {
	console.log("HERE 1");
	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/trustpolicymetadata",
		dataType : "json",
		success : function(data, status, xhr) {
			$("#display_name_host").val(data.display_name);
			current_display_name = data.display_name;
			mainViewModel.createBMLiveViewModel = new CreateBMLiveViewModel();
			
			ko.applyBindings(mainViewModel, document
			.getElementById("create_policy_for_hosts_content_step_1"));
		}
	});

}