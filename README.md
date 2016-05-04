# FFXIVRaffler

This is a program designed to assist in hosting raffles in FFXIV. It takes rolls from `/random` and finds which people
rolled a number closest to a predetermined target number.

## Current features
- Copy/paste log processing
- Target number
- Multiple winners (tie) are displayed for rerolls
- Self-rolls toggleable
- Cross-platform (hopefully?)

## Planned features
- Configurable regex for matches (forwards-compatibility if `/random` format changes)
- Ability to read from FFXIV log file(s) (using separators – configurable)

## Building

`mvn clean package` and finish. The resulting JAR should be found in the `target` folder.
