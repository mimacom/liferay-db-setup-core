# Liferay Portal DB Setup core
Library that allows to setup a number of Liferay artifacts in the DB. It uses xml configuration and Liferay APIs to add all configured artifacts.

# Usage
## Setup
We didn't publish binary yet so you'll need to build the jar yourself. Here are the steps to do it:

1. Download sources.
1. Install Maven 3.x.
1. cd <code>db-setup-core</code>
1. run <code>mvn clean install</code>
1. grab jar from <code>db-setup-core/target</code> or use as a dependency in your maven project
```xml
<dependency>
    <groupId>com.mimacom.liferay</groupId>
    <artifactId>db-setup-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Integration
Run <code>com.mimacom.liferay.portal.setup.LiferaySetup#setup(java.io.File)</code> with following xml configuration:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<setup xmlns="http://www.mimacom.com/liferay/setup">
    <configuration>
        <runasuser>test@liferay.com</runasuser>
    </configuration>
 
    <!--
    This will add new custom field that can be used in theme to control if ads should display on
    particular page.
    -->
    <customFields>
        <field name="showAds" type="boolean" className="com.liferay.portal.model.Layout">
            <role-permission role-name="Guest" permission="view"/>
        </field>
    </customFields>
</setup>
```

# Compatibility
Liferay Portal CE/EE 6.2.x
