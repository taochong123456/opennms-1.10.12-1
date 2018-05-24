package org.opennms.hq.dao;

import org.hyperic.hq.security.server.session.KeystoreEntryImpl;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class DbKeystoreDAO extends AbstractDaoHibernate<KeystoreEntryImpl, Integer> implements OnmsDao<KeystoreEntryImpl,Integer> {

    protected DbKeystoreDAO() {
        super(KeystoreEntryImpl.class);
    }
    
    protected boolean cacheFindAll() {
        return true;
    }

}
