package com.codecraft.aidoc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Generic pagination wrapper that keeps the API payload consistent across list endpoints.
 *
 * @param <T> item type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private long total;

    private long page;

    private long size;

    @Builder.Default
    private List<T> records = Collections.emptyList();

    public static <T> PageResponse<T> of(long total, long page, long size, List<T> records) {
        return PageResponse.<T>builder()
                .total(total)
                .page(page)
                .size(size)
                .records(records)
                .build();
    }
}
