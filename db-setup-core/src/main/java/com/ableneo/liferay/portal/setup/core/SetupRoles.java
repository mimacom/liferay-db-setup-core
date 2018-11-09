package com.ableneo.liferay.portal.setup.core;

/*
 * #%L
 * Liferay Portal DB Setup core
 * %%
 * Copyright (C) 2016 - 2018 mimacom ag
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

import com.liferay.portal.kernel.dao.orm.ObjectNotFoundException;
import com.liferay.portal.kernel.exception.NoSuchRoleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.RequiredRoleException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.ableneo.liferay.portal.setup.core.util.ResolverUtil;
import com.mimacom.liferay.portal.setup.domain.DefinePermission;
import com.mimacom.liferay.portal.setup.domain.DefinePermissions;
import com.mimacom.liferay.portal.setup.domain.PermissionAction;

import java.util.*;

public final class SetupRoles {
    private static final Log LOG = LogFactoryUtil.getLog(SetupRoles.class);
    private static final long COMPANY_ID = PortalUtil.getDefaultCompanyId();

    private static final String SCOPE_INDIVIDUAL = "individual";
    private static final String SCOPE_SITE = "site";
    private static final String SCOPE_SITE_TEMPLATE = "site template";
    private static final String SCOPE_PORTAL = "portal";

    private SetupRoles() {

    }

    public static void setupRoles(final List<com.mimacom.liferay.portal.setup.domain.Role> roles, long runAsUserId, long groupId, long company) {

        for (com.mimacom.liferay.portal.setup.domain.Role role : roles) {
            try {
                RoleLocalServiceUtil.getRole(COMPANY_ID, role.getName());
                LOG.info("Setup: Role " + role.getName() + " already exist, not creating...");
            } catch (NoSuchRoleException | ObjectNotFoundException e) {
                addRole(role);

            } catch (SystemException | PortalException e) {
                LOG.error("error while setting up roles", e);
            }
            addRolePermissions(role, runAsUserId, groupId, company);
        }
    }

    private static void addRole(final com.mimacom.liferay.portal.setup.domain.Role role) {

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

            long defaultUserId = UserLocalServiceUtil.getDefaultUserId(COMPANY_ID);
            RoleLocalServiceUtil.addRole(defaultUserId, null, 0, role.getName(), localeTitleMap,
                    null, roleType, null, null);

            LOG.info("Setup: Role " + role.getName() + " does not exist, adding...");

        } catch (PortalException | SystemException e) {
            LOG.error("error while adding up roles", e);
        }

    }

    public static void deleteRoles(final List<com.mimacom.liferay.portal.setup.domain.Role> roles,
                                   final String deleteMethod) {

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
                                        .deleteRole(RoleLocalServiceUtil.getRole(COMPANY_ID, name));
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
                        RoleLocalServiceUtil.deleteRole(RoleLocalServiceUtil.getRole(COMPANY_ID, name));
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

    private static void addRolePermissions(com.mimacom.liferay.portal.setup.domain.Role role, long runAsUserId, long groupId, long companyId) {
        if (role.getDefinePermissions() != null) {
            String siteName = role.getSite();
            if (siteName != null && !siteName.equals("")) {
                LOG.warn("Note, refering a site inside a role definition makes no sense and will be ignored! This "
                        + "attribute is intended to be used for refering assigning a site role to an Liferay object, such as a user!"
                        + " When doing so, it is necessary to refer a site!");
            }
            DefinePermissions permissions = role.getDefinePermissions();
            if (permissions.getDefinePermission() != null && permissions.getDefinePermission().size() > 0) {
                for (DefinePermission permission : permissions.getDefinePermission() ) {
                    String permissionName = permission.getDefinePermissionName();
                    String resourcePrimKey = "0";

                    if (permission.getElementPrimaryKey() != null) {
                        resourcePrimKey = ResolverUtil.lookupAll(runAsUserId, groupId, companyId, permission.getElementPrimaryKey(), "Role " + role.getName() + " permission name " + permissionName);
                    }
                    String type = role.getType();
                    int scope = ResourceConstants.SCOPE_COMPANY;
                    if (type != null && type.toLowerCase().equals(SCOPE_PORTAL)) {
                        scope = ResourceConstants.SCOPE_COMPANY;
                    }
                    if (type != null && type.toLowerCase().equals(SCOPE_SITE)) {
                        scope = ResourceConstants.SCOPE_GROUP_TEMPLATE;
                    }

                    if (type != null && type.toLowerCase().equals("organization")) {
                        scope = ResourceConstants.SCOPE_GROUP_TEMPLATE;
                    }
                    if (scope == ResourceConstants.SCOPE_COMPANY && !resourcePrimKey.equals("0")) {
                        // if we have a regular role which is set for a particular site, set the scope to group
                        scope = ResourceConstants.SCOPE_GROUP;
                    }
                    if (scope == ResourceConstants.SCOPE_COMPANY && resourcePrimKey.equals("0")) {
                        // if we have a regular role which does not have any resource key set, set the company id as resource key
                        resourcePrimKey = Long.toString(companyId);
                    }

                    // if defined override the define permission scope
                    if (permission.getScope() != null && !permission.getScope().equals("")) {
                        if (permission.getScope().toLowerCase().equals(SCOPE_PORTAL)) {
                            scope = ResourceConstants.SCOPE_COMPANY;
                        } else if (permission.getScope().toLowerCase().equals(SCOPE_SITE_TEMPLATE)) {
                            scope = ResourceConstants.SCOPE_GROUP_TEMPLATE;
                        } else if (permission.getScope().toLowerCase().equals(SCOPE_SITE)) {
                            scope = ResourceConstants.SCOPE_GROUP;
                        } else if (permission.getScope().toLowerCase().equals(SCOPE_INDIVIDUAL)) {
                            scope = ResourceConstants.SCOPE_INDIVIDUAL;
                        }
                    }

                    if (permission.getPermissionAction() != null && permission.getPermissionAction().size() > 0) {
                        ArrayList<String> listOfActions = new ArrayList<String>();
                        for (PermissionAction pa : permission.getPermissionAction()) {
                            String actionname = pa.getActionName();
                            listOfActions.add(actionname);
                        }
                        String[] loa = new String[listOfActions.size()];
                        loa = listOfActions.toArray(loa);
                        try {
                            SetupPermissions.addPermission(role.getName(), permissionName, resourcePrimKey, scope, loa);
                        } catch (SystemException e) {
                            LOG.error("Error when defining permission " + permissionName + " for role " + role.getName(), e);
                        } catch (PortalException e) {
                            LOG.error("Error when defining permission " + permissionName + " for role " + role.getName(), e);
                        }
                    } else  {

                    }
                }
            }
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
