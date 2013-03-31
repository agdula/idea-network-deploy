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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.util.List;

public class EditChooseDialog extends DialogWrapper{
    private String message;
    private JComboBox<String> comboBox;

    protected EditChooseDialog(Project project, String message, List<String> items) {
        super(project);
        this.message = message;

        String[] options = new String[items.size()];
        for (int i=0;i!=items.size();i++) options[i] = items.get(i);
        comboBox = new JComboBox<String>(options);
        comboBox.setEditable(true);

        super.init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(message));
        panel.add(comboBox);
        return panel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return comboBox;
    }

    public static String showDialog(Project project, String message, List<String> items) {
        EditChooseDialog dialog = new EditChooseDialog(project, message, items);
        dialog.show();
        if (dialog.getExitCode()==0) {
            return (String) dialog.comboBox.getSelectedItem();
        }
        return null;
    }
}
