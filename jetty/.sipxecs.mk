jetty_VER = 12.0.22
jetty_REL = 1
jetty_SRPM = jetty-$(jetty_VER)-$(jetty_REL)$(RPM_DIST).src.rpm
jetty_SPEC = $(SRC)/jetty/jetty.spec
jetty_TARBALL = jetty-$(jetty_VER).tar.gz
jetty_SOURCES = $(jetty_TARBALL)

# URL for the Jetty distribution
jetty_URL = https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-home/$(jetty_VER)/jetty-home-$(jetty_VER).tar.gz

# Download rule
$(jetty_TARBALL):
	curl -L $(jetty_URL) -o $(jetty_TARBALL)


jetty.dist: $(jetty_SRPM)
	rpmbuild --rebuild $(jetty_SRPM)

$(jetty_SRPM): $(jetty_SOURCES) $(jetty_SPEC)
	rpmbuild -bs --noclean --define "_sourcedir $(dir $(jetty_SOURCES))" \
	                  --define "_srcrpmdir $(dir $@)" \
	                  $(jetty_SPEC)