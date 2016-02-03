package com.intel.director.api.ui;

import java.util.Date;

import com.intel.director.api.ImageAttributes;

public class ImageAttributesFilter extends ImageAttributes {

    protected Date from_created_date;
    protected Date to_created_date;

    public ImageAttributesFilter(String created_by_user_id, Date created_date,
            String edited_by_user_id, Date edited_date, String id, String name,
            String format, String image_deployments, String status,
            Long image_size, Long sent, String mounted_by_user_id,
            boolean deleted, String location, Date from_created_date,
            Date to_created_date) {
        super(created_by_user_id, created_date, edited_by_user_id, edited_date,
                id, name, format, image_deployments, status, image_size, sent,
                mounted_by_user_id, deleted, location);
        this.from_created_date = from_created_date;
        this.to_created_date = to_created_date;
    }

    public ImageAttributesFilter() {
        super();
    }

    public Date getFrom_created_date() {
        return from_created_date;
    }

    public void setFrom_created_date(Date from_created_date) {
        this.from_created_date = from_created_date;
    }

    public Date getTo_created_date() {
        return to_created_date;
    }

    public void setTo_created_date(Date to_created_date) {
        this.to_created_date = to_created_date;
    }

    @Override
    public String toString() {
        return "ImageAttributesFilter [from_created_date=" + from_created_date
                + ", to_created_date=" + to_created_date + ", id=" + id
                + ", image_name=" + image_name + ", format=" + image_format
                + ", image_deployments=" + image_deployments + ", status="
                + status + ", image_size=" + image_size + ", sent=" + sent
                + ", mounted_by_user_id=" + mounted_by_user_id + ", deleted="
                + deleted + ", location=" + location + ", created_by_user_id="
                + created_by_user_id + ", created_date=" + created_date
                + ", edited_by_user_id=" + edited_by_user_id + ", edited_date="
                + edited_date + "]";
    }

}
