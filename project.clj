(defproject clojure_craft "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
				 [edu.ucdenver.ccp/kr-core "1.4.12"	]
				 [edu.ucdenver.ccp/kr-sesame-core "1.4.12"	]
				 [org.clojure/data.zip "0.1.1"]
				 ;;[clojure.xml]
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
  :main "clojure-craft.core"
)
