package com.xceptance.xlt.agentcontroller.xtc;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.ssl.EasyHostnameVerifier;
import com.xceptance.common.util.ssl.EasyX509TrustManager;
import com.xceptance.xlt.agentcontroller.AgentControllerConfiguration.PrivateAgentType;
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
 * A client to the private agent part of the XTC REST API. Currently, only the registration of a private agent at XTC is
 * supported.
 */
public class RestApiClient
{
    private static final Logger log = LoggerFactory.getLogger(RestApiClient.class);

    private static final EasyHostnameVerifier INSECURE_HOSTNAME_VERIFIER = new EasyHostnameVerifier();

    private static final EasyX509TrustManager INSECURE_TRUST_MANAGER = new EasyX509TrustManager(null);

    private static final SSLSocketFactory INSECURE_SSL_SOCKET_FACTORY = createInsecureSslSocketFactory();

    private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");

    private static final String REGISTER_REQUEST_BODY_TEMPLATE = """
        {
            "id": "%s",
            "name": "%s",
            "description": "%s",
            "ipAddress": "%s",
            "hostName": "%s",
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

    private final HttpUrl privateAgentsUrl;

    private final HttpUrl tokenUrl;

    public RestApiClient(final String host, final int port, final String clientId, final String clientSecret, final String org,
                         final String project)
    {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        httpClient = createHttpClient();

        // build basic URLs
        final HttpUrl baseUrl = new HttpUrl.Builder().scheme("https").host(host).port(port).build();

        privateAgentsUrl = baseUrl.newBuilder().addPathSegments("public/api/v2/orgs").addPathSegment(org).addPathSegment("projects")
                                  .addPathSegment(project).addPathSegment("private-agents").build();

        tokenUrl = baseUrl.newBuilder().addPathSegment("oauth").addPathSegment("token").build();
    }

    /**
     * Registers the current machine as a private agent machine at XTC using the passed details.
     */
    public void registerPrivateAgent(final String id, final String name, final String description, final String hostName,
                                     final String ipAddress, final PrivateAgentType agentType, int cores, long memory, long disk)
        throws IOException
    {
        // build Authorization header
        final String authHeaderValue = "Bearer " + getNewAccessToken("PRIVATEAGENT_REGISTER");

        // build JSON request body
        final String json = String.format(REGISTER_REQUEST_BODY_TEMPLATE, id, name, description, ipAddress, hostName, agentType, cores,
                                          memory, disk);
        final RequestBody jsonBody = RequestBody.create(json, MEDIA_TYPE_JSON);

        // build request
        final Request request = new Request.Builder().url(privateAgentsUrl).header("Authorization", authHeaderValue).post(jsonBody).build();

        // execute request
        try (Response response = httpClient.newCall(request).execute())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Received JSON response: {}", response.body().string());
            }
            else
            {
                // TODO
                System.err.printf("Received JSON response: %s\n", response.body().string());
            }

            // check response
            assertThat(response.code() == 200, "Unexpected status code: " + response.code());
        }
    }

    // public void sendHeartbeat(final String id) throws IOException
    // {
    // // build Authorization header
    // final String authHeaderValue = "Bearer " + getNewAccessToken("PRIVATEAGENT_HEARTBEAT");
    //
    // // build JSON request body
    // final RequestBody jsonBody = RequestBody.create("{}", JSON);
    //
    // // build URL
    // final HttpUrl heartbeatUrl =
    // privateAgentsUrl.newBuilder().addPathSegment(id).addPathSegment("heartbeat").build();
    //
    // // build request
    // final Request request = new Request.Builder().url(heartbeatUrl).header("Authorization",
    // authHeaderValue).put(jsonBody).build();
    //
    // // execute request
    // try (Response response = httpClient.newCall(request).execute())
    // {
    // if (log.isDebugEnabled())
    // {
    // log.debug("Received JSON response: {}", response.body().string());
    // }
    //
    // // check response
    // assertThat(response.code() == 200, "Unexpected status code: " + response.code());
    // }
    // }

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
     * @param message
     */
    private static void assertThat(final boolean validationResult, final String message)
    {
        if (validationResult == false)
        {
            throw new XltException(message);
        }
    }

    /**
     * Creates an OkHttp client
     * 
     * @return the client
     */
    private static OkHttpClient createHttpClient()
    {
        final Builder httpClientBuilder = new OkHttpClient.Builder();

        httpClientBuilder.connectionSpecs(List.of(new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).allEnabledTlsVersions()
                                                                                                       .allEnabledCipherSuites().build()));

        httpClientBuilder.sslSocketFactory(INSECURE_SSL_SOCKET_FACTORY, INSECURE_TRUST_MANAGER);
        httpClientBuilder.hostnameVerifier(INSECURE_HOSTNAME_VERIFIER);

        return httpClientBuilder.build();
    }

    /**
     * Creates an SSL socket factory with a trust manager that accepts invalid/self-signed certificates.
     *
     * @return the socket factory
     */
    private static SSLSocketFactory createInsecureSslSocketFactory()
    {
        try
        {
            final TrustManager[] trustManagers = new TrustManager[]
                {
                    INSECURE_TRUST_MANAGER
                };

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);

            return sslContext.getSocketFactory();
        }
        catch (NoSuchAlgorithmException | KeyManagementException e)
        {
            throw new XltException("Failed to create insecure SSL socket factory", e);
        }
    }
}
