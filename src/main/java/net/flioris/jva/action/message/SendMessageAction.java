package net.flioris.jva.action.message;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

public class SendMessageAction extends RestAction<Integer> {
    private final OkHttpClient client;
    private final okhttp3.HttpUrl.Builder urlBuilder;

    public SendMessageAction(OkHttpClient client, okhttp3.HttpUrl.Builder urlBuilder) {
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

    public SendMessageAction setPhoto(JSONObject savePhotoResponse) {
        urlBuilder.addQueryParameter("attachment", "photo" + savePhotoResponse.getInt("owner_id") +
                "_" + savePhotoResponse.getInt("id") + "_" + savePhotoResponse.getString("access_key"));

        return this;
    }

    public SendMessageAction setReplyTo(String messageId) {
        urlBuilder.addQueryParameter("reply_to", messageId);

        return this;
    }

    public SendMessageAction setMessage(String message) {
        urlBuilder.addQueryParameter("message", message);

        return this;
    }

    public SendMessageAction setLat(int lat) {
        urlBuilder.setQueryParameter("lat", String.valueOf(lat));

        return this;
    }

    public SendMessageAction setLong(int longValue) {
        urlBuilder.setQueryParameter("long", String.valueOf(longValue));

        return this;
    }

    public SendMessageAction setGuid(String id) {
        urlBuilder.setQueryParameter("guid", String.valueOf(id));

        return this;
    }

    public SendMessageAction setForwardMessages(String... forwardMessages) {
        urlBuilder.addQueryParameter("forward_messages", String.join(",", forwardMessages));

        return this;
    }

    public SendMessageAction setSticker(int stickerId) {
        urlBuilder.addQueryParameter("sticker_id", String.valueOf(stickerId));

        return this;
    }

    @Override
    protected Call getCall() {
        return client.newCall(new Request.Builder().url(urlBuilder.build()).build());
    }
}
