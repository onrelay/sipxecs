/**
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.rest;


import java.io.IOException;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.sipfoundry.sipxconfig.localization.LocalizationContext;

public class PersonalAttendantLangResource extends UserResource {
    private LocalizationContext m_context;

    @Get
    public Representation represent(Variant variant) throws ResourceException {        
        
        try {
            String[] langs = m_context.getInstalledLanguages();
            return toRepresentation(langs);

        } catch( IOException ex ) {
            throw new ResourceException( ex );
        }
    }

    public void setLocalizationContext(LocalizationContext context) {
        m_context = context;
    }

}
