(ns fugu.system.router
  (:require
   [mount.core :refer [defstate]]
   [bidi.ring :as bd.rg]
   [ring.middleware.defaults :as rg.mdw.def]
   [taoensso.timbre :as tmb]
   [fugu.system.config :as sys.cfg]
   [fugu.system.ws :as sys.ws]
   [fugu.resource :as rsc]))

;; ============================================================================
;; Middleware
;; ============================================================================

(defn wrap-trailing-slash
  [handler]
  (fn [request]
    (let [uri (:uri request)]
      (handler (assoc request :uri (if (and (not= "/" uri)
                                            (.endsWith uri "/"))
                                     (subs uri 0 (dec (count uri)))
                                     uri))))))

(defn wrap-default
  [handler]
  (rg.mdw.def/wrap-defaults handler rg.mdw.def/site-defaults))

(defn wrap-config
  [handler config]
  (fn [request]
    (handler (assoc-in request [:services :config] config))))

(defn new-middleware
  [config]
  (tmb/info "Creating middleware...")
  (fn [handler]
    (-> handler
        (wrap-default)
        (wrap-config config)
        (wrap-trailing-slash))))

(defstate middleware
  :start (new-middleware @sys.cfg/config))

;; ============================================================================
;; Router
;; ============================================================================

(defn routes
  [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn] :as ws-server}]
  ["" [["/chsk" [[:get ajax-get-or-ws-handshake-fn]
                 [:post ajax-post-fn]]]
       ["/resources/public" [[true rsc/asset-resource]]]
       ["/" rsc/index-resource]]])

(defn new-router
  [middleware' routes']
  (tmb/info "Creating router...")
  (middleware' (bd.rg/make-handler routes')))

(defstate router
  :start (new-router @middleware (routes @sys.ws/ws-server)))
