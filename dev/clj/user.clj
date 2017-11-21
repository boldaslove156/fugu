(ns user
  (:require
   [mount.core :as mnt]
   [figwheel-sidecar.repl-api :as fwra]
   [datomic.api :as dat]
   [fugu.system.db :as sys.db]
   [fugu.app]))

(def config
  {:server-port 9090
   :builds {:dev-complete
            {:source-paths ["src/cljs" "src/cljc"]
             :figwheel true
             :compiler {:main "fugu.app"
                        :output-to "resources/public/js/fugu/app.js"
                        :output-dir "resources/public/js/fugu/out"
                        :source-map true
                        :optimizations :none
                        :pretty-print true}}}})



(defn start!
  []
  (do (mnt/start-with-args {:profile :dev})
      (fwra/start-figwheel! config)))

(defn stop!
  []
  (do (mnt/stop)
      (fwra/stop-figwheel!)))

(defn browser-repl!
  []
  (fwra/cljs-repl))

(defn simple-user
  ([id name]
   (let [email (str name "@" name "." name)]
     (simple-user id name email)))
  ([id name email]
   {:user/id id
    :user/name name
    :user/email email}))

(defn transact-async
  [tx-data]
  (dat/transact-async @sys.db/db-connection tx-data))
