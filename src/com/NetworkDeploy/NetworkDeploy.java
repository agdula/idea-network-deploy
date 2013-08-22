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

import com.NetworkDeploy.history.Prompter;
import com.NetworkDeploy.ui.EditChooseDialog;
import com.NetworkDeploy.worker.LocalCopy;
import com.NetworkDeploy.worker.SftpCopy;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.List;

public class NetworkDeploy extends AnAction {
    private final static Class[] workers = {SftpCopy.class, LocalCopy.class};

    public void actionPerformed(AnActionEvent event) {
        try {
            doAction(event);
        } catch (NetworkDeployException e) {
            notify(e);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            notify(errorMessage, NotificationType.ERROR);
        }
    }

    private void doAction(AnActionEvent event) throws NetworkDeployException {
        VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file ==null || file.isDirectory()) return;

        Prompter prompter = Prompter.getInstance(file);
        Project project = event.getData(PlatformDataKeys.PROJECT);
        List<String> options = prompter.getOptions();
        String destination = EditChooseDialog.showDialog(project, "Where to save '"+ file.getName()+"'?", options);
        if (destination==null) return;
        destination = destination.trim();
        prompter.setLastChoice(destination);

        if (destination.length()!=0) {
            for (Class workerClass : workers) {
                AbstractCopy worker;
                try {
                    worker = (AbstractCopy) workerClass.newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (worker.setDestination(destination, file.getName())) {
                    Document document = FileDocumentManager.getInstance().getDocument(file);
                    assert document != null;
                    FileDocumentManager.getInstance().saveDocument(document);

                    worker.setProject(project);
                    copy(worker, file, prompter);
                    return;
                }
            }
        }
        notify("'"+destination+"' is not a valid destination", NotificationType.ERROR);
    }

    private void copy(final AbstractCopy worker, VirtualFile file, final Prompter prompter) throws NetworkDeployException {
        byte[] buffer;
        try {
            buffer = file.contentsToByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new NetworkDeployException("Can't get file's content");
        }
        final byte[] source = buffer;

        new Thread() {
            public void run() {
                try {
                    worker.copy(source);
                    NetworkDeploy.notify("File is copied successfully", NotificationType.INFORMATION);
                    prompter.save(worker.getDestination(), worker.isDirectory());
                } catch (NetworkDeployException e) {
                    NetworkDeploy.notify(e);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                    NetworkDeploy.notify(errorMessage, NotificationType.ERROR);
                }
            }
        }.start();
    }

    public static void notify(String message, NotificationType type) {
        Notifications.Bus.notify(new Notification("NetworkDeploy", "Network Deploy", message, type));
    }

    public static void notify(String title, String content, NotificationType type) {
        if (content==null) notify(title, type);
        else Notifications.Bus.notify(new Notification("NetworkDeploy", title, content, type));
    }

    public static void notify(NetworkDeployException e) {
        if (e.getCause()!=null) {
            e.getCause().printStackTrace();
            notify(e.getMessage(), e.getCause().getMessage(), NotificationType.ERROR);
        } else notify(e.getMessage(), e.getExtended(), NotificationType.ERROR);
    }
}