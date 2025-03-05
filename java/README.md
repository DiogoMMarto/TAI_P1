#### Note: Run the program using Java 17 or above.

## Build JAR

`mvn package` \

## Program arguments

Runs the FCM or Generator algorithm with the given parameters.
<type>              Type of application (fcm or generator)
-a, --alpha=<alpha>     Alpha value \
-c, --char Output char by char \
-f, --file=<fileName>   File name \
-h, --help Show this help message and exit. \
-k, --context=<k>       Context width \
-m, --mode=<mode>       Mode of operation (e.g., PROBABILITY, MAX, PROBABILITYALPHA) \
-p, --prior=<prior>     Prior string for generator \
-pf, --priorFix Enable prior fixing \
-rl, --responseLength=<responseLength> Length of the response for generator \
-s, --seeding=<seed>    Seed value for random number generation \
-sm, --searchMode=<searchMode> Search mode (e.g., CUTFIRSTCHAR, RANDOM) \
-v, --verbose Verbose output \
-V, --version Print version information and exit. \

## Example arguments

- External file:
  `java -jar target/tai-1.0-SNAPSHOT.jar fcm -a 1 -k 1 -f /Users/ilker/Courses/TAI/TAI_P1/sequence1.txt`
- If the file is under resources:
  `java -jar target/tai-1.0-SNAPSHOT.jar fcm -a 1 -k 1 -f sequence1.txt`

- Enable verbose output:
  `java -jar target/tai-1.0-SNAPSHOT.jar -a 1 -k 1 -f sequence1.txt -v true`