.. index:: installation

.. _rpm-installation:

============
Installation
============

.. note::
  * All servers in the cluster should have a static IP address.
  * The server(s) must have only one active NIC or IP interface.
  * Only IPv4 is supported. Disabling IPv6 on the NIC during OS install is recommended.
  * Review the partition sizes if automatic partitioning is used.

Recommended Specs
-----------------

* 2x CPU/vCPU
* 8GB RAM
* 50GB or larger disk

Operating System
----------------

Recent sipXcom RPMs will only install on top of CentOS 7.x with amd64/x86_64 architecture. We recommend using the `CentOS minimal ISO <http://isoredirect.centos.org/centos/7/isos/x86_64/>`_.

Disk Partitioning Recommendations
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* 1GB ext2 for the /boot partition with the boot flag set
* swap partition equal to the system RAM size
* Allocate the rest of the free space for the root (/) partition as a LVM volume, XFS formatted

.. warning::
  If the disk is larger than 50G and you use automatic partitioning, most of the space will be allocated to /home rather than /.

Downloading RPMs
----------------

Run yum update to update OS packages first. Reboot if you need to after::

        yum update -y
	reboot

Install wget::

	yum install wget -y

Add the sipxcom 20.04 repository file beneath /etc/yum.repos.d, then run yum update to update available packages::

	wget -P /etc/yum.repos.d/ http://download.sipxcom.org/pub/sipXecs/20.04-centos7/sipxecs-20.04.0-centos.repo
	yum update

Install the sipxcom packages::

	yum install sipxcom -y

