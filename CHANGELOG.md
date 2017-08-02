# MASSim CHANGELOG

## Package release 1.4

* protocol
  * added the agent's name to sim-start
  * fixed sim-end message not being deserialized
  * XML request-action messages now organized for better readability
* eismassim
  * added percept for agent's name
  * fixed abort action deemed not being supported
  * fixed a bug where an EISEntity could create multiple listening threads
  * added method for checking an entity's connection to a server to the environment interface
  * starting the environment now also starts its main thread, which will cause it to try to connect its entities with a massim server if they are not connected
* monitor
  * selection of multiple agents in the same location now cycles through all of them
  * zoom to new location on map change#
* scenario
  * fixed tools not being listed in storages after being stored
  * items of completed jobs are now mostly fed back into random shops
* server
  * will now only parse as many agents from team config as required and make up its own names if too few agents are configured

## Package release 1.3

* scenario:
  * blackouts added (charging stations may not work for a couple of steps)
  * assembly assistants are now sorted by their number (i.e. first by length of their name, then lexicographically) when determining the order in which to remove their items
  * fixed missions' rewards being higher than lowestBid
  * adapted configuration for 28 agents
  * fixed a severe bug when abort action was used
* monitor: limit number of displayed jobs and fix displayed items in shops
* protocol: added min/max lat and lon to sim-start percept
* eismassim: removed obsolete missionId parameter from mission percepts

## Package release 1.2

* changed xml format of tools in roles and item-requirements
* added tools to sim-start message
* fixed a possible memory leak that could occur if some agents would not connect
* added a special testing mode for simple (non-assembly) jobs
  * set difficultyMin/Max and missionDifficultyMax to 0
* changed "item" percept to "hasItem" for carried items (eismassim)
* added missing facility percept (eismassim)

## Package release 1.1

* improved job generation (replaced temporary one)
* fixed a bug where assembled items could (or would) have volume 0
* fixed a bug where missions were not added to the percept
