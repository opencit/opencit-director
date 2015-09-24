var wizard_page="edit_policy";
var masterStepsDiv="editPolicySteps";
alert("Inside edit policy wizard js");
var pageInitialized = false;
$(document).ready(function() {
	 if(pageInitialized) return;
	
	var html_url= $("#"+wizard_page+"_step_1").attr('href');
	var htmlpage = html_url.substring(1);
alert("loading html page::"+htmlpage);
	  $("#"+wizard_page+"_content_step_1").load(htmlpage);
	  
	    pageInitialized = true;
});



    


function displayNextEditPolicyPage(){
	
	var divid1=$('#'+masterStepsDiv).find( "li .selected" ).attr('id');
	alert("inside displayNextEditPolicyPage");

	divid=divid1;
	var n = divid.lastIndexOf("_");
	
	 var stepNum = divid.substring(n+1);
	
	 
	
	var nextStepNum=eval(stepNum)+1;
	
	 $("#"+wizard_page+"_content_step_"+stepNum).hide();
	 $("#"+wizard_page+"_content_step_"+nextStepNum).show();
	 var html_url= $("#"+wizard_page+"_step_"+nextStepNum).attr('href');
	 var htmlpage = html_url.substring(1);
		
	 var isEmpty=!$.trim( $("#"+wizard_page+"_content_step_"+nextStepNum).html());
		
	 if(isEmpty==true){
		 $("#"+wizard_page+"_content_step_"+nextStepNum).load(htmlpage);
	 }

	 $("#"+wizard_page+"_step_"+nextStepNum).addClass("selected");
	 $("#"+wizard_page+"_step_"+stepNum).removeClass("selected");
	 $("#"+wizard_page+"_step_"+stepNum).addClass("done");
	 $("#"+wizard_page+"_step_"+nextStepNum).removeClass("disabled");
	 $("#"+wizard_page+"_step_"+nextStepNum).removeClass("done");
	 $("#"+wizard_page+"_step_"+nextStepNum).addClass("selected");
	
	 
}


function displayPreviousEditPolicyPage(divid){
	var divid1=$('#'+masterStepsDiv).find( "li .selected" ).attr('id');
	divid=divid1;
	var n = divid.lastIndexOf("_");
	
	 var stepNum = divid.substring(n+1);
	
	var previousStepNum=eval(stepNum)-1;

	 $("#"+wizard_page+"_content_step_"+stepNum).hide();
	 $("#"+wizard_page+"_step_"+stepNum).removeClass("selected");
	 $("#"+wizard_page+"_step_"+stepNum).addClass("done");
	 $("#"+wizard_page+"_content_step_"+previousStepNum).show();


	 $("#"+wizard_page+"_step_"+previousStepNum).removeClass("done");
	 $("#"+wizard_page+"_step_"+previousStepNum).addClass("selected");

	
}




