window.onload = refereshDashboardGrid();



function refreshPieChart(){
	var myPieChart = document.getElementById('chart').getContext("2d");
		var canvas_legend = document.getElementById("canvasLegend");
		var delay = 1000; 
		setTimeout(function() {
			function loadPage(){
			$.ajax({
				type : "GET",
				data : "",
				url : "/v1/dashboard/pieChart",
				dataType : "json",
				cache : false,
				success : function(response) {

					console.log(response);
					var options = {
						segmentShowStroke : false,
						animateRotate : true,
						animateScale : false,
						percentageInnerCutout : 50,
						showTooltips: false
					};
					var pieChart = new Chart(myPieChart).Pie(response);
					canvas_legend.innerHTML = pieChart.generateLegend();
				},
				error : function(response) {
					alert('Error while request..');
				}
			});
		};

		window.onload = loadPage();
		

		}, delay);
}



function refereshDashboardGrid() {

	console.log("dashboard js loaded");
	function myRecentPolicies() {
		$.ajax({
				type : "GET",
					url : "/v1/dashboard/trustpolicies/47AF9302-BEC9-4385-98DD-8185C9F7353F/currentuser",
					dataType : "json",
					success : function(result) {
						$("#myRecentPolicies").jsGrid({
							height : "300%",
							width : "100%",
							sorting : true,
							paging : true,
							pageSize : 10,
							pageButtonCount : 5,
							data : result,
							fields : [ {
								title : "Image Name",
								name : "name",
								type : "text",
								align : "center",

								width : 75
							}, {
								title : "Image Format",
								name : "image_format",
								type : "text",
								align : "center",

								width : 75
							}, {
								title : "Created Date",
								name : "created_date",
								type : "text",
								align : "center",

								width : 75
							}, {
								title : "Edited Date",
								name : "edited_date",
								type : "text",
								align : "center",

								width : 75
							}

							]

						});
						var delay = 1000; 
						setTimeout(function() {
							$("#myRecentPolicies").show();
							$("#myRecentPolicies").jsGrid("refresh");
						}, delay);


					}

				});

	}

	window.onload = myRecentPolicies();

	function otherRecentlyEditedPolicy() {
		console.log("otherRecentlyEditedPolicy");
		$.ajax({
			type : "GET",
			url : "/v1/dashboard/trustpolicies",
			dataType : "json",
			success : function(result) {
				console.log("sayee");
				$("#otherRecentlyEditedPolicy").jsGrid({

					height : "70%",
					width : "100%",
					sorting : true,
					paging : true,
					pageSize : 10,
					pageButtonCount : 5,
					data : result,
					fields : [ {
						title : "Image Name",
						name : "display_name",
						type : "text",
						align : "center",

						width : 100
					}, {
						title : " User",
						name : "edited_by_user_id",
						type : "text",
						align : "center",

						width : 100
					}, {
						title : "Created Date",
						name : "created_date",
						type : "text",
						align : "center",
						width : 100
					}, {
						title : "Edited Date",
						name : "edited_date",
						type : "text",
						width : 100
					} ]
				});
				var delay = 1000; 
				setTimeout(function() {
					$("#otherRecentlyEditedPolicy").show();
					$("#otherRecentlyEditedPolicy").jsGrid("refresh");
				}, delay);

			}
		});

	}

	window.onload = otherRecentlyEditedPolicy();

	function newlyImportedImages() {
		$.ajax({
					type : "GET",
					url : "/v1/dashboard/images/withouttrustpolicies",
					dataType : "json",
					success : function(result) {
						$("#newlyImportedImages").jsGrid({
							height : "300%",
							width : "100%",
							sorting : true,
							paging : true,
							pageSize : 10,
							pageButtonCount : 5,
							data : result,
							fields : [ {
								title : "Image Name",
								name : "name",
								align : "center",
								
								type : "text"
							}, {
								title : " Image Format",
								name : "image_format",
								align : "center",

								type : "text"
							}, {
								title : "Imported Date",
								name : "created_date",
								align : "center",

								type : "text"
							} ]
						});
				var delay = 1000; 
				setTimeout(function() {
					$("#newlyImportedImages").show();
					$("#newlyImportedImages").jsGrid("refresh");
				}, delay);


					}
				});

	}
	window.onload = newlyImportedImages();

	function imagesReadyToDeploy() {
		$.ajax({
			type : "GET",
			url : "/v1/dashboard/imagesToUpload",
			dataType : "json",
			success : function(result) {
				$("#imagesReadyToDeploy").jsGrid({
					height : "300%",
					width : "100%",
					sorting : true,
					paging : true,
					pageSize : 10,
					pageButtonCount : 5,
					data : result,
					fields : [ {
						title : "Image Name",
						name : "display_name",
						type : "text",
						align : "center",

						width : 75
					}, {
						title : " Image Format",
						name : "image_format",
						type : "text",
						align : "center",
						width : 75
					}, {
						title : "Policy Creation Date",
						name : "created_date",
						type : "text",
						align : "center",
						width : 75
					} ]
				});
				var delay = 1000; 
				setTimeout(function() {
					$("#imagesReadyToDeploy").show();
					$("#imagesReadyToDeploy").jsGrid("refresh");
				}, delay);


			}
		});

	}
	window.onload = imagesReadyToDeploy();

	function recentlyDeployedImages() {
		$.ajax({
			type : "GET",
			url : "/v1/dashboard/uploadedImages",
			dataType : "json",
			success : function(result) {
				$("#recentlyDeployedImages").jsGrid({
					height : "300%",
					width : "100%",
					sorting : true,
					paging : true,
					pageSize : 10,
					pageButtonCount : 5,
					datatype : "json",
					data : result,
					fields : [ {
						title : "Image Name",
						name : "name",
						type : "text",
						align : "center",

						width : 100
					}, {
						title : "Image Format",
						name : "image_format",
						align : "center",
						type : "text",

						width : 100
					}, {
						title : "Imported Date",
						name : "date",
						align : "center",
						type : "text",

						width : 100
					}

					]
				});
				var delay = 1000; 
				setTimeout(function() {
					$("#recentlyDeployedImages").show();
					$("#recentlyDeployedImages").jsGrid("refresh");
				}, delay);

			}
		});

	}
	window.onload = recentlyDeployedImages();

	function uploadProgress() {
		$.ajax({
			type : "GET",
			url : "/v1/dashboard/uploadProgress",
			dataType : "json",
			success : function(result) {
				$("#uploadProgress").jsGrid({
					height : "300%",
					width : "100%",
					sorting : true,
					pageSize : 10,
					pageButtonCount : 5,
					data : result,
					fields : [ {
						title : "Image",
						name : "image_id",
						type : "text",
						align : "center",

						width : 50
					}, {
						title : "Total Size",
						name : "action_size_max",
						type : "number",
						align : "center",

						width : 50
					}, {
						title : "Content Sent",
						name : "action_size",
						type : "number",
						align : "center",
						width : 50
					},{
						title : "Task Name",
						name : "current_task_name",
						type : "number",
						align : "center",
						width : 50
					}, {
						title : "Task Status",
						name : "current_task_status",
						type : "number",
						align : "center",
						width : 50
					}

					]
				});
				var delay = 1000; 
				setTimeout(function() {
					$("#uploadProgress").show();
					$("#uploadProgress").jsGrid("refresh");
				}, delay);


			}
		});

	}
	window.onload = uploadProgress();

}
