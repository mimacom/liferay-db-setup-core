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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import com.mimacom.liferay.portal.setup.domain.Setup;
import org.jboss.vfs.VirtualFile;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public final class MarshallUtil {
    private static final Log LOG = LogFactoryUtil.getLog(MarshallUtil.class);

    private MarshallUtil() {
    }

    public static Setup unmarshall(final File xmlFile) {
        try {
            return (Setup) getUnmarshaller().unmarshal(xmlFile);
        } catch (JAXBException e) {
            LOG.error("cannot unmarshall", e);
            throw new IllegalArgumentException("cannot unmarshallFile");
        }
    }

    public static Setup unmarshall(final InputStream stream) {
        try {
            return (Setup) getUnmarshaller().unmarshal(stream);
        } catch (JAXBException e) {
            LOG.error("cannot unmarshall", e);
            throw new IllegalArgumentException("cannot unmarshallFile");
        }
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("com.mimacom.liferay.portal.setup.domain");
        return jc.createUnmarshaller();
    }

    public static boolean validateAgainstXSD(final InputStream xml) throws IOException {
        ClassLoader cl = MarshallUtil.class.getClassLoader();
        URL url = cl.getResource("setup_definition-2.0.xsd");
        if (url == null) {
            throw new IOException("XSD configuration not found");
        }
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Problem with reading xsd", e);
        }

        File file = null;
        if (uri.getScheme().equals("vfs")) {
            VirtualFile virtualFile = (VirtualFile) url.openConnection().getContent();
            file = virtualFile.getPhysicalFile();

        } else if (uri.getScheme().equals("file")) {
            file = new File(uri);
        } else {
            throw new IOException("Problem with reading xsd");
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(file));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));

            return true;

        } catch (Exception ex) {
            return false;
        }
    }
}
