package com.kfokam48.backend.dto;

public record ApiError(String code, String message, Long reessayerDansSecondes) {
    public ApiError(String code, String message) {
        this(code, message, null);
    }
}
