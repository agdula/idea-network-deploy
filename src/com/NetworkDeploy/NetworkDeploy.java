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

import com.NetworkDeploy.worker.LocalCopy;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

public class NetworkDeploy extends AnAction {
    private final static AbstractCopy[] workers = {new LocalCopy()};

    public void actionPerformed(AnActionEvent event) {
        try {
            Config.prepare();
            doAction(event);
        } catch (NetworkDeployException e) {
            notify(e.getMessage(), NotificationType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            notify(errorMessage, NotificationType.ERROR);
        }
    }

    private void doAction(AnActionEvent event) throws NetworkDeployException {
        VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file ==null || file.isDirectory()) return;

        Project project = event.getData(PlatformDataKeys.PROJECT);
        String destination = Messages.showInputDialog(project, "Where to save '"+ file.getName()+"'?", "", Messages.getQuestionIcon());
        if (destination==null) return;
        destination = destination.trim();

        if (destination.length()!=0) {
            for (AbstractCopy worker : workers) {
                if (worker.isValidDestination(destination)) {
                    Document document = FileDocumentManager.getInstance().getDocument(file);
                    assert document != null;
                    FileDocumentManager.getInstance().saveDocument(document);

                    copy(worker, file, destination);
                    return;
                }
            }
        }
        notify("Destination '"+destination+"' is not valid", NotificationType.ERROR);
    }

    private void copy(final AbstractCopy worker, VirtualFile file, final String destination) throws NetworkDeployException {
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
                    worker.copy(source, destination);
                    NetworkDeploy.notify("File is copied successfully", NotificationType.INFORMATION);
                } catch (NetworkDeployException e) {
                    NetworkDeploy.notify(e.getMessage(), NotificationType.ERROR);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                    NetworkDeploy.notify(errorMessage, NotificationType.ERROR);
                }
            }
        }.start();
    }

    private static void notify(String message, NotificationType type) {
        Notifications.Bus.notify(new Notification("NetworkDeploy", "Network Deploy", message, type));
    }
}