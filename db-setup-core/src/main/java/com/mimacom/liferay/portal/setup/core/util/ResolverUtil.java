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

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserGroupLocalServiceUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ArrayList;
import java.util.List;

public final class ResolverUtil {

    // CHECKSTYLE:OFF
    public static final int ID_TYPE_ID = 0;
    public static final int ID_TYPE_UUID = 1;
    public static final int ID_TYPE_RESOURCE = 2;
    public static final int ID_TYPE_FILE = 3;
    private static final Log LOG = LogFactoryUtil.getLog(ResolverUtil.class);
    private static final String CLOSING_TAG = "$}}";
    private static final String ARTICLE_BY_ART_ID = "{{$ARTICLE-%%IDTYPE%%-BY-ARTICLE-ID=";
    private static final String TEMPLATE_BY_KEY = "{{$%%PREFIX%%-TEMPLATE-%%IDTYPE%%-BY-KEY=";
    private static final String STRUCTURE_BY_KEY = "{{$%%PREFIX%%-STRUCTURE-%%IDTYPE%%-BY-KEY=";
    private static final String FILE_REFERENCE_URL = "{{$FILE-URL=";
    private static final String FILE_REFERENCE_ID = "{{$FILE-ID=";
    private static final String FILE_REFERENCE_UUID = "{{$FILE-UUID=";
    private static final String CLASS_ID_BY_NAME = "{{$CLASS-ID-BY-NAME=";
    private static final String PAGE_ID_BY_FRIENDLY_URL = "{{$%%PTYPE%%-PAGE-%%LAYOUTID%%-BY-FRIENDLY_URL=";
    private static final String DDL_REC_SET_BY_KEY = "{{$DDL-REC-SET-ID-BY-KEY=";
    private static final String TEMPLATE_CATEGORY = "{{$CATEGORY-ID-BY-VOCABULARY-AND-PATH=";
    private static final String DEFAULT_GROUP_NAME = "Guest";
    private static final String ID_OF_SITE_WITH_NAME_KEY = "{{$ID_OF_SITE_WITH_NAME=";
    private static final String VALUE_SPLIT = "::";
    private static final String ID_OF_ORG_USER_GROUP_WITH_NAME_KEY = "{{$%%IDTYPE%%_OF_%%LOOKUPTYPE%%_WITH_NAME=";

    // CHECKSTYLE:ON

    private ResolverUtil() {
    }

    /**
     * Resolves a value with a given key inside a given content. Every occurance
     * of one of the following expressions is substituted by the resolved value:
     * <ul>
     * <li>{{$ARTICLE-ID-BY-ARTICLE-ID=[:: name of the site ::]&lt; article id
     * to look for &gt; $}}</li>
     * <li>{{$ARTICLE-UUIID-BY-ARTICLE-ID=[:: name of the site ::]&lt; article
     * id to look for &gt;$}}</li>
     * <li>{{$ARTICLE-RESID-BY-ARTICLE-ID=[:: name of the site ::]&lt; article
     * id to look for &gt;$}}</li>
     * <li>{{$ART-TEMPLATE-ID-BY-KEY=[:: name of the site ::]&lt; article id to
     * look for &gt; $}}</li>
     * <li>{{$ART-TEMPLATE-UUID-BY-KEY=[:: name of the site ::]&lt; article id
     * to look for &gt; $}}</li>
     * <li>{{$ART-STRUCTURE-ID-BY-KEY=[:: name of the site ::]&lt; article id to
     * look for &gt; $}}</li>
     * <li>{{$ART-STRUCTURE-UUID-BY-KEY=[:: name of the site ::]&lt; article id
     * to look for &gt; $}}</li>
     * <li>{{$ADT-TEMPLATE-ID-BY-KEY=[:: name of the site ::]&lt; article id to
     * look for &gt; $}}</li>
     * <li>{{$ADT-TEMPLATE-UUID-BY-KEY=[:: name of the site ::]&lt; article id
     * to look for &gt; $}}</li>
     * <li>{{$FILE-URL=[:: name of the site ::]&lt; documents and media folder
     * path to the file &gt;&lt; documents and media folder title of the file
     * &gt;$}}</li>
     * <li>{{$FILE-ID=[:: name of the site ::]&lt; documents and media folder
     * path to the file &gt;&lt; documents and media folder title of the file
     * &gt;$}}</li>
     * <li>{{$FILE-UUID=[:: name of the site ::]&lt; documents and media folder
     * path to the file &gt;&lt; documents and media folder title of the file
     * &gt;$}}</li>
     * <li>{{$CLASS-ID-BY-NAME=&lt; fully qualified class name &gt;$}}</li>
     * <li>{{$PRIV-PAGE-ID-BY-FRIENDLY_URL=[:: name of the site ::]&lt; friendly
     * url of the private page &gt;$}}</li>
     * <li>{{$PUB-PAGE-ID-BY-FRIENDLY_URL=[:: name of the site ::]&lt; friendly
     * url of the public page &gt;$}}</li>
     * <li>{{$PRIV-PAGE-PLID-BY-FRIENDLY_URL=[:: name of the site ::]&lt;
     * friendly url of the private page &gt;$}}</li>
     * <li>{{$PUB-PAGE-PLID-BY-FRIENDLY_URL=[:: name of the site ::]&lt;
     * friendly url of the public page &gt;$}}</li>
     * <li>{{$PUB-PAGE-UUID-BY-FRIENDLY_URL=[:: name of the site ::]&lt;
     * friendly url of the public page &gt;$}}</li>
     * <li>{{$DDL-REC-SET-ID-BY-KEYL=[:: name of the site ::]&lt; key of the DDL
     * record set &gt; $}}</li>
     * <li>{{ID_OF_ORG_WITH_NAME=&lt; name of the organization &gt;$}}</li>
     * <li>{{UUID_OF_ORG_WITH_NAME=&lt; name of the organization &gt;$}}</li>
     * <li>{{ID_OF_USER_GROUP_WITH_NAME=&lt; name of the user group &gt;$}}</li>
     * <li>{{UUDID_OF_USER_GROUP_WITH_NAME=&lt; name of the user group &gt;$}}
     * </li>
     * </ul>
     *
     * @param runAsUserId  The user id under which the look up is done.
     * @param groupId      the group id which is used by default for the look up.
     * @param company      the company id that is used for the default look up.
     * @param value        the value string in which the occurance of any above resolve
     *                     expression is resolved.
     * @param resolverHint the resovler hint textually specifies where the value is from
     *                     and is used for logging problems or infos on the resolution.
     *
     * @return Returns the string value with any resolver expression resolved to
     * the corresponding elements.
     */
    public static String lookupAll(final long runAsUserId, final long groupId, final long company,
                                   final String value, final String resolverHint) {
        String retVal = value;

        // substitute references to groups/sites
        retVal = ResolverUtil.lookupSiteIdWithName(resolverHint, value, company);
        // ID for article template
        retVal = ResolverUtil.lookupStructureOrTemplateIdWithKey(retVal, resolverHint, groupId,
                company, false, "ART", true, JournalArticle.class);
        // ID for article structure
        retVal = ResolverUtil.lookupStructureOrTemplateIdWithKey(retVal, resolverHint, groupId,
                company, false, "ART", false, JournalArticle.class);
        // UUID for article structure
        retVal = ResolverUtil.lookupStructureOrTemplateIdWithKey(retVal, resolverHint, groupId,
                company, true, "ART", false, JournalArticle.class);
        // UUID for article template
        retVal = ResolverUtil.lookupStructureOrTemplateIdWithKey(retVal, resolverHint, groupId,
                company, true, "ART", true, JournalArticle.class);
        // UUID for ADT
        retVal = ResolverUtil.lookupStructureOrTemplateIdWithKey(retVal, resolverHint, groupId,
                company, true, "ADT", true, AssetEntry.class);
        // ID for ADT
        retVal = ResolverUtil.lookupStructureOrTemplateIdWithKey(retVal, resolverHint, groupId,
                company, false, "ADT", true, AssetEntry.class);

        //Resolve categories
        retVal=ResolverUtil.substituteCategoryNameWithCategoryId(retVal,resolverHint,groupId,company,runAsUserId);

        // Substitute the article key with the primary key (id)
        retVal = ResolverUtil.lookupArticleWithArticleId(retVal, resolverHint, groupId, company,
                ID_TYPE_ID);
        // Substitute the article key with the uuid of the article
        retVal = ResolverUtil.lookupArticleWithArticleId(retVal, resolverHint, groupId, company,
                ID_TYPE_UUID);
        // Resource type id for articles
        retVal = ResolverUtil.lookupArticleWithArticleId(retVal, resolverHint, groupId, company,
                ID_TYPE_RESOURCE);
        // Substitute references to files by their URLs
        retVal = ResolverUtil.substituteFileReferencesWithURL(retVal, resolverHint, groupId,
                company, groupId, runAsUserId, ID_TYPE_FILE);
        // Substitute references to files by their id
        retVal = ResolverUtil.substituteFileReferencesWithURL(retVal, resolverHint, groupId,
                company, groupId, runAsUserId, ID_TYPE_ID);
        // Substitute references to files by their UUID
        retVal = ResolverUtil.substituteFileReferencesWithURL(retVal, resolverHint, groupId,
                company, groupId, runAsUserId, ID_TYPE_UUID);
        // Substitute class id references
        retVal = ResolverUtil.getClassIdByName(retVal, resolverHint);
        // Substitute private page friendly urls to layout ids
        retVal = ResolverUtil.lookupPageIdWithFriendlyUrl(retVal, resolverHint, groupId, company,
                true, IdMode.ID);
        // Substitute public page friendly urls to layout ids
        retVal = ResolverUtil.lookupPageIdWithFriendlyUrl(retVal, resolverHint, groupId, company,
                false, IdMode.ID);
        // Substitute private page friendly urls to plids
        retVal = ResolverUtil.lookupPageIdWithFriendlyUrl(retVal, resolverHint, groupId, company,
                true, IdMode.PLID);
        // Substitute public page friendly urls to plids
        retVal = ResolverUtil.lookupPageIdWithFriendlyUrl(retVal, resolverHint, groupId, company,
                false, IdMode.PLID);
        // Substitute private page friendly urls to uuids
        retVal = ResolverUtil.lookupPageIdWithFriendlyUrl(retVal, resolverHint, groupId, company,
                true, IdMode.UUID);
        // Substitute public page friendly urls to uuids
        retVal = ResolverUtil.lookupPageIdWithFriendlyUrl(retVal, resolverHint, groupId, company,
                false, IdMode.UUID);
        // lookup ddl record set id by key
        retVal = lookupDDLRecordSetId(retVal, resolverHint, groupId, company);
        // replace id of user groups
        retVal = lookupOrgOrUserGroupIdWithName(resolverHint, retVal, company, false, false);
        // replace uuid of user groups
        retVal = lookupOrgOrUserGroupIdWithName(resolverHint, retVal, company, true, false);
        // replace id of orgs
        retVal = lookupOrgOrUserGroupIdWithName(resolverHint, retVal, company, false, true);
        // replace uuid of orgs
        retVal = lookupOrgOrUserGroupIdWithName(resolverHint, retVal, company, true, true);
        return retVal;
    }

    public static String getClassIdByName(final String value, final String locationHint) {
        String valueCopy = value;
        String retVal = valueCopy;
        while (valueCopy != null && valueCopy.trim().indexOf(CLASS_ID_BY_NAME) > -1) {
            int pos = valueCopy.trim().indexOf(CLASS_ID_BY_NAME);
            int pos2 = valueCopy.indexOf(CLOSING_TAG, pos + 1);
            String name = "";
            if (pos2 > -1) {
                try {
                    name = valueCopy.substring(pos + CLASS_ID_BY_NAME.length(), pos2);

                    long groupId = ResolverUtil.getClassId(name);
                    retVal = valueCopy.substring(0, pos) + groupId
                            + valueCopy.substring(pos2 + CLOSING_TAG.length(), valueCopy.length());
                    valueCopy = retVal;
                } catch (Exception ex) {
                    LOG.error("Could not resolve class " + name + " for " + locationHint, ex);
                }
            } else {
                LOG.warn("Could not resolve site name, as the syntax is offendended, closing $}} "
                        + "is missing for " + locationHint);
                break;
            }
        }

        return retVal;
    }

    public static long getSiteGroupIdByName(final String siteName, final long company,
                                            final String locationName) {
        long siteGroupId = 0;

        if (siteName.toLowerCase().equals("global")) {
            try {
                // look up global site
                siteGroupId = GroupLocalServiceUtil.getCompanyGroup(company).getGroupId();
            } catch (PortalException e) {
                LOG.error("Id of global site could not be retrieved!");
                LOG.error((Throwable) e);
            } catch (SystemException e) {
                LOG.error("Id of global site could not be retrieved!");
                LOG.error((Throwable) e);
            }
        } else {
            try {
                // look up default site
                siteGroupId = GroupLocalServiceUtil.getGroup(company, getSiteName(siteName))
                        .getGroupId();
            } catch (PortalException e) {
                LOG.error("Id of site " + siteName + " could not be retrieved for " + locationName);
                LOG.error((Throwable) e);
            } catch (SystemException e) {
                LOG.error("Id of site " + siteName + " could not be retrieved for" + locationName);
                LOG.error((Throwable) e);
            }
        }
        return siteGroupId;
    }

    private static String getSiteName(final String siteName) {
        if (siteName.toLowerCase().equals("default") || siteName.equals("")) {
            return DEFAULT_GROUP_NAME;
        }
        // WAS needed till version 2.0.x when Organizations were used instead of Sites
//        return siteName + " LFR_ORGANIZATION";
        return siteName;
    }

    /**
     * Substitutes all references for documents and media files. A file
     * reference must have the following format in BNF: <br/>
     * fileReference ::= "{{$FILE=" siteReference+ filePath "$}}"<br/>
     * siteReference ::= "::" &lt; site-name &gt; "::"<br/>
     * filePath ::= ("/" &lt; path-segment &gt;)*<br/>
     * <br/>
     *
     * @param content      The content of the article.
     * @param locationHint A location hint where the substitution is done (for logging),
     *                     eg., the file name of the article.
     * @param groupId      The group id (site) in which scope the article is imported.
     * @param company      The company id.
     * @param repoId       The repository id.
     * @param userId       The id of the importing user.
     *
     * @return Returns the content with all substituted file references.
     */
    public static String substituteFileReferencesWithURL(final String content,
                                                         final String locationHint, final long groupId, final long company, final long repoId,
                                                         final long userId, final int refType) {
        String openingTag = FILE_REFERENCE_URL;
        if (refType == ID_TYPE_ID) {
            openingTag = FILE_REFERENCE_ID;
        } else if (refType == ID_TYPE_UUID) {
            openingTag = FILE_REFERENCE_UUID;
        }
        String result = content;
        int pos = result.indexOf(openingTag);
        while (pos > -1) {
            int pos2 = result.indexOf(CLOSING_TAG, pos);
            if (pos2 < 0) {
                LOG.error("No closing Tag, pos " + pos + " in file " + locationHint);
                break;
            } else {
                // by default the referred file is looked up in current site.
                long siteGroupId = groupId;
                String filePath = result.substring(pos + openingTag.length(), pos2).trim();

                // check for the reference to another site
                String[] refSegs = ResolverUtil.separateSiteRef(filePath);
                if (!refSegs[0].equals("")) {
                    siteGroupId = ResolverUtil.getSiteGroupIdByName(refSegs[0], company,
                            locationHint);
                    filePath = refSegs[1];
                }

                FileEntry fe = DocumentUtil.findDocument(filePath, siteGroupId, company, repoId,
                        userId);
                if (fe == null) {
                    LOG.error("Referred file " + filePath + " is not found in documents and media"
                            + ".");
                    result = result.substring(0, pos) + " <file-not-found /> "
                            + result.substring(pos2 + CLOSING_TAG.length(), result.length());
                } else {
                    String fileEntryRef = " <file-not-found /> ";
                    try {
                        if (refType == ID_TYPE_ID) {
                            fileEntryRef = Long.toString(fe.getFileEntryId());
                        } else if (refType == ID_TYPE_UUID) {
                            fileEntryRef = fe.getUuid();
                        } else {
                            fileEntryRef = DLUtil.getPreviewURL(fe, fe.getFileVersion(), null,
                                    StringPool.BLANK);
                        }
                    } catch (PortalException e) {
                        LOG.error("URL of referred file " + filePath + " cannot be retrieved.");
                    } catch (SystemException e) {
                        LOG.error("URL of referred file " + filePath + " cannot be retrieved.");
                    }
                    result = result.substring(0, pos) + fileEntryRef
                            + result.substring(pos2 + CLOSING_TAG.length(), result.length());
                }
            }
            pos = result.indexOf(openingTag, pos + 1);
        }
        return result;
    }
    public static String substituteCategoryNameWithCategoryId(final String content,
            final String locationHint, final long groupId, final long company,
            final long userId) {
        String openingTag = TEMPLATE_CATEGORY;

        String result=content;

        if (result.startsWith(openingTag)){

            String[] values=result.replace(CLOSING_TAG,"").split("::");
            if(values.length==4) {
                long groupIdResolved=groupId;

                try {
                    groupIdResolved=ResolverUtil.getSiteGroupIdByName(values[1], company, locationHint);

                    try {
                        AssetVocabulary assetVocabulary = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupIdResolved, values[2]);

                        String[] categoryIds=values[3].split("/");

                        try {
                            AssetCategory category= assetVocabulary.getCategories().stream().filter(vocabularyCategory->vocabularyCategory.getName().equals(categoryIds[0])).findFirst().orElseThrow(PortalException::new);

                            for (int i = 1; i < categoryIds.length; i++) {
                                String categoryName=categoryIds[i];
                                category=AssetCategoryLocalServiceUtil.getChildCategories(category.getCategoryId()).stream().filter(childrenCategory->childrenCategory.getName().equals(categoryName)).findFirst().orElseThrow(PortalException::new);
                            }
                            return String.valueOf(category.getCategoryId());
                        }catch (PortalException e){
                            LOG.error("Could not resolve category path for " + locationHint, e);
                        }
                    }catch (PortalException e){
                        LOG.error("Could not resolve vocabulary name for " + locationHint, e);
                    }

                }catch(Exception e){
                    LOG.error("Could not resolve site name for " +locationHint , e);
                }
            }else{
                LOG.error("Categories to be susbstited is not in correct format : SiteName::Vocabulary::CategoriesPath");
            }

        }


        return result;
    }

    public static String lookupSiteIdWithName(final String locationHint, final String value,
                                              final long company) {
        String valueCopy = value;
        String retVal = valueCopy;
        while (valueCopy != null && valueCopy.trim().indexOf(ID_OF_SITE_WITH_NAME_KEY) > -1) {
            int pos = valueCopy.trim().indexOf(ID_OF_SITE_WITH_NAME_KEY);
            int pos2 = valueCopy.indexOf(CLOSING_TAG, pos + 1);
            if (pos2 > -1) {
                try {
                    String name = valueCopy.substring(pos + ID_OF_SITE_WITH_NAME_KEY.length(),
                            pos2);

                    long groupId = ResolverUtil.getSiteGroupIdByName(name, company, locationHint);
                    retVal = valueCopy.substring(0, pos) + groupId
                            + valueCopy.substring(pos2 + CLOSING_TAG.length(), valueCopy.length());
                    valueCopy = retVal;
                } catch (Exception ex) {
                    LOG.error("Could not resolve site name for " + locationHint, ex);
                }
            } else {
                LOG.warn("Could not resolve site name, as the syntax is offendended, closing $}} "
                        + "is missing for " + locationHint);
                break;
            }
        }

        return retVal;
    }

    public static String lookupOrgOrUserGroupIdWithName(final String locationHint,
                                                        final String value, final long company, final boolean uuid, final boolean org) {
        String valueCopy = value;
        String retVal = valueCopy;
        String searchString = ID_OF_ORG_USER_GROUP_WITH_NAME_KEY;
        if (uuid) {
            searchString = searchString.replace("%%IDTYPE%%", "UUID");
        } else {
            searchString = searchString.replace("%%IDTYPE%%", "ID");
        }
        if (org) {
            searchString = searchString.replace("%%LOOKUPTYPE%%", "ORG");
        } else {
            searchString = searchString.replace("%%LOOKUPTYPE%%", "USER_GROUP");
        }
        while (valueCopy != null && valueCopy.trim().indexOf(searchString) > -1) {
            int pos = valueCopy.trim().indexOf(searchString);
            int pos2 = valueCopy.indexOf(CLOSING_TAG, pos + 1);
            if (pos2 > -1) {
                try {
                    String name = valueCopy.substring(pos + searchString.length(), pos2);
                    String replacementId = "NOT FOUND";
                    if (org) {
                        Organization o = ResolverUtil.getOrganization(name, company, name);
                        if (o != null) {
                            if (uuid) {
                                replacementId = o.getUuid();
                            } else {
                                replacementId = Long.toString(o.getOrganizationId());
                            }
                        }
                    } else {
                        UserGroup ug = ResolverUtil.getUserGroup(name, company, name);
                        if (ug != null) {
                            if (uuid) {
                                replacementId = ug.getUuid();
                            } else {
                                replacementId = Long.toString(ug.getUserGroupId());
                            }
                        }
                    }

                    retVal = valueCopy.substring(0, pos) + replacementId
                            + valueCopy.substring(pos2 + CLOSING_TAG.length(), valueCopy.length());
                    valueCopy = retVal;
                } catch (Exception ex) {
                    String type = "user group";
                    if (org) {
                        type = "organization";
                    }
                    LOG.error("Could not resolve  " + type + " name for " + locationHint, ex);
                }
            } else {
                String type = "user group";
                if (org) {
                    type = "organization";
                }
                LOG.warn("Could not resolve " + type + " name, as the syntax is offendended, "
                        + "closing $}} is missing " + "for " + locationHint);
                break;
            }
        }

        return retVal;
    }

    public static String lookupArticleWithArticleId(final String content, final String locationHint,
                                                    final long groupId, final long company, final int typeOfId) {
        String contentCopy = content;
        String retVal = contentCopy;
        long siteGroupId = groupId;
        int pos = -1;
        String lookup = ARTICLE_BY_ART_ID;

        if (typeOfId == 0) {
            lookup = lookup.replace("%%IDTYPE%%", "ID");
        } else if (typeOfId == 1) {
            lookup = lookup.replace("%%IDTYPE%%", "UUID");
        } else if (typeOfId == 2) {
            lookup = lookup.replace("%%IDTYPE%%", "RESID");
        }

        while (contentCopy != null && contentCopy.indexOf(lookup) > -1) {
            pos = contentCopy.indexOf(lookup);
            int pos2 = contentCopy.indexOf(CLOSING_TAG, pos + 1);
            if (pos2 > -1) {
                String name = contentCopy.substring(pos + lookup.length(), pos2);

                // check for the reference to another site
                String[] refSegs = ResolverUtil.separateSiteRef(name);
                if (!refSegs[0].equals("")) {
                    siteGroupId = ResolverUtil.getSiteGroupIdByName(refSegs[0], company,
                            locationHint);
                    name = refSegs[1];
                }
                String templateId = "";
                try {
                    JournalArticle ja = getArticleByArticleID(name, siteGroupId);
                    if (ja != null) {
                        if (typeOfId == 0) {
                            templateId = Long.toString(ja.getId());
                        } else if (typeOfId == 1) {
                            templateId = ja.getUuid();
                        } else if (typeOfId == 2) {
                            templateId = Long.toString(ja.getResourcePrimKey());
                        }
                    } else {
                        LOG.error("Article with article id " + name + " not found for "
                                + locationHint);
                        templateId = "!!NOTFOUND!!";
                    }
                } catch (SystemException e) {
                    LOG.error("Article with article id " + name + " not found for " + locationHint);
                    LOG.error((Throwable) e);
                }

                retVal = contentCopy.substring(0, pos) + templateId
                        + contentCopy.substring(pos2 + CLOSING_TAG.length(), contentCopy.length());
                contentCopy = retVal;
            } else {
                LOG.warn("Could not resolve template, as the syntax is offended, closing $}} is "
                        + "missing for " + locationHint
                        + " abort parsing, as this is possibly an error!");
                break;
            }
        }
        return retVal;
    }

    public static String lookupPageIdWithFriendlyUrl(final String content,
                                                     final String locationHint, final long groupId, final long company,
                                                     final boolean isPrivate, final IdMode mode) {
        String contentCopy = content;
        String lookUp = PAGE_ID_BY_FRIENDLY_URL;
        if (isPrivate) {
            lookUp = lookUp.replace("%%PTYPE%%", "PRIV");
        } else {
            lookUp = lookUp.replace("%%PTYPE%%", "PUB");
        }
        switch (mode) {
            case ID:
                lookUp = lookUp.replace("%%LAYOUTID%%", "PLID");
                break;
            case PLID:
                lookUp = lookUp.replace("%%LAYOUTID%%", "ID");
                break;
            case UUID:
                lookUp = lookUp.replace("%%LAYOUTID%%", "UUID");
                break;
        }
        int pos = contentCopy.indexOf(lookUp);
        while (pos > -1) {
            int pos2 = contentCopy.indexOf(CLOSING_TAG, pos);
            if (pos2 < 0) {
                LOG.error("No closing Tag, pos " + pos + " for " + locationHint);
                break;
            } else {
                // by default the referred file is looked up in current site.
                long siteGroupId = groupId;
                String fUrl = contentCopy.substring(pos + lookUp.length(), pos2).trim();

                // check for the reference to another site
                String[] refSegs = ResolverUtil.separateSiteRef(fUrl);
                if (!refSegs[0].equals("")) {
                    siteGroupId = ResolverUtil.getSiteGroupIdByName(refSegs[0], company,
                            locationHint);
                    fUrl = refSegs[1];
                }

                String pageId = "NOT FOUND";
                Layout l = null;
                try {
                    l = LayoutLocalServiceUtil.getFriendlyURLLayout(siteGroupId, isPrivate, fUrl);
                } catch (PortalException | SystemException e) {
                    e.printStackTrace();
                }

                if (l == null) {
                    LOG.error("Referred page " + fUrl + " is not found .");
                    contentCopy = contentCopy.substring(0, pos) + " PAGE NOT FOUND!! " + contentCopy
                            .substring(pos2 + CLOSING_TAG.length(), contentCopy.length());
                } else {
                    switch (mode) {
                        case ID:
                            pageId = Long.toString(l.getLayoutId());
                            break;
                        case PLID:
                            pageId = Long.toString(l.getPlid());
                            break;
                        case UUID:
                            pageId = l.getUuid();
                            break;
                    }
                    contentCopy = contentCopy.substring(0, pos) + pageId + contentCopy
                            .substring(pos2 + CLOSING_TAG.length(), contentCopy.length());
                }
            }
            pos = contentCopy.indexOf(lookUp, pos + 1);
        }

        //
        return contentCopy;
    }

    public static String lookupDDLRecordSetId(final String content, final String locationHint,
                                              final long groupId, final long company) {
        String contentCopy = content;
        String lookUp = DDL_REC_SET_BY_KEY;

        int pos = contentCopy.indexOf(lookUp);
        while (pos > -1) {
            int pos2 = contentCopy.indexOf(CLOSING_TAG, pos);
            if (pos2 < 0) {
                LOG.error("No closing Tag, pos " + pos + " for " + locationHint);
                break;
            } else {
                // by default the referred file is looked up in current site.
                long siteGroupId = groupId;
                String recordsetId = contentCopy.substring(pos + lookUp.length(), pos2).trim();

                // check for the reference to another site
                String[] refSegs = ResolverUtil.separateSiteRef(recordsetId);
                if (!refSegs[0].equals("")) {
                    siteGroupId = ResolverUtil.getSiteGroupIdByName(refSegs[0], company,
                            locationHint);
                    recordsetId = refSegs[1];
                }

                String pageId = "NOT FOUND";
                DDLRecordSet rs = null;
                try {
                    rs = DDLRecordSetLocalServiceUtil.getRecordSet(siteGroupId, recordsetId);

                } catch (PortalException e) {
                    LOG.error("Error retrieving referred DDL structure " + recordsetId + ".");
                } catch (SystemException e) {
                    LOG.error("Error retrieving referred DDL structure " + recordsetId + ".");
                }

                if (rs == null) {
                    LOG.error("Referred DDL structure " + recordsetId + " is not found .");
                    contentCopy = contentCopy.substring(0, pos) + " PAGE NOT FOUND!! " + contentCopy
                            .substring(pos2 + CLOSING_TAG.length(), contentCopy.length());
                } else {
                    pageId = Long.toString(rs.getRecordSetId());
                    contentCopy = contentCopy.substring(0, pos) + pageId + contentCopy
                            .substring(pos2 + CLOSING_TAG.length(), contentCopy.length());
                }
            }
            pos = contentCopy.indexOf(lookUp, pos + 1);
        }

        //
        return contentCopy;
    }

    // CHECKSTYLE:OFF
    public static String lookupStructureOrTemplateIdWithKey(final String content,
                                                            final String locationHint, final long groupId, final long company, final boolean uuid,
                                                            final String commandPrefix, final boolean isTemplate, final Class referredClass) {
        String contentCopy = content;
        String retVal = contentCopy;
        long siteGroupId = groupId;
        int pos = -1;
        String lookup = TEMPLATE_BY_KEY;
        if (!isTemplate) {
            lookup = STRUCTURE_BY_KEY;
        }
        if (uuid) {
            lookup = lookup.replace("%%IDTYPE%%", "UUID");
        } else {
            lookup = lookup.replace("%%IDTYPE%%", "ID");
        }
        lookup = lookup.replace("%%PREFIX%%", commandPrefix);
        while (contentCopy != null && contentCopy.indexOf(lookup) > -1) {
            pos = contentCopy.indexOf(lookup);
            int pos2 = contentCopy.indexOf(CLOSING_TAG);
            if (pos2 > -1) {
                String name = contentCopy.substring(pos + lookup.length(), pos2);

                // check for the reference to another site
                String[] refSegs = ResolverUtil.separateSiteRef(name);
                if (!refSegs[0].equals("")) {
                    siteGroupId = ResolverUtil.getSiteGroupIdByName(refSegs[0], company,
                            locationHint);
                    name = refSegs[1];
                }
                String templateId = "";
                try {
                    if (uuid) {
                        if (isTemplate) {
                            templateId = getTemplateUUID(name, siteGroupId);
                        } else {
                            templateId = getStructureUUID(name, siteGroupId, referredClass);
                        }
                    } else {
                        if (isTemplate) {
                            templateId = Long
                                    .toString(getTemplateId(name, siteGroupId, referredClass));
                        } else {
                            templateId = Long
                                    .toString(getStructureId(name, siteGroupId, referredClass, false));
                        }
                    }
                } catch (PortalException | SystemException e) {
                    LOG.error("Template with key contentCopy " + name + " not found for "
                            + locationHint);
                    LOG.error((Throwable) e);
                }

                retVal = contentCopy.substring(0, pos) + templateId
                        + contentCopy.substring(pos2 + CLOSING_TAG.length(), contentCopy.length());
                contentCopy = retVal;
            } else {
                LOG.warn("Could not resolve template, as the syntax is offended, closing $}} is "
                        + "missing for " + locationHint
                        + " abort parsing, as this is possibly an error!");
                break;
            }
        }
        return retVal;
    }

    // CHECKSTYLE:ON

    public static JournalArticle getArticleByArticleID(final String articleId, final long groupId)
            throws SystemException {
        JournalArticle article = null;
        article = JournalArticleLocalServiceUtil.fetchLatestArticle(groupId, articleId,
                WorkflowConstants.STATUS_APPROVED);

        return article;
    }

    public static long getStructureId(final String structureKey, final long groupId,
                                      final Class clazz, boolean includeAncestorStructures) throws SystemException, PortalException {

        long classNameId = ClassNameLocalServiceUtil.getClassNameId(clazz);
        DDMStructure structure = DDMStructureLocalServiceUtil.getStructure(groupId, classNameId,
                structureKey, includeAncestorStructures);
        return structure.getStructureId();
    }

    public static String getStructureUUID(final String structureKey, final long groupId,
                                          final Class clazz) throws SystemException, PortalException {

        long classNameId = ClassNameLocalServiceUtil.getClassNameId(clazz);
        DDMStructure structure = DDMStructureLocalServiceUtil.getStructure(groupId, classNameId,
                structureKey);
        return structure.getUuid();
    }

    public static long getTemplateId(final String templateKey, final long groupId,
                                     final Class clazz) throws SystemException, PortalException {

        long classNameId = ClassNameLocalServiceUtil.getClassNameId(clazz);

        DDMTemplate template = DDMTemplateLocalServiceUtil.getTemplate(groupId, classNameId,
                templateKey);
        return template.getTemplateId();
    }

    public static Organization getOrganization(final String name, final long companyId,
                                               final String locationHint) {
        Organization o = null;
        try {
            o = OrganizationLocalServiceUtil.getOrganization(companyId, name);
        } catch (PortalException e) {
            LOG.error("Could not retrieve organization " + name + " in context " + locationHint);
        } catch (SystemException e) {
            LOG.error("Could not retrieve organization " + name + " in context " + locationHint);
        }
        return o;
    }

    public static UserGroup getUserGroup(final String name, final long companyId,
                                         final String locationHint) {
        UserGroup o = null;
        try {
            o = UserGroupLocalServiceUtil.getUserGroup(companyId, name);
        } catch (PortalException e) {
            LOG.error("Could not retrieve organization " + name + " in context " + locationHint);
        } catch (SystemException e) {
            LOG.error("Could not retrieve organization " + name + " in context " + locationHint);
        }
        return o;
    }

    public static String getTemplateUUID(final String templateKey, final long groupId)
            throws SystemException, PortalException {

        DynamicQuery dq = DDMTemplateLocalServiceUtil.dynamicQuery()
                .add(PropertyFactoryUtil.forName("templateKey").eq(templateKey));
        List<DDMTemplate> templateList = new ArrayList<DDMTemplate>();
        String uuid = "NOT FOUND!!!!";
        try {
            DDMTemplate template = null;
            templateList = DDMTemplateLocalServiceUtil.dynamicQuery(dq);
            if (templateList != null && templateList.size() > 0 && templateList.get(0) != null) {
                uuid = templateList.get(0).getUuid();
            }
        } catch (SystemException e) {
            LOG.error("Tempate with key " + templateKey + " not found !!", e);
        }

        return uuid;
    }

    private static String[] separateSiteRef(final String content) {
        String contentCopy = content;
        String[] retVal = new String[2];
        retVal[0] = "";
        retVal[1] = contentCopy;

        if (contentCopy.startsWith("::")) {
            int siteNameEndPos = contentCopy.indexOf("::", 2);
            if (siteNameEndPos > -1) {
                String siteName = contentCopy.substring(0 + 2, siteNameEndPos);
                contentCopy = contentCopy.substring(siteNameEndPos + 2, contentCopy.length())
                        .trim();
                retVal[0] = siteName;
                retVal[1] = contentCopy;
            }
        }
        return retVal;
    }

    private static long getClassId(final String clazzName) {
        long id = ClassNameLocalServiceUtil.getClassNameId(clazzName);
        return id;
    }
}
