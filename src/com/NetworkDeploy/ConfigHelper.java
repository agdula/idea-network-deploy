/*
Copyright (C) 2013 Ruslan Nugmanov

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.NetworkDeploy;

import java.io.*;
import java.util.Properties;

public abstract class ConfigHelper {
    public static void prepare() throws NetworkDeployException {
        File userFolder = new File(System.getProperty("user.home"));
        File pluginFolder = new File(userFolder, ".NetworkDeploy");

        if (pluginFolder.exists()) {
            if (!pluginFolder.isDirectory()) throw new NetworkDeployException("Can't create plugin directory");
        } else {
            if (!pluginFolder.mkdir()) throw new NetworkDeployException("Can't create plugin directory");
        }
        Config.setPluginFolder(pluginFolder);

        File configFile = new File(pluginFolder, "main.properties");
        if (configFile.exists()) {
            if (!configFile.isFile()) throw new NetworkDeployException("Config file is not a regular file");
            if (!configFile.canRead()) throw new NetworkDeployException("You don't have read access to config file");
        } else {
            byte[] buffer;
            int count;
            try {
                InputStream is = NetworkDeploy.class.getClassLoader().getResourceAsStream("com/NetworkDeploy/conf/default.properties");
                buffer = new byte[10000];
                count = is.read(buffer); // todo rewrite this
                is.close();
            } catch (Exception e) {
                throw new NetworkDeployException("Can't get default properties");
            }

            try {
                configFile.createNewFile();
                OutputStream os = new FileOutputStream(configFile);
                os.write(buffer, 0, count); // todo rewrite this
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new NetworkDeployException("Can't create config file");
            }
        }

        Properties prop = new Properties();
        InputStream is = null;

        try {
            is = new FileInputStream(configFile);
            prop.load(is);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new NetworkDeployException("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                is.close();
            } catch (IOException exp) {
                e.printStackTrace();
            }
            throw new NetworkDeployException("Can't load config");
        }

        parse(prop);
    }

    private static void parse(Properties prop) throws NetworkDeployException {
        Config.setUseDestinationHistory(getBoolean(prop, "use_destination_history", false));
    }

    private static boolean getBoolean(Properties prop, String key, boolean defaultValue) throws NetworkDeployException{
        String property = prop.getProperty(key);
        if (property==null) return defaultValue;
        property = property.trim();

        String yes[] = {"y","yes","1","t","true"};
        for (String word : yes) {
            if (property.equalsIgnoreCase(word)) return true;
        }

        String no[] = {"n","no","0","f","false"};
        for (String word : no) {
            if (property.equalsIgnoreCase(word)) return false;
        }

        throw new NetworkDeployException("Incorrect value for config property " + key);
    }
}
