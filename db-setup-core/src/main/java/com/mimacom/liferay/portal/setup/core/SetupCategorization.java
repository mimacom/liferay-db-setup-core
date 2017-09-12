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
package com.mimacom.liferay.portal.setup.core;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.PortalUtil;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.domain.Category;
import com.mimacom.liferay.portal.setup.domain.Organization;
import com.mimacom.liferay.portal.setup.domain.Site;
import com.mimacom.liferay.portal.setup.domain.Vocabulary;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Setup module for creating / updating the categorization. So far it creates
 * tree of categories. In the future also AssetTag creation feature should be
 * here.
 * <p/>
 * Created by guno on 8. 6. 2015.
 */
public final class SetupCategorization {
    private static final Log LOG = LogFactoryUtil.getLog(SetupArticles.class);

    private SetupCategorization() {

    }

    public static void setupVocabularies(final Site site, final long groupId)
            throws SystemException, PortalException {
        List<Vocabulary> vocabularies = site.getVocabulary();

        Locale siteDefaultLocale = PortalUtil.getSiteDefaultLocale(groupId);

        LOG.info("Setting up vocabularies");

        for (Vocabulary vocabulary : vocabularies) {
            setupVocabulary(vocabulary, site, groupId, siteDefaultLocale);
        }
    }

    private static void setupVocabulary(final Vocabulary vocabulary,
                                        final Site site, final long groupId, final Locale defaultLocale) {

        LOG.info("Setting up vocabulary with title: " + vocabulary.getTitle());

        Map<Locale, String> titleMap = new HashMap<>();
        String title = vocabulary.getTitle();
        titleMap.put(defaultLocale, title);

        Map<Locale, String> descMap = new HashMap<>();
        descMap.put(defaultLocale, vocabulary.getDescription());

        AssetVocabulary assetVocabulary = null;
        try {
            assetVocabulary = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, title);
        } catch (PortalException | SystemException e) {
            LOG.error("Asset vocabulary was not found");
        }

        if (assetVocabulary != null) {
            LOG.debug("Vocabulary already exists. Will be updated.");
            assetVocabulary.setTitleMap(titleMap);
            assetVocabulary.setDescriptionMap(descMap);
            try {
                assetVocabulary = AssetVocabularyLocalServiceUtil
                        .updateAssetVocabulary(assetVocabulary);
                LOG.debug("Vocabulary successfully updated.");
            } catch (SystemException e) {
                LOG.info("Error while trying to update AssetVocabulary with ID:"
                        + assetVocabulary.getVocabularyId() + ". Skipping.");
                return;
            }

            setupCategories(assetVocabulary.getVocabularyId(), groupId, 0L,
                    vocabulary.getCategory(), defaultLocale);
            return;
        }

        try {
            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setCompanyId(PortalUtil.getDefaultCompanyId());
            serviceContext.setScopeGroupId(groupId);
            assetVocabulary = AssetVocabularyLocalServiceUtil.addVocabulary(
                    LiferaySetup.getRunAsUserId(), groupId, null, titleMap, descMap, null, serviceContext);
            LOG.info("AssetVocabulary successfuly added. ID:" + assetVocabulary.getVocabularyId()
                    + ", group:" + assetVocabulary.getGroupId());
            setupCategories(assetVocabulary.getVocabularyId(), groupId, 0L,
                    vocabulary.getCategory(), defaultLocale);
        } catch (PortalException | SystemException | NullPointerException e) {
            LOG.error("Error while trying to create vocabulary with title: "
                    + assetVocabulary.getTitle(), e);
        }
    }

    private static void setupCategories(final long vocabularyId, final long groupId,
                                        final long parentId, final List<Category> categories, final Locale defaultLocale) {
        LOG.info("Setting up categories for parentId:" + parentId);

        if (categories != null && !categories.isEmpty()) {
            for (Category category : categories) {
                setupCategory(category, vocabularyId, groupId, defaultLocale, parentId);
            }
        }
    }

    private static void setupCategory(final Category category, final long vocabularyId,
                                      final long groupId, final Locale defaultLocale, final long parentCategoryId) {

        LOG.info("Setting up category with title:" + category.getTitle());

        Map<Locale, String> titleMap = new HashMap<>();
        String title = category.getTitle();
        titleMap.put(defaultLocale, title);

        Map<Locale, String> descMap = new HashMap<>();
        String description = category.getDescription();
        descMap.put(defaultLocale, description);

        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setCompanyId(PortalUtil.getDefaultCompanyId());
        serviceContext.setScopeGroupId(groupId);

        AssetCategory assetCategory = null;

        try {
            String[] properties = {};
            List<AssetCategory> existingCategories = AssetCategoryLocalServiceUtil
                    .getChildCategories(parentCategoryId);
            for (AssetCategory ac : existingCategories) {
                if (ac.getName().equals(category.getTitle())) {
                    assetCategory = ac;
                }
            }
        } catch (SystemException e) {
            LOG.error("Error while trying to find category with name: " + category.getTitle(), e);
        }

        if (assetCategory != null) {
            LOG.error("Asset category already exists for parent category. Updating...");

            assetCategory.setTitleMap(titleMap);
            assetCategory.setDescriptionMap(descMap);
            assetCategory.setName(title);
            try {
                AssetCategoryLocalServiceUtil.updateAssetCategory(assetCategory);
                LOG.info("Category successfully updated.");
            } catch (SystemException e) {
                LOG.error("Error while trying to update category with name: "
                        + assetCategory.getName(), e);
            }

            setupCategories(vocabularyId, groupId, assetCategory.getCategoryId(),
                    category.getCategory(), defaultLocale);
            return;
        }

        try {
            assetCategory = AssetCategoryLocalServiceUtil.addCategory(LiferaySetup.getRunAsUserId(), groupId,
                    parentCategoryId, titleMap, descMap, vocabularyId, null, serviceContext);
            LOG.info("Category successfully added with title: " + assetCategory.getTitle());

            setupCategories(vocabularyId, groupId, assetCategory.getCategoryId(),
                    category.getCategory(), defaultLocale);

        } catch (PortalException | SystemException e) {
            LOG.error("Error in creating category with title: " + category.getTitle(), e);
        }

    }
}
