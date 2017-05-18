# MASSim CHANGELOG

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
