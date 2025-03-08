import csv
import itertools
import subprocess
import argparse
import os

def run_experiment(k, a, input_file):
    """Run the ./fcm command with given k and a values and capture the output."""
    command = ["./fcm", input_file, "-k", str(k), "-a", str(a)]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        output = result.stdout.strip()
    except subprocess.CalledProcessError as e:
        output = f"Error: {e}"
    return output

def generate_data(input_file):
    """Generate experiment data by varying k and a values."""
    # Define parameter variations
    param_sets = [
        {'k_values': [1, 2, 3, 4, 5], 'a_values': [0, 0.1, 0.5, 1]},
        {'k_values': [1, 2, 3, 4, 5], 'a_values': [1, 10, 100, 1000, 10000]},
        {'k_values': [1, 10, 100, 100], 'a_values': [0, 0.1, 0.5, 1]},
    ]
    
    results = []
    
    for param_set in param_sets:
        k_values = param_set['k_values']
        a_values = param_set['a_values']
        
        for k, a in itertools.product(k_values, a_values):
            result = run_experiment(k, a, input_file)
            results.append([k, a, result])
    
    return results

def save_to_csv(filename, data):
    """Save data to a CSV file."""
    with open(filename, mode='w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(["k", "a", "result"])
        writer.writerows(data)

def main():
    # Parse command line arguments
    parser = argparse.ArgumentParser(description="Run the experiment with given input file.")
    parser.add_argument("input_file", help="The input file to be used for the experiment")
    args = parser.parse_args()
    
    # Generate data
    data = generate_data(args.input_file)
    
    # Derive output file name from input file name
    base_name = os.path.splitext(os.path.basename(args.input_file))[0]
    output_file = f"{base_name}_experiment_results.csv"
    
    # Save data to CSV
    save_to_csv(output_file, data)
    print(f"Data saved to {output_file}")

if __name__ == "__main__":
    main()
