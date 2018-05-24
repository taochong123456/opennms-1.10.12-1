package org.opennms.hq.dao;

import java.rmi.dgc.VMID;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.vm.VmMapping;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class VCDAO extends AbstractDaoHibernate<VmMapping, Integer> implements OnmsDao<VmMapping,Integer> {
    protected final Log log = LogFactory.getLog(VCDAO.class.getName());

    protected VCDAO() {
        super(VmMapping.class);
    }
    
    public void remove(List<VmMapping> macToUUIDs) {
        for(VmMapping macToUUID:macToUUIDs) {
            this.remove(macToUUID);
        }
        getSession().flush();
    }

    public void save(List<VmMapping> macToUUIDs) {
        for(VmMapping macToUUID:macToUUIDs) {
            super.save(macToUUID);
        }
        getSession().flush();
    }
    
    @SuppressWarnings("unchecked")
    public List<VmMapping> findByVcUUID(String vcUUID) {
        String sql = "from VmMapping u where u.vcUUID = :vcUUID";
        return (List<VmMapping>) getSession().createQuery(sql).setString("vcUUID", vcUUID).list();         
    }
    
    @SuppressWarnings("unchecked")
    public List<VmMapping> getVMsFromOtherVcenters(String vcUUID) {
        String sql = "from VmMapping u where u.vcUUID <> :vcUUID";
        return (List<VmMapping>) getSession().createQuery(sql).setString("vcUUID", vcUUID).list();         
    }

    @SuppressWarnings("unchecked")
    public VMID findByMac(String mac)  {
        String sql = "from VmMapping u where u.macs like '%+mac+%'";
        sql = sql.replace("+mac+", mac);

        List<VmMapping> rs = getSession().createQuery(sql).list();
        if (rs.size()==0) {
            log.error("no IDs are recorded for " + mac);
            return null;
        }
      /*
        
        VmMapping macToUUID = rs.iterator().next();
        return new VMID(macToUUID.getMoId(),macToUUID.getVcUUID());*/
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public VmMapping findVMByMac(String mac){
        String sql = "from VmMapping u where u.macs like '%+mac+%'";
        sql = sql.replace("+mac+", mac);

        List<VmMapping> rs = getSession().createQuery(sql).list();
        if (rs.size()==0) {
            log.error("no IDs are recorded for " + mac);
            return null;
        }
      
        VmMapping macToUUID = rs.iterator().next();
        return macToUUID;
    }
}
