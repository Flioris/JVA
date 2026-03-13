package net.flioris.jva.action.user;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

public class GetUserPhotoAction extends RestAction<String> {
    private final OkHttpClient client;
    private final HttpUrl.Builder urlBuilder;

    public GetUserPhotoAction(OkHttpClient client, HttpUrl.Builder urlBuilder) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? json.getJSONArray("response").getJSONObject(0).getString("photo_400_orig") : null;
                }
                return null;
            }
        });

        this.client = client;
        this.urlBuilder = urlBuilder;
    }

    @Override
    protected Call getCall() {
        return client.newCall(new Request.Builder().url(urlBuilder.build()).build());
    }
}
