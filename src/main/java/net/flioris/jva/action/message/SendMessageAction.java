package net.flioris.jva.action.message;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;

public class SendMessageAction extends RestAction<Integer> {
    private final OkHttpClient client;
    private final HttpUrl.Builder urlBuilder;

    public SendMessageAction(OkHttpClient client, HttpUrl.Builder urlBuilder) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    Object resp = json.opt("response");
                    if (resp instanceof Number) {
                        return ((Number) resp).intValue();
                    }
                }
                return null;
            }
        });

        this.client = client;
        this.urlBuilder = urlBuilder;
    }

    public SendMessageAction setPhoto(JSONObject savePhotoResponse) {
        JSONObject photo = savePhotoResponse;

        if (savePhotoResponse.has("response")) {
            Object resp = savePhotoResponse.get("response");
            if (resp instanceof JSONArray) {
                JSONArray arr = savePhotoResponse.getJSONArray("response");
                if (!arr.isEmpty()) {
                    photo = arr.getJSONObject(0);
                } else {
                    throw new IllegalArgumentException("savePhotoResponse.response is empty");
                }
            } else if (resp instanceof JSONObject) {
                photo = savePhotoResponse.getJSONObject("response");
            }
        }

        if (!photo.has("owner_id") || !photo.has("id")) {
            throw new IllegalArgumentException("photo object must contain owner_id and id fields: " + photo);
        }

        int ownerId = photo.getInt("owner_id");
        int id = photo.getInt("id");
        String accessKey = photo.optString("access_key", null);
        StringBuilder attachment = new StringBuilder();

        attachment.append("photo").append(ownerId).append("_").append(id);
        if (accessKey != null && !accessKey.isEmpty()) {
            attachment.append("_").append(accessKey);
        }

        urlBuilder.addQueryParameter("attachment", attachment.toString());

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
