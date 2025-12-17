/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResultTest {
    @Test
    fun testFlatMap() {
        val initial = Result.success("initial")
        val otherResult = initial.flatMap { Result.success("other") }
        val errorResult = initial.flatMap { Result.failure<String>(IllegalStateException("error")) }

        assertThat(otherResult.getOrNull()).isEqualTo("other")
        assertThat(errorResult.exceptionOrNull()?.message).isEqualTo("error")
        try {
            initial.flatMap<String, String> { error("caught error") }
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("caught error")
        }

        val initialError = Result.failure<String>(IllegalStateException("initial error"))
        val mapErrorToSuccess = initialError.flatMap { Result.success("other") }
        val mapErrorToError = initialError.flatMap { Result.failure<String>(IllegalStateException("error")) }
        val mapErrorAndCatch: Result<String> = initialError.flatMap { error("error") }

        assertThat(mapErrorToSuccess.exceptionOrNull()?.message).isEqualTo("initial error")
        assertThat(mapErrorToError.exceptionOrNull()?.message).isEqualTo("initial error")
        assertThat(mapErrorAndCatch.exceptionOrNull()?.message).isEqualTo("initial error")
    }

    @Test
    fun testFlatMapCatching() {
        val initial = Result.success("initial")
        val otherResult = initial.flatMapCatching { Result.success("other") }
        val errorResult = initial.flatMapCatching { Result.failure<String>(IllegalStateException("error")) }
        val caughtExceptionResult: Result<String> = initial.flatMapCatching { error("caught error") }

        assertThat(otherResult.getOrNull()).isEqualTo("other")
        assertThat(errorResult.exceptionOrNull()?.message).isEqualTo("error")
        assertThat(caughtExceptionResult.exceptionOrNull()?.message).isEqualTo("caught error")

        val initialError = Result.failure<String>(IllegalStateException("initial error"))
        val mapErrorToSuccess = initialError.flatMapCatching { Result.success("other") }
        val mapErrorToError = initialError.flatMapCatching { Result.failure<String>(IllegalStateException("error")) }
        val mapErrorAndCatch: Result<String> = initialError.flatMapCatching { error("error") }

        assertThat(mapErrorToSuccess.exceptionOrNull()?.message).isEqualTo("initial error")
        assertThat(mapErrorToError.exceptionOrNull()?.message).isEqualTo("initial error")
        assertThat(mapErrorAndCatch.exceptionOrNull()?.message).isEqualTo("initial error")
    }
}
