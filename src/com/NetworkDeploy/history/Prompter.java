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

public class Prompter {
    private static Map<String, Prompter> instances = new TreeMap<String, Prompter>();

    public static Prompter getInstance(VirtualFile file) {
        String key = file.getCanonicalPath();
        if (!instances.containsKey(key)) {
            instances.put(key, new Prompter(file));
        }
        return instances.get(key);
    }

    private String lastChoice;
    private VirtualFile file;

    private Prompter(VirtualFile file) {
        this.file = file;
    }

    public List<String> getOptions() {
        List<String> options = new ArrayList<String>();
        if (lastChoice!=null) {
            options.add(lastChoice);
        }

        if (Config.getInstance().useDestinationHistory) {
            //noinspection unchecked
            options = combineLists(options, HistoryService.get(file), HistoryService.get(file.getParent()));
        }
        return options;
    }

    public void save(String option, boolean isDirectory) {
        lastChoice = null;
        VirtualFile key = isDirectory ? file.getParent() : file;

        if (Config.getInstance().useDestinationHistory) {
            //noinspection unchecked
            List<String> options = combineLists(Arrays.asList(option), getOptions());
            HistoryService.put(key, options);
        }
    }

    public void setLastChoice(String lastChoice) {
        this.lastChoice = lastChoice;
    }

    @SafeVarargs
    private static List<String> combineLists(List<String>... lists) {
        List<String> result = new ArrayList<String>();
        for (List<String> list : lists) {
            if (list != null) {
                for (String str : list) {
                    if (str!=null && !result.contains(str)) {
                        result.add(str);
                    }
                }
            }
        }
        return result;
    }
}
