(ns fugu.system.db
  (:require
   [mount.core :refer-macros [defstate]]
   [datascript.core :as dat]
   [taoensso.timbre :as tmb :include-macros true]))

;; ============================================================================
;; Datascript
;; ============================================================================

(defn new-db-conn
  []
  (tmb/info "Connecting to database...")
  (dat/create-conn
   {:db.remote/id {:db/unique :db.unique/identity}
    :db/ident {:db/unique :db.unique/identity}
    :db/valueType {:db/valueType :db.type/ref}
    :db/unique {:db/valueType :db.type/ref}
    :db/cardinality {:db/valueType :db.type/ref}}))

(defstate db-connection
  :start (new-db-conn)
  :stop ::stop)
