object Solver {
    // Takes in a list of solver methods and applies them to the board in consecutive order.
    // This is the applications entry-point for the solver. In order to improve the solver
    // we simply add new methods to the list that handles different scenarios.
    def solve(board:Board, puzzle:Puzzle, solver_methods:List[(Square, Board, Puzzle) => Square]):Board = {
        var temp_board = board
        
        for(method <- solver_methods) {
            temp_board = resolveValuesUsingMethod(temp_board, puzzle, method)
        }

        temp_board
    }
    
    // Takes in a method that can be used to remove impossible values from a square based
    // on some conditions and applies this method to all squares in the board, gradually
    // removing more values until only the solution is left and the square is solved
    def resolveValuesUsingMethod(board:Board, puzzle:Puzzle, solver_method: (Square, Board, Puzzle) => Square):Board = {
        var temp_board = board
        
        for(i <- 0 until temp_board.squares.length) {
            temp_board = temp_board.replaceSquare(solver_method(temp_board.squares(i), temp_board, puzzle))
        }
        
        temp_board
    }
    
    // Looks for solved neighbors next to the square and removes values that are not possible
    // for that neighbor.
    
    // Example: A: (1, 2, 3, 4, 5) and B: (4) are neighbors. Since A is a neighbor of B, it can
    // only hold the value 3 or 5. Therefore we can remove 1, 2 and 4 leaving us with A: (3, 5)
    def removeValuesBasedOnSolvedNeighbors(square:Square, board:Board, puzzle:Puzzle):Square = {
        var modified = square
         
        for(direction <- modified.neighbors) {
            val neighbor = board.getNeighbor(square, direction)
            
            if(neighbor.isDefined) {
                if(neighbor.get.solved) {
                    val impossible_values = neighbor.get.getImpossibleValuesForNeighbors
                    modified = modified.removeValues(impossible_values)
                }  
            }
        }
        
        modified
    }

    // Looks for solved non-neighbors next to the square and removes values that are not possible
    // for that non-neighbor.
    
    // Example: A: (1, 2, 3, 4, 5) and B: (3) are non-neighbors. Since A is a non-neighbor of B it
    // can not hold any values that are a neighbor of the solution in B, in this case (2, 3, 4)
    // this leaves us with A: (1, 5)
    def removeValuesBasedOnSolvedNonNeighbors(square:Square, board:Board, puzzle:Puzzle):Square = {
        var modified = square

        for(direction <- Direction.values if !modified.neighbors.contains(direction)) {
            val neighbor = board.getNeighbor(square, direction)

            if(neighbor.isDefined) {
                if(neighbor.get.solved) {
                    val impossible_values = neighbor.get.getImpossibleValuesForNonNeighbors

                    modified = modified.removeValues(impossible_values)   
                }
            }
        }

        modified
    }
    
    // Determines if there are values present for an unsolved non-neighbor that are also in
    // the intersection of impossible values for non-neighbors for all values in the set
    
    // Example: A: (1, 2) is next to B: (1, 2, 3, 4) and they are not neighbors.
    // If A is set to 1 the impossible values for B are (1, 2)
    // If A is set to 2 the impossible values for B are (1, 2, 3)
    // The intersection of the sets reveal that 1 and 2 are impossible values in either case
    // therefore we can disregard these values even though neither A nor B is not yet solved.
    def removeValuesBasedOnUnsolvedNonNeighbors(square:Square, board:Board, puzzle:Puzzle):Square = {
        var modified = square
        
        for(direction <- Direction.values if !modified.neighbors.contains(direction)) {
            val neighbor = board.getNeighbor(square, direction)
            
            if(neighbor.isDefined) {
                if(!neighbor.get.solved) {
                    val neighbor_values = neighbor.get.values
                    val neighbor_values_size = neighbor_values.size
                    
                    if(neighbor_values_size < modified.values.size) {
                        val impossible_neighbor_values = neighbor.get.getImpossibleValuesForNonNeighbors
                        
                        for(value <- impossible_neighbor_values) {
                            val count = impossible_neighbor_values.count(_ == value)
                            
                            if (count == neighbor_values_size) {
                                modified = modified.removeValue(value)
                            }
                        }
                    }
                    
                }
            }
        }
        
        modified
    }

    // Determines if there are values present for an unsolved neighbor that are also in
    // the intersection of impossible values for neighbors for all values in the set

    // Example: A: (1, 2) is next to B: (1, 2, 3, 4) and they are neighbors.
    // If A is set to 1 the impossible values for B are (2, 3, 4)
    // If A is set to 2 the impossible values for B are (4)
    // The intersection of the sets reveal that 4 is an impossible values in either case
    // therefore we can disregard these values even though neither A nor B is not yet solved.
    def removeValuesBasedOnUnsolvedNeighbors(square:Square, board:Board, puzzle:Puzzle):Square = {
        var modified = square
        
        for(direction <- modified.neighbors) {
            val neighbor = board.getNeighbor(modified, direction)
            
            if(neighbor.isDefined) {
                if(!neighbor.get.solved) {
                    val neighbor_values = neighbor.get.values
                    val neighbor_values_size = neighbor_values.size
                    
                    if(neighbor_values_size < modified.values.size) {
                        val impossible_neighbor_values = neighbor.get.getImpossibleValuesForNeighbors
                        
                        for(value <- impossible_neighbor_values) {
                            if(impossible_neighbor_values.count(_ == value) == neighbor_values_size) {
                                modified = modified.removeValue(value)
                            }
                        }
                    }
                }
            }
        }
        
        modified
    }
    
    // Removes any solved value from all unsolved squares for a given column
    def removeUsedValuesForColumn(square:Square, board:Board, puzzle:Puzzle):Square = {
        if(square.solved) return square
        
        val column = board.getAllFromColumn(square.x)
        val used_values = retrieveUsedValuesFromLine(column)
        
        square.removeValues(used_values)
    }
    
    // Removes any solved value from all unsolved squares for a given row
    def removeUsedValuesForRow(square:Square, board:Board, puzzle:Puzzle):Square = {
        if(square.solved) return square
        
        
        val row = board.getAllFromRow(square.y)
        val used_values = retrieveUsedValuesFromLine(row)
        
        square.removeValues(used_values)
    }

    // Resolves any unique values on a column and sets the solution accordingly for the square that contains it 
    def resolveUniqueValuesForColumn(square:Square, board:Board, puzzle:Puzzle):Square = {
        val column = board.getAllFromColumn(square.x)
        removeUniqueValuesForLine(square, column, board, puzzle)
    }

    // Resolves any unique values on a row and sets the solution accordingly for the square that contains it
    def resolveUniqueValuesForRow(square:Square, board:Board, puzzle:Puzzle):Square = {
        val row = board.getAllFromRow(square.y)
        removeUniqueValuesForLine(square, row, board, puzzle)
    }

    // Helper method to reuse the same logic for removing unique values from both rows and columns
    def removeUniqueValuesForLine(square:Square, squares:List[Square], board:Board, puzzle:Puzzle):Square = {
        var modified = square

        val unique_values = retrieveUniqueValuesFromLine(squares)

        if(unique_values.nonEmpty && modified.values.containsSlice(unique_values)) {
            val unique_value = modified.values.intersect(unique_values)(0)
            modified = modified.setValue(unique_value)
        }

        modified
    }

    // Determines if there are values present on a line that occur in only a single element

    // Example: A: (1, 2, 3, 4, 5), B: (1, 2, 3, 4), C: (1, 2, 3, 4, 6, 7, 8) are on the same line
    // A is the only square that contains the value 5, therefore it can be set immediately as
    // the each number _has_ to be present on a line.
    def retrieveUniqueValuesFromLine(squares:List[Square]):List[Int] = {
        var resolved = List[List[Int]]()

        squares.foreach(square => {
            val filtered = squares.filter(_ != square)

            val unique = for(value <- square.values if filtered.forall(!_.values.contains(value)))
                yield value

            resolved :+= unique
        })

        resolved.flatten
    }

    // Helper method to reuse the same logic for retrieving unique values from both rows and columns
    def retrieveUsedValuesFromLine(squares:List[Square]):List[Int] = {
        var resolved = List[List[Int]]()
        
        squares.foreach(square => {
            val unique = for(value <- square.values if square.solved) yield value
            
            resolved :+= unique
        })
        
        resolved.flatten
    }
}