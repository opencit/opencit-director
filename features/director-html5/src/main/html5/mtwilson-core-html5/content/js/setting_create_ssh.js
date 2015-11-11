(function() {
    var ssh = {

    		loadData: function(filter) {
    			console.log("Success grid");
    	        return $.ajax({
    	            type: "GET",
    	            url: "/v1/setting/sshsettings/getdata",
    	            data: filter,
    	            dataType: "json",
    	            success: function(result){
    	            	console.log(result);
    	            }
    	        });
    	        
    	    },

		    insertItem: function(item) {
		    	return $.ajax({
		            type: "POST",
		            url: "/v1/setting/sshsettings/postdata",
		            data: ko.toJSON(item),
		            dataType: "json",
		            contentType:"application/json",
		            headers: {'Accept': 'application/json'},
		            success: function(data){
				if (data.status == "Error") {
							$('#error_modal_body_ssh_create_1').text(data.details);
							$("#error_modal_ssh_create_1").modal({
								backdrop : "static"
							});
								$('body').removeClass("modal-open");
							return;
						}else{


		        			ssh.renderdata();
						}
		        		
		        	}
		        });
		    },
		    
		    updateItem: function(item) {
		        return $.ajax({
		            type: "PUT",
		            url: "/v1/setting/sshsettings/update",
		            data: ko.toJSON(item),
		            dataType: "json",
		            contentType:"application/json",
		            headers: {'Accept': 'application/json'},
		        	success: function(data){
if (data.status == "Error") {
							$('#error_modal_body_ssh_create_1').text(data.details);
							$("#error_modal_ssh_create_1").modal({
								backdrop : "static"
							});
								$('body').removeClass("modal-open");
							return;
						}

		        		ssh.displaydata();
		        	}
		        });
		       
		    },
		    
		    deleteItem: function(item) {
		    	
		        return $.ajax({
		            type: "DELETE",
		            url: "/v1/setting/sshsettings/"+item.id+"/delete",
		            data: ko.toJSON(item),
			        dataType: "json",
			        contentType:"application/json",
			        success: function(result){
			        	console.log(result);
			        	postData: result
			        }
			        	
		        
			    });	 
		        
		       
		    },
		    
		    displaydata: function() {
		    	return $("#jsGrid").jsGrid("refresh");
			refreshSSHSetting();
		    	
		    },
		    
		    renderdata: function(){
		    	return $("#jsGrid").jsGrid("render").done(function() {
		    	    console.log("rendering completed and data loaded");
			   refreshSSHSetting();	
		    	});
refreshSSHSetting();
		    }
		    
		    

    
        

    };
    window.db = ssh;
   

}());

