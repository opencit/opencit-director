var imageStores = new Array();

var option;

function mountMetaData() {

}
displayImageStore();

function displayImageStore() {
    if (current_trust_policy_id != "" && current_trust_policy_id != undefined && current_trust_policy_id != "undefined") {
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
                    $('#display_name_last').val(current_display_name);
                    $('#display_name_last_direct').val(current_display_name);
                }
            }
        });
    }
    $
        .ajax({
            type: "GET",
            url: endpoint + "image-stores",
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            dataType: "json",
            success: function(data, status, xhr) {

                imageStores = data.image_stores;

                //option = "<option value='0'>Select</option>";

                option = '<option value="Glance">Glance</option>';

                $('#upload_image_name').val(current_image_name);
                if (currentFlow && (currentFlow == "Upload") && (current_trust_policy_id == "null")) {
                    // / alert("inside !current_trust_policy_id");
                    if (current_display_name == 'undefined' || current_display_name == "") {
                        current_display_name = current_image_name;
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

        if (current_trust_policy_id == undefined || current_trust_policy_id == 'undefined') {
            current_trust_policy_id = "";
        }
        var displayNameFormData = {
            "display_name": current_display_name
        };
        $.ajax({
            type: "PUT",
            url: "/v1/trust-policies/" + current_trust_policy_id,
            contentType: "application/json",
            dataType: "json",
            headers: {
                'Accept': 'application/json'
            },
            data: JSON.stringify(displayNameFormData),
            success: function(data) {
                if (data.status == "Error") {
                    $('#error_docker_body_3_direct').text(data.details);
                    $("#error_docker_3_direct").modal({
                        backdrop: "static"
                    });
                    $('#error_docker_body_3').text(data.details);
                    $("#error_docker_3").modal({
                        backdrop: "static"
                    });
                    $('body').removeClass("modal-open");
                    return;
                }

                var imageActionData = {};
                if (current_trust_policy_id == undefined || current_trust_policy_id == 'undefined' || current_trust_policy_id == '') {
                    imageActionData = {
                        "image_id": current_image_id,
                        "actions": [{
                            "task_name": "Upload Image",
                            "status": "Incomplete",
                            "storename": $('#tarball_upload_combo').val()
                        }]
                    }
                } else {
                    imageActionData = {
                        "image_id": current_image_id,
                        "actions": [{
                                "task_name": "Create Docker Tar",
                                "status": "Incomplete"
                            },{
                            "task_name": "Upload Tar",
                            "status": "Incomplete",
                            "storename": $('#tarball_upload_combo').val()
                        }]
                    }
                }
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
                        if (data.status == "Error") {
                            $('#error_docker_body_3_direct').text(data.details);
                            $("#error_docker_3_direct").modal({
                                backdrop: "static"
                            });
                            $('#error_docker_body_3').text(data.details);
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
                        $("#redirect_direct").modal({
                            backdrop: "static"
                        });
                    }
                });

            }
        });

    }

};


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

        url: "/v1/rpc/createDraftFromPolicy",
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