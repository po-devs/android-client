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

For example issue commands after cloning the repo:

```sh
cd android-client
android update project --path . --name Pokemon-Online-Android --target 17
ant clean build
```

Or if you are on eclipse, you need to import the viewpagerindicator/library as android code in your workspace.

[1]: https://github.com/coyotte508/Adroid-ViewPagerIndicator
