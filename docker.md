# Docker Documentation

The oms docker container supports the running of the entire backend. It contains an apache cassandra database that will be automatically filled with order and item data. When the docker container is built it copies the OmsBackendApi java code into it and builds the server from source, this ensures that it is always running the latest version of the Backend

# Usage
Building the container in the OmsBackendApi directory

    docker-compose build 
Running the container on port 8080

    docker-compose up
    

# Files
- [Dockerfile](Dockerfile)     The file that specifies how the container should be built calls `setup.sh`
- [Docker Compose](docker-compose.yml)Docker compose yaml file that specifies how containers are combined
- [setup.sh](setup.sh)         A script that starts the python populate service
- [populate.py](populate.py)   A python script that populates the database tables and launches the tomcat server
- [mock.csv](mock.csv)         Mock data used for populating db
