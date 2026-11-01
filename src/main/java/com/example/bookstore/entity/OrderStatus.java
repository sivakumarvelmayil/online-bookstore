
package com.example.bookstore.entity;

public enum OrderStatus {

    PLACED,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus nextStatus) {

        return switch (this) {
            case PLACED ->
                    nextStatus == CONFIRMED ||
                            nextStatus == CANCELLED;

            case CONFIRMED ->
                    nextStatus == PROCESSING ||
                            nextStatus == CANCELLED;

            case PROCESSING ->
                    nextStatus == SHIPPED ||
                            nextStatus == CANCELLED;

            case SHIPPED ->
                    nextStatus == DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };
    }
}
