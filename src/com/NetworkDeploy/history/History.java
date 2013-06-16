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

import com.NetworkDeploy.config.Config;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;

public class History {
    private VirtualFile virtualFile;

    private List<String> destinations;
    private static Map<VirtualFile, String> lastChoices = new HashMap<VirtualFile, String>();

    public History(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;

        if (Config.getInstance().useDestinationHistory) {
            destinations = HistoryService.getInstance().fileHistory.get(virtualFile.getCanonicalPath());
        }
        if (destinations==null) destinations = new ArrayList<String>();
    }

    public void save()  {
        if (destinations.indexOf(lastChoices.get(virtualFile))==0) return;
        destinations = newList(destinations, lastChoices.get(virtualFile));

        if (Config.getInstance().useDestinationHistory) {
            HistoryService.getInstance().fileHistory.put(virtualFile.getCanonicalPath(), destinations);
        }
    }

    public List<String> getDestinations() {
        return newList(destinations, lastChoices.get(virtualFile));
    }

    public void addUserChoice(String choice) {
        lastChoices.put(virtualFile, choice);
    }

    private static List<String> newList(List<String> oldList, String newItem) {
        List<String> list = new ArrayList<String>(oldList);
        if (newItem==null) return list;
        list.remove(newItem);
        list.add(0, newItem);
        return list;
    }
}
