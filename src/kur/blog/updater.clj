(ns kur.blog.updater ;; TODO: Updater(delete also?)
  (:require [babashka.fs :as fs]
            [kur.blog.look.home :as look-home]
            [kur.blog.look.post :as look-post]
            [kur.blog.look.tags :as look-tags]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.diff :as post-diff]
            [kur.blog.page.post.name :as name]
            [kur.blog.page.tags :as tags]
            [kur.util.file-system :as uf]))

(defn post-set [md-dir]
  (->> (uf/path-seq md-dir)
       (keep #(when-let [parts (name/valid-parts %)]
                [% parts]))
       (map (fn [[path parts]] (post/post path parts)))
       set))

(defn classify-posts
  "Return post groups classified by file system change(delete, write, as-is)
   NOTE: unchanged(post)s inherit loaded text from old-posts."
  [old-posts now-posts]
  (def old-posts old-posts)
  (def now-posts now-posts)
  (let [mergeds
        (vec (post-diff/merge-and-assoc-happened old-posts now-posts))

        {unchangeds true changeds false}
        (group-by #(= ::post-diff/as-is (:happened %)) mergeds)

        {to-deletes ::post/delete!, to-writes ::post/write!}
        (group-by post/how-update-html changeds)

        map-rm-hap (fn [posts] (map #(dissoc % :happened) posts))]
    {:unchangeds (map-rm-hap unchangeds)
     :to-deletes (map-rm-hap to-deletes)
     :to-writes (map-rm-hap to-writes)}))

(defn site
  "Return commands to maintain html files of site
   commands are [[f & args]*]"
  [unchanged-posts post-to-delete loaded-posts-to-write html-dir]
  (let [unchangeds-and-writes (concat unchanged-posts loaded-posts-to-write)
        html-path #(str (fs/path html-dir %))]
    (concat
     (map (fn [post] [spit (html-path (post/html-file-name post))
                      (look-post/html nil (:text post))])
          loaded-posts-to-write)
     [[spit (html-path "tags.html")
       (look-tags/html (tags/tag:posts unchangeds-and-writes)
                       (filter #(not (tags/has-tags? %))
                               unchangeds-and-writes))]
      [spit (html-path "home.html")
       (look-home/html (sort-by :id unchangeds-and-writes))]]
     (map (fn [post]
            [fs/delete-if-exists (html-path (post/html-file-name post))])
          post-to-delete))))

(defn update! [site]
  (run! (fn [[f & args]] (apply f args)) site))

;;
(comment
  (def md-dir "test/fixture/blog-root/blog-md/")
  (def html-dir "test/fixture/blog-root/tmp-html/")

  (def old-state
    #{{:id "kur2207281052",
       :meta-str "+",
       :md-path "test/fixture/blog-root/blog-md/kur2207281052.+.md",
       :last-modified-millis 1673419774387}
      {:id "kur2207161305",
       :meta-str "+",
       :title "kill-current-sexp의 Emacs, VSCode 구현",
       :md-path "test/fixture/blog-root/blog-md/kur2207161305.+.kill-current-sexp의 Emacs, VSCode 구현.md",
       :last-modified-millis 1673419762417}
      {:id "kur0000000000",
       :meta-str "+",
       :md-path "test/fixture/blog-root/blog-md/kur0000000000.md",
       :last-modified-millis 1673419774387}
      {:id "kur2206082055",
       :title "Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다",
       :md-path "test/fixture/blog-root/blog-md/kur2206082055.Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다.md",
       :last-modified-millis 1673419825027}
      {:id "kur2301092038",
       :meta-str "+",
       :title "초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자",
       :md-path "test/fixture/blog-root/blog-md/kur2301092038.+.초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자.md",
       :last-modified-millis 1673419781187}
      {:id "kur2207111708",
       :title "Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다",
       :md-path
       "test/fixture/blog-root/blog-md/kur2207111708.Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다.md",
       :last-modified-millis 1673419745900}})
  (def new-state
    #{{:id "kur2207281052",
       :meta-str "+",
       :md-path "test/fixture/blog-root/blog-md/kur2207281052.+.md",
       :last-modified-millis 1673419774387}
      {:id "kur2207161305",
       :meta-str "+",
       :title "kill-current-sexp의 Emacs, VSCode 구현",
       :md-path "test/fixture/blog-root/blog-md/kur2207161305.+.kill-current-sexp의 Emacs, VSCode 구현.md",
       :last-modified-millis 1673419762417}
      {:id "kur2207111708",
       :meta-str "+",
       :title "Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다",
       :md-path
       "test/fixture/blog-root/blog-md/kur2207111708.Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다.md",
       :last-modified-millis 1673579652440}
      {:id "kur2206082055",
       :title "Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다",
       :md-path "test/fixture/blog-root/blog-md/kur2206082055.Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다.md",
       :last-modified-millis 1673419825027}
      {:id "kur2205182112",
       :title ".인간의 우열(편차)보다 사용하는 도구와 환경, 문화의 우열이 퍼포먼스에 더 큰 영향을 미친다",
       :md-path "test/fixture/blog-root/blog-md/kur2205182112..인간의 우열(편차)보다 사용하는 도구와 환경, 문화의 우열이 퍼포먼스에 더 큰 영향을 미친다.md",
       :last-modified-millis 1673391112645}
      {:id "kur2004250001",
       :meta-str "+",
       :title "오버 띵킹의 함정을 조심하라",
       :md-path "test/fixture/blog-root/blog-md/kur2004250001.-.오버 띵킹의 함정을 조심하라.md",
       :last-modified-millis 1673391124082}
      {:id "kur2301092038",
       :meta-str "+",
       :title "초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자",
       :md-path "test/fixture/blog-root/blog-md/kur2301092038.+.초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자.md",
       :last-modified-millis 1673419781187}})

  (update! (site #{} old-state html-dir))
  (update! (site old-state new-state html-dir)))