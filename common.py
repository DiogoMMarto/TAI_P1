from math import log
import random

def open_file(file_path: str)-> str:
    with open(file_path,"r",encoding="utf-8") as f:
        return f.read()

def estimate_table(text: str,k: int, alpha: float, alphabet: set[str])-> dict[str,int]:
    table = {}
    for i in range(len(text) - k):
        context = text[i:i+k]
        next_char = text[i+k]
        table[context] = table.setdefault((context,next_char),0) + 1
    
    sums = {}
    for k,v in table.items():
        sums[k[0]] = sums.setdefault(k[0],0) + v

    for k,v in table.items():
        table[k] = (v + alpha) / (sums[k[0]] + alpha * len(alphabet))
        
    return table

def prob(text:str, k: int, table: dict[str,int])-> float:
    prob = 0
    for i in range(len(text) - k):
        context = text[i:i+k]
        next_char = text[i+k]
        prob += log(table[(context,next_char)],2)
    return -1/len(text) * prob

def gen_yield(prior: str, k: int, alphabet: set[str], table: dict[str,int], size: int):
    text = prior
    # TODO: ON BAD CASES USE MAYBE LIKE levenstein DISTANCE (https://en.wikipedia.org/wiki/Levenshtein_distance)
    for _ in range(size):
        context = text[-k:]
        if len(context) >= k:
            probs = [(v,key[1]) for key,v in table.items() if key[0] == context]
            if len(probs) > 0:
                next_char = max(probs,key=lambda x: x[0])[1]
            else:
                next_char = find_best_prefix(table,context[1:]) # context not in table
        else:
            prefix = find_best_prefix(table,context) # context too small (only for user input)
            next_char = prefix
        text += next_char
        yield next_char
    return

def gen(prior: str, k: int, alphabet: set[str], table: dict[str,int], size: int)-> str:
    generator = gen_yield(prior,k,alphabet,table,size)
    text = prior.join(generator)
    return text
        
def find_best_prefix(table: dict[str,int], context: str)-> str|None: 
    prefixes = [(v,key[0],key[1]) for key,v in table.items() if key[0].endswith(context)]
    if len(prefixes) == 0:
        return find_best_prefix(table,context[1:])
    return max(prefixes)[2]
