(ns fugu.system.config
  (:require
   [mount.core :as mnt :refer [defstate]]
   [aero.core :as aro]
   [taoensso.timbre :as tmb]
   [taoensso.encore :as enc]))

;; ============================================================================
;; Aero
;; ============================================================================

(defn read-config
  [{:keys [profile] :as option}]
  {:pre [(enc/have? [:el #{:dev}] profile)]}
  (tmb/info "Reading config...")
  (let [source "resources/private/edn/fugu/config.edn"
        base-config (aro/read-config source option)]
    (assoc base-config :option option)))

(defstate config
  :start (read-config (mnt/args)))
