(ns kur.blog.page.post
  (:require [babashka.fs :as fs]
            [kur.blog.page.post.diff :as post-diff]
            [kur.blog.obsidian.frontmatter :as frontmatter]
            [kur.blog.page.post.name :refer [fname->parts]]
            [kur.util.file-system :as uf]))

(defn post
  ([path] (post path (-> path fs/file-name str fname->parts)))
  ([path parts] ; parts should be parsed from path
   (if (fs/exists? path)
     (assoc parts
            :md-path path
            :last-modified-millis (uf/last-modified-millis path))
     nil))) ; non-exist post should be removed in state

(defn load-text [{path :md-path :as post}]
  (if (fs/exists? path)
    (let [md-str (slurp path)
          {:keys [frontmatter body]} (frontmatter/parse md-str)]
      (assoc post
             :frontmatter frontmatter
             :text (if frontmatter body md-str)))
    nil)) ; non-exist post should be removed in state

(defn url [id] ; TODO: refactor
  (str "http://" "localhost"
       ":" 3000
       "/" id))

(defn title-or-id [{:keys [id title]}]
  (if title
    title
    id))

;; Policies
(defn public? [post]
  (= "+" (:meta-str post)))

(defn html-file-name [post]
  (str (:id post) ".html"))

(defn how-update-html
  "(:happened happened-post) is ::post-diff/{delete, create, update}
   do not input post that (:happened post) = ::post-diff/as-is. 
   Because as-is post don't have to update."
  [happened-post]
  (if (or (not (public? happened-post)) ; hap이 create라도 del! 가능함
          (= (:happened happened-post) ::post-diff/delete))
    ::delete! ; TODO: 없는 html 삭제 시 예외 주의
    ::write!))

;;
(comment
  (def troot "test/fixture/blog-root/blog-md/")
  (post (str troot "kur2004250001.-.오버 띵킹의 함정을 조심하라.md"))
  (post (str troot "kur2206082055.Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다.md"))
  (post (str troot "kur2207281052.+.md")))