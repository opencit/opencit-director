endpoint = "/v1/images/";
var imageFormats = new Array();
var image_policies = new Array();
showBMImageLaunchPolicies();
function EditBMLiveMetaData() {

	this.imageid = current_image_id;
	this.image_name = current_image_name;
	this.display_name = $("#display_name_host_edit").val();

}

function EditBMLiveViewModel() {
	var self = this;
	$("#host_name_live_edit").val(current_image_name);
	self.editBMLiveMetaData = new EditBMLiveMetaData();

	self.editBMLive = function(loginFormElement) {

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
			success : function(data, status, xhr) {
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
					success : function(data, status, xhr) {
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

};

function showBMImageLaunchPolicies() {

	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/trustpolicymetadata",
		dataType : "json",
		success : function(data, status, xhr) {
			current_display_name = data.display_name;
			console.log(current_display_name);
			$("#display_name_host_edit").val(current_display_name);
			mainViewModel.editBMLiveViewModel = new EditBMLiveViewModel();
			ko.applyBindings(mainViewModel, document.getElementById("edit_policy_bm_live_content_step_1"));
		}
	});

};