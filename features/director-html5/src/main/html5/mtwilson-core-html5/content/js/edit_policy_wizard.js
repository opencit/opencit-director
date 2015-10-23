var wizard_page="edit_policy";
var masterStepsDiv="editPolicySteps";

var pageInitialized = false;
$(document).ready(function() {
	 if(pageInitialized) return;
	
	var html_url= $("#"+wizard_page+"_step_1").attr('href');
	var htmlpage = "/v1/html5/features/director-html5/mtwilson-core-html5/content/"+html_url.substring(1);

	  $("#"+wizard_page+"_content_step_1").load(htmlpage);
	  
	    pageInitialized = true;
});



    



