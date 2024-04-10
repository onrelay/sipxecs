#!/bin/bash

for i in "$@"
do
case $i in
    -p=*|--platform=*)
    PLATFORM="${i#*=}"
    shift
    ;;
    -s=*|--subproject=*)
    SUBPROJECT="${i#*=}"
    shift
    ;;
    -v=*|--version=*)
    VERSION="${i#*=}"
    shift
    ;;
    -h|--help)
        echo "
    Usage:
        -p|--platform: platform of sipxcom RPM to build, e.g. centos7, rocket9
        -s|--subproject: subproject to build (or sipx for building all RPMs) or init for the first run, e.g. init, sipXconfig, sipx
        -v|--version: subproject to build, e.g 24.01

    Sample:
        ./master-build.sh --platform=centos7 --subproject=sipXconfig --version=24.01
        ./master-build.sh -p=rocket9 -s=sipXconfig -v=24.07
        "
	    exit
    shift
    ;;
    *)
esac
done

THIS_MASTER_BUILD_SCRIPT=$(readlink -f ${BASH_SOURCE[0]})
SOURCE_DIR=$(dirname ${THIS_MASTER_BUILD_SCRIPT})
BUILD_DIR=${SOURCE_DIR}/build

if [ -z "${PLATFORM}" ]
then
PLATFORM="centos7"
fi

if [ -z "${SUBPROJECT}" ]
then
SUBPROJECT="sipx"
fi

if [ -z "${VERSION}" ]
then
VERSION="24.01"
fi

yum update -y

setupRepos

function setupRepos() 
{
    ARTIFACT_REGISTRY_REPO="/etc/yum.repos.d/artifact-registry-plugin.repo"
    echo "[ar-plugin]" >> ${ARTIFACT_REGISTRY_REPO}
    echo "name=Artifact Registry Plugin" >> ${ARTIFACT_REGISTRY_REPO}
    echo "baseurl=https://packages.cloud.google.com/yum/repos/yum-plugin-artifact-registry-stable" >> ${ARTIFACT_REGISTRY_REPO}
    echo "enabled=1" >> ${ARTIFACT_REGISTRY_REPO}
    echo "gpgcheck=1" >> ${ARTIFACT_REGISTRY_REPO}
    echo "repo_gpgcheck=0" >> ${ARTIFACT_REGISTRY_REPO}
    echo "gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg" >> ${ARTIFACT_REGISTRY_REPO}
    echo "   https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg" >> ${ARTIFACT_REGISTRY_REPO} 

    yum install -y yum-plugin-artifact-registry 
    
    SIPXCOM_REPO=/etc/yum.repos.d/sipxcom.repo
    echo "[sipxcom]" >> ${SIPXCOM_REPO}
    echo "name=sipXcom" >> ${SIPXCOM_REPO}
    echo "enabled=1" >> ${SIPXCOM_REPO}
    echo "baseurl=https://us-central1-yum.pkg.dev/projects/sipxecs/sipxcom-${VERSION}-${PLATFORM}" >> ${SIPXCOM_REPO}
    echo "repo_gpgcheck=0" >> ${SIPXCOM_REPO}
    echo "gpgcheck=0" >> ${SIPXCOM_REPO}
}

function addDependencies() 
{
    yum install -y sudo git make autoconf automake libtool gcc-c++ openssl-devel libmcrypt-devel libtool-ltdl-devel \
        pcre-devel pcre-devel findutils  db4-devel iptables iproute boost-devel libpcap-devel libdnet-devel xmlrpc-c-devel \
        libevent-devel poco-devel libconfig-devel hiredis-devel gtest-devel leveldb-devel cppunit-devel gperftools-devel \
        c-ares-devel libdb4-cxx-devel libdb-cxx-devel popt-devel xerces-c-devel zeromq-devel v8-devel httpd unixODBC-devel \
        mock rsync gem wget systemd-sysv mongo-cxx-driver-devel ev-devel libuuid-devel swig cfengine openfire reciprocate-libs \
        createrepo unzip java-1.8.0-openjdk-devel

    cd /opt
    wget https://storage.googleapis.com/dart-archive/channels/stable/release/1.15.0/sdk/dartsdk-linux-x64-release.zip
    unzip dartsdk-linux-x64-release.zip
    rm -f dartsdk-linux-x64-release.zip
}


