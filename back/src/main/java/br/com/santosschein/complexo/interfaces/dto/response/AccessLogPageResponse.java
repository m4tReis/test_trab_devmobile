package br.com.santosschein.complexo.interfaces.dto.response;

import java.util.List;

public record AccessLogPageResponse(
        List<AccessLogResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
