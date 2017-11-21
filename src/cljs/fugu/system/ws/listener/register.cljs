(ns fugu.system.ws.listener.register
  (:require
   [taoensso.timbre :as tmb :include-macros true]
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

(listen-to-remote
 :chsk/recv
 (fn [_ {:keys [?data]}]
   [[:dispatcher/event {:event ?data}]]))

(listen-to-remote
 :chsk/state
 (fn [_ {:keys [?data]}]
   (let [[old-state new-state] ?data]
     (if (:first-open? new-state)
       [[:dispatcher/event {:event [:db/bootstrap {}]}]]
       []))))

(act-on
 :ws/send
 (fn [{:keys [ws]} _ [_ {:keys [event args]}]]
   (apply sys.ws/send! ws event args)))
