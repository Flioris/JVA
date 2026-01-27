package net.flioris.jva.action.message;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class SendMessageAction extends RestAction<Integer> {
    private final OkHttpClient client;
    private final HttpUrl.Builder urlBuilder;

    public SendMessageAction(OkHttpClient client, HttpUrl.Builder urlBuilder) {
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
        JSONObject photo = savePhotoResponse;

        if (savePhotoResponse.has("server") && savePhotoResponse.has("photo") && savePhotoResponse.has("hash")) {
            HttpUrl current = this.urlBuilder.build();
            String accessToken = current.queryParameter("access_token");
            String version = current.queryParameter("v");
            if (accessToken == null || version == null) {
                throw new IllegalStateException("access_token and v must be present in the request URL builder to save uploaded photo");
            }
            HttpUrl saveUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host("api.vk.com")
                    .addPathSegment("method")
                    .addPathSegment("photos.saveMessagesPhoto")
                    .addQueryParameter("server", String.valueOf(savePhotoResponse.getInt("server")))
                    .addQueryParameter("photo", savePhotoResponse.getString("photo"))
                    .addQueryParameter("hash", savePhotoResponse.getString("hash"))
                    .addQueryParameter("access_token", accessToken)
                    .addQueryParameter("v", version)
                    .build();

            Request saveRequest = new Request.Builder().url(saveUrl).get().build();
            try (Response resp = client.newCall(saveRequest).execute()) {
                if (!resp.isSuccessful()) {
                    throw new RuntimeException("photos.saveMessagesPhoto returned HTTP " + resp.code());
                }
                String body = resp.body().string();
                JSONObject saveJson = new JSONObject(body);
                if (!saveJson.has("response")) {
                    throw new RuntimeException("photos.saveMessagesPhoto returned no response: " + saveJson.toString());
                }
                JSONArray arr = saveJson.getJSONArray("response");
                if (arr.isEmpty()) {
                    throw new RuntimeException("photos.saveMessagesPhoto returned empty response array: " + saveJson.toString());
                }
                photo = arr.getJSONObject(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (savePhotoResponse.has("response")) {
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
