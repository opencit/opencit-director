var endpoint = "/v1/images/";

function SelectDirectoriesMetaData(data) {

	this.imageid = current_image_id;

}

function SelectDirectoriesViewModel() {
	var self = this;

	self.selectDirectoriesMetaData = new SelectDirectoriesMetaData({});

	self.selectDirectoriesSubmit = function(loginFormElement) {

		// //Code

		$.ajax({
			type : "POST",
			url : endpoint + current_image_id + "/createpolicy",
			contentType : "application/json",
			dataType : "text",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.selectDirectoriesMetaData), // $("#loginForm").serialize(),
			success : function(data) {
				if(data == "ERROR")
				{
					current_image_action_id = "";
					$.ajax({
						type : "POST",
						url : endpoint + current_image_id + "/unmount",
						contentType : "application/json",
						headers : {
							'Accept' : 'application/json'
						},
						data : ko.toJSON(self.createImageMetaData),
						success : function(data, status, xhr) {
							$("#error_modal_bm_image_2").modal({backdrop: "static"});
								$('body').removeClass("modal-open");
							console.log("Unmount successfully")

						}
					});
				}
				else
				{
					current_image_action_id = data;
					$.ajax({
						type : "POST",
						url : endpoint + current_image_id + "/unmount",
						contentType : "application/json",
						headers : {
							'Accept' : 'application/json'
						},
						data : ko.toJSON(self.createImageMetaData),
						success : function(data, status, xhr) {
		
							console.log("Unmount successfully")
							editPolicyDraft();
							nextButtonImagesBM();
						}
					});
				}
			}
		});

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
			script : '/v1/images/browse/' + current_image_id + '/search',
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

		node.parent().removeClass('collapsed').addClass('expanded').addClass(
				'selected');

		$("img[id='toggle_" + sel_dir + "']")
				.attr(
						"src",
						"/v1/html5/features/director-html5/mtwilson-core-html5/content/images/arrow-right.png");

		node.attr('checked', false);
		(node.parent()).fileTree(config, function(file, checkedStatus,
				rootRegexDir) {
			editPatch(file, checkedStatus, rootRegexDir);
		});        
    }
	self.applyRegEx = function(loginFormElement) {
		var include = loginFormElement.create_policy_regex_include.value;
		var includeRecursive = loginFormElement.create_policy_regex_includeRecursive.checked;
		var exclude = loginFormElement.create_policy_regex_exclude.value;
		if((include == "" || include == null || include ==  undefined) && (exclude == "" || exclude == null || exclude ==  undefined))
		{
			$("#regex_error_bm_image").html("<font color='red'>Provide atleast one filter</font>");
			return;
		}
		$("#regex_error_bm_image").html("");
		var sel_dir = loginFormElement.sel_dir.value;
		var node = $("input[name='directory_" + sel_dir + "']");
		var config = {
			root : '/',
			dir : sel_dir,
			script : '/v1/images/browse/' + current_image_id + '/search',
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
						"/v1/html5/features/director-html5/mtwilson-core-html5/content/images/locked.png");
		node.attr('checked', true);
		(node.parent()).fileTree(config, function(file, checkedStatus,
				rootRegexDir) {
			editPatch(file, checkedStatus, rootRegexDir);
		});
	}

};

function toggleState(str) {
	var id = str.id;
	var n = id.indexOf("_");
	var path = id.substring(n + 1);
	$("#regex_error_bm_image").html("");
	$('#dir_path').val(path);
	$('#folderPathDiv').text(path);
	$('#sel_dir').val(path);
	$('input[name=asset_tag_policy]').val(path);
	if ($('#regexPanel').hasClass('open')) {
		$('#regexPanel').removeClass('col-md-4');
		$('#regexPanel').removeClass('open');
		$('#regexPanel').addClass('hidden');
		$('#directoryTree').addClass('col-md-12');
		$('#directoryTree').removeClass('col-md-8');
	} else {
		$('#regexPanel').addClass('col-md-4');
		$('#regexPanel').addClass('open');
		$('#regexPanel').removeClass('hidden');
		$('#directoryTree').removeClass('col-md-12');
		$('#directoryTree').addClass('col-md-8');
	}
	document.forms["form-horizontal"].reset();
}

var pageInitialized = false;
var patches = [];
var canPushPatch = true;

setInterval(function() {
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
	canPushPatch = true;

	editPolicyDraft();

}
var editPolicyDraft = function() {
	if (patches.length == 0) {
		return;
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
		type : "POST",
		url : "/v1/images/policydraft/" + current_image_id + "/edit",
		data : formData,
		contentType : "application/json",
		success : function(data, status) {
			patches.length = 0;
			// Show message in div
			var $messageDiv = $('#saveMessage'); // get the reference of the
			// div
			$messageDiv.show().html('Draft saved'); // show and set the message
			setTimeout(function() {
				$messageDiv.hide().html('');
			}, 3000); // 3 seconds later, hide
			// and clear the message
		},
		error : function(jqXHR, textStatus, errorThrown) {
			// / alert("ERROR in saving to draft");
		}
	});
}

$(document)
		.ready(
				function() {
					if (pageInitialized)
						return;
					$('#jstree2').fileTree(
							{
								root : '/',
								dir : '/',
								script : '/v1/images/browse/'
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
	patches.push(addRemoveXml);
}

function backToBMFirstPage()
{
	if(current_image_id != "" && current_image_id !=null)
	{
		$.ajax({
			type : "POST",
			url : endpoint + current_image_id + "/unmount",
			contentType : "application/json",
			headers : {
				'Accept' : 'application/json'
			},
			data : ko.toJSON(self.createImageMetaData),
			success : function(data, status, xhr) {
				backButtonImagesBM();
			}
		});
	}
}