var pageInitialized = false;
$(document).ready(function() {

    if (pageInitialized == true) {
        return;
    }

    refresh_vm_images_Grid();

    pageInitialized = true;
});

function ImageData() {

}

function showImageActionHistoryDialog(imageid) {

    $("#image_action_history_title").html("Image Actions History");

    $('#image_action_history').modal('show');
    show_vm_action_history_grid(imageid);

}


function HistoryData() {

}

function show_vm_action_history_grid(imageid) {


    var self = this;

    $("#vm_image_action_history_grid").html("")
    $.ajax({
        type: "GET",
        url: "/v1/image-actions/history/" + imageid,
        accept: "application/json",
        headers: {
            'Accept': 'application/json'
        },

        success: function(data, status, xhr) {

            console.log("Action History grid refreshed");
            actionHistoryList = data.image_action_history_list;
            var grid = [];

            for (i = 0; i < actionHistoryList.length; i++) {


                self.gridData = new HistoryData();
                self.gridData.artifact_name = actionHistoryList[i].artifact_name;
                self.gridData.execution_status = actionHistoryList[i].execution_status;
                self.gridData.datetime = actionHistoryList[i].datetime;

                self.gridData.artifact_name = actionHistoryList[i].artifact_name;


                self.gridData.store_names = actionHistoryList[i].store_names;

                ///alert("self.gridData.artifact_name::"+self.gridData.artifact_name);


                grid.push(self.gridData);

            }

            $("#vm_image_action_history_grid").jsGrid({

                height: "auto",
                width: "100%",
                sorting: true,
                paging: true,
                pageSize: 5,
                pageButtonCount: 15,
                data: grid,
                fields: [{
                        title: "Artifact",
                        name: "artifact_name",
                        type: "text",
                        width: 150,
                        align: "center"
				}, {
                        title: "Status",
                        name: "execution_status",
                        type: "text",
                        width: 230,
                        align: "center"
				}, {
                        title: "Store",
                        name: "store_names",
                        type: "text",
                        width: 100,
                        align: "center"
				},

                    {
                        title: "ExecutionDate",
                        name: "datetime",
                        type: "text",
                        width: 150,
                        align: "center"
					}],
                onRefreshed: function(args) {
                    if (grid.length <= 0) {
                        return;
                    }
                    var numOfPagesWithDecimal = grid.length / args.grid.pageSize;
                    var numOfPages = Math.ceil(numOfPagesWithDecimal);
                    if (args.grid.pageIndex > numOfPages) {
                        args.grid.reset();
                    }

                }
            });
            var delay = 300;
            setTimeout(function() {
                $("#vm_image_action_history_grid").show();
                $("#vm_image_action_history_grid").jsGrid("refresh");
            }, delay);

        },
        error: function(jqXHR, exception) {

            show_error_in_trust_policy_tab("Failed to get action history list");


        }
    });




}


function refresh_vm_images_Grid() {
    var self = this;

    $("#vmGrid").html("")
    $.ajax({
        type: "GET",
        url: "/v1/images?deploymentType=VM",
        accept: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        success: function(data, status, xhr) {
            console.log("vm grid refreshed");
            images = data.images;
            var grid = [];
            for (i = 0; i < images.length; i++) {
                if (images[i].deleted) {
                    continue;
                }

                self.gridData = new ImageData();
                self.gridData.image_name = images[i].image_name;
                self.gridData.policy_name = images[i].policy_name;
                self.gridData.image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" id=\"vm_remove_row_" + i + "\" onclick=\"deleteImage('" + images[i].id + "')\"/></a>";
                if (images[i].image_upload_status == 'Complete') {

                    self.gridData.trust_policy = "<div id=\"trust_policy_vm_column" + images[i].id + "\">";
                    if (images[i].trust_policy_draft_id == null && images[i].trust_policy_id == null) {

                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" id=\"vm_add_row_" + i + "\" onclick=\"createTrustPolicy('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";

                    }

                    if (images[i].trust_policy_draft_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\" id=\"vm_edit_row_" + i + "\" onclick=\"editTrustPolicy('" + images[i].id + "','" + images[i].image_name + "','" + images[i].trust_policy_draft_id + "')\"></span></a>";
                        tpdid = images[i].trust_policy_draft_id;
                    } else if (images[i].trust_policy_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" id=\"vm_edit_row_" + i + "\" onclick=\"editTrustPolicy('" + images[i].id + "','" + images[i].image_name + "','')\"></span></a>";
                    }

                    if (images[i].trust_policy_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\" id=\"vm_download_row_" + i + "\"  title=\"Download\" onclick=\"downloadPolicy('" + images[i].id + "','" + images[i].trust_policy_id + "')\"></span></a>";
                    }

                    if (images[i].trust_policy_id != null || images[i].trust_policy_draft_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\" id=\"vm_delete_row_" + i + "\"  title=\"Delete Policy\" onclick=\"deletePolicyVM('" + images[i].trust_policy_id + "','" + images[i].trust_policy_draft_id + "','" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";

                    }
                    self.gridData.trust_policy = self.gridData.trust_policy + "</div>";

                } else {
                    //self.gridData.image_delete = "";
                    self.gridData.trust_policy = "";
                }

                self.gridData.image_upload = "";
                if (images[i].image_upload_status == 'Complete') {

                    if (images[i].image_uploads_count != 0 || images[i].policy_uploads_count != 0) {
                        self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" id=\"vm_ok_row_" + i + "\" title=\"Uploaded Before\"  onclick=\"showImageActionHistoryDialog('" + images[i].id + "')\" ></span></a>";
                    } else {
                        self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" id=\"vm_minus_row_" + i + "\" title=\"Never Uploaded\"></span></a>";
                    }

                    self.gridData.image_upload += "&nbsp;" + "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" id=\"vm_upload_row_" + i + "\" onclick=\"uploadToImageStorePage('" + images[i].id + "','" + images[i].image_name + "','" + images[i].trust_policy_id + "')\" ></span></a>";
                }

                self.gridData.created_date = images[i].created_date;



                self.gridData.image_format = images[i].image_format;
                grid.push(self.gridData);

            }
            $("#vmGrid").jsGrid({

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
                        width: 50,
                        align: "center"
				}, {
                        title: "Image Name",
                        name: "image_name",
                        type: "text",
                        width: 250,
                        align: "center"
				}, {
                        title: "Policy Name",
                        name: "policy_name",
                        type: "text",
                        width: 200,
                        align: "center"
				},
                    /* {
                    					title : "Image Format",
                    					name : "image_format",
                    					type : "text",
                    					width : 120,
                    					align : "center"
                    				},*/
                    {
                        title: "Trust Policy",
                        name: "trust_policy",
                        type: "text",
                        width: 100,
                        align: "center"
				}, {
                        title: "Image Store Upload",
                        name: "image_upload",
                        type: "text",
                        width: 100,
                        align: "center"
				}, {
                        title: "Created Date",
                        name: "created_date",
                        type: "text",
                        width: 150,
                        align: "center"
				}],
                onRefreshed: function(args) {
                    if (grid.length <= 0) {
                        return;
                    }
                    var numOfPagesWithDecimal = grid.length / args.grid.pageSize;
                    var numOfPages = Math.ceil(numOfPagesWithDecimal);
                    if (args.grid.pageIndex > numOfPages) {
                        args.grid.reset();
                    }

                }
            });
            var delay = 1000;
            setTimeout(function() {
                $("#vmGrid").show();
                $("#vmGrid").jsGrid("refresh");
            }, delay);

        },
        error: function(jqXHR, exception) {

            show_error_in_trust_policy_tab("Failed to get images list");


        }
    });

}