# SciView - Cx3D 
This repository hosts the [SciView](http://sc.iview.sc) version of Cx3D, allowing Cx3D to grow neuronal processes with SciView’s data structures. Cx3D (Cortex simulation in 3D) is a tool for simulating the growth of cortex in 3D developed at the [Institute of Neuroinformatics](http://www.ini.uzh.ch/) of the [University of Zürich](http://www.uzh.ch/) and [ETH Zürich](http://www.ethz.ch/). It was released in [2009](https://www.ini.uzh.ch/~amw/seco/cx3d/) under the [GNU General Public License version 3](http://www.gnu.org/licenses/gpl.html).


## Quick Start

#### For The Impatient ...

  1. Clone repo
  2. Import into IntelliJ/Eclipse IDE
  3. Run [sc.iview.cx3d.commands.RandomBranchingDemo](https://github.com/morphonets/cx3d/blob/master/src/main/java/sc/iview/cx3d/commands/RandomBranchingDemo.java)

#### For The More Patient..

##### To run from an IDE, e.g. Eclipse:

 1. If you have installed the m2e-egit plugin:

      Run *File* → *Import* → *Check out Maven Projects from SCM*  and paste `https://github.com/morphonets/cx3d.git` in the *SCM URL* field

 2. Without m2e-egit, clone the repository, then:

      Run *File* → *Import* → *Existing Maven Projects*, choosing the path to the cloned directory

### To run from the CLI:

  1. Compile (assuming you have installed git and maven):

  ```bash
  git clone git@github.com:morphonets/cx3d.git
  cd cx3d-mvn
  mvn package
  ```
  2. cd to target directory:

  ```bash
  cd target
  ```

  3. To run the original Cx3D examples:
  
        1. To run an example from [the tutorial](misc/Cx3DTutorial.pdf):
  
      ```bash
      jar ufe cx3d-mvn-0.0.4-SNAPSHOT.jar DividingCell
      java -jar cx3d-mvn-0.0.4-SNAPSHOT.jar 
      ```
  
        2. Or, to reproduce a figure from [the Frontiers paper](http://journal.frontiersin.org/article/10.3389/neuro.10.025.2009/full):
   
      ```bash
      jar ufe cx3d-mvn-0.0.4-SNAPSHOT.jar Figure_3_G
      java -jar cx3d-mvn-0.0.4-SNAPSHOT.jar
      ```
        3. Once the the simulation window opens, uncheck "Pause" to begin simulation.

  NB: For other examples/figures, replace "DividingCell/Figure_3_G" above with others from the [tutorial](src/main/java/sc/iview/cx3d/simulations/tutorial) or [frontiers](src/main/java/sc/iview/cx3d/simulations/frontiers) folders.


## Development
This remains the most active fork of Cx3D. The original [Cx3D Subversion repository](https://svn.ini.uzh.ch/pub/cx3dp-core/.)  is stalled.

## Acknowledgements
This version of Cx3D derived from @tferr's [mavenized fork](https://github.com/tferr/cx3d-mvn)

## Resources
 * [Official Cx3D website](https://www.ini.uzh.ch/~amw/seco/cx3d/) (for convenience, some of the Cx3D tutorials are also mirrored in the [misc directory](./misc))
 * [Parallelized version of Cx3D](https://github.com/tferr/cx3dp-mvn)
 * [C++ port of Cx3D](https://github.com/breitwieser/cx3d-cpp)
