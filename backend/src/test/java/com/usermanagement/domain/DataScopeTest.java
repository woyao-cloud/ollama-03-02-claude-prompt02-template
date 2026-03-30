package com.usermanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataScope 枚举单元测试
 */
class DataScopeTest {

    @Test
    @DisplayName("DataScope 枚举包含所有预期值")
    void shouldHaveAllExpectedValues() {
        // When
        DataScope[] values = DataScope.values();

        // Then
        assertThat(values).containsExactlyInAnyOrder(
            DataScope.ALL,
            DataScope.DEPT,
            DataScope.SELF,
            DataScope.CUSTOM
        );
    }

    @ParameterizedTest
    @EnumSource(DataScope.class)
    @DisplayName("所有 DataScope 值都不为 null")
    void shouldNotBeNull(DataScope dataScope) {
        // Then
        assertThat(dataScope).isNotNull();
    }

    @Test
    @DisplayName("ALL - 全部数据权限")
    void shouldRepresentAllData() {
        // When
        DataScope scope = DataScope.ALL;

        // Then
        assertThat(scope.name()).isEqualTo("ALL");
        assertThat(scope.toString()).isEqualTo("ALL");
    }

    @Test
    @DisplayName("DEPT - 本部门及下级部门数据权限")
    void shouldRepresentDepartmentData() {
        // When
        DataScope scope = DataScope.DEPT;

        // Then
        assertThat(scope.name()).isEqualTo("DEPT");
        assertThat(scope.toString()).isEqualTo("DEPT");
    }

    @Test
    @DisplayName("SELF - 仅个人数据权限")
    void shouldRepresentSelfData() {
        // When
        DataScope scope = DataScope.SELF;

        // Then
        assertThat(scope.name()).isEqualTo("SELF");
        assertThat(scope.toString()).isEqualTo("SELF");
    }

    @Test
    @DisplayName("CUSTOM - 自定义数据权限")
    void shouldRepresentCustomData() {
        // When
        DataScope scope = DataScope.CUSTOM;

        // Then
        assertThat(scope.name()).isEqualTo("CUSTOM");
        assertThat(scope.toString()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("valueOf - 从字符串获取枚举")
    void shouldGetEnumFromString() {
        // When
        DataScope all = DataScope.valueOf("ALL");
        DataScope dept = DataScope.valueOf("DEPT");
        DataScope self = DataScope.valueOf("SELF");
        DataScope custom = DataScope.valueOf("CUSTOM");

        // Then
        assertThat(all).isEqualTo(DataScope.ALL);
        assertThat(dept).isEqualTo(DataScope.DEPT);
        assertThat(self).isEqualTo(DataScope.SELF);
        assertThat(custom).isEqualTo(DataScope.CUSTOM);
    }

    @Test
    @DisplayName("values - 返回所有枚举值")
    void shouldReturnAllValues() {
        // When
        DataScope[] values = DataScope.values();

        // Then
        assertThat(values.length).isEqualTo(4);
    }
}
