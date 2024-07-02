/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("DEPRECATION")

package ui

import com.google.testing.junit.testparameterinjector.TestParameter
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

object ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    private val values: List<IndexedValue<ComposablePreview<AndroidPreviewInfo>>> by lazy {
        AndroidComposablePreviewScanner()
            .scanPackageTrees("io.element.android")
            .getPreviews()
            .withIndex()
            .toList()
    }

    override fun provideValues(): List<IndexedValue<ComposablePreview<AndroidPreviewInfo>>> = this.values
}

object Shard1ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 0 }.map { it.value }
}

object Shard2ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 1 }.map { it.value }
}

object Shard3ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 2 }.map { it.value }
}

object Shard4ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 3 }.map { it.value }
}
