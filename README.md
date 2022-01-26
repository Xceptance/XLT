# XLT
XLT is an extensive load and performance test tool developed and maintained by Xceptance. If you need more information, here is the current website https://www.xceptance.com/xlt/ and the current documentation https://xltdoc.xceptance.com/. There is also a forum available to discuss load testing and test automation with XLT: https://ask.xceptance.de/

The documentation was fully rewritten in 2020 and it is continuously updated. We appreciate your feedback. You can also directly contribute on GitHub https://github.com/Xceptance/xlt-documentation. If you need any information from the legacy documentation, you can still find it at https://lab.xceptance.de/releases/xlt/latest/index.html

# Open Source
XLT has been open sourced on 30 January 2020 under the Apache License 2.0. Stay tuned for more!

# How to Build XLT

The XLT build process is based on the **Apache Ant** build tool. The build works best when being run on a Linux machine.

## Required Tools

* Latest JDK 11
* Latest [Apache Ant](https://ant.apache.org/)
* Chrome or Chromium browser (to package the timer-recorder extension for Chrome/Chromium)
* Optional tools:
    * Only if you want to run the XLT unit tests:
        * [chromedriver](https://chromedriver.chromium.org/)
        * [geckodriver](https://github.com/mozilla/geckodriver)
    * Only if you want to build the XLT documentation:
        * [Jekyll](https://jekyllrb.com/)

## Building XLT

To create the XLT distribution archive `xlt-X.Y.Z.zip`, run the following command. If all went well, the archive can then be found in folder `target/dist`.

```
ant clean dist
```

If you want the HTML documentation (manual, release notes, how-to) to be included in the distribution archive, run this command:

```
ant clean dist -Dcreate.doc=true
```

To perform a release build which requires all unit tests to pass, run any of these commands:

```
ant clean release 
ant clean release -Dcreate.doc=true
```

## Limitations

When you build XLT by yourself, the following limitations apply.

* The timer-recorder extension for Firefox is not signed yet. Hence, when using `XltFirefoxDriver` later on, Firefox will refuse to install that extension. If you need the extension, download the official XLT distribution which contains a correctly signed extension.

## Building on Windows or macOS

* Some unit tests are known to fail on Windows.
* You will need to adjust the path to Chrome/Chromium in `build.properties` according to your system.
