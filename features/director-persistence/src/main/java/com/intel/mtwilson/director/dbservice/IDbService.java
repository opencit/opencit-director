package com.intel.mtwilson.director.dbservice;

import java.util.List;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ui.ImageAttributesFilter;
import com.intel.director.api.ui.ImageAttributesOrderBy;
import com.intel.director.api.User;
import com.intel.mtwilson.director.db.exception.DbException;

public interface IDbService {

    public ImageAttributes saveImageMetadata(ImageAttributes img) throws DbException;

    public void updateImage(ImageAttributes img) throws DbException;

    public void destroyImage(ImageAttributes img) throws DbException;

    public List<ImageAttributes> fetchImages(ImageAttributesOrderBy orderBy) throws DbException;

    public List<ImageAttributes> fetchImages(ImageAttributesOrderBy orderBy, int firstRecord, int maxRecords) throws DbException;

    public List<ImageAttributes> fetchImages(ImageAttributesFilter imgFilter,
            ImageAttributesOrderBy orderBy) throws DbException;

    public List<ImageAttributes> fetchImages(ImageAttributesFilter imgFilter,
            ImageAttributesOrderBy orderBy, int firstRecord, int maxRecords) throws DbException;

    public ImageAttributes fetchImageById(String id) throws DbException;

    public int getTotalImagesCount() throws DbException;

    public int getTotalImagesCount(ImageAttributesFilter imgFilter) throws DbException;

    public User saveUser(User user) throws DbException;

    public void updateUser(User user) throws DbException;

}
