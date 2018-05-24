package org.opennms.hq.dao;

import org.hyperic.hq.security.server.session.HQCertificate;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class HQCertificateDAO extends AbstractDaoHibernate<HQCertificate, Integer> implements OnmsDao<HQCertificate,Integer> {
    
    public HQCertificateDAO() {
        super(HQCertificate.class);
    }

}
