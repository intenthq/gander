# Gander [![Build Status](https://img.shields.io/travis/intenthq/gander/master.svg)](https://travis-ci.org/intenthq/gander) [![Coverage Status](https://img.shields.io/coveralls/intenthq/gander.svg)](https://coveralls.io/github/intenthq/gander?branch=master) [![Maven Central](https://img.shields.io/maven-central/v/com.intenthq/gander_2.11.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.intenthq%22%20AND%20a%3A%22gander_2.11%22) [![Join the chat at https://gitter.im/intenthq/gander](https://img.shields.io/badge/gitter-join%20chat-green.svg)](https://gitter.im/intenthq/gander)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fintenthq%2Fgander.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fintenthq%2Fgander?ref=badge_shield)

**Gander is a scala library that extracts metadata and content from web pages.**

It is based on [Goose](https://github.com/GravityLabs/goose) with the idea to:
- Simplify its codebase by removing some of its functionality (like crawling, there are plenty of project that do it well)
- Keep it alive (goose has been inactive for several years now)
- Make its codebase more functional and take advantage of some of newer scala features

## What data does it extract?

Gander will try to extract three different kinds of data from a web page:
- Metadata: (title, meta description, meta keywords, language, canonical link, open graph data,
publish date)
- Main text for the page
- Links present in the main text of the page

## Using Gander

### Adding the dependency

The artefact is published in maven central. If you are using sbt you just need to add
the following line (remember to replace 1.0 with the latest version):
```
"com.intenthq" % "gander" % "1.0"
```
### In your code

Gander provides a single object and a single method to access its functionality
and it's pretty straightforward and intuitive to use.

This three lines of code, for example, will download the specified url (using
Guava) and extract the page information from the raw html:
```scala
val url = "http://engineering.intenthq.com"
val rawHTML = Resources.toString(new URL(url), charset)
println(Gander.extract(rawHTML))

```

You can find more examples in our tests.

## Philosophy

The idea behind Gander is to do one thing and do it well. That's why we've
removed some of the features that were not related to its core functionality.

This project will always try to be better at extracting data and information
from webpages. But it won't deal with other (probably related but not core)
functionalities (like downloading html from urls).

## Collaborate

Please, feel free to raise an issue, fork the repo, send pull requests...
Any idea or improvement will be welcome.


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fintenthq%2Fgander.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fintenthq%2Fgander?ref=badge_large)