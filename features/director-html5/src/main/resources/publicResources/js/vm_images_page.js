

var pageInitialized = false;
$(document).ready(function() {
	 if(pageInitialized) return;
	
	

	  $("#vm_images_grid_page").load("vm_images_grid_page.html");
	  
	    pageInitialized = true;
});




function createPolicy(imageid,imagename){
	current_image_id=imageid;
	current_image_name=imagename;
	currentCreatePolicyImageId=imageid;
	currentCreatePolicyImageName=imagename;
	goToCreatePolicyWizard();
}

function editPolicy(imageid,imagename,trust_policy_id){
	current_image_id=imageid;
	current_image_name=imagename;
	current_trust_policy_id=trust_policy_id;
	currentEditPolicyImageId=imageid;
	currentEditPolicyImageName=imagename;
	goToEditPolicyWizard();
}




function goToCreatePolicyWizard(){
	 $("#vm_images_grid_page").hide();
	 $("#create_policy_wizard_").show();
	
	 var htmlpage1 = "create_policy_wizard.html";
		
	 var isEmpty=!$.trim( $("#create_policy_wizard_").html());
		
	 if(isEmpty==false){
		 $("#create_policy_wizard_").html("");
	 }
	
	 $("#create_policy_wizard_").load("create_policy_wizard.html");
	
	 
	
}


function goToEditPolicyWizard(){
	 $("#vm_images_grid_page").hide();
	 $("#edit_policy_wizard").show();
	
	 var htmlpage = "edit_policy_wizard.html";
		
	 var isEmpty=!$.trim( $("#edit_policy_wizard").html());
		
	 if(isEmpty==false){
		 $("#edit_policy_wizard").html("");
	 }
	 
	 $("#edit_policy_wizard").load(htmlpage);

	
}


function backToVmImagesPage(){
	 $("#create_policy_script").remove();
	 $("#edit_policy_wizard").html("");
	 $("#create_policy_wizard_").html("");
	
	 $("#vm_images_grid_page").show();
		///ko.applyBindings(mainViewModel, document.getElementById("create_policy_content_step_1"));
	 ko.cleanNode(mainViewModel,document.getElementById("create_policy_content_step_1"));
	 alert("after");
}

