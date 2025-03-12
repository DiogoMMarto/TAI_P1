#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
#include <time.h>

#define mulH 31     
#define PH   4294967029     // large prime
#define SEED 8589934621

#define INITIAL_CAPACITY 2048
#define INITIAL_CAPACITY_ARRAY 8
#define LOAD_FACTOR 0.6
#define LOAD_FACTOR_2 0.8
#define GROWTH_FACTOR 2

typedef struct {
    char* input;
    char* output;
    int depth;
    double alpha;
    char timer;
} Args;

Args default_args() {
    Args args;
    args.depth = 2;
    args.alpha = 1.0;
    args.timer = 0;
    args.output = NULL;
    return args;
}

typedef struct {
    char c;
    uint32_t count;
} CharInt; // MOVE THIS TO 2 ARRAYS TO IMPROVE THE MEMORY LAYOUT

typedef struct {
    char* key;
    uint32_t hash;
    uint32_t size;
    uint32_t capacity;
    uint32_t total;
    CharInt* entries;
} HashEntry;

typedef struct {
    HashEntry* entries;
    uint32_t context_length;
    uint32_t capacity;
    uint32_t size;
} HashTable;

uint32_t hash(const char *data, uint32_t length) {
    uint64_t hash = 0;
    uint64_t* data64 = (uint64_t*)data;
    uint32_t i = 0;
    for (; i + sizeof(uint64_t) <= length; i+=sizeof(uint64_t)) {
        hash = ((hash * mulH) + *data64++) % PH;
    }
    for(; i < length; i++) { // this works well bcs at the end we still scramble a bit
        hash = ((hash * mulH) + data[i]) % PH;
    }
    return ((hash * mulH) + hash) % PH; // avoids collisions experimentally
}

HashTable* hashtable_create(uint32_t context_length) {
    HashTable* table = malloc(sizeof(HashTable));
    table->context_length = context_length;
    table->capacity = INITIAL_CAPACITY;
    table->size = 0;
    table->entries = calloc(INITIAL_CAPACITY, sizeof(HashEntry));
    return table;
}


// With better logic maybe we can do this 
#if 0
uint64_t strcmpDepth(const char* a, const char* b, uint32_t depth) {
    uint32_t i = 0;
    const uint64_t* a64 = (uint64_t*)a;
    const uint64_t* b64 = (uint64_t*)b;
    for(;i + sizeof(uint64_t) <= depth; i+=sizeof(uint64_t), a64++, b64++){
        if(*a64 != *b64) return 1;
    };
    int shift_amt = 8*(8 - depth % 8)*(i >= (depth-depth%8));
    if(shift_amt == 64) return 0;
    uint64_t mask = -1ull >> shift_amt;
    return (*a64 & mask ) - (*b64 & mask);
}
#else
uint64_t strcmpDepth(const char* a, const char* b, uint32_t depth) {
    uint32_t i = 0;
    const uint64_t* a64 = (uint64_t*)a;
    const uint64_t* b64 = (uint64_t*)b;
    for(;i + sizeof(uint64_t) <= depth; i+=sizeof(uint64_t), a64++, b64++){
        if(*a64 != *b64) return 1;
    };
    return 0;
}
#endif

uint32_t probe(HashTable* table,  uint32_t h , char* key) {
    uint64_t index = h % table->capacity;
    uint32_t i = 1;
    while (table->entries[index].key && table->entries[index].hash != h && strcmpDepth(table->entries[index].key, key,table->context_length) != 0) {
        index = (index + i) % table->capacity;
        i++;
    }   
    return index; 
}

uint32_t probe2(HashEntry* e , char c) {
    uint32_t h = ((SEED * mulH) + c) % PH;
    h = ((h * mulH) + h) % e->capacity;
    while (e->entries[h].c && e->entries[h].c != c) {
        h = (h + 1) % e->capacity;
    }
    return h;
}

void da_resize(HashEntry* e) {
    uint32_t new_capacity = e->capacity * GROWTH_FACTOR;
    CharInt* new_entries = calloc(new_capacity, sizeof(CharInt));
    for (uint32_t i = 0; i < e->capacity; i++) {
        CharInt* entry = &e->entries[i];
        if (entry->c != '\0') {
            uint32_t index = ((SEED * mulH) + entry->c) % PH;
            index = ((index * mulH) + index) % new_capacity;
            while (new_entries[index].c && new_entries[index].c != entry->c) {
                index = (index + 1) % new_capacity;
            }
            new_entries[index] = *entry;
        }
    }
    free(e->entries);
    e->entries = new_entries;
    e->capacity = new_capacity;
}

uint32_t da_insert(HashEntry* e , char c , uint32_t count) {
    if(e->size >= e->capacity * LOAD_FACTOR_2) {
        da_resize(e);
    };
    uint32_t index = probe2(e, c);
    if (e->entries[index].c == '\0') {
        e->entries[index].c = c;
        e->size++;
    };
    uint32_t old_count = e->entries[index].count;
    e->entries[index].count += count;
    return old_count;
}

uint32_t da_get(HashEntry* e , char c) {
    uint32_t index = probe2(e, c);
    return e->entries[index].count;
}

void hashtable_resize(HashTable* table) {
    uint32_t new_capacity = table->capacity * GROWTH_FACTOR;
    HashEntry* new_entries = calloc(new_capacity, sizeof(HashEntry));
    for (uint32_t i = 0; i < table->capacity; i++) {
        HashEntry* entry = &table->entries[i];
        if (entry->key != NULL) {
            uint64_t index = entry->hash % new_capacity;
            uint32_t j = 1;
            while (new_entries[index].key && new_entries[index].hash != entry->hash && strcmpDepth(new_entries[index].key, entry->key,table->context_length) != 0) {
                index = (index + j) % new_capacity;
                j++;
            }
            new_entries[index]= *entry;
        }
    }
    free(table->entries);
    table->entries = new_entries;
    table->capacity = new_capacity;
}

void hashtable_increment(HashTable* table, char* key, uint32_t* count,uint32_t* total) {
    if (table->size >= table->capacity * LOAD_FACTOR) {
        hashtable_resize(table);
    }
    uint32_t h = hash(key, table->context_length);
    uint64_t index = probe(table, h, key);
    HashEntry* entry = &table->entries[index];
    if (!entry->key) {
        table->size++;
        entry->key = key;
        entry->hash = h;
        entry->size = 0;
        entry->capacity = INITIAL_CAPACITY_ARRAY;
        entry->total = 0;
        entry->entries = calloc(INITIAL_CAPACITY_ARRAY,sizeof(CharInt));
    } 
    *total = entry->total++;
    *count = da_insert(entry, key[table->context_length], 1);
}

void hashtable_increment_by(HashTable* table, char* key, uint32_t value, uint32_t* count,uint32_t* total) {
    if (table->size >= table->capacity * LOAD_FACTOR) {
        hashtable_resize(table);
    }
    uint32_t h = hash(key, table->context_length);
    uint64_t index = probe(table, h, key);
    HashEntry* entry = &table->entries[index];
    if (!entry->key) {
        table->size++;
        entry->key = key;
        entry->hash = h;
        entry->size = 0;
        entry->capacity = INITIAL_CAPACITY_ARRAY;
        entry->total = 0;
        entry->entries = calloc(INITIAL_CAPACITY_ARRAY,sizeof(CharInt));
    }
    entry->total += value;
    *total = entry->total;
    *count = da_insert(entry, key[table->context_length], value);
}

int hashtable_get(HashTable* table, char* key) {
    uint32_t h = hash(key, table->context_length);
    uint64_t index = probe(table, h, key);
    if (!table->entries[index].key) {
        return 0;
    }
    return da_get(&table->entries[index], key[table->context_length]);
}

void hashtable_free(HashTable* table) {
    free(table->entries);
    free(table);
}

Args parse_args(int argc, char* argv[]) {
    Args args = default_args();
    if (argc < 2) {
        printf("Usage: %s input.txt [-k depth] [-a alpha]\n", argv[0]);
        exit(1);
    }
    args.input = argv[1];
    for (int i = 2; i < argc; ) {
        char* arg = argv[i];
        if (strcmp(arg, "-k") == 0 || strcmp(arg, "--depth") == 0 ) {
            if (i + 1 >= argc) {
                printf("Missing value for %s\n", arg);
                exit(1);
            }
            args.depth = atoi(argv[i + 1]);
            i += 2;
        } else if (strcmp(arg, "-a") == 0 || strcmp(arg, "--alpha") == 0) {
            if (i + 1 >= argc) {
                printf("Missing value for %s\n", arg);
                exit(1);
            }
            args.alpha = strtod(argv[i + 1], NULL);
            i += 2;
        } else if (strcmp(arg, "-t") == 0 || strcmp(arg, "--timer") == 0) {
            args.timer = 1;
            i += 1;
        } else if (strcmp(arg, "-h") == 0 || strcmp(arg, "--help") == 0) {
            printf("Usage: %s input.txt [-k depth] [-a alpha]\n", argv[0]);
            exit(0);
        } else if (strcmp(arg, "-o") == 0 || strcmp(arg, "--output") == 0) {
            if (i + 1 >= argc) {
                printf("Missing value for %s\n", arg);
                exit(1);
            }
            args.output = argv[i + 1];
            i += 2;
        } else {
            printf("Unknown option: %s\n", arg);
            exit(1);
        }
    }
    return args;
}

char* read_file(const char* file_path, uint32_t* size) {
    FILE* file = fopen(file_path, "rb");
    if(file == NULL) {
        printf("Could not open file: %s\n", file_path);
        exit(1);
    }

    fseek(file, 0, SEEK_END);
    uint32_t file_size = ftell(file);
    *size = file_size;
    fseek(file, 0, SEEK_SET);

    char* buffer = (char*)malloc(file_size + 1);
    if(buffer == NULL) {
        printf("Could not allocate memory for file: %s\n", file_path);
        exit(1);
    }

    fread(buffer, file_size, 1, file);
    buffer[file_size] = '\0';

    fclose(file);
    return buffer;
}

uint32_t alphabet_size(char* text, uint32_t size) {
    uint8_t arr[32] = {0};
    uint32_t siz = 0;
    for (uint32_t i = 0; i < size-1; i++) {

        uint8_t index = ( (uint8_t) text[i] ) >> 3;
        uint8_t mask = 1 << ((uint8_t) text[i] & 7);
        uint8_t val = arr[index] & mask;
        if (val == 0) {
            arr[index] |= mask;
            siz++;
        }
    }
    return siz;
}

double estimate_prob(char* text, uint32_t size, int ko, double alpha, uint32_t alphabet_size, char* output_path) {
    if (ko < 0) {
        printf("Depth must be non-negative\n");
        exit(1);
    }
    FILE* output_file;
    if (output_path != NULL) {
        output_file = fopen(output_path, "w");
        if (output_file == NULL) {
            printf("Could not open output file\n");
            exit(1);
        }
    }
    double const_term = alpha * alphabet_size;
    uint32_t context_length = ko;
    uint32_t max_i = size - context_length ;

    HashTable* table = hashtable_create(context_length);
    double sum_total = 0;
    for (uint32_t i = 0; i < max_i; i++) {
        uint32_t total = 0;
        uint32_t count = 0;
        char* context = text + i;
        hashtable_increment(table, context, &count, &total);
        double symbol_length = log(( count+alpha) / (total+const_term));
        sum_total += symbol_length;
        if (output_path != NULL) {
            fwrite(&symbol_length, sizeof(double), 1, output_file);
        }
    }

    hashtable_free(table);
    return -sum_total / ( max_i * log(2) );
}

int main(int argc, char* argv[]) {
    Args args = parse_args(argc, argv);

    uint32_t size = 0;
    clock_t t = clock();
    char* text = read_file(args.input, &size);
    uint32_t alphabet_siz = alphabet_size(text, size);
    double prob = estimate_prob(text, size, args.depth, args.alpha, alphabet_siz,args.output);
    t = clock() - t;

    printf("%f\n", prob);
    if (args.timer)
        printf("time: %f\n", ((double) t) / CLOCKS_PER_SEC);

    free(text);
    return 0;
}