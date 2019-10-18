object PuzzleSolver extends App {
    val solver_methods = List[(Square, Board, Puzzle) => Square](
        Solver.removeValuesBasedOnSolvedNonNeighbors,
        Solver.removeValuesBasedOnSolvedNeighbors,
        Solver.resolveUniqueValuesForRow, 
        Solver.resolveUniqueValuesForColumn,
        Solver.removeUsedValuesForColumn,
        Solver.removeUsedValuesForRow,
        Solver.removeValuesBasedOnUnsolvedNonNeighbors,
        Solver.removeValuesBasedOnUnsolvedNeighbors
    )

    // Uses the sources generated from --scala_out to parse the puzzles
    val boards:List[Board] = FileHandlerScala.ParsePuzzles()
    
    // Uses the sources generated from --java_out to parse the puzzles
    //val boards:List[Board] = FileHandlerJava.ParsePuzzles()
    
    var solved:List[Board] = List[Board]()
    
    for(board <- boards) {
        var temp_board = board
        
        // Testing reveals that if we are unable to solve the board in 25 iterations
        // it is very likely we will never be able to solve it. This simply prevents
        // us from locking up on an impossible puzzle.
        for(_ <- 1 to 25) {
            temp_board = Solver.solve(temp_board, temp_board.puzzle, solver_methods)
        }

        solved +:= temp_board
    }

    // Uses the sources generated from --scala_out to construct messages
    FileHandlerScala.WriteOutput(solved, "puzzle_solved.bin")
    
    // Uses the sources generated from --java_out to construct messages
    //FileHandlerJava.WriteOutput(solved, "puzzle_solved.bin")
    
    println("Solved puzzles serialized and written to 'puzzle_solved.bin'. Terminating..")
}
