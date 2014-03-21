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

package com.faceture.google.play.domain;

/**
 * Creates Play domain objects
 */
public class PlayDomainFactory {

    public SearchRequest createSearchRequest(String q) {
        if (null == q || q.isEmpty()) {
            throw new IllegalArgumentException("q is null or empty");
        }

        return new SearchRequest(q);
    }

    public LoadAllTracksRequest createLoadAllTracksRequest() {
        return new LoadAllTracksRequest();
    }

    public LoadAllTracksRequest createLoadAllTracksRequest(String continuationToken) {
        return new LoadAllTracksRequest(continuationToken);
    }
}
