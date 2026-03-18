/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.sbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.dao.support.DataAccessUtils;

import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.logging.AuditLogContext;
import org.sipfoundry.sipxconfig.logging.AuditLogContext.CONFIG_CHANGE_TYPE;


public class SbcDeviceManagerImpl extends SipxHibernateDaoSupport<SbcDevice> implements SbcDeviceManager,
        BeanFactoryAware {
    private static final String SBC_ID = "sbcId";
    private static final String SBC_NAME = "sbcName";
    private static final String AUDIT_LOG_CONFIG_TYPE = "SBC Device";
    private BeanFactory m_beanFactory;
    private SbcDescriptor m_sipXbridgeSbcModel;
    private AuditLogContext m_auditLogContext;

    
    public void setAuditLogContext(AuditLogContext auditLogContext) {
        m_auditLogContext = auditLogContext;
    }

    
    public void setSipXbridgeSbcModel(SbcDescriptor sipXbridgeSbcModel) {
        m_sipXbridgeSbcModel = sipXbridgeSbcModel;
    }

    public void clear() {
        Collection<SbcDevice> sbcs = getSbcDevices();
        getDaoEventPublisher().publishDeleteCollection(sbcs);
        for (SbcDevice sbcDevice : sbcs) {
            deleteSbcDevice(sbcDevice.getId());
        }
    }

    public void deleteSbcDevice(Integer id) {
        SbcDevice sbcDevice = getSbcDevice(id);
        getDaoEventPublisher().publishDelete(sbcDevice);
        super.removeEntity(sbcDevice);
        m_auditLogContext.logConfigChange(CONFIG_CHANGE_TYPE.DELETED, AUDIT_LOG_CONFIG_TYPE, sbcDevice.getName());
    }

    public void deleteSbcDevice(SbcDevice sbcDevice) {
        super.removeEntity(sbcDevice);
        m_auditLogContext.logConfigChange(CONFIG_CHANGE_TYPE.DELETED, AUDIT_LOG_CONFIG_TYPE, sbcDevice.getName());
    }

    public void deleteSbcDevices(Collection<Integer> ids) {
        for (Integer id : ids) {
            SbcDevice sbcDevice = getSbcDevice(id);
            getDaoEventPublisher().publishDelete(sbcDevice);
            deleteSbcDevice(id);
        }
    }

    public Collection<Integer> getAllSbcDeviceIds() {
        return (Collection<Integer>)super.findByNamedQuery("sbcIds", Integer.class);
    }

    public SbcDevice getSbcDevice(Integer id) {
        return load(SbcDevice.class, id);
    }

    public BridgeSbc getBridgeSbc(Location location) {
        List<BridgeSbc> sbcDevices = getBridgeSbcs();
        for (Iterator<BridgeSbc> iterator = sbcDevices.iterator(); iterator.hasNext();) {
            BridgeSbc sbcDevice = iterator.next();
            if (null != location && (location.equals(sbcDevice.getLocation()))) {
                return sbcDevice;
            }
        }
        return null;
    }

    public List<BridgeSbc> getBridgeSbcs() {
        List<SbcDevice> devices = getSbcDevices();
        List<BridgeSbc> bridges = new ArrayList<>();
        for (SbcDevice device : devices) {
            if (device instanceof BridgeSbc) {
                bridges.add((BridgeSbc) device);
            }
        }
        return bridges;
    }


    private List<SbcDevice> getSbcDevicesByDescriptor(SbcDescriptor descriptor) {
        List<SbcDevice> sbcs = getSbcDevices();
        List<SbcDevice> list = new ArrayList<SbcDevice>();
        for (SbcDevice sbc : sbcs) {
            if (sbc.getModelId().equals(descriptor.getModelId())) {
                list.add(sbc);
            }
        }
        return list;
    }

    public List<SbcDevice> getSbcDevices() {
        return super.loadAllEntities(SbcDevice.class);
    }

    public void checkForNewSbcDeviceCreation(SbcDescriptor descriptor) {
        int maxAllowed = descriptor.getMaxAllowed();
        if (descriptor.getMaxAllowed() > -1) {
            int size = getSbcDevicesByDescriptor(descriptor).size();
            if (size >= maxAllowed) {
                throw new UserException("sbc.creation.error", new String[] {
                    size + "", descriptor.getLabel()
                });
            }
        }
    }

    public boolean maxAllowedLimitReached(SbcDescriptor model) {
        String type = model.getBeanId();
        int limit = model.getMaxAllowed();
        List<Object> count = (List<Object>)super.findByNamedQueryAndNamedParam(
            "countSbcsByType", "sbcBeanId", type, Object.class);
        int sbcNumber = DataAccessUtils.intResult(count);
        return limit != -1 && sbcNumber >= limit;
    }

    public BridgeSbc newBridgeSbc(Location location) {
        BridgeSbc bridgeSbc = (BridgeSbc) newSbcDevice(m_sipXbridgeSbcModel);
        bridgeSbc.setName("sipXbridge-" + location.getId().toString());
        bridgeSbc.setDescription("Internal SBC on " + location.getFqdn());
        bridgeSbc.setLocation(location);
        bridgeSbc.setAddress(location.getAddress());
        // Set location id in order to ensure location id saving in DB.
        // Please note that sbc_device table is not related with location table
        bridgeSbc.setSettingTypedValue("bridge-configuration/location-id", location.getId());
        saveSbcDevice(bridgeSbc);
        return bridgeSbc;
    }


    public SbcDevice newSbcDevice(SbcDescriptor descriptor) {
        String beanId = descriptor.getBeanId();
        SbcDevice newSbc = (SbcDevice) m_beanFactory.getBean(beanId, SbcDevice.class);
        newSbc.setModel(descriptor);
        newSbc.setPort(descriptor.getDefaultPort());
        return newSbc;
    }

    public void saveSbcDevice(SbcDevice sbc) {
        boolean isNew = sbc.isNew();
        if (isNew) {
            checkForNewSbcDeviceCreation(sbc.getModel());
            checkForDuplicateNames(sbc);

        } else {
            // if the sbc name was changed
            if (isNameChanged(sbc)) {
                checkForDuplicateNames(sbc);
            }
        }
        saveEntity(sbc);

        // Replicate occurs only when updating sbc device
        if (isNew) {
            m_auditLogContext.logConfigChange(CONFIG_CHANGE_TYPE.ADDED, AUDIT_LOG_CONFIG_TYPE, sbc.getName());
        } else {
            m_auditLogContext.logConfigChange(CONFIG_CHANGE_TYPE.MODIFIED, AUDIT_LOG_CONFIG_TYPE, sbc.getName());
            sbc.generateProfiles(sbc.getProfileLocation());
            sbc.restart();
        }
    }

    public boolean isInternalSbcEnabled() {
        return (getBridgeSbcs().size() > 0);
    }

    private void checkForDuplicateNames(SbcDevice sbc) {
        if (isNameInUse(sbc)) {
            throw new UserException("error.duplicateSbcName");
        }
    }

    private boolean isNameInUse(SbcDevice sbc) {
        List<Object> count = (List<Object>)super.findByNamedQueryAndNamedParam("anotherSbcWithSameName", new String[] {
            SBC_NAME
        }, new Object[] {
            sbc.getName()
        },
        Object.class);

        return DataAccessUtils.intResult(count) > 0;
    }

    private boolean isNameChanged(SbcDevice sbc) {
        List<Object> count = (List<Object>)super.findByNamedQueryAndNamedParam("countSbcWithSameName", new String[] {
            SBC_ID, SBC_NAME
        }, new Object[] {
            sbc.getId(), sbc.getName()
        },
        Object.class);

        return DataAccessUtils.intResult(count) == 0;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }

    public List<Sbc> getSbcsForSbcDeviceId(Integer sbcDeviceId) {
        return (List<Sbc>)super.findByNamedQueryAndNamedParam(
            "sbcsForSbcDeviceId", 
            SBC_ID, 
            sbcDeviceId,
            Sbc.class);
    }
}
