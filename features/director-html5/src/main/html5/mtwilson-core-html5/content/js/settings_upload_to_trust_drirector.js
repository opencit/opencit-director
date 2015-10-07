$(document).ready(function () {
	$("#gotoUploadWindow").click(function () {
		var OpenWindow = window.open("file_upload_window.html", "_blank", "width=500,height=500,resizable=no");
		OpenWindow.image_deployments_parent = $("#image_type").val();
		OpenWindow.image_format_parent = $("#image_format").val();
	});
});