package com.intel.director.api.ui;

import com.intel.director.api.ImageStoreUploadTransferObject;
import java.util.Date;

public class ImageStoreUploadFilter extends ImageStoreUploadTransferObject {

    protected Date from_date;

    protected Date to_date;

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

    @Override
    public String toString() {
        return "ImageStoreUploadResponseFilter [from_date=" + from_date
                + ", to_date=" + to_date + ", id=" + id + ", image_id="
                + image_id + ", trust_policy_id="
                + ", image_uri=" + image_uri + ", date=" + date
                + ", tmp_location=" + tmp_location + ", checksum=" + checksum
                + ", status=" + status + ", image_size=" + image_size
                + ", sent=" + sent + "]";
    }

}
