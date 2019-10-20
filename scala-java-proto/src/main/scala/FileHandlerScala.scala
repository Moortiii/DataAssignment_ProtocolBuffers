import java.io.FileOutputStream
import java.nio.file.{Files, Paths}

import in.Result
import out.SolvedResult

object FileHandlerScala {
  def ParsePuzzles():List[Board] = {
    val neighbors = Result.parseFrom(Files.readAllBytes(Paths.get("puzzle_unsolved.bin")))
    var boards = List[Board]()

    for(puzzle_buffer <- neighbors.puzzles) {
      // The size of the puzzle is always equal to the square
      // root of the number of elements present in the puzzle
      val size = Math.sqrt(puzzle_buffer.squares.size.asInstanceOf[Double]).asInstanceOf[Int]
      val puzzle = Puzzle(size)
      
      // We removed the x and y from the protocol buffer
      // to save space since we can calculate it ourselves
      var x = 0
      var y = 0

      var squares = List[Square]()

      for(square_buffer <- puzzle_buffer.squares) {
        if(x >= puzzle.size) {
          x = 0
          y += 1
        } 
        
        val value = if(square_buffer.value == 0)
          (1 to puzzle.size).toList else List(square_buffer.value)
        
        var square = Square(x, y, puzzle, value)

        // There are three possible neighbor situations, DOWN, RIGHT or BOTH
        // each of these have been mapped to an identifier to save space
        
        // Only neighbor right     = 1
        // Only neighbor down      = 2
        // Neighbor down and right = 3
        
        if(square_buffer.neighbors == 3) {
          square = square.addNeighbor(Direction.DOWN)
          square = square.addNeighbor(Direction.RIGHT)
        } else if(square_buffer.neighbors == 2) {
          square = square.addNeighbor(Direction.DOWN)
        } else if(square_buffer.neighbors == 1) {
          square = square.addNeighbor(Direction.RIGHT)
        }
   
        squares :+= square

        x += 1
      }
      
      boards :+= Board(squares, puzzle)
    }
    
    // Now that we know if each element has a number to the
    // right and down, we can manually calculate if they
    // also have a neighbor up and to the left
    var parsed = List[Board]()
    
    for(board <- boards) {
      var temp_board = board
      
      for(square <- temp_board.squares) {
        var updated = square
        
        val neighbor_up   = temp_board.getNeighbor(updated, Direction.UP)
        val neighbor_left = temp_board.getNeighbor(updated, Direction.LEFT)
        
        // Logic dictates that if the square above the current square has a
        // neighbor down, the current square must have a neighbor above it
        if(neighbor_up.isDefined) {
          if (neighbor_up.get.neighbors.contains(Direction.DOWN))
            updated = updated.addNeighbor(Direction.UP)
        }

        // Logic dictates that if the square above the current square has a
        // neighbor to the right, the current square must have a neighbor to
        // the left of it
        if(neighbor_left.isDefined) {
          if (neighbor_left.get.neighbors.contains(Direction.RIGHT))
            updated = updated.addNeighbor(Direction.LEFT)
        }
        
        temp_board = temp_board.replaceSquare(updated)
      }
      
      parsed :+= temp_board
    }

    parsed
  }
  
  
  def WriteOutput(boards:List[Board], filename:String):Unit = {
    var result = SolvedResult()
    
    for(board <- boards.reverse) {
      var puzzle = out.PuzzleSolution().withSize(Math.sqrt(board.squares.size.asInstanceOf[Double]).asInstanceOf[Int])
      
      for(square <- board.squares)
        puzzle = puzzle.addValues(square.values(0))
      
      result = result.addPuzzles(puzzle)
    }
  
    result.writeTo(new FileOutputStream(filename))
  }
}