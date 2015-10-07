var wizard_page="create_policy";
var masterStepsDiv="createPolicySteps";

var pageInitialized = false;
$(document).ready(function() {
	 if(pageInitialized) return;
	
	var html_url= $("#"+wizard_page+"_step_1").attr('href');
	var htmlpage = html_url.substring(1);

	  $("#"+wizard_page+"_content_step_1").load(htmlpage);
	  
	    pageInitialized = true;
});



    



