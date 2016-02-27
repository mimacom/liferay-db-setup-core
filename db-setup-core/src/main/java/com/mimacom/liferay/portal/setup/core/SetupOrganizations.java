package com.mimacom.liferay.portal.setup.core;

/*
 * #%L
 * Liferay Portal DB Setup core
 * %%
 * Copyright (C) 2016 mimacom ag
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mimacom.liferay.portal.setup.domain.CustomFieldSetting;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.core.util.CustomFieldSettingUtil;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.ListTypeConstants;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.OrganizationConstants;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.OrganizationLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

public final class SetupOrganizations {

    private static final Log LOG = LogFactoryUtil.getLog(SetupOrganizations.class);
    private static final String DEFAULT_GROUP_NAME = "Guest";
    private static final long COMPANY_ID = PortalUtil.getDefaultCompanyId();

    private SetupOrganizations() {

    }

    public static void setupOrganizations(
            final List<com.mimacom.liferay.portal.setup.domain.Organization> organizations,
            final Organization parentOrg, final Group parentGroup) {

        for (com.mimacom.liferay.portal.setup.domain.Organization organization : organizations) {
            try {
                Organization liferayOrg = null;
                Group liferayGroup = null;
                long groupId = -1;
                if (organization.isDefault()) {
                    liferayGroup = GroupLocalServiceUtil.getGroup(COMPANY_ID, DEFAULT_GROUP_NAME);
                    groupId = liferayGroup.getGroupId();
                    LOG.info("Setup: default site. Group ID: " + groupId);
                } else if (organization.getName() == null) {
                    liferayGroup = GroupLocalServiceUtil.getCompanyGroup(COMPANY_ID);
                    groupId = liferayGroup.getGroupId();
                    LOG.info("Setup: global site. Group ID: " + groupId);
                } else {
                    try {
                        Organization org = OrganizationLocalServiceUtil.getOrganization(COMPANY_ID,
                                organization.getName());
                        liferayGroup = org.getGroup();
                        groupId = org.getGroupId();
                        liferayOrg = org;
                        LOG.info("Setup: Organization " + organization.getName()
                                + " already exist in system, not creating...");

                    } catch (PortalException | SystemException e) {
                        LOG.debug("Organization does not exist.", e);
                    }
                }

                if (groupId == -1) {
                    LOG.info("Setup: Organization " + organization.getName()
                            + " does not exist in system, creating...");

                    long defaultUserId = UserLocalServiceUtil.getDefaultUserId(COMPANY_ID);
                    Organization newOrganization = OrganizationLocalServiceUtil.addOrganization(
                            defaultUserId, 0, organization.getName(),
                            OrganizationConstants.TYPE_REGULAR_ORGANIZATION, false, 0, 0,
                            ListTypeConstants.ORGANIZATION_STATUS_DEFAULT, LiferaySetup.DESCRIPTION,
                            true, new ServiceContext());
                    liferayOrg = newOrganization;
                    liferayGroup = liferayOrg.getGroup();
                    groupId = newOrganization.getGroupId();
                    LOG.info("New Organization created. Group ID: " + groupId);

                }

                if (liferayGroup != null && organization.getSiteName() != null
                        && !organization.getSiteName().equals("")) {
                    liferayGroup.setName(organization.getSiteName());
                    GroupLocalServiceUtil.updateGroup(liferayGroup);
                    liferayGroup = liferayOrg.getGroup();
                }

                if (liferayGroup != null && organization.getSiteFriendlyUrl() != null
                        && !organization.getSiteFriendlyUrl().equals("")) {
                    liferayGroup.setFriendlyURL(organization.getSiteFriendlyUrl());
                    GroupLocalServiceUtil.updateGroup(liferayGroup);
                    liferayGroup = liferayOrg.getGroup();
                }

                if (parentOrg != null && liferayOrg != null
                        && organization.isMaintainOrganizationHierarchy()) {
                    liferayOrg.setParentOrganizationId(parentOrg.getOrganizationId());
                    OrganizationLocalServiceUtil.updateOrganization(liferayOrg);
                } else if (liferayOrg != null && organization.isMaintainOrganizationHierarchy()) {
                    liferayOrg.setParentOrganizationId(0);
                    OrganizationLocalServiceUtil.updateOrganization(liferayOrg);
                }

                if (parentGroup != null && liferayGroup != null
                        && organization.isMaintainSiteHierarchy()) {
                    liferayGroup.setParentGroupId(parentGroup.getGroupId());
                    GroupLocalServiceUtil.updateGroup(liferayGroup);
                } else if (liferayGroup != null && organization.isMaintainSiteHierarchy()) {
                    liferayGroup.setParentGroupId(0);
                    GroupLocalServiceUtil.updateGroup(liferayGroup);
                }

                LOG.info("Setting organization content...");

                SetupDocumentFolders.setupDocumentFolders(organization, groupId, COMPANY_ID);
                LOG.info("Document Folders setting finished.");

                SetupDocuments.setupOrganizationDocuments(organization, groupId, COMPANY_ID);
                LOG.info("Documents setting finished.");

                SetupPages.setupOrganizationPages(organization, groupId, COMPANY_ID,
                        LiferaySetup.getRunAsUserId());
                LOG.info("Organization Pages setting finished.");

                SetupWebFolders.setupWebFolders(organization, groupId, COMPANY_ID);
                LOG.info("Web folders setting finished.");

                SetupCategorization.setupVocabularies(organization, groupId);
                LOG.info("Organization Categories setting finished.");

                SetupArticles.setupOrganizationArticles(organization, groupId, COMPANY_ID);
                LOG.info("Organization Articles setting finished.");

                setCustomFields(LiferaySetup.getRunAsUserId(), groupId, COMPANY_ID, organization,
                        liferayOrg);
                LOG.info("Organization custom fields set up.");

                List<com.mimacom.liferay.portal.setup.domain.Organization> orgs = organization
                        .getOrganization();
                setupOrganizations(orgs, liferayOrg, liferayGroup);

            } catch (Exception e) {
                LOG.error("Error by setting up organization " + organization.getName(), e);
            }
        }

    }

    private static void setCustomFields(final long runAsUserId, final long groupId,
            final long company, final com.mimacom.liferay.portal.setup.domain.Organization org,
            final Organization liferayOrg) {
        if (liferayOrg == null && org.getCustomFieldSetting() != null
                && org.getCustomFieldSetting().size() > 0) {
            LOG.error("Default site or global site has not organization. Thus you may not be able"
                    + " to set custom field" + " values for the organiaztion!");
        } else {
            Class clazz = Organization.class;
            String resolverHint = "Resolving customized value for page " + org.getName() + " "
                    + "failed for key %%key%% " + "and value %%value%%";
            for (CustomFieldSetting cfs : org.getCustomFieldSetting()) {
                String key = cfs.getKey();
                String value = cfs.getValue();
                CustomFieldSettingUtil.setExpandoValue(
                        resolverHint.replace("%%key%%", key).replace("%%value%%", value),
                        runAsUserId, groupId, company, clazz, liferayOrg.getOrganizationId(), key,
                        value);
            }
        }
    }

    public static void deleteOrganization(
            final List<com.mimacom.liferay.portal.setup.domain.Organization> organizations,
            final String deleteMethod) {

        switch (deleteMethod) {
        case "excludeListed":
            Map<String, com.mimacom.liferay.portal.setup.domain.Organization> toBeDeletedOrganisations = convertOrganisationListToHashMap(
                    organizations);
            try {
                for (com.liferay.portal.model.Organization organisation : OrganizationLocalServiceUtil
                        .getOrganizations(-1, -1)) {
                    if (!toBeDeletedOrganisations.containsKey(organisation.getName())) {
                        try {
                            OrganizationLocalServiceUtil
                                    .deleteOrganization(organisation.getOrganizationId());
                            LOG.info("Deleting Organisation" + organisation.getName());
                        } catch (Exception e) {
                            LOG.error("Error by deleting Organisation !", e);
                        }
                    }
                }
            } catch (SystemException e) {
                LOG.error("Error by retrieving organisations!", e);
            }
            break;

        case "onlyListed":
            for (com.mimacom.liferay.portal.setup.domain.Organization organisation : organizations) {
                String name = organisation.getName();
                try {
                    Organization o = OrganizationLocalServiceUtil.getOrganization(COMPANY_ID, name);
                    OrganizationLocalServiceUtil.deleteOrganization(o);
                } catch (Exception e) {
                    LOG.error("Error by deleting Organisation !", e);
                }
                LOG.info("Deleting Organisation " + name);
            }

            break;

        default:
            LOG.error("Unknown delete method : " + deleteMethod);
            break;
        }
    }

    private static Map<String, com.mimacom.liferay.portal.setup.domain.Organization> convertOrganisationListToHashMap(
            final List<com.mimacom.liferay.portal.setup.domain.Organization> objects) {

        HashMap<String, com.mimacom.liferay.portal.setup.domain.Organization> map = new HashMap<>();
        for (com.mimacom.liferay.portal.setup.domain.Organization organization : objects) {
            map.put(organization.getName(), organization);
        }
        return map;
    }

}
