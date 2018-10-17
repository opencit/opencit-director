$(document).ready(function () {
	$("#image_format").hide();
	$("#gotoUploadWindow").click(function () {
		var OpenWindow = window.open("file_upload_window.html", "_blank", "width=500,height=500,resizable=no");
		
		OpenWindow.image_deployments_parent = $("#image_type").val();
		if($("#image_type").val() == 'VM'){
			OpenWindow.image_format_parent = 'qcow2';
		} else {
			OpenWindow.image_format_parent = $("#image_format").val();
		}
	});
	console.log("Before fetchDeploymentType");
	
});
fetchDeploymentType();
function fetchDeploymentType() {
console.log("Inside fetchDeploymentType");
	$.ajax({
		type : "GET",
		url : "/v1/image-deployments",
		dataType : "json",
		success : function(data) {
			var deployment_type = data.image_deployment;
			var option=""; 
			for (var i=0; i < deployment_type.length; i++){ 
				option += '<option value="'+ deployment_type[i].name + '">' + deployment_type[i].display_name + '</option>'; 
			}
			$('#image_type').append(option);
		}
	});

};