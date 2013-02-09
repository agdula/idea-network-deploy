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
            doAction(event);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            Notifications.Bus.notify(new Notification("NetworkDeploy", "Network Deploy Exception", errorMessage, NotificationType.ERROR));
            e.printStackTrace();
        }
    }

    private void doAction(AnActionEvent event) throws Exception{
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
        Notifications.Bus.notify(new Notification("NetworkDeploy", "Network Deploy", "Destination '"+destination+"' is not valid", NotificationType.WARNING));
    }

    private void copy(final AbstractCopy worker, VirtualFile file, final String destination) throws IOException {
        final byte[] source = file.contentsToByteArray();

        new Thread() {
            public void run() {
                String error = worker.copy(source, destination);
                if (error==null) Notifications.Bus.notify(new Notification("NetworkDeploy", "Network Deploy", "File is copied successfully", NotificationType.INFORMATION));
                else Notifications.Bus.notify(new Notification("NetworkDeploy", "Network Deploy", error, NotificationType.ERROR));
            }
        }.start();
    }

}