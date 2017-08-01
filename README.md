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
#### General Improvements:
- Add unit tests for all methods and classes that perform key monitoring logic
  - Notably, create a unit test for reading live log files
- Implement a Linter to detect potential errors in the codebase and identify convention-breaking code
#### Design Improvements:
- Modify `LogParser.java` to be more generic, and potentially make it an abstract class. Each subclass of `LogParser` should parse logs from a different source, including text files, S3 files and database tables
- When an alert is triggered, `MetricPrinter` should print the alert instead of `MetricManager.addAlert()`. This would allow all printing functionality to be migrated to the `MetricPrinter` class. *(Note: since this change potentially required a new Listener interface, I decided to omit this change since it only supports a relatively small feature)*
- Add a `MetricMonitor` interface that the `ThroughputMonitor` would implement. Subsequently, multiple monitoring classes would implement `MetricMonitor` to monitor different metrics, such as failure rates or success ratios. Additionally, `MetricManager` would hold a list of `MetricMonitors`, allowing it to monitor multiple types of metrics in a scalable way
- Create an `AlertRule` class that stores rules and templates for triggering alerts. For instance, an `AlertRule` could store a critical RPS threshold which, when surpassed, would trigger an alert. `MetricMonitor` would hold a list of `AlertRules`, which would be compared with current metrics to trigger appropriate alerts. As a result, different rules could be used in the same `MetricMonitor`, thus making monitors more unit-testable and extensible
- Make `Alert` an abstract class, and create multiple subclasses for different alert types. For instance, the current `Alert`'s fields and functions could be moved to a `ThroughputAlert` class, making it easy to support various alert types


## Sample Output
<img src="https://user-images.githubusercontent.com/10332234/28776657-6ae09d66-75ac-11e7-900c-ae3d920308c9.png" width="75%" height="75%">

