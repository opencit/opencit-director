function showImageActionHistoryDialog(imageid){
	
	var functionToCall = "show_action_history_grid('" + imageid + "')";

	$("#image_action_history_title").html("Image Actions History");
	$("#image_action_history_refresh").attr("onclick",functionToCall);
	$('#image_action_history').modal('show');
	show_action_history_grid(imageid);
	
}


function HistoryData(){
	
}

function show_action_history_grid(imageid){
	

    var self = this;

    $("#image_action_history_grid").html("")
    $.ajax({
        type: "GET",
        url: "/v1/image-actions/history/"+imageid,
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
		self.gridData.datetime= actionHistoryList[i].datetime;

 		self.gridData.artifact_name = actionHistoryList[i].artifact_name;
	

               self.gridData.store_names = actionHistoryList[i].store_names;

                ///alert("self.gridData.artifact_name::"+self.gridData.artifact_name);


                grid.push(self.gridData);

            }
            
            $("#image_action_history_grid").jsGrid({

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
            var delay = 300;
            setTimeout(function() {
                $("#image_action_history_grid").show();
                $("#image_action_history_grid").jsGrid("refresh");
            }, delay);

        },
        error: function(jqXHR, exception) {

            show_error_in_trust_policy_tab("Failed to get action history list");


        }
    });


  	
	
}


function show_error_in_trust_policy_tab(message) {

    $('#error_modal_body_trust_policy_tab').text(message);
    $("#error_modal_trust_policy_tab").modal({
        backdrop: "static"
    });
    $('body').removeClass("modal-open");

}

function show_dialog_content_trust_policy_tab() {
    $('#dialog_box_body_trust_policy_tab').text("Select Files/Directories Help");
    $('#dialog_box_content_trust_policy_tab').html("Trust Policies are composed of a selection of one or more explicit files, and may also include one or more general rules applied to a folder through include/exclude regular expressions.<br/> <br/> Select explicit files and folders by putting a check in the checkbox.  If an individual file is selected, that specific file will be added to the Trust Policy.  If a directory is selected, that directory and all files and directories contained in the selected directory will be added explicitly to the Trust Policy.<br/> <br/> Explicitly added files will always be included in the attestation of the machine governed by the Policy, but new files may be added without changing the resulting Trust Status.<br/>  <br/> The &quot;lock&quot; icon next to each directory allows general rules to be applied to the folder using regular expressions.  These rules can control what files will be measured in a directory, even if they are added later.  For example, by locking a folder with the &quot;include&quot; expression &quot;*&quot; all files in the folder will be measured, including files added later.  Creating a new file in this folder will result in the protected host becoming &quot;Untrusted&quot; after its next reboot.<br/> <br/> Adding an &quot;exclude&quot; filter will exclude any files matching the &quot;exclude&quot; expression.  &quot;Exclude&quot; filters are applied after &quot;include&quot; filters, meaning a folder with an &quot;include&quot; filter of &quot;*&quot; and an exclude filter of &quot;*.txt&quot; will measure all files in the folder, with the exception of any files whose names contain the string &quot;*.txt&quot;.<br/> <br/> Folder &quot;lock&quot; rules can be applied to an individual folder, or can be applied recursively to all subfolders as well.<br/> Any combination of explicitly selected files and directory &quot;lock&quot; filters can be used, including within the same folder.<br/> <br/> Explicitly selected files are measured separately from &quot;lock&quot; filters.  If a file is selected explicitly in a &quot;locked&quot; folder, and the &quot;lock&quot; filters would normally not include the explicitly selected file in the attestation measurement, the file will still be measured and attested.<br/> <br/> IMPORTANT NOTE: Ensure that files are measured only if they are not expected to change between reboots or, in the case of VM Trust Policies, that the files will not change between the source image and any instances.  As an example, for a database workload, select the database executables, libraries, and configuration files, but do not select the actual database contents. ");
    $("#dialog_box_trust_policy_tab").modal({
        backdrop: "static"
    });
    $('body').removeClass("modal-open");

}

function show_dialog_content_trust_policy_tab_message(modalTitle, modalMessage){
    $('#dialog_box_body_trust_policy_tab').text(modalTitle);
    $('#dialog_box_content_trust_policy_tab').html(modalMessage);
    $("#dialog_box_trust_policy_tab").modal({
        backdrop: "static"
    });
    $('body').removeClass("modal-open");	
}

function goToVMPage() {

    $("#docker-dashboard-main-page").hide();
    $("#bm-dashboard-main-page").hide();
    $("#vm-dashboard-main-page").show();

    var isEmpty = !$.trim($("#vm-dashboard-main-page").html());

    if (isEmpty == false) {
        $("#vm-dashboard-main-page").html("");
    }
    hideLoading();
    $("#vm-dashboard-main-page").load(
        "/v1/html5/public/director-html5/vm_images_page.html");
    umount_image();
}

function goToDockerPage() {

    $("#bm-dashboard-main-page").hide();
    $("#vm-dashboard-main-page").hide();
    $("#docker-dashboard-main-page").show();

    var isEmpty = !$.trim($("#docker-dashboard-main-page").html());

    if (isEmpty == false) {
        $("#docker-dashboard-main-page").html("");
    }

    $("#docker-dashboard-main-page").load(
        "/v1/html5/public/director-html5/docker_images_page.html");

    umount_image();
}

function umount_image() {
    if (typeof current_image_id !== 'undefined') {

        if (current_image_id != "" && current_image_id != null) {
            var self = this;
            var mountimage = {
                "id": current_image_id
            }

            $.ajax({
                type: "POST",
                url: "/v1/rpc/unmount-image",
                contentType: "application/json",
                headers: {
                    'Accept': 'application/json'
                },
                data: JSON.stringify(mountimage),
                success: function(data, status, xhr) {
                    current_image_id = "";
                }
            });

        }

    }

}

function goToBMPage() {
    $("#docker-dashboard-main-page").hide();
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

function ImageData() {}

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
                self.gridData.image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" onclick=\"deleteImage('" + images[i].id + "')\"/></a>";
                if (images[i].status == 'Complete') {

                    self.gridData.trust_policy = "<div id=\"trust_policy_vm_column" + images[i].id + "\">";

                    if (images[i].trust_policy_draft_id == null && images[i].trust_policy_id == null) {

                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createTrustPolicy('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";

                    }

                    if (images[i].trust_policy_draft_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editTrustPolicy('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                        tpdid = images[i].trust_policy_draft_id;
                    } else if (images[i].trust_policy_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editTrustPolicy('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                    }

                    if (images[i].trust_policy_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('" + images[i].id + "','" + images[i].trust_policy_id + "')\"></span></a>";
                    }

                    if (images[i].trust_policy_id != null || images[i].trust_policy_draft_id != null) {
                        self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicyVM('" + images[i].trust_policy_id + "','" + images[i].trust_policy_draft_id + "','" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                    }
                    self.gridData.trust_policy = self.gridData.trust_policy + "</div>";

                } else {
                    //self.gridData.image_delete = "";
                    self.gridData.trust_policy = "";
                }

                self.gridData.image_upload = "";
                if (images[i].status == 'Complete') {

                    if (images[i].image_uploads_count != 0 || images[i].policy_uploads_count != 0  ) {
                        self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" title=\"Uploaded Before\"></span></a>";
                    } else {
                        self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" title=\"Never Uploaded\"></span></a>";
                    }

                    self.gridData.image_upload += "&nbsp;" + "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" onclick=\"uploadToImageStorePage('" + images[i].id + "','" + images[i].image_name + "','" + images[i].trust_policy_id + "')\" ></span></a>";
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
				}]
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



function refreshBMOnlineGrid() {
    var self = this;
    endpoint = "/v1/images";
    $("#bmGridOnline").html("")
    $.ajax({
        type: "GET",
        url: "/v1/images?deploymentType=BareMetal",
        dataType: "json",
        success: function(result) {
            images = result.images;
            var grid = [];
            for (i = 0; i < images.length; i++) {
                if (images[i].deleted || images[i].image_format != null) {
                    continue;
                }
                if (images[i].trust_policy_draft_id == null && images[i].trust_policy_id == null) {
                    continue;
                }
                self.gridData = new ImageData();

                self.gridData.policy_name = "<div id='policy_name'" + images[i].id + ">" + images[i].policy_name + "</div>";

                self.gridData.trust_policy = "<div id=\"trust_policy_column" + images[i].id + "\">";

                if (images[i].trust_policy_draft_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMLive('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";

                } else if (images[i].trust_policy_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMLive('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                }

                if (images[i].trust_policy_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicyAndManifest('" + images[i].id + "','" + images[i].trust_policy_id + "')\"></span></a>";
                }

                if (images[i].trust_policy_id != null || images[i].trust_policy_draft_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicy('" + images[i].id + "','" + images[i].trust_policy_id + "','" + images[i].image_name + "')\"></span></a>";
                }

                self.gridData.trust_policy = self.gridData.trust_policy + "</div>";

                self.gridData.image_upload = "";
                self.gridData.image_upload += "&nbsp;" + "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Push To  Host\" onclick=\"pushPolicyToHost('" + images[i].id + "','" + images[i].image_name + "','" + images[i].trust_policy_id + "')\" ></span></a>";

                self.gridData.image_name = images[i].image_name;
                console.log("host name :: " + images[i].image_name + "::" + self.gridData.image_name);

                self.gridData.created_date = images[i].created_date;
                grid.push(self.gridData);

            }
            console.log("BM GRID REFRESHED");
            $("#bmGridOnline").jsGrid({

                height: "auto",
                width: "100%",
                sorting: true,
                paging: true,
                pageSize: 10,
                pageButtonCount: 15,
                data: grid,
                fields: [{
                        title: "Trust Policy Name",
                        name: "policy_name",
                        type: "text",
                        width: 250,
                        align: "center"
				}, {
                        title: "Source Host",
                        name: "image_name",
                        type: "text",
                        width: 100,
                        align: "center"
				}, {
                        title: "Trust Policy",
                        name: "trust_policy",
                        type: "text",
                        width: 100,
                        align: "center"
				},
                    /* {
                    					title : "Push Policy",
                    					name : "image_upload",
                    					type : "text",
                    					width : 100,
                    					align : "center"
                    				}, */
                    {
                        title: "Policy Created Date",
                        name: "created_date",
                        type: "text",
                        width: 150,
                        align: "center"
				}]
            });
        }

    });
}

function refresh_bm_images_Grid() {
    alert("inside bm");
    endpoint = "/v1/images/";
    $("#bmGridImages").html("")
    $.ajax({
        type: "GET",
        url: endpoint + "imagesList/BareMetal",
        accept: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        success: function(data, status, xhr) {
            console.log("bm grid refreshed");
            images = data.images;
            $("#bmGridImages").jsGrid({

                height: "auto",
                width: "100%",
                sorting: true,
                paging: true,
                pageSize: 10,
                pageButtonCount: 15,
                data: images,
                fields: [{
                    title: "Image Name",
                    name: "image_name",
                    type: "text",
                    width: 250,
                    align: "center"
				}, {
                    title: "Image Format",
                    name: "image_format",
                    type: "text",
                    width: 120,
                    align: "center"
				}, {
                    title: "Trust Policy",
                    name: "trust_policy",
                    type: "text",
                    width: 100,
                    align: "center"
				}, {
                    title: "MOdified Image Download",
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
				}]
            });
        },
        error: function(jqXHR, exception) {
            show_error_in_trust_policy_tab("Failed to get images list");
        }
    });

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
                self.gridData.image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" id=\"docker_remove_row_" + i + "\" onclick=\"deleteImageDocker('" + images[i].id + "')\"/></a>";
                if (images[i].image_upload_status == 'Complete') {

                    self.gridData.trust_policy = "<div id=\"trust_policy_docker_column" + images[i].id + "\">";
                    if (images[i].trust_policy_draft_id == null && images[i].trust_policy_id == null) {

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
                        self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\" id=\"docker_delete_row_" + i + "\"  title=\"Delete Policy\" onclick=\"deletePolicyDocker('" + images[i].trust_policy_id + "','" + images[i].trust_policy_draft_id + "','" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                    }
                    self.gridData.trust_policy = self.gridData.trust_policy + "</div>";
                } else {
                    //self.gridData.image_delete = "";
                    self.gridData.trust_policy = "";
                }

                self.gridData.image_upload = "";
                if (images[i].image_upload_status == 'Complete') {

                    if (images[i].image_uploads_count != 0 || images[i].policy_uploads_count != 0) {
                        self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" id=\"docker_ok_row_" + i + "\" title=\"Uploaded Before\"></span></a>";
                    } else {
                        self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" id=\"docker_minus_row_" + i + "\" title=\"Never Uploaded\"></span></a>";
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
                }, {
                    title: "Repository",
                    name: "repository",
                    type: "text",
                    width: 120,
                    align: "center"
                }, {
                    title: "Tag",
                    name: "tag",
                    type: "text",
                    width: 120,
                    align: "center"
                }, {
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
                }]
            });
            var delay = 1000;
            setTimeout(function() {
                $("#dockerGrid").show();
                $("#dockerGrid").jsGrid("refresh");
            }, delay);

        },
        error: function(jqXHR, exception) {

            show_error_in_trust_policy_tab("Failed to get images list");
        }
    });

}

function refresh_all_trust_policy_grids() {
    refresh_vm_images_Grid();
    // refresh_bm_images_Grid();
    refreshBMOnlineGrid();
    refresh_docker_Grid();
}

function showLoading() {
    $("#loader_body").html("");
    var html1 = "<div id='loading_icon_container' style='background-color: rgba(1, 1, 1, 0.3);bottom: 0;left: 0;position: fixed;right: 0;top: 0;text-align: center;z-index: 1;'><img id='director_loading_icon' src='/v1/html5/public/director-html5/images/ajax-loader.gif' style='position:absolute;top:30%;left:48%;z-index: 1;display:none;text-align: center;'  width='75' height='75' /> </div>";

    $("#loader_body").html(html1);
    $("#director_loading_icon").show();

}

function hideLoading() {
    $("#director_loading_icon").hide();
    $("#loading_icon_container").hide();
    $("#director_loading_icon").html("");

}

function confirmDeleteOperation(deleteCallArr){
	var funcName = deleteCallArr[0];

	var args = "";
	if(deleteCallArr.length > 2){
		for(var i=2;i<deleteCallArr.length;i++){
			args=args + "'"+deleteCallArr[i]+"',";			
		}
		args = args.substring(0, args.length-1);
	}
	var func = funcName+"("+args+")";
	$("#confirm_delete").attr("onclick", func);
	$("#delete_confirmation_window_text").text(deleteCallArr[1]);
	$("#delete_confirmation_window").modal('show');
}

function initiate_download(imageid) {
	console.log($("#artifact_to_download").val());
	if($("#artifact_to_download").val() == "Policy"){
		downloadPolicy(imageid);
	} else if($("#artifact_to_download").val() == "Manifest") {
		downloadManifest(imageid);
	} else if($("#artifact_to_download").val() == "ManifestAndPolicy") {
		downloadPolicyAndManifest(imageid,null);
	}
}

function cleanup_orphan_policies() {
	$.ajax({
		type : "POST",
		url : "/v1/rpc/remove-orphan-policies",
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			show_error_in_trust_policy_tab("Orphan policies cleaned up");
		},
		error : function(data, status, xhr) {
            var obj = jQuery.parseJSON(data.responseText);
			show_error_in_trust_policy_tab(obj.error);
		} 
	});
}


function showDownloadModal(imageid,trustpolicyid) {
	var func = "initiate_download('" + imageid + "')";
	$("#artifact_to_download_button").attr("onclick",func)
	$("#download_confirmation_modal").modal('show');
}


function downloadPolicy(imageid, trust_policy_id) {

	var token_request_json = "{ \"data\": [ { \"not_more_than\": 1} ] }";

	$.ajax({
		type : "POST",
		url : "/v1/login/tokens",
		accept : "application/json",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		data : token_request_json,
		success : function(data, status, xhr) {
			var authtoken = authtoken = data.data[0].token;
			var url = "/v1/images/" + imageid
					+ "/downloads/policy?Authorization="
					+ encodeURIComponent(authtoken);

			window.location = url;
		},
		error : function(xhr, status, errorMessage) {
			show_error_in_trust_policy_tab("error in downloading");
		}
	});

}

function downloadManifest(imageid) {

	var token_request_json = "{ \"data\": [ { \"not_more_than\": 1} ] }";

	$.ajax({
		type : "POST",
		url : "/v1/login/tokens",
		accept : "application/json",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		data : token_request_json,
		success : function(data, status, xhr) {
			var authtoken = authtoken = data.data[0].token;
			var url = "/v1/images/" + imageid
					+ "/downloads/manifest?Authorization="
					+ encodeURIComponent(authtoken);

			window.location = url;
		},
		error : function(xhr, status, errorMessage) {
			show_error_in_trust_policy_tab("error in downloading");
		}
	});

}

function downloadImage(imageid) {

	var token_request_json = "{ \"data\": [ { \"not_more_than\": 1} ] }";

	$.ajax({
		type : "POST",
		url : "/v1/login/tokens",
		accept : "application/json",
		contentType : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		data : token_request_json,
		success : function(data, status, xhr) {
			var authtoken = authtoken = data.data[0].token;
			var uri = "/v1/images/" + imageid
					+ "/downloads/image?modified=true&Authorization="
					+ encodeURIComponent(authtoken);

			window.location = uri;

		},
		error : function(xhr, status, errorMessage) {
			show_error_in_trust_policy_tab("error in downloading");
		}
	});

}

function downloadPolicyAndManifest(imageid, trust_policy_id) {

var token_request_json="{ \"data\": [ { \"not_more_than\": 1} ] }";

	$.ajax({
            type: "POST",
            url: "/v1/login/tokens",
            accept: "application/json",
            contentType: "application/json",
            headers: {'Accept': 'application/json'},
     		data: token_request_json,
            success: function(data, status, xhr) {
		var authtoken= authtoken=data.data[0].token;
		var url="/v1/images/" + imageid + "/downloads/policyAndManifest?Authorization="+encodeURIComponent(authtoken);
		window.location = url;                  
            },
            error: function(xhr, status, errorMessage) {
              alert("error in downloading");
            }
    });
}