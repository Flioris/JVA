package net.flioris.jva.action.message;

import net.flioris.jva.action.RestAction;
import net.flioris.jva.models.conversation.Conversation;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetConversationsByIdAction extends RestAction<List<Conversation>> {
    private final OkHttpClient client;
    private final okhttp3.HttpUrl.Builder urlBuilder;

    public GetConversationsByIdAction(OkHttpClient client, okhttp3.HttpUrl.Builder urlBuilder) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    List<Conversation> conversations = new ArrayList<>();
                    if (json.has("response")) {
                        JSONArray array = json.getJSONObject("response").getJSONArray("items");
                        for (int i = 0; i < array.length(); i++) {
                            conversations.add(Conversation.fromJSON(array.getJSONObject(i)));
                        }
                    }
                    return conversations;
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
