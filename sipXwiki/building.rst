.. index:: building

============
Building
============

Server Build
-----------------

You may build sipXcom on a physical server or cloud image where you intend to host the software or create RPMs. 

- 2x CPU/vCPU
- 8GB RAM
- 50GB or larger disk
- Minimal CentOS7 OS installation

Build Server
-----------------

If you build on a physical server, prepare your image as follows:

- Log on as root via ssh

- Run `yum update -y`

- On first boot you may need to edit */etc/sysconfig/network-scripts/YourNICCard*. Change `ONBOOT="no"` to `ONBOOT="yes"`

- Increase Max Number of open files and max user processes for MongoDB (important for larger systems)

  - edit */etc/sysctl.conf* to add fs.file-max = 65536 line. ONLY do this if the default returned from `cat /cproc/sys/fs/file-max` is less than 65536.

  - edit */etc/security/limits.conf* to add the following block of text:

        .. code-block:: bash

            *          soft     nproc          65535 
            *          hard     nproc          65535
            *          soft     nofile         65535
            *          hard     nofile         65535`

- Run `reboot`

Docker Build
-----------------

To instead build sipXcom in a docker image, instantiate a container with the following command:

    .. code-block:: bash
        
        docker run -it --hostname=sipxecs --name=sipxecs-centos7 --privileged --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin --label='org.label-schema.build-date=20201113' --label='org.label-schema.license=GPLv2' --label='org.label-schema.name=CentOS Base Image' --label='org.label-schema.schema-version=1.0' --label='org.label-schema.vendor=CentOS' --label='org.opencontainers.image.created=2020-11-13 00:00:00+00:00' --label='org.opencontainers.image.licenses=GPL-2.0-only' --label='org.opencontainers.image.title=CentOS Base Image' --label='org.opencontainers.image.vendor=CentOS' --runtime=runc -d centos:centos7

Update System
-----------------

- Log on as root via ssh

- Run `yum update -y`

- Run `yum install -y sudo git wget`

- If you are not using a Google Cloud image, you must add and install their artifact registry plugin:

  .. code-block:: bash

    wget -O /etc/yum.repos.d/artifact-registry-plugin.repo https://storage.googleapis.com/sipxecs/artifact-registry/artifact-registry-plugin.repo
    
    yum install -y yum-plugin-artifact-registry`


Add sipx User
-----------------

sipXcom must be built by a user called *sipx* with sudo privileges. Add the *sipx* user as follows:

- Run `useradd -m sipx`
- If not on docker, run `passwd sipx`
- Run `visudo` and append:
  
    .. code-block:: bash

        # add sipx as sudo user
        sipx    ALL=(ALL)       NOPASSWD:ALL

Checkout sipXcom
-----------------

Execute the following commands to checkout the sipXcom repository:

  .. code-block:: bash

    mkdir /src

    cd /src

    git clone https://github.com/onrelay/sipxecs.git

    chown -R sipx.sipx sipxecs

Run Master Build Script
-----------------------

For straightforward builds, just execute the master build script:

  .. code-block:: bash

    su sipx

    cd /src/sipxecs

    sudo ./master-build.sh [options]

`master-build.sh` will create /src/sipxecs/build and /usr/local/sipx directories where all build results are saved.

To build all the source on a host server where you intend to run sipXcom, simply run:

  .. code-block:: bash

    sudo ./master-build.sh

To build all the sipX* rpms from source, just add the --rpm option as follows:

  .. code-block:: bash

    sudo ./master-build.sh --rpm

Otherwise, the master-build.sh script has the following options:

- **-p | --platform**: OS platform of sipxcom RPM to build, e.g. centos-7 (default), rocky-9

- **-a | --architecture**: Hardware architecture of sipxcom RPM to build, e.g. x86_64 (default)

- **-s | --subproject**: subproject to build or sipx for building all RPMs, e.g. sipx (default), sipXconfig, sipXproxy

- **-v | --version**: sipXcom cersion to build, e.g 24.01 (default), 24.07

- **-r | --rpm**: Include this option if building rpms


Advanced Builds
---------------

For more advanced builds, sipXcom relies on GNU autoconf and make mechanisms to build its source. To use these mechanisms directly, you may use the following steps:

- Prepare build folders:  
    - Run `mkdir -p /src/sipxecs/build`

    - Run `cd /src/sipxecs/build`

    - Run `sudo mkdir -p /usr/local/sipx`

    - Run `sudo chown sipx.sipx /usr/local/sipx`

- To exclude *oss_core* module from build:
    - Run `sudo echo oss_core >> .modules-exclude`
    - Run `sudo yum install -y oss_core oss_core-devel oss_core-debuginfo`
    - Run `sudo mkdir -p /usr/local/sipx/lib`
    - Run `sudo ln -s /usr/lib64/liboss_core.la /usr/local/sipx/lib/liboss_core.la`
    - Run `sudo ln -s /usr/lib64/liboss_carp.la /usr/local/sipx/lib/liboss_carp.la`
    - Run `sudo mkdir -p /usr/local/sipx/opt`
    - Run `sudo ln -s /usr/opt/ossapp /usr/local/sipx/opt/ossapp`

- Configure:
    - Run `cd /src/sipxecs`
    - Run `sudo autoreconf -ivf`
    - Run `sudo chown -R sipx.sipx build`

- To build locally:
    - Run `cd build`
    - Run `sudo ../configure` 
    - Run `sudo make sipx`
 
- Create a repo to build RPMs with mock:
    - Run `sudo yum install -y createrepo rpm-build mock`
    - Run `sudo wget http://li.nux.ro/download/nux/misc/el7/x86_64/thttpd-2.25b-33.el7.nux.x86_64.rpm`
    - Run `sudo rpm -ivh thttpd-2.25b-33.el7.nux.x86_64.rpm`
    - Run `rm -f thttpd-2.25b-33.el7.nux.x86_64.rpm`
    - Run `sudo usermod -a -G mock sipx`

    - Run e.g. `sudo ../configure --enable-rpm DISTRO="centos-7-x86_64"`
    - Run `sudo make sipx.rpm` (run `sudo chown -R sipx.sipx repo` if it gives a permission error on first try)

Resolving Dependencies
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If compilation stops for a subproject, it is possible to list all its dependencies:
- E.g. run `cd /src/sipxecs/sipXproxy`
- Run `grep -R '^BuildRequires'  | awk '{print $2}'`






