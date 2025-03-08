import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv('allResults.csv', header=1, names=['file','alpha','k','entropy'])

df['alpha'] = df['alpha'].astype(float)
df['k'] = df['k'].astype(int)
df['entropy'] = df['entropy'].astype(float)

# Sort the alpha values so lines are in ascending order
df = df.sort_values(by='alpha')

# Get unique file names to create one subplot per sequence
sequences = df['file'].unique()
sequences = sorted(sequences)
num_sequences = len(sequences)

rows = (num_sequences + 1) // 2
cols = 2
plt.figure(figsize=(10, 4 * rows))

for i, seq in enumerate(sequences, start=1):
    subset = df[df['file'] == seq].copy()
    pivot_data = subset.pivot(index='k', columns='alpha', values='entropy')

    ax = plt.subplot(rows, cols, i)
    pivot_data.plot(ax=ax, marker='o')
    ax.set_title(seq)
    ax.set_xlabel('k')
    ax.set_ylabel('Entropy (bits/symbol)')
    ax.legend(title='alpha', loc='best')

plt.tight_layout()
# plt.show()
plt.savefig('result_plots.png')