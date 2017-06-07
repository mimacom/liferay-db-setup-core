package com.mimacom.liferay.portal.setup;

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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.mimacom.liferay.portal.setup.domain.ObjectFactory;
import com.mimacom.liferay.portal.setup.domain.Setup;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public final class MarshallUtil {
    private static final Log LOG = LogFactoryUtil.getLog(MarshallUtil.class);

    private MarshallUtil() {
    }

    public static Setup unmarshall(final File xmlFile) throws FileNotFoundException, JAXBException, ParserConfigurationException, SAXException {
        return MarshallUtil.unmarshall(new FileInputStream(xmlFile));
    }

    public static Setup unmarshall(final InputStream stream) throws JAXBException, ParserConfigurationException, SAXException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            //spf.setXIncludeAware(true);
            spf.setNamespaceAware(true);
            XMLReader xr = spf.newSAXParser().getXMLReader();
            /*
            EntityResolver entityResolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    InputStream resourceAsStream = MarshallUtil.class.getClassLoader().getResourceAsStream(systemId);
                    return new InputSource(resourceAsStream);
                }
            };
            xr.setEntityResolver(entityResolver);
            */
            SAXSource src = new SAXSource(xr, new InputSource(stream));
            return (Setup) getUnmarshaller().unmarshal(src);
        } catch (JAXBException | ParserConfigurationException | SAXException e) {
            LOG.error("Cannot unmarshall the provided stream", e);
            throw e;
        }
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {

        ClassLoader cl = ObjectFactory.class.getClassLoader();
        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), cl);
        return jc.createUnmarshaller();
    }

    public static boolean validateAgainstXSD(final InputStream xml) throws IOException {

        ClassLoader cl = MarshallUtil.class.getClassLoader();
        InputStream schemaInputStream = cl.getResourceAsStream("setup_definition-1.0.xsd");
        if (schemaInputStream == null) {
            throw new IOException("XSD configuration not found");
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(schemaInputStream));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));

            return true;

        } catch (Exception ex) {
            return false;
        }
    }
}
