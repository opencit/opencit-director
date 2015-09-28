var imageFormats = new Array();
var image_policies = new Array();

/*
 * $.ajax({ type: "GET", url: endpoint+"image-formats", // accept:
 * "application/json", contentType: "application/json", headers: {'Accept':
 * 'application/json'}, dataType: "json", success: function(data, status, xhr) {
 * 
 * 
 * imageFormats=data.image_formats;
 * 
 * var option=""; for (var i=0;i<imageFormats.length;i++){ option += '<option
 * value="'+ imageFormats[i] + '">' + imageFormats[i] + '</option>';
 *  } $('#image_format').append(option);
 * 
 * 
 * 
 *  } });
 */
$(document).ready(function() {
	fetchImageLaunchPolicies();
});

function CreateImageMetaData(data) {

	this.imageid = currentCreatePolicyImageId;

	this.image_name = $("#image_name").val();
	// / this.isEncrypted=ko.observable(false);
	/* this.selected_image_format= ko.observable(); */

}

function CreateImageViewModel() {
	var self = this;

	$("input[name=isEncrypted]").val(false);
	$("#image_name").val(currentCreatePolicyImageName);

	self.createImageMetaData = new CreateImageMetaData({});

	self.createImage = function(loginFormElement) {

		self.createImageMetaData.launch_control_policy = $(
				'input[name=launch_control_policy]:checked').val();
		self.createImageMetaData.asset_tag_policy = $(
				'input[name=asset_tag_policy]:checked').val();
		self.createImageMetaData.isEncrypted = $(
				'input[name=isEncrypted]:checked').val();

		$.ajax({
			type : "POST",
			url : endpoint + "trustpoliciesmetadata",
			// accept: "application/json",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.createImageMetaData), // $("#loginForm").serialize(),
			success : function(data, status, xhr) {
				displayNextCreatePolicyPage();
			}
		});

	}

};

function fetchImageLaunchPolicies() {
	$.ajax({
		type : "GET",
		url : endpoint + "image-launch-policies",
		// accept: "application/json",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		dataType : "json",
		success : function(data, status, xhr) {

			image_policies = data.image_launch_policies;
			addRadios(image_policies);
			mainViewModel.createImageViewModel = new CreateImageViewModel();
			// /] ko.cleanNode(mainViewModel);

			ko.applyBindings(mainViewModel, document
					.getElementById("create_policy_content_step_1"));
		}
	});

};

function addRadios(arr) {

	var temp = "";
	for (var i = 0; i < arr.length; i++) {

		temp = temp
				+ '<label class="radio-inline"><input type="radio" name="launch_control_policy" value="'
				+ arr[i] + '">' + arr[i] + '</label>';

	}

	$('#launch_control_policy').html(temp);
};

