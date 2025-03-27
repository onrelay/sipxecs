
dart-sdk_VER = 3.7.0
dart-sdk_REL = 1
dart-sdk_TARBALL = dartsdk-linux-x64-release.zip
dart-sdk_URL = https://storage.googleapis.com/dart-archive/channels/stable/release/$(dart-sdk_VER)/sdk/$(dart-sdk_TARBALL)
dart-sdk_x86_64_RPM = dart-sdk-$(dart-sdk_VER)-$(dart-sdk_REL).x86_64.rpm

# Install FPM
fpm-install:
	@if ! command -v fpm >/dev/null 2>&1; then \
	    echo "Installing FPM..."; \
	    gem install --no-document fpm; \
	else \
	    echo "FPM is already installed."; \
	fi

dart-sdk.rpm-setup: fpm-install

dart-sdk.dist dart-sdk.srpm :;

dart-sdk.rpm: $(dart-sdk_x86_64_RPM)
	mv -f $< $(MOCK_RESULTS_DIR)

$(dart-sdk_TARBALL): fpm-install
	wget -O $(dart-sdk_TARBALL) $(dart-sdk_URL)

$(dart-sdk_x86_64_RPM): $(dart-sdk_TARBALL)
	fpm -f -s zip -t rpm -n dart-sdk -v $(dart-sdk_VER) --iteration $(dart-sdk_REL) \
	  --after-install $(SRC)/dart-sdk/after-install.sh \
	  --license BSD --prefix /opt -a $(DISTRO_ARCH) $<
