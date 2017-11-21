(ns fugu.system.db
  (:require
   [mount.core :refer [defstate]]
   [datomic.api :as dat]
   [fugu.system.config :as sys.cfg]
   [taoensso.timbre :as tmb]
   [taoensso.encore :as enc]))

;; ============================================================================
;; Datomic
;; ============================================================================

(defn uri
  [{db-type :type db-name :name}]
  {:pre [(enc/have? [:el #{:mem}] db-type)
         (enc/have? enc/nblank-str? db-name)]}
  (format "datomic:%s://fugu-%s" (name db-type) db-name))

(defn new-db-conn
  [{config :datomic}]
  {:pre [(enc/have? map? config)]}
  (tmb/info "Connecting to database...")
  (let [uri' (uri config)]
    (dat/create-database uri')
    (dat/connect uri')))

(defn release-db-conn
  [conn]
  (tmb/info "Releasing database connection...")
  (dat/release conn))

(defstate db-connection
  :start (new-db-conn @sys.cfg/config)
  :stop (release-db-conn @db-connection))
