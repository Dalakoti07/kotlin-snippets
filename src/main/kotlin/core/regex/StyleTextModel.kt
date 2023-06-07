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
}
