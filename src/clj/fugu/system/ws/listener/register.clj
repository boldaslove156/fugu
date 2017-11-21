(ns fugu.system.ws.listener.register
  (:require
   [taoensso.timbre :as tmb]
   [fugu.system.ws :as sys.ws]
   [fugu.system.dispatcher.listener :refer [act-on]]
   [fugu.system.ws.listener :refer [listen-to-remote]]))

;; ============================================================================
;; Register listener
;; ============================================================================

(listen-to-remote
 :default
 (fn [_ {:keys [id]}]
   (tmb/warn "Attempting to handle unknown remote event:" id)
   []))

(listen-to-remote
 :chsk/ws-ping
 (fn [_ _]
   []))

(act-on
 :ws/send
 (fn [{:keys [ws]} _ [_ {:keys [user-id event]}]]
   (sys.ws/send! ws user-id event)))

(act-on
 :ws/broadcast
 (fn [{:keys [ws]} _ [_ {:keys [event]}]]
   (sys.ws/broadcast! ws event)))
