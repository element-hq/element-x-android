package io.element.android.x.matrix.media

import coil.key.Keyer
import coil.request.Options

internal class MediaKeyer : Keyer<MediaResolver.Meta> {
    override fun key(data: MediaResolver.Meta, options: Options): String? {
        return "${data.source.url()}_${data.kind}"
    }
}