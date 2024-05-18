import FillInParser.Companion.BLANK
import java.io.File
import java.util.function.Function
import kotlin.reflect.KFunction

enum class Direction(val unit: Point) {
    Vertical(Point(0, 1)),
    Horizontal(Point(1, 0));

    fun component(p: Point): Int = when (this) {
        Vertical -> p.y
        Horizontal -> p.x
    }
}

data class Point(val x: Int, val y: Int) {
    operator fun plus(o: Point) = Point(x + o.x, y + o.y)
    operator fun minus(o: Point) = Point(x - o.x, y - o.y)
    operator fun times(i: Int) = Point(x * i, y * i)
}


data class Item(
    val start: Point,
    val direction: Direction,
    val length: Int
) {
    val cells = Array(length) { start + (direction.unit * it) }
}

class FillIn(
    val board: Array<Array<Char>>,
    val items: List<Item>,
    val words: List<String>
) {
    val usedWords = mutableSetOf<String>()

    init {
        for (item in items) {
            val content = values(item)
            if (content.all { it != BLANK })
                usedWords.add(content)
        }
    }

    fun values(item: Item) = item.cells.map { p -> board[p.y][p.x] }.joinToString("")

    fun matchingWords(item: Item) = (words - usedWords)
        .filter { w ->
            w.length == item.length
                    && w.zip(values(item)).all { p ->
                p.second == BLANK || p.first == p.second
            }
        }

    fun solve(item: Item, word: String) {
        usedWords.add(word)
        item.cells.forEachIndexed { i, p -> board[p.y][p.x] = word[i] }
    }

    private fun onlyOptionSolver(item: Item): Item? {
        if (matchingWords(item).size == 1) {
            return item
        }
        return null
    }

    private fun failSolver(item: Item): Item? {
        throw NotImplementedError()
    }

    val solvers = listOf<(Item) -> Item?>(
        this::onlyOptionSolver,
        this::failSolver
        )

    fun chooseSolveNextCell() {
        for (canSolve in solvers) {
            for (item in items) {
                val solved = canSolve(item)
                if (solved != null) {
                    solve(item, matchingWords(item)[0])
                    return
                }
            }
        }

    }

    override fun toString(): String = board.map { it.joinToString("") }.joinToString("\n")
}

fun main(argv: Array<String>) {
    val lines = File(argv[0]).readLines()
    val parser = FillInParser(lines)
    val fillin = parser.toFillIn()

    while (fillin.usedWords.size != fillin.words.size) {
        fillin.chooseSolveNextCell()
        println(fillin)
        println()
    }

}

class FillInParser(lines: List<String>) {
    private val board = mutableListOf<String>()
    private val words = mutableListOf<String>()

    init {
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
        assert(board.map { it.length }.toSet().size == 1)
    }

    fun at(p: Point) = board.getOrNull(p.y)?.getOrNull(p.x) ?: BLOCKED

    fun itemFrom(p: Point, direction: Direction): Item? {
        if (at(p) != BLOCKED && at(p - direction.unit) == BLOCKED) {
            var i = p
            while (at(i) != BLOCKED) {
                i += direction.unit
            }
            return Item(p, direction, direction.component(i - p))
        }
        return null
    }

    fun extractItems(): List<Item> {
        val items = mutableListOf<Item>()
        for (y in 0..<board.size) {
            for (x in 0..<board[y].length) {
                val p = Point(x, y)
                val ih = itemFrom(p, Direction.Horizontal)
                val iv = itemFrom(p, Direction.Vertical)
                if (ih != null) items.add(ih)
                if (iv != null) items.add(iv)
            }
        }
        return items
    }

    fun toArrayBoard() = Array(board.size) { y -> Array(board[y].length) { x -> at(Point(x, y)) } }

    fun toFillIn() = FillIn(
        board = toArrayBoard(),
        items = extractItems(),
        words = words,
    )


    companion object {
        val BLANK = ' '
        val BLOCKED = '-'
    }
}

