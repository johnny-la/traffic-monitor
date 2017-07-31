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
- Add unit tests for all methods and classes that perform key monitoring logic
  - Notably, create a unit test for reading live log files
- Modify `LogParser.java` to be more generic, and potentially make it an abstract class. Each subclass of `LogParser` should parse logs from a different source, including text files, S3 files and database tables
- To make the monitoring system more scalable, the recording of metrics could be separated from the monitoring/analysis of metrics. In fact, `MetricManager` could be split into two classes: `MetricMonitor` and `MetricManager`. `MetricManager` would store relevant metrics, and `MetricMonitor` would analyze these metrics and create relevant alerts
- When an alert is triggered, `MetricPrinter` should print the alert instead of `MetricManager.addAlert()`. This change would migrate all printing functionality to the `MetricPrinter` class. *(Note: since this change potentially required a new Listener interface, I decided to omit this change since it is a relatively small feature)*
- Create an `AlertSchema` class that stores rules and templates for triggering alerts. As a result, different types of alerts could be added in a scalable and maintainable way. To monitor the metrics, the `MetricMonitor` would hold a list of `AlertSchemas`, which would then trigger a variable amount of alerts.  
- Implement a Linter to detect potential errors in the codebase and identify convention-breaking code
