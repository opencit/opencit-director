		var endpoint = "/v1/images/";

		
		
		
				function EditSelectDirectoriesMetaData(data) {
	
				
					this.imageid = current_image_id;
					///this.image_name=ko.observable();

				}

			
				function EditSelectDirectoriesViewModel() {
					var self = this;
				
					self.editSelectDirectoriesMetaData = new EditSelectDirectoriesMetaData({});

					self.editSelectDirectoriesSubmit = function(loginFormElement) {

						////Code

						displayNextEditPolicyPage();

					}

				};



				function ApplyRegexMetaData(data) {
					
					   
				this.dir_path=ko.observable();
				///	this.create_policy_regex_exclude=ko.observable("");
				///	this.create_policy_regex_include=ko.observable("");
					
				this.selected_image_format= ko.observable();

				}


				function ApplyRegExViewModel() {
				    var self = this;

				    
				    
				    
				    self.applyRegexMetaData = new ApplyRegexMetaData({});
				   
				    self.applyRegEx = function(loginFormElement) {
				    
				    	console.log("Inside apply regex method");
				    	
				    $.ajax({
				        type: "POST",
				        url:  endpoint+"trustpoliciesmetadata",
//				                    accept: "application/json",
				        contentType: "application/json",
				        headers: {'Accept': 'application/json'},
				        data: ko.toJSON(self.applyRegexMetaData), //$("#loginForm").serialize(), 
				        success: function(data, status, xhr) {
				        	
				        	console.log("regex server call successfull");
				        }
				    });
				    
				    }
				    

				    
				};

				
				
				
				
				
				
				function toggleState(str) {
					var id=str.id;
					var n = id.indexOf("_");
					 var path = id.substring(n+1);
					 
					 $('#dir_path').val(path);
					$('#folderPathDiv').text("Apply RegEx Filter on: "+path);
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

				setInterval(function (){editPolicyDraft();}, 10000);

				var editPolicyDraft = function (){
					if(patches.length == 0){
						return;
					}
					
					var patchBegin = "<patch>";
					var patchEnd = "</patch>";
					var patchesStr="";
					for(i=0;i<patches.length;i++){
						var addRemoveXml = patches[i];
						patchesStr = patchesStr.concat(addRemoveXml);
					}	

					var finalPatch = patchBegin.concat(patchesStr, patchEnd);
					
					var formData = JSON.stringify({patch:finalPatch});
					
					$.ajax({
						type: "POST",
						url: "/v1/images/policydraft/"+current_image_id+"/edit",
						data: formData,
						contentType: "application/json",
						success: function(data, status) {
							patches.length = 0;
						},
						error: function (jqXHR, textStatus, errorThrown){
							alert("ERROR in saving to draft");
						}
					});							
				}						

				$(document)
						.ready(
								function() {
								
									
									if (pageInitialized)
										return;
									$('#jstree2')
											.fileTree(
													{
														root : 'C:/Temp',
														script : '/v1/images/browse/'+currentEditPolicyImageId+'/search',
														expandSpeed : 1000,
														collapseSpeed : 1000,
														multiFolder : true,
														loadMessage : "Loading..."
													},
													function(file,
															checkedStatus) {
														editPatch(file,
																checkedStatus);
													});
									
									mainViewModel.editSelectDirectoriesViewModel = new EditSelectDirectoriesViewModel();
									 mainViewModel.applyRegExViewModel  =  new ApplyRegExViewModel();
									
									ko
											.applyBindings(
													mainViewModel,
													document
															.getElementById("edit_policy_content_step_2"));

									pageInitialized = true;
								});

				/* Patches processing */

				function editPatch(file, checkedStatus) {
					var addRemoveXml;
					if (checkedStatus == true) {
						addRemoveXml = "<add sel='//*[local-name='Whitelist']'><File>"
								+ file + "</File></add>";
					} else {
						addRemoveXml = "<remove sel='//*[local-name='Whitelist']/File[text()=\""
								+ file + "\"]'/>";
					}
					patches.push(addRemoveXml);
					//editPolicyDraft();
				}
				