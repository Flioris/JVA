package net.flioris.jva.action.message;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendAction extends RestAction<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger("SendAction");
    private final OkHttpClient client;
    private final okhttp3.HttpUrl.Builder urlBuilder;

    public SendAction(OkHttpClient client, okhttp3.HttpUrl.Builder urlBuilder) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? json.getInt("response") : null;
                }
                return null;
            }
        });

        this.client = client;
        this.urlBuilder = urlBuilder;
    }

    public SendAction setPhoto(JSONObject savePhotoResponse) {
        urlBuilder.addQueryParameter("attachment", "photo" + savePhotoResponse.getInt("owner_id") +
                "_" + savePhotoResponse.getInt("id") + "_" + savePhotoResponse.getString("access_key"));

        return this;
    }

    public SendAction setReplyTo(String messageId) {
        urlBuilder.addQueryParameter("reply_to", messageId);

        return this;
    }

    public SendAction setMessage(String message) {
        urlBuilder.addQueryParameter("message", message);

        return this;
    }

    @Override
    protected Call getCall() {
        return client.newCall(new Request.Builder().url(urlBuilder.build()).build());
    }
}
