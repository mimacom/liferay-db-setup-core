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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mimacom.liferay.portal.setup.core.util.FolderUtil;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.core.util.DocumentUtil;
import com.mimacom.liferay.portal.setup.domain.Document;
import com.mimacom.liferay.portal.setup.domain.Organization;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;

public final class SetupDocuments {

    private static final Log LOG = LogFactoryUtil.getLog(SetupDocuments.class);
    private static final HashMap<String, List<String>> DEFAULT_PERMISSIONS;
    private static final int BUFFER_SIZE = 2048;

    static {
        DEFAULT_PERMISSIONS = new HashMap<String, List<String>>();
        List<String> actionsOwner = new ArrayList<String>();

        actionsOwner.add(ActionKeys.ADD_DISCUSSION);
        actionsOwner.add(ActionKeys.DELETE);
        actionsOwner.add(ActionKeys.DELETE_DISCUSSION);
        actionsOwner.add(ActionKeys.OVERRIDE_CHECKOUT);
        actionsOwner.add(ActionKeys.PERMISSIONS);
        actionsOwner.add(ActionKeys.UPDATE);
        actionsOwner.add(ActionKeys.UPDATE_DISCUSSION);
        actionsOwner.add(ActionKeys.VIEW);

        DEFAULT_PERMISSIONS.put(RoleConstants.OWNER, actionsOwner);

        List<String> actionsUser = new ArrayList<String>();
        actionsUser.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.USER, actionsUser);

        List<String> actionsGuest = new ArrayList<String>();
        actionsGuest.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS.put(RoleConstants.GUEST, actionsGuest);
    }

    private SetupDocuments() {

    }

    public static void setupOrganizationDocuments(final Organization organization,
                                                  final long groupId, final long company, long runAsUserId) {
        for (Document doc : organization.getDocument()) {
            String folderPath = doc.getDocumentFolderName();
            String documentName = doc.getDocumentFilename();
            String documentTitle = doc.getDocumentTitle();
            String extension = doc.getExtension();
            String filenameInFilesystem = doc.getFileSystemName();
            long repoId = groupId;
            Long folderId = 0L;
            Folder f = null;
            if (folderPath != null && !folderPath.equals("")) {
                f = FolderUtil.findFolder(company, groupId, repoId, runAsUserId, folderPath, true);
                folderId = f.getFolderId();
            }
            FileEntry fe = DocumentUtil.findDocument(documentName, folderPath, groupId, company,
                    groupId, runAsUserId);
            if (fe == null) {

                fe = DocumentUtil.createDocument(company, groupId, folderId, documentName,
                        documentTitle, runAsUserId, repoId, getDocumentContent(filenameInFilesystem));
                LOG.info(documentName + " is not found! It will be created! ");
            } else {
                LOG.info(documentName + " is found! Content will be updated! ");
                DocumentUtil.updateFile(fe, getDocumentContent(filenameInFilesystem), runAsUserId,
                        documentName);
            }
            SetupPermissions.updatePermission("Document " + folderPath + "/" + documentName,
                    groupId, company, fe.getFileEntryId(), DLFileEntry.class,
                    doc.getRolePermissions(), DEFAULT_PERMISSIONS);
        }
    }

    public static byte[] getDocumentContent(final String filesystemPath) {
        byte[] content = new byte[0];
        InputStream is = SetupDocuments.class.getClassLoader().getResourceAsStream(filesystemPath);
        byte[] buf = new byte[BUFFER_SIZE];
        int readBytes = 0;
        if (is != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                while ((readBytes = is.read(buf)) != -1) {
                    baos.write(buf, 0, readBytes);
                }
                content = baos.toByteArray();
            } catch (IOException e) {
                LOG.error(e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        } else {
            // error file not found
            LOG.error(filesystemPath + ": document in file system not found! Error in XML file! ");
        }
        return content;
    }
}
