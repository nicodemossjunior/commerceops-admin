package com.commerceops.admin.common.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "Standard zero-based paginated response")
public record PageResponse<T>(
        @Schema(description = "Resources on the current page") List<T> content,
        @Schema(description = "Zero-based current page index", example = "0") int page,
        @Schema(description = "Maximum requested page size", example = "20") int size,
        @Schema(description = "Total matching resources", example = "42") long totalElements,
        @Schema(description = "Total number of pages", example = "3") int totalPages,
        @Schema(description = "Whether this is the first page", example = "true") boolean first,
        @Schema(description = "Whether this is the last page", example = "false") boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
