
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

    self.resetRegEx = function(data, event) {
        var sel_dir = $("#sel_dir").val();
        var node = $("input[name='checkbox_" + sel_dir + "']");
        var includeRecursive = $("#create_policy_regex_includeRecursive").is(':checked');
        var include = $('#create_policy_regex_include').val();
        var exclude = $('#create_policy_regex_exclude').val();
        var filterType = $("#create_policy_filter_type").val();
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
            reset_regex: true,
            recursive: includeRecursive,
            include_recursive: includeRecursive,
            include: include,
            exclude: exclude,
            filterType: filterType
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
    	var filterType=$("#create_policy_filter_type").val();
    	console.log("#### filterType:"+filterType);
        var include = loginFormElement.create_policy_regex_include.value;
        var includeRecursive = loginFormElement.create_policy_regex_includeRecursive.checked;
        var exclude = loginFormElement.create_policy_regex_exclude.value;
        console.log(include + "-- " + exclude + " -- " + includeRecursive);
        if ((include == "" || include == null || include == undefined) && (exclude == "" || exclude == null || exclude == undefined)) {
            $("#regex_error_vm").html("<font color=' red'>Provide atleast one filter</font>");
            return;
        }
        $("#regex_error_vm").html("");
        var sel_dir = loginFormElement.sel_dir.value.trim();
        console.log("############### sel_dir:"+sel_dir);
        var node = $("input[name='checkbox_" + sel_dir + "']");
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
            recursive: includeRecursive,
            include: include,
            include_recursive: includeRecursive,
            exclude: exclude,
            filterType: filterType
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
       /// node.attr("recursive", "" + includeRecursive + "");
        closeRegexPanel();
    }

};

function getSymlinkDisplayValue(path){
var displayValue="";
	if(path){

	var pathNodesArr=path.split("\/");

	if(pathNodesArr){
	 displayValue= pathNodesArr[pathNodesArr.length-1];
	}
	}
///	console.log("displayValue::"+displayValue);

	return 	displayValue;
}



function toggleState(node) {
    var id = node.id;
    var n = id.indexOf("_");
    path = id.substring(n + 1);


    var checkboxObj = $("input[name='checkbox_" + path + "']");
    console.log("input[name='checkbox_" + path + "']");
    //Check if the dir has regex
    /*if (checkboxObj.attr("rootregexdir") == undefined || checkboxObj.attr("rootregexdir") == 'undefined') {
        console.log("checkboxObj.rootregexdir:" + checkboxObj.attr("rootregexdir"));
        console.log("No regex");
    } else {*/
       
        ///$('#create_policy_regex_includeRecursive').attr('checked', checkboxObj.attr("recursive") == "true");
    $("#create_policy_filter_type option[value='"+filter_type+"']").attr("selected", "selected");
        console.log("filter_type :"+filter_type);
        var includeObj = $("input[id='create_policy_regex_include']");
        var include = checkboxObj.attr("include");
        if(!include){
        	if(filter_type){
        		include="*";
        	}else{
        		if(filter_type == "regex"){
            		include=".*";
            	}else{
            		include="*";
            	}	
        	}
        	
        }
        includeObj.attr("value", include);

        var excludeObj = $("input[id='create_policy_regex_exclude']");
        var exclude = checkboxObj.attr("exclude");
        filter_type="wildcard";
        var filter_type = checkboxObj.attr("filter_type");
        ///filter_type="regex";
        excludeObj.attr("value", exclude);
        ///$("#create_policy_filter_type option:contains(" + filter_type + ")").attr('selected', 'selected');
       $("#create_policy_filter_type option[value='"+filter_type+"']").attr("selected", "selected");
    ///}
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



$('#create_policy_filter_type').on('change', function() {
	var newFilter=$("#create_policy_filter_type").val();
	console.log("newFilter:"+newFilter);
	 var includeObj = $("input[id='create_policy_regex_include']");
	 var include="";
       if(newFilter){
    	   console.log("newFilter :"+newFilter);
       	if(newFilter == "regex"){
       		include=".*";
       	}else{
       		include="*";
       	}
       }
       includeObj.attr("value", include);
	
	});

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


function createTreeContent(tree_element,withParentDirectoryHtml){
///	alert("tree_element::"+tree_element);
	var tree_content="";
	tree_content+= "<ul class=\"jqueryFileTree\" style=\"display: none;\">";
	tree_content+=createTreeElementRecursively(tree_element,withParentDirectoryHtml);
	///console.log("################### tree content"+tree_content);

	tree_content+="</ul>";
	//////console.log("####################after tree content"+tree_content);
	return tree_content;
}

function createTreeElementRecursively(tree_element,withParentDirectoryHtml) {
	///console.log("Inside createTreeElementRecursively generated_tree::" + tree_element.value);
	var tree_content = "";
	var checked = "";
	var selected = "";
	var regexIdentifier = "";
	var child_tree_elements_array = tree_element.child_elements;
	var anchor_element = "";

	if (tree_element.is_selected == true) {
		checked = "checked=\"true\"";
		selected = "selected";
	}

	if (tree_element.include || tree_element.exclude) {
		regexIdentifier = " include=\"" + tree_element.include + "\" exclude=\""
				+ tree_element.exclude + "\"  filter_type=\""+tree_element.filter_type+"\" ";
	}

	

	if (tree_element.element_type == 'directory') {
		///alert("Inside tree_element.is_directory true"+tree_element.value);
		var expanded_li_class = "";
		var toggle_icon = "";
		
		if(withParentDirectoryHtml==true){
		anchor_element = "<a href=\"#\" " + " style=\"float:left;\" " + " name=\"" + tree_element.path + "\" " + " id=\"" + tree_element.path + "\" " + "rel=\""
			+ tree_element.path + "/\">" + tree_element.value + "</a>";

		if (tree_element.is_selected == true) {
			expanded_li_class = "expanded";
		}

		if (tree_element.include || tree_element.exclude) {
			toggle_icon = "<i class='fa fa-lock' style='color : blue; font-size : 1.6em' title='"
					+ "To unselect files/dirs click on the icon to Reset"
					+ "'  id='toggle_"
					+ tree_element.path
					+ "'   onclick='toggleState(this)'></i>";
		} else {
			toggle_icon = "<i class='fa fa-unlock' style='color : blue; font-size : 1.6em' title='"
					+ "To lock the directory and specific files click on the icon to apply regex"
					+ "'  id='toggle_"
					+ tree_element.path
					+ "'   onclick='toggleState(this)'></i>";
		}

		tree_content += "<li class=\"" + "directory" + " " + selected + " "
				+ expanded_li_class + "\">";
		tree_content += "<input type=\"checkbox\" name=\"checkbox_"
				+ tree_element.path + "\" id=\"checkbox_" + tree_element.path + "\""
				+ checked + regexIdentifier + " style=\"float:left;\"/>";
		tree_content += anchor_element;
		tree_content += toggle_icon;
		}

		///console.log("##########Inside child_elements.length::"+child_tree_elements_array.length+" for element::"+tree_element.value);

		if (child_tree_elements_array.length > 0) {
			///console.log("##########Inside >0 child_elements.length::"+child_tree_elements_array.length+" for element::"+tree_element.value);
			tree_content += "<ul class=\"jqueryFileTree\" style=\"display: none;\">";


			for (var i = 0; i < child_tree_elements_array.length; i++) {
				///console.log("#################Before calling for i="+i);
	
				var childTreeContent = createTreeElementRecursively(child_tree_elements_array[i],true);
				///console.log("#################result came::  childTreeContent ::"+childTreeContent );

			
				tree_content += childTreeContent;
			}
			tree_content += "</ul>";

		}
		if(withParentDirectoryHtml==true){
		tree_content += "</li>";
		}
		
	} else if (tree_element.element_type == 'symlink') {

		var displayValue=getSymlinkDisplayValue(tree_element.path);
		if(displayValue){
			anchor_element = "<a href=\"#\" style=\"text-decoration:underline;\" " +" onMouseOver=\"changeToolTip(this);\""  +  " name=\"" + tree_element.path + "\" " + " id=\"" + tree_element.path + "\" " + "rel=\""
			+ tree_element.value + "\">" + displayValue + "</a>";
			
		   tree_content += "<li class=\"" + "symlink" + " " + selected + "\">";
		   tree_content += "<input type=\"checkbox\" name=\"checkbox_"
				+ tree_element.path + "\" id=\"checkbox_" + tree_element.path + "\""
				+ checked + regexIdentifier + " style=\"float:left;\"/>"
		   tree_content += anchor_element;
		   tree_content += "</li>";	
	   }
		
	}else{
		        
		anchor_element = "<a href=\"#\" " + " name=\"" + tree_element.path + "\" " + " id=\"" + tree_element.path + "\" " + "rel=\""
			+ tree_element.path + "\">" + tree_element.value + "</a>";

		tree_content += "<li class=\"" + "file" + " " + selected + "\">";
		tree_content += "<input type=\"checkbox\" name=\"checkbox_"
				+ tree_element.path + "\" id=\"checkbox_" + tree_element.path + "\""
				+ checked + regexIdentifier + " style=\"float:left;\"/>"
		tree_content += anchor_element;
		tree_content += "</li>";

	}
	return tree_content;
	

}


function changeToolTip(element){
	
	var target=element.rel;
	///$(element).tooltip({contents:"Hello"}).tooltip('close').tooltip('open');
	var checkboxEleName="checkbox_"+target;
	console.log("#################checkboxEleName:"+checkboxEleName);
	if($('[name="'+checkboxEleName+'"]').length==0){
		console.log("no such element checkboxEleName:"+checkboxEleName);
		return;
	}else{
		console.log("element exist checkboxEleName:"+checkboxEleName);
		
	}
	
	
	var checkboxElement=$('[name="'+checkboxEleName+'"]');
	var isChecked="false";
	var checkedValue=checkboxElement.attr("checked");
	if(checkedValue){
		isChecked="true";
	}
	var isRegex="";
	if(checkboxElement.attr("include") || checkboxElement.attr("exclude")){
		isRegex=true;
	}
	console.log("#### isChecked:"+isChecked+" isRegex:"+isRegex);
	//$(element).attr("title","{checked:"/""+isChecked+"/"", regex:"/"+isRegex+"/"}");
}

function getSymlinkTargetToExpand(path){
	var displayValue="";
		if(path){

		var pathNodesArr=path.split("\/");

		if(pathNodesArr){
		 displayValue= pathNodesArr[0];
		 if(displayValue==""){
			 if(pathNodesArr.length>1){
				 displayValue= pathNodesArr[1];
					console.log("getSymlinkTargetToExpand target split index 1::"+displayValue);
				
			 }
		 }
		}
		}
		displayValue="/"+displayValue;
		console.log("getSymlinkTargetToExpand target::"+displayValue);
	///alert("getSymlinkDisplayValue: target"+displayValue);
		return 	displayValue;
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
        type: "POST",
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
            
            /*$.ajax({
                type: "PATCH",
                url: '/v1/images/' + current_image_id + '/upgradePolicy',
                contentType: "application/json",
                headers: {
                    'Accept': 'application/json'
                },
                success: function(data, status, xhr) {
                    alert("Hello");
                }
            });*/
            
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
    var elementType="File";
    
    console.log("### editPatch , filename:"+file); 
    if($("input[name='checkbox_" + file + "']").parent().hasClass('symlink')){
    	elementType="Symlink";	
    	
    }else if($("input[name='" + file + "']").parent().hasClass('Directory')){
    	elementType="Directory";
    }
    /*if (rootRegexDir != "") {
        if (checkedStatus == true) {
            pos = "after";
        }
        addPath = "'//*[local-name()=\"Whitelist\"]/*[local-name()=\"Dir\"][@Path=\"" + rootRegexDir + "\"]'";
    }*/

    if (checkedStatus == true) {

        addRemoveXml = "<add pos=\"" + pos + "\" sel=" + addPath + "><"+elementType+" Path=\"" + file + "\"/></add>";
    } else {
        addRemoveXml = "<remove sel=" + removePath + "/*[local-name()=\""+elementType+"\"][@Path=\"" + file + "\"]'/>";
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
        "trust_policy_draft_id": current_trust_policy_draft_id,
        "image_id": current_image_id
    }
    $.ajax({
        type: "POST",
        url: "/v1/rpc/finalize-trust-policy-draft",
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
            var createResponse = data.error;

            $.ajax({
                type: "POST",
                url: "/v1/rpc/unmount-image",
                contentType: "application/json",
                headers: {
                    'Accept': 'application/json'
                },
                data: JSON.stringify(mountimage),
                success: function(data, status, xhr) {
                    if (createResponse) {
                        $("#error_modal_vm_2").modal({
                            backdrop: "static"
                        });
						$("#error_modal_vm_2_header").html(createResponse);
                        $('body').removeClass("modal-open");
                    } else {
                        current_flow = "Wizard";
                        nextButton();
                    }
                    console.log("ERROR and Unmount successfully")

                }
            });

        }
    });


}