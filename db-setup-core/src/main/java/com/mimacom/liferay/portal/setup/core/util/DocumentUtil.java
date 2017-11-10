package com.mimacom.liferay.portal.setup.core.util;

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

import com.liferay.document.library.kernel.exception.NoSuchFileEntryException;
import com.liferay.document.library.kernel.service.DLAppHelperLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;

/**
 * This utility allows to manage documents of the documents and media library.
 *
 * @author msi
 */
public final class DocumentUtil {

    private static final Log LOG = LogFactoryUtil.getLog(DocumentUtil.class);

    private DocumentUtil() {

    }

    /**
     * Tries to retrieve the file entry of a document.
     *
     * @param documentName The name of the document (i.e., title).
     * @param folderPath   the folder path to look for (where the document is stored).
     * @param groupId      the id of the group to which this document belongs to.
     * @param company      the company name.
     * @param repoId       the id of the repository where the file is stored.
     * @param userId       the user is of the importing user (not used in this case, as
     *                     no folder is automatically created with this operation).
     * @return Returns the file entry of the specified document.
     */
    public static FileEntry findDocument(final String documentName, final String folderPath,
                                         final long groupId, final long company, final long repoId, final long userId) {
        Folder folder = FolderUtil.findFolder(company, groupId, repoId, userId, folderPath, false);
        FileEntry entry = null;
        if (folder != null) {
            try {
                entry = DLAppLocalServiceUtil.getFileEntry(groupId, folder.getFolderId(),
                        documentName);
            } catch (NoSuchFileEntryException e) {
                LOG.info("Document not found: " + documentName);
            } catch (PortalException e) {
                LOG.error("Error while trying to find document: " + documentName);
            } catch (SystemException e) {
                LOG.error("Error while trying to find document: " + documentName);
            }
        }

        return entry;
    }

    /**
     * Finds a document by looking it up with a given document name (title) for
     * a given group of a given company.
     *
     * @param documentName The name of the document (title)
     * @param groupId      the group id.
     * @param company      the id of the company.
     * @param repoId       the repository id.
     * @param userId       the id of the user executing this operation.
     * @return
     */
    public static FileEntry findDocument(final String documentName, final long groupId,
                                         final long company, final long repoId, final long userId) {
        //
        String title = FilePathUtil.getFileName(documentName);
        String getPath = FilePathUtil.getPath(documentName);
        Folder folder = FolderUtil.findFolder(company, groupId, repoId, userId, getPath, false);
        FileEntry entry = null;
        if (folder != null) {
            try {
                entry = DLAppLocalServiceUtil.getFileEntry(groupId, folder.getFolderId(), title);
            } catch (PortalException e) {
                e.printStackTrace();
            } catch (SystemException e) {
                e.printStackTrace();
            }
        }

        return entry;
    }

    /**
     * Updates a given file entry with a given content.
     *
     * @param fe             The file entry to be updated with a given content.
     * @param content        The content to be updated.
     * @param userId         The user id of the updating user.
     * @param sourceFileName The filename of the file.
     */
    public static void updateFile(final FileEntry fe, final byte[] content, final long userId,
                                  final String sourceFileName) {
        try {
            DLAppLocalServiceUtil.updateFileEntry(userId, fe.getFileEntryId(), sourceFileName,
                    fe.getMimeType(), fe.getTitle(), fe.getDescription(), "update content", true,
                    content, new ServiceContext());
        } catch (Exception e) {
            LOG.error("Can not update Liferay Document entry with ID:" + fe.getFileEntryId(), e);
        }
    }

    /**
     * Moves the given file entry to a folder with a given id.
     *
     * @param fe       the file entry.
     * @param folderId the id of the folder where the file will be moved to.
     * @param userId   the user id of the user who executes the moving of the file.
     */
    public static void moveFile(final FileEntry fe, final long folderId, final long userId) {
        try {
            DLAppLocalServiceUtil.moveFileEntry(userId, fe.getFolderId(), folderId,
                    new ServiceContext());
        } catch (PortalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a document that will be located at a particular folder.
     *
     * @param companyId the company id to which the file will be associated.
     * @param groupId   the group id to which the file will be associated.
     * @param folderId  the folder id where the file will be located in documents and
     *                  media
     * @param fileName  the name of the file.
     * @param title     the title of the file (will be used to access the file).
     * @param userId    the id of the user who is importing the content.
     * @param repoId    the id of the repository.
     * @param content   the content of the file file to be stored.
     * @return returns the file entry of the created file.
     */
    // CHECKSTYLE:OFF
    public static FileEntry createDocument(final long companyId, final long groupId,
                                           final long folderId, final String fileName, final String title, final long userId,
                                           final long repoId, final byte[] content) {
        String fname = FilePathUtil.getFileName(fileName);
        String extension = FilePathUtil.getExtension(fname);
        String mtype = MimeTypeMapper.getInstance().getMimeType(extension);
        FileEntry fileEntry = null;
        try {
            fileEntry = DLAppLocalServiceUtil.getFileEntry(groupId, folderId, title);
        } catch (NoSuchFileEntryException nsfee) {
            LOG.info("Document not found: " + title);
        } catch (PortalException e) {
            LOG.error("Error while trying to get file: " + title, e);
        } catch (SystemException e) {
            LOG.error("Error while trying to get file: " + title, e);
        }
        if (fileEntry == null) {
            try {
                fileEntry = DLAppLocalServiceUtil.addFileEntry(userId, repoId, folderId, fname,
                        mtype, title, title, "Mimacom import", content, new ServiceContext());
            } catch (PortalException e) {
                LOG.error("Error while trying to add file entry: " + title, e);
            } catch (SystemException e) {
                LOG.error("Error while trying to add file entry: " + title, e);
            }

        }
        return fileEntry;
    }
    // CHECKSTYLE:ON
}
