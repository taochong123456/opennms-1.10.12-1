package org.opennms.netmgt.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ProtocolProperty;

public class ProtocolPropertyDao extends AbstractDaoHibernate<ProtocolProperty, Integer> implements OnmsDao<ProtocolProperty,Integer>{
    private static final Log log = LogFactory.getLog(ProtocolPropertyDao.class);
	private ServiceTypeDao m_serviceTypeDao;
    
    /**
     * key is nodeId_serviceId
     * @FIXME the design for this is not good
     * **/
    private final Map<String, Map<String,Object>> keyIdToProps  = new HashMap<String, Map<String,Object>>();
    private final Map<String, Integer> serviceNameToId  = new HashMap<String, Integer>();
	public ProtocolPropertyDao(){
    	super(ProtocolProperty.class);
    }
    
    @PostConstruct
    public void preloadQueryCache() {
    	 Session session = getSession();
    	 List<ProtocolProperty> protocolPropList = session.createQuery("from ProtocolProperty").list();
    	 List<OnmsServiceType> serviceTypes = m_serviceTypeDao.findAll();
    	 for(OnmsServiceType type : serviceTypes){
    		 serviceNameToId.put(type.getName(), type.getId());
    	 }
    	 for(ProtocolProperty prop : protocolPropList){
    		 String key = prop.getNodeId()+"_"+prop.getServiceid();
    		 Map<String,Object> propMap = keyIdToProps.get(key);
    		 if(propMap == null){
    			 propMap = new HashMap<String,Object>();
    		 }
			 propMap.put(prop.getKey(), prop.getValue());
    	 }
    }
    
    @PreDestroy
    public void cleanup() {
        synchronized (keyIdToProps) {
        	keyIdToProps.clear();
        }
    }
    
    public Map<String,Object> findPropsById(int nodeId, int serviceId){
    	String key = nodeId+"_"+serviceId;
    	Map<String, Object> props = null;
    	 synchronized (keyIdToProps) {
    		 props = keyIdToProps.get(key);
         }
    	 
    	 if(props == null){
    		 return new HashMap<String, Object>();
    	 }
    	return props;
    }
    
    public Map<String,Object> findPropsById(int nodeId, String serviceName){
    	return findPropsById(nodeId,serviceNameToId.get(serviceName));
    }
    
    public ServiceTypeDao getServiceTypeDao() {
  		return m_serviceTypeDao;
  	}

  	public void setServiceTypeDao(ServiceTypeDao m_serviceTypeDao) {
  		this.m_serviceTypeDao = m_serviceTypeDao;
  	}
    
}
