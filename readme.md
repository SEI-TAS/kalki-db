# KalkiDB

## Usage
First, clone this repo and install maven.

To publish to the local maven repository, use `$ gradle publishToMavenLocal`.

In whatever project you want to include this library in, make sure that your dependencies include this project
and that mavenLocal is under the repositories in your build.gradle file.
```
repositories {
    mavenLocal()
    mavenCentral()
}
 
dependencies {
    compile group: 'edu.cmu.sei.ttg', name: 'kalki-db', version: '0.0.1-SNAPSHOT'
}
```


  
## Code Integration

First, initialize Postgres using 
```
Postgres.initialize(String ip, String port, String dbName, String dbUser);
```
and, if necessary, setup the database, creating tables, triggers, and functions:
            Postgres.setupDatabase();
Now, you can use the database and models.

Examples:
```
DeviceHistory newLight = new DeviceHistory(deviceId);
light.insertOrUpdate();
            
Device device = Postgres.findDevice(deviceId);
```
             
##Design    


####AlertHistory
#####Schema:

  |Property        |Type     |
  |---------------:|:--------|
  |id              |int      |  
  |timestamp       |Timestamp|
  |umboxExternalId |String   | 
  |info            |String   |  
  
######Actions:  
|Function Definition                             |Return Type|
|:-----------------------------------------------|:--------|
|`getAlertHistory(List<String> externalIds)`     |`List<AlertHistory>` |  
|`getAlertHistory(int id) `                      |`CompletionStage<AlertHistory>`|
|`insertAlertHistory(AlertHistory alertHistory)` |`CompletionStage<Integer>`   | 


####Device
######Schema:
|Property        |Type         |
|---------------:|:------------|
|id              |int          |  
|name            |String       |
|description     |String       | 
|typeId          |int          | 
|groupId         |int          |
|ip              |String       |
|historySize     |int          |
|samplingRate    |int          |
|tagIds          |List<Integer>|
######Actions:  
|Function Definition                             |Return Type|
|:-------------------------------------|:--------|
|`getAllDevices()`                     |`CompletionStage<List<Device>>` |  
|`insertDevice(Device device)`         |`CompletionStage<Integer>`|
|`insertOrUpdateDevice(Device device)` |`CompletionStage<Integer>`| 
|`updateDevice(Device device)`         |`CompletionStage<Integer>`|


####DeviceHistory
######Schema:
|Property        |Type              |
|---------------:|:-----------------|
|id              |int               |
|group           |String            |  
|timestamp       |Timestamp         |
|attributes      |Map<String,String>| 
|deviceId        |int               |
######Actions:
|Function Definition | Return Type |  
|:---|:---|  
|`findDevice(int id)`                                         |`CompletionStage<Device> `|
|`getAllDeviceHistories()`                                    |`CompletionStage<List<DeviceHistory>>`|
|`getDeviceHistory(int deviceId)`                             |`CompletionStage<List<DeviceHistory>>`|
|`findDeviceHistory(int id)`                                  |`CompletionStage<DeviceHistory>` |
|`insertDeviceHistory(DeviceHistory deviceHistory)`           |`CompletionStage<Integer>`|
|`insertOrUpdateDeviceHistory(DeviceHistory deviceHistory)`   |`CompletionStage<Integer>`|
|`updateDeviceHistory(DeviceHistory deviceHistory)`           |`CompletionStage<Integer>`|
|`deleteDevice(int id)`                                       |`CompletionStage<Boolean>`|
    

####Group
######Schema:
|Property     |Type     |
|------------:|:--------|
|id           |int      |  
|name         |String   |
######Actions:  
|Function Definition | Return Type |  
|:---|:---| 
|`findGroup(int id)`        |`CompletionStage<Group>`|
|`getAllGroups()`           |`CompletionStage<List<Group>>`|
|`insertGroup(Group group)` |`CompletionStage<Integer>`|
|`deleteGroup(int id)`      |`CompletionStage<Boolean>`|
 

####StateHistory
######Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |  
|deviceId  |int       |
|timestamp |Timestamp | 
|state     |String    |
######Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`getStateHistory(int deviceId)`|`CompletionStage<List<StateHistory>>`|


####Tag
######Schema:
|Property   |Type   |
|----------:|:------|
|id         |int    |  
|name       |String |
######Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`getAllTags()`|`CompletionStage<List<Tag>>`|


####Type
######Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|policyFile      |byte[]   | 
|policyFileName  |String   | 
######Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`getAllTypes()`|`CompletionStage<List<Type>>`|


####UmboxImage
######Schema:
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String |
|path      |String | 
######Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`getAllUmboxImages()`          |`CompletionStage<List<UmboxImage>>`|
|`addUmboxImage(UmboxImage u)`  |`CompletionStage<Void>`|
|`editUmboxImage(UmboxImage u)` |`CompletionStage<Void>`|


####UmboxInstance
######Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |String    |  
|umboxExternalId |String    |
|umboxImageId    |String    | 
|deviceId        |int       |
|startedAt       |Timestamp |
######Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`getUmboxInstance(String externalId)`  |`CompletionStage<UmboxInstance>`|
|`getUmboxInstances(int deviceId)`      |`CompletionStage<List<UmboxInstance>>`|
|`insertUmboxInstance(UmboxInstance u)` |`CompletionStage<Void>`|
|`editUmboxInstance(UmboxInstance u)`   |`CompletionStage<Void>`|
|`deleteUmboxInstance(int id)`          |`CompletionStage<Boolean>`|
