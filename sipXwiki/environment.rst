.. index:: evironment

.. _environment:

======================
Operating Environment 
======================

Recent sipXcom RPMs will only install on Rocky Linux 9.x with x86_64 architecture. 

Both physical servers and dedicated cloud images are supported. 

Docker images may be used for builds, but have not been verified as an execution environment.

Hardware Platform
---------------------

Cloud Images
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The sipXecs open source project is primarily built and validated on Google Cloud Compute Rocky Linux 9.x minimal images.

We recommend using the `Rocky Linix minimal image <https://console.cloud.google.com/marketplace/browse?filter=partner:Rocky%20Linux>`_.

The following is a recommended image configuration: 

- n2-standard-2
- Intel Cascade Lake
- x86/64 architecture
- 2 vCPUs
- 8 GB Memory
- 50GB or larger disk
- VirtIO NIC type

Similar configurations should be used for Rocky Linux 9.x on AWS and Microsoft Azure.

Physical Servers
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

We recommend using the `Rocky Linix minimal ISO <https://download.rockylinux.org/pub/rocky/9/isos/x86_64/Rocky-9.6-x86_64-minimal.iso>`_.

The following is a recommended hardware configuration: 

- 2x CPU/vCPU
- 8GB RAM
- 50GB or larger disk

.. note::
  * All servers in the cluster should have a static IP address.
  * The server(s) must have only one active NIC or IP interface.
  * Only IPv4 is supported. Disabling IPv6 on the NIC during OS install is recommended.
  * Review the partition sizes if automatic partitioning is used.
  * 1GB ext2 for the /boot partition with the boot flag set
  * Set swap partition equal to the system RAM size
  * Allocate the rest of the free space for the root (/) partition as a LVM volume, XFS formatted

.. warning::
  If the disk is larger than 50G and you use automatic partitioning, most of the space will be allocated to /home rather than /.


Docker
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

SipXecs open source project RPMs can be built on Docker images, but has not yet been debugged or validated to exectute in Docker containers.
Contributors are of course welcome to try and suggest corresponding pull requests.

To build RPMs on Docker container add the following image:

  .. code-block:: bash

    docker run -it --hostname=sipxecs --name sipxecs-rocky-linux-9 --privileged \
        --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
        --runtime=runc -d rockylinux:9


System Preparation
---------------------

The following prepares the server environment for both builds and installations.


SSH Root Login
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- Log on as root via ssh:

  .. code-block:: bash

    sudo -s

- If you want root password authentication:

  .. code-block:: bash

    passwd 

    vi /etc/ssh/sshd_config =>

      PermitRootLogin yes

      PasswordAuthentication yes
    
    service sshd restart

.. warning::
Make sure SSH port 22 on your server is firewall restricted to known IPs if you allow root login via SSH


Install and Update Dependencies
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- Update OS: 

  .. code-block:: bash

    dnf update -y


Setup Google Could Artifact registry
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you are NOT using a Google Cloud image, you must add and install their artifact registry plugin:

  .. code-block:: bash

    dnf install -y wget

    wget -O /etc/yum.repos.d/artifact-registry-plugin.repo \
      https://storage.googleapis.com/sipxecs/artifact-registry/artifact-registry-plugin.repo
    
    yum install -y yum-plugin-artifact-registry


Reboot
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- Reboot system command:

  .. code-block:: bash

    reboot


Build or Install
---------------------

You are ready to follow the instructions to building_ or installing_ sipXcom.