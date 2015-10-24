(defproject mandelbrot-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [lein-figwheel "0.2.7-SNAPSHOT"]
                 [reagent "0.5.1"]
                 [re-com "0.6.2"]
                 [cljs-webgl "0.1.5-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.3.7"]]

  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id           "dev"
                        :source-paths ["src" "dev_src"]
                        :compiler     {:output-to      "target/js/mandelbrot.js"
                                       :output-dir     "target/js/out"
                                       :optimizations  :none
                                       :main           mandelbrot.dev
                                       :asset-path     "target/js/out"
                                       :source-map     true
                                       :cache-analysis true}}]}

  :figwheel {:http-server-root "public"
             :nrepl-port       7888
             :server-port      4449})
