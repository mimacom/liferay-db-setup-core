package com.ableneo.liferay.portal.setup.core.util;

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

import com.ableneo.liferay.portal.setup.core.SetupPermissions;
import com.ableneo.liferay.portal.setup.core.SetupWebFolders;
import com.liferay.document.library.kernel.exception.NoSuchFolderException;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.journal.model.JournalFolder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ServiceContext;

public final class FolderUtil {

    private static final Log LOG = LogFactoryUtil.getLog(FolderUtil.class);

    private FolderUtil() {

    }

    public static Folder findFolder(final long company, final long groupId, final long repoId,
                                    final long userId, final String name, final boolean createIfNotExists) {
        String[] folderPath = name.split("/");
        Folder foundFolder = null;
        int count = 0;
        Long parentId = 0L;
        while (count < folderPath.length) {
            String folder = folderPath[count];
            if (!folder.equals("")) {
                foundFolder = findFolder(groupId, parentId, folder);

                if (foundFolder == null && createIfNotExists) {
                    foundFolder = createDocumentFolder(company, groupId, repoId, userId, parentId,
                            folder);
                    SetupPermissions.updatePermission(
                            "Folder " + name + ", creating folder " + "segment " + folder, groupId,
                            company, foundFolder.getFolderId(), JournalFolder.class, null,
                            SetupWebFolders.DEFAULT_PERMISSIONS);
                }

                if (foundFolder == null) {
                    break;
                }
                parentId = foundFolder.getFolderId();
            }
            count++;
        }
        return foundFolder;
    }

    public static Folder findFolder(final Long groupId, final Long parentFolderId,
                                    final String name) {
        Folder dir = null;
        try {
            dir = DLAppLocalServiceUtil.getFolder(groupId, parentFolderId, name);
        } catch (NoSuchFolderException nsfe) {
            LOG.info("Folder not found: " + name);
        } catch (PortalException e) {
            LOG.error("Error while trying to get folder: " + name, e);
        } catch (SystemException e) {
            LOG.error("Error while trying to get folder: " + name, e);
        }
        return dir;
    }

    public static Folder createDocumentFolder(final long companyId, final long groupId,
                                              final long repoId, final long userId, final Long pFolderId, final String folderName) {

        Long currentFolderId = null;
        Folder folder = null;

        // we currently dont have a folder for this naviagtion point, create one
        if (currentFolderId == null) {

            try {
                folder = findFolder(groupId, pFolderId, folderName);
                if (folder == null) {
                    folder = DLAppLocalServiceUtil.addFolder(userId, repoId, pFolderId, folderName,
                            folderName, new ServiceContext());

                }
            } catch (SystemException | PortalException e) {
                e.printStackTrace();
            }

        }
        return folder;
    }

}
