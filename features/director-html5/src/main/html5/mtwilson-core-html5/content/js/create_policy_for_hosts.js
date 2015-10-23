
        var imageFormats= new Array();
        var image_policies=new Array();
        var endpoint = "/v1/images/";

   $( document ).ready(function() {
        	fetchImageLaunchPolicies();
        });


        function CreateBMLiveMetaData(data) {
        	
        	   
        	this.imageid=current_image_id;	
        	this.image_name=$("#image_name").val();


        }

        function CreateBMLiveViewModel() {
            var self = this;
            $("#image_name").val(current_image_name);
            
            self.createBMLiveMetaData = new CreateBMLiveMetaData({});
           
            self.createBMLive = function(loginFormElement) {
            
            self.createBMLiveMetaData.launch_control_policy="MeasureOnly";
            self.createBMLiveMetaData.encrypted=false;
	    self.createBMLiveMetaData.display_name=current_image_name;

      
            	
            $.ajax({
                type: "POST",
                url:  endpoint + "trustpoliciesmetadata",
                contentType: "application/json",
                headers: {'Accept': 'application/json'},
                data: ko.toJSON(self.createBMLiveMetaData), //$("#loginForm").serialize(),
                success: function(data, status, xhr) {
                	$.ajax({
                        type: "POST",
                        url:  endpoint+current_image_id+"/mount",
                        contentType: "application/json",
                        headers: {'Accept': 'application/json'},
                        data: ko.toJSON(self.createBMLiveMetaData), //$("#loginForm").serialize(), 
                        success: function(data, status, xhr) {
                            nextButtonLiveBM();
                        }
                	});
                }
            });
            
            }
            

            
        };
            
            
         function fetchImageLaunchPolicies(){
        	 console.log("HERE 1");
        	   $.ajax({
        	        type: "GET",
        	        url:  endpoint+"image-launch-policies",
        	        contentType: "application/json",
        	        headers: {'Accept': 'application/json'},
        	        dataType: "json",
        	        success: function(data, status, xhr) {
        	        	console.log("HERE 2");

        	        	   mainViewModel.createBMLiveViewModel  =  new CreateBMLiveViewModel();

        	        	ko.applyBindings(mainViewModel, document.getElementById("create_policy_for_hosts_content_step_1"));
        	        }
        	    });
        	   
           } 
           
