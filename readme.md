# KalkiDB
* [Usage](#usage)
* [Code Integration](#code-integration)
* [Models & Actions](#models-&-actions)
    * [Postgres Tables](#postgres-tables)
    * [Java Objects](#java-objects)
## Usage
First, clone this repo.

To publish to the local maven repository, use `$ ./gradlew publishToMavenLocal`.

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

### Docker
Set up a docker network by running `$ docker network create -d bridge kalki_nw`. This initializes a container network. *This only ever needs to be done once*.

Start the database by running `$ ./run_postgres_container.sh` from the project root.
This will create a docker volume named `kalki-database` so the data persists when the database is terminated.

Stop the docker container via `$ docker kill kalki-postgres`  

__NOTE:__ If your application is also running in docker you must include `--net=kalki_nw` in your `docker run` command   
(ex: `$ docker run --net=kalki_nw --name=<container-name> <image>`)
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
|alert_type_id   |int      |
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

#### AlertCondition
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|variables       |hstore   |
|device_id       |int NOT NULL |
|alert_type_id   |int NOT NULL |  
###### Actions:  
|Function Definition                                 |Return Type|
|:---------------------------------------------------|:--------|
|`findAlertCondition(int id) `                       |`CompletionStage<AlertCondition>`      |
|`findAlertConditionsByDevice(int deviceId)`         |`CompletionStage<List<AlertCondition>>`|
|`findAllAlertConditions()`                          |`List<AlertCondition>`|
|`insertAlertCondition(AlertCondition condition`     |`CompletionStage<Integer>`           |
|`insertAlertConditionByDeviceType(AlertCondition cond) `|`CompletionStage<Integer>`       |
|`updateAlertCondition(AlertCondition condition)`    |`CompletionStage<Integer>`           |
|`insertOrUpdateAlertCondition(AlertCondition condition)`| `CompletionStage<Integer>`      |
|`deleteAlertCondition(int id)`                      |`CompletionStage<Boolean>`           |

#### AlertType
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String NOT NULL |
|description     |String   |
|source          |String NOT NULL |
###### Actions:  
|Function Definition                                 |Return Type|
|:---------------------------------------------------|:--------|
|`findAlertType(int id) `                       |`CompletionStage<AlertType>`      |
|`findAllAlertTypes()  `                        |`List<AlerType>`                  |
|`findAlertTypesByDeviceType(int deviceTypeId)` |`CompletionStage<List<AlertType>>`|
|`insertAlertType(AlertType type)`              |`CompletionStage<Integer>`|
|`updateAlertType(AlertType type)`              |`CompletionStage<Integer>`|
|`insertOrUpdate(AlertType type)`               |`CompletionStage<Integer>`|
|`deleteAlertType(int id)`                      |`CompletionStage<Boolean>`|

#### Command
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |serial PRIMARY KEY|
|name            |String   |
###### Actions:  
|Function Definition                   |Return Type|
|:-------------------------------------|:--------|
|`findAllCommands()`|`CompletionStage<List<DeviceCommand>>`|
|`insertCommand(DeviceCommand command)`|`CompletionStage<Integer>`|

#### CommandLookup
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |serial PRIMARY KEY|
|device_type_id  |int NOT NULL|  
|state_id        |int NOT NULL|
|command_id      |int NOT NULL|
###### Actions:  
|Function Definition                   |Return Type|
|:-------------------------------------|:--------|
|`findAllCommandLookups()`             |`CompletionStage<List<DeviceCommand>>`|
|`findCommandsByDevice(Device device)` |`CompletionStage<List<DeviceCommand>>`|
|`insertCommandLookup(DeviceCommand command)`|`int`|
|`insertOrUpdateCommandLookup(DeviceCommand command)`|`CompletionStage<Integer>`|
|`updateCommandLookup(DeviceCommand command)`|`CompletionStage<Integer>`|
|`deleteCommandLookup(int id)`|`CompletionStage<Boolean>`|

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
|`findDevicesByGroup(int groupId)`     |`List<Device>`                  |
|`findDevicesByType(int typeId)`       |`List<Device>`                  |
|`insertDevice(Device device)`         |`CompletionStage<Integer>`      |
|`insertOrUpdateDevice(Device device)` |`CompletionStage<Integer>`      | 
|`updateDevice(Device device)`         |`CompletionStage<Integer>`      |
|`deleteDevice(int id)`                |`CompletionStage<Boolean>`      |

#### DeviceSecurityState
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |  
|deviceId  |int NOT NULL|
|timestamp |Timestamp | 
|state_id  |int NOT NULL |
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findDeviceSecurityState(int id)`                        |`DeviceSecurityState`|
|`findDeviceSecurityStateByDevice(int deviceId)`          |`DeviceSecurityState`|
|`findDeviceSecurityStates(int deviceId)`                 |`CompletionStage<List<DeviceSecurityState>>`|
|`insertDeviceSecurityState(DeviceState deviceState)`     |`CompletionStage<Integer>`           |
|`deleteDeviceSecurityState(int id)`                      |`CompletionStage<Boolean>`           |

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
|`findNDeviceStatuses(int deviceId, int N)`                             |`CompletionStage<List<DeviceStatus>>`|
|`findDeviceStatusesOverTime(int deviceId, int length, String timeUnit)`|`CompletionStage<List<DeviceStatus>>`|
|`findDeviceStatusesByType(int typeId)`                                 |`<Map<Device, DeviceStatus>`|
|`findDeviceStatusesByGroup(int groupId)`                               |`<Map<Device, DeviceStatus>`|
|`findAllDeviceStatuses()`                                              |`CompletionStage<List<DeviceStatus>>`|
|`insertDeviceStatus(DeviceStatus deviceStatus)`                        |`CompletionStage<Integer>`           |
|`insertOrUpdateDeviceStatus(DeviceStatus deviceStatus)`                |`CompletionStage<Integer>`           |
|`updateDeviceStatus(DeviceStatus deviceStatus)`                        |`CompletionStage<Integer>`           |
|`deleteDeviceStatus(int id)`                                           |`CompletionStage<Boolean>`           |

#### DeviceType
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String NOT NULL |
|policy_file     |byte[]   | 
|policy_file_name  |String   | 
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findDeviceType(int id)`     |`CompletionStage<DeviceType>`      |
|`findAllDeviceTypes()`       |`CompletionStage<List<DeviceType>>`|
|`insertDeviceType(DeviceType type)`|`CompletionStage<Integer>`   |
|`updateDeviceType(DeviceType type)`|`CompletionStage<Integer>`   |
|`deleteDeviceType(int id)`   |`CompletionStage<Boolean>`   |

#### Group
###### Schema:
|Property     |Type     |
|------------:|:--------|
|id           |int      |  
|name         |String NOT NULL |
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
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String NOT NULL|
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findSecurityState(int id)`                |`CompletionStage<SecurityState>`      |
|`findAllSecurityStates()`                  |`CompletionStage<List<SecurityState>` |
|`insertSecurityState(SecurityState state)` |`Integer`            |
|`updateSecurityState(SecurityState state)` |`CompletionStage<Integer>`            |
|`deleteSecurityState(int id)`              |`CompletionStage<Boolean>`            |

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

#### UmboxImage
###### Schema:
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String NOT NULL|
|file_name |String NOT NULL| 
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findUmboxImage(int id)`         |`CompletionStage<UmboxImage>`      |
|`findUmboxImagesByDeviceTypeAndSecState(int devTypeId, int secStateId)`|`List<UmboxImage>`|
|`findAllUmboxImages()`           |`CompletionStage<List<UmboxImage>>`|
|`insertUmboxImage(UmboxImage u)` |`CompletionStage<Integer>`            |
|`updateUmboxImage(UmboxImage u)` |`CompletionStage<Integer>`            |
|`deleteUmboxImage(int id)`       |`CompletionStage<Boolean>`            |

#### UmboxInstance
###### Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |int       |  
|alerter_id      |String NOT NULL|
|umbox_image_id  |int NOT NULL |
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

#### UmboxLookup
###### Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |serial PRIMARY KEY|  
|state_id        |int NOT NULL|  
|umbox_image_id  |int NOT NULL|
|device_type_id  |int NOT NULL |
|order           |int NOT NULL|
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findUmboxLookup(int id)`  |`UmboxLookup`|
|`findAllUmboxLookups()`  |`CompletionStage<List<UmboxLookup>>`|
|`insertUmboxLookup(UmboxLookup ul)`  |`Integer`|
|`updateUmboxLookup(UmboxLookup ul)`  |`Integer`|
|`insertOrUpdateUmboxLookup(UmboxLookup ul)`  |`Integer`|
|`deleteUmboxLookup(int id)`  |`CompletionStage<Boolean>`|



### Java Objects
#### Alert
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|timestamp       |Timestamp|
|alerterId       |String   |
|deviceStatusId  |Integer  |
|alertTypeId     |int      |
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`Alert(String name, String alerterId, int alertTypeId)`|
|`Alert(String name, Integer deviceStatusId, int alertTypeId)`|
|`Alert(String name, String alerterId, Integer deviceStatusId, int alertTypeId)`|
|`Alert(String name, Timestamp timestamp, String alerterId, Integer deviceStatusId, int alertTypeId)`|
|`Alert(int id, String name, Timestamp timestamp, String alerterId, Integer deviceStatusId, int alertTypeId)`|
###### Methods
This class supports:
- `get<field>()`
    - ex: getName()
- `set<field>(<field type> value)`
    - ex: setName("Name")
- `insert()`
- `toString()`
#### AlertCondition
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|variables       |Map<String, String>|
|deviceId        |int   |
|alertTypeId     |int   |
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`AlertCondition()`|
|`AlertCondition(Map<String, String> variables, int deviceId, int alertTypeId)`|
|`AlertCondition(Map<String, String> variables, Integer deviceId, int alertTypeId, Integer deviceTypeId)`|
|`AlertCondition(int id, Map<String, String> variables, int deviceId, int alertTypeId)`|
###### Methods
This class supports:
- `get<field>()`
    - ex: getName()
- `set<field>(<field type> value)`
    - ex: setName("Name")
- `insert()`
- `insertOrUpdate()`
- `toString()`
#### AlertType
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|description     |String   |
|source          |String   |
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`AlertType()`|
|`AlertType(String name, String description, String source)`|
|`AlertType(int id, String name, String description, String source)`|
###### Methods
This class supports:
- `get<field>()`
    - ex: getName()
- `set<field>(<field type> value)`
    - ex: setName("Name")
- `insert()`
- `insertOrUpdate()`
- `toString()`
#### Device
###### Schema:
|Property              |Type         |
|---------------------:|:------------|
|id                    |int          |  
|name                  |String       |
|description           |String       | 
|type                  |DeviceType   | 
|group                 |Group        |
|ip                    |String       |
|statusHistorySize     |int          |
|samplingRate          |int          |
|tagIds                |List<Integer>|  
|currentState          |DeviceSecurityState|
|lastAlert             |Alert        |
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`Device()`|
|`Device(String name, String description, DeviceType type, String ip, int statusHistorySize, int samplingRate)`|
|`Device(String name, String description, DeviceType type, Group group, String ip, int statusHistorySize, int samplingRate, DeviceSecurityState currentState, Alert lastAlert)`|
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

#### DeviceCommand
###### Schema:
|Property  |Type      |
|------------:|:---------|
|id           |Integer   |  
|lookupId     |Integer   |
|deviceTypeId |Integer |
|stateId      |Integer       | 
|name         |String    |

###### Constructors:
|Definition |  
|:---|
|`DeviceCommand()`|
|`DeviceCommand(String name)`|
|`DeviceCommand(Integer id, String name)`|
|`DeviceCommand(Integer deviceTypeId, Integer stateId, String name)`|
|`DeviceCommand(Integer id, Integer deviceTypeId, Integer stateId, String name)`|
|`DeviceCommand(Integer id, Integer lookupId, Integer deviceTypeId, Integer stateId)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`    
- `insertOrUpdate`
- `toString()`

#### DeviceSecurityState
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |  
|deviceId  |int       |
|stateId   |int       | 
|timestamp |Timestamp | 
|name      |String    |
###### Constructors:
|Definition |  
|:---|
|`DeviceSecurityState()`|
|`DeviceSecurityState(int deviceId, int stateId)`|
|`DeviceSecurityState(int deviceId, int stateId, String name)`|
|`DeviceSecurityState(int deviceId, int stateId, Timestamp timestamp, String name)`|
|`DeviceSecurityState(int id, int deviceId, int stateId, Timestamp timestamp, String state)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`    
- `toString()`
    
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

#### DeviceType
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
|`DeviceType()`     |
|`DeviceType(int id, String name)`|
|`DeviceType(String name, byte[] policyFile, String policyFileName)`|
|`DeviceType(int id, String name, byte[] policyFile, String policyFileName)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
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
- `insertOrUpdate()`
- `toString()`

#### SecurityState
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |
|name     |String    |
###### Constructors:
|Definition |  
|:---|
|`SecurityState()`|
|`SecurityState(String name)`|
|`SecurityState(int id, String name)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
- `insertOrUpdate()`
- `toString()`

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
- `insertOrUpdate()`
- `toString()`

#### UmboxImage
###### Schema:
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String |
|fileName  |String | 
|dagOrder  |int    |
###### Constructors:
| Definition |  
|:---|
|`UmboxImage()`|
|`UmboxImage(String name, String fileName)`|
|`UmboxImage(String name, String fileName, int dagOrder)`|
|`UmboxImage(int id, String name, String fileName)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
- `insertOrUpdate()`
- `toString()`

#### UmboxInstance
###### Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |String    |  
|alerterId       |String    |
|umboxImageId    |String    |
|deviceId        |int       |
|startedAt       |Timestamp |
###### Constructors:
|Function Definition |
|:---|
|`UmboxInstance()`  |
|`UmboxInstance(String alerterId, int umboxImageId, int deviceId)`|
|`UmboxInstance(String alerterId, int umboxImageId, int deviceId, Timestamp timestamp)`|
|`UmboxInstance(int id, String alerterId, int umboxImageId, int deviceId, Timestamp timestamp)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
- `toString()`

#### UmboxLookup
###### Schema:
|Property        |Type      |
|---------------:|:---------|
|id              |String    |  
|stateId         |Integer    |
|deviceTypeId    |Integer    |
|umboxImageId    |Integer     |
|dagOrder        |Integer |
###### Constructors:
|Function Definition |
|:---|
|`UmboxLookup()`  |
|`UmboxLookup(int id, Integer stateId, Integer deviceTypeId, Integer umboxImageId, Integer dagOrder)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
- `insertOrUpdate()`
- `toString()`

