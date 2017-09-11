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

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetLinkConstants;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetLinkLocalServiceUtil;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalServiceUtil;
import com.liferay.dynamic.data.mapping.exception.StructureDuplicateStructureKeyException;
import com.liferay.dynamic.data.mapping.exception.TemplateDuplicateTemplateKeyException;
import com.liferay.dynamic.data.mapping.model.*;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.dynamic.data.mapping.util.DDMUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleConstants;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.core.util.ResolverUtil;
import com.mimacom.liferay.portal.setup.core.util.ResourcesUtil;
import com.mimacom.liferay.portal.setup.core.util.TaggingUtil;
import com.mimacom.liferay.portal.setup.core.util.TitleMapUtil;
import com.mimacom.liferay.portal.setup.core.util.WebFolderUtil;
import com.mimacom.liferay.portal.setup.domain.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mapa, guno..
 */
public final class SetupArticles {

    private static final Log LOG = LogFactoryUtil.getLog(SetupArticles.class);
    private static final HashMap<String, List<String>> DEFAULT_PERMISSIONS;
    private static final int ARTICLE_PUBLISH_YEAR = 2008;
    private static final int MIN_DISPLAY_ROWS = 10;

    static {
        DEFAULT_PERMISSIONS = new HashMap<String, List<String>>();
        List<String> actionsOwner = new ArrayList<String>();

        actionsOwner.add(ActionKeys.VIEW);
        actionsOwner.add(ActionKeys.ADD_DISCUSSION);
        actionsOwner.add(ActionKeys.DELETE);
        actionsOwner.add(ActionKeys.DELETE_DISCUSSION);
        actionsOwner.add(ActionKeys.EXPIRE);
        actionsOwner.add(ActionKeys.PERMISSIONS);
        actionsOwner.add(ActionKeys.UPDATE);
        actionsOwner.add(ActionKeys.UPDATE_DISCUSSION);

        DEFAULT_PERMISSIONS.put(RoleConstants.OWNER, actionsOwner);

        List<String> actionsUser = new ArrayList<String>();
        actionsUser.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.USER, actionsUser);

        List<String> actionsGuest = new ArrayList<String>();
        actionsGuest.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.GUEST, actionsGuest);
    }

    private SetupArticles() {

    }

    public static void setupOrganizationArticles(final Organization organization,
                                                 final long groupId, final long companyId) throws PortalException, SystemException {
        List<Structure> articleStructures = organization.getArticleStructure();

        if (articleStructures != null) {
            long classNameId = ClassNameLocalServiceUtil.getClassNameId(JournalArticle.class);
            for (Structure structure : articleStructures) {
                try {
                    addDDMStructure(structure, groupId, classNameId);
                } catch (StructureDuplicateStructureKeyException | IOException
                        | URISyntaxException e) {
                    LOG.error(e);
                }
            }
        }

        List<Structure> ddlStructures = organization.getDdlStructure();

        if (articleStructures != null) {
            long classNameId = ClassNameLocalServiceUtil.getClassNameId(DDLRecordSet.class);
            for (Structure structure : ddlStructures) {
                LOG.info("Adding DDL structure " + structure.getName());
                try {
                    addDDMStructure(structure, groupId, classNameId);
                } catch (StructureDuplicateStructureKeyException | IOException
                        | URISyntaxException e) {
                    LOG.error(e);
                }
            }
        }

        List<ArticleTemplate> articleTemplates = organization.getArticleTemplate();
        if (articleTemplates != null) {
            for (ArticleTemplate template : articleTemplates) {
                try {
                    addDDMTemplate(template, groupId);
                } catch (TemplateDuplicateTemplateKeyException | IOException
                        | URISyntaxException e) {
                    LOG.error(e);
                }
            }
        }
        List<Article> articles = organization.getArticle();
        if (articles != null) {
            for (Article article : articles) {
                addJournalArticle(article, groupId, companyId);
            }
        }
        List<Adt> adts = organization.getAdt();
        if (adts != null) {
            for (Adt template : adts) {
                try {
                    addDDMTemplate(template, groupId);
                } catch (TemplateDuplicateTemplateKeyException | URISyntaxException
                        | IOException e) {
                    LOG.error("Error in adding ADT: " + template.getName(), e);
                }
            }
        }
        List<DdlRecordset> recordSets = organization.getDdlRecordset();
        if (recordSets != null) {
            for (DdlRecordset recordSet : recordSets) {
                try {
                    addDDLRecordSet(recordSet, groupId);
                } catch (TemplateDuplicateTemplateKeyException e) {
                    LOG.error("Error in adding DDLRecordSet: " + recordSet.getName(), e);
                }
            }
        }
    }

    public static void addDDMStructure(final Structure structure, final long groupId,
                                       final long classNameId)
            throws SystemException, PortalException, IOException, URISyntaxException {

        LOG.info("Adding Article structure " + structure.getName());
        Map<Locale, String> nameMap = new HashMap<>();
        Locale siteDefaultLocale = PortalUtil.getSiteDefaultLocale(groupId);
        String name = getStructureNameOrKey(structure);
        nameMap.put(siteDefaultLocale, name);
        Map<Locale, String> descMap = new HashMap<>();

        String content = null;
        DDMForm ddmForm = null;
        DDMFormLayout ddmFormLayout = null;
        try {
            content = ResourcesUtil.getFileContent(structure.getPath());
            ddmForm = DDMUtil.getDDMForm(content);
            ddmFormLayout = DDMUtil.getDefaultDDMFormLayout(ddmForm);
        } catch (IOException e) {
            LOG.error("Error Reading Structure File content for: " + structure.getName());
            return;
        } catch (PortalException e) {
            LOG.error("Can not parse given structure JSON content into Liferay DDMForm.", e);
        }

        Locale contentDefaultLocale = ddmForm.getDefaultLocale();
        if (!contentDefaultLocale.equals(siteDefaultLocale)) {
            nameMap.put(contentDefaultLocale, name);
        }

        DDMStructure ddmStructure = null;
        try {
            ddmStructure = DDMStructureLocalServiceUtil.fetchStructure(groupId, classNameId,
                    structure.getKey());
        } catch (SystemException e) {
            LOG.error("Error while trying to find Structure with key: " + structure.getKey(), e);
        }

        if (ddmStructure != null) {
            LOG.info("Structure already exists and will be overwritten.");
            if (structure.getParent() != null && !structure.getParent().isEmpty()) {
                LOG.info("Setting up parent structure: " + structure.getName());
                DDMStructure parentStructure = DDMStructureLocalServiceUtil.fetchStructure(groupId,
                        classNameId, structure.getParent(), true);
                if (parentStructure != null) {
                    ddmStructure.setParentStructureId(parentStructure.getStructureId());
                } else {
                    LOG.info("Parent structure not found: " + structure.getName());
                }
            }

            DDMStructureLocalServiceUtil.updateStructure(LiferaySetup.getRunAsUserId(), ddmStructure.getStructureId(),
                    ddmStructure.getParentStructureId(), nameMap, descMap, ddmForm, ddmFormLayout, new ServiceContext());
            LOG.info("Template successfully updated: " + structure.getName());
            return;
        }

        DDMStructure newStructure = DDMStructureLocalServiceUtil.addStructure(
                LiferaySetup.getRunAsUserId(), groupId, structure.getParent(), classNameId,
                structure.getKey(), nameMap, descMap, ddmForm, ddmFormLayout, "json", 0, new ServiceContext());
        LOG.info("Added Article structure: " + newStructure.getName());
    }

    private static String getStructureNameOrKey(final Structure structure) {
        if (structure.getName() == null) {
            return structure.getName();
        }
        return structure.getKey();
    }

    public static void addDDMTemplate(final ArticleTemplate template, final long groupId)
            throws SystemException, PortalException, IOException, URISyntaxException {

        LOG.info("Adding Article template " + template.getName());
        long classNameId = ClassNameLocalServiceUtil.getClassNameId(DDMStructure.class);
        long resourceClassnameId = ClassNameLocalServiceUtil.getClassNameId(JournalArticle.class);
        Map<Locale, String> nameMap = new HashMap<>();
        Locale siteDefaultLocale = PortalUtil.getSiteDefaultLocale(groupId);
        String name = template.getName();
        if (name == null) {
            name = template.getKey();
        }
        nameMap.put(siteDefaultLocale, name);
        Map<Locale, String> descMap = new HashMap<>();

        String script;
        try {
            script = ResourcesUtil.getFileContent(template.getPath());
        } catch (IOException e) {
            LOG.error("Error Reading Template File content for: " + template.getName());
            return;
        }

        long classPK = 0;
        if (template.getArticleStructureKey() != null) {
            classPK = ResolverUtil.getStructureId(template.getArticleStructureKey(), groupId,
                    JournalArticle.class);
        }

        DDMTemplate ddmTemplate = null;
        try {
            ddmTemplate = DDMTemplateLocalServiceUtil.fetchTemplate(groupId, classNameId, template.getKey());
        } catch (SystemException e) {
            LOG.error("Error while trying to find template with key: " + template.getKey(), e);
        }

        if (ddmTemplate != null) {
            LOG.info("Template already exists and will be overwritten.");
            ddmTemplate.setNameMap(nameMap);
            ddmTemplate.setLanguage(template.getLanguage());
            ddmTemplate.setScript(script);
            ddmTemplate.setClassPK(classPK);

            DDMTemplateLocalServiceUtil.updateDDMTemplate(ddmTemplate);
            LOG.info("Template successfully updated: " + ddmTemplate.getName());
            return;
        }

        DDMTemplate newTemplate = DDMTemplateLocalServiceUtil.addTemplate(
                LiferaySetup.getRunAsUserId(), groupId, classNameId, classPK, resourceClassnameId, template.getKey(),
                nameMap, descMap, DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY, null, template.getLanguage(), script, true, false,
                null, null, new ServiceContext());
        LOG.info("Added Article template: " + newTemplate.getName());
    }

    public static void addDDMTemplate(final Adt template, final long groupId)
            throws SystemException, PortalException, IOException, URISyntaxException {

        LOG.info("Adding ADT " + template.getName());
        long classNameId = PortalUtil.getClassNameId(template.getClassName());
        long resourceClassnameId = ClassNameLocalServiceUtil.getClassNameId(JournalArticle.class);

        Map<Locale, String> nameMap = new HashMap<Locale, String>();

        Locale siteDefaultLocale = PortalUtil.getSiteDefaultLocale(groupId);
        String name = template.getName();
        if (name == null) {
            name = template.getTemplateKey();
        }
        nameMap.put(siteDefaultLocale, name);

        Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
        descriptionMap.put(siteDefaultLocale, template.getDescription());

        DDMTemplate ddmTemplate = null;
        try {
            ddmTemplate = DDMTemplateLocalServiceUtil.fetchTemplate(groupId, classNameId,
                    template.getTemplateKey());
        } catch (SystemException e) {
            LOG.error("Error while trying to find ADT with key: " + template.getTemplateKey());
        }

        String script = ResourcesUtil.getFileContent(template.getPath());

        if (ddmTemplate != null) {
            LOG.info("Template already exists and will be overwritten.");
            ddmTemplate.setLanguage(template.getLanguage());
            ddmTemplate.setNameMap(nameMap);
            ddmTemplate.setDescriptionMap(descriptionMap);
            ddmTemplate.setClassName(template.getClassName());
            ddmTemplate.setCacheable(template.isCacheable());
            ddmTemplate.setScript(script);

            DDMTemplateLocalServiceUtil.updateDDMTemplate(ddmTemplate);
            LOG.info("ADT successfully updated: " + ddmTemplate.getName());
            return;
        }

        DDMTemplate newTemplate = DDMTemplateLocalServiceUtil.addTemplate(
                LiferaySetup.getRunAsUserId(), groupId, classNameId, 0, resourceClassnameId, template.getTemplateKey(),
                nameMap, descriptionMap, DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY, null, template.getLanguage(), script, true, false,
                null, null, new ServiceContext());
        LOG.info("Added ADT: " + newTemplate.getName());
    }

    public static void addJournalArticle(final Article article, final long groupId,
                                         final long companyId) {
        LOG.info("Adding Journal Article " + article.getTitle());

        String content = null;
        long folderId = 0L;
        if (article.getArticleFolderPath() != null && !article.getArticleFolderPath().equals("")) {
            JournalFolder jf = WebFolderUtil.findWebFolder(companyId, groupId,
                    LiferaySetup.getRunAsUserId(), article.getArticleFolderPath(), "", true);
            if (jf == null) {
                LOG.warn("Specified webfolder " + article.getArticleFolderPath() + " of article "
                        + article.getTitle()
                        + " not found! Will put article into web content root folder!");
            } else {
                folderId = jf.getFolderId();
            }
        }
        try {
            content = ResourcesUtil.getFileContent(article.getPath());
            content = ResolverUtil.lookupAll(LiferaySetup.getRunAsUserId(), groupId, companyId,
                    content, article.getPath());
        } catch (IOException e) {
            LOG.error(
                    "Error Reading Article File content for article ID: " + article.getArticleId());
        }
        Map<Locale, String> titleMap = TitleMapUtil.getTitleMap(article.getTitleTranslation(),
                groupId, article.getTitle(), " Article with title " + article.getArticleId());

        Locale articleDefaultLocale = LocaleUtil.fromLanguageId(
                LocalizationUtil.getDefaultLanguageId(content));
        if (!titleMap.containsKey(articleDefaultLocale)) {
            titleMap.put(articleDefaultLocale, article.getTitle());
        }

        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setScopeGroupId(groupId);

        JournalArticle journalArticle = null;

        boolean generatedId = (article.getArticleId().isEmpty());
        if (generatedId) {
            LOG.info("Article " + article.getTitle() + " will have autogenerated ID.");
        } else {
            try {
                journalArticle = JournalArticleLocalServiceUtil.fetchLatestArticle(groupId,
                        article.getArticleId(), WorkflowConstants.STATUS_APPROVED);
            } catch (SystemException e) {
                LOG.error("Error while trying to find article with ID: " + article.getArticleId(),
                        e);
            }
        }

        try {
            if (journalArticle == null) {
                journalArticle = JournalArticleLocalServiceUtil.addArticle(
                        LiferaySetup.getRunAsUserId(), groupId, folderId, 0, 0,
                        article.getArticleId(), generatedId,
                        JournalArticleConstants.VERSION_DEFAULT, titleMap, null, content,
                        article.getArticleStructureKey(), article.getArticleTemplateKey(),
                        StringPool.BLANK, 1, 1, ARTICLE_PUBLISH_YEAR, 0, 0, 0, 0, 0, 0, 0, true, 0,
                        0, 0, 0, 0, true, true, false, StringPool.BLANK, null, null,
                        StringPool.BLANK, serviceContext);

                LOG.info("Added JournalArticle " + journalArticle.getTitle() + " with ID: "
                        + journalArticle.getArticleId());
                Indexer bi = IndexerRegistryUtil.getIndexer(JournalArticle.class);
                if (bi != null) {
                    bi.reindex(journalArticle);
                }
            } else {
                LOG.info("Article " + article.getTitle() + " with article ID: "
                        + article.getArticleId() + " already exists. Will be overwritten.");
                journalArticle.setTitleMap(titleMap);
                journalArticle.setContent(content);

                JournalArticleLocalServiceUtil.updateJournalArticle(journalArticle);

                // if the folder changed, move it...
                if (journalArticle.getFolderId() != folderId) {
                    JournalArticleLocalServiceUtil.moveArticle(groupId,
                            journalArticle.getArticleId(), folderId);
                }
                LOG.info("Updated JournalArticle: " + journalArticle.getTitle());
            }
            TaggingUtil.associateTags(groupId, article, journalArticle);
            processRelatedAssets(article, journalArticle, LiferaySetup.getRunAsUserId(), groupId,
                    companyId);
            SetupPermissions.updatePermission("Article " + journalArticle.getArticleId(), groupId,
                    companyId, journalArticle.getResourcePrimKey(), JournalArticle.class,
                    article.getRolePermissions(), DEFAULT_PERMISSIONS);
        } catch (PortalException | SystemException e) {
            LOG.error("Error while trying to add/update Article with Title: " + article.getTitle(),
                    e);
        }
    }

    private static void addDDLRecordSet(final DdlRecordset recordSet, final long groupId)
            throws SystemException, PortalException {
        LOG.info("Adding DDLRecordSet " + recordSet.getName());
        Map<Locale, String> nameMap = new HashMap<>();
        Locale siteDefaultLocale = PortalUtil.getSiteDefaultLocale(groupId);
        nameMap.put(siteDefaultLocale, recordSet.getName());
        Map<Locale, String> descMap = new HashMap<>();
        descMap.put(siteDefaultLocale, recordSet.getDescription());
        DDLRecordSet ddlRecordSet = null;
        try {
            ddlRecordSet = DDLRecordSetLocalServiceUtil.fetchRecordSet(groupId, recordSet.getKey());
        } catch (SystemException e) {
            LOG.error("Error while trying to find DDLRecordSet with key: " + recordSet.getKey(), e);
        }

        if (ddlRecordSet != null) {
            LOG.info("DDLRecordSet already exists and will be overwritten.");
            ddlRecordSet.setNameMap(nameMap);
            ddlRecordSet.setDescriptionMap(descMap);
            ddlRecordSet.setDDMStructureId(ResolverUtil
                    .getStructureId(recordSet.getDdlStructureKey(), groupId, DDLRecordSet.class));
            DDLRecordSetLocalServiceUtil.updateDDLRecordSet(ddlRecordSet);
            LOG.info("DDLRecordSet successfully updated: " + recordSet.getName());
            return;
        }

        DDLRecordSet newDDLRecordSet = DDLRecordSetLocalServiceUtil.addRecordSet(
                LiferaySetup.getRunAsUserId(), groupId,
                ResolverUtil.getStructureId(recordSet.getDdlStructureKey(), groupId,
                        DDLRecordSet.class),
                recordSet.getDdlStructureKey(), nameMap, descMap, MIN_DISPLAY_ROWS, 0,
                new ServiceContext());
        LOG.info("Added DDLRecordSet: " + newDDLRecordSet.getName());
    }

    public static void processRelatedAssets(final Article article, final JournalArticle ja,
                                            final long runAsUserId, final long groupId, final long companyId) {
        if (article.getRelatedAssets() != null) {
            RelatedAssets ras = article.getRelatedAssets();
            AssetEntry ae = null;
            if (ras.isClearAllAssets()) {

                try {
                    ae = AssetEntryLocalServiceUtil.getEntry(JournalArticle.class.getName(),
                            ja.getResourcePrimKey());
                    AssetLinkLocalServiceUtil.deleteLinks(ae.getEntryId());
                } catch (PortalException e) {
                    LOG.error("Problem clearing related assets of article " + ja.getArticleId(), e);
                } catch (SystemException e) {
                    LOG.error("Problem clearing related assets of article " + ja.getArticleId(), e);
                }
            }
            if (ras.getRelatedAsset() != null && ras.getRelatedAsset().size() > 0) {
                List<RelatedAsset> ra = ras.getRelatedAsset();
                for (RelatedAsset r : ra) {
                    String clazz = r.getAssetClass();
                    String clazzPrimKey = r.getAssetClassPrimaryKey();
                    String resolverHint = "Related asset for article " + ja.getArticleId() + " "
                            + "clazz " + clazz + ", " + "primary key " + clazzPrimKey;
                    clazzPrimKey = ResolverUtil.lookupAll(runAsUserId, groupId, companyId,
                            clazzPrimKey, resolverHint);

                    long id = 0;
                    try {
                        id = Long.parseLong(clazzPrimKey);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    try {

                        AssetEntry ae2 = AssetEntryLocalServiceUtil.getEntry(clazz, id);
                        AssetLinkLocalServiceUtil.addLink(runAsUserId, ae.getEntryId(),
                                ae2.getEntryId(), AssetLinkConstants.TYPE_RELATED, 1);
                        // JournalArticleLocalServiceUtil.getArticle(0).geta
                    } catch (PortalException e) {
                        LOG.error(
                                "Problem resolving related asset of article " + ja.getArticleId()
                                        + " with clazz " + clazz + " primary key " + clazzPrimKey,
                                e);
                    } catch (SystemException e) {
                        LOG.error(
                                "Problem resolving related asset of article " + ja.getArticleId()
                                        + " with clazz " + clazz + " primary key " + clazzPrimKey,
                                e);
                    }

                }
            }

        }
    }

}
