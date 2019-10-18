import neighbors_pb2
import math

def generate_puzzle_strings(result):
    puzzles = []

    for puzzle in result.puzzles:
        size = int(math.sqrt(len(puzzle.squares)))

        output = f"size {size}x{size}"

        for index, square in enumerate(puzzle.squares):
            if index % size == 0:
                output += "\n" + str(square.value) + " "
            else:
                output += str(square.value) + " "
        
        puzzles += output + "\n"
    
    return puzzles

protocol_buffer = neighbors_pb2.Result()

with open("puzzle_solved.bin", "rb") as f:
    protocol_buffer.ParseFromString(f.read())

output = generate_puzzle_strings(protocol_buffer)

with open("puzzle_solved.txt", "w+") as f:
    f.write(f"puzzles {len(protocol_buffer.puzzles)}\n")
    
    for puzzle in output:
        f.write(puzzle)

    print("Results written to 'puzzle_solved.txt'. Terminating..")