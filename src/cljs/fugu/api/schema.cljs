(ns fugu.api.schema
  (:require
   [datascript.core :as dat]
   [taoensso.encore :as enc :include-macros true]
   [fugu.api.api :as api.api]
   [fugu.helpers :as hlp]))

;; ============================================================================
;; Datascript schema
;; ============================================================================

(defn merge-schema
  [ds-connection' next-schema-map]
  (swap! ds-connection'
         (fn [db next-schema-map']
           (let [prev-schema-map (:schema db)]
             (dat/init-db (dat/datoms db :eavt)
                          (hlp/deep-merge prev-schema-map next-schema-map'))))
         next-schema-map))

(def schema-pattern
  ['*
   (into {}
         (map (fn [attr]
                [attr ['*]]))
         api.api/ref-attrs)])

(defn replace-ident
  [schema attr]
  (hlp/update-when schema attr :db/ident))

(defn process-value-type
  [schema]
  (if (= :db.type/ref (:db/valueType schema))
    schema
    (dissoc schema :db/valueType)))

(defn changing-schema-map
  [{:keys [db-after tx-data]}]
  (->> tx-data
       (into [] (keep (fn [[eid attr]]
                        (when (= :db/ident attr)
                          eid))))
       (dat/pull-many db-after schema-pattern)
       (transduce (map
                   (fn [{attr :db/ident :as schema}]
                     {attr (->> api.api/ref-attrs
                                (reduce replace-ident
                                        (dissoc schema :db/ident))
                                (process-value-type))}))
                  enc/merge)))
