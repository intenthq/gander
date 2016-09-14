package com.intenthq.gander.extractors

import java.util.Date

import com.intenthq.gander.Link
import com.intenthq.gander.opengraph.OpenGraphData
import com.intenthq.gander.text.{StopWords, WordStats}
import com.intenthq.gander.twitter.TwitterData
import com.intenthq.gander.utils.JSoup._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.jsoup.nodes.{Document, Element}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.convert.Wrappers.JListWrapper
import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.math.{abs, pow}
import scala.util.Try


object ContentExtractor {

  val MIN_TITLE_PERMUTATION_LENGTH = 0.25;

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def extractTitle(doc: Document): String =
    byTag("title")(doc).headOption.map(_.text).getOrElse("").replace("&#65533;", "").trim


  //TODO: also consider to test title permutations take from the url path
  //in order to achive this we need to further simplify the title and remove all symbold, this simbol less version is than used as key of the hash map
  def processTitle(rawTitle: String, canonical: Option[String], openGraphData: OpenGraphData, twitterData: TwitterData, doc: Document): String = {

    val grades = collection.mutable.Map[String, Int]()

    //create map with words only title to words with symbols§
    val originalTitle = Map[String, String](titlePermutations(rawTitle, false).toSeq map { a => a.replaceAll("[^\\p{L}]", "_").toUpperCase -> a }: _*)
    originalTitle.keys.foreach(k => {
      addOrUpdate[String, Int](grades, k, k -> 1, (v: Int) => v + 1)
    })


    val hTags = doc.select("h1, h2, h3, h4, h5, h6").iterator()
    while (hTags.hasNext()) {
      val element = hTags.next()
      titlePermutations(element.text(), true).toSeq.sortWith(_.length > _.length).toStream.takeWhile(
        !addOrUpdate[String, Int](grades, _, null, (v: Int) => v + 4)
      ).force
    }
    if (openGraphData.title.isDefined) {
      titlePermutations(openGraphData.title.get, true).toSeq.sortWith(_.length > _.length).toStream.takeWhile(
        !addOrUpdate[String, Int](grades, _, null, (v: Int) => v + 1)
      ).force
    }



    if (canonical != None) {
      titlePermutations(canonical.get, true).toSeq.sortWith(_.length > _.length).toStream.takeWhile(
        !addOrUpdate[String, Int](grades, _, null, (v: Int) => v + 2)
      ).force
    }

    //Due to twitter chars limit we only cosider it if it is not too shorter than 75% the title lenght
    if (twitterData.title.isDefined) {
      titlePermutations(twitterData.title.get, true).toSeq.sortWith(_.length > _.length).toStream.takeWhile(
        !addOrUpdate[String, Int](grades, _, null, (v: Int) => v + 1)
      ).force
    }

    val title = ListMap(grades.toSeq.sortWith((leftE, rightE) => {
      if (leftE._2 == rightE._2) {
        leftE._1.length() > rightE._1.length
      } else {
        leftE._2 > rightE._2
      }
    }): _*).iterator.next()._1;


    return originalTitle.get(title).get;

  }

  def titlePermutations(title: String, stripSymbols: Boolean): Set[String] = {
    var permutations = Set[String]()
    val keywords = title.split("[\\s]+");
    for (i <- 0 until keywords.length) {
      var sequence = ""
      for (j <- i until keywords.length) {
        sequence = sequence.concat(keywords(j));
        if (!sequence.isEmpty && sequence.length > title.length * MIN_TITLE_PERMUTATION_LENGTH) {
          if (stripSymbols) {
            permutations += sequence.replaceAll("[^\\p{L}]", "_").toUpperCase;
          } else {
            permutations += sequence;
          }

        }
        sequence = sequence.concat(" ");
      }
    }
    return permutations;
  }

  def addOrUpdate[K, V](m: collection.mutable.Map[K, V], k: K, kv: (K, V), f: V => V): Boolean = {
    return m.get(k) match {
      case Some(e) => {
        m.update(k, f(e));
        return true
      }
      case None => {
        if (kv != null) {
          m += kv
        };
        return false
      }
    }
  }


  def extractLang(doc: Document): Option[String] =
    byTag("html")(doc).headOption.map(_.attr("lang")).filter(_.nonEmpty).orElse(
      metaContent("http-equiv=Content-Language")(doc).orElse(
        metaContent("property=og:locale")(doc)
      )
    )

  def extractDate(doc: Document): Option[DateTime] = {
    metaContent("property=article:published_time")(doc).orElse(
      metaContent("name=DCTERMS.created")(doc).orElse(
        select("time[class=dt-published published entry-date]")(doc).headOption.map(_.attr("datetime").trim).orElse(
          select("time[itemprop=datePublished]")(doc).headOption.map(_.attr("datetime").trim).orElse(
            metaContent("name=DisplayDate")(doc).orElse(
              metaContent("name=date")(doc)
            )
          )
        )
      )
    ).flatMap(x =>
      // replaceAll("/","-") is needed as ISODateTimeFormat will block on /
      // e.g. http://www.bbc.co.uk/sport/0/football/34203622
      Try(ISODateTimeFormat.dateTimeParser.parseDateTime(x.replaceAll("/", "-"))).toOption
    )
  }

  /**
    * if the article has meta description set in the source, use that
    */
  def extractMetaDescription(implicit doc: Document): String =
    metaContent("name=description").orElse(
      metaContent("og:description").orElse(
        metaContent("name=twitter:description")
      )
    ).getOrElse("").trim

  private def metaContent(metaName: String)(implicit doc: Document): Option[String] =
    select(s"meta[$metaName]").headOption.map(_.attr("content").trim)

  /**
    * if the article has meta keywords set in the source, use that
    */
  def extractMetaKeywords(implicit doc: Document): String = metaContent("name=keywords").getOrElse(metaContent("name=news_keywords").getOrElse(""))

  /**
    * if the article has meta canonical link set in the url
    */
  def extractCanonicalLink(implicit doc: Document): Option[String] =
    select("link[rel=canonical]").headOption.map(_.attr("abs:href")).orElse(
      select("meta[property=og:url]").headOption.map(_.attr("abs:content"))
    ).orElse(
      select("meta[name=twitter:url]").headOption.map(_.attr("abs:content"))
    ).map(_.trim)

  def extractDateFromURL(url: String): Option[Date] = {
    def findYearMonthAndDay(segments: Array[String]): (Option[Int], Option[Int], Option[Int]) = {
      def findMonthAndDay(segments: Array[String]): (Option[Int], Option[Int]) = {
        def findDay(segment: String): Option[Int] = Try(segment.toInt).filter(d => d >= 1 && d <= 31).toOption
        Try(segments.head.toInt).filter(m => m >= 1 && m <= 12).map { month =>
          (Some(month), findDay(segments.tail.head))
        }.getOrElse((None, None))
      }

      if (segments.isEmpty)
        (None, None, None)
      else {
        Try(segments.head.toInt).filter(y => y > 1970 && y < 3000).map { year =>
          val (month, day) = findMonthAndDay(segments.tail)
          (Some(year), month, day)
        }.getOrElse(findYearMonthAndDay(segments.tail))
      }
    }

    val (year, month, day) = findYearMonthAndDay(url.split("/"))
    year.map { y =>
      val m = month.getOrElse(1)
      val d = day.getOrElse(1)
      new DateTime(y, m, d, 0, 0).toDate
    }
  }

  /**
    * we're going to start looking for where the clusters of paragraphs are. We'll score a cluster based on the number of stopwords
    * and the number of consecutive paragraphs together, which should form the cluster of text that this node is around
    * also store on how high up the paragraphs are, comments are usually at the bottom and should get a lower score
    */
  def calculateBestNodeBasedOnClustering(document: Document, lang: String): Option[Element] = {
    implicit val doc = document.clone

    val nodesToCheck = byTag("p") ++ byTag("td") ++ byTag("pre") ++ byTag("strong") ++ byTag("li") ++ byTag("code")

    val nodesWithText = nodesToCheck.filter { node =>
      val nodeText = node.text
      val wordStats = StopWords.stopWordCount(nodeText, lang)
      val highLinkDensity = isHighLinkDensity(node)
      logger.trace("Candidate: " + node.tagName() + " score: " + wordStats + " d:" + highLinkDensity + " text:" + nodeText)
      wordStats.stopWordCount > 2 && !highLinkDensity
    }

    val numberOfNodes = nodesWithText.size
    val bottomNodesForNegativeScore = numberOfNodes * 0.25

    logger.trace("About to inspect num of nodes with text: " + numberOfNodes)

    def boostScoreForNode(node: Element, startingBoost: Double, count: Int): (Double, Double) = {
      var newStartingBoost = startingBoost
      var result = 0.0
      if (isOkToBoost(node, lang)) {
        result = (1.0 / startingBoost) * 50
        newStartingBoost += 1
      }
      if (numberOfNodes > 15) {
        if ((numberOfNodes - count) <= bottomNodesForNegativeScore) {
          val booster = bottomNodesForNegativeScore - (numberOfNodes - count)
          result = -pow(booster, 2)
          if (abs(result) > 40)
            result = 5
        }
      }
      (newStartingBoost, result)
    }

    var count = 0
    var startingBoost: Double = 1.0
    val parentNodes = mutable.Set.empty[Element]

    for (node <- nodesWithText) {
      val (newStartingBoost, boostScore) = boostScoreForNode(node, startingBoost, count)
      startingBoost = newStartingBoost

      logger.trace("Location Boost Score: " + boostScore + " on interation: " + count + " tag='" + node.tagName + "' id='" + node.parent.id + "' class='" + node.parent.attr("class"))

      val wordStats: WordStats = StopWords.stopWordCount(node.text, lang)
      val upscore: Int = (wordStats.stopWordCount + boostScore).toInt
      updateScore(node.parent, upscore)
      updateScore(node.parent.parent, upscore / 2)
      updateNodeCount(node.parent, 1)
      updateNodeCount(node.parent.parent, 1)
      parentNodes.add(node.parent)
      parentNodes.add(node.parent.parent)
      count += 1
    }

    if (parentNodes.isEmpty)
      None
    else {
      Some(parentNodes.maxBy(getScore)).filter(getScore(_) >= 20)
    }
  }

  /**
    * alot of times the first paragraph might be the caption under an image so we'll want to make sure if we're going to
    * boost a parent node that it should be connected to other paragraphs, at least for the first n paragraphs
    * so we'll want to make sure that the next sibling is a paragraph and has at least some substatial weight to it
    */
  private def isOkToBoost(node: Element, lang: String): Boolean = {
    var stepsAway: Int = 0
    val minimumStopWordCount = 5
    val maxStepsAwayFromNode = 3

    walkSiblings(node) { currentNode =>
      if (currentNode.tagName == "p" || currentNode.tagName == "strong") {
        if (stepsAway >= maxStepsAwayFromNode) {
          return false
        }
        val wordStats = StopWords.stopWordCount(currentNode.text, lang)
        if (wordStats.stopWordCount > minimumStopWordCount)
          return true
        stepsAway += 1
      }
    }
    false
  }

  private def walkSiblings[T](node: Element)(work: (Element) => T): Seq[T] = {
    var currentSibling = node.previousElementSibling
    val b = mutable.Buffer[T]()

    while (currentSibling != null) {
      b += work(currentSibling)
      currentSibling = currentSibling.previousElementSibling
    }
    b
  }

  /**
    * adds a score to the gravityScore Attribute we put on divs
    * we'll get the current score then add the score we're passing in to the current
    *
    * @param addToScore - the score to add to the node
    */
  private def updateScore(node: Element, addToScore: Int) {
    val currentScore = Try(node.attr("gravityScore").toInt).getOrElse(0)
    val newScore = currentScore + addToScore
    node.attr("gravityScore", newScore.toString)
  }

  /**
    * stores how many decent nodes are under a parent node
    */
  private def updateNodeCount(node: Element, addToCount: Int) {
    val currentScore = Try(node.attr("gravityNodes").toInt).getOrElse(0)
    val newScore: Int = currentScore + addToCount
    node.attr("gravityNodes", newScore.toString)
  }

  private def getScore(node: Element): Int = getGravityScoreFromNode(node).getOrElse(0)

  private def getGravityScoreFromNode(node: Element): Option[Int] = Try(node.attr("gravityScore").toInt).toOption

  /**
    * Checks the density of links within a node. If there's not much text and what's there is mostly links,
    * we're not interested
    */
  private def isHighLinkDensity(implicit e: Element): Boolean = {
    val limit = 1.0
    val links = byTag("a") ++ byAttr("onclick")

    if (links.isEmpty)
      false
    else {
      val words = e.text.trim.split("\\s+")
      val linkWords = links.mkString(" ").split("\\s+")
      val numberOfLinks = links.size
      val numberOfWords = words.length.toDouble
      val numberOfLinkWords = linkWords.length.toDouble
      val score = numberOfLinks * numberOfLinkWords / numberOfWords

      logger.trace("Calculated link density score as: {} for node: {}", score, getShortText(e.text, 50))

      score >= limit
    }
  }

  private def getShortText(e: String, max: Int): String = if (e.length > max) e.take(max) + "..." else e

  /**
    * pulls out links we like
    */
  def extractLinks(implicit node: Element): Seq[Link] =
    select("a[href]")
      .filter(el => el.attr("href") != "#" && !el.attr("abs:href").trim.isEmpty)
      .map(el => Link(el.text, el.attr("abs:href")))

  /**
    * remove any divs that looks like non-content, clusters of links, or paras with no gusto
    */
  def postExtractionCleanup(targetNode: Element, lang: String): Element = {
    val node = addSiblings(targetNode, lang)
    JListWrapper(node.children)
      .filter(e => e.tagName != "p" || isHighLinkDensity(e))
      .filter(e => isHighLinkDensity(e) || isTableTagAndNoParagraphsExist(e) || !isNodeScoreThresholdMet(node, e))
      .foreach(remove)
    node
  }

  private def isTableTagAndNoParagraphsExist(implicit e: Element): Boolean = {
    getChildParagraphs(e).filter(_.text.length < 25).foreach(remove)

    val subParagraphs2 = byTag("p")
    if (subParagraphs2.isEmpty && e.tagName != "td") {
      if (e.tagName == "ul" || e.tagName == "ol") {
        val linkTextLength = byTag("a").map(_.text.length).sum
        val elementTextLength = e.text.length
        elementTextLength <= 2 * linkTextLength
      }
      else true
    } else false
  }

  private def getChildParagraphs(implicit e: Element): Seq[Element] = byTag("p") ++ byTag("strong")

  private def isNodeScoreThresholdMet(node: Element, e: Element): Boolean = {
    val topNodeScore = getScore(node)
    val currentNodeScore = getScore(e)
    val thresholdScore = topNodeScore * .08
    !(currentNodeScore < thresholdScore && e.tagName != "td")
  }

  private def addSiblings(topNode: Element, lang: String): Element = {
    val baselineScoreForSiblingParagraphs = getBaselineScoreForSiblings(topNode, lang)
    val results = walkSiblings(topNode) { currentNode =>
      getSiblingContent(currentNode, baselineScoreForSiblingParagraphs, lang)
    }.reverse.flatten
    topNode.child(0).before(results.mkString)
    topNode
  }

  /**
    * adds any siblings that may have a decent score to this node
    */
  private def getSiblingContent(currentSibling: Element,
                                baselineScoreForSiblingParagraphs: Int,
                                lang: String): Option[String] = {
    if ((currentSibling.tagName == "p" || currentSibling.tagName == "strong") && currentSibling.text.nonEmpty)
      Some(currentSibling.outerHtml)
    else {
      val siblingBaseLineScore = baselineScoreForSiblingParagraphs * 0.3
      val text = getChildParagraphs(currentSibling)
        .filter(p => StopWords.stopWordCount(p.text, lang).stopWordCount >= siblingBaseLineScore)
        .map(p => "<p>" + p.text + "<p>")
        .mkString(" ")
      if (text.isEmpty) None else Some(text)
    }
  }

  /**
    * we could have long articles that have tons of paragraphs so if we tried to calculate the base score against
    * the total text score of those paragraphs it would be unfair. So we need to normalize the score based on the average scoring
    * of the paragraphs within the top node. For example if our total score of 10 paragraphs was 1000 but each had an average value of
    * 100 then 100 should be our base.
    */
  private def getBaselineScoreForSiblings(topNode: Element, lang: String): Int = {
    val nodesToCheck = getChildParagraphs(topNode)

    val scores = nodesToCheck.flatMap { node =>
      val wordStats = StopWords.stopWordCount(node.text, lang)
      if (wordStats.stopWordCount > 2 && !isHighLinkDensity(node)) Some(wordStats.stopWordCount)
      else None
    }

    if (scores.nonEmpty) scores.sum / scores.length
    else Int.MaxValue
  }
}
