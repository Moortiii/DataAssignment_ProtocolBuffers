import java.io.{FileInputStream, FileOutputStream}

import Neighbors.Result

object FileHandlerJava {
  def ParsePuzzles():List[Board] = {
    var boards = List[Board]()
    
    val neighbors:Result = Neighbors.Result.parseFrom(new FileInputStream("puzzle_unsolved.bin"))
    val puzzles = neighbors.getPuzzlesList
    
    puzzles.forEach((p) => {
      val square_list = p.getSquaresList
      var squares = List[Square]()
      val puzzle = Puzzle(p.getSize)
      
      square_list.forEach((sq) => {
        var values:List[Int] = List[Int]()
        
        for(value <- sq.getValuesList.toArray) {
          values :+= value.asInstanceOf[Int]
        }
        
        var square = Square(sq.getX, sq.getY, puzzle, values)

        if(sq.getNeighborDown)
          square = square.addNeighbor(Direction.DOWN)

        if(sq.getNeighborLeft)
          square = square.addNeighbor(Direction.LEFT)

        if(sq.getNeighborRight)
          square = square.addNeighbor(Direction.RIGHT)

        if(sq.getNeighborUp)
          square = square.addNeighbor(Direction.UP)
        
        squares :+= square
      })
      
      boards :+= Board(squares, puzzle)
    })
    
    boards
  }
  
  def WriteOutput(boards:List[Board], filename:String):Unit = {
    var result_builder = Neighbors.Result.newBuilder()

    for(board <- boards.reverse) {
      val puzzle_builder = Neighbors.Puzzle.newBuilder()
        .setSize(board.puzzle.size)
        
      for(sq <- board.squares) {
        var square = Neighbors.Square.newBuilder()
        
        if(sq.solved) {
          square = square.addValues(sq.values(0))
        } else {
          square = square.addValues(0)
        }
        
        puzzle_builder.addSquares(square.build())
      }
      
      val puzzle = puzzle_builder.build()
      
      result_builder = result_builder.addPuzzles(puzzle)
    }
    
    result_builder.build().writeTo(new FileOutputStream(filename))
  }
}
