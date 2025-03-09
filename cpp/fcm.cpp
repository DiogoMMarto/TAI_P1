#include <iostream>
#include <fstream>
#include <unordered_map>
#include <array>
#include <string_view>
#include <string>
#include <set>
#include <chrono>
#include <cmath>
#include <cstdlib>

// Simple command-line parsing.
struct Args {
    std::string input;
    size_t depth = 2;
    double alpha = 1.0;
    bool timer = false;
};

Args parse_args(int argc, char* argv[]) {
    Args args;
    if (argc < 2) {
        std::cerr << "Usage: " << argv[0] << " <input_file> [-k <depth>] [-a <alpha>] [-t]\n";
        std::exit(1);
    }
    args.input = argv[1];
    for (int i = 2; i < argc; i++) {
        std::string arg = argv[i];
        if (arg == "-k" && i + 1 < argc) {
            args.depth = std::stoul(argv[++i]);
        } else if (arg == "-a" && i + 1 < argc) {
            args.alpha = std::stod(argv[++i]);
        } else if (arg == "-t") {
            args.timer = true;
        }
    }
    return args;
}

int main(int argc, char* argv[]) {
    Args args = parse_args(argc, argv);

    std::ifstream infile(args.input, std::ios::binary);
    if (!infile) {
        std::cerr << "Failed to open file: " << args.input << "\n";
        return 1;
    }
    std::string text((std::istreambuf_iterator<char>(infile)),
                     std::istreambuf_iterator<char>());
    infile.close();

    if (text.size() <= args.depth) {
        std::cerr << "Text length must be greater than context depth.\n";
        return 1;
    }

    std::set<char> alphabet_set(text.begin(), text.end());
    size_t alphabet_size = alphabet_set.size();

    double const_term = args.alpha * static_cast<double>(alphabet_size);

    auto start_time = std::chrono::high_resolution_clock::now();
    double sum = 0.0;

    // The outer table maps a context (as a string_view) to a pair:
    //   - the frequency table (std::array<uint32_t, 256>),
    //   - the total count observed for that context.
    using FrequencyTable = std::array<uint32_t, 256>;
    std::unordered_map<std::string_view, std::pair<FrequencyTable, uint32_t>> table;
    table.reserve(text.size() / args.depth); // heuristic reserve

    // Note: since `text` remains alive throughout, the string_views remain valid.
    const size_t num_contexts = text.size() - args.depth;
    for (size_t i = 0; i < num_contexts; i++) {
        std::string_view context(text.data() + i, args.depth);
        char next_char = text[i + args.depth];

        auto [iter, inserted] = table.try_emplace(context, FrequencyTable{}, 0);
        auto& freq_table = iter->second.first;
        auto& total = iter->second.second;

        uint32_t count = freq_table[static_cast<unsigned char>(next_char)];
        double numerator = static_cast<double>(count) + args.alpha;
        double denominator = static_cast<double>(total) + const_term;
        sum -= std::log(numerator / denominator);

        freq_table[static_cast<unsigned char>(next_char)] = count + 1;
        total++;
    }

    double probability = (sum) / (static_cast<double>(num_contexts) * std::log(2.0));
    std::cout << "Probability: " << probability << "\n";

    if (args.timer) {
        auto end_time = std::chrono::high_resolution_clock::now();
        auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
        std::cout << "Elapsed time: " << elapsed.count() << " ms\n";
    }

    return 0;
}
