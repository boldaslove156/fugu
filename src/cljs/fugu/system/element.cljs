(ns fugu.system.element
  (:require
   [goog.dom :as dom]
   [mount.core :refer-macros [defstate]]
   [datascript.core :as dat]
   [rum.core :as rum :refer-macros [defc]]
   [taoensso.timbre :as tmb :include-macros true]
   [fugu.system.db :as sys.db]
   [fugu.system.dispatcher :as sys.dpt]))

;; ============================================================================
;; Element
;; ============================================================================

(defn users-display
  [db dispatcher]
  (if-let [users (not-empty
                  (dat/q '{:find [[(pull ?eid pattern) ...]]
                           :in [$ pattern]
                           :where [[?eid :user/id]]}
                         db
                         [:user/id :user/name :user/email]))]
    [:ul
     (mapv (fn [user]
             [:div {:key (:user/id user)}
              [:li (:user/id user)]
              [:li (:user/name user)]
              [:li (:user/email user)]])
           users)]
    [:h1 "No User!"]))

(defc root-element
  [db dispatcher]
  [:div
   [:h1 "Hello World!"]
   (users-display db dispatcher)])

(defn mount-element
  [db dispatcher]
  (let [node (dom/getRequiredElement "app")
        mount-fn (fn [db']
                   (tmb/info "Mounting Element...")
                   (rum/mount (root-element db' dispatcher) node))
        stop-fn (fn []
                  (tmb/info "Unmounting Element...")
                  (rum/unmount node))]
    (mount-fn db)
    {:node node
     :mount mount-fn
     :stop stop-fn}))

(defn unmount-element
  [el]
  ((:stop el)))

(defstate element
  :start (mount-element (dat/db @sys.db/db-connection) @sys.dpt/dispatcher)
  :stop (unmount-element @element))
