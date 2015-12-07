var endpoint = "/v1/images/";

function SelectDirectoriesMetaData(data) {

	this.imageid = current_image_id;

}



function SelectDirectoriesViewModel() {
	var self = this;

	self.selectDirectoriesMetaData = new SelectDirectoriesMetaData({});

	self.selectDirectoriesSubmit = function(loginFormElement) {
		clearInterval(refreshIntervalId );
		nextButtonClicked = true;
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
        console.log("Yeah !!!");
		var sel_dir = $("#sel_dir").val();
		
        console.log("DIR : "+sel_dir);

		var node = $("input[name='directory_" + sel_dir + "']");
		var config = {
			root : '/',
			dir : sel_dir,
			script : '/v1/images/' + current_image_id + '/search',
			expandSpeed : 1000,
			collapseSpeed : 1000,
			multiFolder : true,
			loadMessage : "Loading...",
			init : false,
			filesForPolicy : false,
			reset_regex : true
		};

		var len = node.parent().children().length;
		var counter = 0;
		node.parent().children().each(function() {
			if (counter++ > 2) {
				$(this).remove();
			}
		});

		node.parent().removeClass('collapsed').addClass('expanded').removeClass(
				'selected');

		$("img[id='toggle_" + sel_dir + "']")
				.attr(
						"src",
						"/v1/html5/public/director-html5/images/arrow-right.png");

		node.attr('checked', false);
		(node.parent()).fileTree(config, function(file, checkedStatus,
				rootRegexDir) {
			editPatch(file, checkedStatus, rootRegexDir);
		});
		closeRegexPanel();
    }
	self.applyRegEx = function(loginFormElement) {
		var include = loginFormElement.create_policy_regex_include.value;
		var includeRecursive = loginFormElement.create_policy_regex_includeRecursive.checked;
		var exclude = loginFormElement.create_policy_regex_exclude.value;
		
		if((include == "" || include == null || include ==  undefined) && (exclude == "" || exclude == null || exclude ==  undefined))
		{
			$("#regex_error_bm_live").html("<font color='red'>Provide atleast one filter</font>");
			return;
		}
		$("#regex_error_bm_live").html("");
		var sel_dir = loginFormElement.sel_dir.value;
		var node = $("input[name='directory_" + sel_dir + "']");
		var config = {
			root : '/',
			dir : sel_dir,
			script : '/v1/images/' + current_image_id + '/search',
			expandSpeed : 1000,
			collapseSpeed : 1000,
			multiFolder : true,
			loadMessage : "Loading...",
			init : false,
			filesForPolicy : false,
			recursive : true,
			include : include,
			include_recursive : includeRecursive,
			exclude : exclude
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

		$("img[id='toggle_" + sel_dir + "']")
				.attr(
						"src",
						"/v1/html5/public/director-html5/images/locked.png");

		node.attr('checked', true);
		(node.parent()).fileTree(config, function(file, checkedStatus,
				rootRegexDir) {
			editPatch(file, checkedStatus, rootRegexDir);
		});
		closeRegexPanel();
	}

};

function toggleState(str) {
	var id = str.id;
	var n = id.indexOf("_");
	var path = id.substring(n + 1);
	var oldSelDir = "";
	if ($('#regexPanel').hasClass('open')) {
		oldSelDir = $('#sel_dir').val();
	}
	$("#regex_error_bm_live").html("");
	$('#dir_path').val(path);
	$('#folderPathDiv').text(path);
	$('#sel_dir').val(path);
	$('input[name=asset_tag_policy]').val(path);
	if ($('#regexPanel').hasClass('open')) {		
		// reset values
		// check if it is open already, and user has clicked lock/unlock icon
		// for a different dir
		// close the current panel and open new
		if(path == oldSelDir){
			closeRegexPanel();		
		}
	} else {
		openRegexPanel();
	}
	document.forms["form-horizontal"].reset();
}

function openRegexPanel(){
	$('#regexPanel').addClass('col-md-4');
	$('#regexPanel').addClass('open');
	$('#regexPanel').removeClass('hidden');
	$('#directoryTree').removeClass('col-md-12');
	$('#directoryTree').addClass('col-md-8');
}

function closeRegexPanel(){
	$('#regexPanel').removeClass('col-md-4');
	$('#regexPanel').removeClass('open');
	$('#regexPanel').addClass('hidden');
	$('#directoryTree').addClass('col-md-12');
	$('#directoryTree').removeClass('col-md-8');

	// reset values et while opening toggle
	$('#dir_path').val("");
	$('#folderPathDiv').text("");
	$('#sel_dir').val("");

}

var pageInitialized = false;
var patches = [];
var temp_patches = [];
var canPushPatch = true;
var nextButtonClicked = false;



var refreshIntervalId = setInterval(function() {
	var d = new Date();
	var n = d.getTime();
	console.log("TImer - EDIT -- "+n);
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

	editPolicyDraft();

}
var editPolicyDraft = function() {
	if(canPushPatch == false){
		return;
	}else{
		canPushPatch = false;
	}

	if (patches.length == 0 && temp_patches.length == 0) {
		canPushPatch = true;
		if(nextButtonClicked){
			createPolicy();
		}
		return;
	}
	
	for (i = 0; i < temp_patches.length; i++) {
		patches.push(temp_patches[i]);		
	}
	if(temp_patches.length > 0){
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
		patch : finalPatch
	});

	$.ajax({
		type : "PUT",
		url : "/v1/trust-policy-drafts/" + current_trust_policy_draft_id,
		data : formData,
		contentType : "application/json",
		success : function(data, status) {
			canPushPatch = true;
			patches.length = 0;
			// Show message in div
			var $messageDiv = $('#saveMessage'); // get the reference of the
			// div
			$messageDiv.show().html('Draft saved'); // show and set the message
			setTimeout(function() {
				$messageDiv.hide().html('');
			}, 3000); // 3 seconds later, hide
			// and clear the message
			console.log("success after edit draft");
			createPolicy();
		},
		error : function(jqXHR, textStatus, errorThrown) {
			canPushPatch = true;
			temp_patches.length=0;
			var $messageDiv = $('#saveMessage'); // get the reference of the
			// div
			$messageDiv.show().html('Error saving draft'); // show and set the
															// message
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
					patches.length = 0;
					if (pageInitialized)
						return;
					$("#dirNextButton").prop('disabled', true);


					$('#jstree2').fileTree(
							{
								root : '/',
								dir : '/',
								script : '/v1/images/'
										+ current_image_id + '/search',
								expandSpeed : 1000,
								collapseSpeed : 1000,
								multiFolder : true,
								init : true,
								loadMessage : "Loading..."
							}, function(file, checkedStatus, rootRegexDir) {
								editPatch(file, checkedStatus, rootRegexDir);
							});

					mainViewModel.selectDirectoriesViewModel = new SelectDirectoriesViewModel();
					mainViewModel.applyRegExViewModel = new ApplyRegExViewModel();

					ko.applyBindings(mainViewModel, document
							.getElementById("select_directories_page"));

					pageInitialized = true;
				});

/* Patches processing */

function editPatch(file, checkedStatus, rootRegexDir) {
	var whichPatchToUse = patches;
	if(canPushPatch == false){
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
		addPath = "'//*[local-name()=\"Whitelist\"]/*[local-name()=\"Dir\"][@Path=\""
				+ rootRegexDir + "\"]'";
	}

	if (checkedStatus == true) {

		addRemoveXml = "<add pos=\"" + pos + "\" sel=" + addPath
				+ "><File Path=\"" + file + "\"/></add>";
	} else {
		addRemoveXml = "<remove sel=" + removePath
				+ "/*[local-name()=\"File\"][@Path=\"" + file + "\"]'/>";
	}
	whichPatchToUse.push(addRemoveXml);
}

function backToBMLiveFirstPage()
{
					var mountimage = {
						"id" : current_image_id
					}
	if(current_image_id != "" && current_image_id !=null)
	{
		$.ajax({
			type : "POST",
			url : "/v1/rpc/unmount-image",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : JSON.stringify(mountimage),
			success : function(data, status, xhr) {
				backButtonLiveBM();
			}
		});
	}
}

function createPolicy(){
	if(nextButtonClicked == false){
		return;
	}else{
		nextButtonClicked = false;
		patches.length = 0;	
	}
	showLoading();
		var createTrustPolicyMetaData = {
			"imageid" : current_image_id
		}
		$.ajax({
			type : "POST",
			url : "/v1/rpc/trust-policies",
			contentType : "application/json",
			dataType : "text",
			data : JSON.stringify(createTrustPolicyMetaData), // $("#loginForm").serialize(),
			success : function(data) {
				var mountimage = {
					"id" : current_image_id
				}
				var createResponse = data;
				current_image_action_id = "";
				$.ajax({
					type : "POST",
					url : "/v1/rpc/unmount-image",
					contentType : "application/json",
					headers : {
						'Accept' : 'application/json'
					},
					data : JSON.stringify(mountimage),
					success : function(data, status, xhr) {
						hideLoading();
						console.log("Unmount successfully")

						if(createResponse == "Error")
						{
							$("#error_modal_bm_live_2").modal({backdrop: "static"});
								$('body').removeClass("modal-open");
						}else{
							nextButtonLiveBM();	
						}

					}
				});
				
			}
		});


}


