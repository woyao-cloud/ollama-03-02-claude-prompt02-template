package com.usermanagement.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ImportResult DTO 测试
 */
class ImportResultTest {

    @Test
    @DisplayName("应该创建导入结果对象")
    void shouldCreateImportResult() {
        // Given & When
        ImportResult result = new ImportResult();
        result.setTotal(10);
        result.setSuccess(8);
        result.setFailed(2);
        result.setErrors(List.of("Row 3: Invalid email", "Row 7: Duplicate email"));

        // Then
        assertThat(result.getTotal()).isEqualTo(10);
        assertThat(result.getSuccess()).isEqualTo(8);
        assertThat(result.getFailed()).isEqualTo(2);
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("全部成功时返回成功状态")
    void shouldReturnSuccessWhenAllImported() {
        // Given & When
        ImportResult result = new ImportResult(10, 10, 0, List.of());

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("有失败记录时返回失败状态")
    void shouldReturnFailureWhenHasErrors() {
        // Given & When
        ImportResult result = new ImportResult(10, 8, 2, List.of("Error 1"));

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getFailed()).isEqualTo(2);
    }

    @Test
    @DisplayName("应该使用 Builder 模式创建对象")
    void shouldCreateWithBuilder() {
        // Given & When
        ImportResult result = ImportResult.builder()
                .total(5)
                .success(5)
                .failed(0)
                .errors(List.of())
                .build();

        // Then
        assertThat(result.getTotal()).isEqualTo(5);
        assertThat(result.getSuccess()).isEqualTo(5);
        assertThat(result.isSuccess()).isTrue();
    }
}
