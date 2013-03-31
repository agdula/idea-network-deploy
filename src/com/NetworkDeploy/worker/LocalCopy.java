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

package com.NetworkDeploy.worker;

import com.NetworkDeploy.AbstractCopy;
import com.NetworkDeploy.NetworkDeployException;

import java.io.*;

public class LocalCopy extends AbstractCopy {
    private File file;

    public boolean setDestination(String destination, String sourceFilename) {
        File dest = new File(destination);

        try {
            //noinspection ResultOfMethodCallIgnored
            dest.getCanonicalPath();
        } catch (IOException e) {
            return false;
        }

        if (!dest.isAbsolute()) return false;

        file = dest.isDirectory() ? new File(dest, sourceFilename) : dest;
        return true;
    }

    public void copy(byte[] source) throws NetworkDeployException {
        if (file.exists()) {
            if (file.isFile()) {
                if (file.canWrite()) {
                    doCopy(source);
                    return;
                } else throw new NetworkDeployException("Can't open '"+file.getAbsolutePath()+"'", "Permission denied");
            } else throw new NetworkDeployException("'"+file.getAbsolutePath()+"' is not a regular file");
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            throw new NetworkDeployException("Can't create '"+file.getAbsolutePath()+"'", e);
        }
        doCopy(source);
    }

    private void doCopy(byte[] source) throws NetworkDeployException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(source);
            os.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                os.close();
            } catch (IOException exp) {
                e.printStackTrace();
            }
            throw new NetworkDeployException("Error occurred while writing to file", e);
        }
    }
}
