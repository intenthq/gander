/**
  * Copyright [2014] Robby Pond
  * *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * *
  * http://www.apache.org/licenses/LICENSE-2.0
  * *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.intenthq.gander.twitter

import java.net.URL

import org.jsoup.nodes.Element

import scala.util.Try

case class TwitterData(title: Option[String] = None,
                       siteName: Option[String] = None,
                       creator: Option[String] = None,
                       description: Option[String] = None,
                       image: Option[URL] = None)

object TwitterData {

  def apply(elem: Element): TwitterData = {
    def attr(property: String): Option[String] =
      Option(elem.select(s"meta[property=$property], meta[name=$property]").first()).map(_.attr("content"))
    def url(x: String) = Try(new URL(x)).toOption

    TwitterData(title = attr("twitter:title"),
      siteName = attr("twitter:site"),
      creator = attr("twitter:creator"),
      description = attr("twitter:description"),
      image = attr("twitter:image").flatMap(url))
  }

}