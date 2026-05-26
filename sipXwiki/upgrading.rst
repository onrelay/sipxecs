.. index:: upgrading

============
Upgrading
============

.. note::
If you are still using CentOS7, which is now End Of Life, please follow the :ref:`Upgrading CentOS7 <upgrading-centos7>` instructions to upgrade older sipXcom CentOS7 installations to the latest 24.01 version. 

Upgrading from 24.01 CentOS 7 to 25.01 on Rocky Linux 9
----------------------------------------------------------

Install
~~~~~~~~~~~~

Follow the :ref:`installing <installing>` instructions to setup a new Rocky Linux 25.01 system.

Consider carefully whether you want the new installation on the same domain with a hard switchover, 
or e.g. if you want to gradually move users to a new domain and / or setup a load balancer with the old domain.

Backup
~~~~~~~~~~~~

Take an FTP backup from within the old CentOS7 24.01 sipXcom admin interface.

Restore
~~~~~~~~~~~~

Restore from the CentOS7 FTP backup from the Rocky Linux 25.01 sipXcom admin interface.


Upgrading Existing  Releases on Rocky Linux 9
----------------------------------------------------------


All sipXcom code changes are backwards compatible, so just update your sipXcom RPMs with dnf as follows:

  .. code-block:: bash

    service sipxecs stop

    dnf clean all

    dnf update -y


Reboot your system:

  .. code-block:: bash

    reboot

Your system should come back up after reboot as normal, but you may want to regenerate profiles from the server admin page.
