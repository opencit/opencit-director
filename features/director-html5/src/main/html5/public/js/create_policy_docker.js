var imageFormats = new Array();
var image_policies = new Array();

$(document).ready(function() {
	$("#display_name_repo").val(current_repository);
    fetchImageLaunchPolicies();
	fetchImaheHashAlgo("Docker","hashtype_docker");
});

function CreateDockerImageMetaData(data) {
    this.image_id = current_image_id;
    this.image_name = current_image_name;
    this.display_name = current_display_name;
}

function CreateDockerImageViewModel() {
    var self = this;

    self.createDockerImageMetaData = new CreateDockerImageMetaData({});

    self.createDockerImage = function(loginFormElement) {

        self.createDockerImageMetaData.launch_control_policy = $('input[name=launch_control_policy]:checked').val();
        self.createDockerImageMetaData.encrypted = false;

        self.createDockerImageMetaData.display_name = current_repository + ":" +$('#display_name').val();
        current_display_name = $('#display_name').val();
		showLoading();
        $.ajax({
            type: "POST",
            url: "/v1/trust-policy-drafts",
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            data: ko.toJSON(self.createDockerImageMetaData), // $("#loginForm").serialize(),
			$('#display_name_last').val(current_display_name);
            success: function(data, status, xhr) {

                if (data.error) {
					hideLoading();
					console.log(data.error);
                    $('#for_mount_docker').hide();
                    $('#default_docker').show();
                    $('#error_modal_body_docker_1').text(data.error);
                    $("#error_modal_docker_1").modal({
                        backdrop: "static"
                    });
                    $("#createDockerPolicyNext").prop('disabled', false);
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
                    data: JSON.stringify(mountimage),
                    success: function(data, status, xhr) {
                        $("#createDockerPolicyNext").prop('disabled', false);
                        if (data.error) {
							hideLoading();
                            $('#default_docker').hide();
                            $('#for_mount_docker').show();
                            $('#error_modal_body_docker_1').text(data.error);
                            $("#error_modal_docker_1").modal({
                                backdrop: "static"
                            });
                            $('body').removeClass("modal-open");
                            return;
                        }
						
                        nextButtonDocker();
                    }
                });
            },
            error: function(data, status, xhr) {
				hideLoading();
                console.log(data);
                $('#for_mount_docker').hide();
                $('#default_docker').show();
                $('#error_modal_body_docker_1').text("");
                var obj = jQuery.parseJSON(data.responseText);
                $('#error_modal_body_docker_1').text(obj.error);
                $("#error_modal_docker_1").modal({
                    backdrop: "static"
                });
                $("#createDockerPolicyNext").prop('disabled', false);
                return;
            }
        });

    }

};

function fetchImageLaunchPolicies() {

    $("#display_name").val(current_image_name);
    $.ajax({
        type: "GET",
        url: "/v1/image-launch-policies?deploymentType=Docker",
        dataType: "json",
        success: function(data, status, xhr) {

            image_policies = data.image_launch_policies;
            addRadios(image_policies);
            mainViewModel.createDockerImageViewModel = new CreateDockerImageViewModel();
            $("input[name=launch_control_policy][value='MeasureOnly']").attr('checked', 'checked');
            ko.applyBindings(mainViewModel, document.getElementById("create_policy_docker_content_step_1"));
        }
    });

};

function addRadios(arr) {

    var temp = "";
    for (var i = 0; i < arr.length; i++) {
        if (arr[i].name == 'encrypted') {
            continue;
        }
        temp = temp + '<label class="radio-inline"><input type="radio" name="launch_control_policy" id="create_policy_' + arr[i].name + '" value="' + arr[i].name + '">' + arr[i].display_name + '</label>';

    }
    $('#launch_control_policy').html(temp);
};