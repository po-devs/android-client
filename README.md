android-client
==============

Client for android for pokemon online

Cloning
========

This contains a submodule, so don't forget to do the following commands after the initial clone:
```sh
git submodule init
git submodule update
```

Building
========

The recommended way of building the app is by using IntelliJ. Set up IntelliJ
with the android SDK, and once it works, do the following steps:
- Clone as described in the section above
- Do import project -> select the directory
- Add all the sources folder except viewpagerindicator/samples/src
- When adding the jars, only select the one in libs/ (not the ones in viewpagerindicator/.../lib)
- Use default options for the rest.

There are other ways of building the app:

For example issue commands after cloning the repo:

```sh
cd android-client
android update project --path . --name Pokemon-Online-Android --target 17
ant clean build
```

Or if you are on eclipse, you need to import the viewpagerindicator/library as android code in your workspace. 
**Name it viewpagerindicator.** 

[1]: https://github.com/coyotte508/Adroid-ViewPagerIndicator
