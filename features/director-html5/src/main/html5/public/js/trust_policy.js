function show_error_in_trust_policy_tab(message){
	
	$('#error_modal_body_trust_policy_tab').text(message);
	$("#error_modal_trust_policy_tab").modal({
		backdrop : "static"
	});
	$('body').removeClass("modal-open");
	
	
}

function show_dialog_content_trust_policy_tab(header,content){
$('#dialog_box_body_trust_policy_tab').text(header);
$('#dialog_box_content_trust_policy_tab p').text(content);
	$("#dialog_box_trust_policy_tab").modal({
		backdrop : "static"
	});
	$('body').removeClass("modal-open");

}

     
function goToVMPage() {

	$("#bm-dashboard-main-page").hide();
	$("#vm-dashboard-main-page").show();

	
	var isEmpty = !$.trim($("#vm-dashboard-main-page").html());

	if (isEmpty == false) {
		$("#vm-dashboard-main-page").html("");
	}
	hideLoading();
	$("#vm-dashboard-main-page")
			.load(
					"/v1/html5/public/director-html5/vm_images_page.html");
umount_image();
}

function umount_image(){
if(typeof current_image_id!== 'undefined'){

if(current_image_id != "" && current_image_id !=null)
	{
		var self = this;
		var mountimage = {
			"id" : current_image_id
		}

		$.ajax({
			type : "POST",
			url : "/v1/rpc/unmount-image",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : JSON.stringify(mountimage),
			success : function(data, status, xhr) {
				current_image_id = "";
			}
		});

	}

}

}

        
function goToBMPage() {

	$("#vm-dashboard-main-page").hide();
	$("#bm-dashboard-main-page").show();

	
	var isEmpty = !$.trim($("#bm-dashboard-main-page").html());

	if (isEmpty == false) {
		$("#bm-dashboard-main-page").html("");
	}
	hideLoading();
	$("#bm-dashboard-main-page")
			.load(
					"/v1/html5/public/director-html5/bm_images_page.html");
umount_image();
}
function ImageData(){
}

function refresh_vm_images_Grid() {
	var self = this;
	
	$("#vmGrid").html("")
	$.ajax({
		type : "GET",
		url : "/v1/images?deploymentType=VM",
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			console.log("vm grid refreshed");
			images = data.images;
			var grid = [];
			for (i = 0; i < images.length; i++) { 
				if(images[i].deleted){
					continue;
				}
				self.gridData = new ImageData();
				self.gridData.image_name = images[i].name;
				self.gridData.policy_name = images[i].policy_name;
				self.gridData.image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" onclick=\"deleteImage('"
					+ images[i].id + "')\"/></a>";
				
				self.gridData.trust_policy = "<div id=\"trust_policy_vm_column"
					+ images[i].id + "\">";

				if (images[i].trust_policy_draft_id == null
					&& images[i].trust_policy_id == null) {

				self.gridData.trust_policy = self.gridData.trust_policy
						+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicy('"
						+ images[i].id + "','" + images[i].name
						+ "')\"></span></a>";

				}

				if (images[i].trust_policy_draft_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicy('"
						+ images[i].id + "','" + images[i].name
						+ "')\"></span></a>";

				} else if (images[i].trust_policy_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicy('"
						+ images[i].id + "','" + images[i].name
						+ "')\"></span></a>";
				}

				if (images[i].trust_policy_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('"
						+ images[i].id + "','"
						+ images[i].trust_policy_id + "')\"></span></a>";
				}

				if (images[i].trust_policy_id != null || images[i].trust_policy_draft_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicyVM('"
						+ images[i].id + "','"
						+ images[i].trust_policy_id + "','"
						+ images[i].name + "')\"></span></a>";
				}
				self.gridData.trust_policy = self.gridData.trust_policy + "</div>";
				
				self.gridData.image_upload = "";
				if (images[i].uploads_count != 0) {
					self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" title=\"Uploaded Before\"></span></a>";
				} else {
					self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" title=\"Never Uploaded\"></span></a>";
				}

				self.gridData.image_upload += "&nbsp;"
					+ "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" onclick=\"uploadToImageStorePage('"
					+ images[i].id + "','" + images[i].name + "','"
					+ images[i].trust_policy_id + "')\" ></span></a>";

				self.gridData.created_date = images[i].created_date;

				
				
				self.gridData.image_format = images[i].image_format;
				grid.push(self.gridData);

			}
			$("#vmGrid").jsGrid({

				height : "auto",
				width : "100%",
				sorting : true,
				paging : true,
				pageSize : 10,
				pageButtonCount : 15,
				data : grid,
				fields : [ {
					title : "Delete",
					name : "image_delete",
					type : "text",
					width : 50,
					align : "center"
				}, {
					title : "Image Name",
					name : "image_name",
					type : "text",
					width : 250,
					align : "center"
				}, {
					title : "Policy Name",
					name : "policy_name",
					type : "text",
					width : 200,
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
			show_error_in_trust_policy_tab("Failed to get images list");
		}
	});

}



function refreshBMOnlineGrid() {
	var self = this;
	endpoint = "/v1/images";
	$("#bmGridOnline").html("")
	$.ajax({
		type : "GET",
		url : "/v1/images?deploymentType=BareMetalLive",
		dataType : "json",
		success : function(result) {
			images = result.images;
			var grid = [];
			for (i = 0; i < images.length; i++) { 
				if(images[i].deleted || images[i].image_format != null){
					continue;
				}
				if (images[i].trust_policy_draft_id == null
					&& images[i].trust_policy_id == null) {
					continue;
				}
				self.gridData = new ImageData();

				self.gridData.policy_name = "<div id='policy_name'" + images[i].id + ">" + images[i].policy_name + "</div>";
				
				self.gridData.trust_policy = "<div id=\"trust_policy_column"
					+ images[i].id + "\">";

				if (images[i].trust_policy_draft_id != null) {
				self.gridData.trust_policy = self.gridData.trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMLive('"
						+ images[i].id + "','" + images[i].name
						+ "')\"></span></a>";

				} else if (images[i].trust_policy_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMLive('"
						+ images[i].id + "','" + images[i].name
						+ "')\"></span></a>";
				}

				if (images[i].trust_policy_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicyAndManifest('"
						+ images[i].id + "','"
						+ images[i].trust_policy_id + "')\"></span></a>";
				}

				if (images[i].trust_policy_id != null
					|| images[i].trust_policy_draft_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicy('"
						+ images[i].id + "','"
						+ images[i].trust_policy_id + "','"
						+ images[i].name + "')\"></span></a>";
				}

				self.gridData.trust_policy = self.gridData.trust_policy + "</div>";
			
				self.gridData.image_upload = "";
				self.gridData.image_upload += "&nbsp;"
					+ "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Push To  Host\" onclick=\"pushPolicyToHost('"
					+ images[i].id + "','" + images[i].name + "','"
					+ images[i].trust_policy_id + "')\" ></span></a>";

				self.gridData.image_name = images[i].name;
				console.log("host name :: "+ images[i].name + "::" + self.gridData.image_name);

				self.gridData.created_date = images[i].created_date;
				grid.push(self.gridData);

			}
			console.log("BM GRID REFRESHED");
			$("#bmGridOnline").jsGrid({

				height : "auto",
				width : "100%",
				sorting : true,
				paging : true,
				pageSize : 10,
				pageButtonCount : 15,
				data : grid,
				fields : [ {
					title : "Trust Policy Name",
					name : "policy_name",
					type : "text",
					width : 250,
					align : "center"
				}, {
					title : "Source Host",
					name : "image_name",
					type : "text",
					width : 100,
					align : "center"
				}, {
					title : "Trust Policy",
					name : "trust_policy",
					type : "text",
					width : 100,
					align : "center"
				},/* {
					title : "Push Policy",
					name : "image_upload",
					type : "text",
					width : 100,
					align : "center"
				}, */{
					title : "Policy Created Date",
					name : "created_date",
					type : "text",
					width : 150,
					align : "center"
				} ]
			});
		}

	});
}


function refresh_bm_images_Grid() {
alert("inside bm");
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
					title : "MOdified Image Download",
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
			show_error_in_trust_policy_tab("Failed to get images list");
		}
	});

}


function refresh_all_trust_policy_grids(){
refresh_vm_images_Grid();
//refresh_bm_images_Grid();
refreshBMOnlineGrid();

}


function showLoading(){
$( "#loader_body" ).html("");
var html1="<div id='loading_icon_container' style='background-color: rgba(1, 1, 1, 0.3);bottom: 0;left: 0;position: fixed;right: 0;top: 0;text-align: center;z-index: 1;'><img id='director_loading_icon' src='/v1/html5/public/director-html5/images/ajax-loader.gif' style='position:absolute;top:30%;z-index: 1;display:none;text-align: center;'  width='75' height='75' /> </div>";   

$( "#loader_body" ).html(html1);
//$( "#loading_icon_container" ).css('text-align','center');
$( "#director_loading_icon" ).show();


}  

function hideLoading(){
$( "#director_loading_icon" ).hide();
$( "#loading_icon_container" ).hide();
$( "#director_loading_icon" ).html("");

}