(ns fugu.system.element.listener.register
  (:require
   [taoensso.encore :as enc :include-macros true]
   [fugu.system.dispatcher.listener :refer [act-on]]))

;; ============================================================================
;; Register Listener
;; ============================================================================

(act-on
 :element/mount
 (fn [{:keys [el]} _ [_ {:keys [db]}]]
   ((:mount el) db)))
