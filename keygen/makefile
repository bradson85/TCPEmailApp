JFLAGS = -g -d ../keygen
JC = javac
.SUFFIXES: .java .class
.java.class:
	mkdir -p ./keygen
	$(JC) $(JFLAGS) $*.java
	

CLASSES = \
        *.java \

default: classes


classes: $(CLASSES:.java=.class)

manifest:
	@echo "Main-Class: keygen.Driver" >> manifest.txt

jar: $(classes)
	jar cvfm ../keygen.jar manifest.txt keygen/*.class 
clean:
	$(RM) keygen/*.class
	$(RM) ../keygen.jar
	$(RM) manifest.txt
	