.. index:: building

.. _building:

============
Building
============

Setup Environment
-----------------

Follow the :ref:`Environment <environment>` instructions to prepare your system for builds.

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

Execute the following commands to checkout the default sipXcom repository:

  .. code-block:: bash

    dnf install -y git

    mkdir /src

    cd /src

    git clone https://github.com/onrelay/sipxecs.git


If you are looking to build a specific branch, specify it as follows:

  .. code-block:: bash

    git clone https://github.com/onrelay/sipxecs.git  --branch release-25.01-rocky9


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

        **-p | --platform**: OS platform of sipxcom RPM to build, e.g. rocky-7=9 (default), centos-7

        **-a | --architecture**: Hardware architecture of sipxcom RPM to build, e.g. x86_64 (default)

        **-s | --subproject**: subproject to build or sipx for building all RPMs, e.g. sipx (default), sipXconfig, sipXproxy

        **-v | --version**: sipXcom cersion to build, e.g 25.01 (default)

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

    sudo ../configure --enable-rpm DISTRO="rocky-9-x86_64"

    sudo make sipx.rpm
    
- Run `sudo chown -R sipx.sipx repo` if it gives a permission error on first try

- If compilation stops for a subproject, it is possible to list all its dependencies:

  .. code-block:: bash

    cd /src/sipxecs/sipXproxy

    grep -R '^BuildRequires'  | awk '{print $2}'






