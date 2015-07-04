(ns com.firstlinq.ssr.log)

(defn ->log-output
  "Pretty prints CLJS objects, but allows POJOs to be logged as is, so that the browser's inspection
  can continue to function for POJOs"
  [e]
  (cond-> e
          (or (coll? e)
              (keyword? e))
          (pr-str)))

