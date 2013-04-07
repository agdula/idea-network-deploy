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

import com.NetworkDeploy.config.Config;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.util.*;

public class DestinationHistory {
    private static File historyFolder;
    private File historyFile;
    private VirtualFile virtualFile;

    private List<String> destinations;
    private static Map<VirtualFile, String> lastChoices = new HashMap<VirtualFile, String>();

    public static void prepare() throws NetworkDeployException{
        if (!Config.getInstance().useDestinationHistory) return;
        historyFolder = Config.getInstance().historyDir;

        if (historyFolder.exists()) {
            if (!historyFolder.isDirectory()) throw new NetworkDeployException("Can't create history directory");
        } else {
            if (!historyFolder.mkdir()) throw new NetworkDeployException("Can't create history directory");
        }
    }

    public DestinationHistory(VirtualFile virtualFile) throws NetworkDeployException {
        this.virtualFile = virtualFile;
        destinations = new ArrayList<String>();
        if (!Config.getInstance().useDestinationHistory) return;

        historyFile = new File(historyFolder, virtualFile.getName());
        if (historyFile.exists()) {
            try {
                Scanner scanner = new Scanner(historyFile);
                while (scanner.hasNext()) {
                    String destination = scanner.next().trim();
                    destinations.add(destination);
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save() throws NetworkDeployException {
        if (destinations.indexOf(lastChoices.get(virtualFile))==0) return;
        destinations = newList(destinations, lastChoices.get(virtualFile));
        if (!Config.getInstance().useDestinationHistory) return;

        OutputStream os = null;
        try {
            //noinspection ResultOfMethodCallIgnored
            historyFile.createNewFile();
            os = new FileOutputStream(historyFile);
            for (String next : destinations) {
                os.write((next+"\n").getBytes());
            }
            os.close();
        } catch (FileNotFoundException e) {
            throw new NetworkDeployException("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (os!=null) os.close();
            } catch (IOException exp) {
                e.printStackTrace();
            }
            throw new NetworkDeployException("Can't save history");
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
