import re
import in_pb2

# The parser for this looks ugly, but it's not stupid if it works

def read_puzzle_file(filename):
    with open(filename, "r") as f:
        lines = f.read()

    lines = re.split("size", lines)
    lines = [line.replace(" ", "size ", 1) for line in lines]

    puzzles = [line for line in lines[1:]]
    puzzles = [re.split("\n", puzzle) for puzzle in puzzles]

    filtered = []

    for puzzle in puzzles:
        output = []

        for line in puzzle:
            if line != "":
                output.append(line)
        
        filtered.append(output)

    return filtered


def serialize_puzzle_to_file(filename, protocol_buffer):
    with open(filename, "wb") as f:
        f.write(protocol_buffer.SerializeToString())
        
        print(f"Puzzle serialized and written to file '{filename}'. Terminating..")


def parse_puzzle(puzzle, puzzle_buffer):
    # Get the size of the current puzzle
    size = int(re.findall(r"(\d+)", puzzle[0])[0])

    # Remove the line which contains the size now that we have extracted it
    # Also remove the last empty line
    puzzle = puzzle[1:]

    """
    Retrieve all wildcards (_) and pre-solved numbers. We can set the value
    of the current square based on this and remove it from the array afterwards.
    """
    numbers = re.findall(r"(\d+|_)", str(puzzle))

    # Normalize the puzzle so that we can more easily parse the neighbors
    puzzle = [re.sub(r"(\d+|_)", "*", line) for line in puzzle]

    # Remove the last line which now contains an empty newline
    #puzzle = puzzle[:-1]

    # Parse the current puzzle
    number_line = True

    for l_index, line in enumerate(puzzle):
        for c_index, character in enumerate(line):

            neighbors = []

            if number_line and character != " " and character != "x":
                if(l_index == 0):
                    if c_index == 0:
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                    elif c_index == len(line) - 1:
                        if line[c_index - 2] == "x":
                            neighbors.append("LEFT")
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                    else:
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                        if line[c_index - 2] == "x":
                            neighbors.append("LEFT")
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                elif l_index == len(puzzle) - 1:
                    if c_index == 0:
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                        if puzzle[l_index - 1][c_index] == "x":
                            neighbors.append("UP")
                    elif c_index == len(line) - 1:
                        if line[c_index - 2] == "x":
                            neighbors.append("LEFT")
                        if puzzle[l_index - 1][c_index] == "x":
                            neighbors.append("UP")
                    else:
                        if puzzle[l_index - 1][c_index] == "x":
                            neighbors.append("UP")
                        if line[c_index - 2] == "x":
                            neighbors.append("LEFT")
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                else:
                    if c_index == 0:
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                        if puzzle[l_index - 1][c_index] == "x":
                            neighbors.append("UP")
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                    elif c_index == len(line) - 1:
                        if line[c_index - 2] == "x":
                            neighbors.append("LEFT")
                        if puzzle[l_index - 1][c_index] == "x":
                            neighbors.append("UP")
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                    else:
                        if puzzle[l_index - 1][c_index] == "x":
                            neighbors.append("UP")
                        if line[c_index - 2] == "x":
                            neighbors.append("LEFT")
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")

                square_buffer = puzzle_buffer.squares.add()

                if numbers[0] == "_":
                    square_buffer.value = 0
                else:
                    square_buffer.value = int(numbers[0])
                
                numbers = numbers[1:]

                square_buffer.x = c_index // 4
                square_buffer.y = l_index // 2
                square_buffer.neighbor_right = "RIGHT" in neighbors
                square_buffer.neighbor_down  = "DOWN"  in neighbors
            
            number_line = not number_line

puzzles = read_puzzle_file("puzzle_unsolved.txt")

protocol_buffer = in_pb2.Result()

for puzzle in puzzles:
    parse_puzzle(puzzle, protocol_buffer.puzzles.add())

serialize_puzzle_to_file("puzzle_unsolved.bin", protocol_buffer)