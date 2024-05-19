.. index:: installing

.. _rpm-installation:

===================
Installing 
===================

System Specs
----------------------

Recent sipXcom RPMs will only install on CentOS 7.x with amd64/x86_64 architecture. 

We recommend using the `CentOS minimal ISO <http://isoredirect.centos.org/centos/7/isos/x86_64/>`_.

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


Prepare Server
---------------------

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

- On first boot you may need to edit */etc/sysconfig/network-scripts/YourNICCard*. Change `ONBOOT="no"` to `ONBOOT="yes"`

- Update OS: 

  .. code-block:: bash

    yum update -y

- Install `wget` used for downloading RPMs

  .. code-block:: bash

    yum install -y wget

- Remove `epel` to avoid conflicts

  .. code-block:: bash

    yum remove -y epel-release

Setup Google Could Artifact registry
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you are NOT using a Google Cloud image, you must add and install their artifact registry plugin:

  .. code-block:: bash

    wget -O /etc/yum.repos.d/artifact-registry-plugin.repo \
      https://storage.googleapis.com/sipxecs/artifact-registry/artifact-registry-plugin.repo
    
    yum install -y yum-plugin-artifact-registry

Configure System
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Increase Max Number of open files and max user processes for MongoDB (important for larger systems)

- edit */etc/sysctl.conf* to add fs.file-max = 65536 line. ONLY do this if the default returned from `cat /proc/sys/fs/file-max` is less than 65536.

- edit */etc/security/limits.conf* to add the following block of text:

  .. code-block:: bash

    *          soft     nproc          65535
    *          hard     nproc          65535
    *          soft     nofile         65535
    *          hard     nofile         65535`

- Enable elasticsearch

  .. code-block:: bash

    systemctl enable elasticsearch

    service elasticsearch start

- Reboot system:

  .. code-block:: bash

    reboot


Install sipXcom
----------------

- Retrieve and install sipXcom RPM:
  
  .. code-block:: bash

    wget -O /etc/yum.repos.d/sipxcom.repo \
      https://storage.googleapis.com/sipxecs/sipxcom/24.01/centos-7-x86_64/sipxcom.repo
    
    yum install -y sipxcom

Setup sipXcom
----------------

Initial setup
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- Execute the sipXcom setup script:

  .. code-block:: bash

    sipxecs-setup

The system will reboot to disable selinux to allow the rest of the setup routine to work properly.

Network Configuration
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- Run setup script again:
  
  .. code-block:: bash

    sipxecs-setup

- Answer questions as follows for a single server instance:

  - hostname: e.g. *us1*

  - domain: e.g. *us1.onrelay.net*
  
  - SIP Domain: e.g. *us1.onrelay.net*
  
  - SIP Realm: e.g. *us1.onrelay.net*
    
    Ignore *"Failed to open /dev/tty: No such device or address"* warnings

- Update system again and reboot:
  
  .. code-block:: bash

    yum update -y
    
    reboot

After a few minutes, the administration web interface should be available at *https://your-host-name-or-ip-address/*