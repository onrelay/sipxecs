# Welcome to sipXecs!

sipXcom is an advanced open soure SIP / IP based enterprise communication system originally developed by PingTel in the early 2000s. It has since then been developed and sponsored by commercial organizations such as Avaya, Nortel, eZuce and CoreDial. It is now being further improved and maintained by OnRelay, who uses sipXcom as an integral part of its cloud based Mobile Business Phone service.

Both sipXecs and sipXcom has been used interchangably for historic reasons. At OnRelay we now use sipXecs to name this open source project and sipXcom to name the resulting product, but this terminology will not be consistent across the code base and legacy documentation.

## Platform Support

Recent versions of sipXcom are built and tested only for CentOS 7 x86_64 architectures. Upcoming releases will support Rocky Linux 9.

### Cloud Server Image
To install or build sipXcom on a cloud Linux image, the following configuration is recommended:

- Min 100GB disk 
- Min 2 CPUs
- Min 8GB memory
- Network interface with a static public IP address
- Assign a public domain for autogeneration of Let's Encrypt SSL certificates
- Minimal OS installation

## Installation

Use the following procedure to install sipXcom on a fresh Linux instance

### Prepare Server

- On first boot - you may need to edit your /etc/sysconfig/network-scripts/YourNICCard. Change `ONBOOT="no"` to `ONBOOT="yes"`

- Run `yum update -y`

- Increase Max Number of ulimit open files and max user processes for MongoDB (important for larger systems)

    - edit /etc/sysctl.conf to add fs.file-max = 65536 line. ONLY do this if default found from `cat /cproc/sys/fs/file-max` is less than 65536.

    - edit /etc/security/limits.conf to add the following block of text:

            *          soft     nproc          65535
            *          hard     nproc          65535
            *          soft     nofile         65535
            *          hard     nofile         65535`

- reboot

### Install sipXcom

- run `yum install -y wget` for downloading repo definitions

- If not a GCP image, we must add and install the artifact registry plugin:

    - Run `wget -O /etc/yum.repos.d/artifact-registry-plugin.repo https://storage.googleapis.com/sipxecs/artifact-registry/artifact-registry-plugin.repo`

    - Run `yum install -y yum-plugin-artifact-registry`

- Run `wget -O /etc/yum.repos.d/sipxcom.repo https://storage.googleapis.com/sipxecs/sipxcom/24.01/centos-7-x86_64/sipxcom.repo`

- Run `yum install -y sipxcom`

- Run `sipxecs-setup` and the system will reboot to disable selinux to allow the rest of the setup routine to work properly.

- Run set`sipxecs-setup`up again and e.g. answer basic questions as follows for a single server instance:
    - hostname: e.g. us1
    - domain: e.g. us1.onrelay.net
    - SIP Domain: e.g. us1.onrelay.net
    - SIP Realm: e.g. us1.onrelay.net
    
        Ignore *"Failed to open /dev/tty: No such device or address"* warnings

- yum update -y

- reboot

- After a couple of minutes, the administration web interface should be available at `https://your-host-name-or-ip-address/`

## Building

### Docker Build
To build sipXcom in a docker image, instantiate a container with the following command:

    docker run -it --hostname=sipxecs --name=sipxecs-centos7 --privileged --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin --label='org.label-schema.build-date=20201113' --label='org.label-schema.license=GPLv2' --label='org.label-schema.name=CentOS Base Image' --label='org.label-schema.schema-version=1.0' --label='org.label-schema.vendor=CentOS' --label='org.opencontainers.image.created=2020-11-13 00:00:00+00:00' --label='org.opencontainers.image.licenses=GPL-2.0-only' --label='org.opencontainers.image.title=CentOS Base Image' --label='org.opencontainers.image.vendor=CentOS' --runtime=runc -d centos:centos7

### Server Build
To build sipXcom on a server where you intend to host the software, follow the steps under *Installation - Prepare Server* above to setup your OS environment.

### Add sipx User
sipXcom must be built by a user called sipx with sudo privileges. Add sipx user as follows:

- Run `yum install -y sudo`
- Run `useradd -m sipx`
- If not on docker, run `passwd sipx`
- Run `visudo` and append:

        # add sipx as sudo user
        sipx    ALL=(ALL)       NOPASSWD:ALL

### Checkout sipXcom
- Run `mkdir /src` and `cd /src`
- Run `git clone https://github.com/onrelay/sipxecs.git`
- Run `chown -R sipx.sipx sipxecs`

### Run Master Build Script
- Run `su sipx` to operate as sipx user
- Run `cd /src/sipxecs`

You can now use `sudo ./master-build.sh [options]` to configure and build the source in one step.

This script will create /src/sipxecs/build and /usr/local/sipx directories where all build results are saved.

#### Building For Current Server

To build on a host server where you intend to run sipXcom, simply run `sudo ./master-build.sh`.

#### Building on Docker
To build all rpms on a docker image, simply run `sudo ./master-build.sh --rpm`.

#### Other Build Options
Additionally, the master-build.sh script has the following options:
- **-p | --platform**: OS platform of sipxcom RPM to build, e.g. centos-7 (default), rocky-9
- **-a | --architecture**: Hardware architecture of sipxcom RPM to build, e.g. x86_64 (default)
- **-s | --subproject**: subproject to build or sipx for building all RPMs, e.g. sipx (default), sipXconfig, sipXproxy
- **-v | --version**: sipXcom cersion to build, e.g 24.01 (default), 24.07
- **-r | --rpm**: Include this option if building rpms

#### Advanced Builds
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
    - Run `sudo make sipx.rpm` 
    (run `sudo chown -R sipx.sipx repo` if it gives a permission error on first try)

#### Resolving Dependencies:
If compilation stops for a subproject, it is possible to list all its dependencies:
- E.g. run `cd /src/sipxecs/sipXproxy`
- Run `grep -R '^BuildRequires'  | awk '{print $2}'`






