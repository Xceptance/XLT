# XLT
XLT is an extensive load and performance test tool developed and maintained by Xceptance. If you need more information, here is the current [website](https://www.xceptance.com/xlt/) and the current [documentation portal](https://docs.xceptance.com/).

The documentation was fully rewritten in 2020 and it is continuously updated. We appreciate your feedback. You can also directly contribute on GitHub at https://github.com/Xceptance/xlt-documentation. If you need any information from the legacy documentation, you can still find it at https://lab.xceptance.de/releases/xlt/5.7.1/index.html

# Open Source
XLT has been open sourced on 30 January 2020 under the Apache License 2.0.

# How to Build XLT

The XLT build process is based on the **Apache Ant** build tool. The build works best when being run on a Linux machine.

## Required Tools

* Latest JDK 21
* Latest [Apache Ant](https://ant.apache.org/)
* Chrome or Chromium browser (to package the timer-recorder extension for Chrome/Chromium)
* Node.js in version 16.15.1 (to bundle the resultbrowser)
* Optional tools:
    * Only if you want to run the XLT unit tests:
        * [chromedriver](https://chromedriver.chromium.org/)
        * [geckodriver](https://github.com/mozilla/geckodriver)

## IDE Import

Follow your IDE's instructions for importing an existing Maven project.

Once the project import is complete, a module package conflict may be reported. In this case, go to your IDE's build path settings and remove the `jdk.jsobject` module from the module dependencies.

Example - Eclipse IDE
* Right-click on imported project and select `Build Path` > `Configure Build Path...`
* In right hand view select tab `Module Dependencies`
* In `All Modules` list lookup `jdk.sobject`
* Mark and remove it

## Build Steps

To create the XLT distribution archive `xlt-X.Y.Z.zip`, run the following command. If all went well, the archive can then be found in folder `target/dist`.

```
ant clean dist
```

To perform a release build which requires all unit tests to pass, run this command:

```
ant clean release 
```

## Limitations

When you build XLT by yourself, the following limitations apply.

* The timer-recorder extension for Firefox is not signed yet. Hence, when using `XltFirefoxDriver` later on, Firefox will refuse to install that extension. If you need the extension, download the official XLT distribution which contains a correctly signed extension.

## Building on Windows or macOS

* Some unit tests are known to fail on Windows.
* You will need to adjust the path to Chrome/Chromium in `build.properties` according to your system.

## Building on Linux

* Depending on the distribution you use, you might need to adjust the path to your Chrome/Chromium executable in `build.properties` on Linux as well.

# XLT as a Service - XTC
If you are looking for a hosted version of XLT with all the bells and whistles, look no further. Xceptance offers [XTC - The Xceptance Test Center](https://xceptance.com/xtc/), a fully hosted, multi-project, infinitely scalable version of XLT in the cloud. [Contact](https://www.xceptance.com/en/contact/) us for more details.

[![image](https://github.com/Xceptance/XLT/assets/1793856/7a8efb7b-caef-4aaf-9321-cdcaba673d8a)](https://xceptance.com/xtc/)
