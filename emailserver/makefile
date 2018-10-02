JFLAGS = -g -d ../emailserver
JC = javac
.SUFFIXES: .java .class
.java.class:
	mkdir -p ./emailserver
	$(JC) $(JFLAGS) $*.java
	

CLASSES = \
        *.java \

default: classes


classes: $(CLASSES:.java=.class)

manifest:
	@echo "Main-Class: emailserver.Driver" >> manifest.txt

jar: $(classes)
	jar cvfm ../server.jar manifest.txt emailserver/*.class 
clean:
	$(RM) emailserver/*.class
	$(RM) ../server.jar
	$(RM) manifest.txt
	$(RM) db/.user_pass
	$(RM) -rf emailserver



	