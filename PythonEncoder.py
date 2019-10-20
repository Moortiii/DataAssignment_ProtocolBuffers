import re
import in_pb2

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
        
        # We have no use for the size of the current puzzle
        output = output[1:]

        filtered.append(output)

    return filtered


def serialize_puzzle_to_file(filename, buffer):
    with open(filename, "wb") as f:
        f.write(buffer.SerializeToString())
        
        print(f"Puzzle serialized and written to file '{filename}'. Terminating..")


def add_buffer_neighbor_identifier(neighbors, square_buffer):
    # Remove duplicate neighbors
    neighbors = set(neighbors)

    # Map the neighbor different neighbor situations to identifiers
    # this saves us two bytes when a square has a neighbor both to
    # its right and down compared to using booleans.
    if neighbors >= set(["RIGHT", "DOWN"]):
        square_buffer.neighbors = 3
    elif neighbors >= set(["DOWN"]):
        square_buffer.neighbors = 2
    elif neighbors >= set(["RIGHT"]):
        square_buffer.neighbors = 1
    else:
        square_buffer.neighbors = 0

    return square_buffer


def add_buffer_value(value, square_buffer):
    if value == "_":
        square_buffer.value = 0
    else:
        square_buffer.value = int(value)
    
    return square_buffer


def get_numbers_and_wildcards(puzzle):
    """
    Retrieve all wildcards (_) and pre-solved numbers. We can set the value
    of the current square based on this and remove it from the array afterwards.
    """
    
    return re.findall(r"(\d+|_)", str(puzzle))

def parse_puzzle(puzzle, puzzle_buffer):
    # Grab all the solved numbers and wildcards from the puzzle before we normalize it
    numbers = get_numbers_and_wildcards(puzzle)

    # Replace all numbers and wildcards with an asterisk to normalize the puzzle
    puzzle = [re.sub(r"(\d+|_)", "*", line) for line in puzzle]

    # Parse the current puzzle
    number_line = True

    # Loop through each value in the puzzle and create a square_buffer for each
    # we only need to find the neighbors to the right and down in order to determine
    # all of the other neighbors for the puzzle.
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
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                    else:
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                elif l_index == len(puzzle) - 1:
                    if c_index == 0:
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                    elif c_index == len(line) - 1:
                        pass
                    else:
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                else:
                    if c_index == 0:
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                    elif c_index == len(line) - 1:
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")
                    else:
                        if line[c_index + 2] == "x":
                            neighbors.append("RIGHT")
                        if puzzle[l_index + 1][c_index] == "x":
                            neighbors.append("DOWN")

                square_buffer = puzzle_buffer.squares.add()

                square_buffer = add_buffer_neighbor_identifier(neighbors, square_buffer)
                square_buffer = add_buffer_value(numbers[0], square_buffer)
                
                numbers = numbers[1:]
            
            number_line = not number_line


if __name__ == "__main__":
    # Protocol buffer to store the results
    buffer = in_pb2.Result()

    # Parse the input files
    puzzles = read_puzzle_file("puzzle_unsolved.txt")

    # Populate the protocol buffer based on the puzzles
    [parse_puzzle(puzzle, buffer.puzzles.add()) for puzzle in puzzles]

    # Write the output binary
    serialize_puzzle_to_file("puzzle_unsolved.bin", buffer)