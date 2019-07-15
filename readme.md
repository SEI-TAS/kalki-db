# KalkiDB
* [Usage](#usage)
* [Code Integration](#code-integration)
* [Models & Actions](#models-&-actions)
    * [Postgres Tables](#postgres-tables)
    * [Java Objects](#java-objects)
    * [Insert Notifications](#insert-notifications)

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
|device_id       |int      |
|alert_type_id   |int      |
|alerter_id      |String   |
|device_status_id|int      |  
###### Actions:  
|Function Definition                             |Return Type|
|:-----------------------------------------------|:--------|
|`findAlert(int id) `                             |`Alert`      |
|`findAlerts(List<String> alerterIds)`            |`List<Alert>`|  
|`findAlertsByDevice(Integer deviceId)`           |`List<Alert>`|  
|`insertAlert(Alert alert)`                       |`Integer`           |
|`updateAlert(Alert alert)`                       |`Integer`           |
|`deleteAlert(int id)`                            |`Boolean`           |

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
|`findAlertCondition(int id) `                       |`AlertCondition`      |
|`findAlertConditionsByDevice(int deviceId)`         |`List<AlertCondition>`|
|`findAllAlertConditions()`                          |`List<AlertCondition>`|
|`insertAlertCondition(AlertCondition condition`     |`Integer`           |
|`insertAlertConditionByDeviceType(AlertCondition cond) `|`Integer`       |
|`updateAlertCondition(AlertCondition condition)`    |`Integer`           |
|`insertOrUpdateAlertCondition(AlertCondition condition)`| `Integer`      |
|`deleteAlertCondition(int id)`                      |`Boolean`           |

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
|`findAlertType(int id) `                       |`AlertType`      |
|`findAllAlertTypes()  `                        |`List<AlerType>`                  |
|`findAlertTypesByDeviceType(int deviceTypeId)` |`List<AlertType>`|
|`insertAlertType(AlertType type)`              |`Integer`|
|`updateAlertType(AlertType type)`              |`Integer`|
|`insertOrUpdate(AlertType type)`               |`Integer`|
|`deleteAlertType(int id)`                      |`CompletionStage<Boolean>`|

#### Command
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |serial PRIMARY KEY|
|name            |String   |
|device_type_id  |int NOT NULL|
###### Actions:  
|Function Definition                   |Return Type|
|:-------------------------------------|:--------|
|`findCommand(int id)`           |`DeviceCommand`|
|`findAllCommands()`|`List<DeviceCommand>`|
|`findCommandsByDevice(Device device)` |`List<DeviceCommand>`|
|`insertCommand(DeviceCommand command)`|`Integer`|
|`insertOrUpdateCommand(DeviceCommand command)`|`Integer`|
|`updateCommand(DeviceCommand command)`|`Integer`|
|`deleteCommand(int id)`|`Boolean`|

#### CommandLookup
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |serial PRIMARY KEY|  
|state_id        |int NOT NULL|
|command_id      |int NOT NULL|
###### Actions:  
|Function Definition                   |Return Type|
|:-------------------------------------|:--------|
|`findCommandLookup(int id)`           |`DeviceCommandLookup`|
|`findCommandLookupsByDevice(int deviceId)`    |`List<DeviceCommandLookup>`|
|`findAllCommandLookups()`             |`List<DeviceCommandLookup>`|
|`insertCommandLookup(DeviceCommandLookup commandLookup)`|`int`|
|`insertOrUpdateCommandLookup(DeviceCommandLookup commandLookup)`|`Integer`|
|`updateCommandLookup(DeviceCommandLookup commandLookup)`|`Integer`|
|`deleteCommandLookup(int id)`|`Boolean`|

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
|`findDevice(int id)`                  |`Device`       |
|`findAllDevices()`                    |`List<Device>` |  
|`findDevicesByGroup(int groupId)`     |`List<Device>`                  |
|`findDevicesByType(int typeId)`       |`List<Device>`                  |
|`insertDevice(Device device)`         |`Integer`      |
|`insertOrUpdateDevice(Device device)` |`Integer`      | 
|`updateDevice(Device device)`         |`Integer`      |
|`deleteDevice(int id)`                |`Boolean`      |
|`resetSecurityState(int id)`          |`DeviceSecurityState`      |


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
|`findDeviceSecurityStates(int deviceId)`                 |`List<DeviceSecurityState>`|
|`insertDeviceSecurityState(DeviceState deviceState)`     |`Integer`           |
|`deleteDeviceSecurityState(int id)`                      |`Boolean`           |

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
|`findDeviceStatus(int id)`                                             |`DeviceStatus>`      |
|`findDeviceStatuses(int deviceId)`                                     |`List<DeviceStatus>`|
|`findNDeviceStatuses(int deviceId, int N)`                             |`List<DeviceStatus>`|
|`findDeviceStatusesOverTime(int deviceId, int length, String timeUnit)`|`List<DeviceStatus>`|
|`findDeviceStatusesByType(int typeId)`                                 |`Map<Device, DeviceStatus>`|
|`findDeviceStatusesByGroup(int groupId)`                               |`Map<Device, DeviceStatus>`|
|`findAllDeviceStatuses()`                                              |`List<DeviceStatus>`|
|`insertDeviceStatus(DeviceStatus deviceStatus)`                        |`Integer`           |
|`insertOrUpdateDeviceStatus(DeviceStatus deviceStatus)`                |`Integer`           |
|`updateDeviceStatus(DeviceStatus deviceStatus)`                        |`Integer`           |
|`deleteDeviceStatus(int id)`                                           |`Boolean`           |

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
|`findDeviceType(int id)`     |`DeviceType>`      |
|`findAllDeviceTypes()`       |`List<DeviceType>`|
|`insertDeviceType(DeviceType type)`|`Integer`   |
|`updateDeviceType(DeviceType type)`|`Integer`   |
|`deleteDeviceType(int id)`   |`Boolean`   |

#### Group
###### Schema:
|Property     |Type     |
|------------:|:--------|
|id           |int      |  
|name         |String NOT NULL |
###### Actions:  
|Function Definition | Return Type |  
|:---|:---| 
|`findGroup(int id)`        |`Group>`      |
|`findAllGroups()`          |`List<Group>`|
|`insertGroup(Group group)` |`Integer`    |
|`updateGroup(Group group)` |`Integer`    |
|`deleteGroup(int id)`      |`Boolean`    |

#### SecurityState
###### Schema:
|Property  |Type     |
|---------:|:------|
|id        |int |  
|name      |String NOT NULL|
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findSecurityState(int id)`                |`SecurityState`      |
|`findAllSecurityStates()`                  |`List<SecurityState>` |
|`insertSecurityState(SecurityState state)` |`Integer`            |
|`updateSecurityState(SecurityState state)` |`Integer`            |
|`deleteSecurityState(int id)`              |`Boolean`            |

#### Tag
###### Schema:
|Property   |Type   |
|----------:|:------|
|id         |int    |  
|name       |String NOT NULL|
###### Actions:
|Function Definition | Return Type |  
|:---|:---|
|`findAllTags()`           |`List<Tag>`|
|`findTagIds(int deviceId)`|`List<Integer>`             |
|`insertTag(Tag tag)`      |`Integer`  |
|`updateTag(Tag tag)`      |`Integer`  |
|`deleteTag(int id)`       |`Boolean`  |

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
|`findUmboxImage(int id)`         |`UmboxImage`      |
|`findUmboxImagesByDeviceTypeAndSecState(int devTypeId, int secStateId)`|`List<UmboxImage>`|
|`findAllUmboxImages()`           |`List<UmboxImage>`|
|`insertUmboxImage(UmboxImage u)` |`Integer`            |
|`updateUmboxImage(UmboxImage u)` |`Integer`            |
|`deleteUmboxImage(int id)`       |`Boolean`            |

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
|`findUmboxInstance(String alerterId)`  |`UmboxInstance`|
|`findUmboxInstances(int deviceId)`     |`List<UmboxInstance>`|
|`insertUmboxInstance(UmboxInstance u)` |`Integer`|
|`updateUmboxInstance(UmboxInstance u)` |`Integer`|
|`deleteUmboxInstance(int id)`          |`Boolean`|

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
|`findUmboxLookupsByDevice(int deviceId)`  |`List<UmboxLookup>`|
|`findAllUmboxLookups()`  |`List<UmboxLookup>`|
|`insertUmboxLookup(UmboxLookup ul)`  |`Integer`|
|`updateUmboxLookup(UmboxLookup ul)`  |`Integer`|
|`insertOrUpdateUmboxLookup(UmboxLookup ul)`  |`Integer`|
|`deleteUmboxLookup(int id)`  |`Boolean`|



### Java Objects
#### Alert
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|name            |String   |
|timestamp       |Timestamp|
|deviceId        |int      |
|alerterId       |String   |
|deviceStatusId  |Integer  |
|alertTypeId     |int      |
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`Alert()`|
|`Alert(String name, int deviceId, int alertTypeId)`|
|`Alert(String name, String alerterId, int alertTypeId)`|
|`Alert(String name, Integer deviceStatusId, int alertTypeId)`|
|`Alert(String name, Timestamp timestamp, String alerterId, Integer deviceStatusId, int alertTypeId)`|
|`Alert(int id, String name, Timestamp timestamp, String alerterId, int deviceId, Integer deviceStatusId, int alertTypeId)`|
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
- `resetSecurityState()`
- `toString()`
- `lastNSamples(int N)`
    - returns `<List<DeviceStatus>>`
- `samplesOverTime(int length, String timeUnit)`
    - returns `<List<DeviceStatus>>`
- `statusesOfSameType()`
    - returns `<Map<Device, DeviceStatus>>`

#### DeviceCommand
###### Schema:
|Property  |Type      |
|------------:|:---------|
|id           |int       | 
|name         |String    | 
|deviceTypeId |Integer   |

###### Constructors:
|Definition |  
|:---|
|`DeviceCommand()`|
|`DeviceCommand(String name, Integer deviceTypeId)`|
|`DeviceCommand(int id, String name, Integer deviceTypeId)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`    
- `insertOrUpdate`
- `toString()`

#### DeviceCommandLookup
###### Schema:
|Property  |Type      |
|------------:|:---------|
|id           |int       |  
|stateId      |Integer   | 
|commandId    |Integer   |

###### Constructors:
|Definition |  
|:---|
|`DeviceCommandLookup()`|
|`DeviceCommandLookup(Integer stateId, Integer commandId)`|
|`DeviceCommandLookup(int id, Integer stateId, Integer commandId)`|
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

### Insert Notifications

When you run the setupDatabase() method, triggers are created that generate postgres notifications.  
These triggers generate notifications on the following conditions.

##### Notification Conditions:
|Table Name            |Action      |Notification Name           |
|---------------------:|:-----------|:-------------------------- |
|alert                 |INSERT      |`alertinsert`               |
|device_security_state |INSERT      |`devicesecuritystateinsert` |
|device_status         |INSERT      |`devicestatusinsert`        |
|device                |INSERT      |`deviceinsert`              |

Without taking any additional steps, the notifications will be generated with nothing listening.
You can start listening to these notifications which will add the necessary handlers to a notification 
listener and check for new notifications once every second.  To start listening to notifications, call
```
Postgres.startListener();
```
You can also stop listening by calling
```
Postgres.stopListener();
```
Once you start listening, you can retrieve all of the newest insertions per table by calling one of the following methods
##### Notification Content Retrieval Methods:
|Method                   |Return Type                  |
|------------------------:|:----------------------------|
|Postgres.getNewAlerts()  | `List<Alert>`               | 
|Postgres.getNewStates()  | `List<DeviceSecurityState>` |
|Postgres.getNewStatuses()| `List<DeviceStatus>`        |

Each of these methods will return all of the insertions for its given table since the last time the method was called,
or if there are no insertions it will return null.




