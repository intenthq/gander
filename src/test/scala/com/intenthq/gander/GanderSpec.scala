package com.intenthq.gander

import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.intenthq.gander.opengraph.OpenGraphData
import org.joda.time.DateTime
import org.specs2.mutable.Specification

class GanderSpec extends Specification {

  def extract(url: String, charset: Charset = Charsets.UTF_8, lang: String = "all"): PageInfo = {
    val resource = getClass.getResourceAsStream("/" + url.stripPrefix("http://").replace('/', '_') + ".gz")
    val rawHTML = CharStreams.toString(new InputStreamReader(new GZIPInputStream(resource), charset))
    Gander.extract(rawHTML, lang).get
  }

  def check(pageInfo: PageInfo, title: String, processedTitle: String, metaDescription: String, metaKeywords: String,
            lang: Option[String], date: Option[String], content: String, url: String, links: Seq[Link]) = {
    pageInfo.title must_== title
    pageInfo.processedTitle must_== processedTitle
    pageInfo.metaDescription must_== metaDescription
    pageInfo.metaKeywords must_== metaKeywords
    pageInfo.lang must_== lang
    pageInfo.publishDate must_== date.map(DateTime.parse(_).toDate)
    pageInfo.cleanedText.get must startWith(content)
    pageInfo.canonicalLink.map(_ must_== url).getOrElse(1 must_== 1)
    pageInfo.links must_== links
  }

  "UTF-8 encoding of unicode non breaking char must be sanitised as a space" >> {
    //Some pages (like the Apple Watch one) contain this char instead of a space
    //For more info check https://en.wikipedia.org/wiki/Non-breaking_space
    val url = "http://www.apple.com/watch/"
    extract(url).cleanedText.get must contain("Apple Watch")
  }

  "intenthq" >> {
    val url = "http://engineering.intenthq.com/2015/03/what-is-good-code-a-scientific-definition/"
    check(extract(url),
      url = url,
      content = "Here at Intent HQ we believe how important it is to write good code. Why? First, because writing good code is much cheaper and more fun than writing bad code. Second, because if you write good code chances are that the product you are building will be much better. Third, and more important, because writing good code is what we are supposed to do: after all, we are getting paid for doing our job well",
      title = "What is good code? A scientific definition. - Intent HQ Engineering blog",
      processedTitle = "What is good code? A scientific definition.",
      metaDescription = "How would you define good code? This article gives a pseudo-scientific answer to that question after asking a sample of 65 developers that same question.",
      metaKeywords = "",
      lang = Some("en-GB"),
      date = Some("2015-03-01"),
      links = List(Link("Uncle Bob", "http://en.wikipedia.org/wiki/Robert_Cecil_Martin"),
        Link("DRY", "http://en.wikipedia.org/wiki/Don%27t_repeat_yourself")))
  }

  "bbc.com" >> {
    val url = "http://www.bbc.com/news/business-33697945"
    check(extract(url),
      url = url,
      content = "Disneyland Paris is facing a pricing probe following accusations that UK and German customers are being frozen out of certain price promotions.",
      title = "Disneyland Paris faces pricing probe - BBC News",
      processedTitle = "Disneyland Paris faces pricing probe",
      metaDescription = "Disneyland Paris is facing a pricing probe following accusations that UK and German customers are being frozen out of promotions available in other European member states.",
      metaKeywords = "",
      lang = Some("en"),
      date = None,
      links = List(Link("Financial Times said", "http://www.ft.com/cms/s/0/27e42c8e-351d-11e5-b05b-b01debd57852.html#axzz3hDFfsPCX"),
        Link("said in a report", "http://www.ft.com/cms/s/0/27e42c8e-351d-11e5-b05b-b01debd57852.html#axzz3hDFfsPCX")))

  }

  "bbc.com/sport" >> {
    val url = "http://www.bbc.com/sport/football/34203622"
    check(extract(url),
      url = url,
      content = "Manchester City's Champions League campaign got off to a disappointing start with a home defeat by last season's runners-up Juventus. City, who have struggled to make a serious impact in the Champions League and have never won their opening home game in the group stage, looked to be on course for victory when Juventus defender Giorgio Chiellini headed into his own net under pressure from Vincent Kompany.",
      title = "Alvaro Morata & Mario Mandzukic score as Juventus shock Man City - BBC Sport",
      processedTitle = "Alvaro Morata & Mario Mandzukic score as Juventus shock Man City",
      metaDescription = "Manchester City concede two goals in the last 20 minutes as Juventus fight back from a goal down to win at Etihad Stadium.",
      metaKeywords = "",
      lang = Some("en"),
      date = None,
      links = List()
    )
  }

  "businessinsider" >> {
    val url = "http://www.businessinsider.com/goldman-on-the-fed-announcement-2011-9"
    check(extract(url),
      url = url,
      content = "From Goldman on the FOMC operation twist announcement: ------------- 1. As we had expected, the Federal Open Market Committee decided to \"do the twist\" and increase the duration of its securities holdings by selling shorter-maturity securities ($400bn of Treasuries with maturity of 3 years or less)",
      title = "GOLDMAN: 4 Key Points On The FOMC Announcement - Business Insider",
      processedTitle = "GOLDMAN: 4 Key Points On The FOMC Announcement",
      metaDescription = "Here it is.",
      metaKeywords = "Federal Reserve, Joe Weisenthal",
      lang = Some("en"),
      date = Some("2011-09-21"),
      links = List(Link("announcement", "http://www.businessinsider.com/federal-reserve-announcement-fomc-operation-twist-2011-9")))
  }

  "elpais" >> {
    val url = "http://internacional.elpais.com/internacional/2015/07/28/actualidad/1438076596_960360.html"
    check(extract(url),
      url = url,
      content = "Los aliados de la OTAN ofrecieron este martes respaldo político a Turquía en su ofensiva contra el Estado Islámico tras una reunión convocada de urgencia por el Gobierno de Ankara.",
      title = "La OTAN apoya con cautela la ofensiva turca contra el yihadismo | Internacional | EL PAÍS",
      processedTitle = "La OTAN apoya con cautela la ofensiva turca contra el yihadismo",
      metaDescription = "La Alianza se ha reunido este martes con carácter de urgencia a pedición de Ankara para tratar el avance del Estado Islámico",
      metaKeywords = "otan, apoyar, cautela, ofensiva, turca, turco, yihadismo, alianza, haber, reunir, martes, urgencia, pedición, ankara, secretario, general, jens stoltenberg, resaltar, unidad, aliado",
      lang = Some("es"),
      date = Some("2015-07-29"),
      links = List(Link("en su ofensiva contra el Estado Islámico", "http://internacional.elpais.com/internacional/2015/07/24/actualidad/1437717227_199769.html"),
        Link("Jens Stoltenberg.", "http://elpais.com/tag/jens_stoltenberg/a/"),
        Link("que este martes hizo estallar un tramo de un gasoducto procedente de Irán", "http://internacional.elpais.com/internacional/2015/07/28/actualidad/1438079899_805996.html"),
        Link("onflicto entre Ankara y los simpatizantes del PKK", "http://internacional.elpais.com/internacional/2015/07/27/actualidad/1437986632_361510.html"),
        Link("crear una zona libre de combatientes del EI", "http://internacional.elpais.com/internacional/2015/07/27/actualidad/1438026945_461718.html"),
        Link("Ahmet Davutoglu", "http://elpais.com/tag/ahmet_davutoglu/a/")))
  }

  "corriere" >> {
    val url = "http://www.corriere.it/cronache/15_luglio_29/relazione-alfano-mafia-fatti-gravi-sindaco-ha-sottovalutato-25146a6c-35b0-11e5-b050-7dc71ce7db4c.shtml"
    check(extract(url, Charsets.ISO_8859_1),
      url = url,
      content = "ROMA La strada è tracciata, la relazione potrebbe arrivare a Palazzo Chigi prima della pausa estiva. Il ministro dell’Interno Angelino Alfano non proporrà lo scioglimento per mafia del comune di Roma, ma nella relazione al governo",
      title = "La relazione di Alfano sulla mafia: fatti gravi, il sindaco ha sottovalutato - Corriere.it",
      processedTitle = "La relazione di Alfano sulla mafia: fatti gravi, il sindaco ha sottovalutato",
      metaDescription = "Non si propone lo scioglimento ma si lascia aperta la possibilità di una «diversa valutazione»",
      metaKeywords = "Ignazio Marino, Angelino Alfano",
      lang = Some("it"),
      date = None,
      links = List(Link("giunta guidata da Ignazio Marino", "http://roma.corriere.it/notizie/politica/15_luglio_28/giunta-marino-senatore-no-tav-esposito-assessore-trasporti-d0e76efa-34fe-11e5-984f-1e10ffe171ae.shtml")))

  }

  "canarias7" >> {
    val url = "http://www.canarias7.es/articulo.cfm?Id=434625"
    check(extract(url, Charsets.UTF_8, "es"),
      url = url,
      //content = "La Junta de Portavoces del Congreso, con el apoyo de la mayoría de los grupos salvo el PP, ha acordado que el ministro de Economía, Luis de Guindos, comparezca para explicar el nombramiento y renuncia de José Manuel Soria como director ejecutivo del Banco Mundial en un pleno extraordinario, para el que aún no hay fecha.\n\nLa fecha de la comparecencia de De Guindos deberá fijarla ahora la Mesa del Congreso, que está semana aprobó el calendario de plenos ordinarios de los próximos meses sin contemplar ninguna sesión plenaria extraordinaria.\n\nNo obstante, PSOE y Unidos Podemos han logrado sacar adelante en la Junta de Portavoces su petición para que De Guindos comparezca en un pleno urgente y no en la Comisión de Economía, como había solicitado el ministro. Solicitud que ha contado con el apoyo de Ciudadanos y otros partidos como el PNV, ERC y Partido Demócrata Catalán, que representa al grupo Mixto, según fuentes parlamentarias.",
      //TODO: improve content extraction
      content = "",
      title = "Canarias7. Nacional. El Congreso acuerda que De Guindos explique en un pleno el caso Soria",
      processedTitle = "El Congreso acuerda que De Guindos explique en un pleno el caso Soria",
      metaDescription = "La Junta de Portavoces del Congreso, con el apoyo de la mayoría de los grupos salvo el PP, ha acordado que el ministro de Economía, Luis de Guindos, comparezca para explicar el nombramiento y renuncia de José Manuel Soria como director ejecutivo del Banco Mundial en un pleno extraordinario, para el ...",
      metaKeywords = "Canarias7, prensa, noticias, información, informacion general, Islas Canarias, Canarias, Canary Islands,",
      lang = Some("es"),
      date = None,
      links = List())

  }


  "www.setal.net" >> {
    val url = "http://www.setal.net/Bouba-Ndour-directeur-des-programmes-de-la-TFM-a-coeur-ouvert-Viviane-etait-une-etape-de-ma-vie-que-j-ai-depassee_a49768.html"
    check(extract(url, Charsets.UTF_8),
      url = url,
      //content = "Anticonformiste, il appelle un chat, un chat… Et c’est ce qui fait toute sa personnalité. Bouba Ndour, directeur des programmes de la télé futurs médias (TFM) ou encore frangin de la star planétaire, n’a pas son pareil pour mettre les pieds dans le plat. Actuellement en studio avec les candidats de la présente édition de Sen Petit Gallé, l’homme a fait parler ses talents de producteur pour pondre un album sur mesure dédié aux familles sénégalaises dans toutes leurs composantes. Un opus qu’il plébiscite déjà véritable chef-d’œuvre, avant sa sortie prévue d’ici décembre. En revanche, on n’aura pas à attendre tout ce temps, pour connaître son point de vue sur cette polémique qui enfle sur la toile et qui voudrait le voir à nouveau, être en couple avec Viviane. «Stérile et puéril, le débat ne mérite pas d’être posé», coupe-t-il court la clameur populaire… Interview\n\nBouba, vous êtes actuellement en studio. Que préparez-vous ?\n\nEffectivement ! J’ai renoué avec mon plus vieil amour, la production. Bien que très occupé à la télévision, j’ai senti la nécessité de replonger dans la musique. J’étais d’ailleurs en studio et je viens de terminer un véritable chef-d’œuvre avec les enfants qui ont eu à participer à cette édition de Sen-Petit Gallé. Mon fils, Philipe Ndour, en fait également partie. C’est un album de 12 titres consacrés aux enfants. Il est fin prêt et je peux vous assurer que ce sera une vraie bombe. Je n’ai jamais été aussi enthousiaste et ravi de bosser dans un projet musical. J’avoue que je suis très impressionné par le talent des gamins. La sortie est prévue d’ici décembre.\n\nQu’est-ce qui a motivé la production de cet opus ?\n\nC’est une idée qui a longtemps germé en moi et c’est seulement maintenant que j’ai pu la réaliser. J’en avais parlé à ma sœur, Ngoné Ndour qui avait adhéré et m’a mis la pression pour que cela se fasse. J’ai donc profité de l’occasion, après la demi-finale, pour retenir les enfants quelques jours, en studio.\n\nA quoi peuvent s’attendre les Sénégalais ?\n\nC’est un album utile, indispensable pour chaque famille, que les Sénégalais pourront consommer sans modération. Ils vont tous, tout âge confondu, s’y retrouver. Ce n’est pas qu’un album musical, il y a beaucoup d’idées derrière. Je peux même affirmer que ce produit sera une démarche participative à l’éducation et à l’émancipation. Nous n’avons pas encore trouvé le nom, mais nous y réfléchissons. Retenez qu’il sera immortel et intemporel. Les générations actuelles et même futures y trouveront leurs comptes.\n\nLes morceaux seront des inédits ou des reprises ?\n\nC’est du tout neuf ! Je m’en arrête là pour ne pas gâcher la surprise…\n\nCôté style musical ?\n\nEn tout cas, il n’y aura pas de Mbalakh. C’est l’atmosphère et l’esprit de l’album même qui demandent cela. La musique ne fait qu’accompagner. L’accent a beaucoup plus été mis sur les voix, les textes et les thèmes.\n\nQu’est-ce que les candidats de Sen Petit Gallé All Stars auront à y gagner ?\n\nIl est très tôt pour en juger, l’album n’étant pas encore sur le marché. Je sais juste que cela peut aller plus loin qu’un album musical et profiter à tout le monde, en commençant par ses enfants qui y ont posé leurs voix.\n\nUn autre sujet qui vous touche personnellement, enflamme le Web. Votre remariage avec la chanteuse Viviane Chidid est au cœur de discussions sur les réseaux sociaux. Beaucoup souhaitent voir votre couple renaître. Qu’en pensez-vous ?\n\nFranchement, je n’ai rien à avoir avec toutes ces messes basses. Je suis complètement détaché de toute cette histoire. Maintenant, mon destin était lié, d’une manière ou d’une autre, à celui de Viviane. C’est Dieu qui a voulu les choses ainsi. Il était écrit que nous allions nous marier et par la suite, divorcer. La vie continue. Il faut que les gens arrêtent de vouloir vivre la vie des autres à leur place. Quelles que soient leurs envies et émotions, cela ne m’engage nullement. Je ne suis pas là pour réaliser leurs rêves. Tout ce qui m’importe, c’est que je veux…\n\nEt que voulez-vous précisément ?\n\nC’est assez simple ! Je veux que les gens comprennent que je suis à mille lieues de tout ce qui se trame. Viviane était une étape de ma vie que j’ai dépassée. Il est clair que j’ai maintenant tourné la page.\n\nN’est-il pas possible que vous reveniez en arrière ?\n\nNon ! Viviane fait partie de mon passé. C’est comme ça et c’est tout. C’est clair dans ma tête, il faut que ça le soit pour ceux qui font des élucubrations sur un quelconque remariage entre Bouba et Viviane. Me concernant, il n’y a ni projet, ni intention de remariage. Je souhaite à Viviane de refaire sa vie comme bon lui semble. Quant à moi, je suis définitivement passé à autre chose." ,
      //TODO: improve content extraction
      content = "",
      title = "Bouba Ndour, directeur des programmes de la TFM, a cœur ouvert : «Viviane était une étape de ma vie que j’ai dépassée»",
      processedTitle = "Bouba Ndour, directeur des programmes de la TFM, a cœur ouvert : «Viviane était une étape de ma vie que j’ai dépassée»",
      metaDescription = "Anticonformiste, il appelle un chat, un chat… Et c’est ce qui fait toute sa personnalité. Bouba Ndour, directeur des programmes de la télé futurs médias (TFM) ou encore frangin de la star planétaire...",
      metaKeywords = "Abdoulaye Wade, Idrissa Seck, Macky Sall, Moustapha Niasse, Tanor, Cheikh Bamba Dieye, Abdou diouf, Karim Wade, Cheikh tidiane Gadio, Senghor, Youssou Ndour, Senegal, Dakar, actualité, dj boubs, Cheikh tidiane mbaye, Sonatel, Touba, Cheikh Ahmadou Bamba, El Hadji Malick Sy, Tivaouane, Monument de la renaissance africaine, Adja Diallo, mimi toure,",
      lang = Some("fr"),
      date = None,
      links = List())

  }


  "www.deutsche-handwerks-zeitung.de" >> {
    val url = "http://www.deutsche-handwerks-zeitung.de/kumpel-keule-zielgruppe-flexitarier/150/3094/335461"
    check(extract(url, Charsets.UTF_8),
      url = url,
      //TODO: improve content extraction
      //content="Metzgern kann hip sein. Wichtige Voraussetzung: Zeigen, was man kann und wie es richtig geht. Darauf setzt die Berliner Metzgerei Kumpel & Keule und gewinnt damit Kunden und Azubis.\n\nDas Team von Kumpel & Keule ist stolz, was da in ihrer Fleischtheke liegt: gute Handwerksarbeit. - ©\n\nMittwochnachmittag in der Markthalle Neun in Berlin Kreuzberg. Markttag war gestern. An den Ständen herrscht Aufräumstimmung. Doch ganz hinten in der Halle ist eine Fleischtheke beleuchtet. Jule füllt sie gerade mit dicken Scheiben Rindfleisch auf. Es hat einen ordentlichen Fettrand und der Knochen ist noch nicht gelöst. Auf Wunsch wäre das schnell geändert, doch die Kunden der Metzgerei Kumpel & Keulewollen das Fleisch meist so – wegen des Geschmacks. Sie wollen es zubereiten, das Fett und die Knochen nutzen.\n\nNeben der Fleischtheke ist die Wurstküche, in der Henri gerade dabei ist, Wurstmasse für eine Merguez zu würzen. Die pikante Bratwurst bekommt Chili, geräucherte Paprika, Salz und Pfeffer jeweils aus kleinen Tütchen. Fertige Mischungen sind tabu – genauso wie fertig zerlegtes, bereits bearbeitetes und anonymes Fleisch. Die Qualitätsansprüche der noch ziemlich jungen Berliner Metzgerei sind hoch. Und genau das kommuniziert sie auch gerne: Über 5.000 Fans hat die Metzgerei auf Facebook, über 260 Follower über Twitter.\n\nKumpel & Keule: anklopfen, Fragen stellen\n\nFür die Präsenz in den sozialen Medien sorgt Hendrik Haase, Blogger unter dem Namen Wurstsack, Künstler, Food-Aktivist und Mitgründer von Kumpel & Keule. Hendrik ist Mitorganisator von Schnippeldiskos, bei denen Menschen gemeinsam Gemüsesuppe aus krummen Möhren und übriggebliebenen Kartoffeln kochen, er macht sich für die Initiative Slow Food stark und fürs Metzgerhandwerk.\n\nDass die Mitarbeiter von Kumpel & Keule ihr Handwerk verstehen, möchten sie auch zeigen: Außer dem Lager im Keller der Markthalle haben alle Betriebsräume Glaswände und an den Fronten, an denen die Kunden vorbeilaufen und nicht selten interessiert stehen bleiben, kleine Fenster. Hier kann man anklopfen, Fragen stellen. Hier erklärt Metzgergeselle Henri gerade, welche Gewürze er in dem Fleischteig für die Merguez streut.\n\nDie gläserne Produktion ist quasi der Grundstein der Metzgerei Kumpel & Keule. Bei einem Event der Markthalle Neun, die bekannt ist für ihre Naschmärkte, Streetfood-Abende und für kleine Manufakturen, die hier ihre Kreationen verkaufen, zeigten Hendrik und Metzgermeister Jörg Förstera, der bis zur Gründung von Kumpel & Keule die Fleischabteilung des KaDeWe leitete, wie man Wurst macht und ernteten damit viel Aufmerksamkeit. Es war ihr erstes gemeinsames Projekt und daraus wurde mehr.\n\nShowbühne fürs Metzgerhandwerk\n\nGesucht und gefunden kam es einige Zeit später dazu, dass die beiden ihre eigene Metzgerei eröffneten und hier ihren Kunden zeigen, wie man dem Handwerk eine kleine Showbühne bereiten kann. Inhalt der Show: Tiere zerlegen, zerkleinern, Knochen auslösen, entscheiden, welches Stück Fleisch zu was verarbeitet wird, Wurst herstellen und Burgerpattys. Die Kreuzberger Metzger wollen zeigen, dass hier das ganze Tier verarbeitet wird. Die Tiere kommen hier als ganze Hälften auf den Arbeitstisch. Einige Teile werden einvakumiert. \"An einem Tag ein ganzes Tier zu verkaufen, das schaffen wir noch nicht\", sagt Hendrik.\n\nIn vielem lehnt sich Kumpel & Keule an das klassische Konzept von Metzgereien an: neben der Verkauf von Fleisch und Wurst, gibt es auch eine Imbissstation. Und doch scheint alles ein wenig anders zu laufen bzw. haben die beiden Gründer einen Ansatz, der das Klassische auch kritisieren soll.\n\nAber erst mal zum Imbiss: Bei Kumpel & Keule gibt es Burger. Lange haben die Mitarbeiter gemeinsam am perfekten Patty getüftelt. Es darf nicht zu dünn sein, denn dann wäre es zu trocken, nicht mit der Maschine gepresst, denn das macht es zu fest. \"Das Patty muss noch Struktur haben und es soll im Mund zerfallen\", schwärmt Hendrik und erklärt weiter, warum der Burger absichtlich eher spartanisch gehalten ist: das gute Fleisch soll man schmecken, nicht nur dicke, süße Soßen.\n\nSoße und Brot gehören zum Kumpel & Keule-Burger aber dennoch dazu: handgemacht bzw. mit einem Bäcker extra für den Burger kreiert. Das Patty wird aus dem selben Fleisch hergestellt, das auch als trocken gereiftes Steak in der Auslage liegt und hat deshalb seinen Preis: sieben Euro pro Burger.\n\nDem Absatz tut das aber nichts. Auch an Tagen, an denen die Metzgerei nicht vom Publikum der Markthalle profitieren kann, sondern auch ganz normale Stammkunden und Laufkundschaft setzt, gehen die Burger einer nach dem anderen über den Tresen. Slow Food-Vertreter Hendrik verteidigt das eigentlich typische Fast-Food: \"Auch unseren Burger gibt es auf die Hand, aber es ist ganz viel Slow Food drin.\" Ausschlaggebend sei, wie die Tiere aufgewachsen sind und das Fleisch verarbeitet wurde.\n\nHendrik Haase: \"Zertifizierungen sind nicht alles\"\n\nFür das Fleisch, das fertig bearbeitet in der Auslage landet, gelten bei Kumpel & Keule strenge Auswahlkriterien. Das Wichtigste auch hier: Transparenz. \"Wir schauen uns an, wo und wie die Tiere leben, was sie fressen und wie sie geschlachtet werden\", sagt Hendrik. Das sollen auch die Kunden sehen und so veröffentlicht Hendrik regelmäßig Fotos und kurze Berichte von seinen Besuchen bei den Landwirten im Internet.\n\nRegional und bio sind dabei Kriterien, die zwar wichtig seien, aber nicht als einzige über die Auswahl entscheiden. \"Ich nehme lieber ein Huhn, das noch nie Antibiotika erhalten hat, aber dafür kein Bio-Siegel besitzt, weil sich der kleine Hof die Zertifizierung nicht leisten kann, als eines, das eben gerade das Muss für das staatliche Bio-Siegel erfüllt\", sagt Hendrik mit Nachdruck. Antibiotika ist auch im Bio-Bereich in bestimmten Fällen erlaubt. Solche Kriterien anzulegen, erfordert allerdings eine tiefgehende Auseinandersetzung mit den Erzeugern, eine intensive Suche und den Aufbau langfristiger Kooperationen.\n\nKumpel & Keule setzt zudem auf alte Rassen wie das Schwäbisch Hällische Landschwein oder ein französisches Schwarzfederhuhn, darauf, dass das Fleisch lange reift bis es verarbeitet wird und dass die Tiere möglichst wenig Stress vor dem Schlachten haben. Sie werden extra einen Tag vorher zum Schlachthof gefahren. Die Fahrt dorthin ist nicht länger als ein paar Kilometer. \"Durch den Ruhetag sind die Tiere bei der Schlachtung entspannter, das hat wiederum Auswirkungen auf die Qualität und den Geschmack\", erklärt Hendrik.\n\nMit ihrem Ansatz, die Handgriffe des Berufs wieder stark in den Vordergrund zu stellen, eine Auseinandersetzung mit dem Konsument anzuregen und den Beruf des Metzgers auch im Netz derart hip zu präsentieren, punktet Kumpel & Keule auch beim Nachwuchs. Die eine Lehrstelle, die es eigentlich geben sollte, ist seit vergangenem Jahr besetzt und noch in diesem Jahr kommt eine neue dazu. Das Besondere: es wird eine Azubine, die für die Ausbildung bei Kumpel & Keule ihr Kunststudium abbricht. Ausbilder ist der Firmengründer Jörg Förstera.\n\nBewusster Konsum statt einfach nur vegan\n\nMit mehr Handwerk und mehr Transparenz will Hendrik auch die sogenannten Flexitarier bedienen, von denen es immer mehr gibt. Flexitarier sind Menschen, die lieber weniger und seltener, aber dafür gutes – und meist auch etwas teureres – Fleisch essen. \"Das Bewusstsein der Leute ändert sich, immer mehr hinterfragen, wie die Tiere aufwachsen, was Fleischessen für das Klima und die Tierhaltung für die Landwirtschaft bedeutet.\"\n\nDass im Moment vegane Lebensmittel derzeit gefragt sind, macht es deshalb nicht mutiger, heutzutage eine Metzgerei zu eröffnen. \"Die Zahl der wirklichen Veganer ist nicht so hoch wie es derzeit scheint\", sagt Hendrik. Aber dafür steige die Anzahl derjenigen, die bewusst konsumieren.",
      content = "",
      title = "Kumpel & Keule: Zielgruppe Flexitarier - dhz.net",
      processedTitle = "Kumpel & Keule: Zielgruppe Flexitarier",
      metaDescription = "Metzgern kann hip sein. Wichtige Voraussetzung: Zeigen, was man kann und wie es richtig geht. Darauf setzt die Berliner Metzgerei Kumpel & Keule und gewinnt dam",
      metaKeywords = "Metzgerhandwerk, Metzger, Metzgerei, Kumpel & Keule, Wurstsack, Hendrik Haase, Slow Food, Markthalle Neun,",
      lang = None,
      date = Some("2016-09-09T12:11:25"),
      links = List(Link("Metzgerei Kumpel & Keule", "http://www.kumpelundkeule.de/#willkommen")))

  }


  "lemonde" >> {
    //    val url = "http://www.lemonde.fr/football/article/2015/07/23/pep-guardiola-un-as-dans-la-manche-des-independantistes_4695701_1616938.html"
    //    check(extract(url),
    //      url = url,
    //      content = "Dans la planète Barça, Pep Guardiola est un demi-dieu. Entraîneur du FC Barcelone entre 2008 et 2012, il a fait remporter aux Blaugrana 14 titres officiels. Dont six en une seule année : 2009",
    //      title = "En Catalogne, Pep Guardiola, figure du Barça, se présente sur la liste indépendantiste",
    //      processedTitle = "En Catalogne, Pep Guardiola, figure du Barça, se présente sur la liste indépendantiste",
    //      metaDescription = "L’ancien entraîneur du FC Barcelone devrait clore la liste unitaire visant à exiger l’indépendance de la Catalogne lors des élections du 27 septembre.",
    //      metaKeywords = "",
    //      lang = Some("fr"),
    //      date = Some("2015-07-23T15:57:46"),
    //      links = List.empty)
    pending
  }

  "globoesporte" >> {
    val url = "http://globoesporte.globo.com/futebol/times/sao-paulo/noticia/2012/04/filho-do-gramado-leao-administra-o-sao-paulo-na-base-da-conversa.html"
    check(extract(url),
      url = url,
      content = "Emerson Leão não foi ao campo na manhã desta terça-feira no centro de treinamento do São Paulo",
      title = "'Filho do gramado', Leão administra o São Paulo na base da conversa | globoesporte.com",
      processedTitle = "'Filho do gramado', Leão administra o São Paulo na base da conversa",
      metaDescription = "Emerson Le&atilde;o cobra lideran&ccedil;a ao S&atilde;o Paulo (Foto: M&aacute;rio &Acirc;ngelo / Ag. Estado) Emerson Le&atilde;o n&atilde;o foi ao campo na manh&atilde; desta ter&ccedil;a-feira no centro de treinamento do S&atilde;o Paulo. Bem humorado e com roupa casual, preferiu acompanhar de longe ...",
      metaKeywords = "notícias, notícia, são paulo",
      lang = None,
      date = Some("2012-04-03T13:49"),
      links = List())
  }

  "opengraph" >> {
    val url = "http://internacional.elpais.com/internacional/2015/07/28/actualidad/1438076596_960360.html"

    extract(url).openGraphData must_==
      OpenGraphData(title = Some("La OTAN apoya con cautela la ofensiva turca contra el yihadismo"),
        siteName = Some("EL PAÍS"),
        url = Some(new URL(url)),
        description = Some("La Alianza se ha reunido este martes con carácter de urgencia a pedición de Ankara para tratar el avance del Estado Islámico"),
        image = Some(new URL("http://ep00.epimg.net/internacional/imagenes/2015/07/28/actualidad/1438076596_960360_1438078067_noticia_normal.jpg")),
        `type` = Some("article"),
        locale = None,
        publishedTime = Some(new DateTime(2015, 7, 29, 0, 0)))

  }

  "fcbarcelona" >> {
    val url = "http://www.fcbarcelona.com/club/detail/article/30-years-since-visit-of-pope-john-paul-ii"

    check(extract(url),
      url = url,
      content = "On November 7, 1982, the Camp Nou enjoyed a historic moment.",
      title = "30 years since visit of Pope John Paul II | FC Barcelona",
      processedTitle = "30 years since visit of Pope John Paul II",
      metaDescription = "This Wednesday is the 30th anniversary of mass given by Pope John Paul at the Camp Nou",
      metaKeywords = "Josep Lluís Núñez, Camp Nou, Club, Season 2012-2013",
      lang = Some("en"),
      date = None,
      links = List()
    )
  }

  "Daily Mail (date is malformed + publish_date misused)" >> {
    val url = "http://www.dailymail.co.uk/news/article-486484/A-spectacular-destruction-How-email-led-downfall-barrister-all.html"

    check(extract(url),
      url = url,
      content = "by PAUL BRACCHI Last updated at 01:01 09 October 2007 An Oxford First, a brilliant radio career and newly qualified as a barrister, Bruce Hyman seemed to have all life's gifts",
      title = "A spectacular destruction: How one email led to the downfall of a barrister who had it all | Daily Mail Online",
      processedTitle = "A spectacular destruction: How one email led to the downfall of a barrister who had it all",
      metaDescription = "An Oxford First, a brilliant radio career and newly qualified as a barrister, Bruce Hyman seemed to have all life's gifts  -  until a moment of utter madness put him behind bars and left his life in ruins",
      metaKeywords = "A,spectacular,destruction,How,email,led,downfall,barrister,all",
      lang = Some("en"),
      date = None,
      links = List()
    )
  }

}
