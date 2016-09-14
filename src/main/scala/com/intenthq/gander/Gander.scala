package com.intenthq.gander

import java.util.Date

import com.intenthq.gander.extractors.ContentExtractor._
import com.intenthq.gander.opengraph.OpenGraphData
import com.intenthq.gander.twitter.TwitterData
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.util.Try


case class Link(text: String, target: String)

case class PageInfo(title: String,
                    processedTitle: String,
                    metaDescription: String,
                    metaKeywords: String,
                    lang: Option[String],
                    canonicalLink: Option[String],
                    openGraphData: OpenGraphData,
                    twitterData: TwitterData,
                    cleanedText: Option[String] = None,
                    structuredText: Option[String] = None,
                    links: Seq[Link] = Seq.empty,
                    publishDate: Option[Date] = None)

object Gander {

  val whiteList: Whitelist = Whitelist.none
  whiteList.addTags("div", "p", "h1", "h2", "h3", "h4", "h5", "span", "ul", "li", "table", "tr", "td", "a", "strong", "br", "code", "hr")
  whiteList.addAttributes("a", "href")

  def extract(html: String, lang: String = "all"): Option[PageInfo] = {
    //This is replacing the non-breaking space with a regular space
    val sanitised = html.replace(' ', ' ')
    Try(Jsoup.parse(sanitised)).toOption.map { doc =>
      val canonicalLink = extractCanonicalLink(doc)
      val publishDate = extractDate(doc).map(_.toDate).orElse(canonicalLink.flatMap(extractDateFromURL))

      val rawTitle = extractTitle(doc)
      val openGraphData = OpenGraphData(doc)
      val twitterData = TwitterData(doc)

      val info = PageInfo(title = rawTitle,
        processedTitle = processTitle(rawTitle, canonicalLink, openGraphData, twitterData,doc),
        metaDescription = extractMetaDescription(doc),
        metaKeywords = extractMetaKeywords(doc),
        lang = extractLang(doc),
        canonicalLink = canonicalLink,
        publishDate = publishDate,
        openGraphData = openGraphData,
        twitterData = twitterData
      )

      val cleanedDoc = DocumentCleaner.clean(doc)
      calculateBestNodeBasedOnClustering(cleanedDoc, lang).map { node =>
        //some mutability beauty
        postExtractionCleanup(node, lang)
        info.copy(cleanedText = Some(node.text()),
          structuredText = Some(Jsoup.clean(node.toString, whiteList).toString()),
          links = extractLinks(node))
      }.getOrElse(info)
    }
  }

}
