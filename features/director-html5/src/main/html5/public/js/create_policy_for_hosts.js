var imageFormats = new Array();
var image_policies = new Array();
fetchImaheHashAlgo("BareMetal","hashtype_bm");
function CreateBMLiveMetaData(data) {

    this.image_id = current_image_id;
    this.image_name = $("#image_name").val();
    this.display_name = $("#display_name_host").val();

}

function show_error_in_bmlivemodal(message) {

    $('#error_modal_body_bm_live_1').text(message);
    $("#error_modal_bm_live_1").modal({
        backdrop: "static"
    });
    $('body').removeClass("modal-open");



}


function addhostandnext() {
    var self = this;
    $("#image_name").val(current_image_name);
    self.BMLiveMetaData = new CreateBMLiveMetaData({});
    self.data = {};



    if (!$.trim($("#display_name_host").val()) == false) {
        self.data.policy_name = $("#display_name_host").val();
    } else {
        show_error_in_bmlivemodal("Policy name is mandatory");
        return;
    }




    if (!$.trim($("#host_ip").val()) == false) {
        self.data.ip_address = $("#host_ip").val();
    } else {
        show_error_in_bmlivemodal("Host Ip/Name is mandatory");
        return;
    }


    if (!$.trim($("#username_for_host").val()) == false) {
        self.data.username = $("#username_for_host").val();
    } else {
        show_error_in_bmlivemodal("Username is mandatory");
        return;
    }



    if (!$.trim($("#password_for_host").val()) == false) {
        self.data.password = $("#password_for_host").val();
    } else {
        show_error_in_bmlivemodal("Password is mandatory");
        return;
    }

    if ($("input[name='host_type']:checked").val() == 'windows') {
		self.data.host_type = "Windows";
    } else {
		self.data.host_type = "Linux";	
	}

    self.data.image_id = current_image_id;

    self.data.name = $("#host_ip").val();

    self.data.key = $("#key").val();


    $("#createBMLivePolicyNext").prop('disabled', true);

    if (current_image_id != "") {

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
                    $("#createBMLivePolicyNext").prop('disabled', false);
                    show_error_in_editbmlivemodal(data.error);
                    return;
                }

				if(data.partition){
					var drives = data.partition.split(",");
					drive_to_push = drives[0];
				} else {
					drive_to_push = "";
				}


                self.BMLiveMetaData.launch_control_policy = "MeasureOnly";
                self.BMLiveMetaData.isEncrypted = false;
                self.BMLiveMetaData.display_name = $("#display_name_host").val();
                var mountimage = {
                    "id": current_image_id
                }
				showLoading();
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
						hideLoading();
                            show_error_in_bmlivemodal("Error mounting remote host");
                            $("#createBMLivePolicyNext").prop('disabled', false);
                            return;
                        }


                        $.ajax({
                            type: "POST",
                            url: "/v1/trust-policy-drafts",
                            contentType: "application/json",
                            headers: {
                                'Accept': 'application/json'
                            },
                            data: ko.toJSON(self.BMLiveMetaData),
                            success: function(data, status, xhr) {

                                if (data.status == "Error") {
								hideLoading();
                                    $("#createBMLivePolicyNext").prop('disabled', false);
                                    show_error_in_bmlivemodal(data.details);


                                    $.ajax({
                                        type: "POST",
                                        url: "/v1/rpc/unmount-image",
                                        contentType: "application/json",
                                        headers: {
                                            'Accept': 'application/json'
                                        },
                                        data: JSON.stringify(mountimage),
                                        success: function(data, status, xhr) {
                                            console.log("IMAGE UNMOUNTED");
                                        }
                                    });

                                    return;
                                }
                                current_trust_policy_draft_id = data.id;
                                var policyTemplateRequest = {
                                    "image_id": current_image_id
                                };
			
                                $.ajax({
                                    type: "POST",


                                    url: "/v1/rpc/apply-trust-policy-template/",
                                    contentType: "application/json",
                                    headers: {
                                        'Accept': 'application/json'
                                    },
                                    data: JSON.stringify(policyTemplateRequest),

                                    success: function(data) {
                                        $("#createBMLivePolicyNext").prop('disabled', false);
				
                                        if (data.error) {
										hideLoading();
                                            show_error_in_bmlivemodal(data.error);
                                            return;
                                        }
                                        nextButtonLiveBM();
                                    }
                                });

                            }
                        });
                    },
					error: function(data,status,xhr){
						hideLoading();
					}
                });

            }

        });


    } else {

        $.ajax({
            type: "POST",
            url: "/v1/images/host",
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            data: JSON.stringify(data),
            success: function(data, status, xhr) {
                if (data.error) {
                    show_error_in_bmlivemodal(data.error);
                    $("#createBMLivePolicyNext").prop('disabled', false);
                    return;
                }

                current_image_id = data.image_id;
                self.BMLiveMetaData.launch_control_policy = "MeasureOnly";
                self.BMLiveMetaData.encrypted = false;
                self.BMLiveMetaData.display_name = $("#display_name_host").val();
                self.BMLiveMetaData.image_id = current_image_id;
                var mountimage = {
                    "id": current_image_id
                }
                var policyTemplateRequest = {
                    "image_id": current_image_id
                }
				showLoading();
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
							hideLoading();
                            show_error_in_bmlivemodal("Error mounting remote host");
                            $("#createBMLivePolicyNext").prop('disabled', false);
                            return;
                        }

                        $.ajax({
                            type: "POST",
                            url: "/v1/trust-policy-drafts",

                            contentType: "application/json",
                            headers: {
                                'Accept': 'application/json'
                            },
                            data: ko.toJSON(self.BMLiveMetaData),
                            success: function(data, status, xhr) {
                                if (data.status == "Error") {
								hideLoading();
                                    $("#createBMLivePolicyNext").prop('disabled', false);
                                    show_error_in_bmlivemodal(data.details);
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

                                    //delete the image
                                    $.ajax({
                                        type: "DELETE",
                                        url: "/v1/images/" + current_image_id,
                                        success: function(data, status, xhr) {
                                            console.log("IMAGE deleted");
                                            current_image_id = "";
                                        }
                                    });

                                    return;
                                }
                                current_trust_policy_draft_id = data.id;
			
                                $.ajax({
                                    type: "POST",


                                    url: "/v1/rpc/apply-trust-policy-template/",
                                    contentType: "application/json",
                                    headers: {
                                        'Accept': 'application/json'
                                    },
                                    data: JSON.stringify(policyTemplateRequest),

                                    success: function(data) {
                                        $("#createBMLivePolicyNext").prop('disabled', false);
										
                                        if (data.error) {
											hideLoading();
                                            show_error_in_bmlivemodal(data.error);
                                            return;
                                        }
                                        nextButtonLiveBM();
										
                                    }
                                });
                            }
                        });
                    },
					error: function(data,status,xhr){
						hideLoading();
					}
                });
            },
            error: function(data, status, xhr) {
                var obj = jQuery.parseJSON(data.responseText);

                show_error_in_bmlivemodal(obj.error);
                $("#createBMLivePolicyNext").prop('disabled', false);
                return;

            }
        });


    }



}