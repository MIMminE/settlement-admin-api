package nuts.commerce.settlement.common.response;

public record ApiResponse<T>(
        boolean success,
        T data,
        Object error
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(Object error) {
        return new ApiResponse<>(false, null, error);
    }
}