# MASSim CHANGELOG

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
