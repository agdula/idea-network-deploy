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
import com.NetworkDeploy.ui.SshUserInfo;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.OutputStream;

public class SftpCopy extends AbstractCopy {
    public static int CONNECT_TIMEOUT = 1000;

    private String user;
    private String host;
    private String path;
    private String sourceFilename;

    private JSch jsch;

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
            session = connect(jsch, host, user);
            if (session==null) throw new NetworkDeployException("Wrong password");

            Channel channel = session.openChannel("sftp");
            channel.connect(CONNECT_TIMEOUT);
            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
            throw new NetworkDeployException("Can't open sftp session");
        }

        try {
            OutputStream os;
            try {
                os = sftp.put(path);
            } catch (SftpException e) {
                if (e.getMessage().endsWith("is a directory")) {
                    os = sftp.put(path + "/" + sourceFilename);
                } else throw e;
            }
            os.write(source);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NetworkDeployException("Can't save file: " + e.getMessage());
        }

        sftp.exit();
        session.disconnect();
    }

    private void prepare() throws NetworkDeployException {
        File homeFolder = new File(System.getProperty("user.home"));
        File identityFile = new File(homeFolder, ".ssh/id_rsa");
        File knownHostsFile = new File(homeFolder, ".ssh/known_hosts");

        if (!identityFile.exists()) throw new NetworkDeployException("Can't find identity file");
        if (!knownHostsFile.exists()) throw new NetworkDeployException("Can't find known hosts file");

        jsch = new JSch();
        try {
            jsch.addIdentity(identityFile.getAbsolutePath());
        } catch (JSchException e) {
            e.printStackTrace();
            throw new NetworkDeployException("Can't add private key");
        }

        try {
            jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
        } catch (JSchException e) {
            e.printStackTrace();
            throw new NetworkDeployException("Can't add known hosts");
        }
    }

    private Session connect(JSch jsch, String host, String user) throws JSchException {
        Session session = jsch.getSession(user, host);
        session.setUserInfo(new SshUserInfo(project));

        try {
            session.connect(CONNECT_TIMEOUT);
        } catch (JSchException e) {
            if (e.getMessage().equals("Auth fail")) {
                session.disconnect();
                return null;
            }
            throw e;
        }
        return session;
    }
}
