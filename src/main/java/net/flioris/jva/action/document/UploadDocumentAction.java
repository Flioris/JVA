package net.flioris.jva.action.document;

import net.flioris.jva.action.RestAction;
import okhttp3.*;
import org.json.JSONObject;

public class UploadDocumentAction extends RestAction<JSONObject> {
    private final OkHttpClient client;
    private final MultipartBody.Builder bodyBuilder;
    private final String uploadUrl;

    public UploadDocumentAction(OkHttpClient client, MultipartBody.Builder bodyBuilder, String uploadUrl) {
        super(response -> {
            try (response) {
                if (response.isSuccessful()) {
                    return new JSONObject(response.body().string());
                }
                return null;
            }
        });

        this.client = client;
        this.bodyBuilder = bodyBuilder;
        this.uploadUrl = uploadUrl;
    }

    @Override
    protected Call getCall() {
        return client.newCall(new Request.Builder().url(uploadUrl).post(bodyBuilder.build()).build());
    }
}
