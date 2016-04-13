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

                if (data.display_name != undefined && data.display_name != null && data.display_name != "") {
                    current_display_name = data.display_name;
                    $('#display_name_last').show();
                    $('#display_name_last').val(current_display_name);
                }
            }
        });

        $.ajax({
            type: "GET",
            url: "/v1/deployment-artifacts?depolymentType=" + current_depolyment_type,
            dataType: "json",
            success: function(data) {
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
        var artifacts_strings = "<option value='0'>Select</option><option value='DockerImage'>Docker Image</option>";
        $('#upload_artifact').html(artifacts_strings);
        $('#upload_artifact').show();
    }
}

$(function() {
    $("#upload_artifact").change(function() {
        $('#upload_image_div').hide();
        $('#upload_policy_div').hide();
        $('#upload_tarball_div').hide();
        var artifact_selected = $('option:selected', this).val();
        var div_to_show = [];
        if (artifact_selected.indexOf("Tarball") > -1) {
            console.log("Tarball");
            div_to_show.push("tarball");
        } else if (artifact_selected.indexOf("Image") > -1) {
            console.log("Image");
            div_to_show.push("image");
        }
        if (artifact_selected.indexOf("Docker") > -1) {
            artifact_selected = "Docker";
        }
        console.log("Artifacts :: " + artifact_selected + " :: divs ::" + div_to_show);
        getImageStore(artifact_selected, div_to_show);
    });
});


function getImageStore(artifact_selected, div_to_show) {
    for (i = 0; i < div_to_show.length; i++) {
        $.ajax({
            type: "GET",
            url: "/v1/image-stores?artifacts=" + artifact_selected,
            dataType: "json",
            async: false,
            success: function(data) {
                var image_stores = data.image_stores;
                var image_stores_strings = "<option value='0'>Select</input>"
                for (j = 0; j < image_stores.length; j++) {
                    image_stores_strings += "<option value=" + image_stores[j].id + ">" + image_stores[j].name + "</option>";
                }
                $('#upload_' + div_to_show[i]).html(image_stores_strings);
                $('#upload_' + div_to_show[i] + '_div').show();
            }
        });
    }
}

function uploadToStore() {
    var self = this;
    var artifact = $('#upload_artifact').val();
    if (artifact == "0") {
        $('#error_docker_body_3').text("Select Artifact To Upload");
        $("#error_docker_3").modal({
            backdrop: "static"
        });
        $('body').removeClass("modal-open");
        return;
    }
    var artifact_name = artifact;
    var uploadStoreMetaData = {}
    uploadStoreMetaData.artifact_store_list = [];
    uploadStoreMetaData.image_id = current_image_id;
    var artifact_loc = "";
    if (artifact.indexOf("Tarball") > -1) {
        artifact_loc = "tarball";
    } else if (artifact.indexOf("Image") > -1) {
        artifact_loc = "image";
    }

    var image_store_id = $("#upload_" + artifact_loc).val();
    if (image_store_id == "0") {
        $('#error_docker_body_3').text("Select Appropriate Image Store");
        $("#error_docker_3").modal({
            backdrop: "static"
        });
        $('body').removeClass("modal-open");
        return;
    }
    var artifact_store = {
        "artifact_name": artifact_name,
        "image_store_id": image_store_id
    };
    uploadStoreMetaData.artifact_store_list.push(artifact_store);
    createImageStoreActions(uploadStoreMetaData);
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

            if (data.error) {
                $('#error_docker_body_3').text(data.error);
                $("#error_docker_3").modal({
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

            if (data.status == "Error") {
                $('#error_docker_body_3').text("Internal Error Occured");
                $("#error_docker_3").modal({
                    backdrop: "static"
                });

                $('body').removeClass("modal-open");
                return false;
            } else {
                current_trust_policy_draft_id = data.id;
				var display_name = data.display_name.substring(data.display_name.lastIndexOf(":") + 1);
				$("#display_name_last").val(data.display_name);
				$("#display_name").val(display_name);
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
            backButtonDocker();
        }
    });
}