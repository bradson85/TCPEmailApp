JFLAGS = -g -d ../emailclient
JC = javac
.SUFFIXES: .java .class
.java.class:
	mkdir -p ./emailclient
	$(JC) $(JFLAGS) $*.java
	

CLASSES = \
        *.java \

default: classes


classes: $(CLASSES:.java=.class)

manifest:
	@echo "Main-Class: emailclient.Driver" >> manifest.txt

jar: $(classes)
	jar cvfm ../client.jar manifest.txt emailclient/*.class 
clean:
	$(RM) emailclient/*.class
	$(RM) ../client.jar
	$(RM) manifest.txt
	$(RM) -rf  emailclient
