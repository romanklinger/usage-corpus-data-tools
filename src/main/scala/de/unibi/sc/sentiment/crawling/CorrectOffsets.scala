package de.unibi.sc.sentiment.crawling

import collection.mutable.{ArrayBuffer, HashMap}
import io.Source


/**
 * Factor Graph oriented Sentiment Analysis
 * User: rklinger
 * Date: 19.03.14
 * Time: 15:17
 */
object Entry { // csventry
  def apply(line:String) = {
    val s = line.split("\t")
    if (s.length > 6)
      new Entry(s(0).trim,s(1).trim,s(2).toInt,s(3).toInt,s(4).trim,s(5).trim,s(6).trim,s(7).trim)
    else
      new Entry(s(0).trim,s(1).trim,s(2).toInt,s(3).toInt,s(4).trim,s(5).trim,"","")
  }
  def apply() = new Entry("","",-1,-1,"","","","")
}
object TxtEntry {
  def apply(line:String) = {
    val s = line.split("\t")
    new TxtEntry(s(0),s(1),s(2),s(3),s(4),s(5))
  }
}

object RELEntry {
  def apply(line:String) = {
    val s = line.split("\t")
    //TARG-SUBJ       1156    1156-aspect3    1156-subjective2        old white Wal-Mart microwave    Sure beats
    new RELEntry(s(0).trim,s(1).trim,s(2).trim,s(3),s(4).trim,s(5).trim)
  }
}

case class RELEntry(classs:String, internalId:String, phraseId1:String, phraseId2:String, stringRepr1:String, stringRepr2:String) {
  def matches(otherRELEntry:RELEntry,otherCSVEntries:ArrayBuffer[Entry],thisCSVEntries:ArrayBuffer[Entry]) : Boolean = {
    // get offsets for both participating phrases and then check if they are the same in the other REL
    val phraseOneOption = thisCSVEntries.find(csvEntry => csvEntry.annotationId == phraseId1)
    val phraseTwoOption = thisCSVEntries.find(csvEntry => csvEntry.annotationId == phraseId2)
    if (phraseOneOption.isDefined && phraseTwoOption.isDefined) {
      // now let's see, if these offsets fit to to the otherRELEntry
      val otherphraseOneOption = otherCSVEntries.find(csvEntry => csvEntry.annotationId == otherRELEntry.phraseId1)
      val otherphraseTwoOption = otherCSVEntries.find(csvEntry => csvEntry.annotationId == otherRELEntry.phraseId2)
      otherphraseOneOption.isDefined && otherphraseTwoOption.isDefined &&
      (
        (phraseOneOption.get.matches(otherphraseOneOption.get) && phraseTwoOption.get.matches(otherphraseTwoOption.get))
        ||
        (phraseOneOption.get.matches(otherphraseTwoOption.get) && phraseTwoOption.get.matches(otherphraseOneOption.get)) // might happen with participants data, though not in my code
      )
    } else {
      false
    }
  }
}

/*
2236    B000ALVUM6      143     Philips HD7546/20 Thermo Kaffeemaschine (1000 W, Tropf-Stopp Funktion) schwarz/Metall   Preis Leistung Weltklasse       Die Kaffeemaschine wurde für das Büro angeschafft. Seit geraumer Zeit ist das gerät im Dauereinsatz ( ca 8 Brühvorgänge ( 8 Liter ) pro Tag.)Bislang keine Mängel erkennbar. Entgegen der AMAZON beschreibung, schaltet sich die Maschine auch von selber aus.das H
 */
case class Entry(classs:String, internalId:String, var leftOffset:Int, var rightOffset:Int, stringRepr:String, annotationId:String, foreigness:String, relatedness:String) {
  override def toString() = classs+"\t"+internalId+"\t"+leftOffset+"\t"+rightOffset+"\t"+stringRepr+"\t"+annotationId+"\t"+foreigness+"\t"+relatedness

  def matches(text:String) = {
     rightOffset <= text.length && text.substring(leftOffset,rightOffset) == stringRepr
  }

  def matches(otherEntry:Entry) : Boolean = {
    (classs == otherEntry.classs) && (internalId == otherEntry.internalId) &&
      (
        (
          (leftOffset == otherEntry.leftOffset) && (rightOffset == otherEntry.rightOffset) // exact
        )
        ||
        (
          // not exact, but string is right
          (scala.math.abs(leftOffset - otherEntry.leftOffset) < 4) && (scala.math.abs(rightOffset - otherEntry.rightOffset) < 4) && stringRepr == otherEntry.stringRepr
        )
      )
  }

  def isNonNull = { leftOffset != -1 }

  def toString(txt:String) = {
    val newStringRepr = if (rightOffset < txt.length) txt.substring(leftOffset,rightOffset) else stringRepr+"[OUT-OF-LENGTH]"
    classs+"\t"+internalId+"\t"+leftOffset+"\t"+rightOffset+"\t"+newStringRepr+"\t"+annotationId+"\t"+foreigness+"\t"+relatedness
  }

  def offsetsMinusValue(value:Int) = {
    leftOffset -= value
    rightOffset -= value
  }
}
case class TxtEntry(internalId:String, productId:String, reviewId:String, name:String, reviewTitle:String, reviewText:String)

object CorrectOffsets {
  final val OFFSET = 4
  def main(args:Array[String]) : Unit = {
    System.err.println("USAGE Corpus Offset correction") ;
    System.err.println("Author: )") ;
    System.err.println("Roman Klinger  (rklinger@cit-ec.uni-bielefeld.de)") ;
    System.err.println("This program corrects possible offset errors based on encoding issues when crawling reviews.")
    if (args.length < 2) {
      System.err.println("Please specify an input text file and the according CSV file with offset annotations.")
      System.err.println("The text file should have the format:");
      System.err.println("InternalId    ProductId      InternalReviewID      ProductTitle   ReviewTitelAndText");
      System.err.println("The offsets are counted from the 5th column on.");
      System.err.println("----------------------------------------------");
      System.exit(-1);
    }
    val minusTitleLength = args.length > 2 && (args(2) == "minusTitleLength") // hidden parameter
    start(args(0),args(1),minusTitleLength)
  }
  def start(txtFile:String,csvFile:String,minusTitleLength:Boolean) {
    System.err.println("Reading text file into memory: "+txtFile+" ...")
    val internalIdText = readTxtFile(txtFile)
    val internalIdProductTitle = if (minusTitleLength) readTxtFileTitlesOnly(txtFile) else new HashMap[String,String]
    // check each line in txt file
    for (line <- Source.fromFile(csvFile).getLines()) {
      val csvEntry = Entry(line)
      val text = internalIdText(csvEntry.internalId)
      if (minusTitleLength) {
        val titleText = internalIdProductTitle(csvEntry.internalId)
        val lengthOfTitle = titleText.length
        //System.err.println(">>> "+titleText+": "+lengthOfTitle)
        csvEntry.offsetsMinusValue(lengthOfTitle+1)
      }
      if (csvEntry.matches(text)) {
        println(csvEntry)
//        System.err.println(line+" OK!")
//        Thread.sleep(100)
      }
      else {
        val correctedEntry = correctEntry(csvEntry,text)
        if (correctedEntry.isNonNull) {
          System.err.println("Correcting:")
          System.err.println(csvEntry.toString(text))
          System.err.println(correctedEntry.toString(text))
          println(correctedEntry)
        } else {
          System.err.println("Non correctable:")
          System.err.println(csvEntry.toString(text))
        }
        //Thread.sleep(10000)
      }

    }
    System.err.println("Done.")
  }
  def correctEntry(csvEntry:Entry,txt:String) : Entry = {
    if (csvEntry.matches(txt)) csvEntry
    else {
      val indices = allStartIndices(csvEntry,txt)
      if (indices.nonEmpty) {
        val betterLeftOffset = indices.head
        val betterRightOffset = indices.head + csvEntry.stringRepr.length
        Entry(csvEntry.classs,csvEntry.internalId,betterLeftOffset,betterRightOffset,csvEntry.stringRepr,csvEntry.annotationId,csvEntry.foreigness,csvEntry.relatedness)
      } else {
        Entry()
      }
    }
  }
  def readTxtFile(txtFile:String) : HashMap[String,String] = {
    val s = Source.fromFile(txtFile)
    val internalIdToTxtEntry = new HashMap[String,String]
    for (line <- s.getLines()) {
      val entry = TxtEntry(line)
      internalIdToTxtEntry += entry.internalId -> (entry.reviewTitle+" "+entry.reviewText)
      //System.err.println(entry.reviewTitle+" "+entry.reviewText)
    }
    internalIdToTxtEntry
  }

  def readTxtFileTitlesOnly(txtFile:String) : HashMap[String,String] = {
    val s = Source.fromFile(txtFile)
    val internalIdToTxtEntry = new HashMap[String,String]
    for (line <- s.getLines()) {
      val entry = TxtEntry(line)
      internalIdToTxtEntry += entry.internalId -> (entry.name)
    }
    internalIdToTxtEntry
  }

  /**
   * Returns all occurrences sorted by distance to original offset. Probably the first is good. It may return an empty list as well, if nothing is found!
   * @param entry
   * @param textt
   * @param startOffset
   * @param maxEndOffset
   * @return
   */
  def allStartIndices(entry:Entry,textt:String,startOffset:Int=0,maxEndOffset:Int=Int.MaxValue) : Seq[Int] = {
    val pattern = entry.stringRepr
    val endOffset = scala.math.min(textt.length,maxEndOffset)
//    if (pattern.contains("zuempfehl")) {
//      System.err.println("=======================================")
//      System.err.println(textt)
//      System.err.println(entry)
//      System.err.println(endOffset)
//      System.err.println(pattern)
//      System.err.println(textt.indexOf(pattern))
//      System.err.println("=======================================")
//    }
    val originalLeftOffset = entry.leftOffset
//    System.err.println("Searching for "+pattern+" in "+textt+" starting at "+startOffset)

    startOffset.until(endOffset).filter(textt.startsWith(pattern, _)).sortBy(x => scala.math.abs(originalLeftOffset-x))
  }
}
