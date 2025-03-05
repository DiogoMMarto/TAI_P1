# open file a.csv and plot it

import matplotlib.pyplot as plt
import numpy as np
# cli input
import sys

y = []
file_path = sys.argv[1]
with open(file_path, "rb") as f: 
    # read c doubles from file
    y = np.fromfile(f, dtype=np.float64)
y = -1*y

S = int(len(y)**(1/2))
print(S)
# apply convolution with kernel of size S with 1/S using np.convolve
y = np.convolve(y, np.ones(S)/S, mode='valid')

plt.ylim(0,np.max(y)+0.5)
plt.plot(y)
plt.show()