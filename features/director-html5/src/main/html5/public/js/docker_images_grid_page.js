var pageInitialized = false;
$(document).ready(function() {

    if (pageInitialized == true) {
        return;
    }

    refresh_docker_Grid();

    pageInitialized = true;
});

function ImageData() {

}






function refresh_docker_Grid() {
    var self = this;

    $("#dockerGrid").html("");
    $.ajax({
        type: "GET",
        url: "/v1/images?deploymentType=Docker",
        accept: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        success: function(data, status, xhr) {
            console.log("Docker grid refreshed");
            images = data.images;
            var grid = [];
            for (i = 0; i < images.length; i++) {
                if (images[i].deleted) {
                    continue;
                }

                self.gridData = new ImageData();
                self.gridData.image_name = images[i].image_name;
                self.gridData.policy_name = images[i].policy_name;
				
				var deleteCallArr = "['deleteImageDocker', 'Are you sure you want to delete the image?', '" + images[i].id + "']";
                self.gridData.image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" id=\"docker_remove_row_" + i + "\" onclick=\"confirmDeleteOperation("+deleteCallArr +")\"/></a>";
                if (images[i].image_upload_status == 'Complete' || images[i].image_upload_status == 'In Progress') {

                    self.gridData.trust_policy = "<div id=\"trust_policy_docker_column" + images[i].id + "\">";
                    if ((images[i].image_upload_status == 'Complete') && (images[i].trust_policy_draft_id == null && images[i].trust_policy_id == null)) {

                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" id=\"docker_add_row_" + i + "\" onclick=\"createPolicyDocker('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";

                    }

                    if (images[i].trust_policy_draft_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\" id=\"docker_edit_row_" + i + "\" onclick=\"editPolicyDocker('" + images[i].id + "','" + images[i].image_name + "','" + images[i].trust_policy_draft_id + "')\"></span></a>";
                        tpdid = images[i].trust_policy_draft_id;
                    } else if (images[i].trust_policy_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" id=\"docker_edit_row_" + i + "\" onclick=\"editPolicyDocker('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                    }

                    if (images[i].trust_policy_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\" id=\"docker_download_row_" + i + "\"  title=\"Download\" onclick=\"downloadPolicy('" + images[i].id + "','" + images[i].trust_policy_id + "')\"></span></a>";
                    }

                    if (images[i].trust_policy_id != null || images[i].trust_policy_draft_id != null) {
						var deleteCallArr = "['deletePolicyDocker', 'Are you sure you want to delete policy?', '" + images[i].trust_policy_id + "', '" + images[i].trust_policy_draft_id + "', '" + images[i].id + "', '" + images[i].image_name + "']"; 
                        self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\" id=\"docker_delete_row_" + i + "\"  title=\"Delete Policy\" onclick=\"confirmDeleteOperation(" + deleteCallArr + ")\"></span></a>";
                    }
                    self.gridData.trust_policy = self.gridData.trust_policy + "</div>";
                } else {
                    //self.gridData.image_delete = "";
                    self.gridData.trust_policy = "";
                }

                self.gridData.image_upload = "";
                if (images[i].image_upload_status == 'Complete') {
	
		     if(images[i].action_entry_created){
			 self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-cloud-upload\" id=\"vm_ok_row_" + i + "\" title=\"Upload Action History\"  onclick=\"showImageActionHistoryDialog('" + images[i].id + "')\" ></span></a>";

		    }
	

                    self.gridData.image_upload += "&nbsp;" + "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" id=\"docker_upload_row_" + i + "\" onclick=\"uploadToImageStoreDockerPage('" + images[i].id + "','" + images[i].image_name + "','" + images[i].trust_policy_id + "')\" ></span></a>";
                }

                self.gridData.created_date = images[i].created_date;

                self.gridData.tag = images[i].tag;
                self.gridData.repository = images[i].repository;
                self.gridData.image_format = images[i].image_format;
                grid.push(self.gridData);

            }
            $("#dockerGrid").jsGrid({

                height: "auto",
                width: "100%",
                sorting: true,
                paging: true,
                pageSize: 10,
                pageButtonCount: 15,
                data: grid,
                fields: [{
                    title: "Delete",
                    name: "image_delete",
                    type: "text",
                    width: 60,
                    align: "center"
                }, {
                    title: "Image Name",
                    name: "image_name",
                    type: "text",
                    width: 240,
                    align: "center"
                }, {
                    title: "Policy Name",
                    name: "policy_name",
                    type: "text",
                    width: 190,
                    align: "center"
                }, {
                    title: "Repository",
                    name: "repository",
                    type: "text",
                    width: 200,
                    align: "center"
                }, {
                    title: "Tag",
                    name: "tag",
                    type: "text",
                    width: 200,
                    align: "center"
                }, {
                    title: "Trust Policy",
                    name: "trust_policy",
                    type: "text",
                    width: 120,
                    align: "center"
                }, {
                    title: "Image Store Upload",
                    name: "image_upload",
                    type: "text",
                    width: 120,
                    align: "center"
                }, {
                    title: "Created Date",
                    name: "created_date",
                    type: "text",
                    width: 120,
                    align: "center"
				} ],
				onRefreshed: function(args) {
					if(grid.length <= 0){
						return;
					}
					var numOfPagesWithDecimal = grid.length/args.grid.pageSize;
					var numOfPages = Math.ceil(numOfPagesWithDecimal);
					if(args.grid.pageIndex > numOfPages){
						args.grid.reset();
					}	
						
				}
            });
            var delay = 1000;
            setTimeout(function() {
                $("#dockerGrid").show();
                $("#dockerGrid").jsGrid("refresh");
            }, delay);
            $("#dockerGrid").jsGrid("sort", { field: "image_name", order: "asc" });

        },
        error: function(jqXHR, exception) {

            show_error_in_trust_policy_tab("Failed to get images list");


        }
    });

}