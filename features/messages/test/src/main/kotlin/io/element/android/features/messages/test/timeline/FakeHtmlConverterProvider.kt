/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.messages.test.timeline

import androidx.compose.runtime.Composable
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.wysiwyg.utils.HtmlConverter

class FakeHtmlConverterProvider(
    private val transform: (String) -> CharSequence = { it },
) : HtmlConverterProvider {

    @Composable
    override fun Update(currentUserId: UserId) = Unit

    override fun provide(): HtmlConverter {
        return object : HtmlConverter {
            override fun fromHtmlToSpans(html: String): CharSequence {
                return transform(html)
            }
        }
    }
}
