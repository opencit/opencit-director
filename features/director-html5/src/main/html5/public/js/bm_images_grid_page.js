var pageInitialized = false;
$(document).ready(function() {

	if (pageInitialized == true)
		return;

	refreshBMOnlineGrid();
	//refresh_bm_images_Grid();

	pageInitialized = true;
});
function addHost() {

current_image_id='';
	goToCreatePolicyWizardForBMLive();
}
function ImageData(){
	
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
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" id=\"bmlive_edit_row_"+i+"\" title=\"Edit Policy\"  onclick=\"editPolicyForBMLive('"
						+ images[i].id + "','" + images[i].image_name
						+ "','"+images[i].trust_policy_draft_id+"')\"></span></a>";

				} else if (images[i].trust_policy_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\" id=\"bmlive_edit_row_"+i+"\"  title=\"Edit Policy\" onclick=\"editPolicyForBMLive('"
						+ images[i].id + "','" + images[i].image_name
						+ "','')\"></span></a>";
				}

				if (images[i].trust_policy_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\" id=\"bmlive_download_row_"+i+"\"  title=\"Download\" onclick=\"downloadPolicyAndManifest('"
						+ images[i].id + "','"
						+ images[i].trust_policy_id + "')\"></span></a>";
				}

				if (images[i].trust_policy_id != null || images[i].trust_policy_draft_id != null) {
					self.gridData.trust_policy = self.gridData.trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\" id=\"bmlive_delete_row_"+i+"\"  title=\"Delete Policy\" onclick=\"deletePolicyBM('"
						+ images[i].id + "')\"></span></a>";
				}

				self.gridData.trust_policy = self.gridData.trust_policy + "</div>";
			
				self.gridData.image_upload = "";
				self.gridData.image_upload += "&nbsp;"
					+ "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" id=\"bmlive_push_row_"+i+"\" title=\"Push To  Host\" onclick=\"pushPolicyToHost('"
					+ images[i].id + "','" + images[i].image_name + "','"
					+ images[i].trust_policy_id + "')\" ></span></a>";

				self.gridData.image_name = images[i].image_name;
				console.log("host name :: "+ images[i].image_name + "::" + self.gridData.image_name);

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
			show_error_trust_policy_tab("Failed to get images list");
		}
	});

}
