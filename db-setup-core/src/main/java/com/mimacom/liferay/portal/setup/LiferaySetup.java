package com.mimacom.liferay.portal.setup;

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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.mimacom.liferay.portal.setup.core.*;
import com.mimacom.liferay.portal.setup.core.util.MarshallUtil;
import com.mimacom.liferay.portal.setup.domain.*;

import java.io.File;
import java.util.List;

public final class LiferaySetup {

    public static final String DESCRIPTION = "Created by setup module.";

    private static final Log LOG = LogFactoryUtil.getLog(LiferaySetup.class);
    private static final String ADMIN_ROLE_NAME = "Administrator";

    private LiferaySetup() {
    }

    public static boolean setup(final File file) {
        Setup setup = MarshallUtil.unmarshall(file);
        return setup(setup);
    }

    public static boolean setup(final Setup setup) {

        Configuration configuration = setup.getConfiguration();
        String runAsUserEmail = configuration.getRunasuser();
        final String principalName = PrincipalThreadLocal.getName();
        final PermissionChecker permissionChecker = PermissionThreadLocal.getPermissionChecker();

        try {
            // iterate over companies or choose default
            if (!configuration.getCompany().isEmpty()) {
                for (Company company : configuration.getCompany()) {
                    Long companyId = company.getCompanyid();
                    String companyWebId = company.getCompanywebid();
                    if (companyId == null) {
                        try {
                            companyId = CompanyLocalServiceUtil.getCompanyByWebId(companyWebId).getCompanyId();
                        } catch (PortalException | SystemException e) {
                            LOG.error("Couldn't find company with webId: " + companyWebId);
                            continue;
                        }
                    }
                    long runAsUserId = configureThreadPermission(runAsUserEmail, companyId);

                    setupPortalInstance(setup, companyId, runAsUserId);

                    // iterate over group names or choose GUEST group for the company
                    if (!company.getGroupName().isEmpty()) {
                        for (String groupName : company.getGroupName()) {
                            long groupId = GroupLocalServiceUtil.getGroup(companyId, groupName).getGroupId();
                            setupPortalGroup(setup, companyId, groupId, runAsUserId);
                        }
                    } else {
                        long groupId = GroupLocalServiceUtil.getGroup(companyId, GroupConstants.GUEST).getGroupId();
                        setupPortalGroup(setup, companyId, groupId, runAsUserId);
                    }
                }
            } else {
                long companyId = PortalUtil.getDefaultCompanyId();
                long runAsUserId = configureThreadPermission(runAsUserEmail, companyId);
                setupPortalInstance(setup, companyId, runAsUserId);

                long groupId = GroupLocalServiceUtil.getGroup(companyId, GroupConstants.GUEST).getGroupId();
                setupPortalGroup(setup, companyId, groupId, runAsUserId);
            }
        } catch (PortalException | SystemException | LiferaySetupException e) {
            LOG.error("An error occured while executing the portal setup");
            return false;
        } finally {
            PrincipalThreadLocal.setName(principalName);
            PermissionThreadLocal.setPermissionChecker(permissionChecker);
        }
        return true;
    }

    private static long configureThreadPermission(String runAsUserEmail, long companyId) throws SystemException, PortalException, LiferaySetupException {
        long runAsUserId;
        if (runAsUserEmail == null || runAsUserEmail.isEmpty()) {
            runAsUserId = UserLocalServiceUtil.getDefaultUserId(companyId);
            setAdminPermissionCheckerForThread(companyId);
            LOG.info("Using default administrator.");
        } else {
            User user = UserLocalServiceUtil
                    .getUserByEmailAddress(PortalUtil.getDefaultCompanyId(), runAsUserEmail);
            runAsUserId = user.getUserId();
            PrincipalThreadLocal.setName(runAsUserId);
            PermissionChecker permissionChecker;
            try {
                permissionChecker = PermissionCheckerFactoryUtil.create(user);
            } catch (Exception e) {
                throw new LiferaySetupException("An error occured while trying to create permissionchecker for user: " + runAsUserEmail, e);
            }
            PermissionThreadLocal.setPermissionChecker(permissionChecker);

            LOG.info("Execute setup module as user " + runAsUserEmail);
        }
        return runAsUserId;
    }

    private static void setupPortalGroup(Setup setup, long companyId, long groupId, long runAsUserId) {
        if (setup.getUsers() != null) {
            LOG.info("Setting up " + setup.getUsers().getUser().size() + " users");
            SetupUsers.setupUsers(setup.getUsers().getUser(), runAsUserId, groupId);
        }

        if (setup.getPageTemplates() != null) {
            SetupPages.setupPageTemplates(setup.getPageTemplates(), groupId, companyId,
                    runAsUserId, runAsUserId);
        }

        LOG.info("Setup of portal groups finished");
    }

    private static void setupPortalInstance(final Setup setup, long companyId, long runAsUserId) {

        if (setup.getDeleteLiferayObjects() != null) {
            LOG.info("Deleting : " + setup.getDeleteLiferayObjects().getObjectsToBeDeleted().size()
                    + " objects");
            deleteObjects(setup.getDeleteLiferayObjects().getObjectsToBeDeleted(), companyId);
        }

        if (setup.getCustomFields() != null) {
            LOG.info("Setting up " + setup.getCustomFields().getField().size() + " custom fields");
            SetupCustomFields.setupExpandoFields(setup.getCustomFields().getField(), companyId);
        }

        if (setup.getRoles() != null) {
            LOG.info("Setting up " + setup.getRoles().getRole().size() + " roles");
            SetupRoles.setupRoles(setup.getRoles().getRole(), companyId);
        }

        if (setup.getPortletPermissions() != null) {
            LOG.info("Setting up " + setup.getPortletPermissions().getPortlet().size() + " portlet/model resource");
            SetupPermissions.setupPortletPermissions(setup.getPortletPermissions(), companyId);
        }

        if (setup.getOrganizations() != null) {
            LOG.info("Setting up " + setup.getOrganizations().getOrganization().size() + " "
                    + "organizations");
            SetupOrganizations.setupOrganizations(setup.getOrganizations().getOrganization(), null,
                    null, runAsUserId);
        }


        LOG.info("Setup of portal instances finished");
    }

    private static void deleteObjects(final List<ObjectsToBeDeleted> objectsToBeDeleted, long companyId) {

        for (ObjectsToBeDeleted otbd : objectsToBeDeleted) {

            if (otbd.getRoles() != null) {
                List<com.mimacom.liferay.portal.setup.domain.Role> roles = otbd.getRoles()
                        .getRole();
                SetupRoles.deleteRoles(roles, otbd.getDeleteMethod(), companyId);
            }

            if (otbd.getUsers() != null) {
                List<com.mimacom.liferay.portal.setup.domain.User> users = otbd.getUsers()
                        .getUser();
                SetupUsers.deleteUsers(users, otbd.getDeleteMethod());
            }

            if (otbd.getOrganizations() != null) {
                List<Organization> organizations = otbd
                        .getOrganizations().getOrganization();
                SetupOrganizations.deleteOrganization(organizations, otbd.getDeleteMethod());
            }

            if (otbd.getCustomFields() != null) {
                List<CustomFields.Field> customFields = otbd.getCustomFields().getField();
                SetupCustomFields.deleteCustomFields(customFields, otbd.getDeleteMethod(), companyId);
            }
        }
    }

    /**
     * Returns Liferay user, that has Administrator role assigned.
     *
     * @param companyId company ID
     * @return Liferay {@link com.mimacom.liferay.portal.setup.domain.User}
     * instance, throws exception if no user is found
     * @throws LiferaySetupException if cannot obtain permission checker or find any user with administration role
     */
    private static User getAdminUser(final long companyId) throws LiferaySetupException {

        try {
            Role adminRole = RoleLocalServiceUtil.getRole(companyId, ADMIN_ROLE_NAME);
            List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());

            if (adminUsers == null || adminUsers.isEmpty()) {
                throw new LiferaySetupException("Cannot find any admin with default administration role: " + ADMIN_ROLE_NAME + " in liferay instance with companyId: " + companyId);
            }
            return adminUsers.get(0);

        } catch (PortalException | SystemException e) {
            throw new LiferaySetupException("Cannot obtain Liferay role for role name: " + ADMIN_ROLE_NAME + " in liferay instance with companyId: " + companyId, e);
        }
    }

    /**
     * Initializes permission checker for Liferay Admin. Used to grant access to
     * custom fields.
     *
     * @param companyId company ID
     * @throws LiferaySetupException if cannot set permission checker
     */
    private static void setAdminPermissionCheckerForThread(final long companyId) throws LiferaySetupException {

        User adminUser = getAdminUser(companyId);
        PrincipalThreadLocal.setName(adminUser.getUserId());
        PermissionChecker permissionChecker;
        try {
            permissionChecker = PermissionCheckerFactoryUtil.create(adminUser);
        } catch (Exception e) {
            throw new LiferaySetupException("Cannot obtain permission checker for Liferay Administrator user",
                    e);
        }
        PermissionThreadLocal.setPermissionChecker(permissionChecker);
    }

}
