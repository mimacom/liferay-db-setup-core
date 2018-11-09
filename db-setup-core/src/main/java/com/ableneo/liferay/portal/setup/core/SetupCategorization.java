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
package com.ableneo.liferay.portal.setup.core;

import com.ableneo.liferay.portal.setup.LiferaySetup;
import com.ableneo.liferay.portal.setup.core.util.TitleMapUtil;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryConstants;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portlet.asset.util.AssetVocabularySettingsHelper;
import com.ableneo.liferay.portal.setup.core.util.ResolverUtil;
import com.mimacom.liferay.portal.setup.domain.AssociatedAssetType;
import com.mimacom.liferay.portal.setup.domain.Category;
import com.mimacom.liferay.portal.setup.domain.Site;
import com.mimacom.liferay.portal.setup.domain.Vocabulary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


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

    private static void setupVocabulary(final Vocabulary vocabulary, final Site site, final long groupId, final Locale defaultLocale) {

        LOG.info("Setting up vocabulary with name: " + vocabulary.getName());

        Map<Locale, String> titleMap = TitleMapUtil.getTitleMap(vocabulary.getTitleTranslation(), groupId, vocabulary.getName(), "");

        Map<Locale, String> descMap = new HashMap<>();
        descMap.put(defaultLocale, vocabulary.getDescription());

        AssetVocabulary assetVocabulary = null;
        try {
            assetVocabulary = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, vocabulary.getName());
        } catch (PortalException | SystemException e) {
            LOG.error("Asset vocabulary was not found");
        }

        if (assetVocabulary != null) {
            LOG.debug("Vocabulary already exists. Will be updated.");

            assetVocabulary.setName(vocabulary.getName());
            assetVocabulary.setTitleMap(titleMap);
            assetVocabulary.setDescriptionMap(descMap);
            assetVocabulary.setSettings(composeVocabularySettings(vocabulary, groupId));

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
                    LiferaySetup.getRunAsUserId(), groupId, null, titleMap, descMap, composeVocabularySettings(vocabulary, groupId), serviceContext);
            LOG.info("AssetVocabulary successfuly added. ID:" + assetVocabulary.getVocabularyId()
                    + ", group:" + assetVocabulary.getGroupId());
            setupCategories(assetVocabulary.getVocabularyId(), groupId, 0L,
                    vocabulary.getCategory(), defaultLocale);
        } catch (PortalException | SystemException | NullPointerException e) {
            LOG.error("Error while trying to create vocabulary with title: "
                    + assetVocabulary.getTitle(), e);
        }
    }

    private static String composeVocabularySettings(Vocabulary vocabulary, final long groupId) {
        AssetVocabularySettingsHelper assetVocabularySettingsHelper = new AssetVocabularySettingsHelper();
        assetVocabularySettingsHelper.setMultiValued(vocabulary.isMultiValued());
        List<AssociatedAssetType> types = vocabulary.getAssociatedAssetType();

        if (Objects.isNull(types) || types.isEmpty()) {
            assetVocabularySettingsHelper.setClassNameIdsAndClassTypePKs(new long[] {AssetCategoryConstants.ALL_CLASS_NAME_ID},
                new long[] {AssetCategoryConstants.ALL_CLASS_TYPE_PK}, new boolean[] {false});
            return assetVocabularySettingsHelper.toString();
        }

        List<Long> classNameIds = new LinkedList<>();
        List<Long> classTypePKs = new LinkedList<>();
        List<Boolean> requireds = new LinkedList<>();

        for (AssociatedAssetType type : types) {
            ClassName className = ClassNameLocalServiceUtil.fetchClassName(type.getClassName());
            if (className.getValue().isEmpty()) {
                continue;
            }

            long subtypePK = -1;
            if ( Objects.nonNull(type.getSubtypeStructureKey()) && !type.getSubtypeStructureKey().isEmpty()) { // has subtype
                try {
                    subtypePK = ResolverUtil.getStructureId(type.getSubtypeStructureKey(), groupId, Class.forName(type.getClassName()), true);
                } catch (ClassNotFoundException | PortalException e) {
                    LOG.error("Class can not be be resolved for classname: " + type.getClassName(), e);
                    continue;
                }
            }

            classNameIds.add(className.getClassNameId());
            classTypePKs.add(subtypePK);
            requireds.add(type.isRequired());
        }

        // no valid associated types case
        if (classNameIds.isEmpty()) {
            assetVocabularySettingsHelper.setClassNameIdsAndClassTypePKs(new long[] {AssetCategoryConstants.ALL_CLASS_NAME_ID},
                new long[] {AssetCategoryConstants.ALL_CLASS_TYPE_PK}, new boolean[] {false});
            return assetVocabularySettingsHelper.toString();
        }


        // when associated types exists
        boolean[] requiredsArray = new boolean[requireds.size()];
        for (int i = 0; i < requireds.size(); i++) {
            requiredsArray[i] = requireds.get(i);
        }

        assetVocabularySettingsHelper.setClassNameIdsAndClassTypePKs(ArrayUtil.toLongArray(classNameIds), ArrayUtil.toLongArray(classTypePKs), requiredsArray);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Vocabulary settings composed for vocabulary:" + vocabulary.getName() + ". Content: " + assetVocabularySettingsHelper.toString());
        }

        return assetVocabularySettingsHelper.toString();
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

        LOG.info("Setting up category with name:" + category.getName());

        Map<Locale, String> titleMap = TitleMapUtil.getTitleMap(category.getTitleTranslation(), groupId, category.getName(),
            "Category with name: " + category.getName());
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
                if (ac.getName().equals(category.getName())) {
                    assetCategory = ac;
                }
            }
        } catch (SystemException e) {
            LOG.error("Error while trying to find category with name: " + category.getName(), e);
        }

        if (assetCategory != null) {
            LOG.error("Asset category already exists for parent category. Updating...");

            assetCategory.setTitleMap(titleMap);
            assetCategory.setDescriptionMap(descMap);
            assetCategory.setName(category.getName());

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
            LOG.error("Error in creating category with name: " + category.getName(), e);
        }

    }
}
