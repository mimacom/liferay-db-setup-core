package com.mimacom.liferay.portal.setup.core.util;

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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.mimacom.liferay.portal.setup.domain.TitleTranslation;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public final class TitleMapUtil {
    private static final Log LOG = LogFactoryUtil.getLog(TitleMapUtil.class);

    private TitleMapUtil() {
    }

    public static Map<Locale, String> getTitleMap(final List<TitleTranslation> translations,
                                                  final long groupId, final String defaultLocaleTitle, final String locationHint) {
        Map<Locale, String> titleMap = new HashMap<>();
        Locale siteDefaultLocale = getDefaultLocale(groupId, locationHint);

        titleMap.put(siteDefaultLocale, defaultLocaleTitle);
        if (translations != null) {
            for (TitleTranslation tt : translations) {
                try {
                    String[] s = tt.getLocale().split("_");

                    Locale l = null;
                    if (s.length > 1) {
                        l = new Locale(s[0], s[1]);
                    } else {
                        l = new Locale(s[0]);
                    }
                    titleMap.put(l, tt.getTitleText());
                } catch (Exception ex) {
                    LOG.error("Exception while retrieving locale " + tt.getLocale() + " for "
                            + locationHint);
                }
            }
        }
        return titleMap;
    }

    public static Locale getDefaultLocale(final long groupId, final String locationHint) {
        Locale siteDefaultLocale = null;
        try {
            siteDefaultLocale = PortalUtil.getSiteDefaultLocale(groupId);
        } catch (PortalException | SystemException e) {
            LOG.error("Error Reading Locale while for " + locationHint);
        }
        return siteDefaultLocale;
    }

    public static String getXMLTitleStructure(final Map<Locale, String> titles,
            final Locale defaultLocale) {
        Set<Locale> locales = titles.keySet();

        String xmlTitleStructure = "";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = factory.createXMLStreamWriter(sw);

            writer.writeStartDocument();
            writer.writeStartElement("root");
            String langs = StringUtil.merge(locales, ",");

            /*
             * TODO remove boolean first = true; for (Locale l : locales) {
             * langs += ((!first) ? "," : "") + l.toString(); first = false; }
             */

            writer.writeAttribute("default-locale", defaultLocale.toString());
            writer.writeAttribute("available-locales", langs);

            for (Locale l : locales) {
                String title = titles.get(l);
                writer.writeStartElement("Title");
                writer.writeAttribute("language-id", l.toString());
                writer.writeCharacters(title);
                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();
            xmlTitleStructure = sw.toString();
            sw.close();
        } catch (XMLStreamException | IOException e) {
            LOG.error("Problem when creating title structure for the following internationalized "
                    + "titles: " + titles + "", e);
        }
        return xmlTitleStructure;
    }

    public static Map<Locale, String> getLocalizationMap(final String value) {
        Map<Locale, String> map = new HashMap<>();

        map.put(LocaleUtil.getDefault(), value);

        return map;
    }
}
