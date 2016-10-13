
var imageFormats = new Array();
var image_policies = new Array();

fetchImaheHashAlgo("BareMetal","hashtype_bm");
edit_policy_bmlive_initialize();

function edit_policy_bmlive_initialize() {

    if (!current_trust_policy_draft_id) {
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

                if (data.error) {
                    return false;
                } else {
                    current_trust_policy_draft_id = data.id;

                    $.ajax({
                        type: "GET",
                        url: "/v1/images/" + current_image_id,
                        contentType: "application/json",
                        headers: {
                            'Accept': 'application/json'
                        },
                        dataType: "json",
                        success: function(data, status, xhr) {
                            $("#host_ip_edit").val(data.ip_address);
                            $("#username_for_host_edit").val(data.username);
                            console.log("Partition :: " + data.partition);
                            if (data.partition) {
                                console.log("Windows");
                                $("input[name='host_type'][value='linux']")
                                    .attr('checked', 'unchecked');
                                $("input[name='host_type'][value='windows']")
                                    .attr('checked', 'checked');
								var drives = data.partition.split(",");
								drive_to_push = drives[0];
                            } else {
                                console.log("Linux");
                                $("input[name='host_type'][value='windows']")
                                    .attr('checked', 'unchecked');
                                $("input[name='host_type'][value='linux']")
                                    .attr('checked', 'checked');
								drive_to_push = "";
                            }

                        }
                    });

                    $
                        .ajax({
                            type: "GET",
                            url: "/v1/trust-policy-drafts/" + current_trust_policy_draft_id,
                            // accept: "application/json",
                            contentType: "application/json",
                            headers: {
                                'Accept': 'application/json'
                            },
                            dataType: "json",
                            success: function(data, status, xhr) {
                                $("#display_name_host_edit").val(
                                    data.display_name);
                            }
                        });

                }

            }
        });
    } else {

        $.ajax({
            type: "GET",
            url: "/v1/images/" + current_image_id,
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            dataType: "json",
            success: function(data, status, xhr) {
                $("#host_ip_edit").val(data.ip_address);
                $("#username_for_host_edit").val(data.username);
                console.log("Partition :: " + data.partition);
                if (data.partition) {
                    console.log("Windows");
                    $("input[name='host_type'][value='linux']").attr('checked', 'unchecked');
                    $("input[name='host_type'][value='windows']").attr('checked', 'checked');
					var drives = data.partition.split(",");
					drive_to_push = drives[0];
                } else {
                    console.log("Linux");
                    $("input[name='host_type'][value='windows']").attr('checked', 'unchecked');
                    $("input[name='host_type'][value='linux']").attr('checked', 'checked');
					drive_to_push = "";
                }
            }
        });

        $.ajax({
            type: "GET",
            url: "/v1/trust-policy-drafts/" + current_trust_policy_draft_id,
            // accept: "application/json",
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            dataType: "json",
            success: function(data, status, xhr) {
                $("#display_name_host_edit").val(data.display_name);
            }
        });

    }

}




function EditBMLiveMetaData() {

    this.image_id = current_image_id;
    this.image_name = current_image_name;
    this.display_name = $("#display_name_host_edit").val();
}


function show_error_in_editbmlivemodal(message) {

    $('#error_modal_body_edit_bm_live_1').text(message);
    $("#error_modal_edit_bm_live_1").modal({
        backdrop: "static"
    });
    $('body').removeClass("modal-open");



}

function editandNext() {
    var self = this;
    $("#host_name_live_edit").val(current_image_name);
    self.editBMLiveMetaData = new EditBMLiveMetaData();
    self.data = {};




    if (!$.trim($("#display_name_host_edit").val()) == false) {
        self.data.policy_name = $("#display_name_host_edit").val();
    } else {
        show_error_in_editbmlivemodal("Policy name is mandatory");
        return;
    }




    if (!$.trim($("#host_ip_edit").val()) == false) {
        self.data.ip_address = $("#host_ip_edit").val();
    } else {
        show_error_in_editbmlivemodal("Host Ip/Name is mandatory");
        return;
    }


    if (!$.trim($("#username_for_host_edit").val()) == false) {
        self.data.username = $("#username_for_host_edit").val();
    } else {
        show_error_in_editbmlivemodal("Username is mandatory");
        return;
    }



    if (!$.trim($("#password_for_host_edit").val()) == false) {
        self.data.password = $("#password_for_host_edit").val();
    } else {
        show_error_in_editbmlivemodal("Password is mandatory");
        return;
    }

    if ($("input[name='host_type']:checked").val() == 'windows') {
        self.data.host_type = "Windows";
    } else {
        self.data.host_type = "Linux";
        drive_to_push = "";
    }




    self.data.key = $("#key_edit").val();
    self.data.image_id = current_image_id;



    $("#editBMLivePolicyNext").prop('disabled', true);


    $.ajax({
        type: "PUT",
        url: "/v1/images/host",
        contentType: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        data: JSON.stringify(data),
        success: function(data, status, xhr) {

            if (data.error) {
                $("#editBMLivePolicyNext").prop('disabled', false);
                show_error_in_editbmlivemodal(data.error);
                return;
            }



            self.editBMLiveMetaData.launch_control_policy = "MeasureOnly";
            self.editBMLiveMetaData.isEncrypted = false;
            self.editBMLiveMetaData.display_name = $("#display_name_host_edit").val();
            var mountimage = {
                "id": current_image_id
            }
            $.ajax({
                type: "POST",
                url: "/v1/rpc/mount-image",

                contentType: "application/json",
                headers: {
                    'Accept': 'application/json'
                },
                data: JSON.stringify(mountimage),
                success: function(data, status, xhr) {

                    if (data.error) {
                        $("#editBMLivePolicyNext").prop('disabled', false);
                        show_error_in_editbmlivemodal(data.error);
                        return;
                    }


                    $.ajax({
                        type: "POST",
                        url: "/v1/trust-policy-drafts",
                        contentType: "application/json",
                        headers: {
                            'Accept': 'application/json'
                        },
                        data: ko.toJSON(self.editBMLiveMetaData),
                        success: function(data, status, xhr) {
                            $("#editBMLivePolicyNext").prop('disabled', false);

                            if (data.error) {
                                show_error_in_editbmlivemodal(data.error);

                                $.ajax({
                                    type: "POST",
                                    url: "/v1/rpc/unmount-image",
                                    contentType: "application/json",
                                    headers: {
                                        'Accept': 'application/json'
                                    },
                                    data: JSON.stringify(mountimage),
                                    success: function(data, status, xhr) {
                                        console.log("IMAGE UNMOUNTED BECAUSE OF BACKTOVMPAGES");
                                    }
                                });

                                return;
                            }
                            current_trust_policy_draft_id = data.id;
                            nextButtonLiveBM();
                        }
                    });
                }
            });

        }

    });
};