import argparse
import time
from math import log

def open_file(file_path: str)-> str:
    with open(file_path,"r",encoding="utf-8") as f:
        return f.read()

def estimate_prob(text: str,ko: int, alpha: float, alphabet: set[str])-> dict[str,int]:
    table = {}
    sums = {}
    for i in range(len(text) - ko):
        context = text[i:i+ko]
        next_char = text[i+ko]
        table[(context,next_char)] = table.get((context,next_char),0) + 1
        
    const = alpha * len(alphabet)
    for k,v in table.items():
        sums[k[0]] = sums.get(k[0], const) + v
        
    sum_ = 0
    print(sorted(list(table.items())))
    for k,v in table.items():
        sum_ += v * log((v + alpha)/sums[k[0]])
    return sum_*-1/(len(text)-ko)/log(2)


def main():
    # ./fcm input_text.txt -k 3 -a 0.01
    parser = argparse.ArgumentParser(description="Calculate the average information content of a Sequence.")
    parser.add_argument("input", help="Input text file")
    parser.add_argument("-k","--depth", type=int, default=2 , help="Depth of the context")
    parser.add_argument("-a","--alpha", type=float, default=1.0 , help="Smoothing factor")
    parser.add_argument("-t","--timer", action="store_true", help="Print time")
    args = parser.parse_args()
        
    text = open_file(args.input)
    alphabet = set(text)
    t = time.time()
    probability = estimate_prob(text, args.depth, args.alpha, alphabet)
    print(probability)
    if args.timer:
        print("time:",time.time() - t)
    
if __name__ == "__main__":
    main()
    