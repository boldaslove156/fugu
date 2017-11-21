(ns fugu.system.db.listener
  (:require
   [mount.core :refer [defstate]]
   [datomic.api :as dat]
   [taoensso.timbre :as tmb]
   [fugu.system.db :as sys.db]
   [fugu.system.dispatcher :as sys.dpt]
   [fugu.api.sync :as api.snc]))

;; ============================================================================
;; Datomic listener
;; ============================================================================

(defn start-db-listener
  [db-conn dispatcher]
  (tmb/info "Starting db listener...")
  (let [tx-report-queue (dat/tx-report-queue db-conn)]
    (future
      (loop []
        (let [tx-report (.take tx-report-queue)
              datoms (api.snc/delta-datoms tx-report)
              event [:db/broadcast-datoms {:datoms datoms}]]
          (sys.dpt/dispatch! dispatcher event)
          (recur))))))

(defn stop-db-listener
  [db-conn]
  (tmb/info "Stopping db listener...")
  (dat/remove-tx-report-queue db-conn))

(defstate db-listener
  :start (start-db-listener @sys.db/db-connection @sys.dpt/dispatcher)
  :stop (stop-db-listener @sys.db/db-connection))
