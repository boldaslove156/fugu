(ns fugu.system.db.listener.register
  (:require
   [datomic.api :as dat]
   [fugu.system.dispatcher.listener :refer [act-on listen-to]]
   [fugu.system.ws.listener :refer [listen-to-remote]]
   [fugu.api.sync :as api.snc]))

;; ============================================================================
;; Register listener
;; ============================================================================

(act-on
 :db/transact
 (fn [{:keys [conn]} _ [_ {:keys [tx-data]}]]
   (dat/transact-async conn tx-data)))

(listen-to
 :db/broadcast-datoms
 (fn [_ [_ {:keys [datoms]}]]
   (let [event [:db/apply-datoms {:datoms datoms}]]
     [[:ws/broadcast {:event event}]])))

(listen-to-remote
 :db/apply-datoms
 (fn [db {:keys [?data]}]
   (let [tx-data (api.snc/local-tx db (:datoms ?data))]
     [[:db/transact {:tx-data tx-data}]])))

(listen-to-remote
 :db/bootstrap
 (fn [db {:keys [uid]}]
   (let [pull-data (api.snc/bootstrap db)
         event [:db/apply-pull-data {:data pull-data}]]
     [[:ws/send {:user-id uid
                 :event event}]])))
