/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.agentcontroller;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.util.security.Credential;

/**
 * A simple login service that allows to add users programmatically.
 */
public class SimpleLoginService extends AbstractLoginService
{
    /**
     * The known user principals keyed by user name.
     */
    private final Map<String, UserPrincipal> userPrincipals = new HashMap<>();

    /**
     * The known user roles keyed by user name.
     */
    private final Map<String, String[]> userPrincipalRoles = new HashMap<>();

    /**
     * Adds a user to this login service.
     * 
     * @param userName
     *            the user name
     * @param credential
     *            the user credential
     * @param roles
     *            the user roles
     */
    public void putUser(final String userName, final Credential credential, final String[] roles)
    {
        final UserPrincipal userPrincipal = new UserPrincipal(userName, credential);

        userPrincipals.put(userName, userPrincipal);
        userPrincipalRoles.put(userName, roles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] loadRoleInfo(UserPrincipal userPrincipal)
    {
        return userPrincipalRoles.get(userPrincipal.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserPrincipal loadUserInfo(String userName)
    {
        return userPrincipals.get(userName);
    }
}
