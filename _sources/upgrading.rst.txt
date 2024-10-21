.. index:: upgrading

============
Upgrading
============


Upgrading from Legacy 21.04 to 24.01 on CentOS 7
----------------------------------------------------------

From 24.01 the sipXcom repository has moved to a Google Cloud Artifact Registry.

Follow the below procedure to upgrade an existing 21.04 sipXcom installation to 24.01 or later on CentOS 7.

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

- Since CentOS 7 is now end of life, we must use its vault for yum

  .. code-block:: bash
    
    sed -i 's|mirror.centos.org|vault.centos.org|g' /etc/yum.repos.d/CentOS-*
    sed -i 's|mirrorlist|#mirrorlist|g' /etc/yum.repos.d/CentOS-*
    sed -i 's|#baseurl|baseurl|g' /etc/yum.repos.d/CentOS-*

    yum clean all


Upgrade sipXcom
~~~~~~~~~~~~~~~~

All sipXcom code changes are backwards compatible with 21.04, so just update your sipXcom RPMs with yum as follows:

  .. code-block:: bash

    service sipxecs stop

    yum update -y


Change active java version to 1.8:

  .. code-block:: bash

    alternatives --config java
    
    => Select java-1.8.0-openjdk.x86_64 

Reboot your system:

  .. code-block:: bash

    reboot

Your system should come back up after reboot as normal, but you may want to regenerate profiles from the server admin page.

