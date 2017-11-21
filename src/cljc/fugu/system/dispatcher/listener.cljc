(ns fugu.system.dispatcher.listener
  #?(:clj
     (:require
      [clojure.core.async :as asn :refer [go-loop]]
      [mount.core :refer [defstate]]
      [datomic.api :as dat]
      [taoensso.timbre :as tmb]
      [fugu.system.dispatcher :as sys.dpt]
      [fugu.system.ws :as sys.ws]
      [fugu.system.db :as sys.db])
     :cljs
     (:require
      [cljs.core.async :as asn]
      [mount.core :refer-macros [defstate]]
      [datascript.core :as dat]
      [taoensso.timbre :as tmb :include-macros true]
      [fugu.system.dispatcher :as sys.dpt]
      [fugu.system.ws :as sys.ws]
      [fugu.system.db :as sys.db]
      [fugu.system.element :as sys.el]))
  #?(:cljs
     (:require-macros
      [cljs.core.async.macros :refer [go-loop]])))

;; ============================================================================
;; Listener
;; ============================================================================

(defmulti handle-effect!
  (fn [services dispatcher [effect-id :as effect]]
    effect-id))

(defn act-on
  [effect-id handler]
  (defmethod handle-effect! effect-id
    [services dispatcher effect]
    (handler services dispatcher effect)))

(defn handle-effects!
  [services dispatcher effects]
  (doseq [effect effects]
    (handle-effect! services dispatcher effect)))

(defmulti handle-event
  (fn [db [event-id :as event]]
    event-id))

(defn listen-to
  [event-id handler]
  (defmethod handle-event event-id
    [db event]
    (handler db event)))

(defn event-callback
  [services dispatcher [_ _ db :as event]]
  (let [db (or db (dat/db (:conn services)))]
    (->> event
         (handle-event db)
         (handle-effects! services dispatcher))))

(defn start-listening
  [listen-ch kill-ch callback]
  (tmb/info "Start listening...")
  (go-loop []
    (let [[event ch] (asn/alts! [listen-ch kill-ch] :priority true)]
      (when (and (not= kill-ch ch)
                 (some? event))
        (callback event)
        (recur)))))

(defn start-listener
  [services dispatcher]
  (tmb/info "Starting listener...")
  (let [kill-ch (asn/chan)
        callback (partial event-callback services dispatcher)]
    (start-listening dispatcher kill-ch callback)
    kill-ch))

(defn stop-listener
  [listener']
  (tmb/info "Stopping listener...")
  (asn/close! listener'))

(defstate listener
  :start (start-listener #?(:clj {:ws @sys.ws/ws-server
                                  :conn @sys.db/db-connection}
                            :cljs {:ws @sys.ws/ws-server
                                   :conn @sys.db/db-connection
                                   :el @sys.el/element})
                         @sys.dpt/dispatcher)
  :stop (stop-listener @listener))
