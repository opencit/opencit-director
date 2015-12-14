var imageFormats = new Array();
var image_policies = new Array();

$.ajax({
	type : "GET",
	url : "/v1/trust-policy-drafts/?imageId=" + current_image_id + "&imageArchive=false",
	contentType : "application/json",
	headers : {
		'Accept' : 'application/json'
	},
	dataType : "json",
	success : function (data, status, xhr) {
		showImageLaunchPolicies(data);
	}
});

function EditImageMetaData(data) {

	this.image_id = current_image_id;
	this.image_name = current_image_name;
	this.display_name = current_display_name;

}

function EditImageViewModel(data) {
	var self = this;

	self.editImageMetaData = new EditImageMetaData(data);

	self.editImage = function (loginFormElement) {
		$("#editVMPolicyNext").prop('disabled', true);
		console.log(current_image_id);
		self.editImageMetaData.launch_control_policy = $(
				'input[name=launch_control_policy]:checked').val();
		// /
		// self.editImageMetaData.asset_tag_policy=$('input[name=asset_tag_policy]:checked').val();
		self.editImageMetaData.encrypted = $('input[name=isEncrypted]').is(
				':checked')
			self.editImageMetaData.display_name = $('#display_name').val();
		current_display_name = $('#display_name').val();

		$.ajax({
			type : "POST",
			url : "/v1/trust-policy-drafts",
			// accept: "application/json",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.editImageMetaData), // $("#loginForm").serialize(),
			success : function (data, status, xhr) {

				if (data.status == "Error") {
					$('#for_mount_edit_vm').hide();
					$('#default_edit_vm').show();
					$('#error_modal_body_edit_vm_1').text(data.details);
					$("#error_modal_edit_vm_1").modal({
						backdrop : "static"
					});
					$("#editVMPolicyNext").prop('disabled', false);
					return;
				}
				current_trust_policy_draft_id = data.id;
				var mountimage = {
					"id" : current_image_id
				}
				$.ajax({
					type : "POST",
					url : "/v1/rpc/mount-image",
					// accept: "application/json",
					contentType : "application/json",
					headers : {
						'Accept' : 'application/json'
					},
					data : JSON.stringify(mountimage), // $("#loginForm").serialize(),
					success : function (data, status, xhr) {
						$("#editVMPolicyNext").prop('disabled', false);
						if (data.status == "Error") {
							$('#for_mount_edit_vm').show();
							$('#default_edit_vm').hide();
							$('#error_modal_body_edit_vm_1').text(data.details);
							$("#error_modal_edit_vm_1").modal({
								backdrop : "static"
							});
							return;
						}
						nextButton();
					}
				});
				// /nextButton();
			}
		});

	}

};

function showImageLaunchPolicies(policydata) {
	current_display_name = policydata.display_name;
	$("#display_name").val(current_display_name);
	$.ajax({
		type : "GET",
		url : "/v1/image-launch-policies?deploymentType=VM",
		dataType : "json",
		success : function (data, status, xhr) {
			image_policies = data.image_launch_policies;
			addRadios(image_policies);
			$("input[name=launch_control_policy][value='" + policydata.launch_control_policy + "']").attr('checked', 'checked');

			if (policydata.encrypted == true) {
				$("input[name=isEncrypted]").prop('checked', 'true');
			}
			mainViewModel.editImageViewModel = new EditImageViewModel(
					policydata);

			ko.applyBindings(mainViewModel, document
				.getElementById("edit_policy_content_step_1"));
		}
	});

};

function addRadios(arr) {

	var temp = "";
	for (var i = 0; i < arr.length; i++) {
		if(arr[i].name == 'encrypted'){
			continue;
		}
		temp = temp
			 + '<label class="radio-inline"><input type="radio" name="launch_control_policy" value="'
			 + arr[i].name + '" >' + arr[i].value + '</label>';
	}

	$('#launch_control_policy').html(temp);
	$.ajax({
		type : 'GET',
		url : '/v1/setting/kms/getproperties',
		contentType : "application/json",
		success : function (data) {

			if (data.kms_endpoint_url == "" || data.kms_login_basic_username == "" || data.kms_tls_policy_certificate_sha1 == "" ||
				data.kms_endpoint_url == null || data.kms_login_basic_username == null || data.kms_tls_policy_certificate_sha1 == null ||
				data.kms_endpoint_url == undefined || data.kms_login_basic_username == undefined || data.kms_tls_policy_certificate_sha1 == undefined) {
				$('#encryptRow').hide();
			} else {
				$('#encryptRow').show();
			}

		}
	});
};
