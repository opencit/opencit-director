package com.intel.director.api;

public class ImageStoreUploadTransferObject extends ImageStoreUploadResponse {

    protected ImageAttributes img;

    public ImageAttributes getImg() {
        return img;
    }

    public void setImg(ImageAttributes img) {
        this.img = img;
    }

}
