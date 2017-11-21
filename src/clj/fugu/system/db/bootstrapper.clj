(ns fugu.system.db.bootstrapper
  (:require
   [mount.core :refer [defstate]]
   [datomic-schema.schema :as dat.sch :refer [schema fields]]
   [io.rkn.conformity :as dat.cnf]
   [taoensso.timbre :as tmb]
   [taoensso.encore :as enc]
   [fugu.system.db :as sys.db]))

;; ============================================================================
;; Datomic bootstrapper
;; ============================================================================

(defmulti conformable-tx
  (fn [ns-k _]
    (first (enc/explode-keyword ns-k))))

(defmethod conformable-tx "schema"
  [_ tx]
  (dat.sch/generate-schema tx {:gen-all? true
                               :index-all? true}))

(defmethod conformable-tx "data"
  [_ tx]
  tx)

(defn generate-conformable
  [mapping]
  (reduce-kv (fn [m ns-k txes]
               (assoc m
                      ns-k
                      {:txes (mapv (partial conformable-tx ns-k) txes)}))
             {}
             mapping))

(defn new-db-bootstrapper
  [connection]
  (let [conformable (do (tmb/info "Generating conformable...")
                        (generate-conformable
                         {:schema/v1 [[(schema user
                                         (fields
                                          [id :string :unique-identity]
                                          [name :string]
                                          [email :string :unique-identity]
                                          [password :string]))]
                                      [(schema role
                                         (fields
                                          [id :string :unique-identity]
                                          [name :string :unique-identity]
                                          [creator :ref]
                                          [members :ref :many]))]]}))]
    (tmb/info "Ensure conforms...")
    (enc/do-true
     (dat.cnf/ensure-conforms connection conformable))))

(defstate db-bootstrapper
  :start (new-db-bootstrapper @sys.db/db-connection))
