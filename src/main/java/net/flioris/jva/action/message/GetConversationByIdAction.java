package net.flioris.jva.action.message;

import net.flioris.jva.action.RestAction;
import net.flioris.jva.models.conversation.Conversation;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

public class GetConversationByIdAction extends RestAction<Conversation> {
    private final OkHttpClient client;
    private final okhttp3.HttpUrl.Builder urlBuilder;

    public GetConversationByIdAction(OkHttpClient client, okhttp3.HttpUrl.Builder urlBuilder) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? Conversation.fromJSON(json.getJSONObject("response")
                            .getJSONArray("items").getJSONObject(0)) : null;
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