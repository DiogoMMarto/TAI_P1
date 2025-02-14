import argparse
from common import open_file , estimate_table , prob

def main():
    # ./fcm input_text.txt -k 3 -a 0.01
    parser = argparse.ArgumentParser(description="Calculate the average information content of a Sequence.")
    parser.add_argument("input", help="Input text file")
    parser.add_argument("-k","--depth", type=int, default=2 , help="Depth of the context")
    parser.add_argument("-a","--alpha", type=float, default=1.0 , help="Smoothing factor")
    args = parser.parse_args()
        
    text = open_file(args.input)
    alphabet = set(text)
    table = estimate_table(text, args.depth, args.alpha, alphabet)
    probability = prob(text, args.depth, table)
    print(probability)
   
    
if __name__ == "__main__":
    main()