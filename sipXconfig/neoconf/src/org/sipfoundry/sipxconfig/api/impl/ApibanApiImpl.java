package org.sipfoundry.sipxconfig.api.impl;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.sipfoundry.sipxconfig.api.ApibanApi;
import org.sipfoundry.sipxconfig.apiban.BannedApi;
import org.sipfoundry.sipxconfig.apiban.model.BannedBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApibanApiImpl implements ApibanApi {
    private static final Log LOG = LogFactory.getLog(ApibanApiImpl.class);
    
    private BannedApi m_bannedApi;
    
    @Override
    public Response getBanned() {
        BannedBean bean = m_bannedApi.getBanned();
        try {
            return Response.ok().entity(bean).build();
        } catch (Exception e) {
            LOG.error("Exception parsing ", e);
            return Response.status(Status.EXPECTATION_FAILED).build();
        }        
    }

    public BannedApi getBannedApi() {
        return m_bannedApi;
    }

    public void setBannedApi(BannedApi bannedApi) {
        m_bannedApi = bannedApi;
    }       
}
