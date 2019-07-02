FROM python:3.6.4-alpine3.7
RUN apk --update add --no-cache g++
RUN ls
RUN apk add curl openjdk8 maven python3 bash py-pip
RUN pip3 install requests pandas
# install cassandra and java runtime
RUN cd && curl "http://apache.claz.org/cassandra/3.11.4/apache-cassandra-3.11.4-bin.tar.gz" > cassandra.tar.gz && tar xvf cassandra.tar.gz
COPY . /root/
EXPOSE 8080
ENTRYPOINT ["/root/setup.sh"]
