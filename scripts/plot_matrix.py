import csv
import matplotlib.pyplot as plt
import sys

def read_csv(filename):
    """Read the CSV file and return the data."""
    data = []
    with open(filename, mode='r') as file:
        reader = csv.reader(file)
        next(reader)  # Skip the header row
        for row in reader:
            k = int(row[0])
            a = float(row[1])
            result = float(row[2])
            data.append((k, a, result))
    return data

def plot_results(data):
    """Plot the results."""
    k_values = sorted(set(k for k, a, result in data))
    a_values = sorted(set(a for k, a, result in data))
    
    # Prepare a matrix for plotting, with k_values as rows and a_values as columns
    plot_matrix = [[None for _ in k_values] for _ in a_values]

    for k, a, result in data:
        k_index = k_values.index(k)
        a_index = a_values.index(a)
        plot_matrix[a_index][k_index] = result

    # Plotting each line of the matrix for different k values
    plt.figure(figsize=(10, 6))
    for i, k in enumerate(a_values):
        plt.plot(k_values, [plot_matrix[i][j] for j in range(len(k_values))], label=f'a={k}', marker='o')

    plt.xlabel('K values')
    plt.xticks([0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20])
    plt.ylabel('Entropy')
    plt.title('Experiment Results for Varying k and a')
    plt.legend()
    plt.grid(True)
    plt.show()

def main():
    # Get the input CSV file name from the user
    input_file = sys.argv[1]
    if input_file == None:
        print("Please provide the input CSV file name as an argument.")
        exit(1)
    
    # Read data from the CSV file
    data = read_csv(input_file)
    
    # Plot the results
    plot_results(data)

if __name__ == "__main__":
    main()
