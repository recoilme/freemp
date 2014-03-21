/*
 * Copyright (c) 2012. Faceture Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.faceture.google.play;

/**
 * The result of a login attempt
 */
public enum LoginResult {

    SUCCESS("Success"),
    BAD_CREDENTIALS("Bad Credentials"),
    FAILURE("Failure");

    private final String value;

    LoginResult(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public String getKey() {
        return name();
    }
}
