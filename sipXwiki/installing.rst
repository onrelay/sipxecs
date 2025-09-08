.. index:: installing

.. _installing:

===================
Installing 
===================

.. note::
If you are still using CentOS7, which is now End Of Life, please follow the :ref:`_installing-centos7` instructions to install sipXcom.

Setup sipXcom repo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
  .. code-block:: bash

    wget -O /etc/yum.repos.d/sipxcom.repo \
      https://storage.googleapis.com/sipxecs/sipxcom/25.01/rocky-9-x86_64/sipxcom.repo
    

Enable elasticsearch
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  .. code-block:: bash

    yum install -y elasticsearch

    systemctl enable elasticsearch

    service elasticsearch start


Install and Setup sipXcom
--------------------------
  
Install sipXcom RPMs
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  .. code-block:: bash
    
    yum install -y sipxcom


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

    yum update -y
    
    reboot

After a few minutes, the administration web interface should be available at *https://your-host-name-or-ip-address/*