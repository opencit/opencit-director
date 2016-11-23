var imageFormats = new Array();
var image_policies = new Array();

edit_policy_initialize();

function edit_policy_initialize() {
	$("#display_name_repo").val(current_repository);
	fetchImaheHashAlgo("Docker","hashtype_docker");
	
	
    $.ajax({
        type: "PATCH",
        contentType: "application/json",

        url: '/v1/images/' + current_image_id + '/upgradePolicy',
        success: function(data, status, xhr) {
        	
        	current_trust_policy_draft_id=data.draft_id;
        	
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
                            ///	show_error_in_trust_policy_tab("Internal error");
                            return false;
                        } else {
                            current_trust_policy_draft_id = data.id;
                            $.ajax({
                                type: "GET",
                                url: "/v1/trust-policy-drafts/" + current_trust_policy_draft_id,
                                contentType: "application/json",
                                headers: {
                                    'Accept': 'application/json'
                                },
                                dataType: "json",
                                success: function(data, status, xhr) {
                                    showImageLaunchPoliciesDocker(data);
                                }
                            });



                        }

                    }
                });
            } else {
                $.ajax({
                    type: "GET",
                    url: "/v1/trust-policy-drafts/" + current_trust_policy_draft_id,
                    contentType: "application/json",
                    headers: {
                        'Accept': 'application/json'
                    },
                    dataType: "json",
                    success: function(data, status, xhr) {
                        showImageLaunchPoliciesDocker(data);
                    }
                });

            }
        	
        	
        	
        }
    });
	
	
	
	


}


function EditDockerImageMetaData(data) {

    this.image_id = current_image_id;
    this.image_name = current_image_name;
    this.display_name = current_display_name;

}

function EditDockerImageViewModel(data) {
    var self = this;

    self.editDockerImageMetaData = new EditDockerImageMetaData(data);

    self.editDockerImage = function(loginFormElement) {
        console.log(current_image_id);
        self.editDockerImageMetaData.launch_control_policy = $('input[name=launch_control_policy]:checked').val();
        self.editDockerImageMetaData.encrypted = false;
        self.editDockerImageMetaData.display_name = current_repository + ":" +$('#display_name').val();
        current_display_name = $('#display_name').val();
		showLoading();
        $.ajax({
            type: "POST",
            url: "/v1/trust-policy-drafts",
            // accept: "application/json",
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            data: ko.toJSON(self.editDockerImageMetaData), // $("#loginForm").serialize(),
            success: function(data, status, xhr) {

                if (data.error) {
					hideLoading();
                    $('#for_mount_edit_docker').hide();
                    $('#default_edit_docker').show();
                    $('#error_modal_body_edit_docker_1').text(data.error);
                    $("#error_modal_edit_docker_1").modal({
                        backdrop: "static"
                    });
                    $("#editDockerPolicyNext").prop('disabled', false);
                    return;
                }
                current_trust_policy_draft_id = data.id;
                var mountimage = {
                    "id": current_image_id
                }
                $.ajax({
                    type: "POST",
                    url: "/v1/rpc/mount-image",
                    // accept: "application/json",
                    contentType: "application/json",
                    headers: {
                        'Accept': 'application/json'
                    },
                    data: JSON.stringify(mountimage), // $("#loginForm").serialize(),
                    success: function(data, status, xhr) {
                        $("#editDockerPolicyNext").prop('disabled', false);
                        if (data.error) {
							hideLoading();
                            $('#for_mount_edit_docker').show();
                            $('#default_edit_docker').hide();
                            $('#error_modal_body_edit_docker_1').text(data.error);
                            $("#error_modal_edit_docker_1").modal({
                                backdrop: "static"
                            });
                            return;
                        }
						
                        nextButtonDocker();
                    },
					error : function(data, textStatus, errorThrown) {
						if (data.responseJSON.error) {
							hideLoading();
							$('#for_mount_edit_docker').hide();
							$('#default_edit_docker').show();
							$('#error_modal_body_edit_docker_1').text(data.responseJSON.error);
							$("#error_modal_edit_docker_1").modal({
								backdrop: "static"
							});
							$("#editDockerPolicyNext").prop('disabled', false);
							return;
						}
					}
                });

            },
			error : function(data, textStatus, errorThrown) {
				if (data.responseJSON.error) {
					hideLoading();
					$('#for_mount_edit_docker').hide();
					$('#default_edit_docker').show();
					$('#error_modal_body_edit_docker_1').text(data.responseJSON.error);
					$("#error_modal_edit_docker_1").modal({
						backdrop: "static"
					});
					$("#editDockerPolicyNext").prop('disabled', false);
					return;
					
				}
			}
        });

    }

};

function showImageLaunchPoliciesDocker(policydata) {
    current_display_name = policydata.display_name.substring(policydata.display_name.lastIndexOf(":") + 1);
    $("#display_name").val(current_display_name);
	fetchDockerVersionedDisplayName();

    $.ajax({
        type: "GET",
        url: "/v1/image-launch-policies?deploymentType=Docker",
        dataType: "json",
        success: function(data, status, xhr) {
            image_policies = data.image_launch_policies;
            addRadios(image_policies);
            $("input[name=launch_control_policy][value='" + policydata.launch_control_policy + "']").attr('checked', 'checked');

            if (policydata.encrypted == true) {
                $("input[name=isEncrypted]").prop('checked', 'true');
            }
            mainViewModel.editDockerImageViewModel = new EditDockerImageViewModel(
                policydata);

            ko.applyBindings(mainViewModel, document
                .getElementById("edit_policy_docker_content_step_1"));
        }
    });

};

function addRadios(arr) {

    var temp = "";
    for (var i = 0; i < arr.length; i++) {
        if (arr[i].name == 'encrypted') {
            continue;
        }
        temp = temp + '<label class="radio-inline"><input type="radio" name="launch_control_policy" id="edit_policy_' + arr[i].name + '" value="' + arr[i].name + '" >' + arr[i].display_name + '</label>';
    }

    $('#launch_control_policy').html(temp);
};

function fetchDockerVersionedDisplayName(){
    var fetchDisplayname = {"image_id": current_image_id};

    $.ajax({
        type: "POST",
        url: "/v1/rpc/fetch-versioned-display-name",
        contentType: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        data: JSON.stringify(fetchDisplayname),
        success: function(data, status, xhr) {
            var display_name = data.details.substring(data.details.lastIndexOf(":") + 1);
            $("#display_name").val(display_name);
        }
    })
};