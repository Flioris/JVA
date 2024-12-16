package net.flioris.jva.action;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class RestAction<T> {
    private final ResponseHandler<T> responseHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger("JVA RestAction");

    public RestAction(ResponseHandler<T> responseHandler) {
        this.responseHandler = responseHandler;
    }

    protected abstract Call getCall();

    /**
     * Executes the request synchronously.
     */
    public T complete() {
        try (Response response = getCall().execute()) {
            if (response.isSuccessful()) {
                return responseHandler.handleResponse(response);
            } else {
                LOGGER.error("Request execution error: {}", response.code());
                return null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * Executes the request asynchronously.
     */
    public void queue(Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        getCall().enqueue(new Callback() {
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
                    onFailure.accept(new RuntimeException("Request execution error: " + response.code()));
                    response.close();
                }
            }
        });
    }

    /**
     * Executes the request asynchronously.
     */
    public void queue(Consumer<T> onSuccess) {
        getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    T result = responseHandler.handleResponse(response);
                    onSuccess.accept(result);
                } else {
                    LOGGER.error("Request execution error: {}", response.code());
                    response.close();
                }
            }
        });
    }

    /**
     * Executes the request asynchronously.
     */
    public void queue() {
        getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Request execution error: {}", response.code());
                }
                response.close();
            }
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

