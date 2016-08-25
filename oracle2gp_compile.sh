#!/bin/bash
set -e

#JAVA_HOME
java_home=`echo $JAVA_HOME`
if [ -z ${java_home} ]
then
    echo "Please install Java Development Kit(JDK), for example: 1.7.0_67 or 1.7.0_80 version"
    exit
else
    java -version > jdk_version_file 2>&1
    jdk_version=`cat jdk_version_file | grep -m 1 "java version" | awk -F\" '{print $2}'`
    rm -f jdk_version_file
fi

echo "Manifest-Version: 1.0" > manifest.txt
echo "Main-Class: ExternalData" >> manifest.txt
echo "Specification-Title: \"Outsourcer\"" >> manifest.txt
echo "Specification-Version: \"1.0\"" >> manifest.txt
echo "Created-By: ${jdk_version}" >> manifest.txt
d=`date +"%Y-%m-%d %H:%M:%S"`
echo "Build-Date: $d" >> manifest.txt

chmod -R +x *

cd src
#javac -cp .:jar/gpdb.jar *.java
javac -Xbootclasspath:${java_home}/jre/lib/rt.jar -source 1.7 -target 1.7 -cp .:../jar/gpdb.jar *.java 
jar cfm ../jar/Oracle2GP.jar manifest.txt Logger.class CommonDB.class Oracle.class SQLServer.class ExternalData.class CustomSQL.class GP.class ExternalDataD.class ExternalDataThread.class OSProperties.class GpfdistRunner.class
jar cfm ../jar/Oracle2GPUI.jar manifest.txt *Model.class *View.class *Control.class UI*.class ServerRunnerUI.class
jar cfm ../jar/Oracle2GPScheduler.jar manifest.txt AgentD.class

cd ..
#bin/stop_all
#sleep 2
#bin/start_all
