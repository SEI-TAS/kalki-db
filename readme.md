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
Pull the correct postgres base image from the docker repository `$ docker pull postgres:9.5.19`

Set up a docker network by running `$ docker network create -d bridge kalki_nw`. This initializes a container network. *This only ever needs to be done once*.

Start the database by running `$ ./run_postgres_container.sh` from the project root.
This will create a docker container named `kalki-postgres` so the data persists when the database is terminated.

Stop the docker container via `$ docker kill kalki-postgres`  

To export current database to a SQL file: 
```
$ pg_dump kalkidb -U kalkiuser -h localhost -p 5432 > [filename].sql
    kalkipass
```

To import a database from a SQL file: 
```
$ psql kalkidb -U kalkiuser -h localhost -p 5432 < [filename].sql
    kalkipass
```
* Container must be started in order to import/export

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
|alert_type_lookup_id   |int NOT NULL |  
###### Actions:  
|Function Definition                                 |Return Type|
|:---------------------------------------------------|:--------|
|`findAlertCondition(int id) `                       |`AlertCondition`      |
|`findAlertConditionsByDevice(int deviceId)`         |`List<AlertCondition>`|
|`findAllAlertConditions()`                          |`List<AlertCondition>`|
|`insertAlertCondition(AlertCondition condition)`    |`Integer`           |
|`insertAlertConditionForDevice(int id) `            |`Integer`       |
|`updateAlertConditionsForDeviceType(AlertTypeLookup alertTypeLookup)`|`Integer`|
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
|`deleteAlertType(int id)`                      |`Boolean`|

#### AlertTypeLookup
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|variables       |hstore   |
|device_type_id  |int      |
|alert_type_id   |int      |
###### Actions:  
|Function Definition                                 |Return Type|
|:----------------------------------------------|:--------|
|`findAlertTypeLookup(int id) `                 |`AlertTypeLookup`      |
|`findAlertTypeLookupsByDeviceType(int typeId)  `  |`List<AlertTypeLookup>`|
|`findAllAlertTypeLookups()` |`List<AlertTypeLookup>`|
|`insertAlertTypeLookup(AlertTypeLookup atl)`              |`int`|
|`updateAlertTypeLookup(AlertTypeLookup atl)`              |`int`|
|`insertOrUpdateAlertTypeLookup(AlertTypeLookup alertTypeLookup)`|`int`|
|`deleteAlertTypeLookup(int id)`                      |`Boolean`|

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
|current_state_id        |int NOT NULL|
|previous_state_id       |int NOT NULL|
|device_type_id          |int NOT NULL|
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
|default_sampling_rate |int NOT NULL |
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
|`findPreviousDeviceSecurityStateId(Device device)`       |`int`|
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

#### StageLog
###### Schema:
|Property  |Type  |
|---------:|:------|
|id        |int    |  
|device_sec_state_id|int NOT NULL|
|timestamp | timestamp DEFAULT now()|
|action    | String NOT NULL|
|stage     | String NOT NULL|
|info      | String |
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findStageLog(int id)`              |`StageLog`      |
|`findAllStageLogs()`                |`List<StageLog>`      |
|`findAllStageLogsForDevice(int deviceId)`|`List<StageLog>`      |
|`insertStageLog(StageLog stageLog)`|`int`|

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

#### UmboxLog
###### Schema:
|Property  |Type  |
|---------:|:------|
|id        |int    |  
|alerter_id| String NOT NULL|
|details   | String NOT NULL|
|timestamp | timestamp DEFAULT now()|
###### Actions:
|Function Definition | Return Type |  
|:---|:---| 
|`findUmboxLog(int id)`              |`UmboxLog`      |
|`findAllUmboxLogs()`                |`List<UmboxLog>`      |
|`findAllUmboxLogsForAlerterId(int alerterId)`|`List<UmboxLog>`      |
|`insertUmboxLog(UmboxLog umboxLog)`|`int`|


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
|deviceName      |String|
|alertTypeLookupId  |int   |
|alertTypeName   |String|
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`AlertCondition()`|
|`AlertCondition(Integer deviceId, Integer alertTypeLookupId, Map<String, String> variables)`|
|`AlertCondition(Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName, Map<String, String> variables)`|
|`AlertCondition(int id, Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName, Map<String, String> variables)`|
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
#### AlertTypeLookup
###### Schema:
|Property        |Type     |
|---------------:|:--------|
|id              |int      |  
|alertTypeId     |int      |
|deviceTypeId    |int      |
|variables       |Map<String, String>|
###### Constructors:  
|Definition|
|:-----------------------------------------------|
|`AlertTypeLookup()`|
|`AlertTypeLookup(int alertTypeId, int deviceTypeId, Map<String, String> variables)`|
|`AlertTypeLookup(int id, int alertTypeId, int deviceTypeId, Map<String, String> variables)`|
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
|defaultSamplingRate   |int          |
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
|`Device(int id, String name, String description, int typeId, int groupId, String ip, int statusHistorySize, int samplingRate, int defaultSamplingRate)`|
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
|currentStateId      |int   | 
|previousStateId     |int |
|commandId    |int   |

###### Constructors:
|Definition |  
|:---|
|`DeviceCommandLookup()`|
|`DeviceCommandLookup(int commandId, int currentStateId, int previousStateId)`|'
|`DeviceCommandLookup(int id, int commandId, int currentStateId, int previousStateId)`|
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

#### StageLog
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |
|deviceSecurityStateId |int       |
|timestamp |Timestamp       |
|action    |String    |
|stage     |String    |
|info      |String    |
|Action    |Enum      |
|Stage     |Enum      |
###### Constructors:
|Definition |  
|:---|
|`StageLog()`|
|`StageLog(int devSecStateId, String action, String stage)`|
|`StageLog(int devSecStateId, String action, String stage, String info)`|
|`StageLog(int devSecStateId, Action action, Stage stage, String info)`|
|`StageLog(int id, int deviceSecurityStateId, Timestamp timestamp, String action, String stage, String info)`|
###### Methods:
This class supports:
- `get<field>()`
 - ex: getName()
- `set<field>(<field type> value)`
 - ex: setName("Name")
- `insert()`
- `toString()`
###### Enum Values:
- Action:
    - `INCREASE_SAMPLE_RATE`
    - `SEND_COMMAND`
    - `DEPLOY_UMBOX`
    - `OTHER`
- Stage:
    - `TRIGGER`
    - `REACT`
    - `FINISH`

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

#### UmboxLog
###### Schema:
|Property  |Type      |
|---------:|:---------|
|id        |int       |
|alerter_id |String       |
|timestamp |Timestamp       |
|details    |String    |
###### Constructors:
|Definition |  
|:---|
|`UmboxLog()`|
|`UmboxLog(String alerter_id, String details)`|
|`UmboxLog(int id, String alerter_id, String details, Timestamp timestamp)`|
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




