(defproject toggl-logger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/tools.cli "0.3.4"]
                 [clj-http "2.1.0"]
                 [cheshire "5.6.1"]]
  :main ^:skip-aot toggl-logger.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :uberjar-name "toggl-logger.jar"}})
