displayImageStorePage();

function displayImageStorePage() {
    if (current_flow == "Wizard") {
        $("#grid_flow_buttons").hide();
        $("#wizard_flow_buttons").show();
    } else {
        $("#grid_flow_buttons").show();
        $("#wizard_flow_buttons").hide();
    }
    $("#display_name_last_div").hide();
    $('#upload_artifact').hide();
    $('#upload_image_div').hide();
    $('#upload_policy_div').hide();
    $('#upload_tarball_div').hide();

    if (current_trust_policy_id != "" && current_trust_policy_id != undefined && current_trust_policy_id != "undefined") {
        $("#display_name_last_div").show();
        $.ajax({
            type: "GET",
            url: "/v1/trust-policy/" + current_trust_policy_id,
            headers: {
                'Accept': 'application/json'
            },
            dataType: "json",
            success: function(data) {
            	data=htmlEncode(data);
            	if (data.display_name != undefined && data.display_name != null && data.display_name != "") {
                    current_display_name = data.display_name;
                    $('#display_name_last').show();
                    $('#display_name_last').val(htmlEncode(current_display_name));
                }
            }
        });

        $.ajax({
            type: "GET",
            url: "/v1/deployment-artifacts?deploymentType=" + current_depolyment_type,
            dataType: "json",
            success: function(data) {
            	data=htmlEncode(data);
                var artifacts_strings = "<option value='0'>Select</option>";
                for (var key in data) {
                    artifacts_strings = artifacts_strings + "<option value=" + key + ">" + data[key] + "</option>";
                }
                $('#upload_artifact').html(artifacts_strings);
                $('#upload_artifact').show();
            }
        });


    } else {
        $("#display_name_last_div").hide();
        var artifacts_strings = "<option value='0'>Select</option><option value='Image'>Image</option>";
        $('#upload_artifact').html(artifacts_strings);
        $('#upload_artifact').show();
    }

    mainViewModel.uploadImageStoreViewModel = new UploadStoreViewModel();
    ko.applyBindings(mainViewModel, document.getElementById("upload_to_image_store"));

}

$(function() {
    $("#upload_artifact").change(function() {
        $('#upload_image_div').hide();
        $('#upload_policy_div').hide();
        $('#upload_tarball_div').hide();
        var artifact_selected = $('option:selected', this).val();
        var artifact_array = artifact_selected.split("With");
        var div_to_show = [];

        for (i = 0; i < artifact_array.length; i++) {
            console.log(artifact_array[i]);
            div_to_show.push(artifact_array[i].toLowerCase());
        }

        getImageStore(artifact_array, div_to_show);
    });
});

function getImageStore(artifact_array, divs) {
    for (i = 0; i < divs.length; i++) {

        $.ajax({
            type: "GET",
            url: "/v1/image-stores?artifacts=" + artifact_array[i],
            dataType: "json",
            async: false,
            success: function(data) {
            	data=htmlEncode(data);
                var image_stores = data.image_stores;
                var image_stores_strings = "<option value='0'>Select</option>";
                for (j = 0; j < image_stores.length; j++) {
                    image_stores_strings = image_stores_strings + "<option value=" + image_stores[j].id + ">" + htmlEncode(image_stores[j].name) + "</option>";
                }
                $('#upload_' + divs[i]).html(image_stores_strings);
                $('#upload_' + divs[i] + '_div').show();
            }
        });
    }
}


function UploadStoreViewModel() {
    var self = this;

    self.uploadToStore = function(loginFormElement) {
        var artifacts = $('#upload_artifact').val().split("With");

        if (artifacts.length == 0 || artifacts[0] == "0") {
            $('#error_vm_body_3').text("Select Artifact To Upload");
            $("#error_vm_3").modal({
                backdrop: "static"
            });
            $('body').removeClass("modal-open");
            return;
        }

        var uploadStoreMetaData = {}
        uploadStoreMetaData.artifact_store_list = [];
        uploadStoreMetaData.image_id = current_image_id;
        for (i = 0; i < artifacts.length; i++) {
            var image_store_id = $("#upload_" + artifacts[i].toLowerCase()).val();

            if (image_store_id == "0") {
                $('#error_vm_body_3').text("Select Appropriate Image Store");
                $("#error_vm_3").modal({
                    backdrop: "static"
                });
                $('body').removeClass("modal-open");
                return;
            }
            var artifact_name = artifacts[i];
            var artifact_store = {
                "artifact_name": artifact_name,
                "image_store_id": image_store_id
            };
            uploadStoreMetaData.artifact_store_list.push(artifact_store);
        }

        current_display_name = $('#display_name_last').val();

        if (current_trust_policy_id == undefined || current_trust_policy_id == 'undefined') {
            current_trust_policy_id = "";
        }

        /*    if ( current_trust_policy_id != undefined  && current_trust_policy_id != "") {
        var displayNameFormData = {
            "display_name": current_display_name
        };
        $.ajax({
            type: "POST",
            url: "/v1/trust-policies/" + current_trust_policy_id,
            contentType: "application/json",
            dataType: "json",
            headers: {
                'Accept': 'application/json'
            },
            data: JSON.stringify(displayNameFormData),
            success: function(data) {
                if (data.error) {
                    $('#error_vm_body_3').text(data.error);
                    $("#error_vm_3").modal({
                        backdrop: "static"
                    });
                    $('body').removeClass("modal-open");
                    return;
                }
	
                createImageStoreActions(uploadStoreMetaData);

            }
        });

    }else{
    	createImageStoreActions(uploadStoreMetaData);
    } */
        createImageStoreActions(uploadStoreMetaData);
    }

};


function createImageStoreActions(uploadStoreMetaData) {
    $.ajax({
        type: "POST",
        url: "/v1/image-actions",
        contentType: "application/json",
        dataType: "json",
        headers: {
            'Accept': 'application/json'
        },
        data: JSON.stringify(uploadStoreMetaData),
        success: function(data, status) {
        	data=htmlEncode(data);
            if (data.error) {
                $('#error_vm_body_3').text(htmlEncode(data.error));
                $("#error_vm_3").modal({
                    backdrop: "static"
                });
                $('body').removeClass("modal-open");
                return false;
            }
            console.log("uploadToStore success" + data);
            current_image_action_id = "";
            current_trust_policy_id = "";
            current_image_id = "";
            $("#redirect").modal({
                backdrop: "static"
            });
        },
        error: function(jqXHR, error, errorThrown) {
        	jqXHR=htmlEncode(jqXHR);
            var message = "";
            if (jqXHR.status == 400) {
                message = (JSON.parse(jqXHR.responseText)).error;
            } else {
                message = "Something went wrong";;
            }
            $('#error_vm_body_3').text(message);
            $("#error_vm_3").modal({
                backdrop: "static"
            });
            $('body').removeClass("modal-open");
        }
    });

}


function createPolicyDraftFromPolicy() {
    var mountimage = {
        "id": current_image_id
    }
    var create_draft_request = {
        "image_id": current_image_id
    }

    $.ajax({
        type: "POST",
        contentType: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        data: JSON.stringify(create_draft_request),

        url: "/v1/rpc/create-draft-from-policy",
        success: function(data, status, xhr) {
        	data=htmlEncode(data);
            if (data.error) {
                $('#error_vm_body_3').text("Internal Error Occured");
                $("#error_vm_3").modal({
                    backdrop: "static"
                });

                $('body').removeClass("modal-open");
                return false;
            } else {
                current_trust_policy_draft_id = data.id;
				$("#display_name_last").val(htmlEncode(data.display_name));
				$("#display_name").val(htmlEncode(data.display_name));
            }

        }
    });
    $.ajax({
        type: "POST",
        url: "/v1/rpc/mount-image",
        contentType: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        data: JSON.stringify(mountimage), // $("#loginForm").serialize(),
        success: function(data, status, xhr) {
        	data=htmlEncode(data);
            backButton();
        }
    });
}

function createImageActions(imageActionData) {
    $.ajax({
        type: "POST",
        url: "/v1/image-actions",
        contentType: "application/json",
        dataType: "json",
        headers: {
            'Accept': 'application/json'
        },
        data: JSON.stringify(imageActionData),
        success: function(data) {
        	data=htmlEncode(data);
            if (data.error) {
                $('#error_vm_body_3_direct').text(data.details);
                $("#error_vm_3_direct").modal({
                    backdrop: "static"
                });
                $('#error_vm_body_3').text(data.details);
                $("#error_vm_3").modal({
                    backdrop: "static"
                });
                $('body').removeClass("modal-open");
                return false;
            }
            console.log("uploadToStore success" + data);
            current_image_action_id = "";
            current_trust_policy_id = "";
            current_image_id = "";
            $("#redirect").modal({
                backdrop: "static"
            });
            $("#redirect_direct").modal({
                backdrop: "static"
            });
        }
    });

}