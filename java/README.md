#### Note: Run the program using Java 17 or above.

## Build JAR

`mvn package`

## Program arguments

Usage: Main [-hvV] [-nc] [-pf] [-a=<alpha>] -f=<fileName> [-k=<k>] [-m=<mode>]
[-p=<prior>] [-rl=<responseLength>] [-s=<seed>] [-sm=<searchMode>]
<type>
Runs the FCM or Generator algorithm with the given parameters.
<type>                   Type of application (fcm or generator) \
-a, --alpha=<alpha>      Smoothing parameter alpha \
-f, --file=<fileName>    File name \
-h, --help Show this help message and exit. \
-k, --contextWidth=<k>   Context width \
-m, --mode=<mode>        Mode of operation (e.g., PROBABILITY, MAX, PROBABILITYALPHA) \
-nc, --noChar Disable output char by char \
-p, --prior=<prior>      Prior string for generator \
-pf, --priorFix Enable prior fixing \
-rl, --responseLength=<responseLength> Length of the response for generator \
-s, --seeding=<seed>     Seed value for random number generation \
-sm, --searchMode=<searchMode> Search mode (e.g., CUTFIRSTCHAR, RANDOM) \
-v, --verbose Verbose output \
-V, --version Print version information and exit. \

## Example arguments

### FMC examples

- External file:
  `java -jar target/tai-1.0-SNAPSHOT.jar fcm -a 1 -k 1 -f /Users/ilker/Courses/TAI/TAI_P1/sequence1.txt`

- Enable verbose output:
  `java -jar target/tai-1.0-SNAPSHOT.jar -a 1 -k 1 -f sequence1.txt -v`

### Generator examples

- Basic call (file under resources):
  `java -jar target/tai-1.0-SNAPSHOT.jar g -f sequence1.txt -rl 500 -p "ATGAA"`

- Recommended call:
  `java -jar target/tai-1.0-SNAPSHOT.jar g -f sequence1.txt -rl 500 -p "ATGAATGAAT" -pf -k 5 -s 85095`

- Extensive calls:
  `java -jar target/tai-1.0-SNAPSHOT.jar g -f sequence1.txt -rl 500 -p "ATGAATGAAT" -pf -k 5 -s 85095 -m PROBABILITYALPHA -a 0.01 -nc`
  `java -jar target/tai-1.0-SNAPSHOT.jar g -f sequence1.txt -rl 500 -p "ATGAATGAAT" -pf -k 5 -s 85095 -m MAX -sm RANDOM -nc`
