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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.mimacom.liferay.portal.setup.core.util.CustomFieldSettingUtil;
import com.mimacom.liferay.portal.setup.core.util.ResolverUtil;
import com.mimacom.liferay.portal.setup.LiferaySetup;
import com.mimacom.liferay.portal.setup.core.util.TitleMapUtil;
import com.mimacom.liferay.portal.setup.domain.CustomFieldSetting;
import com.mimacom.liferay.portal.setup.domain.Organization;
import com.mimacom.liferay.portal.setup.domain.Page;
import com.mimacom.liferay.portal.setup.domain.PageTemplate;
import com.mimacom.liferay.portal.setup.domain.PageTemplates;
import com.mimacom.liferay.portal.setup.domain.Pageportlet;
import com.mimacom.liferay.portal.setup.domain.PortletPreference;
import com.mimacom.liferay.portal.setup.domain.PrivatePages;
import com.mimacom.liferay.portal.setup.domain.PublicPages;
import com.mimacom.liferay.portal.setup.domain.Theme;

import com.liferay.portal.NoSuchLayoutException;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.LayoutPrototype;
import com.liferay.portal.model.LayoutSet;
import com.liferay.portal.model.LayoutTemplate;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.LayoutPrototypeLocalServiceUtil;
import com.liferay.portal.service.LayoutServiceUtil;
import com.liferay.portal.service.LayoutSetLocalServiceUtil;
import com.liferay.portal.service.LayoutTemplateLocalServiceUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.service.ResourceLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.permission.PortletPermissionUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.PortletPreferencesFactoryUtil;

public final class SetupPages {
    private static final Log LOG = LogFactoryUtil.getLog(SetupPages.class);
    private static final HashMap<String, List<String>> DEFAULT_PERMISSIONS_PUBLIC;
    private static final HashMap<String, List<String>> DEFAULT_PERMISSIONS_PRIVATE;

    static {
        DEFAULT_PERMISSIONS_PUBLIC = new HashMap<>();
        DEFAULT_PERMISSIONS_PRIVATE = new HashMap<>();
        List<String> actionsOwner = new ArrayList<>();
        actionsOwner.add(ActionKeys.ADD_DISCUSSION);
        actionsOwner.add(ActionKeys.ADD_LAYOUT);
        actionsOwner.add(ActionKeys.CONFIGURE_PORTLETS);
        actionsOwner.add(ActionKeys.CUSTOMIZE);
        actionsOwner.add(ActionKeys.DELETE);
        actionsOwner.add(ActionKeys.DELETE_DISCUSSION);
        actionsOwner.add(ActionKeys.PERMISSIONS);
        actionsOwner.add(ActionKeys.UPDATE);
        actionsOwner.add(ActionKeys.UPDATE_DISCUSSION);
        actionsOwner.add(ActionKeys.VIEW);

        actionsOwner.add(ActionKeys.UPDATE_DISCUSSION);

        DEFAULT_PERMISSIONS_PUBLIC.put(RoleConstants.OWNER, actionsOwner);

        List<String> actionsUser = new ArrayList<>();
        actionsUser.add(ActionKeys.ADD_DISCUSSION);
        actionsUser.add(ActionKeys.CUSTOMIZE);
        actionsUser.add(ActionKeys.VIEW);

        DEFAULT_PERMISSIONS_PUBLIC.put(RoleConstants.SITE_MEMBER, actionsUser);

        DEFAULT_PERMISSIONS_PRIVATE.putAll(DEFAULT_PERMISSIONS_PUBLIC);
        List<String> actionsGuest = new ArrayList<>();
        actionsGuest.add(ActionKeys.VIEW);
        DEFAULT_PERMISSIONS_PRIVATE.put(RoleConstants.GUEST, actionsGuest);
    }

    private SetupPages() {

    }

    /**
     * @param organization organization entity defined in xml descriptor
     * @param groupId liferay site scope
     * @param company liferay instance scope
     * @param userid run as user
     * @throws SystemException
     * @throws PortalException
     */
    public static void setupOrganizationPages(final Organization organization, final long groupId,
            final long company, final long userid) throws SystemException, PortalException {

        PublicPages publicPages = organization.getPublicPages();
        if (publicPages != null) {
            if (publicPages.getTheme() != null) {
                setupTheme(groupId, publicPages.getTheme(), false);
            }
            if (publicPages.isDeleteExistingPages()) {
                LOG.info("Setup: Deleting pages from organization " + organization.getName());
                deletePages(groupId);
            }
            addPages(publicPages.getPage(), groupId, false, 0, company, userid);
        }

        PrivatePages privatePages = organization.getPrivatePages();
        if (privatePages != null) {
            if (privatePages.getTheme() != null) {
                setupTheme(groupId, privatePages.getTheme(), true);
            }
            if (privatePages.isDeleteExistingPages()) {
                LOG.info("Setup: Deleting pages from organization " + organization.getName());
                deletePages(groupId);
            }

            addPages(privatePages.getPage(), groupId, true, 0, company, userid);
        }
    }

    /**
     * Set the page templates up. As this is heavily based on page (layout).
     *
     * @param pageTemplates
     *            The page template definitions that are imported.
     * @param groupId
     *            The group id of the site where to import the
     * @param company
     *            The id of the company to which the templates are imported.
     * @param userid
     *            The user id of the importing user.
     */
    public static void setupPageTemplates(final PageTemplates pageTemplates, final long groupId,
            final long company, final long userid) {
        try {
            for (PageTemplate pageTemplate : pageTemplates.getPageTemplate()) {
                String name = pageTemplate.getName();
                if (name != null) {

                    LayoutPrototype lp;
                    DynamicQuery dq = LayoutPrototypeLocalServiceUtil.dynamicQuery()
                            .add(PropertyFactoryUtil.forName("name").like("%" + name + "%"));
                    List<LayoutPrototype> listLayoutPrototype = LayoutPrototypeLocalServiceUtil
                            .dynamicQuery(dq);
                    if (listLayoutPrototype != null && listLayoutPrototype.size() > 0) {
                        lp = listLayoutPrototype.get(0);
                    } else {
                        Map<Locale, String> titleMap = TitleMapUtil.getTitleMap(
                                pageTemplate.getTitleTranslation(), groupId, name,
                                " Page template  " + name);
                        lp = LayoutPrototypeLocalServiceUtil.addLayoutPrototype(userid, company,
                                titleMap, name, true, new ServiceContext());
                    }
                    if (lp != null) {
                        Layout layout = lp.getLayout();
                        if (pageTemplate.getPage() != null) {
                            Page page = pageTemplate.getPage();
                            if (page.getFriendlyURL() != null
                                    && !page.getFriendlyURL().equals("")) {
                                LOG.error("The page of page template " + name + " may not have a "
                                        + "friendly URL! Will ignore it!");
                            }
                            setupLiferayPage(layout, page, groupId, false, company, userid, name);
                        }
                    } else {
                        LOG.error("Could not create or find the page template " + name);
                    }
                }
            }
        } catch (PortalException | SystemException e) {
            LOG.error("Problem during creating page templates ", e);
        }
    }

    /**
     * @param groupId
     * @param theme
     * @param isPrivate
     * @throws SystemException
     * @throws PortalException
     */
    private static void setupTheme(final long groupId, final Theme theme, final boolean isPrivate)
            throws SystemException, PortalException {

        Group group = GroupLocalServiceUtil.getGroup(groupId);
        LayoutSet set;
        if (isPrivate) {
            set = group.getPrivateLayoutSet();
        } else {
            set = group.getPublicLayoutSet();
        }
        set.setThemeId(theme.getName());
        LayoutSetLocalServiceUtil.updateLayoutSet(set);
    }

    /**
     * @param pages list of pages defined in xml descriptor
     * @param groupId Liferay site scope
     * @param isPrivate should page be added in private pages?
     * @param parentLayoutId layout id of parent page
     * @param company Liferay instance scope
     * @param userId run as user
     * @throws SystemException
     * @throws PortalException
     */
    private static void addPages(final List<Page> pages, final long groupId,
            final boolean isPrivate, final long parentLayoutId, final long company,
            final long userId) throws SystemException, PortalException {

        for (Page page : pages) {

            Layout layout = null;
            try {
                layout = LayoutLocalServiceUtil.getFriendlyURLLayout(groupId, isPrivate,
                        page.getFriendlyURL());
                LOG.info("Setup: Page " + page.getName() + " already exist, not creating...");
                if (layout != null && page.isDeleteExistingPages()) {
                    LayoutLocalServiceUtil.deleteLayout(layout);
                    if (page.getLinkToURL() == null || page.getLinkToURL().equals("")) {
                        layout = createPage(groupId, page, parentLayoutId, isPrivate);
                    } else {
                        layout = createLinkPage(page, groupId, parentLayoutId, userId);
                    }
                } else if (layout != null
                        && (page.getLinkToURL() != null && !page.getLinkToURL().equals(""))) {
                    updateLinkPage(page, groupId);
                }
            } catch (NoSuchLayoutException e) {
                if (page.getLinkToURL() == null || page.getLinkToURL().equals("")) {
                    layout = createPage(groupId, page, parentLayoutId, isPrivate);
                } else {
                    layout = createLinkPage(page, groupId, parentLayoutId, userId);
                }
                LOG.info("Setup: Page " + page.getName() + " created...");
            } catch (Exception ex) {
                LOG.error(ex);
            }
            setupLiferayPage(layout, page, groupId, isPrivate, company, userId, null);
        }
    }

    private static void setupLiferayPage(final Layout layout, final Page page, final long groupId,
            final boolean isPrivate, final long company, final long userId,
            final String pageTemplateName) throws SystemException, PortalException {
        if (page.getTheme() != null) {
            setPageTheme(layout, page);
        }
        if (page.getLayout() != null) {
            setLayoutTemplate(layout, page, userId);
        }

        List<Pageportlet> portlets = page.getPageportlet();
        if (portlets != null && !portlets.isEmpty()) {
            for (Pageportlet portlet : portlets) {
                try {
                    addPortletIntoPage(page, layout, portlet, company, groupId);

                } catch (ValidatorException | IOException e) {
                    LOG.error(e);
                }
            }
        }

        List<Page> subPages = page.getPage();
        if (subPages != null && !subPages.isEmpty()) {
            if (pageTemplateName != null && !pageTemplateName.equals("")) {
                LOG.error("Page template " + pageTemplateName + " may not have any sub-pages! "
                        + "Will ignore them!");
            } else {
                addPages(subPages, groupId, isPrivate, layout.getLayoutId(), company, userId);
            }
        }

        if (page.getCustomFieldSetting() != null && !page.getCustomFieldSetting().isEmpty()) {
            setCustomFields(userId, groupId, company, page, layout);
        }

        updatePage(layout, page, groupId);
        SetupPermissions.updatePermission("Page " + page.getFriendlyURL(), groupId, company,
                layout.getPlid(), Layout.class, page.getRolePermissions(),
                getDefaultPermissions(isPrivate));
    }

    private static HashMap<String, List<String>> getDefaultPermissions(final boolean isPrivate) {
        if (isPrivate) {
            return DEFAULT_PERMISSIONS_PRIVATE;
        }
        return DEFAULT_PERMISSIONS_PUBLIC;
    }

    private static Layout createLinkPage(final Page p, final long groupId,
            final long parentLayoutId, final long userId) {
        // all values are usually retrieved via special methods from our code
        // for better readability I have added the real values here

        String title = "my title";
        ServiceContext serviceContext = new ServiceContext();
        String layoutType = LayoutConstants.TYPE_URL;
        boolean hidden = p.isHidden();
        String friendlyURL = p.getFriendlyURL();
        // add the layout
        Layout layout = null;
        try {
            layout = LayoutLocalServiceUtil.addLayout(userId, groupId, false, parentLayoutId, title,
                    title, StringPool.BLANK, layoutType, hidden, friendlyURL, serviceContext);

            String linkToPageUrl = p.getLinkToURL();
            // set the value of the "link to page"
            UnicodeProperties props = layout.getTypeSettingsProperties();
            props.put("url", linkToPageUrl);
            layout.setTypeSettingsProperties(props);
            LayoutLocalServiceUtil.updateLayout(layout);
        } catch (PortalException | SystemException e) {
            LOG.error("Could not create link page " + p.getFriendlyURL() + " with link to url "
                    + p.getLinkToURL(), e);
        }
        return layout;
    }

    private static void updateLinkPage(final Page page, final long groupId) {
        try {
            Layout layout = LayoutLocalServiceUtil.getFriendlyURLLayout(groupId, false,
                    page.getFriendlyURL());
            if (layout.getLayoutType().getTypeSettingsProperties().get("url") == null) {
                LOG.error("Could not update link page " + page.getFriendlyURL()
                        + " with link to url" + " " + page.getLinkToURL()
                        + " because page is not a link type page! "
                        + " Maybe it has been imported before as non link type page. Please "
                        + "delete it and rerun!");
            } else {
                UnicodeProperties props = layout.getTypeSettingsProperties();
                props.put("url", page.getLinkToURL());
                layout.setTypeSettingsProperties(props);
                layout.setHidden(page.isHidden());
                LayoutLocalServiceUtil.updateLayout(layout);
            }
        } catch (PortalException | SystemException e) {
            LOG.error("Could not update link page " + page.getFriendlyURL() + " with link to url "
                    + page.getLinkToURL(), e);
        }
    }

    private static void updatePage(final Layout layout, final Page page, final long groupId) {
        Map<Locale, String> titleMap = TitleMapUtil.getTitleMap(page.getTitleTranslation(), groupId,
                page.getName(), " Page with title " + page.getFriendlyURL());
        try {
            if (titleMap != null) {
                layout.setTitleMap(titleMap);
                layout.setNameMap(titleMap);
            }
            LayoutLocalServiceUtil.updateLayout(layout);
        } catch (SystemException e) {
            LOG.error(e);
        }
    }

    private static void setCustomFields(final long runAsUserId, final long groupId,
            final long company, final Page page, final Layout layout) {
        Class clazz = Layout.class;
        String resolverHint = "Resolving customized value for page " + page.getFriendlyURL() + " "
                + "failed for key " + "%%key%% and value %%value%%";
        for (CustomFieldSetting cfs : page.getCustomFieldSetting()) {
            String key = cfs.getKey();
            String value = cfs.getValue();
            CustomFieldSettingUtil.setExpandoValue(
                    resolverHint.replace("%%key%%", key).replace("%%value%%", value), runAsUserId,
                    groupId, company, clazz, layout.getPlid(), key, value);
        }
    }

    private static void setPageTheme(final Layout layout, final Page page) throws SystemException {

        Theme theme = page.getTheme();
        if (theme != null) {
            layout.setThemeId(theme.getName());
            LayoutLocalServiceUtil.updateLayout(layout);

            LOG.info("setting theme on page: " + page.getName() + " : " + theme.getName());
        }
    }

    private static void addPortletIntoPage(final Page page, final Layout layout,
            final Pageportlet portlet, final long company, final long groupId)
                    throws SystemException, ValidatorException, IOException, PortalException {
        if (page.getLinkToURL() != null && !page.getLinkToURL().equals("")) {
            LOG.error("This is a link page! It cannot be cleared. If you intend to use this page "
                    + "for portlets, please"
                    + " delete this page, or remove the link from the page!");
        } else {
            long plid = layout.getPlid();
            int ownerType = PortletKeys.PREFS_OWNER_TYPE_LAYOUT;
            long runAsUserId = LiferaySetup.getRunAsUserId();

            LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout.getLayoutType();

            String portletId = portlet.getPortletId();
            String column = portlet.getColumn();

            String portletIdInc = "";
            try {
                portletIdInc = layoutTypePortlet.addPortletId(runAsUserId, portletId, column, -1);
                if (portletIdInc == null) {
                    portletIdInc = portletId;
                }
            } catch (PortalException | SystemException e) {
                LOG.error("Add portlet error ", e);
            }

            javax.portlet.PortletPreferences preferences = PortletPreferencesFactoryUtil
                    .getLayoutPortletSetup(layout, portletId);
            addPortletPreferences(preferences, portlet, company, groupId, runAsUserId);
            try {
                preferences.store();
                PortletPreferencesLocalServiceUtil.updatePreferences(runAsUserId, ownerType, plid,
                        portletIdInc, preferences);
            } catch (Exception ex) {
                LOG.error("Something went wrong when storing preferences for portlet " + portletId,
                        ex);
            }

            if (Validator.isNotNull(column) && Validator.isNotNull(portletIdInc)) {

                layoutTypePortlet.movePortletId(runAsUserId, portletIdInc, column, 2);
            }
            LayoutServiceUtil.updateLayout(layout.getGroupId(), layout.isPrivateLayout(),
                    layout.getLayoutId(), layout.getTypeSettings());
        }
    }

    private static void addPortletPreferences(final javax.portlet.PortletPreferences prefs,
            final Pageportlet portlet, final long company, final long groupId,
            final long runAsUserId) {
        List<PortletPreference> prefsList = portlet.getPortletPreference();
        for (PortletPreference p : prefsList) {
            try {
                prefs.setValue(p.getKey(), resolvePortletPrefValue(p.getKey(), p.getValue(),
                        portlet, company, groupId, runAsUserId));
            } catch (ReadOnlyException e) {
                LOG.error("Portlet preferences (" + p.getKey() + ", " + p.getValue() + ") of "
                        + "portlet " + portlet.getPortletId() + " caused an excpetion! ");
            }
        }
    }

    /**
     * Substitutes parameters in porlet preferences. Possible values are:
     * <ul>
     * <li>{{$ID_OF_SITE_WITH_NAME= &lt; name of the site/group &gt;}}</li>
     * <li>{{$ART-TEMPLATE-UUID-BY-KEY= &lt; value of article template key &gt;
     * }}</li>
     * <li>{{$ART-STRUCTURE-UUID-BY-KEY=&lt; value of article structure key &gt;
     * }}</li>
     * <li>{{$ART-TEMPLATE-ID-BY-KEY= &lt; value of article template key &gt; }}
     * </li>
     * <li>{{$ART-STRUCTURE-ID-BY-KEY=&lt; value of article structure key &gt;
     * }}</li>
     * <li>{{$ADT-TEMPLATE-ID-BY-KEY= &lt; value of ADT template key &gt; }}
     * </li>
     * <li>{{$ADT-TEMPLATE-UUID-BY-KEY= &lt; value of ADT template key &gt; }}
     * </li>
     * <li>{{$FILE= [value of the site scope in for of ::SITENAME::] &lt; value
     * of path and title of the refered document &gt; }}</li>
     * </ul>
     *
     * @param key
     *            The portlet key.
     * @param value
     *            The defined value which should be parametrized.
     * @param portlet
     *            The pageportlet definition.
     * @param company
     *            Id of the company.
     * @param groupId
     *            The group id.
     * @param runAsUserId
     *            The user id which import the data.
     * @return
     */
    private static String resolvePortletPrefValue(final String key, final String value,
            final Pageportlet portlet, final long company, final long groupId,
            final long runAsUserId) {
        String locationHint = "Key: " + key + " of portlet " + portlet.getPortletId();
        return ResolverUtil.lookupAll(runAsUserId, groupId, company, value, locationHint);
    }

    public static void setLayoutTemplate(final Layout layout, final Page page, final long userid) {
        if (layout.getLayoutType() instanceof LayoutTypePortlet) {
            LayoutTypePortlet portletLayout = (LayoutTypePortlet) layout.getLayoutType();

            if (page.isClearPage()) {
                if (page.getPageportlet() != null && page.getPageportlet().size() > 0
                        && page.getLinkToURL() != null && !page.getLinkToURL().equals("")) {
                    LOG.error("This is a link page! It cannot be cleared. If you intend to use "
                            + "this page for " + "portlets, please"
                            + " delete this page, or remove the link from the page!");
                } else {
                    removeAllPortlets(userid, portletLayout, layout);
                }
            }
            String themeId = null;
            try {

                if (page.getLayoutContainedInThemeWithId() != null
                        && !page.getLayoutContainedInThemeWithId().equals("")) {
                    themeId = page.getLayoutContainedInThemeWithId();
                }
                LayoutTemplate layoutTemplate = LayoutTemplateLocalServiceUtil
                        .getLayoutTemplate(page.getLayout(), false, themeId);

                if (layoutTemplate != null) {
                    LOG.info("Setting layout to " + page.getLayout() + " for page "
                            + page.getName());
                    if (themeId != null) {
                        LOG.info("Layout was looked up in theme " + themeId);
                    }
                    portletLayout.setLayoutTemplateId(
                            UserLocalServiceUtil.getDefaultUserId(layout.getCompanyId()),
                            layoutTemplate.getLayoutTemplateId());
                    LayoutLocalServiceUtil.updateLayout(layout);
                } else {
                    LOG.error("Layout template " + page.getLayout() + " not found !");
                    if (themeId != null) {
                        LOG.error("Layout was looked up in theme " + themeId);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error by setting layout template : " + page.getLayout(), e);
                if (themeId != null) {
                    LOG.error("Layout was looked up in theme " + themeId);
                }
            }

        }
    }

    private static void removeAllPortlets(final long runasUser,
            final LayoutTypePortlet layoutTypePortlet, final Layout layout) {
        List<Portlet> portlets = null;
        try {
            portlets = layoutTypePortlet.getAllPortlets();
        } catch (PortalException | SystemException e1) {
            LOG.error(e1);
        }
        if (portlets != null) {
            for (Portlet portlet : portlets) {

                String portletId = portlet.getPortletId();

                try {
                    if (layoutTypePortlet.hasPortletId(portletId)) {
                        LOG.debug("Removing portlet " + portletId);
                        layoutTypePortlet.removePortletId(runasUser, portletId);
                        String rootPortletId = PortletConstants.getRootPortletId(portletId);
                        LOG.debug("Root portletId: " + rootPortletId);
                        ResourceLocalServiceUtil.deleteResource(layout.getCompanyId(),
                                rootPortletId, ResourceConstants.SCOPE_INDIVIDUAL,
                                PortletPermissionUtil.getPrimaryKey(layout.getPlid(), portletId));
                        LayoutLocalServiceUtil.updateLayout(layoutTypePortlet.getLayout());
                        List<com.liferay.portal.model.PortletPreferences> list = PortletPreferencesLocalServiceUtil
                                .getPortletPreferences(PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
                                        layout.getPlid(), portletId);
                        for (com.liferay.portal.model.PortletPreferences p : list) {
                            PortletPreferencesLocalServiceUtil.deletePortletPreferences(p);
                        }
                    }
                } catch (PortalException | SystemException e) {
                    LOG.error(e);
                }
            }
        }
    }

    private static void deletePages(final long groupId) {

        ServiceContext serviceContext = new ServiceContext();
        try {
            LayoutLocalServiceUtil.deleteLayouts(groupId, false, serviceContext);
            LOG.info("Setup: Pages removed.");
        } catch (PortalException | SystemException e) {
            LOG.error("cannot remove pages: " + e);
        }
    }

    private static Layout createPage(final long groupId, final Page currentPage,
            final long parentLayoutId, final boolean isPrivate)
                    throws SystemException, PortalException {

        return LayoutLocalServiceUtil.addLayout(LiferaySetup.getRunAsUserId(), groupId, isPrivate,
                parentLayoutId, currentPage.getName(), currentPage.getName(),
                LiferaySetup.DESCRIPTION, currentPage.getType(), currentPage.isHidden(),
                currentPage.getFriendlyURL(), new ServiceContext());
    }

}
