import out_pb2
import math

def generate_puzzle_strings(result):
    puzzles = []

    for puzzle in result.puzzles:
        size = puzzle.size
        print(f"Size: {size}")

        output = f"size {size}x{size}"

        
        for index, value in enumerate(puzzle.values):
            print(f"Value: {value}")
            
            if index % size == 0:
                output += "\n" + str(value) + " "
            else:
                output += str(value) + " "
        
        puzzles += output + "\n"
    
    return puzzles

protocol_buffer = out_pb2.SolvedResult()

with open("puzzle_solved.bin", "rb") as f:
    protocol_buffer.ParseFromString(f.read())

output = generate_puzzle_strings(protocol_buffer)

with open("puzzle_solved.txt", "w+") as f:
    f.write(f"puzzles {len(protocol_buffer.puzzles)}\n")
    
    for puzzle in output:
        f.write(puzzle)

    print("Results written to 'puzzle_solved.txt'. Terminating..")