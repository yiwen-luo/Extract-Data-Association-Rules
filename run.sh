#!/bin/bash

DIR=$(dirname $0)

java -classpath ${DIR}/target/ExtractDataAssociationRules-1.0-jar-with-dependencies.jar Main $@
