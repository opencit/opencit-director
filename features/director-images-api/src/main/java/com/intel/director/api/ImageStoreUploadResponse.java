package com.intel.director.api;

import java.util.Date;

public class ImageStoreUploadResponse implements StoreResponse{

	  public String id;
	    public String image_id;
	    public String image_uri;
	    public Date date;
	    public String tmp_location;
	    public String checksum;
	    public String status;
	    public Long image_size;
	    public Long sent;
	    public String uploaded_by_user_id;

	    public String mtwilson_trust_policy_location;

	    public String container_format;
	    public String disk_format;

	    public ImageStoreUploadResponse() {
	        super();
	    }
	    
	    
	    
	    public String getContainer_format() {
			return container_format;
		}



		public void setContainer_format(String container_format) {
			this.container_format = container_format;
		}



		public String getDisk_format() {
			return disk_format;
		}



		public void setDisk_format(String disk_format) {
			this.disk_format = disk_format;
		}



		public String getMtwilson_trust_policy_location() {
			return mtwilson_trust_policy_location;
		}


		public void setMtwilson_trust_policy_location(
				String mtwilson_trust_policy_location) {
			this.mtwilson_trust_policy_location = mtwilson_trust_policy_location;
		}


		public ImageStoreUploadResponse(String image_id,
	            String image_uri, Date date, String tmp_location,
	            String checksum, String status, Long image_size, Long sent) {
	        super();
	        this.image_id = image_id;

	        this.image_uri = image_uri;
	        this.date = date;
	        this.tmp_location = tmp_location;
	        this.checksum = checksum;
	        this.status = status;
	        this.image_size = image_size;
	        this.sent = sent;
	    }

	    public ImageStoreUploadResponse(String id, String image_id,
	            String image_uri, Date date,
	            String tmp_location, String checksum, String status,
	            Long image_size, Long sent) {
	        super();
	        this.id = id;
	        this.image_id = image_id;

	        this.image_uri = image_uri;
	        this.date = date;
	        this.tmp_location = tmp_location;
	        this.checksum = checksum;
	        this.status = status;
	        this.image_size = image_size;
	        this.sent = sent;
	    }

	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public String getImage_id() {
	        return image_id;
	    }

	    public void setImage_id(String image_id) {
	        this.image_id = image_id;
	    }

	    public String getImage_uri() {
	        return image_uri;
	    }

	    public void setImage_uri(String image_uri) {
	        this.image_uri = image_uri;
	    }

	    public Date getDate() {
	        return date;
	    }

	    public void setDate(Date date) {
	        this.date = date;
	    }

	    public String getTmp_location() {
	        return tmp_location;
	    }

	    public void setTmp_location(String tmp_location) {
	        this.tmp_location = tmp_location;
	    }

	    public String getChecksum() {
	        return checksum;
	    }

	    public void setChecksum(String checksum) {
	        this.checksum = checksum;
	    }

	    public String getStatus() {
	        return status;
	    }

	    public void setStatus(String status) {
	        this.status = status;
	    }

	    public Long getImage_size() {
	        return image_size;
	    }

	    public void setImage_size(Long image_size) {
	        this.image_size = image_size;
	    }

	    public Long getSent() {
	        return sent;
	    }

	    public void setSent(Long sent) {
	        this.sent = sent;
	    }

	    public String getUploaded_by_user_id() {
	        return uploaded_by_user_id;
	    }

	    public void setUploaded_by_user_id(String uploaded_by_user_id) {
	        this.uploaded_by_user_id = uploaded_by_user_id;
	    }

	    @Override
	    public String toString() {
	        return "ImageStoreUploadResponse [id=" + id + ", image_id=" + image_id
	                + ", trust_policy_id=" + ", image_uri="
	                + image_uri + ", date=" + date + ", tmp_location="
	                + tmp_location + ", checksum=" + checksum + ", status="
	                + status + ", image_size=" + image_size + ", sent=" + sent
	                + "]";
	    }
	
}
