var authorizationToken = window.opener.mainViewModel.loginViewModel.userProfile.authorizationToken();
var tokenString = 'Token ' +authorizationToken;
var imageType = window.opener.image_type_upload;
var dockerManualUpload = true;
var imageFormat = window.opener.image_format_upload;
var imageId = null;
var chunkSize = 5*1024*1024; // 5MB
var simultaneousUploads = 4;

var r = null;

/** Prepare Resumable upload service */
function prepareResumeUploadService(){
  if(!r.support) {
    $('.resumable-error').show();
  } else {
    $('.resumable-drop').show();
    //r.assignDrop($('.resumable-drop')[0]);
    //r.assignBrowse($('.resumable-browse')[0]);
    r.on('fileAdded', function(file){
        $('.resumable-progress, .resumable-list').show();
        $('.resumable-progress .progress-resume-link').hide();
        $('.resumable-progress .progress-pause-link').show();
        $('.resumable-list').append('<li class="resumable-file-'+file.uniqueIdentifier+'">Uploading <span class="resumable-file-name"></span> <span class="resumable-file-progress"></span>');
        $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-name').html(file.fileName);
        r.upload();
      });
    r.on('pause', function(){
        $('.resumable-progress .progress-resume-link').show();
        $('.resumable-progress .progress-pause-link').hide();
      });
    r.on('complete', function(){
        $('.resumable-progress .progress-resume-link, .resumable-progress .progress-pause-link').hide();
      });
    r.on('fileSuccess', function(file,message){
        $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-progress').html('(completed)');
        $('#upload').prop('disabled', true);
        if (imageType === 'Docker'){
          processDockerImage(imageId);
        }else{
          var goToTPDiv = $("#goToTrustPolicyPageDiv", opener.document);
          var uploadBtnDiv = $("#gotoUploadWindowDiv", opener.document);
          goToTPDiv.show();

          goToTPDiv.css("display", "block");
          window.setTimeout(function(){
            window.close();
          },3000);
        }
      });
    r.on('fileError', function(file, message){
        $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-progress').html('(file could not be uploaded: '+message+')');
      });
    r.on('fileProgress', function(file){
        $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-progress').html(Math.floor(file.progress()*100) + '%');
        $('.progress-bar').css({width:Math.floor(r.progress()*100) + '%'});
      });
  }
};

/** Prepare File Input box so that we can show selected file */
function prepareFileUploadBox(){
  $(document).on('change', ':file', function() {
    var input = $(this),
        numFiles = input.get(0).files ? input.get(0).files.length : 1,
        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
  });
};

/** Attach File Select Event Listener */
function attachFileSelectListener(){
  $(':file').on('fileselect', function(event, numFiles, label) {
    var input = $(this).parents('.input-group').find(':text'),
    log = numFiles > 1 ? numFiles + ' files selected' : label;
    if( input.length ) {
      input.val(log);
    } else {
      if( log ) alert(log);
    }
  });
};


$(document).ready(function() {
  prepareFileUploadBox();
  attachFileSelectListener();
  if(imageType === 'Docker'){
    $('.dockerInputs').show();
  } else {
    $('.dockerInputs').hide();
  }
});

/*
  Show/Hide Docker Options based on upload option selected
*/
function showHideDockerUploadOptions() {
  var status = document.querySelector('input[name="dockerUploadOption"]:checked').value;
  if (status == '0') {
    $('#fileUploadBox').show();
    $('input[name=dockerUploadOption][value="0"]').attr('checked', 'checked');
    $('#upload').attr('value','Upload');
    dockerManualUpload = true;
  } else {
    $('#fileUploadBox').hide();
    $('input[name=dockerUploadOption][value="1"]').attr('checked', 'checked');
    $('#upload').attr('value','Download');
    dockerManualUpload = false;
  }
};

function upload(event){
  $('#errorMsg').hide();
  var uploadFile = document.getElementById('myfile').files[0];
  uploadMetadataAndFile(uploadFile, event);
};


/**
  Create Image Metadata and upload file once we receive the UUID
 */
function uploadMetadataAndFile(uploadFile, event){
  var fName = null;
  var fileSize = null;
  var newFileName = null;
  if(uploadFile === null || uploadFile === undefined){
    fName = $('#dockerRepo').val() + ':' + $('#dockerTag').val();
    newFileName = $('#fileName').val() === '' ? fName : $('#fileName').val();
    fileSize = 0;
  }else {
    fName = uploadFile.webkitRelativePath||uploadFile.fileName||uploadFile.name; // Some confusion in different versions of Firefox;
    newFileName = $('#fileName').val() === '' ? fName : $('#fileName').val();
    fileSize = uploadFile.size;
  }
  var imageMetadataRequest = {
    'image_deployments' : imageType,
    'image_format' : imageFormat,
    'image_name' : newFileName,
    'image_size' : fileSize
  };

  if (imageType === 'Docker') {
    imageMetadataRequest.repository = $('#dockerRepo').val();
    imageMetadataRequest.tag = $('#dockerTag').val();
  }

  $('#upload').prop('disabled', true);

  $.ajax({
    type: 'POST',
    data : JSON.stringify(imageMetadataRequest),
    url: '/v1/images',
    contentType: 'application/json',
    dataType: 'json',
    beforeSend: function (request) {
      request.setRequestHeader('Authorization', tokenString);
    },
    success: function(data) {
      if(data.status === 'Error') {
        $('#errorMsg').html('File could not be uploaded: '+data.details);
        $('#errorMsg').show();
        $('#upload').prop('disabled', false);
        return;
      }
      imageId = data.id;
      if(dockerManualUpload || imageType === 'VM'){
        r = new Resumable({
          target: '/v1/rpc/images/upload/content/'+ imageId,
          testTarget: '/v1/images/upload/content/'+ imageId,
          chunkSize: chunkSize,
          simultaneousUploads: simultaneousUploads,
          testChunks: true,
          generateUniqueIdentifier : getImageId,
          throttleProgressCallbacks: 2,
          method: "octet",
          headers: {
            Authorization: tokenString
          }
        });
        prepareResumeUploadService(r);
        r.addFile(uploadFile, event);
      } else if (imageType === 'Docker'){
        downloadFromDockerHub(imageId);
      }
    },
    error: function(data, textStatus, errorThrown){
      if (imageType === 'Docker'){
        $('#errorMsg').html(data.responseJSON.details);
      }else {
        $('#errorMsg').html('file could not be uploaded: '+textStatus);
      }
      $('#errorMsg').show();
      $('#upload').prop('disabled', false);
    }
  });
};

/** Get Image Id from function */
function getImageId(){
  return imageId;
};


/**
  Download Docker from provider Docker Hub URL (This will happen at backend )
*/
function downloadFromDockerHub(imageId) {
  $.ajax({
    type : 'POST',
    url : '/v1/rpc/docker-pull/' + imageId,
    dataType : 'json',
    headers : {
      'Authorization' : tokenString
    },
    success : function(data) {
      if(data.error){
        $('#errorMsg').html('File could not be uploaded: '+data.details);
        $('#errorMsg').show();
        $('#upload').prop('disabled', false);
        return;
      }

      var goToTPDiv = $("#goToTrustPolicyPageDiv", opener.document);
      var uploadBtnDiv = $("#gotoUploadWindowDiv", opener.document);
      goToTPDiv.show();

      goToTPDiv.css("display", "block");
      window.setTimeout(function(){
        window.close();
      },3000);

      return;
     },
     error: function(data, textStatus, errorThrown){
       $('#errorMsg').html(data.details);
       $('#errorMsg').show();
       $('#upload').prop('disabled', false);
     }
  });
};

function processDockerImage(imageId) {
  $.ajax({
    type : "POST",
    url : "/v1/rpc/docker-process-uploaded-image/" + imageId,
    dataType : "json",
    headers : {
      "Authorization" : tokenString
    },
    success : function(data) {
      $('#upload').prop('disabled', false);
      if (data.error) {
        $('#errorMsg').html(data.details);
        $('#errorMsg').show();
        $('#upload').prop('disabled', false);
        return;
      }

      $('#upload').prop('disabled', false);
      var goToTPDiv = $("#goToTrustPolicyPageDiv",
          opener.document);
      var uploadBtnDiv = $("#gotoUploadWindowDiv",
          opener.document);
      goToTPDiv.show();

      goToTPDiv.css("display", "block");
      window.close();
      return;
    },
    error : function(data, textStatus, errorThrown) {
      if (data.responseJSON.error) {
        $('#errorMsg').html(data.responseJSON.error);
        $('#errorMsg').show();
        $('#upload').prop('disabled', false);
        return false;
      }
    }
  });
};