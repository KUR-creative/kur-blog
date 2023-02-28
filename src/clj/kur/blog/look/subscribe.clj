(ns kur.blog.look.subscribe
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [article-page]]))

(defn html []
  (html5 (article-page {:title "KUR Creative Blog - Subscribe"}
                       {:h1 "Subscribe (공사중...)"
                        :content "곧 이메일 구독 기능이 추가됩니다."})))