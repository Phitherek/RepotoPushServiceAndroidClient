package me.phitherek.repotopushserviceandroidclient;

import android.content.Context;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Used to authenticate a {@link com.pusher.client.channel.PrivateChannel
 * private} or {@link com.pusher.client.channel.PresenceChannel presence}
 * channel subscription.
 *
 * <p>
 * Makes an HTTP request to a defined HTTP endpoint. Expects an authentication
 * token to be returned.
 * </p>
 *
 * <p>
 * For more information see the <a
 * href="http://pusher.com/docs/authenticating_users">Authenticating Users
 * documentation</a>.
 */
public class CustomHttpAuthorizer implements Authorizer {

    private final URL endPoint;
    private HashMap<String, String> mHeaders = new HashMap<String, String>();
    private HashMap<String, String> mQueryStringParameters = new HashMap<String, String>();
    private final String ENCODING_CHARACTER_SET = "UTF-8";
    private Context ctx;

    /**
     * Creates a new authorizer.
     *
     * @param endPoint
     *            The endpoint to be called when authenticating.
     */
    public CustomHttpAuthorizer(final String endPoint, Context ctx) {
        try {
            this.endPoint = new URL(endPoint);
            this.ctx = ctx;
        }
        catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse authentication end point into a valid URL", e);
        }
    }

    /**
     * Set additional headers to be sent as part of the request.
     *
     * @param headers
     */
    public void setHeaders(final HashMap<String, String> headers) {
        mHeaders = headers;
    }

    /**
     * Identifies if the HTTP request will be sent over HTTPS.
     *
     * @return
     */
    public Boolean isSSL() {
        return endPoint.getProtocol().equals("https");
    }

    /**
     * This methods is for passing extra parameters authentication that needs to
     * be added to query string.
     *
     * @param queryStringParameters
     *            the query parameters
     */
    public void setQueryStringParameters(final HashMap<String, String> queryStringParameters) {
        mQueryStringParameters = queryStringParameters;
    }

    @Override
    public String authorize(final String channelName, final String socketId) throws AuthorizationFailureException {

        try {
            final StringBuffer urlParameters = new StringBuffer();
            urlParameters.append("channel_name=").append(URLEncoder.encode(channelName, ENCODING_CHARACTER_SET));
            urlParameters.append("&socket_id=").append(URLEncoder.encode(socketId, ENCODING_CHARACTER_SET));

            // Adding extra parameters supplied to be added to query string.
            for (final String parameterName : mQueryStringParameters.keySet()) {
                urlParameters.append("&").append(parameterName).append("=");
                urlParameters.append(URLEncoder.encode(mQueryStringParameters.get(parameterName),
                        ENCODING_CHARACTER_SET));
            }

            HttpURLConnection connection = null;
            if (isSSL()) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = new BufferedInputStream(ctx.getResources().openRawResource(R.raw.repotopushauth));
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                } finally {
                    caInput.close();
                }
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("repotopushauth", ca);
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                connection = (HttpsURLConnection)endPoint.openConnection();
                ((HttpsURLConnection)connection).setSSLSocketFactory(sslSocketFactory);
            }
            else {
                connection = (HttpURLConnection)endPoint.openConnection();
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.toString().getBytes().length));

            // Add in the user defined headers
            for (final String headerName : mHeaders.keySet()) {
                final String headerValue = mHeaders.get(headerName);
                connection.setRequestProperty(headerName, headerValue);
            }

            connection.setUseCaches(false);

            // Send request
            final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters.toString());
            wr.flush();
            wr.close();

            // Read response
            final InputStream is = connection.getInputStream();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            final StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            final int responseHttpStatus = connection.getResponseCode();
            if (responseHttpStatus != 200) {
                throw new AuthorizationFailureException(response.toString());
            }

            return response.toString();

        }
        catch (final IOException e) {
            throw new AuthorizationFailureException(e);
        } catch (CertificateException e) {
            throw new AuthorizationFailureException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthorizationFailureException(e);
        } catch (KeyStoreException e) {
            throw new AuthorizationFailureException(e);
        } catch (KeyManagementException e) {
            throw new AuthorizationFailureException(e);
        }
    }
}