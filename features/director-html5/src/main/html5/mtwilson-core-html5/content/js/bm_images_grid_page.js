var pageInitialized = false;
$(document).ready(function() {

	if (pageInitialized == true)
		return;

	refreshBMOnlineGrid();
	refresh_bm_images_Grid();

	pageInitialized = true;
});

function refreshBMOnlineGrid() {
	endpoint = "/v1/images/";
	$("#bmGridOnline").html("")
	$.ajax({
		type : "GET",
		url : endpoint + "imagesList/BareMetalLive",
		dataType : "json",
		success : function(result) {

			console.log("BM GRID REFRESHED");
			$("#bmGridOnline").jsGrid({

				height : "auto",
				width : "100%",
				sorting : true,
				paging : true,
				pageSize : 10,
				pageButtonCount : 15,
				data : result.images,
				fields : [ {
					title : "Host Name",
					name : "image_name",
					type : "text",
					width : 250,
					align : "center"
				},  {
					title : "Trust Policy",
					name : "trust_policy",
					type : "text",
					width : 100,
					align : "center"
				},/* {
					title : "Status/Push",
					name : "image_upload",
					type : "text",
					width : 100,
					align : "center"
				},*/ {
					title : "Created Date",
					name : "created_date",
					type : "text",
					width : 150,
					align : "center"
				} ]
			})
		}

	});
}

function refresh_bm_images_Grid() {
	endpoint = "/v1/images/";
	$("#bmGridImages").html("")
	$.ajax({
		type : "GET",
		url : endpoint + "imagesList/BareMetal",
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			console.log("bm grid refreshed");
			images = data.images;
			$("#bmGridImages").jsGrid({

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
					title : "Modified Image Download",
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
		},
		error : function(jqXHR, exception) {
			alert("Failed to get images list");
		}
	});

}
