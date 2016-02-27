package com.mimacom.liferay.portal.setup.core.util;

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


import java.util.List;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.asset.NoSuchTagException;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;

public final class TaggingUtil {
    private static final Log LOG = LogFactoryUtil.getLog(TaggingUtil.class);

    private TaggingUtil() {
    }

    public static void associateTagsWithJournalArticle(final List<String> tags,
            final List<String> categories, final long userId, final long groupId,
            final long primaryKey, final Class className) {

        try {
            long[] catIds = new long[0];
            if (categories != null) {
                catIds = getCategories(categories, groupId, userId);
            }
            AssetEntryLocalServiceUtil.updateEntry(userId, groupId, JournalArticle.class.getName(),
                    primaryKey, catIds, tags.toArray(new String[tags.size()]));
        } catch (PortalException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        }
    }

    public static long[] getCategories(final List<String> categories, final long groupId,
            final long runAsUser) {
        // The categories and tags to assign
        final long[] assetCategoryIds = new long[categories.size()];
        final String[] tagProperties = new String[0]; // Might be null too

        for (int i = 0; i < categories.size(); ++i) {
            final String name = categories.get(i);

            AssetTag assetTag = null;
            try {
                assetTag = AssetTagLocalServiceUtil.getTag(groupId, name);
            } catch (final NoSuchTagException e) {
                try {
                    assetTag = AssetTagLocalServiceUtil.addTag(runAsUser, name, tagProperties,
                            new ServiceContext());
                } catch (PortalException | SystemException e1) {
                    LOG.error("Category " + name + " not found! ", e1);
                }
            } catch (PortalException e) {
                LOG.error("Category " + name + " not found! ", e);
            } catch (SystemException e) {
                LOG.error("Category " + name + " not found! ", e);
            }

            assetCategoryIds[i] = assetTag.getTagId();
        }
        return assetCategoryIds;
    }
}
