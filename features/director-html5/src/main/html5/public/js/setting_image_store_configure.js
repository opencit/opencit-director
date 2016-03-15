function imageStoreSettingPage() {
	$("#image_store_grid").html("");
	$
			.ajax({
				type : "GET",
				url : "/v1/image-stores",
				accept : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				success : function(data, status, xhr) {
					var image_stores = data.image_stores;
					var image_stores_grid = [];

					for (i = 0; i < image_stores.length; i++) {
						var image_store = {};
						console.log("deleted :: " + image_stores[i].deleted);
						if (image_stores[i].deleted) {
							continue;
						}
						image_store.image_store_name = image_stores[i].name;

						image_store.image_artifacts = "";
						for (j = 0; j < image_stores[i].artifact_types.length; j++) {
							image_store.image_artifacts += image_stores[i].artifact_types[j];
							image_store.image_artifacts += ","
						}

						image_store.image_artifacts = image_store.image_artifacts
								.substring(0,
										image_store.image_artifacts.length - 1);
						var image_store_json = JSON.stringify(image_stores[i]);
					var deleteCallArr = '["deleteImageStore", "Delete Image Store?", "' + image_stores[i].id + '"]';
						image_store.actions = '<a href=\'#\' onclick=\'getImageStoreAndPopulateImageStore(\"'
								+ image_stores[i].id
								+ '\")\'><span title=\'Edit\' class=\'glyphicon glyphicon-edit\'></span></a>'
								+ '<a href=\'#\' onclick =\'confirmDeleteOperationImageStore('
								+ deleteCallArr
								+ ')\'><span title=\'Delete\' class=\'glyphicon glyphicon-trash\'></span></a>'
								+ '<a href=\'#\' onclick=\'validateImageStore(\"'
								+ image_stores[i].id
								+ '\")\'><span id="'
								+ image_stores[i].id
								+ '" title=\'Validate\' class=\'glyphicon glyphicon-refresh\'></span></a>';
						image_stores_grid.push(image_store);

					}
					;

					$("#image_store_grid").jsGrid({
						height : "auto",
						width : "100%",
						sorting : true,
						paging : true,
						pageSize : 10,
						pageButtonCount : 15,
						data : image_stores_grid,
						fields : [ {
							title : "External Storage Name",
							name : "image_store_name",
							type : "text",
							width : 150,
							align : "center"
						}, {
							title : "Supported Artifacts",
							name : "image_artifacts",
							type : "text",
							width : 200,
							align : "center"
						}, {
							title : "Actions",
							name : "actions",
							type : "text",
							width : 50,
							align : "center"
						} ]
					});

				},
				error : function(jqXHR, exception) {
					alert("Failed to get images store list list");
				}
			});
}

$(function() {
	$("#connector")
			.change(
					function() {
						$('#artifacts_div').hide();
						var connector_selected = $('option:selected', this)
								.val();

						if (connector_selected == "0") {
							return;
						}
						$
								.ajax({
									type : "GET",
									url : "/v1/image-store-connectors/"
											+ connector_selected,
									dataType : "json",
									success : function(data) {
										var artifact_string = "";
										var artifact_string_edit = "";
										for ( var key in data.supported_artifacts) {
											artifact_string = artifact_string
													+ "<input type=\"checkbox\" class=\"artifacts\" value="
													+ key
													+ ">"
													+ data.supported_artifacts[key]
													+ "</input><br/>";
											artifact_string_edit = artifact_string_edit
													+ "<input type=\"checkbox\" class=\"edit_artifacts\" value="
													+ key
													+ ">"
													+ data.supported_artifacts[key]
													+ "</input><br/>";
										}
										$('#edit_artifacts').html(
												artifact_string_edit);
										$('#artifacts').html(artifact_string);
										$('#artifacts_div').show();
									}
								});
					});
});

function createImageStore() {
	var createImageStoreRequest = {};
	createImageStoreRequest.artifact_types = $(
			'input:checkbox:checked.artifacts').map(function() {
		return this.value;
	}).get();
	createImageStoreRequest.name = $("#image_store_name").val().trim();
	createImageStoreRequest.connector = $("#connector").val();

	if (createImageStoreRequest.name == ""
			|| createImageStoreRequest.name.length == 0) {
		$("#image_store_error").html("Name Cannot be empty");
		return;
	}

	if (createImageStoreRequest.connector == "0") {
		$("#image_store_error").html("Select Connector First");
		return;
	}

	if (createImageStoreRequest.artifact_types.length == 0) {
		$("#image_store_error").html("Cannot Accept Zero Artifacts");
		return;
	}

	for (i = 0; i < createImageStoreRequest.artifact_types.length; i++) {
		console.log("GOOGLE :: " + createImageStoreRequest.artifact_types[i]);
		$(
				"input[value=" + createImageStoreRequest.artifact_types[i]
						+ "].edit_artifacts").prop("checked", true);
	}

	$
			.ajax({
				type : "POST",
				url : "/v1/image-stores",
				contentType : "application/json",
				data : JSON.stringify(createImageStoreRequest),
				accept : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				success : function(data, status, xhr) {
					if (data.error) {
						$("#image_store_error").html(data.error);
						return;
					}
					var str = "";
					createImageStoreRequest = data;
					var image_store_details = createImageStoreRequest.image_store_details;
					for (i = 0; i < image_store_details.length; i++) {
						str = str + "<div class=\"row\">";
						if (image_store_details[i].key.toLowerCase().indexOf(
								"password") != -1) {
							str = str
									+ "<label class=\"control-label col-md-4\" for="
									+ image_store_details[i].id
									+ ">"
									+ image_store_details[i].key_display_value
									+ ": </label>"
									+ "<div class=\"col-md-8\"><input type=\"password\" class=\"form-control\" id="
									+ image_store_details[i].id
									+ " placeholder=\"Enter "
									+ image_store_details[i].key
									+ "\"></div><br />";
						} else {
							str = str
									+ "<label class=\"control-label col-md-4\" for="
									+ image_store_details[i].id
									+ ">"
									+ image_store_details[i].key_display_value
									+ ": </label>"
									+ "<div class=\"col-md-8\"><input type=\"text\" class=\"form-control\" id="
									+ image_store_details[i].id
									+ " placeholder=\"Enter "
									+ image_store_details[i].key
									+ "\"></div><br />";
						}
						str = str + "</div>";
					}
					$("#edit_image_store_name").val(data.name);
					$("#edit_connector").val(data.connector);
					for (i = 0; i < data.artifact_types.length; i++) {
						$(
								"input[value=" + data.artifact_types[i]
										+ "].edit_artifacts").prop("checked",
								true);
					}

					jsonStr = JSON.stringify(createImageStoreRequest);
					saveButtonStr = '<button type=\'button\' class=\'btn btn-default\' onclick=\'updateImageStore('
							+ jsonStr + ', false)\'>Save</button>';
					$("#image_store_details_title").html("");
					$("#image_store_details_title").html(
							$("#image_store_name").val());
					$("#saveButton").html(saveButtonStr);
					$("#image_store_properties").html(str);
					$('#image_store').modal('hide');
					$('#image_store_details').modal('show');
				}
			});

}

function updateImageStore(updateImageStoreRequest, isEdit) {
	$("#image_store_error").html("");
	var image_store_details = updateImageStoreRequest.image_store_details;
	if (isEdit) {
		updateImageStoreRequest.artifact_types = $(
				'input:checkbox:checked.edit_artifacts').map(function() {
			return this.value;
		}).get();
		updateImageStoreRequest.name = $("#edit_image_store_name").val();
		$("#image_store_details_title").html($("#edit_image_store_name").val());

	} else {
		updateImageStoreRequest.artifact_types = $(
				'input:checkbox:checked.artifacts').map(function() {
			return this.value;
		}).get();
		updateImageStoreRequest.name = $("#image_store_name").val();
		updateImageStoreRequest.connector = $("#connector").val();
		$("#image_store_details_title").html($("#image_store_name").val());
	}

	for (i = 0; i < image_store_details.length; i++) {
		console.log(image_store_details.length);
		var id = updateImageStoreRequest.image_store_details[i]["id"];
		updateImageStoreRequest.image_store_details[i]["value"] = $("#" + id)
				.val();
		console.log("KEY :: " + id + " :: " + $("#" + id).val());
	}

	if (updateImageStoreRequest.artifact_types.length == 0) {
		$("#image_store_error").html(
				"Please select at least one supported artifact");
		return;
	}
	$.ajax({
		type : "PUT",
		url : "/v1/image-stores",
		contentType : "application/json",
		data : JSON.stringify(updateImageStoreRequest),
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			if (data.error) {
				$("#image_store_details_error").html(data.error);
				return;
			}

			$.ajax({
				type : "POST",
				url : "/v1/rpc/image-stores/" + updateImageStoreRequest.id
						+ "/validate",
				accept : "application/json",
				headers : {
					'Accept' : 'application/json'
				},
				success : function(data, status, xhr) {
					console.log(xhr.status);
					if (xhr.status == 200) {
						console.log(data.is_valid);
						if (data.is_valid) {
							resetAllFields();
							$("#image_store_details").modal('hide');
							imageStoreSettingPage();
						} else {
							$("#image_store_details_error").html(data.error);
						}
					} else {
						$("#image_store_details_error").html(response);
					}

				}
			});
		}

	});
}

function getImageStoreAndPopulateImageStore(imageStoreId) {
	$.ajax({
		type : "GET",
		url : "/v1/image-stores/" + imageStoreId,
		contentType : "application/json",
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			populateImageStore(data);
		}
	});
}

function editImageStore() {
	$('#editBackButton').show();
	$('#edit_image_store').modal('hide');
	$('#image_store_details').modal({
		backdrop : 'static',
		keyboard : false
	});
	$("#image_store_details_title").html($("#edit_image_store_name").val());
}

function populateImageStore(image_store) {
	$("#edit_image_store_name").val(image_store.name);
	$("#edit_connector").val(image_store.connector);

	$
			.ajax({
				type : "GET",
				url : "/v1/image-store-connectors/" + image_store.connector,
				dataType : "json",
				async : false,
				success : function(data) {
					var artifact_string = "";
					for ( var key in data.supported_artifacts) {
						artifact_string = artifact_string
								+ "<input type=\"checkbox\" class=\"edit_artifacts\" value="
								+ key + ">" + data.supported_artifacts[key]
								+ "</input><br/>";
					}
					$('#edit_artifacts').html(artifact_string);
				}
			});

	for (i = 0; i < image_store.artifact_types.length; i++) {
		$("input[value=" + image_store.artifact_types[i] + "].edit_artifacts")
				.prop("checked", true);
	}
	populateImageStoreDetails(image_store.image_store_details);
	$('#edit_image_store').modal('show');
	jsonStr = JSON.stringify(image_store);
	saveButtonStr = '<button type=\'button\' class=\'btn btn-default\' onclick=\'updateImageStore('
			+ jsonStr + ',true)\'>Save</button>';
	$("#saveButton").html(saveButtonStr);
}

function populateImageStoreDetails(image_store_details) {
	$("#image_store_properties").html("");
	var str = "";
	for (i = 0; i < image_store_details.length; i++) {
		var valueHolder = "";
		str = str + "<div class=\"row\">";
		if (image_store_details[i].value) {
			valueHolder = image_store_details[i].value;
			if (image_store_details[i].key.toLowerCase().indexOf("password") != -1) {
				str = str
						+ "<label align=\"right\" class=\"control-label col-md-6\" for="
						+ image_store_details[i].id
						+ ">"
						+ image_store_details[i].key_display_value
						+ ": </label>"
						+ "<div class=\"col-md-6\"><input type=\"password\" class=\"form-control\" id=\""
						+ image_store_details[i].id + "\" ></div><br />";
				$("#" + image_store_details[i].id).val(valueHolder);
			} else {
				str = str
						+ "<label align=\"right\" class=\"control-label col-md-6\" for="
						+ image_store_details[i].id
						+ ">"
						+ image_store_details[i].key_display_value
						+ ": </label>"
						+ "<div class=\"col-md-6\"><input type=\"text\" class=\"form-control\" id=\""
						+ image_store_details[i].id + "\" ></div><br />";
				$("#" + image_store_details[i].id).val(valueHolder);
			}
		} else {

			if (image_store_details[i].key.toLowerCase().indexOf("password") != -1) {
				valueHolder = image_store_details[i].key;
				str = str
						+ "<label align=\"right\" class=\"control-label col-md-6\" for="
						+ image_store_details[i].id
						+ ">"
						+ image_store_details[i].key_display_value
						+ ": </label>"
						+ "<div class=\"col-md-6\"><input type=\"password\" class=\"form-control\" id="
						+ image_store_details[i].id + " placeholder=\""
						+ valueHolder + "\"></div><br />";
			} else {
				valueHolder = image_store_details[i].key;
				str = str
						+ "<label align=\"right\" class=\"control-label col-md-6\" for="
						+ image_store_details[i].id
						+ ">"
						+ image_store_details[i].key_display_value
						+ ": </label>"
						+ "<div class=\"col-md-6\"><input type=\"text\" class=\"form-control\" id="
						+ image_store_details[i].id + " placeholder=\""
						+ valueHolder + "\"></div><br />";
			}

		}
		str = str + "</div>";
	}

	$("#image_store_properties").html(str);
	for (i = 0; i < image_store_details.length; i++) {
		var valueHolder = "";
		if (image_store_details[i].value) {
			valueHolder = image_store_details[i].value;
			$("#" + image_store_details[i].id).val(valueHolder);
		}
	}
}

function resetAllFields() {
	$("#image_store_details_error").html("");
	$("#image_store_error").html("");
	$("#image_store_name").val("");
	$('.edit_artifacts').prop("checked", false);
	$('.artifacts').prop("checked", false);
	$("#edit_image_store_name").val("");
	$("#edit_connector").val("");
	$("#connector").val("0");
}

function backToEdit() {
	$("#image_store_details_error").html("");
	$('#image_store_details').modal('hide');
	$('#edit_image_store').modal('show');
}
function deleteImageStore(imageStoreId) {
	$.ajax({
		type : "DELETE",
		url : "/v1/image-stores/" + imageStoreId,
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			imageStoreSettingPage();
		}
	});
}
function validateImageStore(imageStoreId) {
	$.ajax({
		type : "POST",
		url : "/v1/rpc/image-stores/" + imageStoreId + "/validate",
		accept : "application/json",
		headers : {
			'Accept' : 'application/json'
		},
		success : function(data, status, xhr) {
			$("span#" + imageStoreId).removeClass("glyphicon glyphicon-refresh");

			console.log(xhr.status);
			if (xhr.status == 200) {
				console.log(data.is_valid);
				if (data.is_valid) {
					$("span#" + imageStoreId).addClass("glyphicon glyphicon-ok");
				} else {
					$("span#" + imageStoreId).addClass("glyphicon glyphicon-remove");
					$("span#" + imageStoreId).attr("title", data.error);
				}
			} else {
				$("span#" + imageStoreId).addClass("glyphicon glyphicon-remove");
				$("span#" + imageStoreId).attr("title", "Unable to validate image store");
			}

		}
	});
	
}

function confirmDeleteOperationImageStore(deleteCallArr){
	var funcName = deleteCallArr[0];

	var args = "";
	if(deleteCallArr.length > 2){
		for(var i=2;i<deleteCallArr.length;i++){
			args=args + "'"+deleteCallArr[i]+"',";			
		}
		args = args.substring(0, args.length-1);
	}
	var func = funcName+"("+args+")";
	$("#image_store_confirm_delete").attr("onclick", func);
	$("#image_store_delete_confirmation_window_text").text(deleteCallArr[1]);
	$("#image_store_delete_confirmation_window").modal('show');
}