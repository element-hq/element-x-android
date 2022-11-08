package io.element.android.x.designsystem.components.avatar

import androidx.compose.runtime.Stable

@Stable
data class AvatarData(
    val name: String = "",
    val model: ByteArray? = null,
    val size: AvatarSize = AvatarSize.MEDIUM
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AvatarData

        if (name != other.name) return false
        if (model != null) {
            if (other.model == null) return false
            if (!model.contentEquals(other.model)) return false
        } else if (other.model != null) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (model?.contentHashCode() ?: 0)
        result = 31 * result + size.value
        return result
    }

}