package net.flioris.jva.action.wall;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

public class PostAction extends RestAction<Integer> {
    private final OkHttpClient client;
    private final okhttp3.HttpUrl.Builder urlBuilder;

    public PostAction(OkHttpClient client, okhttp3.HttpUrl.Builder urlBuilder) {
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

    public PostAction setSigned(boolean signed) {
        urlBuilder.setQueryParameter("signed", signed ? "1" : "0");

        return this;
    }

    public PostAction setPublishDate(long timestamp) {
        urlBuilder.setQueryParameter("publish_date", String.valueOf(timestamp));

        return this;
    }

    public PostAction setLat(int lat) {
        urlBuilder.setQueryParameter("lat", String.valueOf(lat));

        return this;
    }

    public PostAction setLong(int longValue) {
        urlBuilder.setQueryParameter("long", String.valueOf(longValue));

        return this;
    }

    public PostAction setPlaceId(int placeId) {
        urlBuilder.setQueryParameter("place_id", String.valueOf(placeId));

        return this;
    }

    public PostAction setPostId(int postId) {
        urlBuilder.setQueryParameter("post_id", String.valueOf(postId));

        return this;
    }

    public PostAction setGuid(String id) {
        urlBuilder.setQueryParameter("guid", String.valueOf(id));

        return this;
    }

    public PostAction setMarkAsAds(boolean markAsAds) {
        urlBuilder.setQueryParameter("mark_as_ads", markAsAds ? "1" : "0");

        return this;
    }

    public PostAction setCloseComments(boolean closeComments) {
        urlBuilder.setQueryParameter("close_comments", closeComments ? "1" : "0");

        return this;
    }

    public PostAction setDonutPaidDuration(int time) {
        urlBuilder.setQueryParameter("donut_paid_duration", String.valueOf(time));

        return this;
    }

    public PostAction setMuteNotifications(boolean muteNotifications) {
        urlBuilder.setQueryParameter("mute_notifications", muteNotifications ? "1" : "0");

        return this;
    }

    public PostAction setCopyright(String copyright) {
        urlBuilder.setQueryParameter("copyright", copyright);

        return this;
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
