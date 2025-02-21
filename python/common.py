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
        table[(context,next_char)] = table.setdefault((context,next_char),0) + 1
    
    sums = {}
    for k,v in table.items():
        sums[k[0]] = sums.setdefault(k[0],0) + v

    sum_ = 0
    for k,v in table.items():
        # sum_ += v * ( log(v + alpha,2) - log(sums[k[0]] + alpha * len(alphabet),2) )
        table[k] = (v + alpha) / (sums[k[0]] + alpha * len(alphabet))
    # print(sum_*-1/len(text))

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
                # next_char = find_best_leven(table,context,k)
                next_char = find_best_prefix(table,context[1:]) # context not in table
        else:
            # next_char = find_best_leven(table,context,k)
            next_char = find_best_prefix(table,context) # context too small (only for user input)
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

dp = None
def levenshtein_distance(s1, s2):
    # Create a matrix to store distances
    m = len(s1)
    n = len(s2)
    global dp
    if dp is None:
        dp = [[0] * (m + 1) for _ in range(m + 1)]

    # Initialize the matrix
    for i in range(m + 1):
        dp[i][0] = i
    for j in range(n + 1):
        dp[0][j] = j

    # Fill the matrix
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if s1[i - 1] == s2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
            else:
                dp[i][j] = min(dp[i - 1][j],      # Deletion
                               dp[i][j - 1],      # Insertion
                               dp[i - 1][j - 1]) + 1  # Substitution

    # The Levenshtein distance is the value in the bottom-right corner
    return dp[m][n]

def find_best_leven(table: dict[str,int], context: str , k: int)-> str|None:
    levenshtein_min = 10000
    context = " " * (k - len(context)) + context
    best_key = None
    for key in table:
        distance = levenshtein_distance(key[0],context)
        if distance < levenshtein_min:
            levenshtein_min = distance
            best_key = key
    return best_key[1]