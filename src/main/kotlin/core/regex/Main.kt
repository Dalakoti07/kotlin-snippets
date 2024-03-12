package core.regex

fun removeTextBetweenSquareBrackets(input: String): String {
    val regex = "\\[.*?]".toRegex()
    return input.replace(regex, "")
}

fun parseText(input: String): StyledTextModel? {
    val regex = "\\[(.*?)\\]".toRegex()
    val matches = regex.findAll(input)
    val styledTextIndices = mutableListOf<StyledTextModel.StyledTextIndices>()
    var currentIndex = 0
    val cleanedText = removeTextBetweenSquareBrackets(input)

    for (match in matches) {
        val matchText = match.value
        val matchStartIndex = input.indexOf(matchText, currentIndex)
        val matchEndIndex = matchStartIndex + matchText.length
        val matchValue = matchText.substring(1, matchText.length - 1)
        val values = matchValue.split(";")

        val startIndex = values[0].toIntOrNull()
        val endIndex = values[1].toIntOrNull()
        if (startIndex == null) {
            return StyledTextModel(cleanedText)
        }
        if (endIndex == null) {
            return StyledTextModel(cleanedText)
        }
        var colorCode: String? = null
        var textStyle: String? = null
        values.subList(2, values.size).forEach { operation ->
            if (operation.startsWith("#")) {
                colorCode = operation
            } else if (operation == "bold" || operation == "normal" || operation == "italic") {
                textStyle = operation
            }
        }

        styledTextIndices.add(
            StyledTextModel.StyledTextIndices(
                startIndex = startIndex,
                endIndex = endIndex,
                color = colorCode,
                style = textStyle,
                isUnderLined = false,
                sizeProportion = null,
            )
        )

        currentIndex = matchEndIndex
    }

    return StyledTextModel(cleanedText, styledTextIndices)
}

fun main() {
    val model = StyledTextModel(
        text = "Your referral should report to the HUB within 7 days of referral submission",
        styledTextIndices = listOf(
            StyledTextModel.StyledTextIndices(
                startIndex = 46,
                endIndex = 53,
                color = "#12372A",
            ),
        ),
    ).getAllIndicesForSpannableComposeText()
    println("model -> $model")
}