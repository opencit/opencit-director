var endpoint = "/v1/images/";
var pageInitialized = false;
$(document).ready(function() {

	if (pageInitialized == true)
		return;

	refresh_vm_images_Grid();

	pageInitialized = true;
});

function refresh_vm_images_Grid() {
	$("#vmGrid").html("")
	$.ajax({
		type : "GET",
		url : endpoint + "imagesList/VM",
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			console.log("vm grid refreshed");
			images = data.images;
			$("#vmGrid").jsGrid({

				height : "auto",
				width : "100%",
				sorting : true,
				paging : true,
				pageSize : 10,
				pageButtonCount : 15,
				data : images,
				fields : [ {
					title : "Image Name",
					name : "image_name",
					type : "text",
					width : 250,
					align : "center"
				}, {
					title : "Image Format",
					name : "image_format",
					type : "text",
					width : 120,
					align : "center"
				}, {
					title : "Trust Policy",
					name : "trust_policy",
					type : "text",
					width : 100,
					align : "center"
				}, {
					title : "Image Store Upload",
					name : "image_upload",
					type : "text",
					width : 100,
					align : "center"
				}, {
					title : "Created Date",
					name : "created_date",
					type : "text",
					width : 150,
					align : "center"
				} ]
			});
			var delay = 1000; 
			setTimeout(function() {
				$("#vmGrid").show();
				$("#vmGrid").jsGrid("refresh");
			}, delay);

		},
		error : function(jqXHR, exception) {
			alert("Failed to get images list");
		}
	});

}
