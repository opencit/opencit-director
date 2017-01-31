var pageInitialized = false;
$(document)
		.ready(
				function() {
					if (pageInitialized == true)
						return;

					$("#vm_images_grid_page")
							.load(
									"/v1/html5/public/director-html5/vm_images_grid_page.html");

					pageInitialized = true;
				});

function createTrustPolicy(imageid, imagename) {
	currentFlow = "Create";
	current_image_id = imageid;
	current_image_name = imagename;
	current_trust_policy_draft_id = '';

	goToCreatePolicyWizard();
}

function editTrustPolicy(imageid, imagename, trust_policy_draft_id) {

	currentFlow = "Edit";
	current_image_id = imageid;
	current_image_name = imagename;
	current_trust_policy_draft_id = trust_policy_draft_id;

	goToEditPolicyWizard();
}

function uploadToImageStorePage(imageid, imagename, trust_policy_id) {
	current_flow = "Grid";
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

	$("#upload_to_image_store_redirect").load(
			"/v1/html5/public/director-html5/upload_imagestore.html");

}

function goToCreatePolicyWizard() {

	$("#vm_images_grid_page").hide();
	$("#create_policy_wizard_").show();

	var htmlpage1 = "create_policy_wizard.html";

	var isEmpty = !$.trim($("#create_policy_wizard_").html());

	if (isEmpty == false) {
		$("#create_policy_wizard_").html("");
	}

	$("#create_policy_wizard_").load(
			"/v1/html5/public/director-html5/create_policy_wizard.html");

}

function goToEditPolicyWizard() {
	$("#vm_images_grid_page").hide();
	$("#edit_policy_wizard").show();

	var htmlpage = "/v1/html5/public/director-html5/edit_policy_wizard.html";

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
	var self = this;
	var mountimage = {
		"id" : current_image_id
	};
	current_trust_policy_draft_id = '';
	if (current_image_id != "" && current_image_id != null) {

		$.ajax({
			type : "POST",
			url : "/v1/rpc/unmount-image",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : JSON.stringify(mountimage),
			success : function(data, status, xhr) {
				current_image_id = "";
				console.log("IMAGE UNMOUNTED BECAUSE OF BACKTOVMPAGES");
			}
		});
	}
	$('body').removeClass("modal-open");
	refresh_vm_images_Grid();
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
	var htmlpage = "/v1/html5/public/director-html5/" + html_url.substring(1);

	var isEmpty = !$.trim($(
			"#" + active_wizard_page + "_content_step_" + nextStepNum).html());

	if (isEmpty == true) {
		$("#" + active_wizard_page + "_content_step_" + nextStepNum).load(
				htmlpage);
	} else {
		$("#director_loading_icon").hide();
		$("#loading_icon_container").hide();
		$("#director_loading_icon").html("");

	}

	$("#" + active_wizard_page + "_step_" + nextStepNum).addClass("selected");
	$("#" + active_wizard_page + "_step_" + stepNum).removeClass("selected");
	$("#" + active_wizard_page + "_step_" + stepNum).addClass("done");
	$("#" + active_wizard_page + "_step_" + nextStepNum)
			.removeClass("disabled");
	$("#" + active_wizard_page + "_step_" + nextStepNum).removeClass("done");
	$("#" + active_wizard_page + "_step_" + nextStepNum).addClass("selected");

}



function deletePolicyVM(trust_policy_id, trust_policy_draft_id, imageid,
		imagename) {
	var callComplete = false;
	console.log("trust_policy_id :: " + trust_policy_id);
	if (trust_policy_id != "" && trust_policy_id != "null"
			&& trust_policy_id != null && trust_policy_id != undefined
			&& trust_policy_id != "undefined") {
		$.ajax({
			type : "DELETE",
			url : "/v1/trust-policy/" + trust_policy_id,
			dataType : "text",
			success : function(result) {
				callComplete = true;
				refresh_vm_images_Grid();
			}
		});
	}
	console.log("trust_policy_draft_id :: " + trust_policy_draft_id);
	if (trust_policy_draft_id != "" && trust_policy_draft_id != undefined
			&& trust_policy_draft_id != null && trust_policy_draft_id != "null"
			&& trust_policy_draft_id != "undefined") {
		$.ajax({
			type : "DELETE",
			url : "/v1/trust-policy-drafts/" + trust_policy_draft_id,
			dataType : "text",
			success : function(result) {
				callComplete = true;
				refresh_vm_images_Grid();
			}
		});
	}
}

function deleteImage(imageid) {
	$.ajax({
		type : "DELETE",
		url : "/v1/images/" + imageid,
		dataType : "text",
		success : function(result) {
			refresh_vm_images_Grid();
			refreshBMOnlineGrid();
		}
	});

}