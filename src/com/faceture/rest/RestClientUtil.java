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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Helper for the RestClient
 */
public class RestClientUtil {

    // dependencies
    private HttpClientFactory httpClientFactory;
    private HttpUtil httpUtil;
    private RestResponseFactory restResponseFactory;

    public RestClientUtil(HttpClientFactory httpClientFactory, HttpUtil httpUtil,
                          RestResponseFactory restResponseFactory)
    {
        if (null == httpClientFactory) {
            throw new IllegalArgumentException("httpClientFactory is null");
        }
        if (null == httpUtil) {
            throw new IllegalArgumentException("httpUtil is null");
        }
        if (null == restResponseFactory) {
            throw new IllegalArgumentException("restResponseFactory is null");
        }

        this.httpClientFactory = httpClientFactory;
        this.httpUtil = httpUtil;
        this.restResponseFactory = restResponseFactory;
    }

    public RestResponse doRequest(HttpRequestBase httpRequest, boolean https, String hostName, String path,
                            Map<String, String> queryParams, Map<String, String> httpHeaders,
                            Map<String, String> cookies) throws URISyntaxException, IOException
    {
        if (null == httpRequest) {
            throw new IllegalArgumentException("httpRequest is null");
        }
        if (null == hostName || hostName.isEmpty()) {
            throw new IllegalArgumentException("hostName is null or empty");
        }
        if (null == path || path.isEmpty()) {
            throw new IllegalArgumentException("path is null or empty");
        }

        // set the headers -- optional
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            httpUtil.setHeaders(httpRequest, httpHeaders);
        }
        httpRequest.setHeader("User-Agent", "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:25.0) Gecko/20100101 Firefox/25.0");
        //httpRequest.setHeader("X-Device-ID", "af5be68f72681ac1");
        //"X-Device-ID", deviceId
        // get the query string -- optional
        String queryString = null;
        if (queryParams != null && !queryParams.isEmpty()) {
            queryString = httpUtil.getQueryString(queryParams);
        }

        // set the URI
        httpUtil.setUri(httpRequest, https, hostName, path, queryString);

        // get the HTTP Client
        DefaultHttpClient httpClient = httpClientFactory.createHttpClient();

        // set the cookies -- optional
        if (cookies != null && !cookies.isEmpty()) {
            httpUtil.setCookies(httpRequest, cookies);
        }

        HttpResponse httpResponse = httpUtil.execute(httpClient, httpRequest);

        String responseBody = httpUtil.getResponseString(httpResponse);

        int statusCode = httpResponse.getStatusLine().getStatusCode();

        // get the cookies returned in the response
        cookies = httpUtil.getCookies(httpClient);

        // get the headers returned in the response
        httpHeaders = httpUtil.getHeaders(httpResponse);

        // return the response
        return restResponseFactory.create(statusCode, cookies, httpHeaders, responseBody);
    }


}
