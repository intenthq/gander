package com.intenthq.gander.text

import java.io.StringReader
import java.util.regex.Pattern

import com.chenlb.mmseg4j.{ComplexSeg, Dictionary, MMSeg}
import com.intenthq.gander.utils.FileHelper

import scala.collection.mutable

object StopWords {
  // the confusing pattern below is basically just match any non-word character excluding white-space.
  private val punctuationPattern = Pattern.compile("[^\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}\\p{Pc}\\s]")

  private val stopWordsMap = mutable.Map.empty[String, Set[String]]

  private def removePunctuation(str: String): String = punctuationPattern.matcher(str).replaceAll("")

  def stopWords(lname: String): Set[String] =
    stopWordsMap.getOrElse(lname, {
      val stopWordsFile = "stopwords-%s.txt" format lname
      val stopWords = FileHelper.loadResourceFile(stopWordsFile, StopWords.getClass)
        .split(sys.props("line.separator"))
        .map(s => s.trim)
        .toSet
      stopWordsMap += lname -> stopWords
      stopWords
    })

  def candidateWords(strippedInput: String, language: String): Array[String] = strippedInput.split(" ")

  def stopWordCount(content: String, lang: String = "en"): WordStats = {
    val strippedInput = removePunctuation(content)
    val candidates = candidateWords(strippedInput, lang)
    val stop = stopWords(lang)
    val overlappingStopWords = candidates.map(_.toLowerCase).filter(stop.contains)
    WordStats(overlappingStopWords.toList, candidates.length)
  }
}