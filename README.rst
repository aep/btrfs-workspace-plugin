================================
Jenkins plugin: btrfs-workspace
================================

This is a note for compile the 'Jenkins plugin: btrfs-workspace'.

How to compile
===============

1. Install the maven package on Ubuntu.

::

  $ sudo aptitude install maven

2. Check maven version.

::

  $ mvn -version
  Apache Maven 3.0.5
  Maven home: /usr/share/maven
  Java version: 1.7.0_55, vendor: Oracle Corporation
  Java home: /usr/lib/jvm/java-7-openjdk-amd64/jre
  Default locale: en_US, platform encoding: UTF-8
  OS name: "linux", version: "3.13.0-34-generic", arch: "amd64", family: "unix

3. Compile plugin of **Clone workspace from BTRFS snapshot** with debug mode. [1]_

::

  $ mvn -X install
  [...]
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time: 11:04.390s
  [INFO] Finished at: Mon Aug 25 09:14:56 CST 2014
  [INFO] Final Memory: 37M/131M
  [INFO] ------------------------------------------------------------------------

4. Find **btrfs-workspace-plugin.hpi** and manual upload to Jenkins.

::

  $ find -name 'btrfs-workspace-plugin.hpi' -type f
  ./target/btrfs-workspace-plugin.hpi

Reference
==========

- `Plugin tutorial | Jenkins Wiki <https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial#Plugintutorial-Eclipse>`_

.. [1] You can try the ``mvn install``, but it\`s no work with me.
