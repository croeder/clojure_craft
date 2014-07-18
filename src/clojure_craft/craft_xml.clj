(ns clojure-craft.craft-xml)
(use 'clojure.xml)

(def data-dirs (list 'chebi 'cl 'entrezgene 'go_bpmf 'go_cc 'ncbitaxon 'pr 'sections-and-typography 'so))
(def base-data-dir "/home/croeder/git/craft/craft-1.0/xml")
(def file "11532192.txt.annotations.xml")
; classMention mentionClass annotation
(defn xml-example [file]
	(for [x (xml-seq
		(parse (java.io.File. file)))
			:when (= :low-node (:tag x))]
		(first (content x))
))

(defn read-craft-file [file]
	(xml-seq (parse (java.io.File. file))))

	

;user=> (for [x (xml-seq (parse (java.io.File. file)))
;                 :when (= :low-node (:tag x)) ] 
;              (first (:content x))
;          )
;
;
;user=> (for [x (xml-seq 
;              (parse (java.io.File. file)))
;                 :when (= :high-node (:tag x))]
;         (first (:content x)))

;;({:tag :low-node, :attrs nil, :content ["my text"]})

;     {:tag :classMention, 
;				:attrs {
;						:id chebi_Instance_30007}, 
;				:content [
;					{:tag :mentionClass, 
;						:attrs {:id CHEBI:35186}, 
;						:content [terpenes]
;					} ]
;			}

(defn load-xml []
	(let [data (read-craft-file (str (str base-data-dir "/" "chebi") "/" file))]
		(doseq [x data]
			(println "--->" x ))))
			;(println "--->" (:content x) ))))
