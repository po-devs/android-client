android-client
==============

Client for android for pokemon online

Needs the [ViewPagerIndicator][1] library. If git submodule worked properly, you just have to import viewpagerindicator/library in eclipse as Android code. Otherwise, download first the zip for the viewpagerindicator source and extract it in a viewpagerindicator subfolder.

Building
========

For example issue commands after cloning the repo:

```sh
cd android-client
android update project --path . --name Pokemon-Online-Android --target 17
ant clean build
```

[1]: https://github.com/coyotte508/Adroid-ViewPagerIndicator
