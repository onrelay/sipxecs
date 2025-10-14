Summary: A systems administration tool for networks
Name: cfengine
Version: 3.3.8
Release: 1%{?dist}
License: GPLv3
Group: Applications/System
Source0: %{name}-%{version}.tar.gz
Source1: cf-execd
Source2: cf-serverd
Source3: cf-monitord
URL: http://www.cfengine.org/
BuildRequires: db4-devel,openssl-devel,bison,flex,m4,libacl-devel
BuildRequires: libselinux-devel,tetex-dvips,texinfo-tex,pcre-devel
BuildRequires: tokyocabinet-devel
Requires(post): /sbin/install-info
Requires(preun): /sbin/install-info, /sbin/service
Requires(postun): /sbin/service
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root

%description
Cfengine, or the configuration engine is an agent/software robot and a
very high level language for building expert systems to administrate
and configure large computer networks. Cfengine uses the idea of
classes and a primitive form of intelligence to define and automate
the configuration and maintenance of system state, for small to huge
configurations. Cfengine is designed to be a part of a computer immune
system.

%package doc
Summary: Documentation for cfengine
Group: Documentation
Requires: %{name} = %{version}-%{release}
#BuildArch: noarch

%description doc
This package contains the documentation for cfengine.


%prep
%setup -q


%build
%configure BERKELEY_DB_LIB=-ldb \
    --enable-selinux \
    --enable-fhs
make


%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_sbindir}
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}
make DESTDIR=$RPM_BUILD_ROOT install

# It's ugly, but thats the way Mark wants to have it. :(
# If we don't create this link, cfexecd will not be able to start
# (hardcoded) /var/sbin/cf-agent in scheduled intervals. Other option 
# would be to patch cfengine to use %{_sbindir}/cf-agent
# but upstream won't support this
mkdir -p $RPM_BUILD_ROOT/%{_var}/%{name}/bin
ln -sf %{_sbindir}/cf-agent $RPM_BUILD_ROOT/%{_var}/%{name}/bin/
ln -sf %{_sbindir}/cf-promises $RPM_BUILD_ROOT/%{_var}/%{name}/bin/

# init scripts
mkdir -p $RPM_BUILD_ROOT%{_initrddir}
for i in %{SOURCE1} %{SOURCE2} %{SOURCE3}
do
	install -p -m 0755 $i $RPM_BUILD_ROOT%{_initrddir}/
done

rm -f $RPM_BUILD_ROOT%{_infodir}/dir

# All this stuff is pushed into doc/contrib directories
rm -rf $RPM_BUILD_ROOT%{_datadir}/%{name} 
rm -f $RPM_BUILD_ROOT%{_sbindir}/cfdoc


%post
# cfagent won't run nicely, unless your host has keys.
if [ ! -d /mnt/sysimage -a ! -f %{_var}/%{name}/ppkeys/localhost.priv ]; then
	%{_sbindir}/cf-key >/dev/null || :
fi
/sbin/install-info --info-dir=%{_infodir} %{_infodir}/cfengine*.info* 2> /dev/null || :
# add init files to systemctl
if [ "$1" = "1" ]; then
	systemctl enable cf-monitord
	systemctl enable cf-execd
	systemctl enable cf-serverd
fi


%preun
if [ "$1" = "0" ]; then
    /sbin/install-info --delete --info-dir=%{_infodir} %{_infodir}/cfengine*.info* 2> /dev/null || :
    /sbin/service cf-monitord stop >/dev/null 2>&1 || :
    /sbin/service cf-execd stop >/dev/null 2>&1 || :
    /sbin/service cf-serverd stop >/dev/null 2>&1 || :
	systemctl disable cf-monitord
	systemctl disable --del cf-execd
	systemctl disable --del cf-serverd
fi

%postun
if [ $1 -ge 1 ]; then
    /sbin/service cf-monitord condrestart >/dev/null 2>&1 || :
    /sbin/service cf-execd condrestart >/dev/null 2>&1 || :
    /sbin/service cf-serverd condrestart >/dev/null 2>&1 || :
fi


%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root,-)
%doc AUTHORS ChangeLog README
%{_sbindir}/*
/usr/libexec/cfengine/libpromises*
%{_mandir}/man8/*
%{_initrddir}/cf-monitord
%{_initrddir}/cf-execd
%{_initrddir}/cf-serverd
%{_var}/%{name}

%files doc
%defattr(-,root,root,-)
%doc %{_datadir}/doc/cfengine/README
%doc %{_datadir}/doc/cfengine/ChangeLog
%doc %{_datadir}/doc/cfengine/example_config/*.cf
%doc %{_datadir}/doc/cfengine/examples/*.cf
%doc %{_datadir}/doc/cfengine-%{version}/AUTHORS
%doc %{_datadir}/doc/cfengine-%{version}/ChangeLog
%doc %{_datadir}/doc/cfengine-%{version}/README

