package nuts.commerce.settlement.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import nuts.commerce.settlement.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        return ApiResponse.fail(ApiErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                e.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .orElse("Invalid request"),
                req.getRequestURI(),
                (String) req.getAttribute("traceId")
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException e, HttpServletRequest req) {
        return ApiResponse.fail(ApiErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                e.getMessage(),
                req.getRequestURI(),
                (String) req.getAttribute("traceId")
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        return ApiResponse.fail(ApiErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                e.getMessage(),
                req.getRequestURI(),
                (String) req.getAttribute("traceId")
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleIllegalState(IllegalStateException e, HttpServletRequest req) {
        return ApiResponse.fail(ApiErrorResponse.of(
                ErrorCode.CONFLICT,
                e.getMessage(),
                req.getRequestURI(),
                (String) req.getAttribute("traceId")
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleDenied(AccessDeniedException e, HttpServletRequest req) {
        return ApiResponse.fail(ApiErrorResponse.of(
                ErrorCode.FORBIDDEN,
                "Forbidden",
                req.getRequestURI(),
                (String) req.getAttribute("traceId")
        ));
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ApiResponse<Void> handleRse(org.springframework.web.server.ResponseStatusException e, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        ErrorCode code = status == HttpStatus.NOT_FOUND ? ErrorCode.NOT_FOUND : ErrorCode.INVALID_REQUEST;

        return ApiResponse.fail(ApiErrorResponse.of(
                code,
                e.getReason() != null ? e.getReason() : status.getReasonPhrase(),
                req.getRequestURI(),
                (String) req.getAttribute("traceId")
        ));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception e, HttpServletRequest req) {
        return ApiResponse.fail(ApiErrorResponse.of(
                ErrorCode.INTERNAL_ERROR,
                "Internal error",
                req.getRequestURI(),
                (String) req.getAttribute("traceId")
        ));
    }
}