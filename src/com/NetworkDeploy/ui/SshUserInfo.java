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

package com.NetworkDeploy.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jcraft.jsch.UserInfo;

import java.util.HashMap;
import java.util.Map;

public class SshUserInfo implements UserInfo {
    private Project project;
    private Map<Fields, Object> result = new HashMap<Fields, Object>();

    private enum Fields {
        Password,
        Passphrase,
        Answer
    }

    public SshUserInfo(Project project) {
        this.project = project;
    }

    @Override
    public String getPassphrase() {
        return (String) result.get(Fields.Passphrase);
    }

    @Override
    public String getPassword() {
        return (String) result.get(Fields.Password);
    }

    @Override
    public boolean promptPassword(final String s) {
        result.remove(Fields.Password);
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                result.put(Fields.Password, Messages.showPasswordDialog(project, s, "", null));
            }
        }, ModalityState.defaultModalityState());
        return result.get(Fields.Password)!=null;
    }

    @Override
    public boolean promptPassphrase(final String s) {
        result.remove(Fields.Passphrase);
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                result.put(Fields.Passphrase, Messages.showPasswordDialog(project, s, "", null));
            }
        }, ModalityState.defaultModalityState());
        return result.get(Fields.Passphrase)!=null;
    }

    @Override
    public boolean promptYesNo(final String s) {
        result.remove(Fields.Answer);
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                result.put(Fields.Answer, Messages.showYesNoDialog(project, s, "", null));
            }
        }, ModalityState.defaultModalityState());
        Integer answer = (Integer) result.get(Fields.Answer);
        return answer==Messages.YES;
    }

    @Override
    public void showMessage(String s) {
        System.out.println(s);
    }
}
