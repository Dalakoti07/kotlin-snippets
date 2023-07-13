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
    val input = "I am saurabh dalakoti[13;#2C66E3] , you have won 1000"
//    val input = ""
//I am saurabh dalakoti

    // [13;19;#2C66E3]
    // [13; #2C66E3;] bold;

    val response = parseText(input)
    println(response)
}