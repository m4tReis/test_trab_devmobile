package br.com.santosschein.complexo.shared.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class ApiErrorResponseWriter {

    public void write(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
                {"status":%d,"error":"%s","message":"%s","path":"%s","timestamp":"%s"}\
                """.formatted(
                status.value(),
                escape(status.getReasonPhrase()),
                escape(message),
                escape(path),
                OffsetDateTime.now()
        ));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
