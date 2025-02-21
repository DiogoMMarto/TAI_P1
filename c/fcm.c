#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
#include <time.h>

typedef struct {
    char* input;
    int depth;
    double alpha;
    char timer;
} Args;

Args default_args() {
    Args args;
    args.depth = 2;
    args.alpha = 1.0;
    args.timer = 0;
    return args;
}

typedef struct {
    char* key;
    uint32_t value;
    uint32_t hash;
} HashEntry;

typedef struct {
    HashEntry* entries;
    uint32_t context_length;
    uint32_t capacity;
    uint32_t size;
} HashTable;

#if 1
#define mulH 31     // 32-bit FNV offset basis
#define PH   2147483647     // 2^32-1
#define SEED 0
uint32_t hash(const char *data, uint32_t length) {
    uint64_t hash = 0;
    
    for (uint32_t i = 0; i < length; ++i) {
        hash = ((hash * mulH) + data[i]) % PH;
    }
    return hash;
}
#endif

#define INITIAL_CAPACITY 2048
#define LOAD_FACTOR 0.6
HashTable* hashtable_create(uint32_t context_length) {
    HashTable* table = malloc(sizeof(HashTable));
    table->context_length = context_length;
    table->capacity = INITIAL_CAPACITY;
    table->size = 0;
    table->entries = calloc(INITIAL_CAPACITY, sizeof(HashEntry));
    return table;
}

#if 0 
int strcmpDepth(const char* a, const char* b, uint32_t depth) {
    uint32_t i = 1;
    uint64_t* a64 = (uint64_t*)a;
    uint64_t* b64 = (uint64_t*)b;
    for(;*a64 && (*a64 == *b64) && i < depth; i+=sizeof(uint64_t), a64++, b64++);
    uint64_t mask = -1ull << (sizeof(uint64_t)*((8 - depth + i)));
    return (*a64 & mask ) - (*b64 & mask);
}
#else
int strcmpDepth(const char* a, const char* b, uint32_t depth) {
    uint32_t i = 1;
    while(*a && (*a == *b) && i < depth) {
        a++;
        b++;
        i++;
    }
    return *a - *b;
}
#endif

uint32_t probe(HashTable* table,  uint32_t h , char* key) {
    uint64_t index = h % table->capacity;
    uint32_t i = 1;
    while (table->entries[index].key && strcmpDepth(table->entries[index].key, key,table->context_length) != 0) {
        index = (index + i) % table->capacity;
        i++;
    }   
    return index; 
}

void hashtable_resize(HashTable* table) {
    uint32_t new_capacity = table->capacity * 2;
    HashEntry* new_entries = calloc(new_capacity, sizeof(HashEntry));
    for (uint32_t i = 0; i < table->capacity; i++) {
        HashEntry* entry = &table->entries[i];
        if (entry->key != NULL) {
            uint64_t index = entry->hash % new_capacity;
            uint32_t j = 1;
            while (new_entries[index].key && strcmpDepth(new_entries[index].key, entry->key,table->context_length) != 0) {
                index = (index + j) % new_capacity;
                j++;
            }
            new_entries[index]= table->entries[i];
        }
    }
    free(table->entries);
    table->entries = new_entries;
    table->capacity = new_capacity;
}

void hashtable_increment(HashTable* table, char* key) {
    if (table->size >= table->capacity * LOAD_FACTOR) {
        hashtable_resize(table);
    }
    uint32_t h = hash(key, table->context_length);
    uint64_t index = probe(table, h, key);
    HashEntry* entry = &table->entries[index];
    if (!entry->key) {
        entry->key = key;
        entry->value = 1;
        entry->hash = h;
        table->size++;
    } else {
        entry->value++;
    }
}

void hashtable_increment_by(HashTable* table, char* key, uint32_t value) {
    if (table->size >= table->capacity * LOAD_FACTOR) {
        hashtable_resize(table);
    }
    uint32_t h = hash(key, table->context_length);
    uint64_t index = probe(table, h, key);
    HashEntry* entry = &table->entries[index];
    if (!entry->key) {
        entry->key = key;
        entry->value = value;
        entry->hash = h;
        table->size++;
    } else {
        entry->value += value;
    }
}

int hashtable_get(HashTable* table, char* key) {
    uint32_t h = hash(key, table->context_length);
    uint64_t index = probe(table, h, key);
    if (!table->entries[index].key) {
        return 0;
    }
    return table->entries[index].value;
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
        if (strcmp(arg, "-k") || strcmp(arg, "--depth")) {
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
        if (text[i] == '\0') {
            break;
        }
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

double estimate_prob(char* text, uint32_t size, int ko, double alpha, uint32_t alphabet_size) {
    if (ko < 0) {
        printf("Depth must be non-negative\n");
        exit(1);
    }
    double const_term = alpha * (double) alphabet_size;
    uint32_t context_length = ko + 1;
    uint32_t max_i = size - context_length;

    HashTable* table = hashtable_create(context_length);
    for (uint32_t i = 0; i < max_i; i++) {
        char* context = text + i;
        hashtable_increment(table, context);
    }

    HashTable* sub_table = hashtable_create(context_length - 1);
    for(uint32_t i = 0; i < table->capacity; i++) {
        if (!table->entries[i].key) {
            continue;
        }
        char* context = table->entries[i].key;
        uint32_t value = table->entries[i].value;
        hashtable_increment_by(sub_table, context, value);
    }

    double sum_total = 0;
    for(uint32_t i = 0; i < table->capacity; i++) {
        if (!table->entries[i].key) {
            continue;
        }
        double count = table->entries[i].value;
        char* context = table->entries[i].key;
        double sum = hashtable_get(sub_table, context);
        sum_total += count * log((count + alpha) / (sum + const_term));
    }

    hashtable_free(table);
    hashtable_free(sub_table);
    return -sum_total / ( max_i * log(2) );
}

int main(int argc, char* argv[]) {
    Args args = parse_args(argc, argv);

    uint32_t size = 0;
    char* text = read_file(args.input, &size);
    uint32_t alphabet_siz = alphabet_size(text, size);

    clock_t t = clock();
    double prob = estimate_prob(text, size, args.depth, args.alpha, alphabet_siz);
    t = clock() - t;

    printf("%f\n", prob);
    if (args.timer)
        printf("time: %f\n", ((double) t) / CLOCKS_PER_SEC);

    free(text);
    return 0;
}