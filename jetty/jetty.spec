
Name:           jetty
Version:        12.1.4
Release:        1%{?dist}
Summary:        Jetty Home directory with module support for Jetty 12
Group:          Development/Tools
License:        EPL-2.0
URL:            https://eclipse.dev/jetty/
Source0:        %{name}-%{version}.tar.gz

Requires:       java-17-openjdk
BuildRequires:  tar

# Disable debug info packages
%define debug_package %{nil}

%description
This package contains the Jetty Home directory, which includes the core server and module infrastructure for Jetty 12. 
It is intended to be used in conjunction with a Jetty Base directory to configure and run Jetty instances.

%prep
%setup -q -n jetty-home-%{version}

%build
# No build required — we just repack the tarball

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/usr/share/jetty
mkdir -p %{buildroot}/usr/share/java
cp -a * %{buildroot}/usr/share/jetty/
ln -s ../jetty/lib %{buildroot}/usr/share/java/jetty

%files
%attr(644,root,root) /usr/share/jetty/*
%attr(-,root,root) /usr/share/java/jetty
