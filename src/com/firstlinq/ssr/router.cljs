(ns com.firstlinq.ssr.router)

(defprotocol Router
  (navigate-to [this path])
  (path-exists? [this path])
  (path-for [this key params]))

(defn handle-navigate [router href]
  (fn [e]
    (when (path-exists? router href)
      (.preventDefault e)
      (navigate-to router href))
    nil))

