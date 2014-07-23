* Introduction *

This project contains tools to be used with the USAGE corpus
(see http://dx.doi.org/10.4119/unibi/citec.2014.14). It is 
maintained at 
https://code.google.com/p/usage-corpus-data-tools/

* Compilation *

For compilation, you should have Maven installed on your machine.
Then compile:

mvn compile

and package everything into one jar-file:

mvn assembly:single


* Run *

The following tools are available in this project:

** Amazon Review Crawler **

Given a tab separated file with internat IDs in the first column,
Amazon product IDs in the second column and Amazon review IDs in the
third column, you can crawl the respective reviews. Only the review ID
is used for that.

For your convinience, you can call
./bin/crawl.sh INPUTFILE DOMAIN OUTPUTFILE [breaks]

The domain option can be de or com.

An example would be
./bin/crawl.sh example/de-input.txt de example/de-output.txt breaks
or
./bin/crawl.sh example/de-input.txt de example/de-output.txt

If the "breaks" parameter is given, <br /> tags in the retrieved
review are replaced by one space symbol. Otherwise, they are replaced
by an empty string.

** Offset correction **

The Amazon reviews might change a bit over time and encoding issues
might occur. To still be able to use the annotations in the USAGE
corpus, the program to be called with

./bin/correctOffsets.sh INPUTTEXTFILE CSVFILE

An example would be

./bin/correctOffsets.sh example/en-with-text.txt example/en-with-text.csv > example/en-with-text-corrected.csv

The new CSV file is written to standard out.

Searches for the annotated string as specified in a CSV file in a
small proximity and adapts the left and right offset. This is
especially useful when using the "breaks" parameter, as the CSV files
have been annotated without white spaces representing line breaks.

** Evaluation **

For the IGGSA Shared Task, Second Main Task (STAR), the evaluation
program is part of this project as well. You can call it 

./bin/evaluate.sh GOLDCSV GOLDREL PREDICTEDCSV PREDICTEDREL

for example:

./bin/evaluate.sh example/en-with-text.csv example/en-with-text.rel example/en-with-text-predicted.csv example/en-with-text-predicted.rel 

