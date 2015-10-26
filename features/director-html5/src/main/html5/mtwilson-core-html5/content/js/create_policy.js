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
 * value="'+ imageFormats[i] + '">' + imageFormats[i] + '</option>'; }
 * $('#image_format').append(option);
 * 
 * 
 *  } });
 */

$(document).ready(function() {
	// /alert("inside create js ready function");
	fetchImageLaunchPolicies();
});

function CreateImageMetaData(data) {

	this.imageid = current_image_id;

	this.image_name = current_image_name;
	this.display_name = current_display_name;
	// / this.isEncrypted=ko.observable(false);
	/* this.selected_image_format= ko.observable(); */

}

function CreateImageViewModel() {
	var self = this;

	$("input[name=isEncrypted]").val(false);

	self.createImageMetaData = new CreateImageMetaData({});

	self.createImage = function(loginFormElement) {

		self.createImageMetaData.launch_control_policy = $(
				'input[name=launch_control_policy]:checked').val();
		// self.createImageMetaData.asset_tag_policy=$('input[name=asset_tag_policy]:checked').val();
		self.createImageMetaData.encrypted = $('input[name=isEncrypted]').is(
				':checked');

		self.createImageMetaData.display_name = $('#display_name').val();
		current_display_name = $('#display_name').val();
		console.log(self.createImageMetaData.display_name);

		$.ajax({
			type : "POST",
			url : endpoint + "trustpoliciesmetadata",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.createImageMetaData), // $("#loginForm").serialize(),
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
					// accept: "application/json",
					contentType : "application/json",
					headers : {
						'Accept' : 'application/json'
					},
					data : ko.toJSON(self.createImageMetaData), // $("#loginForm").serialize(),
					success : function(data, status, xhr) {
						if (data.status == "Error") {
							$('#error_modal_body').text(data.details);
							$("#error_modal").modal({
								backdrop : "static"
							});
							return;
						}
						nextButton();
					}
				});

				// nextButton();
			}
		});

	}

};

function fetchImageLaunchPolicies() {

	// $.ajax({
	// type : "GET",
	// url : endpoint + "getdisplayname",
	// success : function(data, status, xhr) {
	// current_display_name = data;
	//
	// }
	// });

	// $.ajax({
	// type : "GET",
	// url : endpoint + "image-launch-policies",
	// contentType : "application/json",
	// headers : {
	// 'Accept' : 'application/json'
	// },
	// dataType : "json",
	// success : function(data, status, xhr) {
	//
	// image_policies = data.image_launch_policies;
	// addRadios(image_policies);
	// mainViewModel.createImageViewModel = new CreateImageViewModel();
	//
	// $("input[name=launch_control_policy][value='MeasureOnly']").attr(
	// 'checked', 'checked');
	// // / $("input[name=asset_tag_policy][value='Trust
	// // Only']").attr('checked', 'checked');
	//
	// ko.applyBindings(mainViewModel, document
	// .getElementById("create_policy_content_step_1"));
	// }
	// });
	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/trustpolicymetadata",
		dataType : "json",
		success : function(data, status, xhr) {

			$("#display_name").val(data.display_name);
			current_display_name = data.display_name;
			image_policies = data.image_launch_policies;
			addRadios(image_policies);
			mainViewModel.createImageViewModel = new CreateImageViewModel();

			$("input[name=launch_control_policy][value='MeasureOnly']").attr(
					'checked', 'checked');
			// / $("input[name=asset_tag_policy][value='Trust
			// Only']").attr('checked', 'checked');

			ko.applyBindings(mainViewModel, document
					.getElementById("create_policy_content_step_1"));
		}
	});

};

function addRadios(arr) {

	var temp = "";
	for ( var i = 0; i < arr.length; i++) {

		temp = temp
				+ '<label class="radio-inline"><input type="radio" name="launch_control_policy" value="'
				+ arr[i] + '">' + arr[i] + '</label>';

	}

	$('#launch_control_policy').html(temp);
};

