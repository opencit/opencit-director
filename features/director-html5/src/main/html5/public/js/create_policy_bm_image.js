var image_policies = new Array();
$.ajax({
	type : "GET",
	url :  "/v1/image-formats",
	contentType : "application/json",
	headers : {
		'Accept' : 'application/json'
	},
	dataType : "json",
	success : function(data, status, xhr) {

		imageFormats = data.image_formats;

		var option = "";
		for ( var i = 0; i < imageFormats.length; i++) {
			option += '<option value="' + imageFormats[i] + '">'
					+ imageFormats[i] + '</option>';

		}
		$('#image_format').append(option);

	}
});

$(document).ready(function() {
	fetchBMImageLaunchPolicies();
});

function CreateBMImageMetaData(data) {

	this.imageid = current_image_id;
	this.image_name = current_image_name;
	this.display_name = current_display_name;
}


function CreateBMImageViewModel() {
	var self = this;
	$("input[name=isEncryptedBM]").val(false);
	$("#display_name_bm").val(current_display_name);
	$('input[name=launch_control_policy]:checked').val("MeasureOnly");

	self.createBMImageMetaData = new CreateBMImageMetaData({});

	self.createBMImage = function(loginFormElement) {

		self.createBMImageMetaData.launch_control_policy = "MeasureOnly";
		self.createBMImageMetaData.encrypted = false;
		self.createBMImageMetaData.display_name = $('#display_name_bm').val();
		current_display_name = $('#display_name_bm').val();
		$.ajax({
			type : "POST",
			url : "/v1/trustpoliciesmetadata",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.createBMImageMetaData), // $("#loginForm").serialize(),
			success : function(data, status, xhr) {
				if (data.status == "Error") {
					$('#error_modal_body_bm_image_2').text(data.details);
					$("#error_modal_bm_image_2").modal({
						backdrop : "static"
					});
						$('body').removeClass("modal-open");
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
							$('#error_modal_body_bm_image_2').text(data.details);
							$("#error_modal_bm_image_2").modal({
								backdrop : "static"
							});
							$('body').removeClass("modal-open");
							return;
						}
						nextButtonImagesBM();
					}
				});
			}
		});

	}
};

function fetchBMImageLaunchPolicies() {
	$.ajax({
		type : "GET",
		url : "/v1/" + current_image_id + "/trustpolicymetadata",
		dataType : "json",
		success : function(data, status, xhr) {
			$("#display_name_bm").val(data.display_name);
			current_display_name = data.display_name;
			image_policies = data.image_launch_policies;
			addRadios(image_policies);
			mainViewModel.createBMImageViewModel = new CreateBMImageViewModel();
			ko.applyBindings(mainViewModel,document.getElementById("create_policy_bm_image_content_step_1"));
		}
	);
};

function addRadios(arr) {

	var temp = "";
	for ( var i = 0; i < arr.length; i++) {

		temp = temp
				+ '<label class="radio-inline"><input type="radio" name="launch_control_policy" value="'
				+ arr[i].key + '">' + arr[i].display_name + '</label>';

	}

	$('#launch_control_policy').html(temp);
};
