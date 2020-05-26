<h1 align="center">SciView - Cx3D</h1>
<div align="center">
  Image-based modeling using SciView
</div>
<div align="center">
  <!-- Build Status -->
  <a href="https://travis-ci.org/morphonets/cx3d">
    <img alt="build" src="https://travis-ci.org/morphonets/cx3d.svg?branch=master">
  </a>
    <!-- Issues -->
  <a href="https://github.com/morphonets/cx3d/issues">
    <img alt="GitHub issues" src="https://img.shields.io/github/issues/morphonets/cx3d">
  </a>
  <a href="https://github.com/morphonets/cx3d/issues">
    <img alt="GitHub closed issues" src="https://img.shields.io/github/issues-closed/morphonets/cx3d">
  </a>
</div>

Cx3D (Cortex simulation in 3D) is a tool for simulating the growth of cortex in 3D initially developed at the [Institute of Neuroinformatics](http://www.ini.uzh.ch/) of the [University of Zürich](http://www.uzh.ch/) and [ETH Zürich](http://www.ethz.ch/). It was released in [2009](https://www.ini.uzh.ch/~amw/seco/cx3d/) under the [GNU General Public License version 3](http://www.gnu.org/licenses/gpl.html). 

This repository hosts the [SciView](http://sc.iview.sc) version of Cx3D, making Cx3D compatible with the ImageJ and Fiji ecosystem. This includes using SciView for 3D visualization, allowing Cx3D to grow neuronal processes with SciView’s data structures, and support for image-based modeling. 

## Quick Start

  1. Clone the repository
  2. Import into IntelliJ/Eclipse/NetBeans IDE
  3. Run [sc.iview.cx3d.commands.RandomBranchingDemo](https://github.com/morphonets/cx3d/blob/master/src/main/java/sc/iview/cx3d/commands/RandomBranchingDemo.java)

### To run the original Cx3D demos and tutorials from the CLI

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

  3. To run an example from [the tutorial](misc/Cx3DTutorial.pdf):

  ```bash
  jar ufe cx3d-mvn-0.0.4-SNAPSHOT.jar DividingCell
  java -jar cx3d-mvn-0.0.4-SNAPSHOT.jar 
  ```

4. Or, to reproduce a figure from [the 2009 Frontiers paper](http://journal.frontiersin.org/article/10.3389/neuro.10.025.2009/full):

  ```bash
  jar ufe cx3d-mvn-0.0.4-SNAPSHOT.jar Figure_3_G
  java -jar cx3d-mvn-0.0.4-SNAPSHOT.jar
  ```

  NB: For other examples/figures, replace "DividingCell/Figure_3_G" above with others from the [tutorial](src/main/java/sc/iview/cx3d/simulations/tutorial) or [frontiers](src/main/java/sc/iview/cx3d/simulations/frontiers) folders.

## Development
This remains the most active fork of Cx3D. The original [Cx3D Subversion repository](https://svn.ini.uzh.ch/pub/cx3dp-core/.) is stalled.

## Contributing
Want to contribute? Please, please do! We welcome [issues](https://github.com/morphonets/cx3d/issues) and [pull requests](https://github.com/morphonets/cx3d/pulls) any time.

## Acknowledgements
This version of Cx3D derived from @tferr's [mavenized fork](https://github.com/tferr/cx3d-mvn)

## Resources
 * [Official Cx3D website](https://www.ini.uzh.ch/~amw/seco/cx3d/) (for convenience, some of the Cx3D tutorials are also mirrored in the [misc directory](./misc))
 * [Parallelized version of Cx3D](https://github.com/tferr/cx3dp-mvn) (abandoned project)
 * [C++ port of Cx3D](https://github.com/breitwieser/cx3d-cpp)
