.. index:: upgrading

============
Upgrading
============


Upgrading from Legacy 21.04 on CentOS 7
----------------------------------------

Follow the below procedure to upgrade an existing 21.04 sipXcom installation on CentOS 7.

Backup
~~~~~~~~~~~~

Take a backup from within the sipXcom admin interface.

If you are using cloud images we also recommend taking a complete disk snapshot before upgrading.

Setup Repos
~~~~~~~~~~~~

- If you are NOT using a Google Cloud image, you must add and install Google's artifact registry plugin:

  .. code-block:: bash

    wget -O /etc/yum.repos.d/artifact-registry-plugin.repo \
        https://storage.googleapis.com/sipxecs/artifact-registry/artifact-registry-plugin.repo
    
    yum install -y yum-plugin-artifact-registry

- Update your sipXcom repo:

  .. code-block:: bash

    rm /etc/yum.repos.d/sipxecs-21.04.0-centos.repo 

    wget -O /etc/yum.repos.d/sipxcom.repo \
        https://storage.googleapis.com/sipxecs/sipxcom/24.01/centos-7-x86_64/sipxcom.repo


Upgrade sipXcom
~~~~~~~~~~~~~~~~

All changes are compatible with 21.04, so just update your sipXcom RPMs with yum as follows:

  .. code-block:: bash

    systemctl stop sipxecs.service

    yum update -y

    reboot

Your system should come back up after reboot as normal, but you may want to regenerate profiles from the server admin page.

