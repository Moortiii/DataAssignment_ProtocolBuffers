import java.io.FileOutputStream
import java.nio.file.{Files, Paths}

import in.Result
import out.SolvedResult

object FileHandlerScala {
  def ParsePuzzles():List[Board] = {
    val neighbors = Result.parseFrom(Files.readAllBytes(Paths.get("puzzle_unsolved.bin")))
    var boards = List[Board]()

    for(p <- neighbors.puzzles) {
      val puzzle = Puzzle(Math.sqrt(p.squares.size.asInstanceOf[Double]).asInstanceOf[Int])

      var squares = List[Square]()
      
      var x = 0
      var y = 0
      
      for(sq <- p.squares) {
        if(x >= puzzle.size) {
          x = 0
          y += 1
        }
        
        var square = Square(x, y, puzzle, if(sq.value == 0) (1 to puzzle.size).toList else List(sq.value))

        if(sq.neighborDown)
          square = square.addNeighbor(Direction.DOWN)

        if(sq.neighborRight)
          square = square.addNeighbor(Direction.RIGHT)
   
        squares :+= square
        
        x += 1
      }
      
      boards :+= Board(squares, puzzle)
    }
    
    var parsed = List[Board]()
    
    for(board <- boards) {
      var temp_board = board
      
      for(sq <- temp_board.squares) {
        var updated = sq
        
        val neighbor_up = temp_board.getNeighbor(updated, Direction.UP)
        val neighbor_left = temp_board.getNeighbor(updated, Direction.LEFT)
        
        if(neighbor_up.isDefined) {
          if (neighbor_up.get.neighbors.contains(Direction.DOWN)) {
            updated = updated.addNeighbor(Direction.UP)
          }
        }
          
        if(neighbor_left.isDefined) {
          if(neighbor_left.get.neighbors.contains(Direction.RIGHT)) {
            updated = updated.addNeighbor(Direction.LEFT)
          }
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
      
      for(sq <- board.squares) {
        puzzle = puzzle.addValues(sq.values(0))
      }
      
      result = result.addPuzzles(puzzle)
    }
  
    result.writeTo(new FileOutputStream(filename))
  }
}