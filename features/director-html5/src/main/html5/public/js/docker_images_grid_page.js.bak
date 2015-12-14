var endpoint = "/v1/images/";
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
            var tpid = "";
            var tpdid = "";
            for (i = 0; i < images.length; i++) {
                if (images[i].deleted) {
                    continue;
                }

                tpid = "";
                tpdid = "";

                self.gridData = new ImageData();
                self.gridData.image_name = images[i].image_name;
                self.gridData.policy_name = images[i].policy_name;
                self.gridData.image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" onclick=\"deleteImageDocker('" + images[i].id + "')\"/></a>";

                self.gridData.trust_policy = "<div id=\"trust_policy_docker_column" + images[i].id + "\">";

                if (images[i].trust_policy_draft_id == null && images[i].trust_policy_id == null) {

                    self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicyDocker('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";

                }

                if (images[i].trust_policy_draft_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyDocker('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                    tpdid = images[i].trust_policy_draft_id;
                } else if (images[i].trust_policy_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyDocker('" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                }

                if (images[i].trust_policy_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('" + images[i].id + "','" + images[i].trust_policy_id + "')\"></span></a>";
                }

                if (images[i].trust_policy_id != null || images[i].trust_policy_draft_id != null) {
                    self.gridData.trust_policy = self.gridData.trust_policy + "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicyDocker('" + images[i].trust_policy_id + "','" + images[i].trust_policy_draft_id + "','" + images[i].id + "','" + images[i].image_name + "')\"></span></a>";
                }
                self.gridData.trust_policy = self.gridData.trust_policy + "</div>";

                self.gridData.image_upload = "";
                if (images[i].uploads_count != 0) {
                    self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" title=\"Uploaded Before\"></span></a>";
                } else {
                    self.gridData.image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" title=\"Never Uploaded\"></span></a>";
                }

                self.gridData.image_upload += "&nbsp;" + "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" onclick=\"uploadToImageStoreDockerPage('" + images[i].id + "','" + images[i].image_name + "','" + images[i].trust_policy_id + "')\" ></span></a>";

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