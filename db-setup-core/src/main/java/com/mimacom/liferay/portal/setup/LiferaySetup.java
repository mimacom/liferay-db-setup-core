package com.mimacom.liferay.portal.setup;

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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.registry.RegistryUtil;
import com.liferay.registry.dependency.ServiceDependencyListener;
import com.liferay.registry.dependency.ServiceDependencyManager;
import com.mimacom.liferay.portal.setup.core.SetupCustomFields;
import com.mimacom.liferay.portal.setup.core.SetupOrganizations;
import com.mimacom.liferay.portal.setup.core.SetupPages;
import com.mimacom.liferay.portal.setup.core.SetupPermissions;
import com.mimacom.liferay.portal.setup.core.SetupRoles;
import com.mimacom.liferay.portal.setup.core.SetupSites;
import com.mimacom.liferay.portal.setup.core.SetupUserGroups;
import com.mimacom.liferay.portal.setup.core.SetupUsers;
import com.mimacom.liferay.portal.setup.domain.Configuration;
import com.mimacom.liferay.portal.setup.domain.CustomFields;
import com.mimacom.liferay.portal.setup.domain.ObjectsToBeDeleted;
import com.mimacom.liferay.portal.setup.domain.Organization;
import com.mimacom.liferay.portal.setup.domain.Setup;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class LiferaySetup {

    public static final String DESCRIPTION = "Created by setup module.";

    private static final Log LOG = LogFactoryUtil.getLog(LiferaySetup.class);
    private static final String ADMIN_ROLE_NAME = "Administrator";
    private static long runAsUserId;
    private static ServiceDependencyManager serviceDependencyManager;
    private static List<String> dependeciesFilters = new ArrayList<>();
    private static int delayMillisecond = 0;

    private LiferaySetup() {

    }

    public static void addDependency(String dependency) {

        dependeciesFilters.add(dependency);
    }

    public static void addDelay(int millis) {

        delayMillisecond = millis;
    }

    public static void setup(final File file) throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException {

        Setup setup = MarshallUtil.unmarshall(file);
        setup(setup);
    }

    public static void setup(final InputStream inputStream) throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException {

        Setup setup = MarshallUtil.unmarshall(inputStream);
        setup(setup);
    }

    public static void setup(final Setup setup) {

        new Thread(() -> {
            if (delayMillisecond > 0) {
                try {
                    LOG.info(String.format("Deferring configurator execution by %d milliseconds", delayMillisecond));
                    Thread.sleep(delayMillisecond);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
            if (!dependeciesFilters.isEmpty()) {
                serviceDependencyManager = new ServiceDependencyManager();
                serviceDependencyManager.addServiceDependencyListener(new ServiceDependencyListener() {

                    @Override
                    public void dependenciesFulfilled() {

                        preSetup(setup);
                    }

                    @Override
                    public void destroy() {
                        // ignore
                    }
                });
                dependeciesFilters.stream().map(dependency -> RegistryUtil.getRegistry().getFilter(dependency))
                                  .forEach(serviceDependencyManager::registerDependencies);
            } else {
                preSetup(setup);
            }
        }).start();
    }

    private static void preSetup(final Setup setup) {

        try {
            Configuration configuration = setup.getConfiguration();

            String runAsUser = configuration.getRunasuser();
            if (runAsUser == null || runAsUser.isEmpty()) {
                setAdminPermissionCheckerForThread(PortalUtil.getDefaultCompanyId());
                LOG.info("Using default administrator.");
            } else {
                User user = UserLocalServiceUtil.getUserByEmailAddress(PortalUtil.getDefaultCompanyId(), runAsUser);
                runAsUserId = user.getUserId();
                PrincipalThreadLocal.setName(runAsUserId);
                PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(user);
                PermissionThreadLocal.setPermissionChecker(permissionChecker);

                LOG.info("Execute setup module as user " + setup.getConfiguration().getRunasuser());
            }

            setupPortal(setup);
        } catch (Exception e) {
            LOG.error("An error occured while executing the portal setup ", e);
        } finally {
            PrincipalThreadLocal.setName(null);
            PermissionThreadLocal.setPermissionChecker(null);
        }
    }

    private static void setupPortal(final Setup setup) {

        long defaultUserId = 0;
        long companyId = PortalUtil.getDefaultCompanyId();
        try {
            defaultUserId = UserLocalServiceUtil.getDefaultUserId(companyId);
        } catch (PortalException e1) {
            LOG.error("default user not found", e1);
        }
        long groupId = 0;
        Group g;
        try {
            g = GroupLocalServiceUtil.getGroup(companyId, "Guest");
            groupId = g.getGroupId();
        } catch (PortalException e) {
            LOG.error("Default site not found", e);
        }

        if (setup.getDeleteLiferayObjects() != null) {
            LOG.info("Deleting : " + setup.getDeleteLiferayObjects().getObjectsToBeDeleted().size() + " objects");
            deleteObjects(setup.getDeleteLiferayObjects().getObjectsToBeDeleted());
        }

        if (setup.getCustomFields() != null) {
            LOG.info("Setting up " + setup.getCustomFields().getField().size() + " custom fields");
            SetupCustomFields.setupExpandoFields(setup.getCustomFields().getField());
        }

        if (setup.getRoles() != null) {
            LOG.info("Setting up " + setup.getRoles().getRole().size() + " roles");
            SetupRoles.setupRoles(setup.getRoles().getRole(), runAsUserId, groupId, companyId);
        }
        if (setup.getUsers() != null) {
            LOG.info("Setting up " + setup.getUsers().getUser().size() + " users");
            SetupUsers.setupUsers(setup.getUsers().getUser(), defaultUserId, groupId);
        }

        if (setup.getOrganizations() != null) {
            LOG.info("Setting up " + setup.getOrganizations().getOrganization().size() + " organizations");
            SetupOrganizations.setupOrganizations(setup.getOrganizations().getOrganization(), null, null);
        }

        if (setup.getUserGroups() != null) {
            LOG.info("Setting up " + setup.getUserGroups().getUserGroup().size() + " User Groups");
            SetupUserGroups.setupUserGroups(setup.getUserGroups().getUserGroup());
        }

        if (setup.getPortletPermissions() != null) {
            LOG.info("Setting up " + setup.getPortletPermissions().getPortlet().size() + " roles");
            SetupPermissions.setupPortletPermissions(setup.getPortletPermissions());
        }

        if (setup.getSites() != null) {
            LOG.info("Setting up " + setup.getSites().getSite().size() + " sites");
            SetupSites.setupSites(setup.getSites().getSite(), null);
        }

        if (setup.getPageTemplates() != null) {
            SetupPages.setupPageTemplates(setup.getPageTemplates(), groupId, companyId, defaultUserId);
        }

        LOG.info("Setup finished");
    }

    private static void deleteObjects(final List<ObjectsToBeDeleted> objectsToBeDeleted) {

        for (ObjectsToBeDeleted otbd : objectsToBeDeleted) {

            if (otbd.getRoles() != null) {
                List<com.mimacom.liferay.portal.setup.domain.Role> roles = otbd.getRoles().getRole();
                SetupRoles.deleteRoles(roles, otbd.getDeleteMethod());
            }

            if (otbd.getUsers() != null) {
                List<com.mimacom.liferay.portal.setup.domain.User> users = otbd.getUsers().getUser();
                SetupUsers.deleteUsers(users, otbd.getDeleteMethod());
            }

            if (otbd.getOrganizations() != null) {
                List<Organization> organizations = otbd.getOrganizations().getOrganization();
                SetupOrganizations.deleteOrganization(organizations, otbd.getDeleteMethod());
            }

            if (otbd.getCustomFields() != null) {
                List<CustomFields.Field> customFields = otbd.getCustomFields().getField();
                SetupCustomFields.deleteCustomFields(customFields, otbd.getDeleteMethod());
            }
        }
    }

    /**
     * Returns Liferay user, that has Administrator role assigned.
     *
     * @param companyId company ID
     *
     * @return Liferay {@link com.mimacom.liferay.portal.setup.domain.User} instance, if no user is found, returns null
     *
     * @throws Exception if cannot obtain permission checker
     */
    private static User getAdminUser(final long companyId) throws Exception {

        try {
            Role adminRole = RoleLocalServiceUtil.getRole(companyId, ADMIN_ROLE_NAME);
            List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());

            if (adminUsers == null || adminUsers.isEmpty()) {
                return null;
            }
            return adminUsers.get(0);
        } catch (PortalException | SystemException e) {
            throw new Exception("Cannot obtain Liferay role for role name: " + ADMIN_ROLE_NAME, e);
        }
    }

    /**
     * Initializes permission checker for Liferay Admin. Used to grant access to custom fields.
     *
     * @param companyId company ID
     *
     * @throws Exception if cannot set permission checker
     */
    private static void setAdminPermissionCheckerForThread(final long companyId) throws Exception {

        User adminUser = getAdminUser(companyId);
        PrincipalThreadLocal.setName(adminUser.getUserId());
        PermissionChecker permissionChecker;
        try {
            permissionChecker = PermissionCheckerFactoryUtil.create(adminUser);
        } catch (Exception e) {
            throw new Exception("Cannot obtain permission checker for Liferay Administrator user", e);
        }
        PermissionThreadLocal.setPermissionChecker(permissionChecker);
    }

    public static long getRunAsUserId() {

        return runAsUserId;
    }
}
