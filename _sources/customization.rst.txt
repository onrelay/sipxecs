.. index:: customization

=============
Customization
=============

The sipxconfig web UI can be customized without rebuilding or repackaging any
software. The directory ``/etc/sipxpbx`` is on the classpath of the sipxconfig
web application, so loose resource files placed there are picked up at startup.
This can be used to override skin assets such as the logo, the login image,
the favicon, or the stylesheets.

Two mechanisms work together:

* ``/etc/sipxpbx/sipxplugin.beans.xml`` -- a Spring bean file that is loaded by
  sipxconfig at startup and can override or add beans. For skinning it declares
  a ``skinProperties`` bean that points to a properties file.
* ``/etc/sipxpbx/skin.properties`` -- maps a skin asset name to a classpath
  resource. Because ``/etc/sipxpbx`` is a classpath root, a value such as
  ``skin/logo.png`` resolves to the file ``/etc/sipxpbx/skin/logo.png``.

Example: Replacing the Logo
===========================

1. Create ``/etc/sipxpbx/sipxplugin.beans.xml``::

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.springframework.org/schema/beans
                          http://www.springframework.org/schema/beans/spring-beans.xsd">
      <bean id="skinProperties" class="org.sipfoundry.sipxconfig.site.skin.SkinProperties">
        <property name="locationPath" value="skin.properties" />
      </bean>
    </beans>

2. Create ``/etc/sipxpbx/skin.properties``::

    logo.png=skin/logo.png

3. Copy your logo to ``/etc/sipxpbx/skin/logo.png``. Any size works; the UI
   scales it proportionally (the banner area is 265x84 pixels, the login page
   logo is 120 pixels high).

4. Make sure the files are readable by the ``sipx`` user, then restart the
   web UI::

    systemctl restart sipxconfig

The new logo is served at ``https://<server>/sipxconfig/skin/logo.png`` and
appears in the banner and on the login page.

Other Skin Assets
=================

Any asset name registered by the skin can be overridden the same way by adding
more lines to ``skin.properties``. Commonly overridden assets include::

    logo.png=skin/logo.png
    login.jpg=skin/login.jpg
    favicon.ico=skin/favicon.ico
    custom.css=skin/custom.css

The full list of default assets can be found in the source file
``sipXconfig/web/src/org/sipfoundry/sipxconfig/site/skin/SkinControl.java``.

Packaged Plugins
================

Customizations that include Java code (phone plugins, hooks, additional pages)
are still delivered as plugin jars installed in
``/usr/share/java/sipXecs/sipXconfig/plugins``. A plugin jar may contain its
own ``sipxplugin.beans.xml``, ``skin.properties`` and resources, following the
same conventions as above.
