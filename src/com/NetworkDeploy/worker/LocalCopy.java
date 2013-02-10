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

public class LocalCopy implements AbstractCopy {
    private File file;

    public boolean isValidDestination(String destination) {
        File file = new File(destination);

        try {
            file.getCanonicalPath();
        } catch (IOException e) {
            return false;
        }

        return file.isAbsolute();
    }

    public void copy(byte[] source, String destination) throws NetworkDeployException {
        file = new File(destination);
        if (file.exists()) {
            if (file.isFile()) {
                if (file.canWrite()) {
                    copy(source);
                    return;
                } else throw new NetworkDeployException("You don't have write access to '"+destination+"'");
            } else throw new NetworkDeployException("'"+destination+"' is not a regular file");
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new NetworkDeployException("Can't create file '"+destination+"'");
        }
        copy(source);
    }

    private void copy(byte[] source) throws NetworkDeployException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(source);
            os.close();
        } catch (FileNotFoundException e) {
            throw new NetworkDeployException("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                os.close();
            } catch (IOException exp) {
                e.printStackTrace();
            }
            throw new NetworkDeployException("Error occurred while writing to file");
        }
    }
}
