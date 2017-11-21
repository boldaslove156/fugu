(ns fugu.system.ws.listener
  #?(:clj
     (:require
      [clojure.core.async :as asn]
      [mount.core :refer [defstate]]
      [datomic.api :as dat]
      [taoensso.timbre :as tmb]
      [fugu.system.ws :as sys.ws]
      [fugu.system.dispatcher :as sys.dpt]
      [fugu.system.dispatcher.listener :as sys.dpt.lst]
      [fugu.system.db :as sys.db])
     :cljs
     (:require
      [cljs.core.async :as asn]
      [mount.core :refer-macros [defstate]]
      [datascript.core :as dat]
      [taoensso.timbre :as tmb :include-macros true]
      [fugu.system.ws :as sys.ws]
      [fugu.system.dispatcher :as sys.dpt]
      [fugu.system.dispatcher.listener :as sys.dpt.lst]
      [fugu.system.db :as sys.db])))

;; ============================================================================
;; WS listener
;; ============================================================================

(defmulti handle-remote-event
  (fn [db {:keys [id] :as remote-event}]
    id))

(defn listen-to-remote
  [remote-event-id handler]
  (defmethod handle-remote-event remote-event-id
    [db remote-event]
    (handler db remote-event)))

(defn event-callback
  [db-conn dispatcher event]
  (let [db (dat/db db-conn)
        effects (handle-remote-event db event)]
    (sys.dpt/dispatch! dispatcher [:dispatcher/effects {:effects effects}])))

(defn start-ws-listener
  [ws-server db-conn dispatcher]
  (tmb/info "Starting ws listener...")
  (let [listen-ch (sys.ws/listen-ch ws-server)
        kill-ch (asn/chan)
        callback (partial event-callback db-conn dispatcher)]
    (sys.dpt.lst/start-listening listen-ch kill-ch callback)
    kill-ch))

(defn stop-ws-listener
  [ws-listener']
  (tmb/info "Stopping ws listener...")
  (asn/close! ws-listener'))

(defstate ws-listener
  :start (start-ws-listener @sys.ws/ws-server
                            @sys.db/db-connection
                            @sys.dpt/dispatcher)
  :stop (stop-ws-listener @ws-listener))
