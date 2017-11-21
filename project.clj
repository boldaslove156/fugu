(defproject fugu "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [;; clj
                 [org.clojure/clojure "1.9.0-RC1"]
                 [com.google.guava/guava "23.0"]
                 [aleph "0.4.4"]
                 [aero "1.1.2"]
                 [com.datomic/datomic-free "0.9.5561.62"]
                 [datomic-schema "1.3.0"]
                 [io.rkn/conformity "0.5.1"]
                 [ring/ring-defaults "0.3.1"]
                 [metosin/ring-http-response "0.9.0"]
                 [com.cognitect/transit-clj "0.8.300"]
                 ;; cljs
                 [org.clojure/clojurescript "1.9.946"]
                 [datascript "0.16.2"]
                 [rum "0.10.8"]
                 [com.cognitect/transit-cljs "0.8.243"]
                 ;; cljc
                 [org.clojure/core.async "0.3.465"]
                 [mount "0.1.11"]
                 [bidi "2.1.2"]
                 [com.taoensso/sente "1.11.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.taoensso/encore "2.92.0"]]
  :source-paths ["src/clj" "src/cljc"]
  :clean-targets ^{:protect false} [:target-path "resources/public/js/fugu"]
  :profiles {:dev {:source-paths ["src/cljs" "dev/clj"]
                   :dependencies [[org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.14"]]
                   :plugins [[refactor-nrepl "2.4.0-SNAPSHOT"]
                             [cider/cider-nrepl "0.16.0-SNAPSHOT"]]
                   :repl-options {:nrepl-middleware
                                  [cemerick.piggieback/wrap-cljs-repl]}}}
  :aliases {"dev" ["with-profile" "+dev"]
            "dev-repl" ["dev" "repl" ":headless"]
            "fresh-dev-repl" ["do" "clean," "dev-repl"]})

;; ;; TODO:
;; 1. How to validate new/existing data as a whole
;; 2. How to generate data
;; 3. Authentication
;; 4. Authorization
;; 5. How to add date created for each datom
