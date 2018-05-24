package org.opennms.hq.dao;

import org.hyperic.hq.events.server.session.AlertDefinition;

public interface AlertDefinitionDAOInterface {
    
    AlertDefinition findById(Integer id);
}
