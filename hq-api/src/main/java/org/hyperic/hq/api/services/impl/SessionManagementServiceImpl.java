package org.hyperic.hq.api.services.impl;
import org.hyperic.hq.api.services.SessionManagementService;
import org.hyperic.hq.auth.shared.SessionNotFoundException;
import org.hyperic.hq.auth.shared.SessionTimeoutException;

public class SessionManagementServiceImpl extends RestApiService implements SessionManagementService {

    public void logout() throws SessionNotFoundException, SessionTimeoutException {}

}
