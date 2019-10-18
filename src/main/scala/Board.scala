import Direction.Direction

case class Board(squares:List[Square], puzzle:Puzzle, solved:Boolean = false) {
  def getSquare(x:Int, y:Int):Square = {
    squares.filter((square:Square) => square.x == x && square.y == y)(0)
  }
  
  def replaceSquare(square:Square):Board = {
    val filtered = squares.filter((s:Square) => !(s.x == square.x && s.y == square.y))
    val replaced = (filtered :+ square).sortWith((s1, s2) => s1.x < s2.x).sortWith((s1, s2) => s1.y < s2.y)
    
    copy(squares = replaced, solved = replaced.forall((s:Square) => s.solved))
  }

  def getAllFromColumn(x:Int):List[Square] = {
    squares.filter(_.x == x).sortWith((s1, s2) => s1.y < s2.y)
  }

  def getAllFromRow(y:Int):List[Square] = {
    squares.filter(_.y == y).sortWith((s1, s2) => s1.x < s2.x)
  }
  
  def printSquares():Unit = {
    squares.sortWith((s1, s2) => s1.x < s2.x).sortWith((s1, s2) => s1.y < s2.y).foreach(println)
  }
  
  def getNeighbor(square:Square, direction: Direction):Option[Square] = {
    try {
      direction match {
        case Direction.UP => Some(getSquare(square.x, square.y - 1))
        case Direction.DOWN => Some(getSquare(square.x, square.y + 1))
        case Direction.LEFT => Some(getSquare(square.x - 1, square.y))
        case Direction.RIGHT => Some(getSquare(square.x + 1, square.y))
      }
    } catch {
      case e: Exception => None
    }
  }
}
