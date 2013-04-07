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

package com.NetworkDeploy.config;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;

import java.io.File;

@State(
    name = "NetworkDeploy",
    storages = @com.intellij.openapi.components.Storage(id="other", file = StoragePathMacros.APP_CONFIG + "/NetworkDeploy.xml")
)
public class Config implements PersistentStateComponent<Config> {
    public static final String KNOWN_HOSTS_FILENAME = ".ssh/known_hosts";
    public static final String RSA_IDENTITY_FILENAME = ".ssh/id_rsa";

    public boolean useDestinationHistory;
    public String historyDirName;

    public int sftpConnectTimeout;
    public String knownHostsFilename;
    public String rsaIdentityFilname;

    public Config() {
        useDestinationHistory = true;
        historyDirName = ".network_deploy_history";

        sftpConnectTimeout = 30000;
        knownHostsFilename = KNOWN_HOSTS_FILENAME;
        rsaIdentityFilname = RSA_IDENTITY_FILENAME;
    }

    @Transient
    private File userDir;

    private void init() {
        userDir = new File(System.getProperty("user.home"));
        if (!userDir.exists() || !userDir.isDirectory()) throw new RuntimeException();
    }

    public File newFile(String filename) {
        return new File(userDir, filename).getAbsoluteFile();
    }

    @Transient
    private static Config instance;

    public static Config getInstance() {
        if (instance==null) {
            instance = ServiceManager.getService(Config.class);
            instance.init();
        }
        return instance;
    }

    @Override
    public Config getState() {
        return this;
    }

    @Override
    public void loadState(Config state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
