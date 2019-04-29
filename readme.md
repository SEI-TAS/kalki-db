# KalkiDB
* [Usage](#usage)
* [Code Integration](#code-integration)
* [Models & Actions](#models-&-actions)
    * [Postgres Tables](#postgres-tables)
    * [Java Objects](#java-objects)
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
```
Postgres.setupDatabase();
```
            
Now, you can use the database and models. See [Java Objects](#java-objects)

Examples:
```
DeviceStatus newLight = new DeviceStatus(deviceId);
light.insertOrUpdate();
            
Device device = Postgres.findDevice(deviceId);
```
             
## Models & Actions    
### Postgres Tables
#### Alert
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String NOT NULL |
|timestamp       |Timestamp NOT NULL|
|alerter_id      |String   |
|device_status_id|int      |  
###### Actions:  
|Function Definition                             |Return Type|
|:-----------------------------------------------|:--------|
|`findAlert(int id) `                             |`CompletionStage<Alert>`      |
|`findAlerts(List<String> alerterIds)`            |`CompletionStage<List<Alert>>`|  
|`insertAlert(Alert alert)`                       |`CompletionStage<Integer>`           |
|`updateAlert(Alert alert)`                       |`CompletionStage<Integer>`           |
|`deleteAlert(int id)`                            |`CompletionStage<Boolean>`           |

#### Device
###### Schema:
|Property              |Type         |
|---------------------:|:------------|
|id                    |int          |  
|name                  |String NOT NULL |
|description           |String       | 
|type_id               |int NOT NULL | 
|group_id              |int          |
|ip_address            |String       |
|status_history_size   |int NOT NULL |
|sampling_rate         |int NOT NULL |
|current_state_id      |int |
|last_alert_id         |int |
###### Actions:  
|Function Definition                   |Return Type|
|:-------------------------------------|:--------|
|`findDevice(int id)`                  |`CompletionStage<Device>`       |
|`findAllDevices()`                    |`CompletionStage<List<Device>>` |  
|`insertDevice(Device device)`         |`CompletionStage<Integer>`      |
|`insertOrUpdateDevice(Device device)` |`CompletionStage<Integer>`      | 
|`updateDevice(Device device)`         |`CompletionStage<Integer>`      |
|`deleteDevice(int id)`                |`CompletionStage<Boolean>`      |

#### DeviceStatus
###### Schema:
|Property        |Type              |
|---------------:|:-----------------|
|id              |int               |
|device_id       |int NOT NULL      |
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

#### Group
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
 

#### SecurityState
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |  
|deviceId  |int NOT NULL|
|timestamp |Timestamp | 
|state     |String NOT NULL |
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findSecurityState(int id)`                        |`SecurityState`|
|`findSecurityStateByDevice(int deviceId)`          |`SecurityState`|
|`findSecurityStates(int deviceId)`                 |`CompletionStage<List<SecurityState>>`|
|`insertSecurityState(SecurityState securityState)` |`CompletionStage<Integer>`           |
|`updateSecurityState(SecurityState securityState)` |`CompletionStage<Integer>`           |
|`deleteSecurityState(int id)`                      |`CompletionStage<Boolean>`           |

#### Tag
###### Schema:
|Property   |Type   |
|----------:|:------|
|id         |int    |  
|name       |String NOT NULL|
###### Actions:
|Function Definition | Return Type |  
|:---|:---|
|`findAllTags()`           |`CompletionStage<List<Tag>>`|
|`findTagIds(int deviceId)`|`List<Integer>`             |
|`insertTag(Tag tag)`      |`CompletionStage<Integer>`  |
|`updateTag(Tag tag)`      |`CompletionStage<Integer>`  |
|`deleteTag(int id)`       |`CompletionStage<Boolean>`  |

#### DeviceType
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|policy_file     |byte[]   | 
|policy_file_name  |String   | 
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findDeviceType(int id)`     |`CompletionStage<Type>`      |
|`findAllDeviceTypes()`       |`CompletionStage<List<Type>>`|
|`insertDeviceType(Type type)`|`CompletionStage<Integer>`   |
|`updateDeviceType(Type type)`|`CompletionStage<Integer>`   |
|`deleteDeviceType(int id)`   |`CompletionStage<Boolean>`   |

#### UmboxImage
###### Schema:
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String NOT NULL|
|path      |String NOT NULL| 
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findUmboxImage(int id)`         |`CompletionStage<UmboxImage>`      |
|`findAllUmboxImages()`           |`CompletionStage<List<UmboxImage>>`|
|`insertUmboxImage(UmboxImage u)` |`CompletionStage<Integer>`            |
|`updateUmboxImage(UmboxImage u)` |`CompletionStage<Integer>`            |
|`deleteUmboxImage(int id)`       |`CompletionStage<Boolean>`            |

#### UmboxInstance
###### Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |String    |  
|alerter_id      |String NOT NULL|
|umbox_image_id  |int NOT NULL |
|containerId     |String NOT NULL | 
|device_id       |int NOT NULL |
|started_at      |Timestamp NOT NULL|
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findUmboxInstance(String alerterId)`  |`CompletionStage<UmboxInstance>`|
|`findUmboxInstances(int deviceId)`     |`CompletionStage<List<UmboxInstance>>`|
|`insertUmboxInstance(UmboxInstance u)` |`CompletionStage<Integer>`|
|`updateUmboxInstance(UmboxInstance u)` |`CompletionStage<Integere>`|
|`deleteUmboxInstance(int id)`          |`CompletionStage<Boolean>`|

### Java Objects
#### AlertHistory
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|timestamp       |Timestamp|
|alerterId       |String   |
|deviceStatusId  |int      |  
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`Alert(String name, String alerterId, int deviceStatusId)`|
|`Alert(String name, Timestamp timestamp, String alerterId, int deviceStatusId)`|
|`Alert(int id, String name, Timestamp timestamp, String alerterId, int deviceStatusId)`|
###### Methods
This class supports:
- `get<field>()`
    - ex: getName()
- `set<field>(<field type> value)`
    - ex: setName("Name")
- `insert()`
- `toString()`
#### Device
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
|lastAlert             |Alert        |
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`Device(String name, String description, int typeId, int groupId, String ip, int statusHistorySize, int samplingRate)`|
|`Device(int id, String name, String description, int typeId, int groupId, String ip, int statusHistorySize, int samplingRate)`|
###### Methods:
This class supports:
- `get<field>()`
    - ex: getName()
- `set<field>(<field type> value)`
    - ex: setName("Name")
- `insert()`
- `insertOrUpdate()`
- `toString()`
- `lastNSamples(int N)`
    - returns `CompletionStage<List<DeviceStatus>>`
- `samplesOverTime(int length, String timeUnit)`
    - returns `CompletionStage<List<DeviceStatus>>`
- `statusesOfSameType()`
    - returns `CompletionStage<Map<Device, DeviceStatus>>`
    
#### DeviceStatus
###### Schema:
|Property        |Type              |
|---------------:|:-----------------|
|id              |int               |
|deviceId        |int               |
|timestamp       |Timestamp         |
|attributes      |Map<String,String>| 
###### Constructors:
|Definition |  
|:---|  
|`DeviceStatus(int deviceId)`|
|`DeviceStatus(int deviceId, Map<String, String> attributes)`|
|`DeviceStatus(int deviceId, Map<String, String> attributes, Timestamp timestamp)`|
|`DeviceStatus(int deviceId, Map<String, String> attributes, Timestamp timestamp, int id)`|
###### Methods:
This class supports:
- `get<field>()`
    - ex: getName()
- `set<field>(<field type> value)`
    - ex: setName("Name")
- `addAttribute(String key, String value)`
- `insert()`
- `update()`
- `insertOrUpdate()`
- `toString()`

#### Group
###### Schema:
|Property     |Type     |
|------------:|:--------|
|id           |int      |  
|name         |String   |
###### Actions:  
|Function Definition |
|:---|
|`Group()`|
|`Group(String Name)`|
|`Group(int id, String name)`|
 ###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`

#### SecurityState
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |  
|deviceId  |int       |
|timestamp |Timestamp | 
|state     |String    |
###### Constructors:
|Definition |  
|:---|
|`SecurityState()`|
|`SecurityState(int deviceId, String state)`|
|`SecurityState(int id, int deviceId, Timestamp timestamp, String state)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`

#### Tag
###### Schema:
|Property   |Type   |
|----------:|:------|
|id         |int    |  
|name       |String |
###### Constructors:
| Definition |
|:--|
|`Tag()`|
|`Tag(String name)`|
|`Tag(int id, String name)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`

#### Type
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|policyFile      |byte[]   | 
|policyFileName  |String   | 
###### Constructors:
| Definition |
|:---| 
|`Type()`     |
|`Type(int id, String name)`|
|`Type(String name, byte[] policyFile, String policyFileName)`|
|`Type(int id, String name, byte[] policyFile, String policyFileName)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`

#### UmboxImage
###### Schema:
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String |
|path      |String | 
###### Constructors:
| Definition |  
|:---|
|`UmboxImage()`|
|`UmboxImage(String name, String path)`|
|`UmboxImage(int id, String name, String path)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
- `toString()`

#### UmboxInstance
###### Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |String    |  
|alerterId       |String    |
|umboxImageId    |String    |
|containerId     |String    | 
|deviceId        |int       |
|startedAt       |Timestamp |
###### Constructors:
|Function Definition |
|:---|
|`UmboxInstance()`  |
|`UmboxInstance(String alerterId, int umboxImageId, String containerId, int deviceId)`|
|`UmboxInstance(String alerterId, int umboxImageId, String containerId, int deviceId, Timestamp timestamp)`|
|`UmboxInstance(int id, String alerterId, int umboxImageId, String containerId, int deviceId, Timestamp timestamp)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
- `toString()`