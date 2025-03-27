%global pkg_name mongo-cxx-driver

Name:           mongo-cxx-driver
Version:        4.0.0
Release:        1%{?dist}
Summary:        The MongoDB C++ driver library and its include files
Group:          Development/Libraries
License:        Apache 2.0
URL:            https://github.com/mongodb/mongo-cxx-driver
Source0:        https://github.com/mongodb/mongo-cxx-driver/releases/download/r%{version}/mongo-cxx-driver-r%{version}.tar.gz

# do not build a debuginfo package
%define debug_package %{nil}

BuildRequires: cmake
BuildRequires: gcc-c++
BuildRequires: openssl-devel

%description
This package provides the shared library for the MongoDB C++ driver.

%package -n %{pkg_name}-devel
Summary:        MongoDB C++ driver header files
Group:          Development/Libraries
Requires:       %{name}%{?_isa} = %{version}-%{release}

Provides: libmongodb-devel = %{version}-%{release}
Obsoletes: libmongodb-devel

%description -n %{pkg_name}-devel
This package provides the header files for the MongoDB C++ driver.

%prep
%setup -D -n mongo-cxx-driver-r%{version}
# rm -rf %{buildroot}%{_libdir}/libmongocxx*
# rm -rf %{buildroot}%{_libdir}/libbsoncxx*
# rm -rf %{buildroot}%{_libdir}/cmake/mongocxx-*
# rm -rf %{buildroot}%{_libdir}/cmake/bsoncxx-*
# rm -rf %{buildroot}%{_includedir}/mongocxx
# rm -rf %{buildroot}%{_includedir}/bsoncxx
# rm -rf %{buildroot}%{_libdir}/pkgconfig/libmongocxx.pc
# rm -rf %{buildroot}%{_libdir}/pkgconfig/libbsoncxx.pc

%build
cmake -S . -B build \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_CXX_STANDARD=17 \
    -DBUILD_SHARED_LIBS=ON \
    -DCMAKE_INSTALL_PREFIX=%{_prefix} \
    -DCMAKE_PREFIX_PATH=%{_prefix} \
    -DNEED_DOWNLOAD_C_DRIVER=FALSE \
    -DCMAKE_INSTALL_LIBDIR=%{_libdir}
    
cmake --build build -- %{_smp_mflags}

%install
rm -rf %{buildroot}

# Create necessary directories
mkdir -p %{buildroot}%{_libdir}
mkdir -p %{buildroot}%{_includedir}/bsoncxx/config
mkdir -p %{buildroot}%{_includedir}/mongocxx/config
mkdir -p %{buildroot}%{_libdir}/cmake/bsoncxx
mkdir -p %{buildroot}%{_libdir}/cmake/mongocxx
mkdir -p %{buildroot}%{_libdir}/pkgconfig

# Copy shared libraries
cp -p build/src/mongocxx/libmongocxx.so* %{buildroot}%{_libdir}/
cp -p build/src/bsoncxx/libbsoncxx.so* %{buildroot}%{_libdir}/

# Copy header files
cp -pr src/bsoncxx/include/bsoncxx/*.hpp %{buildroot}%{_includedir}/bsoncxx/
cp -pr src/bsoncxx/include/bsoncxx/v_noabi/bsoncxx/* %{buildroot}%{_includedir}/bsoncxx/
cp -pr src/mongocxx/include/mongocxx/v_noabi/mongocxx/* %{buildroot}%{_includedir}/mongocxx/
cp -pr build/src/bsoncxx/lib/bsoncxx/v_noabi/bsoncxx/config/*.hpp %{buildroot}%{_includedir}/bsoncxx/config
cp -pr build/src/mongocxx/lib/mongocxx/v_noabi/mongocxx/config/*.hpp %{buildroot}%{_includedir}/mongocxx/config

# Copy CMake configuration files
cp -pr build/src/bsoncxx/*.cmake %{buildroot}%{_libdir}/cmake/bsoncxx/
cp -pr build/src/mongocxx/*.cmake %{buildroot}%{_libdir}/cmake/mongocxx/

# Copy pkgconfig files
cp -p build/src/bsoncxx/cmake/libbsoncxx.pc %{buildroot}%{_libdir}/pkgconfig/
cp -p build/src/mongocxx/cmake/libmongocxx.pc %{buildroot}%{_libdir}/pkgconfig/

%files -n %{pkg_name}
%{_libdir}/libmongocxx.so
%{_libdir}/libmongocxx.so.%{version}
%{_libdir}/libmongocxx.so._noabi
%{_libdir}/libbsoncxx.so
%{_libdir}/libbsoncxx.so.%{version}
%{_libdir}/libbsoncxx.so._noabi
# %doc %{_datadir}/mongo-cxx-driver/*

%files -n %{pkg_name}-devel
%{_libdir}/cmake/bsoncxx/*
%{_libdir}/cmake/mongocxx/*
%{_libdir}/pkgconfig/libmongocxx.pc
%{_libdir}/pkgconfig/libbsoncxx.pc
%{_includedir}/bsoncxx/**
%{_includedir}/mongocxx/**

%changelog
* Mon Dec 16 2024 Your Name <your_email@example.com> - 4.0.0-1
- Updated to MongoDB C++ driver version 4.0.0
- Converted to CMake-based build