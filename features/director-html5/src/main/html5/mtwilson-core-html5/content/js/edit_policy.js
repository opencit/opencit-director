var imageFormats = new Array();
var image_policies = new Array();

$.ajax({
	type : "GET",
	url : endpoint + current_image_id + "/getpolicymetadataforimage",
	// accept: "application/json",
	contentType : "application/json",
	headers : {
		'Accept' : 'application/json'
	},
	dataType : "json",
	success : function(data, status, xhr) {
		// / alert("getmetadata data::"+data);
		showImageLaunchPolicies(data);

		/*
		 * imageFormats=data.image_formats;
		 * 
		 * var option=""; for (var i=0;i<imageFormats.length;i++){ option += '<option
		 * value="'+ imageFormats[i] + '">' + imageFormats[i] + '</option>'; }
		 * $('#image_format').append(option);
		 */

	}
});

function EditImageMetaData(data) {

	this.imageid = current_image_id;
	this.image_name = current_image_name;
	this.display_name = current_display_name;

}

function EditImageViewModel(data) {
	var self = this;

	self.editImageMetaData = new EditImageMetaData(data);

	self.editImage = function(loginFormElement) {
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
			url : endpoint + "trustpoliciesmetadata",
			// accept: "application/json",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.editImageMetaData), // $("#loginForm").serialize(),
			success : function(data, status, xhr) {
				if (data.status == "Error") {
					$('#error_modal_body_edit_vm_1').text(data.details);
					$("#error_modal_edit_vm_1").modal({
						backdrop : "static"
					});
					return;
				}
				$.ajax({
					type : "POST",
					url : endpoint + current_image_id + "/mount",
					// accept: "application/json",
					contentType : "application/json",
					headers : {
						'Accept' : 'application/json'
					},
					data : ko.toJSON(self.createImageMetaData), // $("#loginForm").serialize(),
					success : function(data, status, xhr) {
						if (data.status == "Error") {
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

	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/trustpolicymetadata",
		dataType : "json",
		success : function(data, status, xhr) {

			current_display_name = data.display_name;
			console.log(current_display_name);
			$("#display_name").val(current_display_name);

			image_policies = data.image_launch_policies;
			addRadios(image_policies);
			$(
					"input[name=launch_control_policy][value='"
							+ policydata.launch_control_policy + "']").attr(
					'checked', 'checked');

			// / $("input[name=asset_tag_policy][value='Trust
			// Only']").attr('checked', 'checked');
			if (policydata.encrypted == true) {
				$("input[name=isEncrypted]").prop('checked', 'true');
			}
			mainViewModel.editImageViewModel = new EditImageViewModel(
					policydata);
			// /] ko.cleanNode(mainViewModel);

			ko.applyBindings(mainViewModel, document
					.getElementById("edit_policy_content_step_1"));
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
	$.ajax({
		type : 'GET',
		url : '/v1/setting/kms/getproperties',
		contentType : "application/json",
		success : function(data) {
			
			if(data.kms_endpoint_url == "" || data.kms_login_basic_username == "" || data.kms_tls_policy_certificate_sha1 == "" ||
				data.kms_endpoint_url == null || data.kms_login_basic_username == null || data.kms_tls_policy_certificate_sha1 == null ||
				data.kms_endpoint_url == undefined || data.kms_login_basic_username == undefined || data.kms_tls_policy_certificate_sha1 == undefined )
			{
				$('#encryptRow').hide();
			}
		else
			{
				$('#encryptRow').show();
			}
			
		}
	});
};

