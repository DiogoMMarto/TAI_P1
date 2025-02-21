#include <cstdlib>
#include <iostream>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <cmath>
#include <fstream>
#include <chrono>
#include <string_view>

using namespace std;

struct Args {
    string input;
    int depth = 2;
    double alpha = 1.0;
    bool timer = false;
};

Args parse_args(int argc, char* argv[]) {
    Args args;
    if (argc < 2) {
        cerr << "Usage: " << argv[0] << " input.txt [-k depth] [-a alpha]" << endl;
        exit(1);
    }
    args.input = argv[1];
    for (int i = 2; i < argc; ) {
        string arg = argv[i];
        if (arg == "-k" || arg == "--depth") {
            if (i + 1 >= argc) {
                cerr << "Missing value for " << arg << endl;
                exit(1);
            }
            args.depth = stoi(argv[i + 1]);
            i += 2;
        } else if (arg == "-a" || arg == "--alpha") {
            if (i + 1 >= argc) {
                cerr << "Missing value for " << arg << endl;
                exit(1);
            }
            args.alpha = stod(argv[i + 1]);
            i += 2;
        } else if (arg == "-t" || arg == "--timer") {
            args.timer = true;
            i += 1;
        } else if (arg == "-h" || arg == "--help") {
            cerr << "Usage: " << argv[0] << " input.txt [-k depth] [-a alpha]" << endl;
            exit(0);
        } else {
            cerr << "Unknown option: " << arg << endl;
            exit(1);
        }
    }
    return args;
}

string read_file(const string& file_path) {
    ifstream file(file_path, ios::binary | ios::ate);
    if (!file) {
        cerr << "Could not open file: " << file_path << endl;
        exit(1);
    }
    streamsize size = file.tellg();
    file.seekg(0, ios::beg);
    string text(size, '\0');
    if (!file.read(&text[0], size)) {
        cerr << "Error reading file: " << file_path << endl;
        exit(1);
    }
    return text;
}

double estimate_prob(const string& text, int ko, double alpha, const unordered_set<char>& alphabet) {
    if (ko < 0) {
        cerr << "Depth must be non-negative" << endl;
        exit(1);
    }
    size_t alphabet_size = alphabet.size();
    const double const_term = alpha * alphabet_size;

    unordered_map<string_view, int> table;
    table.reserve(1000);
    size_t text_length = text.size();
    size_t context_length = ko + 1;

    if (text_length >= context_length) {
        size_t max_i = text_length - context_length;
        for (size_t i = 0; i <= max_i; ++i) {
            string_view context(&text[i], context_length);
            table[context]++;
        }
    }

    unordered_map<string_view, double> sums;
    sums.reserve(1000);
    for (const auto& kv : table) {
        string_view context = kv.first;
        int count = kv.second;
        string_view parent = context.substr(0, ko);
        sums[parent] += count;
    }

    double sum_total = 0.0;
    for (const auto& kv : table) {
        string_view context = kv.first;
        int count = kv.second;
        string_view parent = context.substr(0, ko);
        auto sum_parent = sums[parent] + const_term;
        sum_total += count * log((count + alpha) / sum_parent);
    }

    size_t denominator = text_length - context_length;
    if (denominator == 0) {
        return 0.0;
    }
    double result = (-sum_total) / static_cast<double>(denominator) / log(2);
    return result;
}

int main(int argc, char* argv[]) {
    Args args = parse_args(argc, argv);
    string text = read_file(args.input);
    unordered_set<char> alphabet(text.begin(), text.end());

    auto start = chrono::high_resolution_clock::now();
    double probability = estimate_prob(text, args.depth, args.alpha, alphabet);
    auto end = chrono::high_resolution_clock::now();
    chrono::duration<double> duration = end - start;

    cout << probability << endl;
    if (args.timer)
        cout << "time: " << duration.count() << endl;

    return 0;
}