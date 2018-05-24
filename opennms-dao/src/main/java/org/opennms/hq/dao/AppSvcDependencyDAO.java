package org.opennms.hq.dao;

import java.util.Collection;

import org.hyperic.hq.appdef.AppService;
import org.hyperic.hq.appdef.AppSvcDependency;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

public class AppSvcDependencyDAO  extends AbstractDaoHibernate<AppSvcDependency, Integer> implements OnmsDao<AppSvcDependency,Integer> {
    public AppSvcDependencyDAO() {
        super(AppSvcDependency.class);
    }

    public AppSvcDependency create(AppService appSvc, AppService depSvc) {
        AppSvcDependency a = new AppSvcDependency();
        a.setAppService(appSvc);
        a.setDependentService(depSvc);
        save(a);

        appSvc.getAppSvcDependencies().add(depSvc);
        return a;
    }

    public AppSvcDependency findByDependentAndDependor(Integer appsvcId, Integer depAppSvcId) {
        String sql = "from AppSvcDependency " + "where appService.id=? and dependentService.id=?";
        return (AppSvcDependency) getSession().createQuery(sql).setInteger(0, appsvcId.intValue())
            .setInteger(1, depAppSvcId.intValue()).uniqueResult();
    }

    public Collection findByDependents(AppService entity) {
        String sql = "from AppSvcDependency where dependentService = :appSvc";
        return getSession().createQuery(sql).setEntity("appSvc", entity).list();
    }

    public Collection findByApplication(Integer appId) {
        String sql = "select distinct a from AppSvcDependency a " + " join fetch a.appService asv "
                     + " where asv.application.id=?";
        return getSession().createQuery(sql).setInteger(0, appId.intValue()).list();
    }

    public Collection findByAppAndService(Integer appId, Integer serviceId) {
        String sql = "select distinct a from AppSvcDependency a " + " join fetch a.appService asv "
                     + " where asv.application.id=? and asv.service.id=?";
        return getSession().createQuery(sql).setInteger(0, appId.intValue()).setInteger(1,
            serviceId.intValue()).list();
    }

}
