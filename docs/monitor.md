MASSim Monitor Documentation
============================

The monitor allows you to view live matches and replays in your browser.

Live monitor
------------

[Start the server](server.md) with the `--monitor 8000` flag and navigate to
[http://localhost:8000/](http://localhost:8000/) in your browser.

Viewing a replay
----------------

Start the monitor and provide a path to a replay directory.

Usage:

```
java -jar monitor/monitor-[version]-with-dependencies.jar [--port PORT] <server/replays/path>
```

Then navigate to [http://localhost:8000/?/](http://localhost:8000/?/) (or similar)
in your browser.
