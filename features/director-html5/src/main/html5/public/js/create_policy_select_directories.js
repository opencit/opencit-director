var endpoint = "/v1/images/";

function SelectDirectoriesMetaData(data) {

    this.imageid = current_image_id;

}

function SelectDirectoriesViewModel() {
    var self = this;

    self.selectDirectoriesMetaData = new SelectDirectoriesMetaData({});

    self.selectDirectoriesSubmit = function(loginFormElement) {
        $("#createVmPolicyDirNext").prop('disabled', true);
        clearInterval(refreshIntervalId);
        navButtonClicked = true;
        editPolicyDraft();
    }

};

function ApplyRegexMetaData(data) {
    this.sel_dir = ko.observable("");
    this.dir_path = ko.observable();
    this.create_policy_regex_exclude = ko.observable("");
    this.create_policy_regex_include = ko.observable("");
    this.create_policy_regex_includeRecursive = ko.observable("");

    this.selected_image_format = ko.observable();

}

function ApplyRegExViewModel() {
    var self = this;

    self.applyRegexMetaData = new ApplyRegexMetaData({});

    self.resetRegEx = function(event) {
        var sel_dir = $("#sel_dir").val();
        var node = $("input[name='directory_" + sel_dir + "']");
        console.log(node.attr("name"));
        var config = {
            root: '/',
            dir: sel_dir,
            script: '/v1/images/' + current_image_id + '/search',
            expandSpeed: 1000,
            collapseSpeed: 1000,
            multiFolder: true,
            loadMessage: "Loading...",
            init: false,
            filesForPolicy: false,
            reset_regex: true
        };

        var len = node.parent().children().length;
        var counter = 0;
        node.parent().children().each(function() {
            console.log("removing children");
            if (counter++ > 2) {
                $(this).remove();
            }
        });

        node.parent().removeClass('collapsed').addClass('expanded').removeClass('selected');

        $("i[id='toggle_" + sel_dir + "']").attr("class", "fa fa-unlock");
        $("i[id='toggle_" + sel_dir + "']").attr("style", "color: blue; font-size : 1.6em");

        node.attr('checked', false);

        (node.parent()).fileTree(config, function(file, checkedStatus,
            rootRegexDir) {
            editPatch(file, checkedStatus, rootRegexDir);
        });

        node.removeAttr("rootregexdir");
        node.removeAttr("include");
        node.removeAttr("exclude");
        node.removeAttr("recursive");
        //hide the ApplyRegex panel
        closeRegexPanel();
    }

    self.applyRegEx = function(loginFormElement) {
        var include = loginFormElement.create_policy_regex_include.value;
        var includeRecursive = loginFormElement.create_policy_regex_includeRecursive.checked;
        var exclude = loginFormElement.create_policy_regex_exclude.value;
        console.log(include + "-- " + exclude + " -- " + includeRecursive);
        if ((include == "" || include == null || include == undefined) && (exclude == "" || exclude == null || exclude == undefined)) {
            $("#regex_error_vm").html("<font color='red'>Provide atleast one filter</font>");
            return;
        }
        $("#regex_error_vm").html("");
        var sel_dir = loginFormElement.sel_dir.value.trim();

        var node = $("input[name='directory_" + sel_dir + "']");
        console.log(sel_dir);
        var config = {
            root: '/',
            dir: sel_dir,
            script: '/v1/images/' + current_image_id + '/search',
            expandSpeed: 1000,
            collapseSpeed: 1000,
            multiFolder: true,
            loadMessage: "Loading...",
            init: false,
            filesForPolicy: false,
            recursive: true,
            include: include,
            include_recursive: includeRecursive,
            exclude: exclude
        };
        var len = node.parent().children().length;
        var counter = 0;
        node.parent().children().each(function() {
            if (counter++ > 2) {
                $(this).remove();
            }
        });

        node.parent().removeClass('collapsed').addClass('expanded').addClass(
            'selected');

        $("i[id='toggle_" + sel_dir + "']").attr("class", "fa fa-lock");
        $("i[id='toggle_" + sel_dir + "']").attr("style", "color: blue; font-size : 1.6em");


        node.attr('checked', true);
        (node.parent()).fileTree(config, function(file, checkedStatus,
            rootRegexDir) {
            editPatch(file, checkedStatus, rootRegexDir);
        });


        //set regex params
        node.attr("rootregexdir", sel_dir);
        node.attr("include", include);
        node.attr("exclude", exclude);
        node.attr("recursive", "" + includeRecursive + "");
        closeRegexPanel();
    }

};

function toggleState(node) {
    var id = node.id;
    var n = id.indexOf("_");
    path = id.substring(n + 1);


    var checkboxObj = $("input[name='directory_" + path + "']");
    console.log("input[name='directory_" + path + "']");
    //Check if the dir has regex
    if (checkboxObj.attr("rootregexdir") == undefined || checkboxObj.attr("rootregexdir") == 'undefined') {
        console.log("checkboxObj.rootregexdir:" + checkboxObj.attr("rootregexdir"));
        console.log("No regex");
    } else {
        path = checkboxObj.attr("rootregexdir");
        $('#create_policy_regex_includeRecursive').attr('checked', checkboxObj.attr("recursive") == "true");
        var includeObj = $("input[id='create_policy_regex_include']");
        var include = checkboxObj.attr("include");
        includeObj.attr("value", include);

        var excludeObj = $("input[id='create_policy_regex_exclude']");
        var exclude = checkboxObj.attr("exclude");
        excludeObj.attr("value", exclude);
    }
    var oldSelDir = "";
    if ($('#regexPanel').hasClass('open')) {
        oldSelDir = $('#sel_dir').val();
    }
    $("#regex_error_vm").html("");
    $('#dir_path').val(path);
    $('#folderPathDiv').text(id.substring(n + 1));
    $('#sel_dir').val(path);
    $('input[name=asset_tag_policy]').val(path);
    if ($('#regexPanel').hasClass('open')) {
        //reset values
        //check if it is open already, and user has clicked lock/unlock icon for a different dir
        //close the current panel and open new
        if (path == oldSelDir) {
            closeRegexPanel();
        }
    } else {
        openRegexPanel();
    }
    document.forms["form-horizontal"].reset();
}

function openRegexPanel() {
    $('#regexPanel').addClass('col-md-4');
    $('#regexPanel').addClass('open');
    $('#regexPanel').removeClass('hidden');
    $('#directoryTree').removeClass('col-md-12');
    $('#directoryTree').addClass('col-md-8');
}

function closeRegexPanel() {
    $('#regexPanel').removeClass('col-md-4');
    $('#regexPanel').removeClass('open');
    $('#regexPanel').addClass('hidden');
    $('#directoryTree').addClass('col-md-12');
    $('#directoryTree').removeClass('col-md-8');

    //reset values et while opening toggle
    $('#dir_path').val("");
    $('#folderPathDiv').text("");
    $('#sel_dir').val("");

    $("input[id='create_policy_regex_include']").attr("value", "");
    $("input[id='create_policy_regex_exclude']").attr("value", "");
    $("input[id='create_policy_regex_includeRecursive']").attr("checked", false);
}

var pageInitialized = false;
var patches = [];
var temp_patches = [];
var canPushPatch = true;
var navButtonClicked = false;

var refreshIntervalId = setInterval(function() {
    var d = new Date();
    var n = d.getTime();

    console.log("TImer - EDIT -- " + n);

    editPolicyDraft();
}, 10000);

function editPatchWithDataFromServer(patch) {
    var cnt = 0;
    for (cnt in patch) {
        var addRemovePatch = patch[cnt];
        if (jQuery.inArray(addRemovePatch, patches) == -1) {
            patches.push(addRemovePatch);
        }
    }
    console.log("Server patch - EDIT");

    editPolicyDraft();
}



var editPolicyDraft = function() {
    console.log("interval Id : " + refreshIntervalId);
    if (canPushPatch == false) {
        return;
    } else {
        canPushPatch = false;
    }

    if (patches.length == 0 && temp_patches.length == 0) {
        canPushPatch = true;
        if (navButtonClicked) {
            createPolicy();
        }
        return;
    }

    for (i = 0; i < temp_patches.length; i++) {
        patches.push(temp_patches[i]);
    }
    if (temp_patches.length > 0) {
        temp_patches.length = 0;
    }


    var patchBegin = "<patch>";
    var patchEnd = "</patch>";
    var patchesStr = "";
    for (i = 0; i < patches.length; i++) {
        var addRemoveXml = patches[i];
        patchesStr = patchesStr.concat(addRemoveXml);
    }

    var finalPatch = patchBegin.concat(patchesStr, patchEnd);

    var formData = JSON.stringify({
        patch: finalPatch
    });

    $.ajax({
        type: "PUT",
        url: "/v1/trust-policy-drafts/" + current_trust_policy_draft_id,
        data: formData,
        contentType: "application/json",
        success: function(data, status) {
            console.log("Patch applied");
            canPushPatch = true;
            patches.length = 0;
            // Show message in div
            var $messageDiv = $('#saveMessage'); // get the reference of the
            // div
            $messageDiv.show().html('Draft saved'); // show and set the message
            setTimeout(function() {
                $messageDiv.hide().html('');
            }, 3000); // 3 seconds later, hide
            createPolicy();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            $("#createVmPolicyDirNext").prop('disabled', false);
            canPushPatch = true;
            temp_patches.length = 0;
            var $messageDiv = $('#saveMessage'); // get the reference of the
            // div
            $messageDiv.show().html('Error saving draft'); // show and set the message
            setTimeout(function() {
                $messageDiv.hide().html('');
            }, 3000); // 3 seconds later, hide
            createPolicy();
        }
    });
}

$(document)
    .ready(
        function() {
            console.log("******* before tree");

            if (pageInitialized)
                return;
            console.log("******* before tree 1");
            $("#dirNextButton").prop('disabled', true);
            $('#jstree2').fileTree({
                root: '/',
                dir: '/',
                script: '/v1/images/' + current_image_id + '/search',
                expandSpeed: 1000,
                collapseSpeed: 1000,
                multiFolder: true,
                init: true,
                loadMessage: "Loading..."
            }, function(file, checkedStatus, rootRegexDir) {
                editPatch(file, checkedStatus, rootRegexDir);
            });

            mainViewModel.selectDirectoriesViewModel = new SelectDirectoriesViewModel();
            mainViewModel.applyRegExViewModel = new ApplyRegExViewModel();

            ko.applyBindings(mainViewModel, document
                .getElementById("select_directories_page"));
            patches.length = 0;
            pageInitialized = true;
        });

/* Patches processing */

function editPatch(file, checkedStatus, rootRegexDir) {
    var whichPatchToUse = patches;
    if (canPushPatch == false) {
        whichPatchToUse = temp_patches;
    }
    var addRemoveXml;
    var node = $("input[name='" + file + "']");
    var parent = node.parent();
    var addPath = "'//*[local-name()=\"Whitelist\"]'";
    var removePath = "'//*[local-name()=\"Whitelist\"]";
    var pos = "prepend";

    if (rootRegexDir != "") {
        if (checkedStatus == true) {
            pos = "after";
        }
        addPath = "'//*[local-name()=\"Whitelist\"]/*[local-name()=\"Dir\"][@Path=\"" + rootRegexDir + "\"]'";
    }

    if (checkedStatus == true) {

        addRemoveXml = "<add pos=\"" + pos + "\" sel=" + addPath + "><File Path=\"" + file + "\"/></add>";
    } else {
        addRemoveXml = "<remove sel=" + removePath + "/*[local-name()=\"File\"][@Path=\"" + file + "\"]'/>";
    }
    whichPatchToUse.push(addRemoveXml);
}

function backToFirstPage() {
    clearInterval(refreshIntervalId);

    var mountimage = {
        "id": current_image_id
    }
    if (current_image_id != "" && current_image_id != null) {
        $.ajax({
            type: "POST",
            url: "/v1/rpc/unmount-image",
            contentType: "application/json",
            headers: {
                'Accept': 'application/json'
            },
            data: JSON.stringify(mountimage),
            success: function(data, status, xhr) {
                backButton();
            }
        });
    }
}



function createPolicy() {
    if (navButtonClicked == false) {
        return;
    } else {
        navButtonClicked = false;
    }
    var createTrustPolicyMetaData = {
        "trust_policy_draft_id": current_trust_policy_draft_id
    }
    $.ajax({
        type: "POST",
        url: "/v1/rpc/trust-policies",
        contentType: "application/json",
        headers: {
            'Accept': 'application/json'
        },
        dataType: "json",
        data: JSON.stringify(createTrustPolicyMetaData), // $("#loginForm").serialize(),
        success: function(data) {
            $("#createVmPolicyDirNext").prop('disabled', false);
            current_trust_policy_id = data.id;
            var mountimage = {
                "id": current_image_id
            }
            current_image_action_id = "";
            var createResponse = data.status;

            $.ajax({
                type: "POST",
                url: "/v1/rpc/unmount-image",
                contentType: "application/json",
                headers: {
                    'Accept': 'application/json'
                },
                data: JSON.stringify(mountimage),
                success: function(data, status, xhr) {
                    if (createResponse == 'Error') {
                        $("#error_modal_vm_2").modal({
                            backdrop: "static"
                        });
                        $('body').removeClass("modal-open");
                    } else {
                        nextButton();
                    }
                    console.log("ERROR and Unmount successfully")

                }
            });

        }
    });


}