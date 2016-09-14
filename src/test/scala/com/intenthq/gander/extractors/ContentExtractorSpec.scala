package com.intenthq.gander.extractors

import org.joda.time.DateTime
import org.specs2.mutable.Specification

class ContentExtractorSpec extends Specification {

  def date(year: Int, month: Int, day: Int) = Some(new DateTime(year, month, day, 0, 0).toDate)

  "extractDateFromURLUnsafe" >> {
    " should extract the date from the path, if present" >> {
      ContentExtractor.extractDateFromURL("http://a.com/no/date/in/this/path") must_== None
      ContentExtractor.extractDateFromURL("http://a.com/not/every/number/1900/is/a/date") must_== None
      ContentExtractor.extractDateFromURL("http://a.com/number/2000a/plus/letters") must_== None

      ContentExtractor.extractDateFromURL("http://a.com/a/year/2000/and/nothing/else") must_== date(2000, 1, 1)
      ContentExtractor.extractDateFromURL("http://a.com/a/year/2000/and/10/not/a/month") must_== date(2000, 1, 1)
      ContentExtractor.extractDateFromURL("http://a.com/a/year/2000/13/not/a/month") must_== date(2000, 1, 1)

      ContentExtractor.extractDateFromURL("http://a.com/a/year/2000/10/and/a/month") must_== date(2000, 10, 1)
      ContentExtractor.extractDateFromURL("http://a.com/not/2000/10/a/20/day") must_== date(2000, 10, 1)
      ContentExtractor.extractDateFromURL("http://a.com/not/2000/10/32/a/day") must_== date(2000, 10, 1)

      ContentExtractor.extractDateFromURL("http://a.com/not/2000/10/31/a/day") must_== date(2000, 10, 31)
    }
  }
  /*
    "processTitle" >> {
      " should keep the raw title if there is no canonical" >> {
        ContentExtractor.processTitle("This is the title", None) must_== "This is the title"
      }

      " should keep the raw title if the domain name is not contained in the title" >> {
        ContentExtractor.processTitle("This is the title | Not related", Some("http://something.com")) must_== "This is the title | Not related"
      }

      " should remove the part of the title that contains the site name" >> {
        ContentExtractor.processTitle("This is the title | BBC News", Some("http://www.bbc.co.uk")) must_== "This is the title"
      }

      " should remove the part of the title that contains the site name, even if it's two words" >> {
        ContentExtractor.processTitle("Business Insider | This is the title", Some("http://www.businessinsider.com")) must_== "This is the title"
      }

      " should split the title by a dash" >> {
        ContentExtractor.processTitle("This is the title - BBC News", Some("http://www.bbc.co.uk")) must_== "This is the title"
      }

      " should match the title even if it uses character variations" >> {
        ContentExtractor.processTitle("This is the title - El País", Some("http://www.elpais.com")) must_== "This is the title"
      }
    }
    */
}
