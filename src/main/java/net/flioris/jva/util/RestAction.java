package net.flioris.jva.util;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RestAction<T> {
    private final Call call;
    private final ResponseHandler<T> responseHandler;

    public RestAction(Call call, ResponseHandler<T> responseHandler) {
        this.call = call;
        this.responseHandler = responseHandler;
    }

    /**
     * Executes the request synchronously.
     */
    public T complete() {
        try (Response response = call.execute()) {
            if (response.isSuccessful()) {
                return responseHandler.handleResponse(response);
            } else {
                throw new RuntimeException("Ошибка выполнения запроса: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the request asynchronously.
     */
    public void queue(Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onFailure.accept(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    T result = responseHandler.handleResponse(response);
                    onSuccess.accept(result);
                } else {
                    onFailure.accept(new RuntimeException("Ошибка выполнения запроса: " + response.code()));
                }
            }
        });
    }

    /**
     * Executes the request asynchronously.
     */
    public void queue(Consumer<T> onSuccess) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    T result = responseHandler.handleResponse(response);
                    onSuccess.accept(result);
                }
            }
        });
    }

    /**
     * Executes the request asynchronously.
     */
    public void queue() {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) {}
        });
    }

    /**
     * Executes the request asynchronously.
     *
     * @return CompletableFuture.
     */
    public CompletableFuture<T> submit() {
        CompletableFuture<T> future = new CompletableFuture<>();
        queue(future::complete, future::completeExceptionally);
        return future;
    }

    @FunctionalInterface
    public interface ResponseHandler<T> {
        T handleResponse(Response response) throws IOException;
    }
}

