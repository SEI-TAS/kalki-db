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
DeviceStatus newLight = new DeviceStatus(deviceId);
light.insertOrUpdate();
            
Device device = Postgres.findDevice(deviceId);
```
             
## Design    

### AlertHistory
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|timestamp       |Timestamp|
|source          |String   |
|alerterId       |String   |
|device_status_id|int      |  
###### Actions:  
|Function Definition                             |Return Type|
|:-----------------------------------------------|:--------|
|`findAlertHistory(int id) `                             |`CompletionStage<AlertHistory>`      |
|`findAlertHistories(List<String> alerterIds)`           |`CompletionStage<List<AlertHistory>>`|  
|`insertAlertHistory(AlertHistory alertHistory)`         |`CompletionStage<Integer>`           |
|`updateAlertHistory(AlertHistory alertHistory)`         |`CompletionStage<Integer>`           |
|`deleteAlertHistory(int id)`                            |`CompletionStage<Boolean>`           |

### Device
###### Schema:
|Property              |Type         |
|---------------------:|:------------|
|id                    |int          |  
|name                  |String       |
|description           |String       | 
|typeId                |int          | 
|groupId               |int          |
|ip                    |String       |
|statusHistorySize     |int          |
|samplingRate          |int          |
|tagIds                |List<Integer>|  
|currentState          |SecurityState|
|lastAlert             |AlertHistory |
###### Actions:  
|Function Definition                   |Return Type|
|:-------------------------------------|:--------|
|`findDevice(int id)`                  |`CompletionStage<Device>`       |
|`findAllDevices()`                    |`CompletionStage<List<Device>>` |  
|`insertDevice(Device device)`         |`CompletionStage<Integer>`      |
|`insertOrUpdateDevice(Device device)` |`CompletionStage<Integer>`      | 
|`updateDevice(Device device)`         |`CompletionStage<Integer>`      |
|`deleteDevice(int id)`                |`CompletionStage<Boolean>`      |

### DeviceStatus
###### Schema:
|Property        |Type              |
|---------------:|:-----------------|
|id              |int               |
|deviceId        |int               |
|timestamp       |Timestamp         |
|attributes      |Map<String,String>| 
###### Actions:
|Function Definition | Return Type |  
|:---|:---|  
|`findDeviceStatus(int id)`                                             |`CompletionStage<DeviceStatus>`      |
|`findDeviceStatuses(int deviceId)`                                     |`CompletionStage<List<DeviceStatus>>`|
|`findAllDeviceStatuses()`                                              |`CompletionStage<List<DeviceStatus>>`|
|`findNDeviceStatuses(int deviceId, int N)`                             |`CompletionStage<List<DeviceStatus>>`|
|`findDeviceStatusesOverTime(int deviceId, int length, String timeUnit)`|`CompletionStage<List<DeviceStatus>>`|
|`findDeviceStatusesByType(int typeId)`                                 |`CompletionStage<Map<Device, DeviceStatus>>`|
|`insertDeviceStatus(DeviceStatus deviceStatus)`                        |`CompletionStage<Integer>`           |
|`insertOrUpdateDeviceStatus(DeviceStatus deviceStatus)`                |`CompletionStage<Integer>`           |
|`updateDeviceStatus(DeviceStatus deviceStatus)`                        |`CompletionStage<Integer>`           |
|`deleteDeviceStatus(int id)`                                           |`CompletionStage<Boolean>`           |

### Group
###### Schema:
|Property     |Type     |
|------------:|:--------|
|id           |int      |  
|name         |String   |
###### Actions:  
|Function Definition | Return Type |  
|:---|:---| 
|`findGroup(int id)`        |`CompletionStage<Group>`      |
|`findAllGroups()`          |`CompletionStage<List<Group>>`|
|`insertGroup(Group group)` |`CompletionStage<Integer>`    |
|`updateGroup(Group group)` |`CompletionStage<Integer>`    |
|`deleteGroup(int id)`      |`CompletionStage<Boolean>`    |
 

### SecurityState
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
|`findSecurityState(int id)`                        |`SecurityState`|
|`findSecurityStateByDevice(int deviceId)`          |`SecurityState`|
|`findSecurityStates(int deviceId)`                 |`CompletionStage<List<SecurityState>>`|
|`insertSecurityState(SecurityState securityState)` |`CompletionStage<Integer>`           |
|`updateSecurityState(SecurityState securityState)` |`CompletionStage<Integer>`           |
|`deleteSecurityState(int id)`                      |`CompletionStage<Boolean>`           |

### Tag
###### Schema:
|Property   |Type   |
|----------:|:------|
|id         |int    |  
|name       |String |
###### Actions:
|Function Definition | Return Type |  
|:---|:---|
|`findAllTags()`           |`CompletionStage<List<Tag>>`|
|`findTagIds(int deviceId)`|`List<Integer>`             |
|`insertTag(Tag tag)`      |`CompletionStage<Integer>`  |
|`updateTag(Tag tag)`      |`CompletionStage<Integer>`  |
|`deleteTag(int id)`       |`CompletionStage<Boolean>`  |

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
|`findType(int id)`     |`CompletionStage<Type>`      |
|`findAllTypes()`       |`CompletionStage<List<Type>>`|
|`insertType(Type type)`|`CompletionStage<Integer>`   |
|`updateType(Type type)`|`CompletionStage<Integer>`   |
|`deleteType(int id)`   |`CompletionStage<Boolean>`   |

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
|`findUmboxImage(int id)`         |`CompletionStage<UmboxImage>`      |
|`findAllUmboxImages()`           |`CompletionStage<List<UmboxImage>>`|
|`insertUmboxImage(UmboxImage u)` |`CompletionStage<Integer>`            |
|`updateUmboxImage(UmboxImage u)` |`CompletionStage<Integer>`            |
|`deleteUmboxImage(int id)`       |`CompletionStage<Boolean>`            |

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
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findUmboxInstance(String alerterId)`  |`CompletionStage<UmboxInstance>`|
|`findUmboxInstances(int deviceId)`     |`CompletionStage<List<UmboxInstance>>`|
|`insertUmboxInstance(UmboxInstance u)` |`CompletionStage<Integer>`|
|`updateUmboxInstance(UmboxInstance u)` |`CompletionStage<Integere>`|
|`deleteUmboxInstance(int id)`          |`CompletionStage<Boolean>`|
