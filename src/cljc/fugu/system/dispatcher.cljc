(ns fugu.system.dispatcher
  #?(:clj
     (:require
      [clojure.core.async :as asn]
      [mount.core :refer [defstate]]
      [taoensso.timbre :as tmb])
     :cljs
     (:require
      [cljs.core.async :as asn]
      [mount.core :refer-macros [defstate]]
      [taoensso.timbre :as tmb :include-macros true])))

;; ============================================================================
;; Dispatcher
;; ============================================================================

(defn new-dispatcher
  []
  (tmb/info "Creating new dispatcher...")
  (asn/chan 256))

(defn close-dispatcher
  [dispatcher']
  (tmb/info "Closing the dispatcher...")
  (asn/close! dispatcher'))

(defstate dispatcher
  :start (new-dispatcher)
  :stop (close-dispatcher @dispatcher))

(defn dispatch!
  [dispatcher' event]
  (asn/put! dispatcher' event))
