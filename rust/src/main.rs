use clap::Parser;
use fxhash::FxHashMap as HashMap;
use std::collections::HashSet;
use std::fs;
use std::hash::{Hash, Hasher};
use std::time::Instant;

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    input: String,

    #[arg(short = 'k', long = "depth", default_value_t = 2)]
    depth: usize,

    #[arg(short = 'a', long = "alpha", default_value_t = 1.0)]
    alpha: f64,

    #[arg(short = 't', long = "timer")]
    timer: bool,
}

/// A context with a precomputed rolling hash and a slice of bytes.
/// We use the precomputed hash in the `Hash` implementation.
struct RollingContext<'a> {
    hash: u64,
    data: &'a [u8],
}

impl<'a> Hash for RollingContext<'a> {
    fn hash<H: Hasher>(&self, state: &mut H) {
        state.write_u64(self.hash);
    }
}

impl<'a> PartialEq for RollingContext<'a> {
    fn eq(&self, other: &Self) -> bool {
        self.data == other.data
    }
}

impl<'a> Eq for RollingContext<'a> {}

fn main() {
    let args = Args::parse();

    let text_bytes = fs::read(&args.input).expect("Failed to read file");
    let ko = args.depth;

    if text_bytes.len() <= ko {
        eprintln!("Text length must be greater than depth k");
        std::process::exit(1);
    }

    let alphabet: Vec<u8> = text_bytes
        .iter()
        .cloned()
        .collect::<HashSet<_>>()
        .into_iter()
        .collect();

    let alpha_size = alphabet.len() as f64;
    let const_term = args.alpha * alpha_size;

    let start_time = Instant::now();
    let mut sum = 0.0;
    let mut table: HashMap<RollingContext, (HashMap<u8, u32>, u32)> = HashMap::default();

    let base: u64 = 257;
    let pow = base.pow((ko - 1) as u32);

    let mut rolling_hash: u64 = 0;
    for j in 0..ko {
        rolling_hash = rolling_hash
            .wrapping_mul(base)
            .wrapping_add(text_bytes[j] as u64);
    }

    for i in 0..(text_bytes.len() - ko) {
        let context_slice = &text_bytes[i..i + ko];
        let context = RollingContext {
            hash: rolling_hash,
            data: context_slice,
        };
        let next_char = text_bytes[i + ko];

        let (context_table, total) = table
            .entry(context)
            .or_insert_with(|| (HashMap::default(), 0));

        let count = context_table.entry(next_char).or_insert(0);
        let numerator = *count as f64 + args.alpha;
        let denominator = *total as f64 + const_term;
        sum -= (numerator / denominator).ln();

        *count += 1;
        *total += 1;

        if i < text_bytes.len() - ko - 1 {
            // Update rolling hash
            rolling_hash = rolling_hash
                .wrapping_sub((text_bytes[i] as u64).wrapping_mul(pow));
            rolling_hash = rolling_hash.wrapping_mul(base);
            rolling_hash = rolling_hash.wrapping_add(text_bytes[i + ko] as u64);
        }
    }

    let probability = sum / (text_bytes.len() - ko) as f64 / 2.0f64.ln();
    println!("Probability: {}", probability);

    if args.timer {
        println!("Elapsed time: {:?}", start_time.elapsed());
    }
}