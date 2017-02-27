{%- from 'gateway/settings.sls' import gateway with context %}

include:
  - gateway.repo

knox:
  pkg.installed

#/usr/hdp/current/knox-server/conf/topologies/admin.xml:
#  file.absent

#/usr/hdp/current/knox-server/conf/topologies/knoxsso.xml:
#  file.absent

/usr/hdp/current/knox-server/conf/topologies/manager.xml:
  file.absent

/usr/hdp/2.6.0.0-422/knox/lib/org/apache/hadoop/gateway/filter/rewrite/impl/UrlRewriteResponse.class:
  file.managed:
    - makedirs: True
    - source: salt://gateway/patch/UrlRewriteResponse.class

patch-gateway-provider-rewrite:
  cmd.run:
    - name: cd /usr/hdp/2.6.0.0-422/knox/lib/ && jar uf gateway-provider-rewrite-0.11.0.2.6.0.0-422.jar org/apache/hadoop/gateway/filter/rewrite/impl/UrlRewriteResponse.class && touch patched-gateway-provider-rewrite
    - creates: /usr/hdp/2.6.0.0-422/knox/lib/patched-gateway-provider-rewrite

/usr/hdp/2.6.0.0-422/knox/lib/org/apache/hadoop/gateway/filter/XForwardedHeaderRequestWrapper.class:
  file.managed:
    - makedirs: True
    - source: salt://gateway/patch/XForwardedHeaderRequestWrapper.class

patch-gateway-server-xforwarded-filter:
  cmd.run:
    - name: cd /usr/hdp/2.6.0.0-422/knox/lib/ && jar uf gateway-server-xforwarded-filter-0.11.0.2.6.0.0-422.jar org/apache/hadoop/gateway/filter/XForwardedHeaderRequestWrapper.class && touch patch-gateway-server-xforwarded-filter
    - creates: /usr/hdp/2.6.0.0-422/knox/lib/patch-gateway-server-xforwarded-filter

/usr/hdp/2.6.0.0-422/knox/lib/applications/knoxauth/app/js/knoxauth.js:
  file.managed:
    - makedirs: True
    - source: salt://gateway/patch/knoxauth.js

patch-gateway-applications:
  cmd.run:
    - name: cd /usr/hdp/2.6.0.0-422/knox/lib/ && jar uf gateway-applications-0.11.0.2.6.0.0-422.jar applications/knoxauth/app/js/knoxauth.js && touch patch-gateway-applications
    - creates: /usr/hdp/2.6.0.0-422/knox/lib/patch-gateway-applications

/var/lib/knox/data-2.6.0.0-422/applications/knoxauth/app/js/knoxauth.js:
  file.managed:
    - source: salt://gateway/patch/knoxauth.js

#/usr/hdp/2.6.0.0-422/knox/lib/gateway-provider-rewrite-0.11.0.2.6.0.0-422.jar:
#  file.managed:
#    - source: salt://gateway/patch/gateway-provider-rewrite-0.11.0.2.6.0.0-422.jar

#/usr/hdp/2.6.0.0-422/knox/lib/gateway-server-xforwarded-filter-0.11.0.2.6.0.0-422.jar:
#  file.managed:
#    - source: salt://gateway/patch/gateway-server-xforwarded-filter-0.11.0.2.6.0.0-422.jar

knox-master-secret:
  cmd.run:
    - name: /usr/hdp/current/knox-server/bin/knoxcli.sh create-master --master '{{ salt['pillar.get']('gateway:password') }}'
    - user: knox
    - creates: /usr/hdp/current/knox-server/data/security/master

knox-create-cert:
  cmd.run:
    - name: /usr/hdp/current/knox-server/bin/knoxcli.sh create-cert --hostname {{ salt['pillar.get']('gateway:address') }}
    - user: knox
    - creates: /usr/hdp/current/knox-server/data/security/keystores/gateway.jks

knox-export-cert:
  cmd.run:
    - name: /usr/hdp/current/knox-server/bin/knoxcli.sh export-cert --type PEM
    - user: knox
    - creates: /usr/hdp/current/knox-server/data/security/keystores/gateway-identity.pem

#openssl x509 -in /usr/hdp/current/knox-server/data/security/keystores/gateway-identity.pem -text -noout

/usr/hdp/current/knox-server/conf/users.ldif:
  file.managed:
    - source: salt://gateway/config/users.ldif.j2
    - template: jinja

/usr/hdp/current/knox-server/conf/gateway-site.xml:
  file.managed:
    - source: salt://gateway/config/gateway-site.xml.j2
    - template: jinja

{% if salt['pillar.get']('gateway:ssoprovider') %}

/usr/hdp/current/knox-server/conf/topologies/knoxsso.xml:
  file.managed:
    - source: salt://gateway/config/knoxsso.xml.j2
    - template: jinja
    - user: knox
    - group: knox

{% else %}

/usr/hdp/current/knox-server/conf/topologies/knoxsso.xml:
  file.absent

{% endif %}

/usr/hdp/current/knox-server/conf/topologies/{{ salt['pillar.get']('gateway:topology') }}.xml:
  file.managed:
    - source: salt://gateway/config/topology.xml.j2
    - template: jinja
    - user: knox
    - group: knox

#https://github.com/rabits/salt-stack-modules/blob/master/openvpn/server.sls
#setcap 'cap_net_bind_service=+ep' /usr/hdp/current/knox-server/bin/gateway.sh:
#  cmd.run:
#    - unless: getcap /usr/hdp/current/knox-server/bin/gateway.sh | grep -q 'cap_net_bind_service+ep'


{% if gateway.is_systemd %}

/etc/systemd/system/knox-ldap.service:
  file.managed:
    - source: salt://gateway/systemd/knox-ldap.service

start-knox-ldap:
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /etc/systemd/system/knox-ldap.service
  service.running:
    - enable: True
    - name: knox-ldap
    - watch:
       - file: /etc/systemd/system/knox-ldap.service


/etc/systemd/system/knox-gateway.service:
  file.managed:
    - source: salt://gateway/systemd/knox-gateway.service

start-knox-gateway:
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /etc/systemd/system/knox-gateway.service
  service.running:
    - enable: True
    - name: knox-gateway
    - watch:
       - file: /etc/systemd/system/knox-gateway.service


{% else %}

# Upstart case

/usr/hdp/current/knox-server/bin/ldap.sh:
  file.managed:
    - source: salt://gateway/upstart/ldap.sh
    - mode: 755

/etc/init/knox-ldap.conf:
  file.managed:
    - source: salt://gateway/upstart/knox-ldap.conf

start-knox-ldap:
  service.running:
    - enable: True
    - name: knox-ldap

/usr/hdp/current/knox-server/bin/gateway.sh:
  file.managed:
    - source: salt://gateway/upstart/gateway.sh
    - mode: 755

/etc/init/knox-gateway.conf:
  file.managed:
    - source: salt://gateway/upstart/knox-gateway.conf

start-knox-gateway:
  service.running:
    - enable: True
    - name: knox-gateway

{% endif %}
