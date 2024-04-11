#!/bin/bash

SIPX_USER=sipx

DEFAULT_PLATFORM=centos-7

DEFAULT_ARCHITECTURE=x86_64

DEFAULT_SUBPROJECT=sipx

DEFAULT_VERSION=24.01

SIPXCOM_USER_GROUP=sipxcom-dev@googlegroups.com

THIS_MASTER_BUILD_SCRIPT=$(readlink -f ${BASH_SOURCE[0]})

SOURCE_DIR=$(dirname ${THIS_MASTER_BUILD_SCRIPT})

BUILD_DIR=${SOURCE_DIR}/build

function checkUser()
{
    echo "Begin checkUser(): SUDO_USER=${SUDO_USER}, USER=${USER}, SIPX_USER=${SIPX_USER}"

    if [ "${SUDO_USER}" != "${SIPX_USER}" ] || [ "${USER}" != "root" ]; then
        echo "
        Error: You must run this script with sudo root privileges as user ${SIPX_USER}. 
        
        Please setup your instance as follows:

        yum update -y
        
        useradd -m ${SIPX_USER}

        passwd ${SIPX_USER}

        visudo (append):
            # add ${SIPX_USER} as sudo user
            ${SIPX_USER}    ALL=(ALL)       NOPASSWD:ALL

        yum install -y sudo

        su ${SIPX_USER}

        sudo ./${THIS_MASTER_BUILD_SCRIPT} [options]

        "
        exit 1;
    fi

    echo "Done checkUser()"

}

function checkInput()
{
    echo "Begin checkInput(): options=$1"

    for i in "$1"
    do
        case $i in
            -p=*|--platform=*)
            PLATFORM="${i#*=}"
            shift
            ;;
            -a=*|--architecture=*)
            ARCHITECTURE="${i#*=}"
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
            -r|--rpm)
            RPM=".rpm"
            shift
            ;;
            -h|--help)
                echo "

            Usage:
                -p|--platform: platform of sipxcom RPM to build, e.g. ${DEFAULT_PLATFORM} (default), rocky-9
                -a|--architecture: architecture of sipxcom RPM to build, e.g. ${DEFAULT_ARCHITECTURE} (default)
                -s|--subproject: subproject to build or sipx for building all RPMs, e.g. ${DEFAULT_SUBPROJECT} (default) sipXconfig
                -v|--version: version to build, e.g ${DEFAULT_VERSION} (default), 24.07
                -r|--rpm: include if building rpms

            Sample:
                sudo master-build.sh --platform=centos-7 --subproject=sipXconfig --version=24.01
                sudo master-build.sh -p=rocky-9 -a=x86_64 -s=sipXconfig -v=24.07
                "
                exit
            shift
            ;;
            *)
        esac
    done

    if [ -z "${PLATFORM}" ]
    then
        PLATFORM=${DEFAULT_PLATFORM}
    else
        case $PLATFORM in

            "centos-7" | "rocky-9")
                ;;

            *)
                echo "Error: Unknown platform ${PLATFORM}"
                exit 1
                ;;
        esac
    fi

    if [ -z "${ARCHITECTURE}" ]
    then
        ARCHITECTURE=${DEFAULT_ARCHITECTURE}
    else
        case $ARCHITECTURE in

        "x86_64")
            ;;

        *)
            echo "Error: Unknown architecture ${ARCHITECTURE}"
            exit 1
            ;;
        esac
    fi

    if [ -z "${SUBPROJECT}" ]
    then
        SUBPROJECT=${DEFAULT_SUBPROJECT}
    fi

    if [ -z "${VERSION}" ]
    then
        VERSION=${DEFAULT_VERSION}
    else
        case $VERSION in

        "24.01")
            ;;

        *)
            echo "Error: Unknown version ${VERSION}"
            exit 1
            ;;
        esac
    fi

    echo "Done checkInput(): PLATFORM=${PLATFORM}, ARCHITECTURE=${ARCHITECTURE}, SUBPROJECT=${SUBPROJECT}, VERSION=${VERSION}, RPM=${RPM}"
}

function init()
{
    echo "Begin init()"

    sudo mkdir -p ${BUILD_DIR}

    sudo chown ${SIPX_USER}.${SIPX_USER} ${BUILD_DIR}

    echo "Done init()"
}

function setupRepos() 
{
    echo "Begin setupRepos()"

    sudo wget -O /etc/yum.repos.d/artifact-registry-plugin.repo https://storage.googleapis.com/sipxecs/artifact-registry/artifact-registry-plugin.repo

    sudo yum install -y yum-plugin-artifact-registry 

    wget -O /etc/yum.repos.d/sipxcom.repo https://storage.googleapis.com/sipxecs/sipxcom/${VERSION}/${PLATFORM}-${ARCHITECTURE}/sipxcom.repo

    echo "Done setupRepos()"
}

function setupCentOS7() 
{
    echo "Begin setupCentOS7()"

    sudo yum install -y git make autoconf automake libtool gcc-c++ openssl-devel libmcrypt-devel libtool-ltdl-devel \
        pcre-devel pcre-devel findutils  db4-devel iptables iproute boost-devel libpcap-devel libdnet-devel xmlrpc-c-devel \
        libevent-devel poco-devel libconfig-devel hiredis-devel gtest-devel leveldb-devel cppunit-devel gperftools-devel \
        c-ares-devel libdb4-cxx-devel libdb-cxx-devel popt-devel xerces-c-devel zeromq-devel v8-devel httpd unixODBC-devel \
        mock rsync gem wget systemd-sysv mongo-cxx-driver-devel ev-devel libuuid-devel swig cfengine openfire reciprocate-libs \
        createrepo unzip java-1.8.0-openjdk-devel

    cd ${SOURCE_DIR}
    sudo mkdir -p /usr/local/sipx
    sudo chown sipx.sipx /usr/local/sipx

    echo "Using legacy oss_core for CentOS7"
    sudo yum install -y oss_core oss_core-devel oss_core-debuginfo
    sudo mkdir -p /usr/local/sipx/lib
    sudo ln -s /usr/lib64/liboss_core.la /usr/local/sipx/lib/liboss_core.la
    sudo ln -s /usr/lib64/liboss_carp.la /usr/local/sipx/lib/liboss_carp.la
    sudo mkdir -p /usr/local/sipx/opt
    sudo ln -s /usr/opt/ossapp /usr/local/sipx/opt/ossapp

    cd ${BUILD_DIR}
    sudo echo oss_core >> .modules-exclude

    echo "Done setupCentOS7()"
}

function setupRocky9() 
{
    echo "Begin setupRocky9()"

    echo "Done setupRocky9()"
}

function configure() 
{
    echo "Begin configure()"

    sed -i "/^AC_INIT.*/ s//AC_INIT(sipX, ${VERSION}, ${SIPXCOM_USER_GROUP})/" ${SOURCE_DIR}/configure.ac

    cd ${SOURCE_DIR}
    sudo autoreconf -ivf

    cd ${BUILD_DIR}

    if [ -z "${RPM}" ]; then
        sudo ../configure
    else
        sudo yum install -y createrepo rpm-build mock
        sudo wget http://li.nux.ro/download/nux/misc/el7/x86_64/thttpd-2.25b-33.el7.nux.x86_64.rpm
        sudo rpm -ivh thttpd-2.25b-33.el7.nux.x86_64.rpm
        rm -f thttpd-2.25b-33.el7.nux.x86_64.rpm
        sudo usermod -a -G mock sipx

        sudo echo '$(sipx_all)' > .modules-include
        
        sudo ../configure --enable-rpm DISTRO="${PLATFORM}-${ARCHITECTURE}"
    fi

    echo "Done configure()"
}


function build() 
{
    echo "Begin build()"

    cd ${BUILD_DIR}
    sudo make ${SUBPROJECT}${RPM}
 
    # The make command may kick a permission error when creating RPMs
    if [ ! -z "${RPM}" ]; then
        if [ $? -ne 0 ]; then 
            echo "Retrying make with new repo permissions"
            sudo chown -R sipx.sipx repo
            sudo make ${SUBPROJECT}${RPM}
        fi
    fi

    echo "Done build()"
}

function main()
{

    echo "Starting ${THIS_MASTER_BUILD_SCRIPT}"

    checkUser

    checkInput $1

    init

    setupRepos


    case $PLATFORM in

        "centos-7")
            setupCentOS7
            ;;

        "rocky-9")
            setupRocky9
            ;;

        *)
            echo "Error: Unknown platform ${PLATFORM}"
            exit 1
            ;;
    esac
    
    configure

    build

    echo "Finished ${THIS_MASTER_BUILD_SCRIPT}"
}

main $@

