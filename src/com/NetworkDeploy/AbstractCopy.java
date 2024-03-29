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

import com.intellij.openapi.project.Project;

public abstract class AbstractCopy {
    protected Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    public abstract boolean setDestination(String destination, String sourceFilename);

    public abstract String getDestination();

    public abstract boolean isDirectory();

    public abstract void copy(byte[] source) throws NetworkDeployException;
}
