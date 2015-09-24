var endpoint = "/v1/images/";
var pageInitialized = false;
$(document).ready(function() {
	
	 if(pageInitialized) return;
	
	    refresh_vm_images_Grid();
	  
	    pageInitialized = true;
});
    
        function refresh_vm_images_Grid(){
  
        $.ajax({
            type: "GET",
            url:  endpoint+"imagesList/VM",
            accept: "application/json",
            headers: {'Accept': 'application/json'},
            success: function(data, status, xhr) {    
            	
        		images=data.images;
				$("#vmGrid").jsGrid({
					
					height: "70%",
					width: "100%",
					sorting: true,
					paging: true,
					pageSize: 10,
					pageButtonCount: 15,
					data : images,
					fields: [
					            { title: "Image Name", name: "image_name", type: "text", width: 150, align: "center" },
					            { title: "Image Format", name: "image_format", type: "text", width: 100, align: "center" },
					            { title: "Trust Policy", name: "trust_policy", type: "text", width: 200, align: "center" },
					            { title: "Image Store Upload", name: "image_upload", type: "text", width: 150, align: "center" },
					            { title: "Created Date", name: "created_date", type: "text", width: 150, align: "center" }
					        ]
				});
				/*
        		var imageData="";
            		for (var i=0;i<images.length;i++){
            		   imageData += '<p>' + images[i].image_name + '</p>';
            		   imageData += '<p>' + images[i].image_format + '</p>';
            		   imageData += '<p>' + images[i].trust_policy + '</p>';
            		   imageData += '<p>' + images[i].image_upload + '</p>';
            		   imageData += '<p>' + images[i].created_date + '</p>';
            		}
            		$('#images').html(imageData);
            	*/
            },
        	error: function(jqXHR, exception) {
        		alert("Failed to get images list");
        	}
        });
        
        }
    




