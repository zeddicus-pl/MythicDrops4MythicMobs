# MythicDrops4MythicMobs
Plugin that controls MythicDrops drop on MythicMobs entities

Depends on:
- MythicDrops
- MythicMobs
- WorldGuard

This plugin receives MythicMob's "death" event, uses its configuration to determine what tiers of items (and gems, identification books, socket extenders, custom items) can drop from that mob.
It also takes into account WorldGuard regions - you can set a number (positive or negative) which will influence the drop based on the region player is in.

This is my first MC plugin, so please forgive me the code quality :)

## Config files
Plugin will create the config.yml file, and `drops` folder in `plugins/MythicDropsForMythicMobs`.

### Main config file

Example  `config.yml` file:
```yml
#
# MythicDrops for MythicMobs plugin
#

regions:
  "hard Region": 2
  "super Hard Region": 5

unidentifiedTiers:
  - rare
  - unique
  - legendary
```

- `regions` - map of name -> modifier value. The effect of modifier is described below, in drop calculation section.
- `unidentifiedTiers` - list of names of tiers which should always drop unidentified items. Empty list means no unidentified items being dropped. This applies only to items, not to "special" tiers (read next section).

### Drops folder

This folder can contain any number of `.yml` files, of any name, containing drop configuration for MythicMobs monsters.

Structure of each file:
```yml
# The identifier of the mob, as they are stated in MythicMobs configuration
name_of_mob:
  # Optional base template of the mob. Read "Templating" section for more information.
  template
  # MythicDrop tiers that can drop from this mob.
  # The number on the right side is the chance, using the "weight" method (the higher the number the bigger the chance)
  # Read more on the randomization algorihtm in next section.
  # You can use all tier names configured in your MythicDrops config, plus these "special" tiers:
  # - empty - drops no item
  # - gems - drops random gem, chances based on MythicDrops' `socketGems.yml`
  # - identityTome - drops identity tome
  # - socketExtender - drops random socket extender, chances based on MythicDrops' `socket-extender-types` property in `socketTypes.yml`
  # - customItems - drops random custom item, chances based on MythicDrops' `customItems.yml`
  tiers:
    common: 100
    uncommon: 40
    rare: 20
    gems: 10
    legendary: 5
    customItems: 1
  # Number of times the drop is calculated and dropped upon mob death.
  # You can also use format of x-y (like: 1-3) and then the number of droped items will vary between 1 to 3 items
  passes: 5
  # Chance to drop one more additional item. The number is a percentage chance.
  # You can use names of MythicDrops' custom items, or gems.
  extraDrops:
    socketsword: 0.5
    "Torment I": 0.1
```

## Drop calculation

The algorith is as follows:
- take all possible tiers the mob can drop
- sort them from biggest weight to lowest weight
- find a random number between `0 + region_drop_modifier` and sum of all weights
- iterate through the sorted tiers and see on which tier that number lands

**Example:**
For tiers:
- common: 30
- uncommon: 20
- rare: 5

The sorted tiers will look something like this:
```
|------------------|------------|----|
|      common      | uncommon   |rare|
|------------------|------------|----|
0     10    20     30    40    50   55
```

If the region modifier is missing (or zero), the random number will be selected between 0 and 55.
Let's say it ended up being 35 - this results in "uncommon" tier drop.

Now, assume the region modifier is `20`. The random number will be selected from values between 20 and 55, effectively making the "common" drop much more unlikely, and the other tiers more likely.

Alternatively, if the region modifier is -20, that will cause the random number to be selected between values -20 and 55. That will virtually add an "empty" drop tier between -20 and 0, so the player has some change not to drop anything, and other tiers are now also less likely to be selected.

Setting a high number of region modifier can make some tiers to be skipped totally. For example if we set region modifier to 45 in the above scenarion, the only tiers left will be "uncommon" and "rare", both with weights 5 (so 50% percent of getting one of them).

## Templating

The `template` property of drop configuration support both inheritance and composition. Here's an example:
```
BASE_ZOMBIE:
  tiers:
    common: 50
    uncommon: 10
    identityBook: 2
  passes: 1

BASE_BETTER_ZOMBIE:
  template: BASE_ZOMBIE
  tiers:
    uncommon: 30
    rare: 10
  passes: 2

EXTRA_GEMS_DROP:
  tiers:
    gems: 10

MY_BETTER_ZOMBIE:
  template: BASE_WEIRD_ZOMBIE, EXTRA_GEMS_DROP
  extraDrops:
    "super sword": 0.05
```

In this case, the "MY BETTER ZOMBIE" mob will end up with this configuration:
```
MY_BETTER_ZOMBIE:
  tiers:
    common: 50
    uncommon: 30
    rare: 10
    identityBook: 2
    gems: 10
  passes: 2
  extraDrops:
    "super sword": 0.05
```

**Rules:**
- If multiple templates are given in one row (separated by `,`), they are parsed from right to left. That means that the one on the left always overwrites values from the one on the right
- When lists are merged, keys that already exist are overwritten. If given key doesn't exist, it is added to the list. This applies to `tiers` and `extraDrops` properties.
- Single values are overwritten when merged. This applies to `passes`
