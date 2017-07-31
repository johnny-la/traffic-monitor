# Traffic Monitor

Monitors traffic from an Apache HTTP log

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
- Add unit tests for all the methods and classes that perform key monitoring logic
- Create a test that uses live log files to test the traffic monitoring. Currently, the unit tests monitor throughput of fake requests, without using log files. 
- Implement a Linter to detect potential errors in the codebase, and identify code that is breaking code conventions.
