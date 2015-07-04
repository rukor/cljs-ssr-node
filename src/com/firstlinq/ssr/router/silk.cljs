(ns com.firstlinq.ssr.router.silk
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [com.firstlinq.ssr.router :refer [Router path-for] :as r]
            [com.firstlinq.ssr.state :refer [get-state]]
            [domkm.silk :as silk]
            [clojure.string :as str]
            [cljs.core.async :refer [put! chan <!]]
            [goog.events :as events])
  (:import [goog.history Html5History EventType]))

(defrecord SilkRouter [routes ch]
  Router
  (navigate-to [this path]
    (when ch
      (when-let [path (if (vector? path) (apply path-for this path) path)]
        (put! ch path))))

  (path-exists? [_ path]
    (some?
      (try (silk/arrive routes path)
           (catch js/Error _))))

  (path-for [_ key params]
    (try
      (cond-> (silk/depart routes key (or params {}))
              (not (empty? (:query params)))
              (str "?" (silk/encode-query (get params :query {}))))
      (catch js/Error e
        (print "error" e)))))

;--------------------
; inspiration from : https://github.com/steida/este-library/blob/master/este/history/tokentransformer.coffee
(defn- token-transformer [])

(set! (.-retrieveToken (.-prototype token-transformer))
      (fn [path-prefix location]
        (str (.substr (.-pathname location) (.-length path-prefix))
             (.-search location)
             (.-hash location))))

(set! (.-createUrl (.-prototype token-transformer))
      (fn [token path-prefix location]
        (str path-prefix token)))
;------------------

(defn- create-history-channel [routes handler]
  (when (.isSupported Html5History)
    (let [ch      (chan)
          history (doto (Html5History. nil (token-transformer.))
                    (.setUseFragment false)
                    (.setPathPrefix "")
                    (.setEnabled true))]

      (events/listen
        history
        EventType/NAVIGATE
        (fn [event]
          (when (.-isNavigation event)
            (put! ch (.-token event)))
          nil))
      (go
        (while true
          (let [href (<! ch)]
            (when-let [route-map (silk/arrive routes href)]
              (when handler
                (. history (setToken href nil))
                (handler (get route-map ::silk/name) route-map))))))
      ch)))

(defn silk-router
  "Creates a silk based router"
  [routes & [handler]]
  (let [routes     (silk/routes routes)
        handler    (or handler get-state)
        history-ch (create-history-channel routes handler)]
    (map->SilkRouter {:routes routes
                      :ch     history-ch})))

(defn create-request->state
  "Creates a request->state function based on silk routes"
  [silk-routes & {:keys [state-fn user-fn user-key opts]
                  :or   {state-fn get-state
                         user-fn  :user
                         user-key :user}}]
  (let [routes (silk/routes silk-routes)]
    (fn [request]
      (when-let [match (silk/arrive routes (:uri request))]
        (let [params   (assoc match :query (:query-params request))
              route-id (::silk/name params)
              user     (user-fn request)
              init     {user-key user}
              params   {}  #_(merge {}
                              (:query-params request)       ; route params
                              (dissoc params ::silk/routes ::silk/pattern))
              ]                                             ; optional stuff
          (state-fn init route-id params opts))))))         ; no longer merge the state, let the client do so
