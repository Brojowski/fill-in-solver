import java.io.File

val EXAMPLE = "C:\\Users\\alex\\IdeaProjects\\fill-in-solver\\src\\example.txt"

class GameBoard(
    words: List<String>,
    board: List<String>,
) {
    companion object {
        val BLANK = '-'
    }
}

fun main() {
    parseFillInFile()
}

fun parseFillInFile() {
    val lines  = File(EXAMPLE).readLines()

    val board = mutableListOf<String>()
    val words = mutableListOf<String>()
    for (line in lines) {
        val char = line.first()
        if (char == '|') {
            board.add(line.trim('|'))
        } else if (char == '=') {
            // do nothing with table borders
        } else {
            words.add(line.trim())
        }
    }
    // All are the same length
    assert(board.map{ it.length }.toSet().size == 1)

    board.forEach { println(it) }
    words.forEach { println(it) }
}