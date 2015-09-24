$(document).ready(function(){
				$("#gotoUploadWindow").click(function(){
					var image_deployments = $( "#image_type" ).val();
					var image_format = $("#image_format").val();
					var myData = '{"imageAttributes": {"image_format":"'+ image_format + '", "image_deployments":"'+ image_deployments +'" }}';
					$.ajax({
						type: 'POST',
						url: '/v1/images/uploadtotd',
						data: myData,
						crossDomain: true,
						contentType: "application/json",
						dataType: "json",
						complete:function(resp){
							var mylocation = String(resp.getResponseHeader("Location"));
							console.log(mylocation);
							openUploadWindow(mylocation);
					    }
					});
				});
				function openUploadWindow(mylocation)
				{
					var OpenWindow = window.open("file_upload_window.html", "_blank", "width=500,height=200,resizable=no");
			        OpenWindow.urlToUpload = mylocation; 
					
				}
			});