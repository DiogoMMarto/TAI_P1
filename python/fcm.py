import argparse
import time
from math import log

def open_file(file_path: str)-> str:
    with open(file_path,"rb") as f:
        return f.read()

def estimate_prob(text: str,ko: int, alpha: float, alphabet: set[str])-> dict[str,int]:
    table = {}
    _sum = 0
    const_term = alpha * len(alphabet)
    for i in range(len(text) - ko):
        context = text[i:i+ko]
        next_char = text[i+ko]
        context_table, total = table.get(context,({},0))
        count = context_table.get(next_char,0)
        
        symbol_length = log(( count+alpha) / (total+const_term))
        _sum += symbol_length
        
        context_table[next_char] = count + 1
        table[context] = (context_table, total + 1)
        
    return _sum*-1/(len(text)-ko)/log(2)


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
    