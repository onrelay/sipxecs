.. index:: maintenance

===========
Maintenance
===========

Sipxcom runs so well that it can be easy to become complacent on server maintenance. A common issue we see is lack of free disk space.

.. warning::
  If the server runs out of free disk space all services will halt!

If the server has ran out of free disk space, free up some space by deleting files and reboot. The system should recover upon service startup. 
Backup archives and logs usually consume the most space. Both can be safely deleted.

Disk Maintenance and Backup Integrity
-------------------------------------

  * All sipx logs are beneath /var/log/sipxpbx/. Mongo logs are beneath /var/log/mongodb/.
    Apache logs are beneath /var/log/httpd/. Postgresql logs are beneath /var/lib/pgsql/data/pg_log/.
    Rotated logs are suffixed with a date, or may end with a *.gz extension.
    Any rotated logs should be deleted periodically (monthly or yearly recommended) to conserve disk space, reduce snapshot size, etc.

  * If using scheduled backups make certain the "Number of backups to keep" option is not set to unlimited. Backups can be very large. That option should be set to a very conservative level (5 or less recommended).

  * Periodically verify that the scheduled backup archives are being created and saved correctly to a safe location.

Software updates
----------------

  * Run a 'yum update -y' on a daily, weekly, or monthly schedule to keep the OS patched with the latest security updates.
    This won't upgrade sipxcom packages unless you've changed the sipxcom repo file beneath /etc/yum.repos.d/ to point to a different version.

  * Check that your sipxcom version is the latest stable version at least once a year. The footer of the sipxcom webui should indicate what version it is.
    Always check the release notes of the new version for any critical notices prior to upgrading.

  * Check annually that your phones are running the latest GA firmware for the model. Phone bugs are mitigated by firmware upgrades.
    Polycom has two firmware pages available, one for `SoundPoint and SoundStation IP models <https://downloads.polycom.com/voice/voip/sip_sw_releases_matrix.html>`_ 
    and the other for `VVX models <https://downloads.polycom.com/voice/voip/uc_sw_releases_matrix.html>`_.
