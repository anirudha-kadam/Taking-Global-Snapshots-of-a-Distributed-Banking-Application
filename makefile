JFLAGS = -g
JC = javac
JVM= java
FILE=
.SUFFIXES: .java .class
.java.class:
	$(JC) -classpath $(JFLAGS) $*.java
CLASSES = \
	ChatClientSocket.java \
	ChatReceiveWorker.java \
	ChatSendWorker.java \
	ClientDriver.java \
	ChatData.java \
	ChatQueueWorker.java \
	ChatServerSocket.java \
	ChatServerWorker.java \
	ServerDriver.java \
	Message.java
    
MAIN = Main

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean:
	$(RM) *.class