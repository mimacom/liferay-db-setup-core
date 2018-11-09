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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.ableneo.liferay.portal.setup.LiferaySetup;
import com.ableneo.liferay.portal.setup.core.util.CustomFieldSettingUtil;
import com.ableneo.liferay.portal.setup.domain.CustomFieldSetting;
import com.ableneo.liferay.portal.setup.domain.Role;
import com.ableneo.liferay.portal.setup.domain.UserAsMember;
import com.ableneo.liferay.portal.setup.domain.UserGroup;
import java.util.List;
import java.util.Objects;

public class SetupUserGroups {

    private static final Log LOG = LogFactoryUtil.getLog(SetupUserGroups.class);
    private static final long COMPANY_ID = PortalUtil.getDefaultCompanyId();

    private SetupUserGroups() {

    }

    public static void setupUserGroups(final List<UserGroup> userGroups) {
        final long userId = LiferaySetup.getRunAsUserId();

        for (UserGroup userGroup : userGroups) {
            com.liferay.portal.kernel.model.UserGroup liferayUserGroup = null;
            long liferayUserGroupId = -1;
            try {
                liferayUserGroup = UserGroupLocalServiceUtil.getUserGroup(COMPANY_ID, userGroup.getName());
                liferayUserGroupId = liferayUserGroup.getUserGroupId();
            } catch (PortalException e) {
                LOG.info("UserGroup does not exists, creating new one for name: " + userGroup.getName());
            }
            if (liferayUserGroupId == -1) {
                try {
                    liferayUserGroup = UserGroupLocalServiceUtil.addUserGroup(userId, COMPANY_ID, userGroup.getName(), userGroup.getDescription(),new ServiceContext());
                } catch (PortalException e) {
                    LOG.error("Can not create UserGroup with name: " + userGroup.getName(), e);
                    continue;
                }
            }

            if (userGroup.getCustomFieldSetting() != null && !userGroup.getCustomFieldSetting().isEmpty()) {
                setCustomFields(userId, liferayUserGroup, COMPANY_ID, userGroup.getCustomFieldSetting(), userGroup);
            }

            if (!userGroup.getRole().isEmpty()) {
                LOG.info("Setting Roles for UserGroup.");
                addRolesToUserGroup(userGroup, liferayUserGroup);
            }

            if (!userGroup.getUserAsMember().isEmpty()) {
                LOG.info("Setting User Members.");
                addUsersToUserGroup(userGroup.getUserAsMember(), liferayUserGroup);
            }

        }
    }

    private static void addUsersToUserGroup(List<UserAsMember> usersAsMember, com.liferay.portal.kernel.model.UserGroup liferayUserGroup) {

        for (UserAsMember member : usersAsMember) {
            User user = UserLocalServiceUtil.fetchUserByScreenName(COMPANY_ID, member.getScreenName());
            if (Objects.isNull(user)) {
                LOG.error("Can not set user " + member.getScreenName() + " as member of UserGroup. User does not exists...");
                continue;
            }

            UserGroupLocalServiceUtil.addUserUserGroup(user.getUserId(), liferayUserGroup.getUserGroupId());
            LOG.info("User " + user.getScreenName() + " successfully added as a member to UserGroup " + liferayUserGroup.getName());
        }

    }

    private static void setCustomFields(final long runAsUser, final com.liferay.portal.kernel.model.UserGroup liferayUserGroup,
                                        final long company, final List<CustomFieldSetting> customFieldSettings,
                                        final UserGroup userGroup) {

        if (liferayUserGroup == null) {
            return;
        }

        Class clazz = com.liferay.portal.kernel.model.UserGroup.class;

        for (CustomFieldSetting cfs : customFieldSettings) {
            String resolverHint = "Custom value for userGroup " + userGroup.getName() + ", "
                + " Key " + cfs.getKey() + ", value " + cfs.getValue();
            CustomFieldSettingUtil.setExpandoValue(resolverHint, runAsUser, liferayUserGroup.getUserGroupId(),
                company, clazz, liferayUserGroup.getUserGroupId(), cfs.getKey(), cfs.getValue());
        }
    }

    private static void addRolesToUserGroup(final UserGroup userGroup,
                                       final com.liferay.portal.kernel.model.UserGroup liferayUserGroup) {
        try {
            for (Role role : userGroup.getRole()) {
                com.liferay.portal.kernel.model.Role liferayRole = RoleLocalServiceUtil.getRole(COMPANY_ID, role.getName());
                String roleType = role.getType();
                switch (roleType) {
                    case "portal":
                        GroupLocalServiceUtil.addRoleGroup(liferayRole.getRoleId(), liferayUserGroup.getGroupId());
                        LOG.info("Adding role " + liferayRole.getDescriptiveName() + " to userGroup "
                            + liferayUserGroup.getName());
                        break;

                    case "site":
                    case "organization":
                        LOG.error("Adding site or organization roles to UserGroup is not supported. " +
                            "Bind userGroups to Site Roles within the Site elemennt.");
                        break;
                    default:
                        LOG.error("unknown role type " + roleType);
                        break;
                }
            }
        } catch (PortalException | SystemException e) {
            LOG.error("Error in adding roles to userGroup " + userGroup.getName(), e);
        }
    }
}
