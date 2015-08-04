package com.intel.mtwilson.director.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageAttributeFields;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ui.ImageAttributesFilter;
import com.intel.director.api.ui.ImageAttributesOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyFields;
import com.intel.director.api.ui.TrustPolicyFilter;
import com.intel.director.api.ui.TrustPolicyOrderBy;

import com.intel.director.api.User;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;

import com.intel.mtwilson.director.dbservice.IPersistService;

public class DbServiceTest {

    IPersistService dBServiceImpl = new DbServiceImpl();
    List<ImageAttributes> imgAttributesList;
    List<ImageAttributes> persistedImgAttributesList;
    User myUser, myUser2, myUser3;
    Date currentDate, threeDaysBackDate, oneDaysBackDate, sevenDaysBackDate;
    String createdUserId, createdUserId2, createdUserId3;

    @Before
    public void setup() throws Exception {

        removeAllImagesEntries();
        removeAllPoliciesEntries();
        imgAttributesList = new ArrayList<ImageAttributes>();
        persistedImgAttributesList = new ArrayList<ImageAttributes>();
        User user = new User();
        user.setDisplayname("Aakash");
        user.setUsername("admin");
        user.setEmail("aa@gmail.com");
        myUser = dBServiceImpl.saveUser(user);
        createdUserId = myUser.getId();

        User user2 = new User();
        user2.setDisplayname("Anish");
        user2.setUsername("admin");
        user2.setEmail("aa@gmail.com");
        myUser2 = dBServiceImpl.saveUser(user2);
        createdUserId2 = myUser2.getId();
        User user3 = new User();
        user3.setDisplayname("Alok");
        user3.setUsername("admin");
        user3.setEmail("aa@gmail.com");
        myUser3 = dBServiceImpl.saveUser(user3);
        createdUserId3 = myUser3.getId();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -3);
        currentDate = new Date();
        threeDaysBackDate = c.getTime();

        c.add(Calendar.DATE, 2);
        oneDaysBackDate = c.getTime();

        c.add(Calendar.DATE, -5);
        sevenDaysBackDate = c.getTime();

    }

    @Test
    public void integrationTest() throws DbException {
        testImageDao();
        testPolicyDao();
    }

    public void testImageDao() throws DbException {

        ImageAttributes imgAttrs = new ImageAttributes(createdUserId,
                new Date(), createdUserId, new Date(), "img1", "qcow", "VM",
                "ACTIVE", 512, 24, null, false, "C://temp");

        // // new ImageAttributes("img1", "qcow", "VM", new Date(), "ACTIVE",
        // 512, 24, null, false, "C://temp");
        ImageAttributes img = dBServiceImpl.saveImageMetadata(imgAttrs);

        Assert.assertTrue("Create operation fail", img.getId() != null);

        String imageId = img.getId();

        img.setMounted_by_user_id((new UUID()).toString());
        dBServiceImpl.updateImage(img);
        ImageAttributes retrieveImage = dBServiceImpl.fetchImageById(imageId);
        Assert.assertTrue("Update operation fail",
                retrieveImage.getMounted_by_user_id() != null);

        imgAttributesList
                .add(new ImageAttributes(createdUserId, threeDaysBackDate,
                                createdUserId, threeDaysBackDate, "img2", "qcow",
                                "BareMetal", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
                createdUserId, currentDate, "img3", "qcow", "VM", "ACTIVE",
                512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                oneDaysBackDate, createdUserId, oneDaysBackDate, "img4",
                "qcow", "VM", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList
                .add(new ImageAttributes(createdUserId, threeDaysBackDate,
                                createdUserId, threeDaysBackDate, "img5", "qcow",
                                "BareMetal", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu1",
                "qcow", "VM", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu2",
                "qcow", "VM", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList
                .add(new ImageAttributes(createdUserId, threeDaysBackDate,
                                createdUserId, threeDaysBackDate, "ubuntu3", "qcow",
                                "BareMetal", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu4",
                "qcow", "VM", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
                createdUserId, currentDate, "ubuntu5", "qcow", "VM",
                "UPLOADED", 512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
                createdUserId, currentDate, "ubuntu6", "vhd", "VM", "UPLOADED",
                512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                sevenDaysBackDate, createdUserId, sevenDaysBackDate, "ubuntu7",
                "vhd", "BareMetal", "UPLOADED", 512, 24, null, false,
                "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu8",
                "qcow", "VM", "ACTIVE", 1024, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                sevenDaysBackDate, createdUserId, sevenDaysBackDate, "ubuntu9",
                "qcow", "VM", "ACTIVE", 512, 24, null, false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                threeDaysBackDate, createdUserId, threeDaysBackDate,
                "ubuntu10", "test", "BareMetal", "ACTIVE", 512, 24, null,
                false, "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                threeDaysBackDate, createdUserId, threeDaysBackDate,
                "ubuntu11", "test", "VM", "ACTIVE", 512, 24, null, false,
                "C://temp"));

        imgAttributesList.add(new ImageAttributes(createdUserId,
                sevenDaysBackDate, createdUserId, sevenDaysBackDate,
                "ubuntu12", "qcow", "VM", "ACTIVE", 512, 24, null, false,
                "C://temp"));

        imgAttributesList
                .add(new ImageAttributes(createdUserId, threeDaysBackDate,
                                createdUserId, threeDaysBackDate, "fedora1", "qcow",
                                "BareMetal", "ACTIVE", 512, 24, null, false, "C://temp"));
        imgAttributesList.add(new ImageAttributes(createdUserId,
                threeDaysBackDate, createdUserId, threeDaysBackDate, "fedora2",
                "test", "VM", "UPLOADED", 512, 24, null, false, "C://temp"));
        imgAttributesList
                .add(new ImageAttributes(createdUserId, sevenDaysBackDate,
                                createdUserId, sevenDaysBackDate, "fedora3", "qcow",
                                "BareMetal", "ACTIVE", 512, 24, null, false, "C://temp"));
        imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
                createdUserId, currentDate, "fedora4", "qcow", "VM",
                "UPLOADED", 512, 24, null, false, "C://temp"));

        for (int i = 0; i < imgAttributesList.size(); i++) {
            persistedImgAttributesList.add(dBServiceImpl.saveImageMetadata(imgAttributesList.get(i)));
        }

        ImageAttributesFilter searchFilter = new ImageAttributesFilter();
        searchFilter.setName("ubun");

        searchFilter.setFrom_created_date(sevenDaysBackDate);
        searchFilter.setTo_created_date(oneDaysBackDate);
        searchFilter.setImage_deployments("VM");
        ImageAttributesOrderBy imgOrderBy = new ImageAttributesOrderBy();
        imgOrderBy.setImgFields(ImageAttributeFields.CREATED_DATE);
        imgOrderBy.setOrderBy(OrderByEnum.DESC);
        List<ImageAttributes> searchImageList = dBServiceImpl.fetchImages(
                searchFilter, imgOrderBy);

        System.out.println("#######################Search############"
                + searchImageList.size());

        for (ImageAttributes imgAttr : searchImageList) {
            System.out.println("### imgAttr::" + imgAttr);
        }
        Assert.assertTrue(
                "fetchImages(searchFilter, imgOrderBy) fail",
                (searchImageList.size() == 7)
                && (searchImageList.get(0).getName()
                .equalsIgnoreCase("ubuntu8")));

        int totalCount = dBServiceImpl.getTotalImagesCount();

        int serachTotalCount = dBServiceImpl.getTotalImagesCount(searchFilter);

        Assert.assertTrue("getTotalCount() method fail", totalCount == 21);
        Assert.assertTrue("getTotalCount(searchFilter) method fail",
                serachTotalCount == 7);

        List<ImageAttributes> searchImagePaginatedList = dBServiceImpl
                .fetchImages(searchFilter, imgOrderBy, 2, 2);
        for (ImageAttributes imgAttr : searchImagePaginatedList) {
            System.out.println("### imgAttr::" + imgAttr);
        }
        Assert.assertTrue(
                "fetchImages(searchFilter, imgOrderBy,firstElement,maxelements method fail",
                searchImagePaginatedList.size() == 2
                && searchImagePaginatedList.get(0).getName()
                .equalsIgnoreCase("ubuntu4"));

        dBServiceImpl.destroyImage(img);

    }

    public void removeAllImagesEntries() throws DbException {

        List<ImageAttributes> imgAttributesList = dBServiceImpl
                .fetchImages(null);
        for (ImageAttributes imgAttr : imgAttributesList) {
            dBServiceImpl.destroyImage(imgAttr);
        }

    }

    public void removeAllPoliciesEntries() throws DbException {

        List<TrustPolicy> policiesList = dBServiceImpl
                .fetchPolicies(null);
        for (TrustPolicy policy : policiesList) {
            dBServiceImpl.destroyPolicy(policy);
        }

    }

    public void clearImageEntries() throws DbException {

        for (ImageAttributes imgAttr : imgAttributesList) {
            dBServiceImpl.destroyImage(imgAttr);
        }

    }

    public void testPolicyDao() throws DbException {
        try {

            List<TrustPolicy> deleteTrustPoliciesList = new ArrayList<TrustPolicy>();
            TrustPolicy trustPolicy1 = new TrustPolicy(createdUserId, sevenDaysBackDate, createdUserId, sevenDaysBackDate, "trustPolicy1desc", "<abc>TrustPolicy xml1 field</abc>", "TrustPolicy1_" + sevenDaysBackDate, persistedImgAttributesList.get(0));
            deleteTrustPoliciesList.add(trustPolicy1);
            TrustPolicy trustPolicyCreated = dBServiceImpl.savePolicy(trustPolicy1);
            Assert.assertTrue("Create operation fail", trustPolicyCreated.getId() != null);
            String trustPolicyId = trustPolicyCreated.getId();
            trustPolicyCreated.setDescription("Updated Description");
            dBServiceImpl.updatePolicy(trustPolicyCreated);
            TrustPolicy trustPolicyRetrieved = dBServiceImpl.fetchPolicyById(trustPolicyId);
            System.out.println("Image Name::" + trustPolicyRetrieved.getImgAttributes().getName());
            System.out.println("trustPolicyRetrieved.getDescription()::" + trustPolicyRetrieved.getDescription());
            Assert.assertTrue("Update operation fail",
                    (trustPolicyRetrieved.getDescription().equals("Updated Description")));
            List<TrustPolicy> trustPoliciesList = new ArrayList<TrustPolicy>();
            trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy2desc", "<abc>TrustPolicy xml2 field</abc>", "TrustPolicy2_" + sevenDaysBackDate, persistedImgAttributesList.get(1)));
            trustPoliciesList.add(new TrustPolicy(createdUserId, threeDaysBackDate, createdUserId, threeDaysBackDate, "trustPolicy3desc", "<abc>TrustPolicy xml3 field</abc>", "TrustPolicy3_" + sevenDaysBackDate, persistedImgAttributesList.get(2)));
            trustPoliciesList.add(new TrustPolicy(createdUserId2, oneDaysBackDate, createdUserId2, oneDaysBackDate, "trustPolicy4desc", "<abc>TrustPolicy xml4 field</abc>", "TrustPolicy4_" + sevenDaysBackDate, persistedImgAttributesList.get(3)));
            trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy5desc", "<abc>TrustPolicy xml5 field</abc>", "TrustPolicy5_" + sevenDaysBackDate, persistedImgAttributesList.get(4)));
            trustPoliciesList.add(new TrustPolicy(createdUserId, threeDaysBackDate, createdUserId, threeDaysBackDate, "trustPolicy6desc", "<abc>TrustPolicy xml6 field</abc>", "TrustPolicy6_" + sevenDaysBackDate, persistedImgAttributesList.get(5)));
            trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy7desc", "<abc>TrustPolicy xml7 field</abc>", "TrustPolicy7_" + sevenDaysBackDate, persistedImgAttributesList.get(6)));
            trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy8desc", "<abc>TrustPolicy xml8 field</abc>", "TrustPolicy8_" + sevenDaysBackDate, persistedImgAttributesList.get(7)));
            trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy9desc", "<abc>TrustPolicy xml9 field</abc>", "TrustPolicy9_" + sevenDaysBackDate, persistedImgAttributesList.get(8)));

            for (TrustPolicy trustPolicy : trustPoliciesList) {

                dBServiceImpl.savePolicy(trustPolicy);
            }

            TrustPolicyFilter trustPolicyFilter = new TrustPolicyFilter();
            trustPolicyFilter.setCreated_by_user_id(createdUserId2);

            TrustPolicyOrderBy tpOrderBy = new TrustPolicyOrderBy();
            tpOrderBy.setTrustPolicyFields(TrustPolicyFields.CREATED_DATE);

            tpOrderBy.setOrderBy(OrderByEnum.DESC);
            List<TrustPolicy> searchPolicyList = dBServiceImpl.fetchPolicies(
                    trustPolicyFilter, tpOrderBy);

            System.out.println("#######################Search Trust policies############"
                    + searchPolicyList.size());

            for (TrustPolicy tp : searchPolicyList) {
                System.out.println("### trust policy::" + tp);
                System.out.println("### searchPolicyList imageName::" + tp.getImgAttributes().getName());
            }
            Assert.assertTrue(
                    "fetchPolicies(searchFilter, OrderBy) fail",
                    (searchPolicyList.size() == 6)
                    && (searchPolicyList.get(0).getDescription()
                    .equalsIgnoreCase("trustPolicy4desc")));

            int totalCount = dBServiceImpl.getTotalPoliciesCount();

            int serachTotalCount = dBServiceImpl.getTotalPoliciesCount(trustPolicyFilter);
            System.out.println("#######################totalCount_ trust policy######::" + totalCount);
            Assert.assertTrue("getTotalPoliesCount() method fail", totalCount == 9);
            Assert.assertTrue("getTotalPoliesCount(searchFilter) method fail",
                    serachTotalCount == 6);

            List<TrustPolicy> searchPoliciesPaginatedList = dBServiceImpl
                    .fetchPolicies(trustPolicyFilter, tpOrderBy, 2, 2);
            for (TrustPolicy tp : searchPoliciesPaginatedList) {
                System.out.println("### trustPolicy after paging::" + tp);
            }
            Assert.assertTrue(
                    "fetchImages(searchFilter, imgOrderBy,firstElement,maxelements method fail",
                    searchPoliciesPaginatedList.size() == 2
            );
            TrustPolicyFilter trustPolicyFilterByImage = new TrustPolicyFilter();
            trustPolicyFilterByImage.setFormat("qcow");
            TrustPolicyOrderBy tpOrderByImage = new TrustPolicyOrderBy();
            tpOrderByImage.setTrustPolicyFields(TrustPolicyFields.IMAGE_NAME);

            tpOrderByImage.setOrderBy(OrderByEnum.DESC);

            List<TrustPolicy> searchPolicyFilterByImageList = dBServiceImpl.fetchPolicies(
                    trustPolicyFilterByImage, tpOrderByImage);
            for (TrustPolicy tp : searchPolicyFilterByImageList) {
                System.out.println("### trustPolicy after searchPolicyFilter orderByImageList desc::" + tp);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }

    }

}
