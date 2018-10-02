all:
	+$(MAKE) -C emailclient
	+$(MAKE) -C emailserver
	+$(MAKE) -C keygen
	+$(MAKE) -C emailclient manifest
	+$(MAKE) -C emailserver	manifest
	+$(MAKE) -C keygen manifest
	+$(MAKE) -C emailclient jar
	+$(MAKE) -C emailserver	jar
	+$(MAKE) -C keygen jar

clean:
	+$(MAKE) -C emailclient clean
	+$(MAKE) -C emailserver clean
	+$(MAKE) -C keygen clean