(ns fugu.api.sync
  (:require
   [datomic.api :as dat]
   [taoensso.encore :as enc]
   [fugu.api.api :as api.api]))

;; ============================================================================
;; Send datoms
;; ============================================================================

(defn delta-datoms
  [{:keys [db-after tx-data] :as tx-report}]
  (dat/q '{:find [?e ?aname ?v ?t ?added]
           :in [$ [[?e ?a ?v ?t ?added]]]
           :where [[?a :db/ident ?aname]]}
         db-after
         tx-data))

;; ============================================================================
;; Accept datoms
;; ============================================================================

(defn translate-remote-eid
  ([db attr ?eid]
   (if (api.api/attr-ref? db attr)
     (translate-remote-eid ?eid)
     ?eid))
  ([eid]
   (if (enc/neg-int? eid)
     (dat/tempid :db.part/user eid)
     eid)))

(defn translate-remote-data
  [db [db-fn eid attr ?eid]]
  (let [translated-eid (translate-remote-eid eid)
        ?translated-eid (translate-remote-eid db attr ?eid)]
    [db-fn translated-eid attr ?translated-eid]))

(defn local-tx
  [db datoms]
  (into []
        (comp (map api.api/datom->data)
           (map (partial translate-remote-data db)))
        datoms))

;; ============================================================================
;; Bootstrap
;; ============================================================================

(defn bootstrap
  [db]
  (->> (dat/datoms db :eavt)
       (into [] (comp (map (fn [[e a v t]] e))
                   (enc/xdistinct)))
       (dat/pull-many db '[*])
       (into [] (filter (comp not :db/fn)))))
