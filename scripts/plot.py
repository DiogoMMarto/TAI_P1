# open file a.csv and plot it

import matplotlib.pyplot as plt
import numpy as np
# cli input
import sys

y = []
file_path = sys.argv[1]
file_name = sys.argv[1].split(".")[0]
with open(file_path, "rb") as f: 
    # read c doubles from file
    y = np.fromfile(f, dtype=np.float64)
y = -1*y/np.log(2)

# S = int(len(y)**(1/2))
S = 1000
print(S)
y = np.convolve(y, np.ones(S)/S, mode='valid')

plt.ylim(0,np.max(y)+0.5)
plt.title(file_name)
plt.xlabel("Index")
plt.ylabel("Entropy")
plt.plot(y)
plt.savefig(file_name + ".png")

