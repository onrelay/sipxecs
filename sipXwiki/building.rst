.. index:: building

============
Building
============

Use Build Server
-----------------

To build the sipXcom source for execution or RPM generation on a physical server or cloud image, follow the instructions from installing_ to setup and configure a server.

Use Docker Container
-----------------

To build sipXcom RPMs in a desktop or server Docker image, instantiate a container with the following command:

    .. code-block:: bash
        
        docker run -it --hostname=sipxecs --name=sipxecs-centos7 --privileged \
        --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
        --label='org.label-schema.build-date=20201113' --label='org.label-schema.license=GPLv2' \
        --label='org.label-schema.name=CentOS Base Image' --label='org.label-schema.schema-version=1.0' \
        --label='org.label-schema.vendor=CentOS' --label='org.opencontainers.image.created=2020-11-13 00:00:00+00:00' \
        --label='org.opencontainers.image.licenses=GPL-2.0-only' --label='org.opencontainers.image.title=CentOS Base Image' \
        --label='org.opencontainers.image.vendor=CentOS' --runtime=runc -d centos:centos7

Setup System
-----------------

- Log on as root via ssh

  .. code-block:: bash
    
    yum update -y

    yum install -y sudo git wget

- If you are NOT using a Google Cloud image, you must add and install their artifact registry plugin:

  .. code-block:: bash

    wget -O /etc/yum.repos.d/artifact-registry-plugin.repo \
        https://storage.googleapis.com/sipxecs/artifact-registry/artifact-registry-plugin.repo
    
    yum install -y yum-plugin-artifact-registry


Add sipx User
-----------------

sipXcom must be built by a user called *sipx* with sudo privileges. 

- Add the *sipx* user:

    .. code-block:: bash

        useradd -m sipx
  
- If not on desktop docker, protect the sipx user with a password:
  
    .. code-block:: bash

        passwd sipx
  
- Assign sipx user sudo privileges:
  
    .. code-block:: bash

        visudo 
        
        ->

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

Build sipXcom
-----------------------

To build sipXcom from source, execute the master build script from the root scr folder as the sipx user with sudo privileges:

  .. code-block:: bash

    su sipx

    cd /src/sipxecs

The `sipxecs-build` script will create /src/sipxecs/build and /usr/local/sipx directories where all build results are saved.


Build RPMs
~~~~~~~~~~~~~~~~~~

To build all the sipX* RPMs from source, just add the --rpm option to the sipxecs-build script as follows:

  .. code-block:: bash

    sudo ./sipxecs-build --rpm

The resulting RPMs are e.g. found in the build/repo/CentOS_7/x86_64 folder for CentOS 7.

To also install the RPMs locally, add the '--install' option:

  .. code-block:: bash

    sudo ./sipxecs-build --rpm --install

Build Executables
~~~~~~~~~~~~~~~~~~

To just build the sipX* executables from source, simply run:

  .. code-block:: bash

    sudo ./sipxecs-build

Additional Build Options
~~~~~~~~~~~~~~~~~~~~~~~~~

The sipxecs-build script has the following additional options:

  .. code-block:: bash

    sudo ./sipxecs-build [options]

        **-p | --platform**: OS platform of sipxcom RPM to build, e.g. centos-7 (default), rocky-9

        **-a | --architecture**: Hardware architecture of sipxcom RPM to build, e.g. x86_64 (default)

        **-s | --subproject**: subproject to build or sipx for building all RPMs, e.g. sipx (default), sipXconfig, sipXproxy

        **-v | --version**: sipXcom cersion to build, e.g 24.01 (default), 24.07

        **-r | --rpm**: Include this option if building rpms


Advanced Builds
~~~~~~~~~~~~~~~

For more advanced builds, sipXcom relies on GNU autoconf and make to build its source. To use these mechanisms directly, you may use the following steps:

- Prepare build folders:  

  .. code-block:: bash

    mkdir -p /src/sipxecs/build

    cd /src/sipxecs/build

    sudo mkdir -p /usr/local/sipx

    sudo chown sipx.sipx /usr/local/sipx

- To exclude *oss_core* module from build:

  .. code-block:: bash

    sudo echo oss_core >> .modules-exclude

    sudo yum install -y oss_core oss_core-devel oss_core-debuginfo

    sudo mkdir -p /usr/local/sipx/lib

    sudo ln -s /usr/lib64/liboss_core.la /usr/local/sipx/lib/liboss_core.la

    sudo ln -s /usr/lib64/liboss_carp.la /usr/local/sipx/lib/liboss_carp.la

    sudo mkdir -p /usr/local/sipx/opt

    sudo ln -s /usr/opt/ossapp /usr/local/sipx/opt/ossapp

- Configure:

  .. code-block:: bash

    cd /src/sipxecs

    sudo autoreconf -ivf

    sudo chown -R sipx.sipx build


- To build for running sipXcom locally:

  .. code-block:: bash

    cd build

    sudo ../configure

    sudo make sipx
 
- Create a repo to build RPMs with mock:

  .. code-block:: bash

    sudo yum install -y createrepo rpm-build mock

    sudo wget http://li.nux.ro/download/nux/misc/el7/x86_64/thttpd-2.25b-33.el7.nux.x86_64.rpm

    sudo rpm -ivh thttpd-2.25b-33.el7.nux.x86_64.rpm

    rm -f thttpd-2.25b-33.el7.nux.x86_64.rpm

    sudo usermod -a -G mock sipx`

- To create sipXcom RPMs:

  .. code-block:: bash

    sudo ../configure --enable-rpm DISTRO="centos-7-x86_64"

    sudo make sipx.rpm
    
- Run `sudo chown -R sipx.sipx repo` if it gives a permission error on first try

- If compilation stops for a subproject, it is possible to list all its dependencies:

  .. code-block:: bash

    cd /src/sipxecs/sipXproxy

    grep -R '^BuildRequires'  | awk '{print $2}'






