(ns fugu.api.sync
  (:require
   [datascript.core :as dat]
   [taoensso.encore :as enc :include-macros true]
   [fugu.api.api :as api.api]))

;; ============================================================================
;; Send datoms
;; ============================================================================

(defn find-remote-eid
  [db local-eid]
  (dat/q '{:find [?translated-eid .]
           :in [$ ?local-eid]
           :where [[?local-eid :db.remote/id ?translated-eid]]}
         db
         local-eid))

(defn translate-local-eid
  ([db attr ?eid]
   (if (api.api/attr-ref? db attr)
     (translate-local-eid db ?eid)
     ?eid))
  ([db eid]
   (if-let [translated-eid (find-remote-eid db eid)]
     translated-eid
     (dat/tempid :db.part/user eid))))

(defn translate-local-datom
  [db [eid attr ?eid t added?]]
  (let [translated-eid (translate-local-eid db eid)
        ?translated-eid (translate-local-eid db attr ?eid)]
    [translated-eid attr ?translated-eid t added?]))

(defn delta-datoms
  [{:keys [db-after tx-data]}]
  (into [] (map (partial translate-local-datom db-after)) tx-data))

;; ============================================================================
;; Accept datoms
;; ============================================================================

(defn find-local-eid
  [db remote-eid]
  (dat/q '{:find [?local-eid .]
           :in [$ ?remote-eid]
           :where [[?local-eid :db.remote/id ?remote-eid]]}
         db
         remote-eid))

(defn translate-remote-eid
  [eid-mapping db eid]
  (enc/cond
    :let [translated-eid (get eid-mapping eid)]

    (some? translated-eid)
    translated-eid

    :let [translated-eid (find-local-eid db eid)]

    (some? translated-eid)
    translated-eid

    :else (dat/tempid :db.part/user eid)))

(defn process-remote-data
  [eid-mapping db [db-fn eid attr ?eid]]
  (let [translated-eid (translate-remote-eid eid-mapping db eid)
        eid-map {eid translated-eid}
        ref? (api.api/attr-ref? db attr)
        ?translated-eid (if ref?
                          (translate-remote-eid (enc/merge eid-mapping eid-map)
                                                db
                                                ?eid)
                          ?eid)]
    {:data [db-fn translated-eid attr ?translated-eid]
     :eid-mapping (enc/assoc-when eid-map ?eid (when ref?
                                                 ?translated-eid))}))

(defn reduce-remote-data
  [db]
  (fn data-reducer
    ([]
     {})
    ([{:keys [tx-data eid-mapping]}]
     (into tx-data
           (comp (filter (comp enc/neg-int? second))
              (map (fn [[remote-eid local-eid]]
                     [:db/add local-eid :db.remote/id remote-eid])))
           eid-mapping))
    ([{:keys [eid-mapping] :as container} data]
     (let [result (process-remote-data eid-mapping db data)]
       (-> container
           (update :tx-data conj (:data result))
           (update :eid-mapping enc/merge (:eid-mapping result)))))))

(defn local-tx
  [db datoms]
  (transduce (map api.api/datom->data)
             (reduce-remote-data db)
             datoms))

;; ============================================================================
;; Datomize pull result
;; ============================================================================

(declare new-datom)

(defn map->datom
  [m]
  (let [eid (enc/have (:db/id m))]
    (vec (mapcat (fn [[attr value]]
                   (new-datom eid attr value nil true))
                 (dissoc m :db/id)))))

(defn new-datom
  [eid attr value t added?]
  (enc/cond
    (map? value)
    (conj (map->datom value)
          [eid attr (:db/id value) t added?])

    (coll? value)
    (vec (mapcat #(new-datom eid attr % t added?) value))

    :else [[eid attr value t added?]]))

(defn datomize
  [x]
  (enc/cond!
   (map? x)
   (map->datom x)

   (coll? x)
   (vec (mapcat map->datom x))))
