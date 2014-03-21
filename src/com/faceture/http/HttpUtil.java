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

package com.faceture.http;

import com.faceture.google.play.HeaderName;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP utility
 */
public class HttpUtil {

    private HttpClientFactory httpClientFactory;

    public HttpUtil(HttpClientFactory httpClientFactory) {
        if (null == httpClientFactory) {
            throw new IllegalArgumentException("httpClientFactory is null");
        }

        this.httpClientFactory = httpClientFactory;
    }

    public String getResponseString(HttpResponse httpResponse) throws IOException {
        if (null == httpResponse) {
            throw new IllegalArgumentException("httpResponse is null");
        }

        HttpEntity entity = httpResponse.getEntity();
        String responseStr = EntityUtils.toString(entity);

        return responseStr;
    }

    public String getQueryString(Map<String, String> queryParams) {
        if (null == queryParams || queryParams.isEmpty()) {
            throw new IllegalArgumentException("queryParams is null or empty");
        }

        List<NameValuePair> nvQueryParams = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
            NameValuePair nameValuePair = new BasicNameValuePair(queryParam.getKey(), queryParam.getValue());
            nvQueryParams.add(nameValuePair);
        }

        String queryString = URLEncodedUtils.format(nvQueryParams, "UTF-8");

        return queryString;
    }

    public void setUri(HttpRequestBase httpRequest, boolean https, String hostName, String path,
                       String queryString) throws URISyntaxException, MalformedURLException {
        if (null == httpRequest) {
            throw new IllegalArgumentException("httpRequest is null");
        }
        if (null == hostName || hostName.isEmpty()) {
            throw new IllegalArgumentException("hostName is null or empty");
        }
        if (null == path || path.isEmpty()) {
            throw new IllegalArgumentException("path is null or empty");
        }
        // empty query string is ok

        // figure out the scheme
        String scheme = https ? SchemeString.HTTPS : SchemeString.HTTP;

        // set the URI
        URL url;
        if (queryString != null && !queryString.isEmpty()) {
            // we have query params
            url = new URL(scheme, hostName, path + "?" + queryString);
        }
        else {
            // no query params
            url = new URL(scheme, hostName, path);
        }

        httpRequest.setURI(url.toURI());
    }

    public void setHeaders(HttpRequestBase httpRequest, Map<String, String> headers) {
        if (null == httpRequest) {
            throw new IllegalArgumentException("httpRequest is null");
        }
        if (null == headers || headers.isEmpty()) {
            throw new IllegalArgumentException("headers is null or empty");
        }

        for (Map.Entry<String, String> header : headers.entrySet()) {
            httpRequest.setHeader(header.getKey(), header.getValue());
        }
    }

    public void setFormData(HttpPost httpPost, Map<String, String> formData) throws UnsupportedEncodingException {
        if (null == httpPost) {
            throw new IllegalArgumentException("httpPost is null");
        }
        if (null == formData || formData.isEmpty()) {
            throw new IllegalArgumentException("formData is null or empty");
        }

        // create the multipart form
        MultipartEntity multipartEntity = httpClientFactory.createMultipartEntity();

        // add the fields
        for (Map.Entry<String, String> field: formData.entrySet()) {
            StringBody stringBody = httpClientFactory.createStringBody(field.getValue());
            multipartEntity.addPart(field.getKey(), stringBody);
        }

        // add the form to the POST
        httpPost.setEntity(multipartEntity);
    }

    public void setCookies(HttpRequestBase httpRequest, Map<String, String> cookies) {
        if (null == httpRequest) {
            throw new IllegalArgumentException("httpRequest is null");
        }
        if (null == cookies || cookies.isEmpty()) {
            throw new IllegalArgumentException("cookies is null or empty");
        }

        // manually handle the cookies
        httpRequest.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);//CookiePolicy.IGNORE_COOKIES);

        String cookieHeaderValue = "";
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            if (!cookieHeaderValue.isEmpty()) {
                cookieHeaderValue += "; ";
            }
            cookieHeaderValue += cookie.getKey() + "=" + cookie.getValue();
        }

        httpRequest.addHeader(HeaderName.COOKIE, cookieHeaderValue);
    }

    public Map<String, String> getCookies(DefaultHttpClient httpClient) {
        if (null == httpClient) {
            throw new IllegalArgumentException("defaultHttpClient is null");
        }

        CookieStore cookieStore = httpClient.getCookieStore();
        List<Cookie> cookies = cookieStore.getCookies();

        Map<String, String> cookieMap = new HashMap<String, String>();
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie.getValue());
        }

        return cookieMap;
    }

    public Map<String, String> getHeaders(HttpResponse httpResponse) {
        if (null == httpResponse) {
            throw new IllegalArgumentException("httpResponse is null");
        }

        Header[] httpHeaders = httpResponse.getAllHeaders();
        Map<String, String> headers = new HashMap<String, String>();

        for (Header httpHeader : httpHeaders) {
            headers.put(httpHeader.getName(), httpHeader.getValue());
        }

        return headers;
    }

    /**
     * This method exists to wrap the DefaultHttpClient.execute method which uses a static method and was hard to
     * unit test.
     * @param httpClient The HttpClient to use to execute the given request
     * @param httpRequest the request to execute
     * @return the response returned from executing the given request on the http client
     * @throws java.io.IOException when execute goes wrong
     */
    public HttpResponse execute(HttpClient httpClient, HttpRequestBase httpRequest) throws IOException {
        if (null == httpClient) {
            throw new IllegalArgumentException("httpClient is null");
        }
        if (null == httpRequest) {
            throw new IllegalArgumentException("httpRequest is null");
        }

        return httpClient.execute(httpRequest);
    }
}
