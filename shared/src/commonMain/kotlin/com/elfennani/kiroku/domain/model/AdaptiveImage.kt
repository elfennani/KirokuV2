package com.elfennani.kiroku.domain.model

data class AdaptiveImage(
    val extraLarge: String,
    val large: String,
    val medium: String,
    val color: String?
) {
    companion object {
        fun construct(
            extraLarge: String?,
            large: String?,
            medium: String?,
            color: String?
        ): AdaptiveImage? {
            if (extraLarge == null && large == null && medium == null) {
                return null
            }

            return AdaptiveImage(
                extraLarge = extraLarge ?: large ?: medium!!,
                large = large ?: extraLarge ?: medium!!,
                medium = medium ?: large ?: extraLarge!!,
                color = color
            )
        }
    }
}

