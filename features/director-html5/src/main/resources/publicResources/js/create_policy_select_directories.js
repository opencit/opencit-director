var endpoint = "/v1/images/";

function SelectDirectoriesMetaData(data) {

	this.imageid = current_image_id;
	// /this.image_name=ko.observable();

}

function SelectDirectoriesViewModel() {
	var self = this;

	self.selectDirectoriesMetaData = new SelectDirectoriesMetaData({});

	self.selectDirectoriesSubmit = function(loginFormElement) {
		
		// //Code
		  $.ajax({
              type: "POST",
              url:  endpoint+current_image_id+"/createpolicy",
//                          accept: "application/json",
              contentType: "application/json",
              headers: {'Accept': 'application/json'},
              data: ko.toJSON(self.selectDirectoriesMetaData), //$("#loginForm").serialize(), 
              success: function(data, status, xhr) {
            	  if(data.id!=undefined && data.id!=null && data.id!="" ){
            	  current_image_action_id = data.id;
            	  }
            	  $.ajax({
                         type: "POST",
                         url:  endpoint+current_image_id+"/unmount",
//                                     accept: "application/json",
                         contentType: "application/json",
                         headers: {'Accept': 'application/json'},
                         data: ko.toJSON(self.createImageMetaData), //$("#loginForm").serialize(), 
                         success: function(data, status, xhr) {
     
                        	nextButton();
                         }
                     });
             	///nextButton();
              }
          });
		  
		///nextButton();
		///displayNextCreatePolicyPage();

	}

};

function ApplyRegexMetaData(data) {
	this.sel_dir = ko.observable("");
	this.dir_path = ko.observable();
	this.create_policy_regex_exclude = ko.observable("");
	this.create_policy_regex_include = ko.observable("");

	this.selected_image_format = ko.observable();

}

function ApplyRegExViewModel() {
	var self = this;

	self.applyRegexMetaData = new ApplyRegexMetaData({});

	self.applyRegEx = function(loginFormElement) {
		var include = loginFormElement.create_policy_regex_include.value;
		var exclude = loginFormElement.create_policy_regex_exclude.value;
		var sel_dir = loginFormElement.sel_dir.value;
		var node = $("input[name='directory_"+sel_dir+"']");
		var config = {root: 'C:/Temp',  dir: sel_dir, 
				script: '/v1/images/browse/'+current_image_id+'/search' ,
				expandSpeed: 1000,
				collapseSpeed: 1000,
				multiFolder: true, 
				loadMessage: "Loading...",
				init: false,
				filesForPolicy: false,
				recursive: true,
				include: include
			};

		var len = node.parent().children().length;	
		var counter = 0;
		node.parent().children().each(function() {
			if(counter++ >1){
				$(this).remove();
			}
		});

		
		node.parent().removeClass('collapsed').addClass('expanded');
		(node.parent()).fileTree(config, function(file, checkedStatus) {
		editPatch(file, checkedStatus);
		});		
	}

};

function toggleState(str) {
	var id = str.id;
	var n = id.indexOf("_");
	var path = id.substring(n + 1);

	$('#dir_path').val(path);
	$('#folderPathDiv').text("Apply RegEx Filter on: " + path);
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
			//Show message in div
			console.log("************* saved");
			var $messageDiv = $('#saveMessage'); // get the reference of the div
			console.log("************* 1111111");
			$messageDiv.show().html('Draft saved'); // show and set the message
			console.log("************* 222222222");
			setTimeout(function(){ $messageDiv.hide().html('');}, 3000); // 3 seconds later, hide
			console.log("************* 3333333333");
			                                                             // and clear the message
		},
		error : function(jqXHR, textStatus, errorThrown) {
			alert("ERROR in saving to draft");
		}
	});
}

var callRegex = function() {
	var node = $("input[name='directory_C:/Temp/Test']");

	var config = {
		root : 'C:/Temp',
		dir : 'C:/Temp/Test',
		script : '/v1/images/browse/'+current_image_id+'/search',
		expandSpeed : 1000,
		collapseSpeed : 1000,
		multiFolder : true,
		loadMessage : "Loading...",
		init : false,
		filesForPolicy : false,
		recursive : true,
		include : "*.txt"
	};

	var len = node.parent().children().length;
	var counter = 0;
	node.parent().children().each(function() {
		if (counter++ > 1) {
			$(this).remove();
		}
	});

	node.parent().removeClass('collapsed').addClass('expanded');
	(node.parent()).fileTree(config, function(file, checkedStatus) {
		editPatch(file, checkedStatus);
	});
}

$(document)
		.ready(
				function() {
					if (pageInitialized)
						return;
					$('#jstree2').fileTree(
							{
								root : 'C:/Temp',
								dir : 'C:/Temp',
								script : '/v1/images/browse/'
										+ current_image_id
										+ '/search',
								expandSpeed : 1000,
								collapseSpeed : 1000,
								multiFolder : true,
								init : true,
								loadMessage : "Loading..."
							}, function(file, checkedStatus) {
								editPatch(file, checkedStatus);
							});

					mainViewModel.selectDirectoriesViewModel = new SelectDirectoriesViewModel();
					mainViewModel.applyRegExViewModel = new ApplyRegExViewModel();

					ko.applyBindings(mainViewModel, document
							.getElementById("select_directories_page"));

					pageInitialized = true;
				});

/* Patches processing */

function editPatch(file, checkedStatus) {
	var addRemoveXml;
	var node = $("input[name='" + file + "']");
	var parent = node.parent();

	if (checkedStatus == true) {
		addRemoveXml = "<add pos=\"prepend\" sel='//*[local-name()=\"Whitelist\"]'><File Path=\""
				+ file + "\"/></add>";
	} else {
		addRemoveXml = "<remove sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"File\"][@Path=\""
				+ file + "\"]'/>";
	}
	patches.push(addRemoveXml);
}
