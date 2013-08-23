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

import com.NetworkDeploy.history.HistoryService;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ConfigScreen implements Configurable {
    private Config config;

    private JCheckBox useDestinationHistoryBox;
    private JTextField knownHostsField;
    private JTextField rsaIdentityField;
    private JTextField sftpTimeoutField;

    public ConfigScreen() {
        config = Config.getInstance();

        useDestinationHistoryBox = new JCheckBox("Suggest previous destinations");

        knownHostsField = new JTextField(25);
        rsaIdentityField = new JTextField(25);

        sftpTimeoutField = new JTextField(5);
        sftpTimeoutField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == KeyEvent.VK_DELETE || c == KeyEvent.VK_BACK_SPACE) return;
                if (Character.isDigit(c)) return;
                e.consume();
            }
        });
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Network Deploy";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel border = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        border.add(main, BorderLayout.NORTH);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(useDestinationHistoryBox);
        JButton cleanHistoryBtn = new JButton();
        cleanHistoryBtn.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HistoryService.getInstance().fileHistory.clear();
                setEnabled(!HistoryService.getInstance().fileHistory.isEmpty());
            }
        });
        cleanHistoryBtn.setText("Clear History");
        cleanHistoryBtn.setEnabled(!HistoryService.getInstance().fileHistory.isEmpty());
        panel.add(cleanHistoryBtn);
        main.add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new Label());
        main.add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new Label("SFTP settings"));
        main.add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Timeout, ms"));
        panel.add(sftpTimeoutField);
        main.add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Known hosts file"));
        panel.add(knownHostsField);
        main.add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Identity file"));
        panel.add(rsaIdentityField);
        main.add(panel);

        reset();
        return border;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean isModified() {
        if (useDestinationHistoryBox.isSelected()!=config.useDestinationHistory) return true;
        if (getNewSftpTimeout()!=config.sftpConnectTimeout) return true;
        if (!knownHostsField.getText().trim().equals(config.knownHostsFilename)) return true;
        if (!rsaIdentityField.getText().trim().equals(config.rsaIdentityFilname)) return true;
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (useDestinationHistoryBox.isSelected()!=config.useDestinationHistory)
            config.useDestinationHistory = useDestinationHistoryBox.isSelected();
        if (getNewSftpTimeout()!=config.sftpConnectTimeout)
            config.sftpConnectTimeout = getNewSftpTimeout();
        if (!knownHostsField.getText().trim().equals(config.knownHostsFilename))
            config.knownHostsFilename = knownHostsField.getText().trim();
        if (!rsaIdentityField.getText().trim().equals(config.rsaIdentityFilname))
            config.rsaIdentityFilname = rsaIdentityField.getText().trim();
    }

    @Override
    public void reset() {
        useDestinationHistoryBox.setSelected(config.useDestinationHistory);
        knownHostsField.setText(config.knownHostsFilename);
        rsaIdentityField.setText(config.rsaIdentityFilname);
        sftpTimeoutField.setText(String.valueOf(config.sftpConnectTimeout));
    }

    @Override
    public void disposeUIResources() {
    }

    private int getNewSftpTimeout() {
        if (sftpTimeoutField.getText().isEmpty()) return 0;
        return Integer.parseInt(sftpTimeoutField.getText());
    }
}
