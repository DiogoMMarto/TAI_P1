use clap::Parser;
use fxhash::FxHashMap as HashMap;
use std::fs;
use std::time::Instant;

fn estimate_prob(text: &[u8], depth: usize, alphas: &[f64], alphabet_len: usize) -> Vec<f64> {
    let ko = depth;
    let mut table: HashMap<&[u8], i32> = HashMap::default();
    // Build frequency table of all (depth+1)-length contexts
    for i in 0..text.len().saturating_sub(ko) {
        *table.entry(&text[i..i + ko + 1]).or_insert(0) += 1;
    }
    
    let mut sums: HashMap<&[u8], i32> = HashMap::default();
    for (context, &count) in &table {
        *sums.entry(&context[..ko]).or_insert(0) += count;
    }

    // Calculate entropy components for each alpha
    let mut probs = Vec::with_capacity(alphas.len());
    for &alpha in alphas {
        let mut sum = 0.0;
        let const_val = alpha * alphabet_len as f64;
        for (context, &count) in &table {
            let s = *sums.get(&context[..ko]).unwrap() as f64;
            let c = count as f64;
            sum += c * ((c + alpha) / (s + const_val)).ln();
        }

        let n = (text.len().saturating_sub(ko)) as f64;
        probs.push((-sum) / (n * 2.0f64.ln()));
    }

    probs
}

#[derive(Parser, Debug)]
#[command(version, about)]
struct Args {
    /// Input text file
    input: String,

    /// Context depth
    #[arg(short, long, default_value_t = 2)]
    depth: usize,

    /// Smoothing factor (can be specified multiple times)
    #[arg(short, long, value_parser = clap::value_parser!(f64))]
    alpha: Vec<f64>,
}

fn main() {
    let mut args = Args::parse();

    let text = fs::read(&args.input).expect("Failed to read input file");
    let alphabet_len = text.iter().collect::<std::collections::HashSet<_>>().len();
    if args.alpha.is_empty() {
        args.alpha.push(1.0);
    }

    let start = Instant::now();
    let probabilities = estimate_prob(&text, args.depth, &args.alpha, alphabet_len);
    let duration = start.elapsed();

    println!("Execution time: {:?}", duration);
    for (i, &alpha) in args.alpha.iter().enumerate() {
        println!("Alpha: {:<10}, res: {:.4} bits", alpha, probabilities[i]);
    }
}