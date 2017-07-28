package com.mimacom.liferay.portal.setup.core.util;

/*-
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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.jboss.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class FileUtil {

    private static final Log LOG = LogFactoryUtil.getLog(FileUtil.class);

    private FileUtil() {
       // hide default constructor
    }

    /**
     * checks and returns file used for setup.
     *
     * @param fileName name of resource
     * @return setup xml file
     */
    public static final File getSetupFileFromClassloader(ClassLoader cl, final String fileName) {

        URL url = cl.getResource(fileName);
        if (url == null) {
            LOG.error("XML configuration not found");

            return null;
        }

        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            LOG.error("Problem with reading configuration xml", e);
            return null;
        }

        File file = null;
        if (uri.getScheme().equals("vfs")) {
            try {
                VirtualFile virtualFile = (VirtualFile) url.openConnection().getContent();
                file = virtualFile.getPhysicalFile();

            } catch (IOException e) {
                LOG.error("Couldn't open xml configuration", e);
                return null;
            }
        } else if (uri.getScheme().equals("file")) {
            file = new File(uri);
        }

        return file;
    }

}
