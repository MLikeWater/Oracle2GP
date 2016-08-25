#!/bin/bash
set -e

echo "Manifest-Version: 1.0" > manifest.txt
echo "Main-Class: ExternalData" >> manifest.txt
echo "Specification-Title: \"Oracle2GP\"" >> manifest.txt
echo "Specification-Version: \"1.0\"" >> manifest.txt
echo "Created-By: 1.7.0_80-b15" >> manifest.txt
d=`date +"%Y-%m-%d %H:%M:%S"`
echo "Build-Date: $d" >> manifest.txt

#修改权限    
chmod -R +x *

# 源码目录     
cd src
#javac -cp .:jar/gpdb.jar *.java
javac -Xbootclasspath:/usr/java/jdk1.7.0_80/jre/lib/rt.jar -source 1.7 -target 1.7 -cp .:../jar/gpdb.jar *.java 
jar cfm ../jar/Oracle2GP.jar manifest.txt Logger.class CommonDB.class Oracle.class SQLServer.class ExternalData.class CustomSQL.class GP.class ExternalDataD.class ExternalDataThread.class OSProperties.class GpfdistRunner.class
jar cfm ../jar/Oracle2GPUI.jar manifest.txt *Model.class *View.class *Control.class UI*.class ServerRunnerUI.class
jar cfm ../jar/Oracle2GPScheduler.jar manifest.txt AgentD.class
