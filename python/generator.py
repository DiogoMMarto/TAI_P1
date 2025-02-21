import argparse
import sys
import time
from common import open_file , estimate_table , gen , gen_yield

def main():
    parser = argparse.ArgumentParser(description="Generate text following a learned model.")
    parser.add_argument("input", help="Input text file")
    parser.add_argument("-k","--depth", type=int, default=2 , help="Depth of the context")
    parser.add_argument("-a","--alpha", type=float, default=1.0 , help="Smoothing factor")
    parser.add_argument("-p","--prior", type=str, default="abc" , help="Prior string")
    parser.add_argument("-s","--size", type=int, default=500 , help="Size of the text to generate")
    args = parser.parse_args()
    
    text = open_file(args.input)
    alphabet = set(text)
    table = estimate_table(text, args.depth, args.alpha, alphabet)
    print(args.prior,end="")
    gen = gen_yield(args.prior, args.depth, alphabet, table, args.size)
    for char in gen:
        sys.stdout.write(char)
        sys.stdout.flush()
        

if __name__ == "__main__":
    main()