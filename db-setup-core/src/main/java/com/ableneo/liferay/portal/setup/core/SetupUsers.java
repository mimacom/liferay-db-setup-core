package com.ableneo.liferay.portal.setup.core;

/*
 * #%L
 * Liferay Portal DB Setup core
 * %%
 * Original work Copyright (C) 2016 - 2018 mimacom ag
 * Modified work Copyright (C) 2018 - 2020 ableneo, s. r. o.
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.*;
import com.liferay.portal.kernel.util.PortalUtil;
import com.ableneo.liferay.portal.setup.core.util.CustomFieldSettingUtil;
import com.ableneo.liferay.portal.setup.domain.CustomFieldSetting;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;

public final class SetupUsers {

    private static final Log LOG = LogFactoryUtil.getLog(SetupUsers.class);
    private static final long COMPANY_ID = PortalUtil.getDefaultCompanyId();
    private static final int DEFAULT_BIRTHDAY_YEAR = 1970;

    private SetupUsers() {

    }

    public static void setupUsers(final List<com.ableneo.liferay.portal.setup.domain.User> users,
            final long runAsUser, final long groupId) {

        for (com.ableneo.liferay.portal.setup.domain.User user : users) {
            User liferayUser = null;
            try {
                liferayUser = UserLocalServiceUtil.getUserByEmailAddress(COMPANY_ID,
                        user.getEmailAddress());
                LOG.info("User " + liferayUser.getEmailAddress()
                        + " already exist, not creating...");

            } catch (NoSuchUserException e) {
                liferayUser = addUser(user);

            } catch (Exception e) {
                LOG.error("Error by retrieving user " + user.getEmailAddress());
            }

            if( null != liferayUser ){
                addUserToOrganizations(user, liferayUser);
                addRolesToUser(user, liferayUser);
                if (user.getCustomFieldSetting() != null && !user.getCustomFieldSetting().isEmpty()) {
                    setCustomFields(runAsUser, groupId, COMPANY_ID, liferayUser, user);
                }
            } else {
                LOG.warn("Could not create user with screenName '" + user.getScreenName()+"'");
            }
        }
    }

    private static void setCustomFields(final long runAsUser, final long groupId,
            final long company, final User liferayUser,
            final com.ableneo.liferay.portal.setup.domain.User user) {
        Class clazz = liferayUser.getClass();
        for (CustomFieldSetting cfs : user.getCustomFieldSetting()) {
            String resolverHint = "Custom value for user " + user.getScreenName() + ", "
                    + user.getEmailAddress() + "" + " Key " + cfs.getKey() + ", value "
                    + cfs.getValue();
            CustomFieldSettingUtil.setExpandoValue(resolverHint, runAsUser, groupId, company, clazz,
                    liferayUser.getUserId(), cfs.getKey(), cfs.getValue());
        }
    }

    private static User addUser(final com.ableneo.liferay.portal.setup.domain.User setupUser) {

        LOG.info("User " + setupUser.getEmailAddress() + " not exists, creating...");

        User liferayUser = null;
        long creatorUserId = 0;
        boolean autoPassword = false;
        String password1 = setupUser.getPassword();
        String password2 = setupUser.getPassword();
        boolean autoScreenName = false;
        boolean male = true;
        String emailAddress = setupUser.getEmailAddress();
        long facebookId = 0;
        String jobTitle = StringPool.BLANK;
        String openId = StringPool.BLANK;
        Locale locale = Locale.US;
        String middleName = StringPool.BLANK;
        int prefixId = 0;
        int suffixId = 0;
        int birthdayMonth = Calendar.JANUARY;
        int birthdayDay = 1;
        int birthdayYear = DEFAULT_BIRTHDAY_YEAR;
        long[] groupIds = new long[] {};
        long[] roleIds = new long[] {};
        long[] organizationIds = new long[] {};
        long[] userGroupIds = null;
        boolean sendEmail = false;
        ServiceContext serviceContext = new ServiceContext();

        try {
            liferayUser = UserLocalServiceUtil.addUser(creatorUserId, COMPANY_ID, autoPassword,
                    password1, password2, autoScreenName, setupUser.getScreenName(), emailAddress,
                    facebookId, openId, locale, setupUser.getFirstName(), middleName,
                    setupUser.getLastName(), prefixId, suffixId, male, birthdayMonth, birthdayDay,
                    birthdayYear, jobTitle, groupIds, organizationIds, roleIds, userGroupIds,
                    sendEmail, serviceContext);
            LOG.info("User " + setupUser.getEmailAddress() + " created");

        } catch (Exception ex) {
            LOG.error("Error by adding user " + setupUser.getEmailAddress(), ex);
        }

        return liferayUser;
    }

    private static void addUserToOrganizations(
        final com.ableneo.liferay.portal.setup.domain.User setupUser, final User liferayUser) {

        try {
            for (com.ableneo.liferay.portal.setup.domain.Organization organization : setupUser
                    .getOrganization()) {
                Organization liferayOrganization = OrganizationLocalServiceUtil
                        .getOrganization(COMPANY_ID, organization.getName());
                UserLocalServiceUtil.addOrganizationUsers(liferayOrganization.getOrganizationId(),
                        new long[] {liferayUser.getUserId()});
                LOG.info("Adding user" + setupUser.getEmailAddress() + " to Organization "
                        + liferayOrganization.getName());
            }
        } catch (PortalException | SystemException e) {
            LOG.error("cannot add users");
        }

    }

    private static void addRolesToUser(final com.ableneo.liferay.portal.setup.domain.User setupUser,
            final User liferayUser) {

        try {
            for (com.ableneo.liferay.portal.setup.domain.Role userRole : setupUser.getRole()) {

                Role role = RoleLocalServiceUtil.getRole(COMPANY_ID, userRole.getName());
                long[] roleIds = {role.getRoleId()};
                String roleType = userRole.getType();
                switch (roleType) {
                case "portal":
                    RoleLocalServiceUtil.addUserRoles(liferayUser.getUserId(), roleIds);
                    LOG.info("Adding regular role " + userRole.getName() + " to user "
                            + liferayUser.getEmailAddress());
                    break;

                case "site":
                case "organization":
                    Group group = GroupLocalServiceUtil.getGroup(COMPANY_ID, userRole.getSite());
                    UserGroupRoleLocalServiceUtil.addUserGroupRoles(liferayUser.getUserId(),
                            group.getGroupId(), roleIds);

                    LOG.info("Adding " + roleType + " role " + userRole.getName() + " to user "
                            + liferayUser.getEmailAddress());
                    break;

                default:
                    LOG.error("unknown role type " + roleType);
                    break;
                }
            }
        } catch (PortalException | SystemException e) {
            LOG.error("Error in adding roles to user " + setupUser.getEmailAddress(), e);
        }
    }

    /**
     * by this method, all users will be deleted from liferay, excluding those
     * listed in the setup.xml. from security reasons, no administrators, or
     * default users are deleted
     */
    public static void deleteUsers(final List<com.ableneo.liferay.portal.setup.domain.User> users,
            final String deleteMethod) {

        switch (deleteMethod) {
        case "excludeListed":

            Map<String, com.ableneo.liferay.portal.setup.domain.User> usersMap = convertUserListToHashMap(
                    users);
            try {
                List<User> allUsers = UserLocalServiceUtil.getUsers(-1, -1);
                for (User user : allUsers) {
                    if (!usersMap.containsKey(user.getEmailAddress())) {
                        if (user.isDefaultUser() || PortalUtil.isOmniadmin(user.getUserId())) {
                            LOG.info("Skipping deletion of system user " + user.getEmailAddress());
                        } else {
                            try {
                                UserLocalServiceUtil.deleteUser(user.getUserId());
                            } catch (PortalException | SystemException e) {
                                LOG.error("Unable to delete user.", e);
                            }
                            LOG.info("Deleting User " + user.getEmailAddress());
                        }
                    }
                }

            } catch (SystemException e) {
                LOG.error("Unable to get user", e);
            }
            break;

        case "onlyListed":
            for (com.ableneo.liferay.portal.setup.domain.User user : users) {
                try {
                    String email = user.getEmailAddress();
                    User u = UserLocalServiceUtil.getUserByEmailAddress(COMPANY_ID, email);
                    UserLocalServiceUtil.deleteUser(u);

                    LOG.info("Deleting User " + email);
                } catch (PortalException | SystemException e) {
                    LOG.error("Unable to delete user.", e);
                }
            }
            break;

        default:
            LOG.error("Unknown delete method : " + deleteMethod);
            break;
        }
    }

    private static Map<String, com.ableneo.liferay.portal.setup.domain.User> convertUserListToHashMap(
            final List<com.ableneo.liferay.portal.setup.domain.User> objects) {

        HashMap<String, com.ableneo.liferay.portal.setup.domain.User> map = new HashMap<>();
        for (com.ableneo.liferay.portal.setup.domain.User user : objects) {
            map.put(user.getEmailAddress(), user);
        }
        return map;
    }

}
