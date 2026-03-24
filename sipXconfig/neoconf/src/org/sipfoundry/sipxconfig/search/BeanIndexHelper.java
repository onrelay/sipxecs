/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.search;

import java.util.List;

import org.sipfoundry.sipxconfig.phone.Phone;


public class BeanIndexHelper  {
    private static final String BEAN_ID = "beanId";
    private static final String MODEL_ID = "modelId";

    public void setupIndexProperties(BeanIndexProperties beanIndexProperties) {
        Object entity = beanIndexProperties.getEntity();
        Object id = beanIndexProperties.getId();
        List<String> propertyNames = beanIndexProperties.getPropertyNamesList();
        List<Object> state = beanIndexProperties.getStateList();

        modifyPhoneBeans(entity, id, propertyNames, state);
    }

    // Replace the beanId in Phone class with the phone's model ID
    private void modifyPhoneBeans(Object entity, Object id, List<String> propertyNames, List<Object> state ) {
        if (null != entity && entity instanceof Phone) {
            Phone phone = (Phone) entity;
            if( propertyNames.contains(MODEL_ID)) {
                int modelIdIndex = propertyNames.indexOf(MODEL_ID);
                String modelId = (String) state.get(modelIdIndex);
                phone.setModelId( modelId ); // ensures it is set early / first
            }

            if (propertyNames.contains(BEAN_ID)) {
                int propertyIndex = propertyNames.indexOf(BEAN_ID);
                state.set(propertyIndex, phone.getModelLabel());
            }
        }
    }
}
