endpoint = "/v1";
var imageFormats = new Array();
var image_policies = new Array();

$.ajax({
	type : "GET",
	url : "/v1/trust-policy-drafts/?imageId="+ current_image_id + "&imageArchive=false",
	contentType : "application/json",
	headers : {
		'Accept' : 'application/json'
	},
	dataType : "json",
	success : function(data, status, xhr) {
		showBMImageLaunchPolicies(data);
	}
});

function EditBMImageMetaData(data) {

	this.imageid = current_image_id;
	this.image_name = current_image_name;
	this.display_name = current_display_name;

}

function EditBMImageViewModel(data) {
	var self = this;

	self.editBMImageMetaData = new EditBMImageMetaData(data);

	self.editBMImage = function(loginFormElement) {

		self.editBMImageMetaData.launch_control_policy = "MeasureOnly";
		self.editBMImageMetaData.encrypted = false;
		self.editBMImageMetaData.display_name = $('#display_name_edit_bm')
				.val();
		current_display_name = $('#display_name_edit_bm').val();
		$.ajax({
			type : "POST",
			url : endpoint + "trustpoliciesmetadata",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.editBMImageMetaData),
			success : function(data, status, xhr) {
				if (data.status == "Error") {
					$('#error_modal_body_edit_bm_image_1').text(data.details);
					$("#error_modal_edit_bm_image_1").modal({
						backdrop : "static"
					});
					return;
				}
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
					success : function(data, status, xhr) {
						if (data.error) {
							$('#error_modal_body_edit_bm_image_1').text(data.error);
							$("#error_modal_edit_bm_image_1").modal({
								backdrop : "static"
							});
							return;
						}
						nextButtonImagesBM();
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
			$("#display_name_edit_bm").val(current_display_name);

			image_policies = data.image_launch_policies;
			addRadios(image_policies);
			$(
					"input[name=launch_control_policy][value='"
							+ policydata.launch_control_policy + "']").attr(
					'checked', 'checked');

			// / $("input[name=asset_tag_policy][value='Trust
			// Only']").attr('checked', 'checked');
			if (policydata.isEncrypted == true) {
				$("input[name=isEncrypted]").prop('checked', 'true');
			}
			mainViewModel.editBMImageViewModel = new EditBMImageViewModel(
					policydata);
			// /] ko.cleanNode(mainViewModel);

			ko.applyBindings(mainViewModel, document
					.getElementById("edit_policy_bm_image_content_step_1"));
		}
	});

};

function addRadios(arr) {

	var temp = "";
	for ( var i = 0; i < arr.length; i++) {

		temp = temp
				+ '<label class="radio-inline"><input type="radio" name="launch_control_policy" value="'
				+ arr[i].key + '" >' + arr[i].value + '</label>';

	}

	$('#launch_control_policy').html(temp);
};

