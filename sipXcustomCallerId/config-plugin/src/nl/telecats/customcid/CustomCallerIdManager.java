/*
 * Copyright (c) 2011 Telecats B.V. All rights reserved. Contributed to
 * SIPfoundry and eZuce, Inc. under a Contributor Agreement.
 * This library or application is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public
 * License (AGPL) as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library or application is distributed in the hope that it will be
 * useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License (AGPL) for more details.
 *
 * $
 */
package nl.telecats.customcid;

import java.util.Collection;

public interface CustomCallerIdManager {
    CustomCallerIdSettings getSettings();

    void saveSettings(CustomCallerIdSettings settings);

    Collection<CustomCallerAlias> getDnisRewrites();

    Collection<CustomCallerAlias> getDdiRewrites();

    void setDdiRewrites(Collection<CustomCallerAlias> rewrites);

    void setDnisRewrites(Collection<CustomCallerAlias> rewrites);
}
