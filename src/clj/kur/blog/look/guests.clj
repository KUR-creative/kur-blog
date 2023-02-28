(ns kur.blog.look.guests
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [article-page]]))

(defn html []
  (html5 (article-page {:title "KUR Creative Blog - Guests"}
                       {:h1 "Guest Book (공사중...)"
                        :content "곧 댓글 기능과 함께 방명록이 추가됩니다."})))