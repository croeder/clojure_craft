(defproject clojure_craft "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
; :global-vars {*print-length* 10}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-Xmx512m" "-XX:-OmitStackTraceInFastThrow"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.zip "0.1.1"]
                 [log4j "1.2.15"
                  :exclusions [ [javax.mail/mail :extension "jar"]
                                [javax.jms/jms   :classifier "*"]
                                com.sun.jdmk/jmxtools
                                com.sun.jmx/jmxri ]]
                 ]
  :repositories [
		["clojars" {	
			:url "http://clojars.org/repo"
			:snapshots false
			:sign-release false
			:checksum :fail
			:update :always }	]
		["java.net" "http://download.java.net/maven/2"] ]
;;;  :main "clojure-craft.core"
)
