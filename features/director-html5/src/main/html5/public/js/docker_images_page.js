var pageInitialized = false;
$(document)
    .ready(
        function() {
            if (pageInitialized == true)
                return;

            $("#docker_images_grid_page").load("/v1/html5/public/director-html5/docker_images_grid_page.html");

            pageInitialized = true;
        });

function createPolicyDocker(imageid, imagename) {
    currentFlow = "Create";
    current_image_id = imageid;
    current_image_name = imagename;
    current_trust_policy_draft_id = '';

    goToCreatePolicyDockerWizard();
}

function editPolicyDocker(imageid, imagename) {

    currentFlow = "Edit";
    current_image_id = imageid;
    current_image_name = imagename;
    current_trust_policy_draft_id = '';

    goToEditPolicyDockerWizard();
}

function uploadToImageStoreDockerPage(imageid, imagename, trust_policy_id) {

    currentFlow = "Upload";
    current_image_id = imageid;
    current_image_name = imagename;
    current_trust_policy_id = trust_policy_id;
    goToUploadToImageStoreDockerPage();
}

function goToUploadToImageStoreDockerPage() {
    $("#docker_images_grid_page").hide();
    $("#upload_to_image_store_redirect_docker").show();
    var isEmpty = !$.trim($("#upload_to_image_store_redirect_docker").html());

    if (isEmpty == false) {
        $("#upload_to_image_store_redirect_docker").html("");
    }

    $("#upload_to_image_store_redirect_docker").load("/v1/html5/public/director-html5/upload_imagestore_direct_docker.html");

}

function goToCreatePolicyDockerWizard() {

    $("#docker_images_grid_page").hide();
    $("#create_policy_wizard_docker").show();

    var htmlpage = "create_policy_wizard_docker.html";

    var isEmpty = !$.trim($("#create_policy_wizard_docker").html());

    if (isEmpty == false) {
        $("#create_policy_wizard_docker").html("");
    }

    $("#create_policy_wizard_docker").load("/v1/html5/public/director-html5/create_policy_wizard_docker.html");

}

function goToEditPolicyDockerWizard() {
    $("#docker_images_grid_page").hide();
    $("#edit_policy_wizard_docker").show();

    var htmlpage = "/v1/html5/public/director-html5/edit_policy_wizard_docker.html";

    var isEmpty = !$.trim($("#edit_policy_wizard_docker").html());

    if (isEmpty == false) {
        $("#edit_policy_wizard_docker").html("");
    }

    $("#edit_policy_wizard_docker").load(htmlpage);

}

function backToDockerPage() {
    $("#edit_policy_wizard_docker").html("");
    $("#create_policy_wizard_docker").html("");
    $("#create_policy_wizard_docker").hide("");
    $("#edit_policy_wizard_docker").hide("");
    $("#upload_to_image_store_redirect_docker").html("");
    $("#upload_to_image_store_redirect_docker").hide("");
    $("#docker_images_grid_page").show();
    var self = this;
    var mountimage = {
        "id": current_image_id
    };
    current_trust_policy_draft_id = '';
    if (current_image_id != "" && current_image_id != null) {

        $.ajax({
            type: "POST",
            url: "/v1/rpc/unmount-image",
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            data: JSON.stringify(mountimage),
            success: function(data, status, xhr) {
                console.log("DOCKER RMI successfully");
                current_image_id = "";
                console.log("IMAGE UNMOUNTED BECAUSE OF BACKTO Docker PAGES");
            }
        });

    }
    $('body').removeClass("modal-open");
    refresh_docker_Grid();

}


function backToDockerPageWithountUnmount() {
    $("#edit_policy_wizard_docker").html("");
    $("#create_policy_wizard_docker").html("");
    $("#create_policy_wizard_docker").hide("");
    $("#edit_policy_wizard_docker").hide("");
    $("#upload_to_image_store_redirect_docker").html("");
    $("#upload_to_image_store_redirect_docker").hide("");
    $("#docker_images_grid_page").show();
    $('body').removeClass("modal-open");
    refresh_docker_Grid();

}

function backButtonDocker() {

    var activeMasterStepsDiv;
    var active_wizard_page;
    if ($('#create_policy_wizard_docker').is(':hidden')) {
        activeMasterStepsDiv = "editPolicyDockerSteps";
        active_wizard_page = "edit_policy_docker";

    } else if ($('#edit_policy_wizard_docker').is(':hidden')) {
        activeMasterStepsDiv = "createPolicyDockerSteps";
        active_wizard_page = "create_policy_docker";

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

function nextButtonDocker() {

    var activeMasterStepsDiv;
    var active_wizard_page;
    if ($('#create_policy_wizard_docker').is(':hidden')) {
        activeMasterStepsDiv = "editPolicyDockerSteps";
        active_wizard_page = "edit_policy_docker";

    } else if ($('#edit_policy_wizard_docker').is(':hidden')) {
        activeMasterStepsDiv = "createPolicyDockerSteps";
        active_wizard_page = "create_policy_docker";

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

    var token_request_json = "{ \"data\": [ { \"not_more_than\": 1} ] }";



    $.ajax({
        type: "POST",
        url: "/v1/login/tokens",
        accept: "application/json",
        contentType: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        data: token_request_json,
        success: function(data, status, xhr) {
            var authtoken = authtoken = data.data[0].token;
            var url = "/v1/images/" + imageid + "/downloads/policy?Authorization=" + encodeURIComponent(authtoken);

            window.location = url;
        },
        error: function(xhr, status, errorMessage) {
            show_error_in_trust_policy_tab("error in downloading");
        }
    });


}

function downloadImage(imageid) {


    var token_request_json = "{ \"data\": [ { \"not_more_than\": 1} ] }";



    $.ajax({
        type: "POST",
        url: "/v1/login/tokens",
        accept: "application/json",
        contentType: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        data: token_request_json,
        success: function(data, status, xhr) {
            var authtoken = authtoken = data.data[0].token;
            var uri = "/v1/images/" + imageid + "/downloadImage?modified=true&Authorization=" + encodeURI(authtoken);

            window.location = uri;

        },
        error: function(xhr, status, errorMessage) {
            show_error_in_trust_policy_tab("error in downloading");
        }
    });


}

function deletePolicyDocker(trust_policy_id, trust_policy_draft_id, imageid, imagename) {
    var callComplete = false;
    console.log("trust_policy_id :: " + trust_policy_id);
    if (trust_policy_id != "" && trust_policy_id != "null" && trust_policy_id != null && trust_policy_id != undefined && trust_policy_id != "undefined") {
        $.ajax({
            type: "DELETE",
            url: "/v1/trust-policy/" + trust_policy_id,
            dataType: "text",
            success: function(result) {
                callComplete = true;
                refresh_docker_Grid();
            }
        });
    }
    console.log("trust_policy_draft_id :: " + trust_policy_draft_id);
    if (trust_policy_draft_id != "" && trust_policy_draft_id != undefined && trust_policy_draft_id != null && trust_policy_draft_id != "null" && trust_policy_draft_id != "undefined") {
        $.ajax({
            type: "DELETE",
            url: "/v1/trust-policy-drafts/" + trust_policy_draft_id,
            dataType: "text",
            success: function(result) {
                callComplete = true;
                refresh_docker_Grid();
            }
        });
    }

}

function deleteImageDocker(imageid) {
    $.ajax({
        type: "DELETE",
        url: "/v1/images/" + imageid,
        dataType: "text",
        success: function(result) {
            refresh_docker_Grid();
        }
    });
}