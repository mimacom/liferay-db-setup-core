# Liferay Portal DB Setup core [![Build Status](https://travis-ci.org/mimacom/liferay-db-setup-core.svg?branch=master)](https://travis-ci.org/mimacom/liferay-db-setup-core) [![Gitter chat](https://badges.gitter.im/mimacom/liferay-db-setup-core.png)](https://gitter.im/mimacom/liferay-db-setup-core)
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
    <version>1.0.1-SNAPSHOT</version>
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
When adding the library to a liferay OSGI module it will be necessary to specify the dependency into the bnd file:
```java
Include-Resource: @db-setup-core-1.0.1-SNAPSHOT.jar
```
the name of the file is the same that you will find inside the maven/gradle repository.

# Compatibility
Liferay Portal CE/EE 7.0.x
