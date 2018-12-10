# Return To MapFrame

**Return To MapFrame** is an Android application for **Pepper the robot**.
It was developed using the QiSDK (https://qisdk.softbankrobotics.com).

This sample shows how to use **LocalizeAndMap** and **Localize**
actions to:

* Compute a map of an environment.
* Use a map to make Pepper localize himself in the corresponding
environment.
* Make Pepper return to his original position, defined as the position
  where he started mapping his environment (map frame).

## Minimum configuration

* Pepper 1.9.
* API level 3.
* A real robot (does not work on a virtual robot).

## Application flow

### Introduction

Pepper will explain the purpose of this sample.

### Menu

Select if you want to map Pepper's environment or use a saved map.

### Create a new map

Follow instructions so that Pepper can map his surroundings and save
the resulting map.

### Use saved map

Select if you want Pepper to localize himself or if you want him to go
to his initial position.

### Localization

Follow instructions so that Pepper can localize himself in his
environment.

### Go to initial position

Follow instructions so that Pepper can go to his original position
(map frame).

## Licence

See the [COPYING](COPYING.md) file for the licence.
