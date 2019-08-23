# Cx3D (Mavenized project)

This repository hosts a [mavenized](https://maven.apache.org) version of the Cx3D program.

Cx3D (Cortex simulation in 3D) is a tool for simulating the growth of cortex in 3D developed at the [Institute of Neuroinformatics](http://www.ini.uzh.ch/) of the [University of Zürich](http://www.uzh.ch/) and [ETH Zürich](http://www.ethz.ch/). It is distributed under the [GNU General Public License version 3](http://www.gnu.org/licenses/gpl.html).

For more information refer to the official [Cx3D website](https://www.ini.uzh.ch/~amw/seco/cx3d/).


## Streamlined Setup
This repository provides a really quick project setup to run Cx3D.

1. To compile (assuming you have installed git and maven):

  ```
  git clone https://github.com/tferr/cx3d-mvn
  cd cx3d-mvn
  mvn compile
  ```

2. To run from Eclipse:

 1. If you have installed the m2e-egit plugin:

      Run *File* → *Import* → *Check out Maven Projects from SCM*  and paste `https://github.com/tferr/cx3d-mvn.git` in the *SCM URL* field

 2. Without m2e-egit, clone the repository, then:

      Run *File* → *Import* → *Existing Maven Projects*, choosing the path to the cloned directory
 
3. To run without Eclipse:

  After compiling in step 1:
  
  ```
  mvn package
  cd target
  ```
  
  Then:
  
    1. To run an example from [the Tutorial](misc/Cx3DTutorial.pdf):
  
      ```  
      jar ufe cx3d-mvn-0.0.3.jar DividingCell
      java -jar cx3d-mvn-0.0.3.jar 
      ```
  
    2. Or, to reproduce a figure from [the Frontiers paper](http://journal.frontiersin.org/article/10.3389/neuro.10.025.2009/full):
   
      ```  
      jar ufe cx3d-mvn-0.0.3.jar Figure_3_G
      java -jar cx3d-mvn-0.0.3.jar
      ```
    3. Once the the simulation window opens, uncheck "Pause" to begin simulation.
  
  Note: For other examples/figures, replace "DividingCell/Figure_3_G" above with others from the [tutorial](src/main/java/sc/iview/cx3d/simulations/tutorial) or [frontiers](src/main/java/sc/iview/cx3d/simulations/frontiers) folders.

## Version
Cx3D public release 0.03


# Development
Refer to the Cx3D public [Subversion repository](https://svn.ini.uzh.ch/pub/cx3dp-core/.) (read-only)


## Resources
 * [Official Cx3D website](https://www.ini.uzh.ch/~amw/seco/cx3d/) (for convenience, some of the Cx3D tutorials are also mirrored in the [misc directory](./misc))
 * [Parallelized version of Cx3D](https://github.com/tferr/cx3dp-mvn)
 * [C++ port of Cx3D](https://github.com/breitwieser/cx3d-cpp)
