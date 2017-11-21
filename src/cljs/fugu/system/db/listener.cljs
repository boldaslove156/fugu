(ns fugu.system.db.listener
  (:require
   [mount.core :refer-macros [defstate]]
   [datascript.core :as dat]
   [taoensso.timbre :as tmb :include-macros true]
   [taoensso.encore :as enc :include-macros true]
   [fugu.system.db :as sys.db]
   [fugu.system.dispatcher :as sys.dpt]
   [fugu.api.sync :as api.snc]
   [fugu.api.schema :as api.sch]))

;; ============================================================================
;; Datascript listener callback
;; ============================================================================

(defn sync?
  [tx-meta]
  (:db/sync? tx-meta true))

(defn new-listener-callback
  [dispatcher]
  (tmb/info "Constructing db listener callback...")
  {:sync (fn [{:keys [tx-meta] :as tx-report}]
           (when (sync? tx-meta)
             (let [datoms (api.snc/delta-datoms tx-report)
                   event [:db/send-datoms {:datoms datoms}]]
               (sys.dpt/dispatch! dispatcher event))))
   :component (fn [{:keys [db-after]}]
                (let [effect [:element/mount {:db db-after}]
                      event [:dispatcher/effects {:effects [effect]}]]
                  (sys.dpt/dispatch! dispatcher event)))
   :schema (fn [tx-report]
             (enc/when-let [schema-map (api.sch/changing-schema-map tx-report)
                            effect [:db/merge-schema {:schema-map schema-map}]
                            event [:dispatcher/effects {:effects [effect]}]]
               (sys.dpt/dispatch! dispatcher event)))
   :debug (fn [_]
            (tmb/debug "CHANGED!"))})

(defstate listener-callback
  :start (new-listener-callback @sys.dpt/dispatcher))

;; ============================================================================
;; Datascript listener
;; ============================================================================

(defn start-db-listener!
  [db-conn k->cb]
  (tmb/info "Starting db listener...")
  (enc/do-true
   (doseq [[k cb] k->cb]
     (dat/listen! db-conn k cb))))

(defn stop-db-listener!
  [db-conn k->cb]
  (tmb/info "Stopping db listener...")
  (enc/do-false
   (doseq [[k] k->cb]
     (dat/unlisten! db-conn k))))

(defstate db-connection-listener
  :start (start-db-listener! @sys.db/db-connection @listener-callback)
  :stop (stop-db-listener! @sys.db/db-connection @listener-callback))
