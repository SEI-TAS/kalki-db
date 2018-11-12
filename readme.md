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
             
## Design    

### AlertHistory
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|timestamp       |Timestamp|
|alerterId       |String   |
|info            |String   |  
###### Actions:  
|Function Definition                             |Return Type|
|:-----------------------------------------------|:--------|
|`findAlertHistory(int id) `                     |`CompletionStage<AlertHistory>`|
|`findAlertHistories(List<String> alerterIds)`   |`List<AlertHistory>`           |  
|`insertAlertHistory(AlertHistory alertHistory)` |`CompletionStage<Integer>`     | 

### Device
###### Schema:
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
###### Actions:  
|Function Definition                   |Return Type|
|:-------------------------------------|:--------|
|`findDevice(int id)`                  |`CompletionStage<Device>`       |
|`findAllDevices()`                    |`CompletionStage<List<Device>>` |  
|`insertDevice(Device device)`         |`CompletionStage<Integer>`      |
|`insertOrUpdateDevice(Device device)` |`CompletionStage<Integer>`      | 
|`updateDevice(Device device)`         |`CompletionStage<Integer>`      |
|`deleteDevice(int id)`                |`CompletionStage<Boolean>`      |

### DeviceHistory
###### Schema:
|Property        |Type              |
|---------------:|:-----------------|
|id              |int               |
|group           |String            |  
|timestamp       |Timestamp         |
|attributes      |Map<String,String>| 
|deviceId        |int               |
###### Actions:
|Function Definition | Return Type |  
|:---|:---|  
|`findDeviceHistory(int id)`                                  |`CompletionStage<DeviceHistory>`      |
|`findDeviceHistories(int deviceId)`                          |`CompletionStage<List<DeviceHistory>>`|
|`findAllDeviceHistories()`                                   |`CompletionStage<List<DeviceHistory>>`|
|`insertDeviceHistory(DeviceHistory deviceHistory)`           |`CompletionStage<Integer>`            |
|`insertOrUpdateDeviceHistory(DeviceHistory deviceHistory)`   |`CompletionStage<Integer>`            |
|`updateDeviceHistory(DeviceHistory deviceHistory)`           |`CompletionStage<Integer>`            |

### Group
###### Schema:
|Property     |Type     |
|------------:|:--------|
|id           |int      |  
|name         |String   |
###### Actions:  
|Function Definition | Return Type |  
|:---|:---| 
|`findGroup(int id)`        |`CompletionStage<Group>`|
|`findAllGroups()`          |`CompletionStage<List<Group>>`|
|`insertGroup(Group group)` |`CompletionStage<Integer>`|
|`deleteGroup(int id)`      |`CompletionStage<Boolean>`|
 

### StateHistory
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |  
|deviceId  |int       |
|timestamp |Timestamp | 
|state     |String    |
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findStateHistories(int deviceId)`|`CompletionStage<List<StateHistory>>`|


### Tag
###### Schema:
|Property   |Type   |
|----------:|:------|
|id         |int    |  
|name       |String |
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findAllTags()`|`CompletionStage<List<Tag>>`|


### Type
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|policyFile      |byte[]   | 
|policyFileName  |String   | 
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findAllTypes()`|`CompletionStage<List<Type>>`|


### UmboxImage
###### Schema:
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String |
|path      |String | 
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findAllUmboxImages()`          |`CompletionStage<List<UmboxImage>>`|
|`insertUmboxImage(UmboxImage u)`  |`CompletionStage<Void>`|
|`updateUmboxImage(UmboxImage u)` |`CompletionStage<Void>`|


### UmboxInstance
###### Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |String    |  
|alerterId       |String    |
|umboxImageId    |String    |
|containerId     |String    | 
|deviceId        |int       |
|startedAt       |Timestamp |
|containerId     |String    |
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findUmboxInstance(String alerterId)`   |`CompletionStage<UmboxInstance>`|
|`findUmboxInstances(int deviceId)`      |`CompletionStage<List<UmboxInstance>>`|
|`insertUmboxInstance(UmboxInstance u)` |`CompletionStage<Void>`|
|`updateUmboxInstance(UmboxInstance u)`   |`CompletionStage<Void>`|
|`deleteUmboxInstance(int id)`          |`CompletionStage<Boolean>`|
