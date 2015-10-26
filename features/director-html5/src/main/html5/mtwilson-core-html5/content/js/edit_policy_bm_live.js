endpoint = "/v1/images/";
var imageFormats = new Array();
var image_policies = new Array();

$.ajax({
	type : "GET",
	url : endpoint + current_image_id + "/getpolicymetadataforimage",
	contentType : "application/json",
	headers : {
		'Accept' : 'application/json'
	},
	dataType : "json",
	success : function(data, status, xhr) {
		showBMImageLaunchPolicies(data);

	}
});

function EditBMLiveMetaData(data) {

	this.imageid = current_image_id;
	this.image_name = current_image_name;
	this.display_name = current_display_name;

}

function EditBMLiveViewModel(data) {
	var self = this;

	self.editBMLiveMetaData = new EditBMLiveMetaData(data);

	self.editBMLive = function(loginFormElement) {

		self.editBMLiveMetaData.launch_control_policy = "MeasureOnly";
		self.editBMLiveMetaData.isEncrypted = false;
		self.editBMLiveMetaData.display_name = current_display_name;
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
					$('#error_modal_body').text(data.details);
					$("#error_modal").modal({
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
							$('#error_modal_body').text(data.details);
							$("#error_modal").modal({
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

function showBMImageLaunchPolicies(policydata) {

	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/trustpolicymetadata",
		dataType : "json",
		success : function(data, status, xhr) {

			current_display_name = data.display_name;
			console.log(current_display_name);
			$("#display_name_live_edit").val(current_display_name);

			image_policies = data.image_launch_policies;
			// addRadios(image_policies);
			$(
					"input[name=launch_control_policy][value='"
							+ policydata.launch_control_policy + "']").attr(
					'checked', 'checked');

			// / $("input[name=asset_tag_policy][value='Trust
			// Only']").attr('checked', 'checked');
			if (policydata.isEncrypted == true) {
				$("input[name=isEncrypted]").prop('checked', 'true');
			}
			mainViewModel.editBMLiveViewModel = new EditBMLiveViewModel(
					policydata);
			// /] ko.cleanNode(mainViewModel);

			ko.applyBindings(mainViewModel, document
					.getElementById("edit_policy_bm_live_content_step_1"));
		}
	});

};

function addRadios(arr) {

	var temp = "";
	for ( var i = 0; i < arr.length; i++) {

		temp = temp
				+ '<label class="radio-inline"><input type="radio" name="launch_control_policy" value="'
				+ arr[i] + '" >' + arr[i] + '</label>';

	}

	$('#launch_control_policy').html(temp);
};

