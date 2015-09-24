

var imageStores= new Array();

var option;
alert("inside upload_to_image_store.js");
displayImageStore();
function displayImageStore(){
alert("inside displayImageStore");
$.ajax({
    type: "GET",
    url:  endpoint+"imagestores",
// accept: "application/json",
    contentType: "application/json",
    headers: {'Accept': 'application/json'},
    dataType: "json",
    success: function(data, status, xhr) {
    	alert("imagestores data::"+data.imageStoreNames);
    	imageStores=data.imageStoreNames;
    	
    	
    
    
    	
    	 option="<option value='0'>Select</option>";
    		for (var i=0;i<imageStores.length;i++){
    		   option += '<option value="'+ imageStores[i] + '">' + imageStores[i] + '</option>';
    		
    		}
    		
    		
    	///alert("option:"+option);
    		$("input[name=tarball_radio][value='1']").attr('checked', 'checked');
    		$('#upload_image_name').val(current_image_name);
    		$('#tarball_upload_div').show();
    		$('#image_policy_upload_div').hide();
    		$('#tarball_upload_combo').append(option);
    		
    	
    	
    	  mainViewModel.uploadImageStoreViewModel  =  new UploadStoreViewModel();
   	   // /] ko.cleanNode(mainViewModel);
   	   
   	ko.applyBindings(mainViewModel, document.getElementById("create_policy_content_step_3"));

    }
});

}

function toggleradios11() {
	 alert("Inside on change");
		var showTaraballDiv=	$('input[name=tarball_radio]:checked').val();
	
		if(showTaraballDiv==0){
			alert("11");
			$('#image_policy_upload_div').show();
			$('#tarball_upload_div').hide();
			
			$('#image_upload_combo').append(option);
			$('#policy_upload_combo').append(option);
		}else{
			alert("22");
			$('#tarball_upload_div').show();
			$('#image_policy_upload_div').hide();
			$('#tarball_upload_combo').append(option);
		}
	};

/* 
$("input[name=tarball_radio]:checked").change(function () {
	alert("Inside on change");
	var showTaraballDiv=	$('input[name=tarball_radio]:checked').val();
	alert("showTaraballDiv::"+showTaraballDiv);
	if(showTaraballDiv=0){
		$('#image_policy_upload_div').show();
		$('#tarball_upload_div').hide();
		
		$('#image_upload_combo').append(option);
		$('#policy_upload_combo').append(option);
	}else{
		$('#tarball_upload_div').show();
		$('#image_policy_upload_div').hide();
		$('#tarball_upload_combo').append(option);
	}
		
 })
*/



function UploadStoreMetaData(data) {
	
	   
	///this.upload_image_name  =currentCreatePolicyImageName;
	this.image_id=current_image_id;
	

}

function UploadStoreViewModel() {
    var self = this;

    
  
    
    self.uploadStoreMetaData = new UploadStoreMetaData();
   
    self.uploadToStore = function(loginFormElement) {
    
    self.uploadStoreMetaData.store_name_for_tarball_upload=$('#tarball_upload_combo').val();
    self.uploadStoreMetaData.store_name_for_image_upload=$('#image_upload_combo').val();
    self.uploadStoreMetaData.store_name_for_policy_upload=$('#policy_upload_combo').val();
    	
    $.ajax({
        type: "POST",
        url:  endpoint+"uploadToStore",
// accept: "application/json",
        contentType: "application/json",
        headers: {'Accept': 'application/json'},
        data: ko.toJSON(self.uploadStoreMetaData), // $("#loginForm").serialize(),
        success: function(data, status, xhr) {
        	alert("uploadToStore success"+data);
       	
        }
    });
    
    }
    

    
};
        
    


    
    
    
    
    





