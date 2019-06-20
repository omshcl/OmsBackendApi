FROM alpine

RUN apk add curl openjdk8 maven
RUN ls
# install cassandra and java runtime
RUN cd && curl "http://apache.claz.org/cassandra/3.11.4/apache-cassandra-3.11.4-bin.tar.gz" > cassandra.tar.gz && tar xvf cassandra.tar.gz
COPY . /root/
CMD ./root/apache-cassandra-3.11.4/bin/cassandra -R && cd /
EXPOSE 8080