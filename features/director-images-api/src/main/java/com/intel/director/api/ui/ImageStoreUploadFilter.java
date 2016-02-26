package com.intel.director.api.ui;

import java.util.Date;

import com.intel.director.api.ImageStoreUploadTransferObject;

public class ImageStoreUploadFilter extends ImageStoreUploadTransferObject {

    protected Date from_date;

    protected Date to_date;

    protected String image_id;

    protected String image_name;

    protected String image_format;
    
    protected boolean  enableDeletedCheck;
    
	public String getImage_name() {
        return image_name;
    }

	
  







	public boolean isEnableDeletedCheck() {
		return enableDeletedCheck;
	}










	public void setEnableDeletedCheck(boolean enableDeletedCheck) {
		this.enableDeletedCheck = enableDeletedCheck;
	}










	public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getImage_format() {
        return image_format;
    }

    public void setImage_format(String image_format) {
        this.image_format = image_format;
    }

    public Date getFrom_date() {
        return from_date;
    }

    public void setFrom_date(Date from_date) {
        this.from_date = from_date;
    }

    public Date getTo_date() {
        return to_date;
    }

    public void setTo_date(Date to_date) {
        this.to_date = to_date;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

}
