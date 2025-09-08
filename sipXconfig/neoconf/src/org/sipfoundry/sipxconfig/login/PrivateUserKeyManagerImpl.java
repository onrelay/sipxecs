/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.login;

import java.util.List;

import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.User;
import org.springframework.dao.support.DataAccessUtils;

public class PrivateUserKeyManagerImpl extends SipxHibernateDaoSupport<PrivateUserKey> implements
        PrivateUserKeyManager {
    @Override
    public User getUserFromPrivateKey(String privateKey) {
        List<User> users = super.findByNamedQueryAndNamedParam(
            "userForPrivateKey", 
            "key", 
            privateKey,
            User.class);
        return (User) DataAccessUtils.singleResult(users);
    }

    @Override
    public String getPrivateKeyForUser(User user) {
        String key = getKeyForUser(user);
        if (key != null) {
            return key;
        }
        return createUserPrivateKey(user);
    }

    private String createUserPrivateKey(User user) {
        PrivateUserKey privateUserKey = new PrivateUserKey(user);
        super.mergeEntity(privateUserKey);
        return privateUserKey.getKey();
    }

    private String getKeyForUser(User user) {
        List<String> keys = super.findByNamedQueryAndNamedParam(
            "privateKeyForUser", "user", user, String.class);
        return (String) DataAccessUtils.singleResult(keys);
    }
}
