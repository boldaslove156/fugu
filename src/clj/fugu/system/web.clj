(ns fugu.system.web
  (:require
   [mount.core :refer [defstate]]
   [aleph.http :as lph.htp]
   [taoensso.timbre :as tmb]
   [taoensso.encore :as enc]
   [fugu.system.config :as sys.cfg]
   [fugu.system.router :as sys.rtr]))

;; ============================================================================
;; Aleph
;; ============================================================================

(defn start-server
  [handler {config :aleph}]
  {:pre [(enc/have? map? config)
         (enc/have? enc/pos-int? (:port config))]}
  (tmb/info "Starting web server...")
  (lph.htp/start-server handler config))

(defn stop-server
  [server]
  (tmb/info "Stopping web server...")
  (.close server))

(defstate web-server
  :start (start-server @sys.rtr/router @sys.cfg/config)
  :stop (stop-server @web-server))
