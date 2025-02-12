import java.util.*
import kotlin.random.Random
import kotlin.system.exitProcess

// Reversi

fun drawBoard(board: Array<MutableList<Char>>) {
    // This function prints out the board that it was passed. Returns None.

    val horizontalLine = "  +---+---+---+---+---+---+---+---+"
    val verticalLine = "  |   |   |   |   |   |   |   |   |"

    println("    1   2   3   4   5   6   7   8")
    println(horizontalLine)
    for (y in 0..7) {
        println(verticalLine)
        print("${y + 1} ")
        for (x in 0..7) {
            print("| ${board[x][y]}")
            print(" ")
        }
        println("|")
        println(verticalLine)
        println(horizontalLine)
    }
    println("    1   2   3   4   5   6   7   8")
}

fun resetBoard(board: Array<MutableList<Char>>) {
    // Blanks out the board it is passed, except for the original starting position.
    for (x in 0..7) {
        for (y in 0..7) {
            board[x][y] = ' '
        }
    }
    // Starting pieces:
    board[3][3] = 'X'
    board[3][4] = 'O'
    board[4][3] = 'O'
    board[4][4] = 'X'
}

fun getNewBoard(): Array<MutableList<Char>> {
    // Creates a brand new, blank board data structure.
    val board = Array(8) { MutableList(8) { ' ' } }
    return board
}

fun isOnBoard(x: Int, y: Int): Boolean { // Returns True if the coordinates are located on the board.
    return x in 0..7 && y in 0..7
}

fun isValidMove(board: Array<MutableList<Char>>, tile: Char, xStart: Int, yStart: Int): Any {
    // Returns False if the player's move on space xStart, yStart is invalid.
    // If it is a valid move, returns a list of spaces that would become the player's if they made a move here.
    if (board[xStart][yStart] != ' ' || !isOnBoard(xStart, yStart)) {
        return false
    }

    board[xStart][yStart] = tile // temporarily set the tile on the board.

    val otherTile = if (tile == 'X') 'O' else 'X'

    val tilesToFlip = mutableListOf<Pair<Int, Int>>()
    val directions = listOf(
        Pair(0, 1), Pair(1, 1), Pair(1, 0), Pair(1, -1),
        Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(-1, 1)
    )
    for ((xDirection, yDirection) in directions) {
        var x = xStart
        var y = yStart
        x += xDirection // first step in the direction
        y += yDirection // first step in the direction
        if (isOnBoard(x, y) && board[x][y] == otherTile) {
            x += xDirection
            y += yDirection
            if (!isOnBoard(x, y)) {
                // continue in for loop
                continue
            }
            while (board[x][y] == otherTile) {
                x += xDirection
                y += yDirection
                if (!isOnBoard(x, y)) { // break out of while loop, then continue in for loop
                    break
                }
            }
            if (!isOnBoard(x, y)) {
                continue
            }
            if (board[x][y] == tile) {
                // There are pieces to flip over. Go in the reverse direction until we reach the original space, noting all the tiles along the way.
                while (true) {
                    x -= xDirection
                    y -= yDirection
                    if (x == xStart && y == yStart) {
                        break
                    }
                    tilesToFlip.add(Pair(x, y))
                }
            }
        }
    }

    board[xStart][yStart] = ' ' //restore the empty space
    if (tilesToFlip.isEmpty()) { // If no tiles were flipped, this is not a valid move.
        return false
    }
    return tilesToFlip
}

fun getBoardWithValidMoves(board: Array<MutableList<Char>>, tile: Char): Array<MutableList<Char>> {
    // Returns a new board with . marking the valid moves the given player can make.
    val dupeBoard = getBoardCopy(board)

    for ((x, y) in getValidMoves(dupeBoard, tile)) {
        dupeBoard[x][y] = '.'
    }

    return dupeBoard
}

fun getValidMoves(board: Array<MutableList<Char>>, tile: Char): List<Pair<Int, Int>> {
    // Returns a list of [x,y] lists of valid moves for the given player on the given board
    val validMoves = mutableListOf<Pair<Int, Int>>()
    for (x in 0..7) {
        for (y in 0..7) {
            if (isValidMove(board, tile, x, y) != false) {
                validMoves.add(Pair(x, y))
            }
        }
    }
    return validMoves
}

fun getScoreOfBoard(board: Array<MutableList<Char>>): Map<Char, Int> {
    // Determine the score by counting the tiles. Returns a dictionary with keys 'X' and 'O'.
    var xScore = 0
    var oScore = 0
    for (x in 0..7) {
        for (y in 0..7) {
            if (board[x][y] == 'X') {
                xScore += 1
            }
            if (board[x][y] == 'O') {
                oScore += 1
            }
        }
    }
    return mapOf('X' to xScore, 'O' to oScore)
}

fun enterPlayerTile(): Pair<Char, Char> {
    // Lets the player type which tile they want to be.
    // Returns a list with the player's tile as the first item, and the computer's tile as the second.
    var tile: Char? = null
    while (!(tile == 'X' || tile == 'O')) {
        println("Do you want to be X or O?")
        val inputTile = readlnOrNull() ?: ""
        if (inputTile.isNotEmpty()) {
            tile = inputTile.uppercase(Locale.getDefault())[0]
        }
    }
    // the first element in the list is the player's tile, the second is the computer's tile.
    return if (tile == 'X') Pair('X', 'O') else Pair('O', 'X')
}

fun whoGoesFirst(): String {
    // Randomly choose the player who goes first.
    return if (Random.nextInt(2) == 0) "computer" else "player"
}

fun playAgain(): Boolean {
    // This function returns True if the player wants to play again, otherwise it returns False.
    println("Do you want to play again? (yes or no)")
    val inputLine = readlnOrNull() ?: ""
    return inputLine.lowercase(Locale.getDefault()).startsWith("y")
}

fun makeMove(board: Array<MutableList<Char>>, tile: Char, xStart: Int, yStart: Int): Boolean {
    // Place the tile on the board at xStart, yStart, and flip any of the opponent's pieces.
    // Returns False if this is an invalid move, True if it is valid.
    val tilesToFlipAny = isValidMove(board, tile, xStart, yStart)
    if (tilesToFlipAny == false) {
        return false
    }
    @Suppress("UNCHECKED_CAST")
    val tilesToFlip = tilesToFlipAny as List<Pair<Int, Int>>
    board[xStart][yStart] = tile
    for ((x, y) in tilesToFlip) {
        board[x][y] = tile
    }
    return true
}

fun getBoardCopy(board: Array<MutableList<Char>>): Array<MutableList<Char>> {
    // Make a duplicate of the board list and return the duplicate.
    val dupeBoard = getNewBoard()
    for (x in 0..7) {
        for (y in 0..7) {
            dupeBoard[x][y] = board[x][y]
        }
    }
    return dupeBoard
}

fun isOnCorner(x: Int, y: Int): Boolean {
    // Returns True if the position is in one of the four corners.
    return (x == 0 && y == 0) ||
            (x == 7 && y == 0) ||
            (x == 0 && y == 7) ||
            (x == 7 && y == 7)
}

fun getPlayerMove(board: Array<MutableList<Char>>, playerTile: Char): Any {
    // Let the player type in their move.
    // Returns the move as [x, y] (or returns the strings 'hints' or 'quit')
    val digits = "1 2 3 4 5 6 7 8".split(" ")
    while (true) {
        println("Enter your move in column-row format (ex. 18 will be the bottom-right corner), or type \"quit\" to end the game")
        val move = (readlnOrNull() ?: "").lowercase(Locale.getDefault())
        if (move == "quit") {
            return "quit"
        }
        if (move.length == 2 && digits.contains(move[0].toString()) && digits.contains(move[1].toString())) {
            val x = move[0].toString().toInt() - 1
            val y = move[1].toString().toInt() - 1
            if (isValidMove(board, playerTile, x, y) == false) {
                continue
            } else {
                return Pair(x, y)
            }
        } else {
            println("That is not a valid move. Type the x digit (1-8), then the y digit (1-8).")
            println("For example, 81 will be the top-right corner.")
        }
    }
}

fun getComputerMove(board: Array<MutableList<Char>>, computerTile: Char): Pair<Int, Int> {
    // Given a board and the computer's tile, determine where to
    // move and return that move as a [x, y] list.
    val possibleMoves = getValidMoves(board, computerTile).toMutableList()

    // randomize the order of the possible moves
    possibleMoves.shuffle()

    // always go for a corner if available.
    for ((x, y) in possibleMoves) {
        if (isOnCorner(x, y)) {
            return Pair(x, y)
        }
    }

    // Go through all the possible moves and remember the best scoring move
    var bestScore = -1
    var bestMove: Pair<Int, Int> = possibleMoves[0]
    for ((x, y) in possibleMoves) {
        val dupeBoard = getBoardCopy(board)
        makeMove(dupeBoard, computerTile, x, y)
        val score = getScoreOfBoard(dupeBoard)[computerTile] ?: 0
        if (score > bestScore) {
            bestMove = Pair(x, y)
            bestScore = score
        }
    }

    return bestMove
}

lateinit var mainBoard: Array<MutableList<Char>>

fun showPoints(playerTile: Char, computerTile: Char) {
    // Prints out the current score.
    val scores = getScoreOfBoard(mainBoard)
    println("You have ${scores[playerTile]} points. The computer has ${scores[computerTile]} points.")
}

fun main() {
    // MAIN
    println("Welcome to Othello!")
    Thread.sleep(2000)

    while (true) {
        // Reset the board and game.
        mainBoard = getNewBoard()
        resetBoard(mainBoard)
        val (playerTile, computerTile) = enterPlayerTile()
        var turn = whoGoesFirst()
        println("The $turn will go first.")
        Thread.sleep(2000)

        while (true) {
            if (turn == "player") {
                // Player's turn.

                val validMovesBoard = getBoardWithValidMoves(mainBoard, playerTile)
                drawBoard(validMovesBoard)
                Thread.sleep(2000)
                showPoints(playerTile, computerTile)
                Thread.sleep(2000)

                // Get the player's move
                val moveAny = getPlayerMove(mainBoard, playerTile)
                if (moveAny is String && moveAny == "quit") {
                    println("Thanks for playing!")
                    exitProcess(0) //terminate the program
                } else {
                    @Suppress("UNCHECKED_CAST")
                    val move = moveAny as Pair<Int, Int>
                    println("Your move was: [${move.first + 1}, ${move.second + 1}]")
                    Thread.sleep(2000)
                    makeMove(mainBoard, playerTile, move.first, move.second)
                    Thread.sleep(2000)
                }

                if (getValidMoves(mainBoard, computerTile).isEmpty()) {
                    break
                } else {
                    turn = "computer"
                }
            } else {
                // Computer's turn.
                drawBoard(mainBoard)
                showPoints(playerTile, computerTile)
                println("Press Enter to see the computer's move.")
                readlnOrNull()
                val (x, y) = getComputerMove(mainBoard, computerTile)
                println("The computer's move was: [${x + 1}, ${y + 1}]")
                Thread.sleep(2000)
                makeMove(mainBoard, computerTile, x, y)


                if (getValidMoves(mainBoard, playerTile).isEmpty()) {
                    break
                } else {
                    turn = "player"
                }
            }
        }
        // Display the final score.
        drawBoard(mainBoard)
        Thread.sleep(2000)
        val scores = getScoreOfBoard(mainBoard)
        println("X scored ${scores['X']} points. O scored ${scores['O']} points.")
        Thread.sleep(2000)
        if ((scores[playerTile] ?: 0) > (scores[computerTile] ?: 0)) {
            println("You beat the computer by ${ (scores[playerTile] ?: 0) - (scores[computerTile] ?: 0) } points! Congratulations!")
        } else if ((scores[playerTile] ?: 0) < (scores[computerTile] ?: 0)) {
            println("You lost. The computer beat you by ${ (scores[computerTile] ?: 0) - (scores[playerTile] ?: 0) } points.")
        } else {
            println("The game was a tie!")
        }

        if (!playAgain()) {
            break
        }
    }
}
