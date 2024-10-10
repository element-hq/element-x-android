/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.uri

import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UriExtensionTest {
    @Test
    fun `url with prompt parameter should replace existing value`() {
        val url = "https://beta.element.io/account/authorize" +
            "?response_type=code" +
            "&client_id=01J9RB9MEJCMVHWNYYHTVDNBVJ" +
            "&redirect_uri=io.element%3A%2Fcallback" +
            "&scope=scope" +
            "&state=x61ILblUF6BwOTUA" +
            "&nonce=N7TdVfDhyVNF9PbH" +
            "&prompt=consent" +
            "&code_challenge_method=S256" +
            "&code_challenge=bDV2DWX0j0U-QtewSUJeXr3DEmvFxlHfQN_1UxXpOUk"
        val result = url.toUri()
            .setQueryParameter("prompt", "create")
            .toString()
        assertThat(result).isEqualTo(
            "https://beta.element.io/account/authorize" +
                "?response_type=code" +
                "&client_id=01J9RB9MEJCMVHWNYYHTVDNBVJ" +
                "&redirect_uri=io.element%3A%2Fcallback" +
                "&scope=scope" +
                "&state=x61ILblUF6BwOTUA" +
                "&nonce=N7TdVfDhyVNF9PbH" +
                "&code_challenge_method=S256" +
                "&code_challenge=bDV2DWX0j0U-QtewSUJeXr3DEmvFxlHfQN_1UxXpOUk" +
                "&prompt=create"
        )
    }

    @Test
    fun `url without prompt parameter should add the parameter`() {
        val url = "https://beta.element.io/account/authorize" +
            "?response_type=code" +
            "&client_id=01J9RB9MEJCMVHWNYYHTVDNBVJ" +
            "&redirect_uri=io.element%3A%2Fcallback" +
            "&scope=scope" +
            "&state=x61ILblUF6BwOTUA" +
            "&nonce=N7TdVfDhyVNF9PbH" +
            "&code_challenge_method=S256" +
            "&code_challenge=bDV2DWX0j0U-QtewSUJeXr3DEmvFxlHfQN_1UxXpOUk"
        val result = url.toUri()
            .setQueryParameter("prompt", "create")
            .toString()
        assertThat(result).isEqualTo(
            "https://beta.element.io/account/authorize" +
                "?response_type=code" +
                "&client_id=01J9RB9MEJCMVHWNYYHTVDNBVJ" +
                "&redirect_uri=io.element%3A%2Fcallback" +
                "&scope=scope" +
                "&state=x61ILblUF6BwOTUA" +
                "&nonce=N7TdVfDhyVNF9PbH" +
                "&code_challenge_method=S256" +
                "&code_challenge=bDV2DWX0j0U-QtewSUJeXr3DEmvFxlHfQN_1UxXpOUk" +
                "&prompt=create"
        )
    }
}
