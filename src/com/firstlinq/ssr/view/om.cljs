(ns com.firstlinq.ssr.view.om
  (:require [om.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [com.firstlinq.ssr.router :refer [path-for handle-navigate]]))

(defn link
  "Creates an anchor element with the link on-click handler defined using a
  custom navigator. Passed-in props must include a href property for it to work.
  Alternatively, specify route as [id params]"
  [router props & children]
  (let [[r-id r-params] (:route props)
        href      (if (:href props)
                    (:href props)
                    (when r-id (path-for router r-id r-params)))
        handler   (handle-navigate router href)
        new-props (into props {:href    href
                               :onClick handler})]
    (apply dom/a (clj->js new-props) children)))


(defn make-om-renderer [root-component router]
  (fn [state]
    (->> (om/build root-component state {:opts {:router router}})
         (dom/render-to-str))))