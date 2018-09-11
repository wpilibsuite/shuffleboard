# Travis CI

By default Travis uses Ubuntu 14.04 LTS for building,
but it is possible to run the build in any Linux distribution using [Docker](https://www.docker.com/).

Network Tables relies upon the ABI (Application Binary Interface) that is incompatible
with Ubuntu 14.04. As such, trying to call any Network Tables methods cause errors
when the JNI library is loaded.

The workaround for this issue is to use a Docker container running Ubuntu 16.04
inside of Travis CI.

This build configuration is inspired by the
[openSUSE/snapper](https://github.com/openSUSE/snapper) project.

The Docker image Travis caching mechanism logic was inspired by
[this Github comment](https://github.com/travis-ci/travis-ci/issues/5358#issuecomment-248915326).

## Setup
The `Dockerfile` defines the steps needed for building the Docker image that
Shuffleboard is tested within.

The `.travis.yml` file defines the build matrix which runs the build using Docker.

## Running the Build Locally

**NOTE** This is only necessary if you are having issues with tests failing on TravisCI.
You should be able to develop for Shuffleboard on your local machine without involving
Docker.

 1. [Install and start Docker](https://docs.docker.com/engine/installation/)
 2. Run a similar same commands as in the `.travis.yml` file:
    1. First build the docker image locally:
       ```bash
       docker build -f Dockerfile -t shuffleboard-devel .
       ```
       The Docker image automatically includes also the copy of the current Shuffleboard sources.
       If you change any of the sources you will need to re-run this step.
       Don't worry, it gets faster after the first time as Docker can skip downloading the
       `apt` packages.
    2. Then run the build:
       ```bash
       docker run -it --rm shuffleboard-devel ./.travis.ubuntu.sh
       # ALTERNATIVELY:
       # If you want to use the gradle dependency cache on your host machine:
       docker run -it --rm -v $HOME/.gradle/:/root/.gradle/ shuffleboard-devel ./.travis.ubuntu.sh
       ```
       (The `--rm` will cleanup the new Docker image layer created by the build,
       if you want to inspect the build artifacts then remove it.)
 3. If you need to debug a failure then run this instead of the Travis script and run the
    build steps manually:
    ```bash
    docker run -it --rm shuffleboard-devel bash
    ```
    If you need an editor or some other tool you can install them via the respective packaging tool,
    see the Dockerfile for examples.
