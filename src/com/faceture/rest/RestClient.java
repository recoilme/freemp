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

package com.faceture.rest;

import com.faceture.http.HttpClientFactory;
import com.faceture.http.HttpUtil;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Utility class for doing RESTful calls
 */
public class RestClient {

    private HttpClientFactory httpClientFactory;
    private HttpUtil httpUtil;
    private RestClientUtil restClientUtil;

    public RestClient(HttpClientFactory httpClientFactory, HttpUtil httpUtil, RestClientUtil restClientUtil) {
        if (null == httpClientFactory) {
            throw new IllegalArgumentException("httpClientFactory is null");
        }
        if (null == httpUtil) {
            throw new IllegalArgumentException("httpUtil is null");
        }
        if (null == restClientUtil) {
            throw new IllegalArgumentException("restClientUtil is null");
        }

        this.httpClientFactory = httpClientFactory;
        this.httpUtil = httpUtil;
        this.restClientUtil = restClientUtil;
    }

    public RestResponse doPost(boolean https, String hostName, String path, Map<String, String> queryParams,
                         Map<String, String> httpHeaders, Map<String, String> cookies, Map<String, String> formFields)
            throws URISyntaxException, IOException
    {
        if (null == hostName || hostName.isEmpty()) {
            throw new IllegalArgumentException("hostName is null or empty");
        }
        if (null == path || path.isEmpty()) {
            throw new IllegalArgumentException("path is null or empty");
        }

        // create our POST request
        HttpPost httpPost = httpClientFactory.createHttpPost();

        // set the form data -- optional
        if (formFields != null && !formFields.isEmpty()) {
            httpUtil.setFormData(httpPost, formFields);
        }

        RestResponse restResponse = restClientUtil.doRequest(httpPost, https, hostName, path, queryParams, httpHeaders, cookies);

        return restResponse;
    }

    public RestResponse doGet(boolean https, String hostName, String path, Map<String, String> queryParams,
                        Map<String, String> httpHeaders, Map<String, String> cookies) throws IOException, URISyntaxException
    {
        if (null == hostName || hostName.isEmpty()) {
            throw new IllegalArgumentException("hostName is null or empty");
        }
        if (null == path || path.isEmpty()) {
            throw new IllegalArgumentException("path is null or empty");
        }

        HttpGet httpGet = httpClientFactory.createHttpGet();

        RestResponse restResponse = restClientUtil.doRequest(httpGet, https, hostName, path, queryParams, httpHeaders, cookies);

        return restResponse;
    }


}
