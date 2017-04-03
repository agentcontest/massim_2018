# MASSim Javaagents Documentation

This module provides some very basic agent framework written in Java. EISMASSim
is integrated so that agents can communicate with the MASSim server out of the box.

Some very basic agents are included mainly for testing purposes.

## Create your own agent
* Add a new class for your agent somewhere in _massim.javaagents.agents_
 * Make your class extend _massim.javaagents.agents.Agent_
 * Add your class with a type name to the _setEnvironment()_ method of _massim.javaagents.Scheduler_
 * Create a JSON configuration file for your agents

### Java agents configuration file
A sample configuration might look like this
```json
{
  "agents" : {
    "agentA1" : {"entity" : "connectionA1", "team" : "A", "class" : "BasicAgent"},
    "agentA2" : {"entity" : "connectionA2", "team" : "A", "class" : "BasicAgent"},
    "agentA3" : {"entity" : "connectionA3", "team" : "A", "class" : "BasicAgent"},
    "agentA4" : {"entity" : "connectionA4", "team" : "A", "class" : "BasicAgent"},
    "agentA5" : {"entity" : "connectionA5", "team" : "A", "class" : "BasicAgent"},
    "agentA6" : {"entity" : "connectionA6", "team" : "A", "class" : "BasicAgent"}
  }
}
```
The attributes of each agent are
* [key]: the agent's name
* entity: the EIS entity name as configured in eismassimconfig.json
* team: the agent's team name
* class: the agent's type as registered in the scheduler class
