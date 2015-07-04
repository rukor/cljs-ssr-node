(ns com.firstlinq.ssr.view.reagent
  (:require [reagent.core :as reagent]
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
                               :on-click handler})]
    (into [:a new-props] children)))


(defn make-reagent-renderer [root-component router]
  (fn [state]
    (let [opts      {:router router}
          component (root-component opts)]
      (reagent/render-to-string [component (atom state)]))))
