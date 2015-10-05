
var imageStores = new Array();

var option;

displayImageStore();
function displayImageStore() {

	$
			.ajax({
				type : "GET",
				url : endpoint + "imagestores",
				// accept: "application/json",
				contentType : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				dataType : "json",
				success : function(data, status, xhr) {

					imageStores = data.imageStoreNames;

					option = "<option value='0'>Select</option>";
					for (var i = 0; i < imageStores.length; i++) {
						option += '<option value="' + imageStores[i] + '">'
								+ imageStores[i] + '</option>';

					}

		///		alert("current_trust_policy_id::"+current_trust_policy_id+" currentFlow::"+currentFlow);
					if (currentFlow && (currentFlow == "Upload") && (current_trust_policy_id=="null")) {
				///			alert("inside !current_trust_policy_id");
						$('#upload_image_name').val(current_image_name);
						$('#tarball_radio_div').hide();
						$('#tarball_upload_div').hide();
						
						$('#image_policy_upload_div').show();
						$('#policy_upload_div').hide();
						$('#image_upload_combo').append(option);
					} else {

						$("input[name=tarball_radio][value='1']").attr(
								'checked', 'checked');
						$('#upload_image_name').val(current_image_name);
						$('#tarball_upload_div').show();
						$('#image_policy_upload_div').hide();
						$('#tarball_upload_combo').append(option);
						$('#image_upload_combo').append(option);
						$('#policy_upload_combo').append(option);

					}
					mainViewModel.uploadImageStoreViewModel = new UploadStoreViewModel();
					// /] ko.cleanNode(mainViewModel);

					ko.applyBindings(mainViewModel, document
							.getElementById("upload_to_image_store"));

				}
			});

}

function toggleradios11() {

	var showTaraballDiv = $('input[name=tarball_radio]:checked').val();

	if (showTaraballDiv == 0) {

		$('#image_policy_upload_div').show();
		$('#tarball_upload_div').hide();

		//	$('#image_upload_combo').append(option);
		///	$('#policy_upload_combo').append(option);
	} else {

		$('#tarball_upload_div').show();
		$('#image_policy_upload_div').hide();
		///	$('#tarball_upload_combo').append(option);
	}
};



function UploadStoreMetaData(data) {
	this.image_id = current_image_id;
}

function UploadStoreViewModel() {
	var self = this;

	self.uploadStoreMetaData = new UploadStoreMetaData();

	self.uploadToStore = function(loginFormElement) {
		console.log(current_image_action_id);

	
			
			self.uploadStoreMetaData.check_image_action_id = checkImageActionId;
			self.uploadStoreMetaData.image_action_id = current_image_action_id;
		
		
		self.uploadStoreMetaData.store_name_for_tarball_upload = $(
				'#tarball_upload_combo').val();
		self.uploadStoreMetaData.store_name_for_image_upload = $(
				'#image_upload_combo').val();
		self.uploadStoreMetaData.store_name_for_policy_upload = $(
				'#policy_upload_combo').val();
		console.log(self.uploadStoreMetaData.store_name_for_tarball_upload);
		$.ajax({
			type : "POST",
			url : endpoint + "uploads",
			// accept: "application/json",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.uploadStoreMetaData), 
			success : function(data, status, xhr) {
				console.log("uploadToStore success" + data);
				current_image_action_id = undefined; 
			}
		});

	}

};

function createPolicyDraftFromPolicy()
{
	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/recreatedraft?action_id="+current_image_action_id,
		success : function(data, status, xhr) {
			console.log("Draft Created Successfully");
			
		}
	});
	backButton();
}

function validateEntries()
{
	var isTarball = $('input[name=tarball_radio]:checked').val();
	console.log(isTarball);
	if (isTarball != 0) 
	{
		var tarLocation = $('#tarball_upload_combo').val();
		console.log(tarLocation);
		if(tarLocation == 0)
		{
			console.log("Not Valid");
			return false;
		}
	} else {
		var policyLocation = $('#policy_upload_combo').val();
		var imageLocation = $('#image_upload_combo').val();
		
		if(policyLocation == 0 && imageLocation == 0)
		{
			console.log("Not Valid Policy && Image Policy");
			return false;
		}
		if(policyLocation == "0" && imageLocation == "0")
		{
			console.log("Not Valid Policy && Image Policy As String");
			return false;
		}
		
		
	}
	console.log("Valid");
	return true;
}
