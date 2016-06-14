# FIMS v1.1 RESTful Reference Implementation

This folder contains the REST sample implementation for the FIMS v1.1 version of the specification.
This is the first version of FIMS with support for REST and adds a JSON data binding alongside an XML
data binding.

The implementation is written in Scala and has been tested on the Java v1.7 platform. The code is 
licensed under an open source Apache 2.0 license.

The implementation is for a server that can run mock jobs and pretend to be a repository, suitable for
study and use in testing by developers but not intended for day-to-day professional deployment. It is not
a full implementation of a media service and cannot capture, transfer, transform or store content.

## Running the server

To run the server, you need to have a Java runtime installed. This implementation has been tested on
a v1.7 platform. All other required libraries are included with the distribution. This implementation
should run on Windows, Mac and Linux systems.

Download and install a Java runtime from http://www.java.com

To run the server, open a terminal and unzip the distribution into new folder/directory. Open a terminal
or command shell and cd to that directory. You can choose to run the implementation in two different 
containers: an Apache CXF container (http://cxf.apache.org/); the Oracle Jersey (https://jersey.java.net/)
RESTful reference implementation. On Linux or Mac, type one of ...

1. ./fims-rest-cxf.sh
2. ./fims-rest-jersy.sh

On Windows, type one of ...

1. fims-rest-cxf.bat
2. fims-rest-jersey.bat

You need to interact with the server using a web client, such as a web browser, your favourite HTTP programming 
library or a command line tool like curl (http://curl.haxx.se/). The server runs on port 9000. To check whether 
the server is working OK, browser to "http://localhost:9000/api/job". You should see an XML response like ...

<bms:jobs detail="full" totalSize="0"/>

To stop the server, type Ctrl-C.

## Examples

### XML fragments to try

For more detailed exploration of what the server can do, consider installing a REST testing utility in your
browser, such as Chrome Poster (https://github.com/dengzhp/chrome-poster). Such a tool can be used to try the 
following steps:

1. Create a content placeholder by posting the following to "http://localhost:9000/api/content":

```xml
<bms:bmContent xmlns:bms="http://base.fims.tv" 
    xmlns:desc="http://description.fims.tv"   
    xmlns:xml="http://www.w3.org/XML/1998/namespace"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <bms:resourceID>urn:uuid:11b5293f-1be1-445a-997a-caa6f2b2bee4</bms:resourceID>
  <bms:descriptions>
    <bms:description>
      <bms:resourceID>urn:uuid:3e4faa49-97c9-49bc-9db2-41af36d67d2b</bms:resourceID>
      <bms:ExtensionGroup>
        <isa:Attributes xmlns:isa="http://isa.quantel.com">
          <isa:Title>Games of the century</isa:Title>
          <isa:Category>NTSC</isa:Category>
          <isa:Owner>AMWA EBU History</isa:Owner>
          <isa:Description>Best games of the past 100 years.</isa:Description>
        </isa:Attributes>
      </bms:ExtensionGroup>
    </bms:description>
  </bms:descriptions>
</bms:bmContent>
```

2. Check that the placeholder has been received and stored in memory with URL:

```
http://localhost:9000/api/content/11b5293f-1be1-445a-997a-caa6f2b2bee4
```

3. Edit the following XML fragment to set a job start time (bms:startJob) in the near future. Note
that "Z" time is UTC Zulu time, also known as GMT, so you may need to adjust for your
timezone and daylight savings time. Create a mock capture job by posting the following 
XML fragment to "http://localhost:9000/api/job".

```xml
<bms:job xsi:type="cms:CaptureJobType" xmlns:cms="http://capturemedia.fims.tv" 
    xmlns:bms="http://base.fims.tv" xmlns:desc="http://description.fims.tv" 
    xmlns:xml="http://www.w3.org/XML/1998/namespace" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <bms:resourceID>urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600</bms:resourceID>
  <bms:bmObjects>
    <bms:bmObject>
      <bms:resourceID>urn:uuid:c57dd600-e99e-46e2-9d04-ba1b46885888</bms:resourceID>
      <bms:bmContents>
        <bms:bmContent>
          <bms:resourceID>
            urn:uuid:11b5293f-1be1-445a-997a-caa6f2b2bee4
          </bms:resourceID>
        </bms:bmContent>
      </bms:bmContents>
    </bms:bmObject>
  </bms:bmObjects>
  <bms:priority>medium</bms:priority>
  <bms:startJob xsi:type="bms:StartJobByTimeType">
    <bms:time>2014-03-13T17:32:00.00Z</bms:time>
  </bms:startJob>
  <profiles>
    <captureProfile> <!-- Reference to HBR profile -->
      <bms:resourceID>urn:uuid:30bdb5ca-66e5-4b7a-b2fd-23f9569e82e2</bms:resourceID>
    </captureProfile>
  </profiles>
  <startProcess xsi:type="bms:StartProcessByNoWaitType"></startProcess>
  <stopProcess xsi:type="bms:StopProcessByDurationType">
    <bms:duration>
      <bms:normalPlayTime>PT2M</bms:normalPlayTime>
    </bms:duration>
  </stopProcess>
  <sourceID>rtp://224.0.0.1:5000/</sourceID>
  <sourceType>uncontrolled</sourceType>
</bms:job>
```

4. A mock job actor will run a fake capture job for two minutes from the start time given.
Watch the console output, check the log file in the logs folder and youu can check the status
of the job with ...

```
http://localhost:9000/api/job/c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600?detail=summary
```

### Examine the tests

The test files provided with this distribution are intended to be both runnable and informative. These can be
below the "src/test/scala/tv/amwa/ebu/fims/rest/" folder. In particular, see the XMLExampleSpec provided in the 
following file:

```
src/test/scala/tv/amwa/ebu/fims/rest/rest/XMLExamplesSpec.scala
```

As a side effect, this test prints fragments of XML that represent messages as sent to or from the server along
with a short explanation. 

In a system with the a full Java Development Kit and maven (http://maven.apache.org/) installed, type "mvn test"
to run the tests.

## Compiling the source code and development

The source code for the server is provided. You can build the code from source using the maven build tool that 
uses the file "pom.xml". This distribution was made by running "mvn package". For more information on using maven, 
see http://maven.apache.org/.

To extend the code provided with your own service implementation, consider extending the ServiceEngine trait. A
Scala trait is similar to a Java interface. See file ...

```
src/main/scala/tv/amwa/ebu/fims/rest/engine/ServiceEngine.scala
```

## TODO

The following operations have not yet been implemented. It is planned to add these items to this implementation at 
some point in the future:

* The implementation has limited support for fault mapping codes and does not have coverage of all possible system
and application-level error conditions.
* The following repository operations are not supported: GenerateUniqueID, AddEssencePlaceholder, AddEssence, 
CancelAddEssence, ReplaceContent, UpdateContentProperties, Lock, UnLock, ClearLock, GetLocks, RemoveEssence,
UnRemoveEssence, RemoveContent, UnRemoveContent, PurgeEssence, CancelPurgeEssence, PurgeContent, CancelPurgeContent,
RetrieveEssence, CancelRetrieveEssence, ContentQuery.
* The RCR (Repository Capabilities Registry) is not supported in this implementation.
* No queue operations are supported, but the serialization of queue-related resources to and from XML and JSON is.
* Similarly, no transfer or tranform operations are supported, but code for the serialization of transfer and transform 
resources to and from XML and JSON is.
* The following capture operations are not fully supported: QueryJob



