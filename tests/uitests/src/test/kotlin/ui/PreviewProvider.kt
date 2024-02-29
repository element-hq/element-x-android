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

package ui

import com.airbnb.android.showkase.models.Showkase
import com.google.testing.junit.testparameterinjector.TestParameter

object PreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<TestPreview> {
        val metadata = Showkase.getMetadata()
        val components = metadata.componentList.map(::ComponentTestPreview)
        val colors = metadata.colorList.map(::ColorTestPreview)
        val typography = metadata.typographyList.map(::TypographyTestPreview)

        return (components + colors + typography).filter { !it.toString().contains("compound") }
    }
}
