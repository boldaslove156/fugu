(ns fugu.system.db.listener.register
  (:require
   [datascript.core :as dat]
   [fugu.system.dispatcher.listener :refer [act-on listen-to]]
   [fugu.api.sync :as api.snc]
   [fugu.api.schema :as api.sch]))

;; ============================================================================
;; Register listener
;; ============================================================================

(act-on
 :db/transact
 (fn [{:keys [conn]} _ [_ {:keys [tx-data tx-meta]}]]
   (if (map? tx-meta)
     (dat/transact-async conn tx-data tx-meta)
     (dat/transact-async conn tx-data))))

(listen-to
 :db/send-datoms
 (fn [_ [_ {:keys [datoms]}]]
   (let [event [:db/apply-datoms {:datoms datoms}]]
     [[:ws/send {:event event}]])))

(listen-to
 :db/apply-datoms
 (fn [db [_ {:keys [datoms]}]]
   (let [tx-data (api.snc/local-tx db datoms)]
     [[:db/transact {:tx-data tx-data
                     :tx-meta {:db/sync? false}}]])))

(listen-to
 :db/apply-pull-data
 (fn [db [_ {:keys [data]}]]
   (let [tx-data (api.snc/local-tx db (api.snc/datomize data))]
     [[:db/transact {:tx-data tx-data
                     :tx-meta {:db/sync? false}}]])))

(listen-to
 :db/bootstrap
 (fn [_ _]
   [[:ws/send {:event [:db/bootstrap {}]}]]))

(act-on
 :db/merge-schema
 (fn [{:keys [conn]} _ [_ {:keys [schema-map]}]]
   (api.sch/merge-schema conn schema-map)))
