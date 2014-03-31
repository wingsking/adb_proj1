#!/bin/bash

mkdir -p bin
CP1="src:commons-codec-1.9.jar:htmlparser.jar:org-json.jar:"
CP2="bin:commons-codec-1.9.jar:htmlparser.jar:org-json.jar:"
javac -d bin/ -cp ${CP1} src/*/*java
java -cp ${CP2}  bing.BingSearch $1 $2 "$3"
