package io.element.android.x.core.compose

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class PairCombinedPreviewParameter<T1, T2>(
    private val provider: Pair<PreviewParameterProvider<T1>, PreviewParameterProvider<T2>>
) : PreviewParameterProvider<Pair<T1, T2>> {
    override val values: Sequence<Pair<T1, T2>>
        get() = provider.first.values.flatMap { first ->
            provider.second.values.map { second ->
                first to second
            }
        }
}
