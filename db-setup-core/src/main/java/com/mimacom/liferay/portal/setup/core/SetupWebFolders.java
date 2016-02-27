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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mimacom.liferay.portal.setup.domain.Organization;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.core.util.WebFolderUtil;
import com.mimacom.liferay.portal.setup.domain.ArticleFolder;

import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portlet.journal.model.JournalFolder;

public final class SetupWebFolders {
    private static final HashMap<String, List<String>> DEFAULT_PERMISSIONS;

    static {
        DEFAULT_PERMISSIONS = new HashMap<>();
        List<String> actionsOwner = new ArrayList<>();

        actionsOwner.add(ActionKeys.VIEW);
        actionsOwner.add(ActionKeys.UPDATE);
        actionsOwner.add(ActionKeys.PERMISSIONS);
        actionsOwner.add(ActionKeys.DELETE);
        actionsOwner.add(ActionKeys.ADD_SUBFOLDER);
        actionsOwner.add(ActionKeys.ADD_ARTICLE);
        actionsOwner.add(ActionKeys.ACCESS);

        DEFAULT_PERMISSIONS.put(RoleConstants.OWNER, actionsOwner);

        List<String> actionsUser = new ArrayList<>();
        actionsUser.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.USER, actionsUser);

        List<String> actionsGuest = new ArrayList<>();
        actionsGuest.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.GUEST, actionsGuest);
    }

    private SetupWebFolders() {

    }

    public static void setupWebFolders(
        final Organization org, final long groupId,
        final long companyId) {
        for (ArticleFolder af : org.getArticleFolder()) {
            String webFolderPath = af.getFolderPath();
            String description = af.getDescription();
            JournalFolder jf = WebFolderUtil.findWebFolder(companyId, groupId,
                    LiferaySetup.getRunAsUserId(), webFolderPath, description, true);
            SetupPermissions.updatePermission("Folder " + af.getFolderPath(), groupId, companyId,
                    jf.getFolderId(), JournalFolder.class, af.getRolePermissions(),
                    DEFAULT_PERMISSIONS);
        }
    }

}
