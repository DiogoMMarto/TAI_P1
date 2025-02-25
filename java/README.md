Should be ran using Java 17 or above.
Arguments should be passed in order:
alpha(double) k(int) file_path(or name) verbose(boolean) 

- External file:
`java -jar target/tai-1.0-SNAPSHOT.jar 0.01 1 /Users/ilker/Courses/TAI/TAI_P1/sequence1.txt`
- If the file is under resources:
`java -jar target/tai-1.0-SNAPSHOT.jar 0.01 1 sequence1.txt`

- Enable verbose output:
`java -jar target/tai-1.0-SNAPSHOT.jar 0.01 1 sequence1.txt true`