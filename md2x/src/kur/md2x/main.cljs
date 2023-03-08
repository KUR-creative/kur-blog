(ns kur.md2x.main
  (:require [kur.blog.policy :as policy]
            [kur.md2x.wikilink.plugin :refer [enable-wikilink!]]))

(def mdit-a (js/require "markdown-it-anchor"))

(def mdit
  (doto ((js/require "markdown-it")
         #js{:linkify true, :html true, :breaks true})
    (.use (js/require "markdown-it-mark"))
    (.use (js/require "markdown-it-collapsible"))
    (.use mdit-a #js{:slugify policy/slugify
                     :permalink ((.. mdit-a -permalink -headerLink)
                                 #js{:class "only-cursor"})})
    (.use (js/require "markdown-it-footnote"))
    (.use (js/require "markdown-it-highlightjs"))
    (.use enable-wikilink!)))

(def exports
  #js{:obsidian (fn [md] ^js (.render mdit md))})

(comment
  (do
    (def cases
      [;; Invalid
       "[[]]"         "<p>[[]]</p>\n"
       "png]]"        "<p>png]]</p>\n"
       ;; Normal wikilink
       "[[png]]"            "<p><a href=\"png\">png</a></p>\n"
       "[[글댓비full]]"      "<p><a href=\"글댓비full\">글댓비full</a></p>\n"
       "[[kur2206020921.+.학습 주도 개발로 나만의 음악 앱 만들기]]"
       "<p><a href=\"kur2206020921.+.학습 주도 개발로 나만의 음악 앱 만들기\">kur2206020921.+.학습 주도 개발로 나만의 음악 앱 만들기</a></p>\n"
       ;; if not md(has extension), convert path under resource dir
       "[[abc.png]]"        "<p><a href=\"resource/abc.png\">abc.png</a></p>\n"
       "[[abc.png|aaasss]]" "<p><a href=\"resource/abc.png\">aaasss</a></p>\n"
       "[[글댓비full.png]]"  "<p><a href=\"resource/글댓비full.png\">글댓비full.png</a></p>\n"
       ;; with heading
       "[[aaa#bbb ccc ddd]]" "<p><a href=\"aaa#bbb-ccc-ddd\">aaa</a></p>\n"
       "[[aaa#bbb pipe|ddd]]" "<p><a href=\"aaa#bbb-pipe\">ddd</a></p>\n"
       "[[kur2302031338.+.블로그 프로젝트 - 블로그를 하다. 그리고 네이버 블로그를 버리다.#에디터가 구데기|이전 글]]"
       "<p><a href=\"kur2302031338.+.블로그 프로젝트 - 블로그를 하다. 그리고 네이버 블로그를 버리다.#에디터가-구데기\">이전 글</a></p>\n"
       ;; Embedded wikilink with correct/incorrect/no extension
       "![[abc.png|aaasss]]"        "<p><img src=\"resource/abc.png\" alt=\"aaasss\"></p>\n"
       "![[abc|aaas|ss]]"           "<p>![[abc|aaas|ss]]</p>\n"
       "![[abc.p.ng|aaas|ss]]"      "<p>![[abc.p.ng|aaas|ss]]</p>\n"
       "![[abc.p.s.xng|aaas|s|s]]"  "<p>![[abc.p.s.xng|aaas|s|s]]</p>\n"
       ;; Embedding img
       "![[abc.png]]"
       "<p><img src=\"resource/abc.png\" alt=\"resource/abc.png\"></p>\n"
       "![[abc.jpg]]"
       "<p><img src=\"resource/abc.jpg\" alt=\"resource/abc.jpg\"></p>\n"
       "![[abc.jpeg]]"
       "<p><img src=\"resource/abc.jpeg\" alt=\"resource/abc.jpeg\"></p>\n"
       "![[니가그렇다면_그런거겠지.gif]]"
       "<p><img src=\"resource/니가그렇다면_그런거겠지.gif\" alt=\"resource/니가그렇다면_그런거겠지.gif\"></p>\n"
       ;; Embedding video
       "![[abc.mp4]]"
       "<p><video src=\"resource/abc.mp4\" autoplay=\"\" muted=\"\" loop=\"\">abc.mp4</video></p>\n"
       ;; Trimming white space
       "aa [[abc.png  ]] bb"
       "<p>aa <a href=\"resource/abc.png\">abc.png</a> bb</p>\n"
       "[[aa ]]![[abc.png]] bb"
       "<p><a href=\"aa\">aa</a><img src=\"resource/abc.png\" alt=\"resource/abc.png\"> bb</p>\n"
       ;; Nested list
       "- [[abc.png]] [t](a)\n  - x [[bb.asd]] y"
       "<ul>\n<li><a href=\"resource/abc.png\">abc.png</a> <a href=\"a\">t</a>\n<ul>\n<li>x <a href=\"resource/bb.asd\">bb.asd</a> y</li>\n</ul>\n</li>\n</ul>\n"
       ;; mark
       "==test==" "<p><mark>test</mark></p>\n"
       ;; linkify
       "http://test.com"
       "<p><a href=\"http://test.com\">http://test.com</a></p>\n"
       ;; Collapsible
       "+++ summary \n hidden \n+++"
       "<details>\n<summary><span class=\"details-marker\">&nbsp;</span>summary</summary><p>hidden</p>\n</details>\n"
       ;; Heading id & link
       "# Te St 테스 트! ? !  "
       "<h1 id=\"Te-St-테스-트!-?-!\" tabindex=\"-1\"><a class=\"only-cursor\" href=\"#Te-St-테스-트!-?-!\">Te St 테스 트! ? !</a></h1>\n"
       "##  Te St 테스  트!!"
       "<h2 id=\"Te-St-테스-트!!\" tabindex=\"-1\"><a class=\"only-cursor\" href=\"#Te-St-테스-트!!\">Te St 테스  트!!</a></h2>\n"
       ;; Add footnote
       "Here is a footnote[^1] reference,[^1] and another.[^longnote]

[^1]: Here is the footnote.
[^longnote]: Here's one with multiple blocks.

    Subsequent paragraphs are indented to show that they
belong to the previous footnote." "<p>Here is a footnote<sup class=\"footnote-ref\"><a href=\"#fn1\" id=\"fnref1\">[1]</a></sup> reference,<sup class=\"footnote-ref\"><a href=\"#fn1\" id=\"fnref1:1\">[1:1]</a></sup> and another.<sup class=\"footnote-ref\"><a href=\"#fn2\" id=\"fnref2\">[2]</a></sup></p>\n<hr class=\"footnotes-sep\">\n<section class=\"footnotes\">\n<ol class=\"footnotes-list\">\n<li id=\"fn1\" class=\"footnote-item\"><p>Here is the footnote. <a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a> <a href=\"#fnref1:1\" class=\"footnote-backref\">↩︎</a></p>\n</li>\n<li id=\"fn2\" class=\"footnote-item\"><p>Here's one with multiple blocks.</p>\n<p>Subsequent paragraphs are indented to show that they<br>\nbelong to the previous footnote. <a href=\"#fnref2\" class=\"footnote-backref\">↩︎</a></p>\n</li>\n</ol>\n</section>\n"])

    (def md->html (.-obsidian exports))
    (keep (fn [[md html]]
            (let [out (md->html md)]
              (when-not (= html out)
                {:md md
                 :expected html
                 :actual out})))
          (partition 2 cases)))

  (md->html "![[abc.mp4]]")
  (md->html "[[글댓비full.png]]")
  (md->html "asd [[test]] aa")

  (md->html "# Te St 테스 트! ? !  ")

  (md->html "```c\n int a[10] = 200;")
  (md->html "```clojure\n (map inc [1 2 3 4 :a])")

  ;; test cljc
  (require '[kur.blog.page.post.name :as name])
  (name/id-info "kur1234567890")
  (name/fname->parts "kur1234567890.+.AAA sdws.md"))

