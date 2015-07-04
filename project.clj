(defproject com.firstlinq/cljs-ssr-node "0.1.0"
  :description "CLJS Server Side Rendering for NodeJS"
  :url "http://github.com/rukor/cljs-ssr-node"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 ;; Core dependencies
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ;; Swappable components

                 ;; OM
                 [org.omcljs/om "0.8.8" :scope "provided"]

                 ;; Reagent
                 [reagent "0.5.0" :scope "provided"]

                 ;; Silk routing
                 [com.domkm/silk "0.0.4" :scope "provided"]

                 ;; State serialisation
                 [com.cognitect/transit-cljs "0.8.220" :scope "provided"]
                 ]
  :node-dependencies [[source-map-support "0.2.8"]
                      [express "4.12.4"]
                      [plates "0.4.11"]
                      [react "0.12.2"]
                      [st "0.5.4"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-npm "0.4.0"]]

  :cljsbuild
  {:builds
   {:lib {:source-paths ["src"]
          :compiler     {:output-to     "resources/public/js/lib.js"
                         :output-dir    "resources/public/js"
                         :pretty-print  true
                         :optimizations :none}}}})
