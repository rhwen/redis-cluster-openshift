# redis-cluster-openshift

Create Redis node by command line:

[root@master ~]# oc process -p REDIS_PORT=9008 -f dc-redis-cluster-template.yaml | oc create -f -