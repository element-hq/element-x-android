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

package io.element.android.services.toolbox.impl.strings

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidStringProvider @Inject constructor(private val resources: Resources) : StringProvider {
    override fun getString(@StringRes resId: Int): String {
        return resources.getString(resId)
    }

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return resources.getString(resId, *formatArgs)
    }

    override fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any?): String {
        return resources.getQuantityString(resId, quantity, *formatArgs)
    }
}
