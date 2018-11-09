package com.ableneo.liferay.portal.setup.core.util;

/*
 * #%L
 * Liferay Portal DB Setup core
 * %%
 * Original work Copyright (C) 2016 - 2018 mimacom ag  * Modified work Copyright (C) 2018 - 2020 ableneo, s. r. o.
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

import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.service.ServiceContext;

import java.util.List;

public final class WebFolderUtil {

    private WebFolderUtil() {
    }

    public static JournalFolder findWebFolder(final long companyId, final long groupId,
                                              final long userId, final String name, final String description,
                                              final boolean createIfNotExists) {
        String[] folderPath = name.split("/");
        JournalFolder foundFolder = null;
        int count = 0;
        Long parentId = 0L;
        while (count < folderPath.length) {
            String folder = folderPath[count];
            if (!folder.equals("")) {
                foundFolder = findWebFolder(groupId, parentId, folder);

                if (foundFolder == null && createIfNotExists) {
                    foundFolder = createWebFolder(userId, companyId, groupId, parentId, folder,
                            description);
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

    public static JournalFolder findWebFolder(final Long groupId, final Long parentFolderId,
            final String name) {
        JournalFolder dir = null;
        List<JournalFolder> dirs;
        try {
            dirs = JournalFolderLocalServiceUtil.getFolders(groupId, parentFolderId);
            for (JournalFolder jf : dirs) {
                if (jf.getName() != null && jf.getName().equals(name)) {
                    dir = jf;
                    break;
                }
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return dir;
    }

    public static JournalFolder createWebFolder(final long userId, final long companyId,
            final long groupId, final long parentFolderId, final String name,
            final String description) {
        JournalFolder folder = null;
        try {
            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setScopeGroupId(groupId);
            serviceContext.setCompanyId(companyId);

            folder = JournalFolderLocalServiceUtil.addFolder(userId, groupId, parentFolderId, name,
                    description, serviceContext);

        } catch (PortalException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return folder;
    }
}
