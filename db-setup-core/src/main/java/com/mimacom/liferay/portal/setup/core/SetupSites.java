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

import com.liferay.exportimport.kernel.service.StagingLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.core.util.CustomFieldSettingUtil;
import com.mimacom.liferay.portal.setup.core.util.PortletConstants;
import com.mimacom.liferay.portal.setup.core.util.TitleMapUtil;
import com.mimacom.liferay.portal.setup.domain.CustomFieldSetting;
import com.mimacom.liferay.portal.setup.domain.Site;
import com.mimacom.liferay.portal.setup.domain.Staging;

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
                    groupId = liferayGroup.getGroupId();
                    LOG.info("Setup: default site. Group ID: " + groupId);
                } else if (site.getName() == null) {
                    liferayGroup = GroupLocalServiceUtil.getCompanyGroup(COMPANY_ID);
                    groupId = liferayGroup.getGroupId();
                    LOG.info("Setup: global site. Group ID: " + groupId);
                } else {
                    try {
                        liferayGroup = GroupLocalServiceUtil.getGroup(COMPANY_ID, site.getName());
                        groupId = liferayGroup.getGroupId();
                        LOG.info("Setup: Site " + site.getName()
                                + " already exists in system, not creating...");

                    } catch (PortalException | SystemException e) {
                        LOG.debug("Site does not exist.", e);
                    }
                }

                if (groupId == -1) {
                    LOG.info("Setup: Group (Site) " + site.getName()
                            + " does not exist in system, creating...");

                    long defaultUserId = UserLocalServiceUtil.getDefaultUserId(COMPANY_ID);

                    ServiceContext serviceContext = new ServiceContext();

                    com.liferay.portal.kernel.model.Group newGroup = GroupLocalServiceUtil.addGroup(
                            defaultUserId, GroupConstants.DEFAULT_PARENT_GROUP_ID, Group.class.getName(),
                            0, 0, TitleMapUtil.getLocalizationMap(site.getName()), null, GroupConstants.TYPE_SITE_RESTRICTED, true, GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, site.getSiteFriendlyUrl(), true, true, serviceContext);
                    liferayGroup = newGroup;
                    groupId = newGroup.getGroupId();
                    LOG.info("New Organization created. Group ID: " + groupId);
                }

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

                SetupDocumentFolders.setupDocumentFolders(site, groupId, COMPANY_ID);
                LOG.info("Document Folders setting finished.");

                SetupDocuments.setupSiteDocuments(site, groupId, COMPANY_ID);
                LOG.info("Documents setting finished.");

                SetupPages.setupSitePages(site, groupId, COMPANY_ID, userId);
                LOG.info("Organization Pages setting finished.");

                SetupWebFolders.setupWebFolders(site, groupId, COMPANY_ID);
                LOG.info("Web folders setting finished.");

                SetupCategorization.setupVocabularies(site, groupId);
                LOG.info("Organization Categories setting finished.");

                SetupArticles.setupSiteArticles(site, groupId, COMPANY_ID);
                LOG.info("Organization Articles setting finished.");

                setCustomFields(userId, groupId, COMPANY_ID, site);
                LOG.info("Organization custom fields set up.");

                setStaging(userId, liferayGroup, site.getStaging());

                List<com.mimacom.liferay.portal.setup.domain.Site> sites = site
                        .getSite();
                setupSites(sites, liferayGroup);

            } catch (Exception e) {
                LOG.error("Error by setting up site " + site.getName(), e);
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
