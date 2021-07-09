package coroutines.generators

fun main() {
  val sequence = sequenceExample().take(10)
  sequence.forEach {
    print("$it ")
  }
}

fun sequenceExample() = sequence {
  yieldAll(generateSequence(2) { it * 2 })
}