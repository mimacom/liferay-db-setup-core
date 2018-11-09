package com.ableneo.liferay.portal.setup.core;

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


import com.liferay.expando.kernel.model.*;
import com.liferay.expando.kernel.service.ExpandoColumnLocalServiceUtil;
import com.liferay.expando.kernel.service.ExpandoTableLocalServiceUtil;
import com.liferay.expando.kernel.util.ExpandoBridgeFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.*;
import com.mimacom.liferay.portal.setup.domain.CustomFields;
import com.mimacom.liferay.portal.setup.domain.RolePermission;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;

public final class SetupCustomFields {

    private static final Log LOG = LogFactoryUtil.getLog(SetupCustomFields.class);
    private static final long COMPANY_ID = PortalUtil.getDefaultCompanyId();

    private SetupCustomFields() {

    }

    public static void setupExpandoFields(final List<CustomFields.Field> fields) {

        for (CustomFields.Field field : fields) {
            String className = field.getClassName();
            LOG.info("Add field " + field.getName() + "(" + className + ") to expando bridge");

            ExpandoBridge bridge = ExpandoBridgeFactoryUtil.getExpandoBridge(COMPANY_ID, className);
            addAttributeToExpandoBridge(bridge, field);
        }
    }

    /**
     * @return all expandos with types specified in the "excludeListed" List to
     *         avoid deleting all expandos in the portal!
     */
    private static List<ExpandoColumn> getAllExpandoColumns(
            final List<CustomFields.Field> customFields) {

        List<ExpandoColumn> all = new ArrayList<>();
        SortedSet<String> tables = new TreeSet<>();
        for (CustomFields.Field field : customFields) {
            ExpandoTable table;
            try {
                table = ExpandoTableLocalServiceUtil.getDefaultTable(COMPANY_ID,
                        field.getClassName());
                if (table != null && !tables.contains(table.getName())) {
                    tables.add(table.getName());
                    List<ExpandoColumn> columns = ExpandoColumnLocalServiceUtil
                            .getColumns(COMPANY_ID, field.getClassName(), table.getName());
                    all.addAll(columns);
                }
            } catch (PortalException | SystemException e) {
                LOG.error("Error in getAllExpandoColumns()." + e.getMessage());
            }
        }
        return all;
    }

    private static void addAttributeToExpandoBridge(final ExpandoBridge bridge,
            final CustomFields.Field field) {

        String name = field.getName();
        try {
            int fieldTypeKey = getFieldTypeKey(field.getType());
            if (bridge.hasAttribute(name)) {
                ExpandoColumn column = ExpandoColumnLocalServiceUtil.getColumn(COMPANY_ID,
                        bridge.getClassName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, name);
                ExpandoColumnLocalServiceUtil.updateColumn(column.getColumnId(), name, fieldTypeKey,
                        getAttributeFromString(fieldTypeKey, field.getDefaultData()));
            } else {
                bridge.addAttribute(name, fieldTypeKey,
                        getAttributeFromString(fieldTypeKey, field.getDefaultData()));
            }
            UnicodeProperties properties = bridge.getAttributeProperties(name);
            properties.setProperty(ExpandoColumnConstants.INDEX_TYPE,
                    Integer.toString(getIndexedType(field.getIndexed())));
            properties.setProperty(ExpandoColumnConstants.PROPERTY_DISPLAY_TYPE,
                    getDisplayType(field.getDisplayType()));
            bridge.setAttributeProperties(name, properties);
            setCustomFieldPermission(field.getRolePermission(), bridge, name);
        } catch (PortalException | SystemException e) {
            LOG.error("Could not set custom attribute: " + name, e);
        }
    }

    private static void setCustomFieldPermission(final List<RolePermission> rolePermissions,
            final ExpandoBridge bridge, final String fieldName) throws SystemException {

        LOG.info("Set read permissions on  field " + fieldName + " for " + rolePermissions.size()
                + " rolePermissions");
        ExpandoColumn column = ExpandoColumnLocalServiceUtil.getColumn(COMPANY_ID,
                bridge.getClassName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, fieldName);
        for (RolePermission rolePermission : rolePermissions) {
            String roleName = rolePermission.getRoleName();
            String permission = rolePermission.getPermission();
            try {
                switch (permission) {
                case "update":
                    SetupPermissions.addReadWrightRight(roleName, ExpandoColumn.class.getName(),
                            String.valueOf(column.getColumnId()));
                    LOG.info("Added update permission on field " + fieldName + " for role "
                            + roleName);
                    break;
                case "view":
                    SetupPermissions.addReadRight(roleName, ExpandoColumn.class.getName(),
                            String.valueOf(column.getColumnId()));
                    LOG.info("Added read permission on field " + fieldName + " for role "
                            + roleName);
                    break;
                default:
                    LOG.info("Unknown permission:" + permission + ". No permission added on "
                            + "field " + fieldName + " for role " + roleName);
                    break;
                }

            } catch (PortalException e) {
                LOG.error("Could not set permission to " + roleName + " on " + fieldName, e);
            }
        }
    }

    public static void deleteCustomField(final CustomFields.Field customField,
            final String deleteMethod) {
        deleteCustomFields(Arrays.asList(customField), deleteMethod);
    }

    public static void deleteCustomFields(final List<CustomFields.Field> customFields,
            final String deleteMethod) {

        if ("excludeListed".equals(deleteMethod)) {
            // delete all (from types in the list) but listed
            List<String> skipFields = attributeNamesList(customFields);
            List<ExpandoColumn> expandoColumns = getAllExpandoColumns(customFields);
            if (expandoColumns != null) {
                for (ExpandoColumn expandoColumn : expandoColumns) {
                    if (!skipFields.contains(expandoColumn.getName())) {
                        try {
                            ExpandoColumnLocalServiceUtil.deleteColumn(expandoColumn.getColumnId());
                        } catch (PortalException | SystemException e) {
                            LOG.error("Could not delete CustomField " + expandoColumn.getName(), e);
                        }
                    }
                }
            }
        } else if (deleteMethod.equals("onlyListed")) {
            for (CustomFields.Field field : customFields) {
                try {
                    ExpandoTable table = ExpandoTableLocalServiceUtil.getDefaultTable(COMPANY_ID,
                            field.getClassName());
                    ExpandoColumnLocalServiceUtil.deleteColumn(COMPANY_ID, field.getClassName(),
                            table.getName(), field.getName());
                } catch (PortalException | SystemException e) {
                    LOG.error("Could not delete Custom Field " + field.getName(), e);
                    continue;
                }
                LOG.info("custom field " + field.getName() + " deleted ");
            }
        }
    }

    private static int getFieldTypeKey(final String name) {

        if ("stringArray".equals(name)) {
            return ExpandoColumnConstants.STRING_ARRAY;
        }
        if ("string".equals(name)) {
            return ExpandoColumnConstants.STRING;
        }
        if ("int".equals(name)) {
            return ExpandoColumnConstants.INTEGER;
        }
        if ("boolean".equals(name)) {
            return ExpandoColumnConstants.BOOLEAN;
        }
        if ("date".equals(name)) {
            return ExpandoColumnConstants.DATE;
        }
        if ("long".equals(name)) {
            return ExpandoColumnConstants.LONG;
        }
        if ("double".equals(name)) {
            return ExpandoColumnConstants.DOUBLE;
        }
        if ("float".equals(name)) {
            return ExpandoColumnConstants.FLOAT;
        }
        LOG.error("bad setup name: " + name);
        return -1;
    }

    private static List<String> attributeNamesList(final List<CustomFields.Field> customFields) {

        List<String> names = new ArrayList<>();
        for (CustomFields.Field f : customFields) {
            if (f.getName() != null) {
                names.add(f.getName());
            }
        }
        return names;
    }

    private static int getIndexedType(final String indexed) {

        if ("none".equals(indexed)) {
            return ExpandoColumnConstants.INDEX_TYPE_NONE;
        } else if ("text".equals(indexed)) {
            return ExpandoColumnConstants.INDEX_TYPE_TEXT;
        } else if ("keyword".equals(indexed)) {
            return ExpandoColumnConstants.INDEX_TYPE_KEYWORD;
        } else {
            LOG.error("cannot get unknown index type: " + indexed);
            return 0;
        }
    }

    private static String getDisplayType(final String displayType) {
        if ("checkbox".equals(displayType)) {
            return ExpandoColumnConstants.PROPERTY_DISPLAY_TYPE_CHECKBOX;
        } else if ("radio".equals(displayType)) {
            return ExpandoColumnConstants.PROPERTY_DISPLAY_TYPE_RADIO;
        } else if ("selection-list".equals(displayType)) {
            return ExpandoColumnConstants.PROPERTY_DISPLAY_TYPE_SELECTION_LIST;
        } else if ("text-box".equals(displayType)) {
            return ExpandoColumnConstants.PROPERTY_DISPLAY_TYPE_TEXT_BOX;
        } else {
            LOG.error("cannot get unknown display type: " + displayType);
            return ExpandoColumnConstants.PROPERTY_DISPLAY_TYPE_TEXT_BOX;
        }
    }

    private static Map<String, CustomFields.Field> convertCustomFieldListToHashMap(
            final List<CustomFields.Field> objects) {

        HashMap<String, CustomFields.Field> map = new HashMap<>();
        for (CustomFields.Field field : objects) {
            map.put(field.getName(), field);
        }
        return map;
    }

    public static Serializable getAttributeFromString(final int type, final String attribute) {

        if (attribute == null) {
            return null;
        }

        if (type == ExpandoColumnConstants.BOOLEAN) {
            return GetterUtil.getBoolean(attribute);
        } else if (type == ExpandoColumnConstants.BOOLEAN_ARRAY) {
            return GetterUtil.getBooleanValues(StringUtil.split(attribute));
        } else if (type == ExpandoColumnConstants.DATE) {
            return GetterUtil.getDate(attribute, getDateFormat());
        } else if (type == ExpandoColumnConstants.DATE_ARRAY) {
            return GetterUtil.getDateValues(StringUtil.split(attribute), getDateFormat());
        } else if (type == ExpandoColumnConstants.DOUBLE) {
            return GetterUtil.getDouble(attribute);
        } else if (type == ExpandoColumnConstants.DOUBLE_ARRAY) {
            return GetterUtil.getDoubleValues(StringUtil.split(attribute));
        } else if (type == ExpandoColumnConstants.FLOAT) {
            return GetterUtil.getFloat(attribute);
        } else if (type == ExpandoColumnConstants.FLOAT_ARRAY) {
            return GetterUtil.getFloatValues(StringUtil.split(attribute));
        } else if (type == ExpandoColumnConstants.INTEGER) {
            return GetterUtil.getInteger(attribute);
        } else if (type == ExpandoColumnConstants.INTEGER_ARRAY) {
            return GetterUtil.getIntegerValues(StringUtil.split(attribute));
        } else if (type == ExpandoColumnConstants.LONG) {
            return GetterUtil.getLong(attribute);
        } else if (type == ExpandoColumnConstants.LONG_ARRAY) {
            return GetterUtil.getLongValues(StringUtil.split(attribute));
        } else if (type == ExpandoColumnConstants.SHORT) {
            return GetterUtil.getShort(attribute);
        } else if (type == ExpandoColumnConstants.SHORT_ARRAY) {
            return GetterUtil.getShortValues(StringUtil.split(attribute));
        } else if (type == ExpandoColumnConstants.STRING_ARRAY) {
            return StringUtil.split(attribute);
        } else {
            return attribute;
        }
    }

    private static DateFormat getDateFormat() {
        return DateUtil.getISO8601Format();
    }
}
