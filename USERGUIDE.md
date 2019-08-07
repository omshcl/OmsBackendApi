# OmsBackendApi
https://github.com/omshcl/OmsBackendApi

OmsBackendApi runs in a Docker container. It contains an apache cassandra database that will be automatically filled with order and item data. When the docker container is built it copies the OmsBackendApi java code into it and builds the server from source, this ensures that it is always running the latest version of the Backend

### Usage
Building the container in the OmsBackendApi directory
     
    docker-compose build 
Running the container on port 8080

    docker-compose up
    

### Files
- [Dockerfile](https://github.com/omshcl/OmsBackendApi/blob/master/Dockerfile) The file that specifies how the container should be built, calls `setup.sh`
- [Docker Compose](https://github.com/omshcl/OmsBackendApi/blob/master/docker-compose.yml) Docker compose yaml file that specifies how containers are combined
- [setup.sh](https://github.com/omshcl/OmsBackendApi/blob/master/setup.sh) A script that starts the python populate service
- [populate.py](https://github.com/omshcl/OmsBackendApi/blob/master/populate.py) A python script that populates the database tables and launches the tomcat server
- [mock.csv](https://github.com/omshcl/OmsBackendApi/blob/master/mock.csv) Mock data used for populating db

## 
# Omsui
https://github.com/omshcl/omsui/tree/master/oms

Omsui is built in Angular and provides a frontend for OmsBackendApi.

### Usage
Build and run the application on port 4200

    npm start
Agent User Login

    u: agent
    p: Agent!123
Admin User Login

    u: admin
    p: Admin!123
    
### Files
- [proxy-config.json](https://github.com/omshcl/omsui/blob/master/oms/proxy.config.json) Contains proxy settings to direct the app to a running instance of OmsBackendApi

## 
# OmsApp 
https://github.com/omshcl/omsapp

An android application that implements a Curbside Pickup use case utilizing location services. Requires OmsBackendApi for login authentication and order management.

### Usage
Build and run the application using Android Studio.

User Login

    u: bbagel
    p: password

### Files
- <b>google-services.json</b> Not included in Git repository. Should be placed in /OmsApp/app folder. Contains Google Firebase API keys for receiving push notifications.
- <b>google_maps_api.xml</b> Not included in Git repository. Should be placed in /OmsApp/app/src/main/res/values folder. Contains Google Map API keys for Google Map services.
- [strings.xml](https://github.com/omshcl/OmsApp/blob/master/app/src/main/res/values/strings.xml) Value for <i>backend_url</i> should point to a running instance of OmsBackendApi
- [hcl approach.kml](https://github.com/omshcl/OmsApp/blob/master/hcl%20approach.kml) Contains mock location data for simulating a curbside pickup.
