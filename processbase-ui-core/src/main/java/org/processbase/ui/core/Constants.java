/**
 * Copyright (C) 2011 PROCESSBASE Ltd.
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.processbase.ui.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * @author mgubaidullin
 */
public class Constants {

    public static final String ACTION_ACCEPT = "ACTION_ACCEPT";
    public static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_RETURN = "ACTION_RETURN";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_OPEN = "ACTION_OPEN";
    public static final String ACTION_SUSPEND = "ACTION_SUSPEND";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_ADD = "ACTION_ADD";
    public static final String ACTION_DELETE = "ACTION_DELETE";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    public static final String ACTION_EDIT_PROCESSES = "ACTION_EDIT_PROCESSES";
    public static final String ACTION_ADD_UI = "ACTION_ADD_UI";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_RESTART = "ACTION_RESTART";
    public static final String ACTION_DELETE_PROCESS_AND_INSTANCES = "ACTION_DELETE_PROCESS_AND_INSTANCES";
    public static final String ACTION_DELETE_INSTANCES = "ACTION_DELETE_INSTANCES";
    public static final String ACTION_DELETE_PROCESS_INSTANCE = "ACTION_DELETE_PROCESS_INSTANCE";
    public static boolean LOADED = false;
    public static String TASKLIST_PAGE_URL;
    public static String CUSTOM_UI_JAR_PATH;
    public static Properties properties = new Properties();
    public static Hashtable BONITA_EJB_ENV = new Hashtable();
    public static String DL_GROUP = null;
    public static String BAM_DB_POOLNAME;
    public static String BAM_DB_DIALECT;
    public static String BONITA_DOMAIN = "default";
    public static String APP_SERVER = "default";

    public static void loadConstants() {
        try {
            File file = null;
            String userHomeDir = System.getProperty("BONITA_HOME");
            file = new File(userHomeDir + "/processbase3.properties");//global configuration can be accessed %USER_HOME%\processbase3.properties
            if (file.exists()) {
                load(file);
            } else {
                save(file);
                load(file);
            }
            LOADED = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void load(File file) throws FileNotFoundException, IOException {

        FileInputStream fis = new FileInputStream(file);
        if (properties == null) {
            properties = new Properties();
        }
        properties.loadFromXML(fis);
        fis.close();
        TASKLIST_PAGE_URL = properties.getProperty("TASKLIST_PAGE_URL");
        System.setProperty("org.ow2.bonita.api-type", properties.containsKey("org.ow2.bonita.api-type") ? properties.getProperty("org.ow2.bonita.api-type") : "EJB3");

        DL_GROUP = properties.containsKey("DL_GROUP") ? properties.getProperty("DL_GROUP") : "DOCUMENTS";
        BONITA_DOMAIN = properties.containsKey("BONITA_DOMAIN") ? properties.getProperty("BONITA_DOMAIN") : "default";
        APP_SERVER = properties.containsKey("APP_SERVER") ? properties.getProperty("APP_SERVER") : "GLASSFISH3";

        if (APP_SERVER.startsWith("GLASSFISH")) {
            BONITA_EJB_ENV.put("org.omg.CORBA.ORBInitialHost", properties.containsKey("org.omg.CORBA.ORBInitialHost") ? properties.getProperty("org.omg.CORBA.ORBInitialHost") : "localhost");
            BONITA_EJB_ENV.put("org.omg.CORBA.ORBInitialPort", properties.containsKey("org.omg.CORBA.ORBInitialPort") ? properties.getProperty("org.omg.CORBA.ORBInitialPort") : "23700");
        }
        BONITA_EJB_ENV.put("org.ow2.bonita.api-type", properties.containsKey("org.ow2.bonita.api-type") ? properties.getProperty("org.ow2.bonita.api-type") : "EJB3");

        if (!APP_SERVER.startsWith("TOMCAT")) {
            BONITA_EJB_ENV.put("java.naming.factory.initial", properties.getProperty("java.naming.factory.initial"));
            BONITA_EJB_ENV.put("java.naming.factory.url.pkgs", properties.getProperty("java.naming.factory.url.pkgs"));
            BONITA_EJB_ENV.put("java.naming.factory.state", properties.containsKey("java.naming.factory.state") ? properties.getProperty("java.naming.factory.state") : "");
            BONITA_EJB_ENV.put("java.naming.provider.url", properties.getProperty("java.naming.provider.bonitaurl"));
            BONITA_EJB_ENV.put("java.security.auth.login.config", properties.getProperty("java.security.auth.login.config"));
        }

        BAM_DB_POOLNAME = properties.containsKey("BAM_DB_POOLNAME") ? properties.getProperty("BAM_DB_POOLNAME") : "jdbc/pbbam";
        BAM_DB_DIALECT = properties.containsKey("BAM_DB_DIALECT") ? properties.getProperty("BAM_DB_DIALECT") : "org.hibernate.dialect.Oracle10gDialect";

        CUSTOM_UI_JAR_PATH = properties.containsKey("CUSTOM_UI_JAR_PATH") ? properties.getProperty("CUSTOM_UI_JAR_PATH") : "";
    }

    private static void save(File file) throws FileNotFoundException, IOException {
//        properties.setProperty("APP_SERVER", "GLASSFISH3");
        properties.setProperty("APP_SERVER", "TOMCAT");
        properties.setProperty("org.ow2.bonita.api-type", "Standard");
        properties.setProperty("TASKLIST_PAGE_URL", "/web/guest/processbase");
        properties.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
        properties.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
        properties.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        properties.setProperty("java.naming.provider.bonitaurl", "iiop://localhost:23700");
        properties.setProperty("java.security.auth.login.config", "appclientlogin.conf");
        properties.setProperty("DL_GROUP", "DOCUMENTS");
        properties.setProperty("BONITA_DOMAIN", "default");

        properties.setProperty("BAM_DB_POOLNAME", "jdbc/pbbam");
        properties.setProperty("BAM_DB_DIALECT", "org.hibernate.dialect.Oracle10gDialect");
        properties.setProperty("CUSTOM_UI_JAR_PATH", "/processbasecustomuijar");

        properties.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        properties.setProperty("org.omg.CORBA.ORBInitialPort", "23700");

        FileOutputStream fos = new FileOutputStream(file);
        properties.storeToXML(fos, null);
        fos.close();
    }
}
