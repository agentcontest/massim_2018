MASSim
======

_MASSim_ (Multi-Agent Systems Simulation Platform), the simulation (server)
software used in the
[Multi-Agent Programming Contest](https://multiagentcontest.org/),
where participants program agents to compete with each other in a
predefined game.

_MASSim_ simulations run in discrete steps. Agents connect remotely to the
contest server, receive percepts and send their actions, which are in turn
executed by _MASSim_.

Building MASSim
---------------

The build requires Maven.

Run `mvn package` in the main directory. Maven should automatically
fetch all nescessary dependencies.

Documentation
-------------

[docs/server.md](server.md) describes how the _MASSim_ server can be configured and started.

[docs/scenario.md](scenario.md) contains the description of the current scenario.

[docs/protocol.md](protocol.md) describes the _MASSim_ protocol, i.e. XML message formats for communicating with the _MASSim_ server.

[docs/eismassim.md](eismassim.md) explains _EISMASSim_, a Java library using the Environment Interface Standard (EIS) to communicate with the _MASSim_ server, that can be used with platforms which support the EIS.

[docs/javaagents.md](javaagents.md) gives a short introduction to the java agents framework, which holds skeletons that can already communicate with the MASSim server and have basic agent capabilities.

License
-------

_MASSim_ is licensed under the AGPLv3+. See COPYING.txt for the full
license text.
