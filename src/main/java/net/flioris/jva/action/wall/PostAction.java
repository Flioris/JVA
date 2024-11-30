package net.flioris.jva.action.wall;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

public class PostAction extends RestAction<Integer> {
    private final OkHttpClient client;
    private final okhttp3.HttpUrl.Builder urlBuilder;

    public PostAction( OkHttpClient client, okhttp3.HttpUrl.Builder urlBuilder) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? json.getJSONObject("response").getInt("post_id") : null;
                }
                return null;
            }
        });

        this.client = client;
        this.urlBuilder = urlBuilder;
    }

    public PostAction setPhoto(JSONObject saveDocResponse) {
        urlBuilder.addQueryParameter("attachments", "doc" + saveDocResponse.getInt("owner_id") + "_" +
                saveDocResponse.getInt("id"));

        return this;
    }

    public PostAction setMessage(String message) {
        urlBuilder.addQueryParameter("message", message);

        return this;
    }

    public PostAction setFromGroup(boolean fromGroup) {
        urlBuilder.setQueryParameter("from_group", fromGroup ? "1" : "0");

        return this;
    }

    @Override
    protected Call getCall() {
        return client.newCall(new Request.Builder().url(urlBuilder.build()).build());
    }
}
