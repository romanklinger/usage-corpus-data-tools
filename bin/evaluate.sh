#!/bin/bash
here=`dirname $0`
param=$@
targetPath=${here}/../target/recrawling-0.5-jar-with-dependencies.jar 
java -Xmx2g -cp ${targetPath} de.unibi.sc.sentiment.Evaluate $param
