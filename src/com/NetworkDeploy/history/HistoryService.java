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

package com.NetworkDeploy.history;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@State(
        name = "NetworkDeploy.HistoryService",
        storages = @com.intellij.openapi.components.Storage(id="other", file = StoragePathMacros.APP_CONFIG + "/NetworkDeployHistory.xml")
)
public class HistoryService implements PersistentStateComponent<HistoryService> {
    public Map<String,List<String>> fileHistory;

    public HistoryService() {
        fileHistory = new TreeMap<String, List<String>>();
    }

    public static List<String> get(VirtualFile file) {
        return getInstance().fileHistory.get(file.getCanonicalPath());
    }

    public static void put(VirtualFile file, List<String> value) {
        getInstance().fileHistory.put(file.getCanonicalPath(), value);
    }

    @Transient
    private static HistoryService instance;

    public static HistoryService getInstance() {
        if (instance==null) {
            instance = ServiceManager.getService(HistoryService.class);
        }
        return instance;
    }

    @Override
    public HistoryService getState() {
        return this;
    }

    @Override
    public void loadState(HistoryService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
