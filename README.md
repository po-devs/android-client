android-client
==============

Client for android for pokemon online

Building
========

The recommended way of building the app is by using IntelliJ. Set up IntelliJ
with the android SDK, and once it works, do the following steps:
- Clone as described in the section above
- Do import project -> select the directory
- When adding the jars, only select the one in libs/
- Use default options for the rest.

There are other ways of building the app:

For example issue commands after cloning the repo:

```sh
cd android-client
android update project --path . --name Pokemon-Online-Android --target 17
ant clean build
```
