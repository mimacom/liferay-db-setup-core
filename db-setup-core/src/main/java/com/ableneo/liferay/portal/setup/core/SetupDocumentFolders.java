package com.ableneo.liferay.portal.setup.core;

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


import com.ableneo.liferay.portal.setup.LiferaySetup;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.ableneo.liferay.portal.setup.core.util.FolderUtil;
import com.mimacom.liferay.portal.setup.domain.DocumentFolder;
import com.mimacom.liferay.portal.setup.domain.Site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class SetupDocumentFolders {
    public static final HashMap<String, List<String>> DEFAULT_PERMISSIONS;

    static {
        DEFAULT_PERMISSIONS = new HashMap<>();
        List<String> actionsOwner = new ArrayList<>();

        actionsOwner.add(ActionKeys.VIEW);
        actionsOwner.add(ActionKeys.UPDATE);
        actionsOwner.add(ActionKeys.PERMISSIONS);
        actionsOwner.add(ActionKeys.DELETE);
        actionsOwner.add(ActionKeys.ADD_SUBFOLDER);
        actionsOwner.add(ActionKeys.ADD_SHORTCUT);
        actionsOwner.add(ActionKeys.ADD_DOCUMENT);
        actionsOwner.add(ActionKeys.ACCESS);
        actionsOwner.add(ActionKeys.SUBSCRIBE);

        DEFAULT_PERMISSIONS.put(RoleConstants.OWNER, actionsOwner);

        List<String> actionsUser = new ArrayList<>();
        actionsUser.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.USER, actionsUser);

        List<String> actionsGuest = new ArrayList<>();
        actionsGuest.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.GUEST, actionsGuest);
    }

    private SetupDocumentFolders() {

    }

    public static void setupDocumentFolders(final Site group, final long groupId, final long companyId) {
        for (DocumentFolder df : group.getDocumentFolder()) {
            boolean create = df.isCreateIfNotExists();
            String folderName = df.getFolderName();

            Folder folder = FolderUtil.findFolder(companyId, groupId, groupId, LiferaySetup.getRunAsUserId(), folderName, create);
            SetupPermissions.updatePermission("Document folder " + folderName, groupId, companyId,
                    folder.getFolderId(), DLFolder.class, df.getRolePermissions(),
                    DEFAULT_PERMISSIONS);
        }
    }
}
