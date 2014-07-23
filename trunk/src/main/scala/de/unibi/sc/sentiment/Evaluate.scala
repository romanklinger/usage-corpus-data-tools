package de.unibi.sc.sentiment

import collection.mutable.{ArrayBuffer, HashMap}
import crawling.{TxtEntry, Entry}
import io.Source


/**
 * Factor Graph oriented Sentiment Analysis
 * User: rklinger
 * Date: 23.07.14
 * Time: 14:17
 */
case class RELEntry(classs:String, internalId:String, phraseId1:String, phraseId2:String, stringRepr1:String, stringRepr2:String) {

}

object Evaluate {
  final val OFFSET = 4
  def main(args:Array[String]) : Unit = {
    System.err.println("USAGE Corpus Offset correction") ;
    System.err.println("Author: )") ;
    System.err.println("Roman Klinger  (rklinger@cit-ec.uni-bielefeld.de)") ;
    System.err.println("This program evaluates predictions with gold annotations.")
    if (args.length < 4) {
      System.err.println("Parameters: GoldCSV GoldRel PredictedCSV PredictedRel")
      System.err.println("All four parameters must be provided. If Rel-files are \n" +
        "not available, state \"null\" at the respective positions");
      System.exit(-1);
    }
    start(args(0),args(1),args(2),args(3))
  }
  def start(goldCsvFilename:String,goldRelFilename:String,predictedCsvFilename:String,predictedRelFilename:String) {
    System.err.println("Reading gold CSV file into memory: "+goldCsvFilename+" ...")
    val goldCSV = readCSVFile(goldCsvFilename)
    val predictedCSV = readCSVFile(predictedCsvFilename)
    printHeader
    evaluatePhrase(goldCSV,predictedCSV,"aspect")
    evaluatePhrase(goldCSV,predictedCSV,"subjective")
    if (predictedRelFilename.toLowerCase != "null" && goldRelFilename.toLowerCase != "null") {

      // evaluateRelation()
    }


    System.err.println("Done.")
  }

  def readCSVFile(csvFilename:String) : ArrayBuffer[Entry] = {
    val csvBuffer = new ArrayBuffer[Entry]()
    for (line <- Source.fromFile(csvFilename).getLines()) {
      val csvEntry = Entry(line)
      csvBuffer += csvEntry
    }
    csvBuffer
  }

  def evaluatePhrase(goldCSV:ArrayBuffer[Entry],predictedCSV:ArrayBuffer[Entry],phraseClass:String) = {
    val goldCSVForAnalysis = goldCSV.filter(entry => entry.classs == phraseClass)
    var tp = 0
    var fp = 0
    var fn = 0
    for (prediction <- predictedCSV.filter(entry => entry.classs == phraseClass)) {
      val fittingGoldPhrase = goldCSVForAnalysis.find(goldEntry => goldEntry.matches(prediction))
      if (fittingGoldPhrase.isDefined) {
        tp += 1
        goldCSVForAnalysis -= fittingGoldPhrase.get
      } else {
        fp += 1
      }
    }
    fn = goldCSVForAnalysis.length
    printResult(phraseClass,tp,fp,fn)
  }

  def printResult(prefix:String,tp:Double,fp:Double,fn:Double) = {
    val precision:Double = if (tp+fp > 0) tp / (tp + fp) else 1
    val recall:Double = if (tp + fn > 0) tp / (tp + fn) else 0
    val f = (2 * precision * recall) / (precision + recall)
    println(prefix+"\t"+"%1.0f\t%1.0f\t%1.0f\t%1.3f\t%1.3f\t%1.3f".format(tp,fp,fn,precision,recall,f))
  }

  def printHeader = println("Class"+"\t"+"tp"+"\t"+"fp"+"\t"+"fn"+"\t"+"preci"+"\t"+"recall"+"\t"+"f1")
}
