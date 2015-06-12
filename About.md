# Introduction #

This project will consist of additional tools to be used on the context of the data published at
http://dx.doi.org/10.4119/unibi/citec.2014.14

# Download and Use #

Check out the code anonymously by:

```
svn checkout http://usage-corpus-data-tools.googlecode.com/svn/trunk/ usage-corpus-data-tools-read-only
```

Then change to the directory:

```
cd usage-corpus-data-tools-read-only
```

Then compile the tool and assembly it in a jar file (you need to have Maven (http://maven.apache.org/) for that):

```
mvn compile
mvn assembly:single
```

Then you can run the Amazon crawler on the example data by

```
java -jar target/recrawling-0.5-jar-with-dependencies.jar example/de-input.txt de example/de-output.txt
```

or, for English:

```
java -jar target/recrawling-0.5-jar-with-dependencies.jar example/en-input.txt com example/en-output.txt
```

Alternatively, the script crawl.sh should do the Java call for you.

For more information, please refer to dx.doi.org/10.4119/unibi/citec.2014.14