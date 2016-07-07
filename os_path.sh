################################################################################################
#Oracle2GP的配置信息     
################################################################################################

#Oracle2GP安装的家目录        
OSHOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
export OSHOME

#Oracle2GP图形化界面端口号         
export UIPORT=38090

#gpfdist的端口号，如果在部署后再次修改该端口号，需要重新执行os_install.sh程序         
export OSPORT=38999

#Oracle2GP的jobs端口号范围(确保此端口号没有被占用)                  
export OSPORT_LOWER=31850
export OSPORT_UPPER=31900

#Oracle2GP的custom tables的端口号范围(确保此端口号没有被占用)       
export OSPORT_CUSTOM_LOWER=32000
export OSPORT_CUSTOM_UPPER=33000

#gpfdist的最大行长度，如果程序执行过程中遇到"line too long"错误那么请增加此值            
GPFDISTMAXROW=32768

#Oracle2GP的lock文件路径              
export JOBLOCK=/tmp/oracle2gp-jobstart.lock

#gpfdist程序的超时时间                
export GPFDISTTIMEOUT=30

#Greenplum的yml文件路径        
export YML=$OSHOME/yml/oracle2gp.yml

#Greenplum数据库的配置信息            
export CONFIG=$OSHOME/config.properties

################################################################################################
#Oracle2GP相关日志文件配置            
################################################################################################
#Oracle2GP主程序的日志文件             
export OSLOG=$OSHOME/log/Oracle2GP.log

#Oracle2GP的UI图形化日志文件              
export UILOG=$OSHOME/log/Oracle2GP-UI.log

#Oracle2GP的Agent的日志文件           
export AGENTLOG=$OSHOME/log/Oracle2GP-Agent.log

#Oracle2GP的gpfdist日志文件             
export GPFDISTLOG=$OSHOME/log/Oracle2GP-gpfdist.log

#gpfdist作业的日志的前缀                  
export JOBLOG=$OSHOME/log/job

#自定义的gpfdist日志文件的前缀            
export CUSTOMLOG=$OSHOME/log/custom

#Oracle2GP UI Sessions log file
export SESSIONS=$OSHOME/log/Oracle2GP-sessions.log

################################################################################################
#Java内存配置            
################################################################################################
#Oracle2GP的最小内存          
export XMS=256m

#Oracle2GP的最大内存        
export XMX=768m

################################################################################################
#Oracle2GP所需要的Jar文件          
################################################################################################
#Oracle2GP的Jar包            
OSJAR=$OSHOME/jar/Oracle2GP.jar

#Oracle2GPScheduler的Jar包          
export OSAGENTJAR=$OSHOME/jar/Oracle2GPScheduler.jar

#Oracle2GPUI的Jar包       
export OSUIJAR=$OSHOME/jar/Oracle2GPUI.jar

#GPDB的Jar包         
export GPDBJAR=$OSHOME/jar/gpdb.jar

#Microsoft JDBC的Jar包           
export MSJAR=$OSHOME/jar/sqljdbc4.jar

#Oracle JDBC的Jar包        
export OJAR=$OSHOME/jar/ojdbc6.jar

#Nano的Jar包              
export NANOJAR=$OSHOME/jar/nanohttpd.jar

################################################################################################
#Oracle2GP的PATH配置    
################################################################################################
#Oracle2GP启动的Classpath       
export OSCLASSPATH=$OSJAR\:$MSJAR\:$OJAR\:$GPDBJAR

#Oracle2GP UI启动的Classpath
export OSUICLASSPATH=$OSJAR\:$OSUIJAR\:$MSJAR\:$OJAR\:$GPDBJAR\:$NANOJAR

#Oracle2GP Agent启动的Classpath
export OSAGENTCLASSPATH=$OSJAR\:$OSAGENTJAR\:$GPDBJAR

#设置PATH            
export PATH=$OSHOME/bin:$PATH
