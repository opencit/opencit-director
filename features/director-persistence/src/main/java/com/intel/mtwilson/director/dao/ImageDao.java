package com.intel.mtwilson.director.dao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFields;
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.api.ui.ImageInfoOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.SearchImageByPolicyCriteria;
import com.intel.director.api.ui.SearchImageByUploadCriteria;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwTrustPolicy;
import com.intel.mtwilson.director.db.exception.DbException;

public class ImageDao extends BaseDao{
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageDao.class);
	
	public ImageDao(EntityManagerFactory emf) {
		super(emf);
	}


	public MwImage findMwImageById(String id) throws DbException {
		return (MwImage) get(MwImage.class, id);
	}

	public void destroyImage(MwImage img) throws DbException {
		delete(MwImage.class, img);
	}

	public List<ImageInfo> findMwImageEntities(ImageInfoFilter imgFilter,
			ImageInfoOrderBy orderBy) throws DbException {
		return findMwImageEntities(true, -1, -1, imgFilter, orderBy);
	}

	public List<ImageInfo> findMwImageEntities(int firstResult, int maxResults,
			ImageInfoFilter imgFilter, ImageInfoOrderBy orderBy)
			throws DbException {
		return findMwImageEntities(false, firstResult, maxResults, imgFilter,
				orderBy);
	}

	private List<ImageInfo> findMwImageEntities(boolean all, int firstResult,
			int maxResults, ImageInfoFilter imgFilter, ImageInfoOrderBy orderBy)
			throws DbException {
		EntityManager em = getEntityManager();
		try {

			Map<ImageInfoFields, String> imageInfotoDataMapper = mapper
					.getImageInfoToDataColumnsMapper();

			StringBuffer queryString = new StringBuffer(
					"select id,name,image_format,image_deployments,created_by_user_id,created_date,"
							+ "location,mounted_by_user_id,sent,status,edited_by_user_id,edited_date,deleted,"
							+ "trust_policy_id,trust_policy_name,trust_policy_draft_id,trust_policy_draft_name,image_upload_count,content_length,repository,tag,policy_upload_count,upload_variables_md5,tmp_location,drives from mw_image_info_view where deleted=false and (status='In Progress' or status='Complete') and 1=1");

			if (imgFilter != null) {
				if (imgFilter.getId() != null) {
					queryString.append(" and id like '%" + imgFilter.getId()
							+ "%'");
				}

				if (imgFilter.getImage_format() != null) {
					queryString.append(" and image_format like '%"
							+ imgFilter.getImage_format() + "%'");
				}
				if (imgFilter.getImage_name() != null) {
					queryString.append(" and name like '%"
							+ imgFilter.getImage_name() + "%'");
				}
				if (imgFilter.getImage_deployments() != null) {
					queryString.append(" and image_deployments like '%"
							+ imgFilter.getImage_deployments() + "%'");
				}
				if (imgFilter.getCreated_by_user_id() != null) {
					queryString.append(" and created_by_user_id like '%"
							+ imgFilter.getCreated_by_user_id() + "%'");
				}

				if (imgFilter.getFrom_created_date() != null) {
					queryString.append(" and  created_date >=  '"
							+ (imgFilter.getFrom_created_date()) + "'");
					// /
					// predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImage.<java.sql.Date>
					// get(policyAttributestoDataMapper.get(TrustPolicyFields.CREATED_DATE)),new
					// java.sql.Date(trustPolicyFilter.getFrom_created_date().getTime())
					// ));
				}
				if (imgFilter.getTo_created_date() != null) {
					queryString.append(" and  created_date <=  '"
							+ (imgFilter.getTo_created_date()) + "'");
					// /
					// predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImage.<java.sql.Date>
					// get(policyAttributestoDataMapper.get(TrustPolicyFields.CREATED_DATE)),new
					// java.sql.Date(trustPolicyFilter.getTo_created_date().getTime())
					// ));
				}

				if (imgFilter.getPolicyCriteria() != null) {
					if (imgFilter.getPolicyCriteria() == SearchImageByPolicyCriteria.WITH) {
						queryString.append(" and trust_policy_id is not NULL");
					} else if (imgFilter.getPolicyCriteria() == SearchImageByPolicyCriteria.WITHOUT) {
						queryString.append(" and trust_policy_id is NULL");
					}
				}

				if (imgFilter.getUploadCriteria() != null) {
					if (imgFilter.getUploadCriteria() == SearchImageByUploadCriteria.UPLOADED) {
						queryString.append(" and image_upload_count>0");
					} else if (imgFilter.getUploadCriteria() == SearchImageByUploadCriteria.NOT_UPLOADED) {
						queryString.append(" and image_upload_count=0");
					}
				}
			}

			if (orderBy != null) {
				if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
					queryString.append(" order by "
							+ imageInfotoDataMapper.get(orderBy.getImgFields())
							+ "  ASC");

				} else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
					queryString.append(" order by "
							+ imageInfotoDataMapper.get(orderBy.getImgFields())
							+ "  DESC");
				}
			}

			if (!all) {
				if (maxResults != 0)
					queryString.append(" limit " + maxResults + " ");
				if (firstResult != 0)
					queryString.append(" offset " + firstResult + " ");

			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Query query = emf.createEntityManager().createNativeQuery(
					queryString.toString());
			List<ImageInfo> imageInfoList = new ArrayList<ImageInfo>();
			if (query.getResultList() != null) {
				List<Object[]> imageObjList = query.getResultList();
				for (Object[] imageObj : imageObjList) {
					ImageInfo imgInfo = new ImageInfo();

					imgInfo.setId((String) imageObj[0]);
					imgInfo.setImage_name((String) imageObj[1]);
					imgInfo.setImage_format((String) imageObj[2]);
					imgInfo.setImage_deployments((String) imageObj[3]);
					imgInfo.setCreated_by_user_id((String) imageObj[4]);
					
					Timestamp timestamp = (Timestamp) imageObj[5];
			        String format = dateFormat.format(timestamp);
			        Date parse = dateFormat.parse(format);
			        Calendar calendar = Calendar.getInstance();
			        calendar.setTime(parse);
					imgInfo.setCreated_date(calendar);
					
					
					imgInfo.setLocation((String) imageObj[6]);
					imgInfo.setMounted_by_user_id((String) imageObj[7]);
					imgInfo.setSent((Long) imageObj[8]);
					imgInfo.setStatus((String) imageObj[9]);
					imgInfo.setEdited_by_user_id((String) imageObj[10]);
					
					timestamp = (Timestamp) imageObj[11];
			        format = dateFormat.format(timestamp);
			        parse = dateFormat.parse(format);
			        calendar = Calendar.getInstance();
			        calendar.setTime(parse);
					imgInfo.setEdited_date(calendar);
					
					imgInfo.setDeleted((Boolean) imageObj[12]);
					imgInfo.setTrust_policy_id((String) imageObj[13]);
					imgInfo.setTrust_policy_name((String) imageObj[14]);
					imgInfo.setTrust_policy_draft_id((String) imageObj[15]);
					imgInfo.setTrust_policy_draft_name((String) imageObj[16]);
					imgInfo.setImage_uploads_count(((Long) imageObj[17]).intValue());
					imgInfo.setImage_size(((Long) imageObj[18]));
					imgInfo.setRepository((String) imageObj[19]);
					imgInfo.setTag((String) imageObj[20]);
					imgInfo.setPolicy_uploads_count(((Long) imageObj[21]).intValue());
					imgInfo.setUploadVariableMD5((String) imageObj[22]);
					imgInfo.setTmpLocation((String) imageObj[23]);
					imgInfo.setPartition((String) imageObj[24]);
					imageInfoList.add(imgInfo);
				}
			}
			return imageInfoList;

		} catch (Exception e) {
			log.error("findMwImageEntities failed",e);
			throw new DbException("ImageDao,findMwImageEntities failed", e);
		}

		finally {
			em.close();
		}
	}
	
	public ImageInfo findMwImage(String id) throws DbException {
		EntityManager em = getEntityManager();
		try {
			MwImage mwImage = em.find(MwImage.class, id);
			if(mwImage==null){
				return null;
			}
			ImageInfo imgInfo = new ImageInfo();

			imgInfo.setId(mwImage.getId());
			imgInfo.setImage_deployments(mwImage.getImageDeploymentType());
			imgInfo.setImage_format(mwImage.getImageFormat());
			imgInfo.setImage_name(mwImage.getName());
			;
			imgInfo.setMounted_by_user_id(mwImage.getMountedByUserId());
			imgInfo.setLocation(mwImage.getLocation());
			imgInfo.setDeleted(mwImage.isDeleted());
			imgInfo.setCreated_by_user_id(mwImage.getCreatedByUserId());
			imgInfo.setEdited_by_user_id(mwImage.getEditedByUserId());
			imgInfo.setCreated_date(mwImage.getCreatedDate());
			imgInfo.setEdited_date(mwImage.getEditedDate());
			imgInfo.setImage_size(mwImage.getContentLength());
			imgInfo.setStatus(mwImage.getStatus());
			imgInfo.setSent(mwImage.getSent());
			imgInfo.setUploadVariableMD5(mwImage.getUploadVariablesMd5());
			Collection<MwTrustPolicy> trustPolicyCollection=mwImage.getTrustPolicyCollection();
			if (trustPolicyCollection!= null && trustPolicyCollection.size()>0 ) {
				for(MwTrustPolicy mwtp: trustPolicyCollection){
					if(!mwtp.isArchive()){
						imgInfo.setTrust_policy_id(mwtp.getId());
						imgInfo.setTrust_policy_name(mwtp.getName());
						break;
					}
				}
			
			}
			imgInfo.setPartition(mwImage.getPartition());
			if (mwImage.getTrustPolicyDraft() != null) {
				imgInfo.setTrust_policy_draft_id(mwImage.getTrustPolicyDraft()
						.getId());
				imgInfo.setTrust_policy_draft_name(mwImage
						.getTrustPolicyDraft().getName());
			}
			if(mwImage.getRepository() != null){
				imgInfo.setRepository(mwImage.getRepository());
			}
			if(mwImage.getTag() != null){
				imgInfo.setTag(mwImage.getTag());
			}
			return imgInfo;
		} catch (Exception e) {
			log.error("findMwImage() failed",e);
			throw new DbException("ImageDao,findMwImage() failed", e);
		}

		finally {
			em.close();
		}

	}

	public int getMwImageCount() throws DbException {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery<Long> cq = em.getCriteriaBuilder().createQuery(
					Long.class);
			Root<MwImage> rt = cq.from(MwImage.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} catch (Exception e) {
			log.error("getMwImageCount failed",e);
			throw new DbException("ImageDao,getMwImageCount() failed", e);
		}

		finally {
			em.close();
		}
	}

	public int getMwImageCount(ImageInfoFilter imgFilter) throws DbException {
		EntityManager em = getEntityManager();
		try {
			StringBuffer queryString = new StringBuffer(
					"select count(*) from mw_image_info_view where 1=1");

			if (imgFilter != null) {
				if (imgFilter.getId() != null) {
					queryString.append(" and id like '%" + imgFilter.getId()
							+ "%'");
				}

				if (imgFilter.getImage_format() != null) {
					queryString.append(" and image_format like '%"
							+ imgFilter.getImage_format() + "%'");
				}
				if (imgFilter.getImage_name() != null) {
					queryString.append(" and name like '%"
							+ imgFilter.getImage_name() + "%'");
				}
				if (imgFilter.getImage_deployments() != null) {
					queryString.append(" and image_deployments like '%"
							+ imgFilter.getImage_deployments() + "%'");
				}
				if (imgFilter.getCreated_by_user_id() != null) {
					queryString.append(" and created_by_user_id like '%"
							+ imgFilter.getCreated_by_user_id() + "%'");
				}

				if (imgFilter.getFrom_created_date() != null) {
					queryString.append(" and  created_date >=  '"
							+ (imgFilter.getFrom_created_date()) + "'");
					// /
					// predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImage.<java.sql.Date>
					// get(policyAttributestoDataMapper.get(TrustPolicyFields.CREATED_DATE)),new
					// java.sql.Date(trustPolicyFilter.getFrom_created_date().getTime())
					// ));
				}
				if (imgFilter.getTo_created_date() != null) {
					queryString.append(" and  created_date <=  '"
							+ (imgFilter.getTo_created_date()) + "'");
					// /
					// predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImage.<java.sql.Date>
					// get(policyAttributestoDataMapper.get(TrustPolicyFields.CREATED_DATE)),new
					// java.sql.Date(trustPolicyFilter.getTo_created_date().getTime())
					// ));
				}

				if (imgFilter.getPolicyCriteria() != null) {
					if (imgFilter.getPolicyCriteria() == SearchImageByPolicyCriteria.WITH) {
						queryString.append(" and trust_policy_id is not NULL");
					} else if (imgFilter.getPolicyCriteria() == SearchImageByPolicyCriteria.WITHOUT) {
						queryString.append(" and trust_policy_id is NULL");
					}
				}

				if (imgFilter.getUploadCriteria() != null) {
					if (imgFilter.getUploadCriteria() == SearchImageByUploadCriteria.UPLOADED) {
						queryString.append(" and image_upload_count>0");
					} else if (imgFilter.getUploadCriteria() == SearchImageByUploadCriteria.NOT_UPLOADED) {
						queryString.append(" and image_upload_count=0");
					}
				}
			}

			Query query = emf.createEntityManager().createNativeQuery(
					queryString.toString());

			return ((Long) query.getSingleResult()).intValue();

		} catch (Exception e) {
			log.error("getMwImageCount failed",e);
			throw new DbException(
					"ImageDao,getMwImageCount(imageFilter) failed", e);
		}

		finally {
			em.close();
		}
	}


	public MwImage createImage(MwImage mwImage) throws DbException {
		return (MwImage) create(mwImage);
	}


	public MwImage getMwImage(String id) throws DbException {
		return (MwImage) get(MwImage.class, id);
	}


	public void updateImage(MwImage mwImage) throws DbException {
		update(mwImage);
	}

}
