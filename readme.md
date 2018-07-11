##KalkiDB

##Usage
First, clone this repo and install maven.

To publish to the local maven repository, use 

            gradle publishToMavenLocal
            
In whatever project you want to include this library in, make sure that your dependencies include this project and that mavenLocal is under the repositories in your build.gradle file.

            repositories {
                 mavenLocal()
                 mavenCentral()
             }
             
             dependencies {
                 compile group: 'edu.cmu.sei.ttg', name: 'kalki-db', version: '0.0.1-SNAPSHOT'


  
###Code Integration

First, initialize Postgres

            Postgres.initialize(ip, port, dbName, dbUser);

and, if necessary, setup the database, creating tables, triggers, and functions:

            Postgres.setupDatabase();

Now, you can use the database and models.

Examples:

            DeviceHistory newLight = new DeviceHistory(deviceId);
            light.insertOrUpdate();
            
            Device device = Postgres.findDevice(deviceId);
            
Currently only tables are made for all models except Device and DeviceHistory.