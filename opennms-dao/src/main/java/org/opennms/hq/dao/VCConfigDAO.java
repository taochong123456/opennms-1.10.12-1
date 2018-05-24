package org.opennms.hq.dao;

import org.hyperic.hq.vm.VCConfig;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class VCConfigDAO extends AbstractDaoHibernate<VCConfig, Integer> implements OnmsDao<VCConfig,Integer>{

    public VCConfigDAO() {
        super(VCConfig.class);
    }
    
    public VCConfig getVCConnectionSetByUI() {
            String hql = "from VCConfig where setByUI=:setByUI";
            return (VCConfig) getSession().createQuery(hql)
                                             .setString("setByUI", "true")
                                            .uniqueResult();
    }
   

}
