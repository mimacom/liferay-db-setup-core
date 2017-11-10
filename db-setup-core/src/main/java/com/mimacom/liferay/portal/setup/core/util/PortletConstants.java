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

import com.liferay.exportimport.kernel.staging.StagingConstants;

/**
 * Created by gustavnovotny on 12.09.17.
 */
public class PortletConstants {

    public static final String STAGING_PARAM_TEMPLATE = StagingConstants.STAGED_PREFIX + StagingConstants.STAGED_PORTLET + "#" + "--";
    public static final String STAGING_PORTLET_ID_ADT = "com_liferay_dynamic_data_mapping_web_portlet_PortletDisplayTemplatePortlet";
    public static final String STAGING_PORTLET_ID_BLOGS = "com_liferay_blogs_web_portlet_BlogsPortlet";
    public static final String STAGING_PORTLET_ID_BOOKMARKS = "com_liferay_bookmarks_web_portlet_BookmarksPortlet";
    public static final String STAGING_PORTLET_ID_CALENDAR = "com_liferay_calendar_web_portlet_CalendarPortlet";
    public static final String STAGING_PORTLET_ID_DDL = "com_liferay_dynamic_data_lists_web_portlet_DDLPortlet";
    public static final String STAGING_PORTLET_ID_DL = "com_liferay_document_library_web_portlet_DLPortlet";
    public static final String STAGING_PORTLET_ID_FORMS = "com_liferay_dynamic_data_lists_form_web_portlet_DDLFormAdminPortlet";
    public static final String STAGING_PORTLET_ID_MB = "com_liferay_message_boards_web_portlet_MBAdminPortlet";
    public static final String STAGING_PORTLET_ID_MDR = "com_liferay_mobile_device_rules_web_portlet_MDRPortlet";
    public static final String STAGING_PORTLET_ID_POLLS = "com_liferay_polls_web_portlet_PollsPortlet";
    public static final String STAGING_PORTLET_ID_WEB_CONTENT = "com_liferay_journal_web_portlet_JournalPortlet";
    public static final String STAGING_PORTLET_ID_WIKI = "com_liferay_wiki_web_portlet_WikiPortlet";
}
