(ns fugu.system.dispatcher.listener.register
  #?(:clj
     (:require
      [taoensso.timbre :as tmb]
      [fugu.system.dispatcher :as sys.dpt]
      [fugu.system.dispatcher.listener :refer [act-on listen-to]])
     :cljs
     (:require
      [taoensso.timbre :as tmb :include-macros true]
      [fugu.system.dispatcher :as sys.dpt]
      [fugu.system.dispatcher.listener :refer [act-on listen-to]])))

;; ============================================================================
;; Register listener
;; ============================================================================

(listen-to
 :default
 (fn [db [event-id :as event]]
   (tmb/warn "Attempting to handle unknown event:" event-id)
   []))

(act-on
 :default
 (fn [services dispatcher [effect-id :as effect]]
   (tmb/warn "Attempting to handle unknown effect:" effect-id)
   []))

(act-on
 :dispatcher/event
 (fn [_ dispatcher [_ {:keys [event]}]]
   (sys.dpt/dispatch! dispatcher event)))

(listen-to
 :dispatcher/effects
 (fn [_ [_ {:keys [effects]}]]
   effects))
