package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Pair<T extends Request, N extends Response> {
    T request;
    CompletableFuture<N> responseFuture;

    public Pair(T request, CompletableFuture<N> responseFuture) {
        this.request = request;
        this.responseFuture = responseFuture;
    }

    public Pair(T request) {
        this.request = request;
        this.responseFuture = new CompletableFuture<>();
    }

    public T getRequest() {
        return request;
    }

    public CompletableFuture<N> getResponseFuture() {
        return responseFuture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;
        return Objects.equals(request, pair.request) && Objects.equals(responseFuture, pair.responseFuture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, responseFuture);
    }

}
