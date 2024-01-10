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

package io.element.android.libraries.designsystem.colors

import com.google.common.truth.Truth.assertThat
import io.element.android.compound.theme.avatarColorsDark
import io.element.android.compound.theme.avatarColorsLight
import org.junit.Test

class AvatarColorsTest {
    @Test
    fun `ensure the size of the avatar color are equal for light and dark theme`() {
        assertThat(avatarColorsDark.size).isEqualTo(avatarColorsLight.size)
    }

    @Test
    fun `compute string hash`() {
        assertThat("@alice:domain.org".toHash()).isEqualTo(6)
        assertThat("@bob:domain.org".toHash()).isEqualTo(3)
        assertThat("@charlie:domain.org".toHash()).isEqualTo(0)
    }

    @Test
    fun `compute string hash reverse`() {
        assertThat("0".toHash()).isEqualTo(0)
        assertThat("1".toHash()).isEqualTo(1)
        assertThat("2".toHash()).isEqualTo(2)
        assertThat("3".toHash()).isEqualTo(3)
        assertThat("4".toHash()).isEqualTo(4)
        assertThat("5".toHash()).isEqualTo(5)
        assertThat("6".toHash()).isEqualTo(6)
        assertThat("7".toHash()).isEqualTo(7)
    }
}
