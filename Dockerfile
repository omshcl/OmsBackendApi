FROM alpine

RUN apk add curl openjdk8 maven python bash py2-pip
RUN pip install requests
# install cassandra and java runtime
RUN cd && curl "http://apache.claz.org/cassandra/3.11.4/apache-cassandra-3.11.4-bin.tar.gz" > cassandra.tar.gz && tar xvf cassandra.tar.gz
COPY . /root/
EXPOSE 8080
ENTRYPOINT ["/root/setup.sh"]
