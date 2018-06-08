package com.mimacom.liferay.portal.setup.core;

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

import com.liferay.exportimport.kernel.service.StagingLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.*;
import com.liferay.portal.kernel.util.PortalUtil;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.core.util.CustomFieldSettingUtil;
import com.mimacom.liferay.portal.setup.core.util.PortletConstants;
import com.mimacom.liferay.portal.setup.core.util.FieldMapUtil;
import com.mimacom.liferay.portal.setup.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by gustavnovotny on 28.08.17.
 */
public class SetupSites {

    private static final Log LOG = LogFactoryUtil.getLog(SetupSites.class);
    private static final String DEFAULT_GROUP_NAME = "Guest";
    private static final long COMPANY_ID = PortalUtil.getDefaultCompanyId();

    private SetupSites() {

    }

    public static void setupSites(final List<com.mimacom.liferay.portal.setup.domain.Site> groups, final Group parentGroup) {

        CompanyThreadLocal.setCompanyId(COMPANY_ID);
        for (com.mimacom.liferay.portal.setup.domain.Site site : groups) {
            try {
                Group liferayGroup = null;
                long groupId = -1;
                if (site.isDefault()) {
                    liferayGroup = GroupLocalServiceUtil.getGroup(COMPANY_ID, DEFAULT_GROUP_NAME);
                    LOG.info("Setup: default site. Group ID: " + groupId);
                } else if (site.getName() == null) {
                    liferayGroup = GroupLocalServiceUtil.getCompanyGroup(COMPANY_ID);
                    LOG.info("Setup: global site. Group ID: " + groupId);
                } else {
                    try {
                        liferayGroup = GroupLocalServiceUtil.getGroup(COMPANY_ID, site.getName());
                        LOG.info("Setup: Site " + site.getName()
                                + " already exists in system, not creating...");

                    } catch (PortalException | SystemException e) {
                        LOG.debug("Site does not exist.", e);
                    }
                }
                long defaultUserId = UserLocalServiceUtil.getDefaultUserId(COMPANY_ID);
                ServiceContext serviceContext = new ServiceContext();

                if (liferayGroup == null) {
                    LOG.info("Setup: Group (Site) " + site.getName()
                            + " does not exist in system, creating...");

                    liferayGroup = GroupLocalServiceUtil.addGroup(
                            defaultUserId, GroupConstants.DEFAULT_PARENT_GROUP_ID, Group.class.getName(),
                            0, 0, FieldMapUtil.getLocalizationMap(site.getName()), null, GroupConstants.TYPE_SITE_RESTRICTED, true, GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, site.getSiteFriendlyUrl(), true, true, serviceContext);
                    LOG.info("New Organization created. Group ID: " + groupId);
                } else {
                    LOG.info("Setup: Updating " + site.getName());
                    GroupLocalServiceUtil.updateFriendlyURL(liferayGroup.getGroupId(), site.getSiteFriendlyUrl());
                }
                groupId = liferayGroup.getGroupId();

                if (parentGroup != null && liferayGroup != null
                        && site.isMaintainSiteHierarchy()) {
                    liferayGroup.setParentGroupId(parentGroup.getGroupId());
                    GroupLocalServiceUtil.updateGroup(liferayGroup);
                } else if (liferayGroup != null && site.isMaintainSiteHierarchy()) {
                    liferayGroup.setParentGroupId(0);
                    GroupLocalServiceUtil.updateGroup(liferayGroup);
                }

                LOG.info("Setting site content...");

                long userId = LiferaySetup.getRunAsUserId();

                setStaging(userId, liferayGroup, site.getStaging());

                // If staging group exists for present Group, add all content to staging group
                Group stagingGroup = liferayGroup.getStagingGroup();
                if (Objects.nonNull(stagingGroup)) {
                    groupId = stagingGroup.getGroupId();
                }

                SetupArticles.setupSiteStructuresAndTemplates(site, groupId, COMPANY_ID);
                LOG.info("Site DDM structures and templates setting finished.");

                SetupDocumentFolders.setupDocumentFolders(site, groupId, COMPANY_ID);
                LOG.info("Document Folders setting finished.");

                SetupDocuments.setupSiteDocuments(site, groupId, COMPANY_ID);
                LOG.info("Documents setting finished.");

                SetupPages.setupSitePages(site, groupId, COMPANY_ID, userId);
                LOG.info("Site Pages setting finished.");

                SetupWebFolders.setupWebFolders(site, groupId, COMPANY_ID);
                LOG.info("Web folders setting finished.");

                SetupCategorization.setupVocabularies(site, groupId);
                LOG.info("Site Categories setting finished.");

                SetupArticles.setupSiteArticles(site, groupId, COMPANY_ID);
                LOG.info("Site Articles setting finished.");

                setCustomFields(userId, groupId, COMPANY_ID, site);
                LOG.info("Site custom fields set up.");

                // Users and Groups should be referenced to live Group
                setMembership(site.getMembership(), COMPANY_ID, liferayGroup.getGroupId());

                List<com.mimacom.liferay.portal.setup.domain.Site> sites = site
                        .getSite();
                setupSites(sites, liferayGroup);

            } catch (Exception e) {
                LOG.error("Error by setting up site " + site.getName(), e);
            }
        }
    }

    private static void setMembership(Membership membership, long companyId, long groupId) {
        if (Objects.isNull(membership)) {
            return;
        }

        List<UsergroupAsMember> memberGroups = membership.getUsergroupAsMember();
        assignMemberGroups(memberGroups, companyId, groupId);

        List<UserAsMember> memberUsers = membership.getUserAsMember();
        assignMemberUsers(memberUsers, companyId, groupId);

    }

    private static void assignMemberUsers(List<UserAsMember> memberUsers, long companyId, long groupId) {
        if (Objects.isNull(memberUsers) || memberUsers.isEmpty()) {
            return;
        }

        for (UserAsMember memberUser : memberUsers) {
            User user = UserLocalServiceUtil.fetchUserByScreenName(companyId, memberUser.getScreenName());
            if (Objects.isNull(user)) {
                LOG.error("User with screenName " + memberUser.getScreenName() + " does not exists. Won't be assigned as site member.");
                continue;
            }

            try {
                Group liferayGroup = GroupLocalServiceUtil.getGroup(groupId);
                GroupLocalServiceUtil.addUserGroup(user.getUserId(), liferayGroup.getGroupId());
                LOG.info("User " + user.getScreenName() + " was assigned as member of site " + liferayGroup.getDescriptiveName());

                assignUserMemberRoles(memberUser.getRole(), companyId, liferayGroup, user);

            } catch (PortalException e) {
                e.printStackTrace();
            }

        }
    }

    private static void assignUserMemberRoles(List<Role> membershipRoles, long companyId, Group liferayGroup, User liferayUser) {
        if (Objects.isNull(membershipRoles) || membershipRoles.isEmpty()) {
            return;
        }

        for (Role membershipRole : membershipRoles) {
            try {
                com.liferay.portal.kernel.model.Role liferayRole = RoleLocalServiceUtil.getRole(companyId, membershipRole.getName());
                UserGroupRoleLocalServiceUtil.addUserGroupRoles(liferayUser.getUserId(), liferayGroup.getGroupId(), new long[]{liferayRole.getRoleId()});
                StringBuilder sb = new StringBuilder("Role ")
                        .append(liferayRole.getDescriptiveName())
                        .append(" assigned to User ")
                        .append(liferayUser.getScreenName())
                        .append(" for site ")
                        .append(liferayGroup.getDescriptiveName());

                LOG.info(sb.toString());
            } catch (PortalException e) {
                LOG.error("Can not add role with name" + membershipRole.getName() + " does not exists. Will not be assigned.");
            }
        }

    }

    private static void assignMemberGroups(List<UsergroupAsMember> memberGroups, long companyId, long groupId) {
        if (Objects.isNull(memberGroups) || memberGroups.isEmpty()) {
            return;
        }

        for (UsergroupAsMember memberGroup : memberGroups) {
            try {
                UserGroup liferayUserGroup = UserGroupLocalServiceUtil.getUserGroup(companyId, memberGroup.getUsergroupName());
                Group liferayGroup = GroupLocalServiceUtil.getGroup(groupId);
                GroupLocalServiceUtil.addUserGroupGroup(liferayUserGroup.getUserGroupId(), liferayGroup);
                LOG.info("UserGroup " + liferayUserGroup.getName() + " was assigned as site member to " + liferayGroup.getDescriptiveName());

                assignGroupMemberRoles(memberGroup.getRole(), companyId, liferayGroup, liferayUserGroup);
            } catch (PortalException e) {
                LOG.error("Cannot find UserGroup with name: " + memberGroup.getUsergroupName() + ". Group won't be assigned to site.", e);
                continue;
            }
        }
    }

    private static void assignGroupMemberRoles(List<Role> membershipRoles, long companyId, Group liferayGroup, UserGroup liferayUserGroup) {
        if (Objects.isNull(membershipRoles) || membershipRoles.isEmpty()) {
            return;
        }

        for (Role membershipRole : membershipRoles) {
            try {
                com.liferay.portal.kernel.model.Role liferayRole = RoleLocalServiceUtil.getRole(companyId, membershipRole.getName());
                UserGroupGroupRoleLocalServiceUtil.addUserGroupGroupRoles(liferayUserGroup.getUserGroupId(), liferayGroup.getGroupId(), new long[]{liferayRole.getRoleId()});
                StringBuilder sb = new StringBuilder("Role ")
                        .append(liferayRole.getDescriptiveName())
                        .append(" assigned to UserGroup ")
                        .append(liferayUserGroup.getName())
                        .append(" for site ")
                        .append(liferayGroup.getDescriptiveName());

                LOG.info(sb.toString());
            } catch (PortalException e) {
                LOG.error("Can not add role with name" + membershipRole.getName() + " does not exists. Will not be assigned.");
            }
        }
    }

    static void setStaging(long userId, Group liveGroup, Staging staging) {
        if (Objects.isNull(staging)) {
            LOG.info("No staging configuration present for site.");
            return;
        }

        // only local staging supported, yet
        LOG.info("Setting up staging for site: " + liveGroup.getName());
        try {
            if (staging.getType().equals("local")) {
                ServiceContext serviceContext = new ServiceContext();
                serviceContext.setAttribute(PortletConstants.STAGING_PARAM_TEMPLATE.replace("#", "com_liferay_dynamic_data_mapping_web_portlet_PortletDisplayTemplatePortlet"), staging.isStageAdt());

                setStagingParam(staging.isStageAdt(), PortletConstants.STAGING_PORTLET_ID_ADT, serviceContext);
                setStagingParam(staging.isStageBlogs(), PortletConstants.STAGING_PORTLET_ID_BLOGS, serviceContext);
                setStagingParam(staging.isStageBookmarks(), PortletConstants.STAGING_PORTLET_ID_BOOKMARKS, serviceContext);
                setStagingParam(staging.isStageCalendar(), PortletConstants.STAGING_PORTLET_ID_CALENDAR, serviceContext);
                setStagingParam(staging.isStageDdl(), PortletConstants.STAGING_PORTLET_ID_DDL, serviceContext);
                setStagingParam(staging.isStageDocumentLibrary(), PortletConstants.STAGING_PORTLET_ID_DL, serviceContext);
                setStagingParam(staging.isStageForms(), PortletConstants.STAGING_PORTLET_ID_FORMS, serviceContext);
                setStagingParam(staging.isStageMessageBoards(), PortletConstants.STAGING_PORTLET_ID_MB, serviceContext);
                setStagingParam(staging.isStageMobileRules(), PortletConstants.STAGING_PORTLET_ID_MDR, serviceContext);
                setStagingParam(staging.isStagePolls(), PortletConstants.STAGING_PORTLET_ID_POLLS, serviceContext);
                setStagingParam(staging.isStageWebContent(), PortletConstants.STAGING_PORTLET_ID_WEB_CONTENT, serviceContext);
                setStagingParam(staging.isStageWiki(), PortletConstants.STAGING_PORTLET_ID_WIKI, serviceContext);

                StagingLocalServiceUtil.enableLocalStaging(userId, liveGroup, staging.isBranchingPublic(), staging.isBranchingPrivate(), serviceContext);
                LOG.info("Local staging switched on.");
            }
            if (staging.getType().equals("remote")) {
                LOG.error("Remote staging setup is not supported, yet. Staging not set up.");
            }
            if (staging.getType().equals("none") && liveGroup.hasLocalOrRemoteStagingGroup()) {
                ServiceContext serviceContext = new ServiceContext();
                serviceContext.setUserId(userId);
                serviceContext.setAttribute("forceDisable", true);
                StagingLocalServiceUtil.disableStaging(liveGroup, serviceContext);
                LOG.info("Staging switched off.");
            }

        } catch (PortalException e) {
            LOG.error("Error while setting up staging.", e);
        }
    }

    private static void setStagingParam(boolean isParamOn, String portletId, ServiceContext serviceContext) {
        serviceContext.setAttribute(PortletConstants.STAGING_PARAM_TEMPLATE.replace("#", portletId), String.valueOf(isParamOn));
    }

    static void setCustomFields(final long runAsUserId, final long groupId,
                                final long company, final Site site) {
        if (site.getCustomFieldSetting() == null || site.getCustomFieldSetting().isEmpty()) {
            LOG.error("Site does has no Expando field settings.");
        } else {
            Class clazz = com.liferay.portal.kernel.model.Group.class;
            String resolverHint = "Resolving customized value for page " + site.getName() + " "
                    + "failed for key %%key%% " + "and value %%value%%";
            for (CustomFieldSetting cfs : site.getCustomFieldSetting()) {
                String key = cfs.getKey();
                String value = cfs.getValue();
                CustomFieldSettingUtil.setExpandoValue(
                        resolverHint.replace("%%key%%", key).replace("%%value%%", value),
                        runAsUserId, groupId, company, clazz, groupId, key, value);
            }
        }
    }

    public static void deleteSite(
            final List<com.mimacom.liferay.portal.setup.domain.Site> sites,
            final String deleteMethod) {

        switch (deleteMethod) {
            case "excludeListed":
                Map<String, Site> toBeDeletedOrganisations = convertSiteListToHashMap(
                        sites);
                try {
                    for (com.liferay.portal.kernel.model.Group siteGroup : GroupLocalServiceUtil
                            .getGroups(QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {
                        if (!toBeDeletedOrganisations.containsKey(siteGroup.getName())) {
                            deleteLiferayGroup(siteGroup);
                        }
                    }
                } catch (SystemException e) {
                    LOG.error("Error by retrieving sites!", e);
                }
                break;

            case "onlyListed":
                for (com.mimacom.liferay.portal.setup.domain.Site site : sites) {
                    String name = site.getName();
                    try {
                        com.liferay.portal.kernel.model.Group o = GroupLocalServiceUtil.getGroup(COMPANY_ID, name);
                        GroupLocalServiceUtil.deleteGroup(o);
                    } catch (Exception e) {
                        LOG.error("Error by deleting Site !", e);
                    }
                    LOG.info("Deleting Site " + name);
                }

                break;

            default:
                LOG.error("Unknown delete method : " + deleteMethod);
                break;
        }
    }

    private static void deleteLiferayGroup(Group siteGroup) {
        try {
            GroupLocalServiceUtil.deleteGroup(siteGroup.getGroupId());
            LOG.info("Deleting Site" + siteGroup.getName());
        } catch (Exception e) {
            LOG.error("Error by deleting Site !", e);
        }
    }

    public static void addSiteUser(com.liferay.portal.kernel.model.Group group, User user) {
        LOG.info("Adding user with screenName: " + user.getScreenName() + "to group with name: " + group.getName());
        GroupLocalServiceUtil.addUserGroup(user.getUserId(), group);
    }

    public static void addSiteUsers(com.liferay.portal.kernel.model.Group group, User... users) {
        for (int i = 0; i < users.length; i++) {
            addSiteUser(group, users[i]);
        }
    }

    private static Map<String, com.mimacom.liferay.portal.setup.domain.Site> convertSiteListToHashMap(
            final List<com.mimacom.liferay.portal.setup.domain.Site> objects) {

        HashMap<String, Site> map = new HashMap<>();
        for (com.mimacom.liferay.portal.setup.domain.Site site : objects) {
            map.put(site.getName(), site);
        }
        return map;
    }

}
