package net.flioris.jva.action.message;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SendMessagesAction extends RestAction<List<Integer>> {
    private final OkHttpClient client;
    private final okhttp3.HttpUrl.Builder urlBuilder;

    public SendMessagesAction(OkHttpClient client, okhttp3.HttpUrl.Builder urlBuilder) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    List<Integer> ids = new ArrayList<>();
                    if (json.has("response")) {
                        JSONArray array = json.getJSONArray("response");
                        for (int i = 0; i < array.length(); i++) {
                            ids.add(array.getJSONObject(i).getInt("message_id"));
                        }
                    }
                    return ids;
                }
                return null;
            }
        });

        this.client = client;
        this.urlBuilder = urlBuilder;
    }

    public SendMessagesAction setPhoto(JSONObject savePhotoResponse) {
        urlBuilder.addQueryParameter("attachment", "photo" + savePhotoResponse.getInt("owner_id") +
                "_" + savePhotoResponse.getInt("id") + "_" + savePhotoResponse.getString("access_key"));

        return this;
    }

    public SendMessagesAction setReplyTo(String messageId) {
        urlBuilder.addQueryParameter("reply_to", messageId);

        return this;
    }

    public SendMessagesAction setMessage(String message) {
        urlBuilder.addQueryParameter("message", message);

        return this;
    }

    public SendMessagesAction setLat(int lat) {
        urlBuilder.setQueryParameter("lat", String.valueOf(lat));

        return this;
    }

    public SendMessagesAction setLong(int longValue) {
        urlBuilder.setQueryParameter("long", String.valueOf(longValue));

        return this;
    }

    public SendMessagesAction setGuid(String id) {
        urlBuilder.setQueryParameter("guid", String.valueOf(id));

        return this;
    }

    public SendMessagesAction setForwardMessages(String... forwardMessages) {
        urlBuilder.addQueryParameter("forward_messages", String.join(",", forwardMessages));

        return this;
    }

    public SendMessagesAction setSticker(int stickerId) {
        urlBuilder.addQueryParameter("sticker_id", String.valueOf(stickerId));

        return this;
    }

    @Override
    protected Call getCall() {
        return client.newCall(new Request.Builder().url(urlBuilder.build()).build());
    }
}
