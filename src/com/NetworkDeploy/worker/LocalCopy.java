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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

    public String copy(byte[] source, String destination) {
        file = new File(destination);
        if (file.exists()) {
            if (file.isFile()) {
                if (file.canWrite()) {
                    return copy(source);
                } else return "Don't have a write access";
            } else return "'"+destination+"' is not a regular file";
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            return "Can't create file";
        }
        return copy(source);
    }

    private String copy(byte[] source) {
        String error = null;

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(source);
        } catch (Exception e) {
            error = e.getMessage();
            if (error==null) error = e.getClass().getName();
        } finally {
            System.out.println("HERE");
            if (os!=null) try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return error;
    }
}
