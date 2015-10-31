
var pageInitialized = false;
$(document)
		.ready(
				function() {
					if (pageInitialized == true)
						return;

					$("#vm_images_grid_page")
							.load(
									"/v1/html5/features/director-html5/mtwilson-core-html5/content/vm_images_grid_page.html");

					pageInitialized = true;
				});

function createPolicy(imageid, imagename) {
	currentFlow = "Create";
	current_image_id = imageid;
	current_image_name = imagename;

	goToCreatePolicyWizard();
}

function editPolicy(imageid, imagename) {

	currentFlow = "Edit";
	current_image_id = imageid;
	current_image_name = imagename;
	// ///current_trust_policy_id=trust_policy_id;

	goToEditPolicyWizard();
}

function uploadToImageStorePage(imageid, imagename, trust_policy_id) {

	currentFlow = "Upload";
	current_image_id = imageid;
	current_image_name = imagename;
	current_trust_policy_id = trust_policy_id;
	goToUploadToImageStorePage();
}

function goToUploadToImageStorePage() {
	$("#vm_images_grid_page").hide();
	$("#upload_to_image_store_redirect").show();
	var isEmpty = !$.trim($("#upload_to_image_store_redirect").html());

	if (isEmpty == false) {
		$("#upload_to_image_store_redirect").html("");
	}

	$("#upload_to_image_store_redirect")
			.load(
					"/v1/html5/features/director-html5/mtwilson-core-html5/content/upload_imagestore_direct.html");

}

function goToCreatePolicyWizard() {

	$("#vm_images_grid_page").hide();
	$("#create_policy_wizard_").show();

	var htmlpage1 = "create_policy_wizard.html";

	var isEmpty = !$.trim($("#create_policy_wizard_").html());

	if (isEmpty == false) {
		$("#create_policy_wizard_").html("");
	}

	$("#create_policy_wizard_")
			.load(
					"/v1/html5/features/director-html5/mtwilson-core-html5/content/create_policy_wizard.html");

}

function goToEditPolicyWizard() {
	$("#vm_images_grid_page").hide();
	$("#edit_policy_wizard").show();

	var htmlpage = "/v1/html5/features/director-html5/mtwilson-core-html5/content/"
			+ "edit_policy_wizard.html";

	var isEmpty = !$.trim($("#edit_policy_wizard").html());

	if (isEmpty == false) {
		$("#edit_policy_wizard").html("");
	}

	$("#edit_policy_wizard").load(htmlpage);

}

function backToVmImagesPage() {
	$("#create_policy_script").remove();
	$("#edit_policy_wizard").html("");
	$("#create_policy_wizard_").html("");
	$("#create_policy_wizard_").hide("");
	$("#edit_policy_wizard").hide("");
	$("#upload_to_image_store_redirect").html("");
	$("#upload_to_image_store_redirect").hide("");
	$("#vm_images_grid_page").show();

	if(current_image_id != "" && current_image_id !=null)
	{
		$.ajax({
			type : "POST",
			url : endpoint + current_image_id + "/unmount",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.createImageMetaData),
			success : function(data, status, xhr) {
				current_image_id = "";
				console.log("IMAGE UNMOUNTED BECAUSE OF BACKTOVMPAGES");
			}
		});

	}
	$('body').removeClass("modal-open");
	refresh_vm_images_Grid();
	// /
	// ko.cleanNode(mainViewModel,document.getElementById("create_policy_content_step_1"));

}

function backToVmImagesPageWithountUnmount() {
	$("#create_policy_script").remove();
	$("#edit_policy_wizard").html("");
	$("#create_policy_wizard_").html("");
	$("#create_policy_wizard_").hide("");
	$("#edit_policy_wizard").hide("");
	$("#upload_to_image_store_redirect").html("");
	$("#upload_to_image_store_redirect").hide("");
	$("#vm_images_grid_page").show();
	$('body').removeClass("modal-open");
	refresh_vm_images_Grid();
	// /
	// ko.cleanNode(mainViewModel,document.getElementById("create_policy_content_step_1"));

}

function backButton() {

	var activeMasterStepsDiv;
	var active_wizard_page;
	if ($('#create_policy_wizard_').is(':hidden')) {
		activeMasterStepsDiv = "editPolicySteps";
		active_wizard_page = "edit_policy";

	} else if ($('#edit_policy_wizard').is(':hidden')) {
		activeMasterStepsDiv = "createPolicySteps";
		active_wizard_page = "create_policy";

	}

	var divid = $('#' + activeMasterStepsDiv).find("li .selected").attr('id');

	var n = divid.lastIndexOf("_");

	var stepNum = divid.substring(n + 1);

	var previousStepNum = eval(stepNum) - 1;

	$("#" + active_wizard_page + "_content_step_" + stepNum).hide();
	$("#" + active_wizard_page + "_step_" + stepNum).removeClass("selected");
	$("#" + active_wizard_page + "_step_" + stepNum).addClass("done");
	$("#" + active_wizard_page + "_content_step_" + previousStepNum).show();

	$("#" + active_wizard_page + "_step_" + previousStepNum)
			.removeClass("done");
	$("#" + active_wizard_page + "_step_" + previousStepNum).addClass(
			"selected");

}

function nextButton() {

	var activeMasterStepsDiv;
	var active_wizard_page;
	if ($('#create_policy_wizard_').is(':hidden')) {
		activeMasterStepsDiv = "editPolicySteps";
		active_wizard_page = "edit_policy";

	} else if ($('#edit_policy_wizard').is(':hidden')) {
		activeMasterStepsDiv = "createPolicySteps";
		active_wizard_page = "create_policy";

	}

	var divid = $('#' + activeMasterStepsDiv).find("li .selected").attr('id');

	var n = divid.lastIndexOf("_");

	var stepNum = divid.substring(n + 1);

	var nextStepNum = eval(stepNum) + 1;

	$("#" + active_wizard_page + "_content_step_" + stepNum).hide();
	$("#" + active_wizard_page + "_content_step_" + nextStepNum).show();
	var html_url = $("#" + active_wizard_page + "_step_" + nextStepNum).attr(
			'href');
	var htmlpage = "/v1/html5/features/director-html5/mtwilson-core-html5/content/"
			+ html_url.substring(1);

	var isEmpty = !$.trim($(
			"#" + active_wizard_page + "_content_step_" + nextStepNum).html());

	if (isEmpty == true) {
		$("#" + active_wizard_page + "_content_step_" + nextStepNum).load(
				htmlpage);
	}

	$("#" + active_wizard_page + "_step_" + nextStepNum).addClass("selected");
	$("#" + active_wizard_page + "_step_" + stepNum).removeClass("selected");
	$("#" + active_wizard_page + "_step_" + stepNum).addClass("done");
	$("#" + active_wizard_page + "_step_" + nextStepNum)
			.removeClass("disabled");
	$("#" + active_wizard_page + "_step_" + nextStepNum).removeClass("done");
	$("#" + active_wizard_page + "_step_" + nextStepNum).addClass("selected");

}

function downloadPolicy(imageid, trust_policy_id) {
	window.location = "/v1/images/" + imageid + "/downloadPolicy";
}

function downloadImage(imageid) {
	window.location = "/v1/images/" + imageid + "/downloadImage?modified=true";
}