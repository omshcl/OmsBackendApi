# uses alpine linux to build a lightweight container
FROM python:3.6.4-alpine3.7
RUN apk --update add --no-cache g++
# install cassandra and tomcat dependencies
RUN apk add curl openjdk8 maven python3 bash py-pip
RUN pip3 install requests pandas cassandra-driver
# install cassandra and java runtime
RUN cd && curl "http://apache.claz.org/cassandra/3.11.4/apache-cassandra-3.11.4-bin.tar.gz" > cassandra.tar.gz && tar xvf cassandra.tar.gz
COPY . /root/
EXPOSE 8080
EXPOSE 9042
EXPOSE 9160
#NTRYPOINT ["/bin/bash"]
ENTRYPOINT ["/root/setup.sh"]
