# MASSim Documentation

TODO:
* introduction
* Commands (scenario)

### Building MASSim
The complete package can be built by running ```mvn package``` in the main directory. All necessary dependencies are available online and maven should
automatically fetch them.

### Structure

[server.md](server.md) describes how the server can be configured and started.

[scenario.md](scenario.md) contains the description of the current scenario.

[monitor.md](monitor.md) explains the MASSim web monitor.

[protocol.md](protocol.md) describes the MASSim protocol, i.e. XML message formats for communicating with the MASSim server.

[eismassim.md](eismassim.md) explains EISMASSim, a Java library using the Environment Interface Standard (EIS) to communicate with the MASSim server, that can be used with platforms which support the EIS.

[javaagents.md](javaagents.md) gives a short introduction to the java agents framework, which holds skeletons that can already communicate with the MASSim server and have basic agent capabilities.

[rules.md](rules.md) lists all rules and requirements for participating in the MAPC.
