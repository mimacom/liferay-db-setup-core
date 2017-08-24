package com.mimacom.liferay.portal.setup.core;

/*
 * #%L
 * Liferay Portal DB Setup core
 * %%
 * Copyright (C) 2016 - 2017 mimacom ag
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.RequiredRoleException;
import com.liferay.portal.kernel.dao.orm.ObjectNotFoundException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SetupRoles {
    private static final Log LOG = LogFactoryUtil.getLog(SetupRoles.class);

    private SetupRoles() {

    }

    public static void setupRoles(final List<com.mimacom.liferay.portal.setup.domain.Role> roles, long companyId) {

        for (com.mimacom.liferay.portal.setup.domain.Role role : roles) {
            try {
                RoleLocalServiceUtil.getRole(companyId, role.getName());
                LOG.info("Setup: Role " + role.getName() + " already exist, not creating...");
            } catch (NoSuchRoleException | ObjectNotFoundException e) {
                addRole(role, companyId);

            } catch (SystemException | PortalException e) {
                LOG.error("error while setting up roles", e);
            }
        }
    }

    private static void addRole(final com.mimacom.liferay.portal.setup.domain.Role role, long companyId) {

        Map<Locale, String> localeTitleMap = new HashMap<>();
        localeTitleMap.put(Locale.ENGLISH, role.getName());

        try {
            int roleType = RoleConstants.TYPE_REGULAR;
            if (role.getType() != null) {
                if (role.getType().equals("site")) {
                    roleType = RoleConstants.TYPE_SITE;
                } else if (role.getType().equals("organization")) {
                    roleType = RoleConstants.TYPE_ORGANIZATION;
                }
            }

            long defaultUserId = UserLocalServiceUtil.getDefaultUserId(companyId);
            RoleLocalServiceUtil.addRole(defaultUserId, companyId, role.getName(), localeTitleMap,
                    null, roleType);

            LOG.info("Setup: Role " + role.getName() + " does not exist, adding...");

        } catch (PortalException | SystemException e) {
            LOG.error("error while adding up roles", e);
        }

    }

    public static void deleteRoles(final List<com.mimacom.liferay.portal.setup.domain.Role> roles,
                                   final String deleteMethod, final long companyId) {

        switch (deleteMethod) {
            case "excludeListed":
                Map<String, com.mimacom.liferay.portal.setup.domain.Role> toBeDeletedRoles = convertRoleListToHashMap(
                        roles);
                try {
                    for (Role role : RoleLocalServiceUtil.getRoles(-1, -1)) {
                        String name = role.getName();
                        if (!toBeDeletedRoles.containsKey(name)) {
                            try {
                                RoleLocalServiceUtil
                                        .deleteRole(RoleLocalServiceUtil.getRole(companyId, name));
                                LOG.info("Deleting Role " + name);

                            } catch (Exception e) {
                                LOG.info("Skipping deletion fo system role " + name);
                            }
                        }
                    }
                } catch (SystemException e) {
                    LOG.error("problem with deleting roles", e);
                }
                break;

            case "onlyListed":
                for (com.mimacom.liferay.portal.setup.domain.Role role : roles) {
                    String name = role.getName();
                    try {
                        RoleLocalServiceUtil.deleteRole(RoleLocalServiceUtil.getRole(companyId, name));
                        LOG.info("Deleting Role " + name);

                    } catch (RequiredRoleException e) {
                        LOG.info("Skipping deletion fo system role " + name);

                    } catch (PortalException | SystemException e) {
                        LOG.error("Unable to delete role.", e);
                    }
                }
                break;

            default:
                LOG.error("Unknown delete method : " + deleteMethod);
                break;
        }

    }

    private static Map<String, com.mimacom.liferay.portal.setup.domain.Role> convertRoleListToHashMap(
            final List<com.mimacom.liferay.portal.setup.domain.Role> objects) {

        HashMap<String, com.mimacom.liferay.portal.setup.domain.Role> map = new HashMap<>();
        for (com.mimacom.liferay.portal.setup.domain.Role role : objects) {
            map.put(role.getName(), role);
        }
        return map;
    }
}
