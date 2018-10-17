var wizard_page="edit_policy";
var masterStepsDiv="editPolicySteps";

var pageInitialized = false;
$(document).ready(function() {
	 if(pageInitialized) return;
	
	var html_url= $("#"+wizard_page+"_step_1").attr('href');
	var htmlpage = "/v1/html5/public/director-html5/"+html_url.substring(1);

	  $("#"+wizard_page+"_content_step_1").load(htmlpage);
	  
	    pageInitialized = true;
});



    



