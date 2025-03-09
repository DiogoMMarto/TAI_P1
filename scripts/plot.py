import matplotlib.pyplot as plt
import numpy as np
import sys
import re

y = []
file_path = sys.argv[1]
parts = re.split(r'[\\.]+', file_path)
file_name = parts[-2]
ext = parts[-1]
if ext == "bin":
    with open(file_path, "rb") as f: 
        y = np.fromfile(f, dtype=np.float64)
if ext == "csv":
    with open(file_path, "r") as f:
        y = np.loadtxt(f)
y = -1*y/np.log(2)

# S = int(len(y)**(1/2))
S = 1
print(y)
print(S)
y = np.convolve(y, np.ones(S)/S, mode='valid')
plt.ylim(0,np.max(y)+0.5)
plt.title(file_name)
plt.xlabel("Index")
plt.ylabel("Entropy")
plt.plot(y)
plt.savefig(file_name + ".png")

