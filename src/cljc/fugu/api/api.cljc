(ns fugu.api.api
  #?(:clj
     (:require
      [datomic.api :as dat])
     :cljs
     (:require
      [datascript.core :as dat])))

;; ============================================================================
;; Generic
;; ============================================================================

(defn datom->data
  [[e a v t added?]]
  (let [db-fn {true :db/add
               false :db/retract}]
    [(db-fn added?) e a v]))

(def ref-attrs
  #{:db/unique :db/valueType :db/cardinality})

(defn attr-ref?
  [db attr]
  (or (boolean (ref-attrs attr))
      (= :db.type/ref
         (dat/q '{:find [?val-type .]
                  :in [$ ?ident]
                  :where [[?ident-eid :db/ident ?ident]
                          [?ident-eid :db/valueType ?val-type-eid]
                          [?val-type-eid :db/ident ?val-type]]}
                db
                attr))))
