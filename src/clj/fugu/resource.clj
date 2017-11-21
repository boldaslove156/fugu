(ns fugu.resource
  (:require
   [clojure.java.io :as jio]
   [rum.core :as rum :refer [defc]]
   [ring.util.http-response :as rg.res]
   [taoensso.encore :as enc]))

;; ============================================================================
;; Asset Resource
;; ============================================================================

(defn asset-resource
  [{:keys [request-method] :as request}]
  (if (= :get request-method)
    (let [file (jio/file (subs (:uri request) 1))]
      (if (and (.exists file) (.isFile file))
        (rg.res/ok file)
        (rg.res/not-found)))
    (rg.res/method-not-allowed)))

;; ============================================================================
;; Index Resource
;; ============================================================================

(defn asset
  [path]
  (str "resources/public/" path))

(defc index-page
  [config]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
    [:title "fugu"]]
   [:body
    [:#app]
    [:script {:type "text/javascript"
              :src (asset "js/fugu/app.js")}]]])

(defn index-resource
  [{:keys [services request-method] :as request}]
  (if (= :get request-method)
    (let [config (:config services)]
      (-> (rum/render-html (index-page config))
          (rg.res/ok)
          (rg.res/header "content-type" "text/html")))
    (rg.res/method-not-allowed)))
