var imageStores = new Array();

var option;

displayImageStore();
function displayImageStore() {
	$.ajax({
		type : "GET",
		url : endpoint + current_image_id + "/trustpolicymetadata",
		dataType : "json",
		success : function(data) {
			
				if(data.display_name != undefined &&  data.display_name != null && data.display_name != ""){
					current_display_name = data.display_name;
					$('#display_name_last').val(current_display_name);
					$('#display_name_last_direct')
					.val(current_display_name);
				}
		}
	});
	
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
			
			//option = "<option value='0'>Select</option>";
			
			option = '<option value="Glance">Glance</option>';
			
			$('#upload_image_name').val(current_image_name);
			if (currentFlow && (currentFlow == "Upload")
			&& (current_trust_policy_id == "null")) {
				// / alert("inside !current_trust_policy_id");
				if(current_display_name=='undefined' || current_display_name==""){
					current_display_name=current_image_name;
				}
				$('#display_name_last').val(current_display_name);
				$('#display_name_last_direct')
				.val(current_display_name);
				$('#tarball_radio_div').hide();
				$('#tarball_upload_div').hide();
				
				$('#image_policy_upload_div').show();
				$('#policy_upload_div').hide();
				$('#image_upload_combo').append(option);
				} else {
				$('#tarball_radio_div').hide();
				$("input[name=tarball_radio][value='1']").attr(
				'checked', 'checked');
				
				$('#display_name_last').val(current_display_name);
				$('#display_name_last_direct')
				.val(current_display_name);
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
		$('#image_policy_upload_div').hide();
		
		} else {
		
		$('#tarball_upload_div').hide();
		$('#image_policy_upload_div').hide();
		
	}
};

function UploadStoreMetaData(data) {
	this.image_id = current_image_id;
	this.display_name = current_display_name;
}
function UploadStoreViewModel() {
	var self = this;
	$('#display_name_last').val(current_display_name);
	$('#display_name_last_direct').val(current_display_name);
	
	self.uploadStoreMetaData = new UploadStoreMetaData();
	
	self.uploadToStore = function(loginFormElement) {
		self.uploadStoreMetaData.check_image_action_id = checkImageActionId;
		self.uploadStoreMetaData.image_action_id = current_image_action_id;
		
		self.uploadStoreMetaData.store_name_for_tarball_upload = $(
		'#tarball_upload_combo').val();
		self.uploadStoreMetaData.store_name_for_image_upload = $(
		'#image_upload_combo').val();
		self.uploadStoreMetaData.store_name_for_policy_upload = $(
		'#policy_upload_combo').val();
		
		if (checkImageActionId) {
			if ($('#display_name_last').val() != current_display_name) {
				self.uploadStoreMetaData.display_name = $('#display_name_last')
				.val();
				current_display_name = $('#display_name_last').val();
			}
			} else {
			if ($('#display_name_last_direct').val() != current_display_name) {
				self.uploadStoreMetaData.display_name = $(
				'#display_name_last_direct').val();
				current_display_name = $('#display_name_last_direct').val();
			}
		}
		
		
		
		$.ajax({
			type : "POST",
			url : endpoint + "uploads",
			// accept: "application/json",
			contentType : "application/json",
			dataType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.uploadStoreMetaData),
			success : function(data) {
				if (data.status == "Error") {
					$('#error_vm_body_3_direct').text(data.details);
					$("#error_vm_3_direct").modal({
						backdrop : "static"
					});
						$('body').removeClass("modal-open");
					return;
				}
				console.log("uploadToStore success" + data);
				current_image_action_id = "";
				current_image_id = "";
				$("#redirect").modal({
					backdrop : "static"
				});
			}
		});
		
	}
	
};

function createPolicyDraftFromPolicy() {
	if (current_image_action_id != "") {
		$.ajax({
			type : "GET",
			url : endpoint + current_image_id + "/recreatedraft?action_id="
			+ current_image_action_id,
			success : function(data, status, xhr) {
				console.log("Draft Created Successfully");
				
			}
		});
		} else {
		$.ajax({
			type : "GET",
			url : endpoint + current_image_id + "/recreatedraft",
			success : function(data, status, xhr) {
				console.log("Draft Created Successfully");
				
			}
		});
	}
	$.ajax({
		type : "POST",
		url : endpoint + current_image_id + "/mount",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		data : ko.toJSON(self.createImageMetaData), // $("#loginForm").serialize(),
		success : function(data, status, xhr) {
			backButton();
		}
	});
}