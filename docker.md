# Docker Documentation

The oms docker container supports the running of the entire backend. It contains an apache cassandra database that will be automatically filled with order and item data. When the docker container is built it copies the OmsBackendApi java code into it and builds the server from source, this ensures that it is always running the latest version of the Backend

# Usage
Building the container in the OmsBackendApi directory

    docker build -t oms . 
Running the container on port 8080

    docker run -p 8080:8080 -it oms
    

# Files
- [Dockerfile](Dockerfile)     The file that specifies how the container should be built
- [setup.sh](setup.sh)         A script that starts the Cassandra Database and the python populate service
- [populate.py](populate.py)   A python script that populates the database tables and launches the tomcat server
- [setup.txt](setup.txt)       File that specifies initial table schema 
- [mock.csv](mock.csv)         Mock data used for populating db
