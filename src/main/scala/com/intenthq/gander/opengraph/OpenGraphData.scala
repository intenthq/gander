/**
Copyright [2014] Robby Pond

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.intenthq.gander.opengraph

import java.net.URL

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.jsoup.nodes.Element

import scala.util.Try

case class OpenGraphData(title: Option[String] = None,
                         siteName: Option[String] = None,
                         url: Option[URL] = None,
                         description: Option[String] = None,
                         image: Option[URL] = None,
                         `type`: Option[String] = None,
                         locale: Option[String] = None,
                         publishedTime: Option[DateTime] = None)
object OpenGraphData {

  def apply(elem: Element): OpenGraphData = {
    def attr(property: String): Option[String] =
      Option(elem.select(s"meta[property=$property], meta[name=$property]").first()).map(_.attr("content"))
    def url(x: String) = Try(new URL(x)).toOption
    def date(x: String) = Try(ISODateTimeFormat.dateTimeParser.parseDateTime(x)).toOption

    OpenGraphData(title = attr("og:title"),
                  siteName = attr("og:site_name"),
                  url = attr("og:url").flatMap(url),
                  description = attr("og:description"),
                  image = attr("og:image").flatMap(url),
                  `type` = attr("og:type"),
                  locale = attr("og:locale"),
                  publishedTime = attr("article:published_time").flatMap(date))
  }

}