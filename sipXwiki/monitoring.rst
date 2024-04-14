Monitoring
==========

There are several 3rd party options available to monitor the system, depending on your needs. **Do not run other configuration management agents such as Puppet or Chef on the server!** This is because `CFengine <https://cfengine.com/>`_ (sipxsupervisor service) is already in use. Other configuration management agents will likely interfere with CFengine/sipxsupervisor functioning correctly.

  * Sipxcom has built-in SNMP alarms for your convenience beneath Diagnostics - Alarms. Be sure to check those first.
  * The sipcodes.sh script can also be used to produce high level SIP statistics ad hoc. Given the proxy log is at INFO or DEBUG verbosity the sipcodes script will automatically collect and report statistics upon snapshot collection as ./var/log/sipxpbx/sipcodes.log.
  * The SIP proxy service has a option to save proxy statistics to a json file, /var/log/sipxpbx/proxy_stats.json.

  .. image:: system_services_proxy_stats.png
     :align: center

  * The /var/log/sipxpbx/proxy_stats.json file can be consumed by tools such as `Grafana <https://grafana.com/oss/grafana/>`_ , `ELK stacks <https://www.elastic.co/elastic-stack>`_, `Graylog <https://www.graylog.org/products/open-source>`_, `Splunk <https://www.splunk.com/>`_, etc to create current or historical graphs.
  * `Nagios <https://www.nagios.org/downloads/nagios-core/>`_ , `Munin <http://munin-monitoring.org/>`_, `Cacti <https://www.cacti.net/>`_ and many others can also be used to monitor service status, server health, etc.

Nagios
------

This section provides example configuration of a Nagios Core to monitor sipxcom services.

Prerequisites
~~~~~~~~~~~~~

You'll need a separate server running `Nagios Core <https://www.nagios.org/downloads/nagios-core/>`_, and administrative access to all the sipxcom servers. For this example I have compiled Nagios Core from source using the default settings rather than using a OS package. The path to files may vary if you have installed via rpm or apt. I will use the `Nagios Remote Plugin Executor (NRPE) <https://exchange.nagios.org/directory/Addons/Monitoring-Agents/NRPE--2D-Nagios-Remote-Plugin-Executor/details>`_ as a means to aggregate the checks, but there are alternatives available if NRPE doesn't suit your needs. Most of the checks used are available from the standard Nagios Plugins. If you want to expand on these there are many more available from the Nagios Exchange.

Overview
~~~~~~~~

If compiled from source using the defaults, Nagios Core will install to /usr/local/nagios:: 

  # tree --charset=ASCII -d nagios/
  nagios/
  |-- bin
  |-- etc
  |   `-- objects
  |-- libexec
  |-- sbin
  |-- share
  |   |-- contexthelp
  |   |-- docs
  |   |   `-- images
  |   |-- images
  |   |   `-- logos
  |   |-- includes
  |   |   `-- rss
  |   |       `-- extlib
  |   |-- js
  |   |-- media
  |   |-- ssi
  |   `-- stylesheets
  `-- var
      |-- archives
      |-- rw
      `-- spool
          `-- checkresults

The etc/objects directory is where your host configuration files are stored. I recommend organizing hosts into groups beneath the objects directory, then grouping similar services beneath that. For example, if you have a group of three sipxcom servers at example.org::

$ mkdir /usr/local/nagios/etc/objects/example.org
$ mkdir /usr/local/nagios/etc/objects/example.org/sipx
$ touch /usr/local/nagios/etc/objects/example.org/sipx/sipx1.cfg
$ touch /usr/local/nagios/etc/objects/example.org/sipx/sipx2.cfg
$ touch /usr/local/nagios/etc/objects/example.org/sipx/sipx3.cfg

By structuring in this way the system administrator can quickly understand who it belongs to and what it does. This is also especially helpful if you intend on running Nagios in a multi tenant fashion.

Preparing a host for monitoring
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Before stepping into the sipx1.cfg configuration on the Nagios server we’ll need to prepare the sipxcom server(s) for our checks. Nagios Remote Plugin Executor (NRPE) works as an aggregate point for multiple check scripts.

  .. image:: nagios_nrpe.png
     :align: center

You'll need to download and install both NRPE and the standard Nagios plugins on each host you intend on monitoring. After installing these you may wish to pause for a moment and review the check scripts now available under /usr/local/nagios/libexec. The NRPE configuration, /usr/local/nagios/etc/nrpe.cfg, was likely copied from the sample provided within the NRPE tarball. You should review this file for any environmental changes you may need to make such as partition locations:: 

  command[check_hda1]=/usr/local/nagios/libexec/check_disk -w 20% -c 10% -p /dev/hda1
  command[check_zombie_procs]=/usr/local/nagios/libexec/check_procs -w 5 -c 10 -s Z
  command[check_total_procs]=/usr/local/nagios/libexec/check_procs -w 150 -c 200

The commands defined should match what is being called within the host configuration file. For example, checks for sipx1.example.org are defined on the nagios server in /usr/local/nagios/etc/objects/example.org/sipx/sipx1.cfg:: 

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check Users
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_users
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check Swap
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_swap
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check Load
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_load
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check Boot Partition
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_boot
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check Root Partition
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_root
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check Zombie Processes
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_zombie_procs
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check Total Processes
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_total_procs
          }

 
The check_command line is essentially "connect with NRPE and run xxx". Be sure that xxx is a defined within /usr/local/nagios/etc/nrpe.cfg of the host you are checking against. For things you want to execute from the Nagios server, make certain that you've defined those commands in the Nagios server /usr/local/nagios/etc/objects/commands.cfg. For example I defined the SSL certificate check on my Nagios server command.cfg::

  define command {
          command_name    check_ssl_certificate
          command_line    $USER1$/check_ssl_certificate -H $HOSTADDRESS$ -c 3 -w 7
         }

But in /usr/local/nagios/etc/objects/example.org/sipx1.cfg, this is defined without the check_nrpe prefix so it will execute from the Nagios server rather than on the sipx1.example.org host::

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             SSL Certificate Expiration
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_ssl_certificate
          }

Sipxcom services
~~~~~~~~~~~~~~~~

Below are additional examples for sipx1.example.org that pertain to sipXcom/sipx services. These would be defined in /usr/local/nagios/etc/objects/example.org/sipx/sipx1.cfg on our Nagios server::

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             NTP
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_ntp_time!0.5!1
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check SIP Registration
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_sip_registration
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             SSH
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_ssh!-p 22
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             TFTP
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_tftp
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             FTP
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_ftp!-H sipx1.example.org -p 21
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check sipx Web UI
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_ui
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check XMPP
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_jabber
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             TCP SIP SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_tcp_sip_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             UDP SIP SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_udp_sip_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             TCP SIPS SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_tcp_sips_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             SIP TLS SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_sip_tls_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             SIP RR SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_sip_rr_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             SIP MWI SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_sip_mwi_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             XMPP client SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_xmpp_client_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             XMPP server SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_xmpp_server_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             XMPP conference server SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_xmpp_conf_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             TCP Voicemail SRV
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_tcp_vm_srv
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check SIPXCONFIG
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_sipxconfig
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check SIPXCDR
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_sipxcdr
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             Check MySQL homer.db
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_homer
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             MongoDB Connection Check
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_mongo_connect
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             MongoDB Long running ops
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_mongo_lag
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             MongoDB Operations Count
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_nrpe!check_mongo_ops
          }

  define service{
          use                             generic-service
          host_name                       sipx1.example.org
          service_description             SSL Certificate Expiration
          contact_groups                  admins
          notifications_enabled           1
          check_command                   check_ssl_certificate
          }

The command definitions for all commands prefixed with check_nrpe should be defined on sipx1.example.org within /usr/local/nagios/etc/nrpe.cfg, for example:: 

  # system checks
  command[check_users]=/usr/local/nagios/libexec/check_users -w 5 -c 10
  command[check_load]=/usr/local/nagios/libexec/check_load -w 15,10,5 -c 30,25,20
  command[check_root]=/usr/local/nagios/libexec/check_disk -w 20% -c 10% -p /dev/mapper/vg_root-lv_root
  command[check_boot]=/usr/local/nagios/libexec/check_disk -w 20% -c 10% -p /dev/vda1
  command[check_zombie_procs]=/usr/local/nagios/libexec/check_procs -w 5 -c 10 -s Z
  command[check_total_procs]=/usr/local/nagios/libexec/check_procs -w 200 -c 250
  command[check_swap]=/usr/local/nagios/libexec/check_swap -w 80% -c 50%
  command[check_memory]=/usr/local/nagios/libexec/check_memory.pl

  # sipx service checks
  command[check_sipxconfig]=/usr/local/nagios/libexec/check_postgres.pl -db SIPXCONFIG --action connection
  command[check_sipxcdr]=/usr/local/nagios/libexec/check_postgres.pl -db SIPXCDR --action connection
  command[check_ui]=/usr/local/nagios/libexec/check_http -w5 -c 10 --ssl -H sipx1.example.org -u /sipxconfig/app
  command[check_sip_registration]=/usr/local/nagios/libexec/check_registrations.sh
  command[check_ntp_time]=/usr/local/nagios/libexec/check_ntp_time -H sipx1.example.org -w 0.5 -c 1
  command[check_mongo_connect]=/usr/bin/python /usr/local/nagios/libexec/check_mongo -H sipx1.example.org -A connect
  command[check_mongo_ops]=/usr/bin/python /usr/local/nagios/libexec/check_mongo -H sipx1.example.org -A count
  command[check_mongo_lag]=/usr/bin/python /usr/local/nagios/libexec/check_mongo -H sipx1.example.org -A long

  # dns checks
  command[check_tcp_sip_srv]=/usr/local/nagios/libexec/check_dns -H _sip._tcp.example.org -s 127.0.0.1 -q SRV
  command[check_udp_sip_srv]=/usr/local/nagios/libexec/check_dns -H _sip._udp.example.org -s 127.0.0.1 -q SRV
  command[check_tcp_sips_srv]=/usr/local/nagios/libexec/check_dns -H _sips._tcp.example.org -s 127.0.0.1 -q SRV
  command[check_sip_tls_srv]=/usr/local/nagios/libexec/check_dns -H _sip._tls.example.org -s 127.0.0.1 -q SRV
  command[check_sip_mwi_srv]=/usr/local/nagios/libexec/check_dns -H _sip._tcp.mwi.example.org -s 127.0.0.1 -q SRV
  command[check_sip_rr_srv]=/usr/local/nagios/libexec/check_dns -H _sip._tcp.rr.example.org -s 127.0.0.1 -q SRV
  command[check_tcp_vm_srv]=/usr/local/nagios/libexec/check_dns -H _sip._tcp.vm.example.org -s 127.0.0.1 -q SRV
  command[check_xmpp_server_srv]=/usr/local/nagios/libexec/check_dns -H _xmpp-server._tcp.example.org -s 127.0.0.1 -q SRV
  command[check_xmpp_client_srv]=/usr/local/nagios/libexec/check_dns -H _xmpp-client._tcp.example.org -s 127.0.0.1 -q SRV
  command[check_xmpp_conf_srv]=/usr/local/nagios/libexec/check_dns -H _xmpp-server._tcp.conference.example.org -s 127.0.0.1 -q SRV

As there are checks that are executed server side, those need to be defined in /usr/local/nagios/etc/objects/commands.cfg on the Nagios server::

  define command{
  command_name check_tftp
  command_line $USER1$/check_tftp --get $HOSTADDRESS$ 000000000000.cfg 7167
  }

  define command{
  command_name check_jabber
  command_line $USER1$/check_jabber -H $HOSTADDRESS$ --expect='xmlns="jabber:client" from="example.org"'
  }

  define command {
  command_name check_ssl_certificate
  command_line $USER1$/check_ssl_certificate -H $HOSTADDRESS$ -c 3 -w 7
  }

3rd Party Checks
~~~~~~~~~~~~~~~~
`check_jabber <https://exchange.nagios.org/directory/Plugins/Instant-Messaging/check_jabber_login/details>`_ is used for XMPP checks.
`check_mongo <https://github.com/mzupan/nagios-plugin-mongodb>`_ is used for MongoDB checks.
`check_postgres <https://exchange.nagios.org/directory/Plugins/Databases/PostgresQL/check_postgres/details>`_ is used for PostgreSQL checks.
For check_sip_registration I created a shell script that utilizes sipx-dbutil. 
 
Additional Notes
~~~~~~~~~~~~~~~~

  * You may find some checks complain of missing utils.pm. If you do, check if the script is making any references to the nagios plugins directory. You may need to alter the path to /usr/local/nagios/libexec/.
  * Be sure to inspect any firewalls between your Nagios server and the sipXcom/sipx servers prior to running your checks. Some services such as ssh are restrictive by default in the sipXcom/sipx firewall.
  * It is possible to utilize sipsak to test against the SIP stack, however **be aware that by default sipxcom SIP security feature will will ban the source IP address of client using default sipsak User Agent string.**
  * Try not to cause unnecessary stress or bandwidth consumption on the server with your service checks. Once a day is probably good enough for a check interval for some services such as the SSL certificate check. See the "External Command Check Interval" section here : http://nagios.sourceforge.net/docs/3_0/configmain.html.

Graylog
-------

`Graylog <https://graylog.org/products/open-source/>`_ is open source log management/aggregation software. A fully supported Commercial/Enterprise version also exists. For this example I am using the open source version on a Debian 10 server.

Installation on Debian 10
~~~~~~~~~~~~~~~~~~~~~~~~~

Starting from a fresh Debian 10 minimal installation::

  apt-get update && apt-get upgrade -y
  apt-get install apt-transport-https openjdk-11-jre-headless uuid-runtime pwgen dirmngr curl
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 4B7C549A058F8B6B
  echo "deb http://repo.mongodb.org/apt/debian buster/mongodb-org/4.2 main" | tee /etc/apt/sources.list.d/mongodb-org-4.2.list
  apt-get update && apt-get install mongodb-org -y
  systemctl daemon-reload
  systemctl enable mongod.service
  systemctl restart mongod.service
  wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -
  echo "deb https://artifacts.elastic.co/packages/oss-6.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-6.x.list
  apt-get update && apt-get install elasticsearch-oss -y
  echo "cluster.name: graylog" >> /etc/elasticsearch/elasticsearch.yml
  echo "action.auto_create_index: false" >> /etc/elasticsearch/elasticsearch.yml
  systemctl daemon-reload
  systemctl enable elasticsearch.service
  systemctl restart elasticsearch.service
  wget https://packages.graylog2.org/repo/packages/graylog-3.1-repository_latest.deb
  dpkg -i graylog-3.1-repository_latest.deb
  apt-get update && apt-get install graylog-server -y
  
For admin password as password and hash edit /etc/graylog/server/server.conf and set::

  echo "password_secret = naln41C22HRxw3hy9mJ8bipFWBo1aewKFgtXDXp22dNjNJNqEtid6uC0476zIfX5iQ3mZuRp9y7h3XcNY63inPo6vJy7FuLP"
  echo "root_password_sha2 = 5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
  echo "http_bind_address = 192.168.1.114:9000"
  echo "http_publish_uri = http://192.168.1.114:9000"
  systemctl enable graylog-server.service
  systemctl start graylog-server.service

The Graylog webui should now be up on http://192.168.1.114:9000. Create a GELF UDP input using the default port 12201.

Fluentd on Graylog server
~~~~~~~~~~~~~~~~~~~~~~~~~
Fluent Bit is an open source and multi-platform Log Processor and Forwarder which allows you to collect data/logs from different sources, unify and send them to multiple destinations. It's fully compatible with Docker and Kubernetes environments. Fluent Bit is written in C and has a pluggable architecture supporting around 30 extensions.

For this example fluentd is running on the Graylog server. It is used to convert data received into the Graylog GELF format. ::

  # fluentd on graylog
  apt-get install sudo ntp ntpdate ntpstat ruby-gelf
  curl -L https://toolbelt.treasuredata.com/sh/install-debian-buster-td-agent3.sh
  systemctl daemon-reload
  systemctl enable td-agent
  td-agent-gem install gelf
  cd /etc/td-agent/plugin
  wget https://raw.githubusercontent.com/emsearcy/fluent-plugin-gelf/master/lib/fluent/plugin/out_gelf.rb
  cd ../

Append to /etc/td-agent/td-agent.conf::

  <source>
      type syslog
      tag hostname_goes_here
  </source>
  <match *.*>
      type copy
      <store>
          type gelf
          host 0.0.0.0
          port 12201
          flush_interval 5s
      </store>
      <store>
          type stdout
      </store>
  </match>

Restart the service and configure the service to start at boot with::

  systemctl restart td-agent
  systemctl enable td-agent

Fluentbit on the sipxcom server
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

For this example I am using fluentbit on the sipxcom server to ship logs to the fluentd instance of the Graylog server:: 

  # fluentbit on sipx/uniteme centos7
  cd /etc/yum.repos.d/
  nano fluentbit.repo

Inside fluentbit.repo::

  [fluentbit]
  name = fluentbit
  baseurl = http://packages.fluentbit.io/centos/7
  gpgcheck=1
  gpgkey=http://packages.fluentbit.io/fluentbit.key
  enabled=1

Next update the packages::

  yum update
  yum install td-agent-bit -y
  mv /etc/td-agent-bit/td-agent-bit.conf ~/td-agent-bit.conf.orig
  nano /etc/td-agent-bit/td-agent-bit.conf

Inside td-agent-bit.conf::

  [INPUT]
      Name cpu
      Tag  cpu.local
      Interval_Sec 1

  [INPUT]
      Name mem
      Tag memory

  [INPUT]
      Name disk
      Tag disk.local
      Interval_Sec 1

  [INPUT]
      Name netif
      Tag netif.eth0
      Interval_Sec 1
      Interface eth0

  [INPUT]
      Name health
      Tag health.proxy
      Host 192.168.2.14
      Port 5060
      Interval_Sec 60
      Alert true
      Add_Host true
      Add_Port true

  [INPUT]
      Name health
      Tag health.registrar
      Host 192.168.2.14
      Port 5070
      Interval_Sec 60
      Alert true
      Add_Host true
      Add_Port true

  [INPUT]
      Name health
      Tag health.bridge
      Host 192.168.2.14
      Port 5090
      Interval_Sec 60
      Alert true
      Add_Host true
      Add_Port true

  [INPUT]
      Name health
      Tag health.mongo
      Host 127.0.0.1
      Port 27017
      Interval_Sec 60
      Alert true
      Add_Host true
      Add_Port true

  [INPUT]
      Name health
      Tag health.pgsql
      Host 127.0.0.1
      Port 5432
      Interval_Sec 60
      Alert true
      Add_Host true
      Add_Port true

  [INPUT]
      Name health
      Tag health.dns
      Host 127.0.0.1
      Port 53
      Interval_Sec 60
      Alert true
      Add_Host true
      Add_Port true

  [INPUT]
      Name tail
      Path /var/log/sipxpbx/proxy_stats.json
      Refresh_Interval 1
      Parser json

  [OUTPUT]
      Name  forward
      Match *
      Host 192.168.1.114
      Port 24224

And finally restart the service::

  service td-agent-bit restart

You should now see Graylog reporting activity on the GELF input.
