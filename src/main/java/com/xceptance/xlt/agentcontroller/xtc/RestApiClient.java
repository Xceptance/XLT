/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.agentcontroller.xtc;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.agentcontroller.AgentControllerConfiguration.PrivateMachineType;
import com.xceptance.xlt.api.util.XltException;

import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A client to the private machine part of the XTC REST API. Currently, only the registration of a private machine at
 * XTC is supported.
 */
public class RestApiClient
{
    private static final Logger log = LoggerFactory.getLogger(RestApiClient.class);

    private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");

    private static final String REGISTER_REQUEST_BODY_TEMPLATE = """
        {
            "hostName": "%s",
            "ipAddress": "%s",
            "type": "%s",
            "specs": {
                "cores": %d,
                "memory": %d,
                "disk": %d
            }
        }
        """;

    private final String clientId;

    private final String clientSecret;

    private final OkHttpClient httpClient;

    private final HttpUrl privateMachinesUrl;

    private final HttpUrl tokenUrl;

    public RestApiClient(final String host, final int port, final String clientId, final String clientSecret, final String org,
                         final String project)
    {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        httpClient = createHttpClient();

        // build basic URLs
        final HttpUrl baseUrl = new HttpUrl.Builder().scheme("https").host(host).port(port).build();

        privateMachinesUrl = baseUrl.newBuilder().addPathSegments("public/api/v2/orgs").addPathSegment(org).addPathSegment("projects")
                                    .addPathSegment(project).addPathSegment("private-machines").build();

        tokenUrl = baseUrl.newBuilder().addPathSegment("oauth").addPathSegment("token").build();
    }

    /**
     * Registers the current machine as a private machine at XTC using the passed details.
     */
    public void registerPrivateMachine(final String hostName, final String ipAddress, final PrivateMachineType agentType, final int cores,
                                       final long memory, final long disk)
        throws IOException
    {
        // build Authorization header
        final String authHeaderValue = "Bearer " + getNewAccessToken("PRIVATEMACHINE_REGISTER");

        // build JSON request body
        final String json = String.format(REGISTER_REQUEST_BODY_TEMPLATE, hostName, ipAddress, agentType, cores, memory, disk);
        final RequestBody jsonBody = RequestBody.create(json, MEDIA_TYPE_JSON);

        // build request
        final Request request = new Request.Builder().url(privateMachinesUrl).header("Authorization", authHeaderValue).post(jsonBody)
                                                     .build();

        // execute request
        try (Response response = httpClient.newCall(request).execute())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Received JSON response: {}", response.body().string());
            }

            // check response
            assertThat(response.code() == 200, "Unexpected status code: " + response.code());
        }
    }

    /**
     * Requests a new access token for the given scope from XTC.
     *
     * @param scope
     *            the scope
     * @return the token
     * @throws IOException
     *             if the API cannot be contacted
     */
    private String getNewAccessToken(final String scope) throws IOException
    {
        final FormBody tokenRequestBody = new FormBody.Builder().add("client_id", clientId).add("client_secret", clientSecret)
                                                                .add("grant_type", "client_credentials").add("scope", scope).build();

        final Request request = new Request.Builder().url(tokenUrl).post(tokenRequestBody).build();

        try (Response response = httpClient.newCall(request).execute())
        {
            final String responseBodyText = response.body().string();

            log.debug("Received JSON response: {}", responseBodyText);

            // check response
            assertThat(response.code() == 200, "Unexpected status code: " + response.code());

            // extract token
            final String token = StringUtils.substringBetween(responseBodyText, "\"access_token\":\"", "\"");
            assertThat(token != null, "Token is null");

            return token;
        }
    }

    /**
     * Checks if the passed validation result is false and throws an exception with the given message. This is a simple
     * helper for validating things.
     *
     * @param validationResult
     *            a boolean indicating the validation result
     * @param message
     *            the error message
     */
    private static void assertThat(final boolean validationResult, final String message)
    {
        if (validationResult == false)
        {
            throw new XltException(message);
        }
    }

    /**
     * Creates the underlying OkHttp client.
     *
     * @return the client
     */
    private static OkHttpClient createHttpClient()
    {
        final Builder httpClientBuilder = new OkHttpClient.Builder();

        httpClientBuilder.connectionSpecs(List.of(new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).allEnabledTlsVersions()
                                                                                                       .allEnabledCipherSuites().build()));

        // setUpHttpClientForDevMode(httpClientBuilder);

        return httpClientBuilder.build();
    }

//    /**
//     * Adds any customization needed for local development to the passed builder.
//     */
//    private static void setUpHttpClientForDevMode(final Builder httpClientBuilder)
//    {
//        // create the needed components to accept invalid/self-signed certificates and ignore host name verification errors
//        final EasyHostnameVerifier insecureHostNameVerifier = new EasyHostnameVerifier();
//        final EasyX509TrustManager insecureTrustManager = new EasyX509TrustManager(null);
//
//        final SSLSocketFactory insecureSslSocketFactory;
//        try
//        {
//            final TrustManager[] trustManagers = new TrustManager[]
//                {
//                    insecureTrustManager
//                };
//
//            final SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, trustManagers, null);
//
//            insecureSslSocketFactory = sslContext.getSocketFactory();
//        }
//        catch (final NoSuchAlgorithmException | KeyManagementException e)
//        {
//            throw new XltException("Failed to create insecure SSL socket factory", e);
//        }
//
//        // now modify the builder
//        httpClientBuilder.sslSocketFactory(insecureSslSocketFactory, insecureTrustManager);
//        httpClientBuilder.hostnameVerifier(insecureHostNameVerifier);
//    }
}
