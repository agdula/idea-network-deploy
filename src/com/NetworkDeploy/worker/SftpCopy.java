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

package com.NetworkDeploy.worker;

import com.NetworkDeploy.AbstractCopy;
import com.NetworkDeploy.NetworkDeployException;
import com.NetworkDeploy.config.Config;
import com.NetworkDeploy.ui.SshUserInfo;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class SftpCopy extends AbstractCopy {
    private String user;
    private String host;
    private String path;
    private String sourceFilename;

    private JSch jsch;
    private Config config;

    public boolean setDestination(String destination, String sourceFilename) {
        this.sourceFilename = sourceFilename;

        int index = destination.indexOf(':');
        if (index<0) return false;
        path = destination.substring(index+1);

        String rest = destination.substring(0, index);
        index = rest.lastIndexOf('@');
        if (index<0) return false;
        user = rest.substring(0, index);
        host = rest.substring(index+1);
        if (path.isEmpty() || path.endsWith("/")) path += sourceFilename;

        return true;
    }

    public void copy(byte[] source) throws NetworkDeployException {
        prepare();

        Session session;
        ChannelSftp sftp;
        try {
            session = jsch.getSession(user, host);
            session.setUserInfo(new SshUserInfo(project));
            session.connect(config.sftpConnectTimeout);

            Channel channel = session.openChannel("sftp");
            channel.connect(config.sftpConnectTimeout);
            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
            throw new NetworkDeployException("Can't open sftp session", e);
        }

        OutputStream os;
        try {
            try {
                os = sftp.put(path);
            } catch (SftpException e) {
                if (e.getMessage().endsWith("is a directory")) {
                    path = path + "/" + sourceFilename;
                    os = sftp.put(path);
                } else throw e;
            }
        } catch (SftpException e) {
            throw new NetworkDeployException("Can't open '" + path + "'", e);
        }

        try {
            os.write(source);
        } catch (IOException e) {
            throw new NetworkDeployException("Error occurred while writing to file", e);
        }

        sftp.exit();
        session.disconnect();
    }

    private void prepare() throws NetworkDeployException {
        config = Config.getInstance();
        jsch = new JSch();

        File identityFile = config.newFile(config.rsaIdentityFilname);
        if (identityFile.exists()) {
            try {
                jsch.addIdentity(identityFile.getAbsolutePath());
            } catch (JSchException e) {
                throw new NetworkDeployException("Can't add identity key '" + identityFile.getPath() + "'", e);
            }
        } else {
            if (!config.rsaIdentityFilname.equals(Config.RSA_IDENTITY_FILENAME))
                throw new NetworkDeployException("Identity key '" + identityFile.getPath() + "' doesn't exist");
        }

        File knownHostsFile = config.newFile(config.knownHostsFilename);
        if (knownHostsFile.exists()) {
            try {
                jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
            } catch (JSchException e) {
                throw new NetworkDeployException("Can't set known hosts file '" + knownHostsFile.getPath() + "'", e);
            }
        } else {
            if (!config.knownHostsFilename.equals(Config.KNOWN_HOSTS_FILENAME))
                throw new NetworkDeployException("knownHosts key '" + knownHostsFile.getPath() + "' doesn't exist");
        }
    }
}
