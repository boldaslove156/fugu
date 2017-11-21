(ns fugu.system.ws
  #?(:clj
     (:require
      [clojure.core.async :as asn]
      [mount.core :refer [defstate]]
      [taoensso.sente.server-adapters.aleph :as snt.adp.lph]
      [taoensso.sente.packers.transit :as snt.pck.trt]
      [taoensso.sente :as snt]
      [taoensso.timbre :as tmb]
      [taoensso.encore :as enc])
     :cljs
     (:require
      [cljs.core.async :as asn]
      [mount.core :refer-macros [defstate]]
      [taoensso.sente.packers.transit :as snt.pck.trt]
      [taoensso.sente :as snt]
      [taoensso.timbre :as tmb :include-macros true]
      [taoensso.encore :as enc :include-macros true])))

;; ============================================================================
;; WS server
;; ============================================================================

(defn start-ws-server
  []
  (tmb/info "Starting ws server...")
  (let [target #?(:clj (snt.adp.lph/get-sch-adapter)
                  :cljs "/chsk")
        option #?(:clj {:packer (snt.pck.trt/get-transit-packer)}
                  :cljs {:packer (snt.pck.trt/get-transit-packer)
                         :type :auto})]
    (snt/make-channel-socket! target option)))

(declare listen-ch)

(defn stop-ws-server
  [server]
  (tmb/info "Starting ws server...")
  (asn/close! (listen-ch server)))

(defstate ws-server
  :start (start-ws-server)
  :stop (stop-ws-server @ws-server))

(defn listen-ch
  [server]
  (:ch-recv server))

#?(:clj
   (defn send!
     [server user-id event]
     (let [send' (:send-fn server)]
       (send' user-id event)))
   :cljs
   (defn send!
     [server event & more]
     (let [send' (:send-fn server)]
       (apply send' event more))))

#?(:clj
   (defn broadcast!
     [server event]
     (let [uids (:connected-uids server)]
       (doseq [uid (:any @uids)]
         (send! server uid event)))))
