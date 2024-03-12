package core.regex

data class StyledTextModel(var text: String? = null,
    var styledTextIndices: List<StyledTextIndices>? = null
){
    data class StyledTextIndices(
        var startIndex: Int? = null,
        var endIndex: Int? = null,
        var color: String? = null,
        var style: String? = null,
        val isUnderLined: Boolean? = null,
        val sizeProportion: Float? = null // e.g. 1.2f for bigger text, 0.7f for smaller text
    )

    fun getAllIndicesForSpannableComposeText(): StyledTextModel {
        val currentSegments = styledTextIndices
        if (currentSegments.isNullOrEmpty()) {
            // this is the default setting if nothing given
            // color is text primary and font is 14f
            return this.copy(
                styledTextIndices = listOf(
                    StyledTextIndices(
                        startIndex = 0,
                        endIndex = this.text?.length,
                        sizeProportion = 14f,
                        color = "#333333",
                    ),
                )
            )
        } else {
            val result = mutableListOf<StyledTextIndices>()
            var prevEnd = 0
            val defaultFont = currentSegments[0].sizeProportion
            currentSegments.forEach { each ->
                if (each.startIndex.orZero() > prevEnd) {
                    result.add(
                        StyledTextIndices(
                            startIndex = prevEnd,
                            endIndex = each.startIndex.orZero() - 1,
                            color = "#333333",
                            sizeProportion = defaultFont,
                        )
                    )
                }
                result.add(each)
                prevEnd = each.endIndex.orZero() + 1
            }
            if (prevEnd < this.text.orEmpty().length) {
                result.add(
                    StyledTextIndices(
                        startIndex = prevEnd,
                        endIndex = this.text.orEmpty().length - 1,
                        color = "#333333",
                        sizeProportion = 14f,
                    )
                )
            }
            return this.copy(
                styledTextIndices = result,
            )
        }
    }
}

fun Int?.orZero(): Int = this ?: 0
