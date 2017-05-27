package me.avelar.donee.web;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import me.avelar.donee.R;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class ServiceFactory {

    private static OkHttpClient okHttpClient;
    private static RestAdapter  globalAdapter;
    private static DoneeService service;

    public static DoneeService getService(Context context) {
        if (service == null) {
            service = getAdapter(context).create(DoneeService.class);
        }
        return service;
    }

    private static RestAdapter getAdapter(Context context) {
        if (globalAdapter == null) {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            globalAdapter = new RestAdapter.Builder()
                .setEndpoint(UrlRepository.API_BASE)
              //.setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .setClient(new OkClient(prepareClient(context)))
                .build();
        }
        return globalAdapter;
    }

    private static OkHttpClient prepareClient(Context context) {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
            try {
                // loading CAs from an InputStream
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream cert = context.getResources().openRawResource(R.raw.donee_api_cert);
                Certificate ca = cf.generateCertificate(cert);
                cert.close();

                // creating a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // creating a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // creating an SSLSocketFactory that uses our TrustManager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        return okHttpClient;
    }

}