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

import com.faceture.google.GoogleUtil;
import com.faceture.google.gson.GsonWrapper;
import com.faceture.google.play.domain.PlayDomainFactory;
import com.faceture.http.HttpClientFactory;
import com.faceture.http.HttpUtil;
import com.faceture.rest.RestClient;
import com.faceture.rest.RestClientUtil;
import com.faceture.rest.RestResponseFactory;
import com.google.gson.Gson;

/**
 * Builds PlayClients
 */
public class PlayClientBuilder {

    public PlayClient create() {
        // create all of the dependencies
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        HttpUtil httpUtil = new HttpUtil(httpClientFactory);
        RestResponseFactory restResponseFactory = new RestResponseFactory();
        RestClientUtil restClientUtil = new RestClientUtil(httpClientFactory, httpUtil, restResponseFactory);

        RestClient restClient = new RestClient(httpClientFactory, httpUtil, restClientUtil);
        PlaySessionFactory playSessionFactory = new PlaySessionFactory();
        GoogleUtil googleUtil = new GoogleUtil();
        Gson gson = new Gson();
        GsonWrapper gsonWrapper = new GsonWrapper(gson);
        PlayDomainFactory playDomainFactory = new PlayDomainFactory();
        LoginResponseFactory loginResponseFactory = new LoginResponseFactory();

        // create the PlayClient
        PlayClient playClient = new PlayClient(restClient, playSessionFactory, googleUtil,
                      gsonWrapper, playDomainFactory, loginResponseFactory);

        return playClient;
    }
}
