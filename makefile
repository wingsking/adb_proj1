all: run
run: run.sh
	./run.sh $(ACCOUNT) $(PRECISION) "$(QUERY)"
clean:
	rm -r bin

