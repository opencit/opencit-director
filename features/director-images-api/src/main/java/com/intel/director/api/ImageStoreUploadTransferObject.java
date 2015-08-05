package com.intel.director.api;

import java.util.Date;

public class ImageStoreUploadTransferObject {

    protected String id;

    protected String image_uri;
    protected Date date;
    protected String tmp_location;
    protected String checksum;
    protected String status;
    protected Integer image_size;
    protected Integer sent;
    protected String uploaded_by_user_id;
    protected ImageAttributes img;

    public ImageStoreUploadTransferObject(String image_uri, Date date,
            String tmp_location, String checksum, String status,
            Integer image_size, Integer sent, String uploaded_by_user_id,
            ImageAttributes img) {
        super();
        this.image_uri = image_uri;
        this.date = date;
        this.tmp_location = tmp_location;
        this.checksum = checksum;
        this.status = status;
        this.image_size = image_size;
        this.sent = sent;
        this.uploaded_by_user_id = uploaded_by_user_id;
        this.img = img;
    }

    public ImageStoreUploadTransferObject() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getImage_size() {
        return image_size;
    }

    public void setImage_size(Integer image_size) {
        this.image_size = image_size;
    }

    public Integer getSent() {
        return sent;
    }

    public void setSent(Integer sent) {
        this.sent = sent;
    }

    public String getUploaded_by_user_id() {
        return uploaded_by_user_id;
    }

    public void setUploaded_by_user_id(String uploaded_by_user_id) {
        this.uploaded_by_user_id = uploaded_by_user_id;
    }

    public ImageAttributes getImg() {
        return img;
    }

    public void setImg(ImageAttributes img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "ImageStoreUploadTransferObject [id=" + id + ", image_uri="
                + image_uri + ", date=" + date + ", tmp_location="
                + tmp_location + ", checksum=" + checksum + ", status="
                + status + ", image_size=" + image_size + ", sent=" + sent
                + ", uploaded_by_user_id=" + uploaded_by_user_id + ", img="
                + img + "]";
    }

}
