package net.flioris.jva.action.photo;

import net.flioris.jva.action.RestAction;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

public class UploadPhotoAction extends RestAction<JSONObject> {
    private final OkHttpClient client;
    private final okhttp3.MultipartBody.Builder bodyBuilder;
    private final String uploadUrl;

    public UploadPhotoAction(OkHttpClient client, okhttp3.MultipartBody.Builder bodyBuilder, String uploadUrl) {
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
