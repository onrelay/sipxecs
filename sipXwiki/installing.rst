.. index:: installing

.. _installing:

===================
Installing 
===================

Before following the below instructions, setup your environment as described :ref:`here <environment>`.

.. note::
If you are still using CentOS7, which is now End Of Life, please follow the :ref:`Installing CentOS7 <installing-centos7>` instructions to install sipXcom.

Setup sipXcom repo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
  .. code-block:: bash

    dnf install -y wget

    wget -O /etc/yum.repos.d/sipxcom.repo \
      https://storage.googleapis.com/sipxecs/sipxcom/25.01/rocky-9-x86_64/sipxcom.repo
    

Enable additional repos
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  .. code-block:: bash

    dnf install epel-release -y

    dnf config-manager --set-enabled crb


Install and Setup sipXcom
--------------------------
  
Install sipXcom RPMs
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  .. code-block:: bash
    
    dnf install -y sipxcom


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

  - domain: e.g. *onrelay.net*
  
  - SIP Domain: e.g. *us1.onrelay.net*
  
  - SIP Realm: e.g. *us1.onrelay.net*
    
    Ignore *"Failed to open /dev/tty: No such device or address"* warnings

- Update system again and reboot:
  
  .. code-block:: bash

    dnf update -y
    
    reboot

After a few minutes, the administration web interface should be available at *https://your-host-name-or-ip-address/*
