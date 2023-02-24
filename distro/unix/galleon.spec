Summary: Java Home Media Option for Tivo
Name: javaHMO
Version: 2
Release: 3
Vendor: JavaHMO <http://javahmo.sourceforge.net>
Copyright: GPL
Group: System Environment/Daemons
Source: http://unc.dl.sourceforge.net/sourceforge/javahmo/javaHMO-%{version}.src.zip
BuildRoot: /var/tmp/%{name}-buildroot

#Do not build the debug package
%define debug_package %{nil}

%description 
JavaHMO is an implementation of the Home Media Option (from TiVo) implemented in the Java programming language.

%prep
%setup -c

%build
for java in `ls /usr/java` ; do
  if [ "${java%j2sdk*}" == "" ] ; then
      export JAVA_HOME=/usr/java/$java
      break
  fi
done
ant package

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/rc.d/init.d
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}/lib
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}/plugins
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}/doc
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}/images
mkdir -p $RPM_BUILD_ROOT%{_bindir}

cd build
install -m 644 lib/* $RPM_BUILD_ROOT%{_datadir}/%{name}/lib
install -m 644 images/* $RPM_BUILD_ROOT%{_datadir}/%{name}/images
install -m 644 plugins/* $RPM_BUILD_ROOT%{_datadir}/%{name}/plugins
install -m 644 conf/* $RPM_BUILD_ROOT%{_sysconfdir}/%{name}
install -s -m 755 bin/wrapper $RPM_BUILD_ROOT%{_bindir}/wrapper
install -s -m 755 bin/JavaHMO $RPM_BUILD_ROOT%{_sysconfdir}/rc.d/init.d
install -s -m 755 bin/bash.script $RPM_BUILD_ROOT%{_bindir}/jhmo
ln -s %{_bindir}/wrapper $RPM_BUILD_ROOT%{_bindir}/JavaHMO

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%config %{_sysconfdir}/%{name}/configure.xml 
%doc Readme.txt copying ReleaseNotes.txt ThirdPartyLicenses.txt doc/

%{_bindir}/wrapper
%{_bindir}/JavaHMO
%{_bindir}/jhmo
%{_datadir}/%{name}
%{_sysconfdir}/%{name}/log4j.xml
%{_sysconfdir}/%{name}/wrapper.conf
%{_sysconfdir}/rc.d/init.d/JavaHMO

%pre
#1.0A6-2 rpm had some problems with the uninstall script, so we need to
#remove it before we install the new version
PACKAGE=`rpm -q javaHMO`;
if [ "$PACKAGE" == "javaHMO-1.0A6-2" ]; then
rpm -e javaHMO;
fi

%post
chkconfig --add JavaHMO
#clean up some stuff from pre A8 rpms that didn't properly remove
#all of their directories
if [ -d /usr/lib/javaHMO ] ; then
rm -rf /usr/lib/javaHMO
fi
if [ -d /usr/include/javaHMO ] ; then
rm -rf /usr/include/javaHMO ]
fi

#this removes the shoutcast cache file to prevent errors in javahmo
if [ -e /etc/javaHMO/shoutcast.cache ]; then
rm -f /etc/javaHMO/shoutcast.cache
fi

%preun
chkconfig --del JavaHMO

%changelogn* Thu Jan 20 2005 Jon Stroud <accounts@bsclimbing.com>n-- nn* Thu Jan 20 2005 Jon Stroud <accounts@bsclimbing.com>n-- nn* Thu Jan 20 2005 Jon Stroud <accounts@bsclimbing.com>n-- nn* Thu Jan 20 2005 Jon Stroud <accounts@bsclimbing.com>n-- update for TTGn
* Mon Feb 16 2004 Jon Stroud <accounts@bsclimbing.com>
-- Fixed bug in path assignment in init script
-- Fixed version number in GUI

* Thu Feb 12 2004 Jon Stroud <accounts@bsclimbing.com>
-- Initial relese of version 1.2

* Sun Oct 12 2003 Jon Stroud <accounts@bsclimbing.com>
-- Fixed bug with conf directory.
-- Added rc script to bin directory.
-- Fixed Configurator.java to point to correct configure file.

* Sat Oct 11 2003 Jon Stroud <accounts@bsclimbing.com>
-- New sourcecode with Unix support.  Code patching no longer required.

* Wed Oct 01 2003 Jon Stroud <accounts@bsclimbing.com>
-- Version 1.0 release.
-- Several changes to RPM package to clean up build process
