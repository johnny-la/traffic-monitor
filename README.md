# Traffic Monitor

Monitors traffic from HTTP logs written in NCSA Common Log Format

## Setup
```
$ git clone <project-url>
$ cd <project-dir>
$ mvn clean install
### Note: Use JAR with dependencies ###
$ java -jar target/traffic-monitor-0.0.1-SNAPSHOT-jar-with-dependencies.jar path/to/log/file
```

## Improvements
To improve the application's design, I would do the following:
- Add unit tests for all the methods and classes that perform key monitoring logic.
  - Notably, create a unit test for reading live log files (in order to test live traffic monitoring).
- Modify LogParser.java to be more generic, potentially making it an abstract class. Each subclass of LogParser could parse logs from a different source, including text files, S3 files or even database queries.
- Implement a Linter to detect potential errors in the codebase and identify code that is breaking conventions.
