import matplotlib.pyplot as plt
import numpy as np
import sys

y = []
file_path = sys.argv[1]
file_name = sys.argv[1].split(".")[0]
with open(file_path, "rb") as f:
    f.readline()
    y = np.loadtxt(f)
print(y)
y = (-1*y)/np.log(2)
print(y)
# S = int(len(y)**(1/8))
S = 1
print(S)
y = np.convolve(y, np.ones(S)/S, mode='valid')

plt.figure()
plt.ylim(0,np.max(y)+0.5)
plt.title(file_name)
plt.xlabel("Index")
plt.ylabel("Entropy")
plt.plot(y)
plt.savefig(file_name + ".png")
plt.close()