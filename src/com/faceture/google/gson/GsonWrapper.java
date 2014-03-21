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

package com.faceture.google.gson;

import com.google.gson.Gson;

/**
 * Wrapper for Gson. This class exists to make unit testing possible. Gson is a final class, so Mockito can't mock it.
 */
public class GsonWrapper {

    private Gson gson;

    public GsonWrapper(Gson gson) {
        if (null == gson) {
            throw new IllegalArgumentException("gson is null");
        }
        this.gson = gson;
    }

    public String toJson(Object obj) {
        if (null == obj) {
            throw new IllegalArgumentException("obj is null");
        }

        return gson.toJson(obj);
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        if (null == json || json.isEmpty()) {
            throw new IllegalArgumentException("json is null or empty");
        }
        if (null == classOfT) {
            throw new IllegalArgumentException("classOfT is null");
        }

        return gson.fromJson(json, classOfT);
    }
}
