var wizard_page="create_policy_for_hosts";
var masterStepsDiv="createPolicyStepsForHosts";

var pageInitialized = false;
$(document).ready(function() {
	 if(pageInitialized) return;
	
	var html_url= $("#"+wizard_page+"_step_1").attr('href');
	var htmlpage = html_url.substring(1);

	  $("#" + wizard_page + "_content_step_1").load("/v1/html5/public/director-html5/"+htmlpage);
	  
	    pageInitialized = true;
});



    



