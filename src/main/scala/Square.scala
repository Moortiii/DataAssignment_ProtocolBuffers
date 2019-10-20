import Direction.Direction

case class Square(x:Int, y:Int,
             puzzle:Puzzle,
             values:List[Int],
             neighbors:List[Direction] = List[Direction](),
             solved:Boolean = false)
{
  def setValue(value:Int):Square = {
    copy(values = List(value), solved = true)
  }
  
  def setValues(value_list:List[Int]):Square = {
    copy(values = value_list, solved = value_list.size == 1)
  }
  
  def removeValue(value:Int):Square = {
    if(solved) return this
    
    val altered_values = values.filter(_ != value)
    
    copy(values = altered_values, solved = altered_values.size == 1)
  }
  
  def removeValues(values_to_remove:List[Int]):Square = {
    if(solved) return this
    
    val altered_values = values.diff(values_to_remove)
    
    copy(values = altered_values, solved = altered_values.size == 1)
  }
  
  def setNeighbors(directions:List[Direction]):Square = {
    copy(neighbors = directions)
  }
  
  def addNeighbor(direction:Direction):Square = {
    copy(neighbors = (neighbors :+ direction).distinct, solved = values.size == 1)
  }
  
  def removeNeighbor(direction:Direction):Square = {
    copy(solved = values.size == 1, neighbors = neighbors.filter(_ != direction))
  }
  
  def getImpossibleValuesForNeighbors:List[Int] = {
    val impossible_neighbor_values = for(value <- values)
      yield (1 to puzzle.size).toList.diff(List(value + 1, value - 1))
    
    impossible_neighbor_values
        .flatten
        .filter(x => x > 0 && x <= puzzle.size)
  }
  
  def getImpossibleValuesForNonNeighbors:List[Int] = {
    val impossible_neighbor_values = for(value <- values)
      yield List(value, value + 1, value - 1)

    impossible_neighbor_values
        .flatten
        .filter((x) => x > 0 && x <= puzzle.size)
  }
  
  def getPossibleValuesForNeighbors:List[Int] = {
    val neighbor_values_lists = for(value <- values) yield List(value + 1, value - 1)
    
    neighbor_values_lists
      .flatten
      .distinct
      .filter((value) => value > 0 && value <= puzzle.size)
      .sortWith(_ < _)
  }
  
  def getPossibleValuesForNonNeighbors:List[Int] = {
    val non_neighbor_values_list = (for(value <- values) yield List(value, value + 1, value -1))
        .flatten
        .distinct
        .filter((value) => value > 0 && value <= puzzle.size)
        .sortWith(_ < _)
    
    (1 to puzzle.size).diff(non_neighbor_values_list).toList
  }
  
  override def toString:String = {
    s"$x, $y: (${values.mkString(", ")}), [${neighbors.mkString(", ")}]. Solved: $solved"
  }
}