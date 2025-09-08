mongo-cxx-driver_VER = 4.1.0
mongo-cxx-driver_REL = 1
mongo-cxx-driver_SRPM = mongo-cxx-driver-$(mongo-cxx-driver_VER)-$(mongo-cxx-driver_REL)$(RPM_DIST).src.rpm
mongo-cxx-driver_SPEC = $(SRC)/mongo-cxx-driver/mongo-cxx-driver.spec
mongo-cxx-driver_TARBALL = mongo-cxx-driver-r$(mongo-cxx-driver_VER).tar.gz
mongo-cxx-driver_SOURCES = $(mongo-cxx-driver_TARBALL)

# URL for the source tarball
mongo-cxx-driver_URL = https://github.com/mongodb/mongo-cxx-driver/releases/download/r$(mongo-cxx-driver_VER)/$(mongo-cxx-driver_TARBALL)

# Rule to ensure the tarball is downloaded
$(mongo-cxx-driver_TARBALL):
	curl -OL $(mongo-cxx-driver_URL)

mongo-cxx-driver.dist: $(mongo-cxx-driver_SRPM)
	rpmbuild --rebuild $(mongo-cxx-driver_SRPM)

$(mongo-cxx-driver_SRPM): $(mongo-cxx-driver_SOURCES) $(mongo-cxx-driver_SPEC)
	rpmbuild -bs --noclean --define "_sourcedir $(dir $(mongo-cxx-driver_SOURCES))" \
	                  --define "_srcrpmdir $(dir $@)" \
	                  $(mongo-cxx-driver_SPEC)

