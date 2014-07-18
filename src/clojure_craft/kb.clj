(ns clojure-craft.kb)

(use 'edu.ucdenver.ccp.kr.kb)
(use 'edu.ucdenver.ccp.kr.rdf)
(use 'edu.ucdenver.ccp.kr.sparql)
(require 'edu.ucdenver.ccp.kr.sesame.kb)


(defn kr-stuff []
	(def my-kb (kb :sesame-mem))
	(register-namespaces my-kb
      '(("ex" "http://www.example.org/") 
        ("rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
        ("foaf" "http://xmlns.com/foaf/0.1/"))))

