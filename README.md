# Cx3D (Mavenized project)

This repository hosts a [mavenized](https://maven.apache.org) version of the Cx3D program.

Cx3D (Cortex simulation in 3D) is a tool for simulating the growth of cortex in 3D developed at the [Institute of Neuroinformatics](http://www.ini.uzh.ch/) of the [University of Zürich](http://www.uzh.ch/) and [ETH Zürich](http://www.ethz.ch/). It is distributed under the [GNU General Public License version 3](http://www.gnu.org/licenses/gpl.html).

For more information refer to the official [Cx3D website](https://www.ini.uzh.ch/~amw/seco/cx3d/).

# Streamlined Setup
This repository provides a really quick project setup to run Cx3D.

 1. To compile (assuming you have installed git and maven):

  ```
  git clone https://github.com/tferr/cx3d-mvn
  mvn compile

  ```

2. To run from Eclipse:

 1. If you have installed the m2e-egit plugin:

      Run *File* → *Import* → *Check out Maven Projects from SCM*  and paste `https://github.com/tferr/cx3d-mvn.git` in the *SCM URL* field

 2. Without m2e-egit, clone the repository, then:

      Run *File* → *Import* → *Existing Maven Projects*, choosing the path to the cloned directory


# Version

 * Cx3D public release 0.03


# Development
Refer to the Cx3D public [Subversion repository](https://svn.ini.uzh.ch/pub/cx3dp-core/.) (read-only)


# Resources
Always refer to the official [Cx3D website](https://www.ini.uzh.ch/~amw/seco/cx3d/).
For convenience, some of the Cx3D tutorials are also in the [misc directory](./misc).
