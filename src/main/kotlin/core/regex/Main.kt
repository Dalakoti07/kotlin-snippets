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

    for (match in matches) {
        val matchText = match.value
        val matchStartIndex = input.indexOf(matchText, currentIndex)
        val matchEndIndex = matchStartIndex + matchText.length
        val matchValue = matchText.substring(1, matchText.length - 1)
        val values = matchValue.split(";")

        val startIndex = values[0].toIntOrNull()
        val endIndex = values[1].toIntOrNull()
        if (startIndex == null) {
            return null
        }
        if (endIndex == null) {
            return null
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

    val cleanedText = removeTextBetweenSquareBrackets(input)

    return StyledTextModel(cleanedText, styledTextIndices)
}


fun main() {
    val input = "I am saurabh[5;12;#3456] dalakoti[13;21;bold;#4567] ₹1000[23;27;#]"
//I am saurabh dalakoti

    val response = parseText(input)
    println(response)
}