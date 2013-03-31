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

public class NetworkDeployException extends Exception {
    private String extended;

    public NetworkDeployException(String message) {
        super(message);
    }

    public NetworkDeployException(String message, String extended) {
        super(message);
        this.extended = extended;
    }

    public NetworkDeployException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkDeployException(Throwable cause) {
        super(cause);
    }

    public String getExtended() {
        return extended;
    }
}
