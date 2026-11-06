package com.santosschein.app.core.network;

import android.os.Build;

import com.santosschein.app.BuildConfig;
import com.santosschein.app.core.session.SessionManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

public final class OkHttpClientFactory {

    /**
     * Referencia opcional do aparelho gravada no historico de acessos (coluna "dispositivo").
     */
    public static final String DEVICE_HEADER = "X-Device-Name";

    private OkHttpClientFactory() {
    }

    public static OkHttpClient create(SessionManager sessionManager) {
        // HEADERS (e nao BODY): o corpo do login traz a senha e nao pode ir para o log.
        // O cabecalho Authorization (token) tambem e ocultado.
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.HEADERS
                : HttpLoggingInterceptor.Level.NONE);
        loggingInterceptor.redactHeader("Authorization");

        String deviceName = deviceName();

        return new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    if (deviceName.isEmpty()) {
                        return chain.proceed(request);
                    }
                    return chain.proceed(request.newBuilder().header(DEVICE_HEADER, deviceName).build());
                })
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(loggingInterceptor)
                .build();
    }

    static String deviceName() {
        String manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.trim();
        String model = Build.MODEL == null ? "" : Build.MODEL.trim();
        String name = model.toLowerCase(Locale.ROOT).startsWith(manufacturer.toLowerCase(Locale.ROOT))
                ? model
                : (manufacturer + " " + model).trim();
        // Cabecalhos HTTP aceitam apenas ASCII visivel.
        name = name.replaceAll("[^\\x20-\\x7E]", "").trim();
        if (name.isEmpty()) {
            return "";
        }
        return name.length() > 120 ? name.substring(0, 120) : name;
    }
}
