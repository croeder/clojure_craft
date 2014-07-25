(ns clojure-craft.craft-xml)
(use 'clojure.xml)

(def data-dirs (list 'chebi 'cl 'entrezgene 'go_bpmf 'go_cc 'ncbitaxon 'pr))
(def base-data-dir "/home/croeder/git/craft/craft-1.0/xml")
(def file "11532192.txt.annotations.xml")

(defn xml-example [file]
	(for [x (xml-seq
		(parse (java.io.File. file)))
	      	:when (= :low-node (:tag x))]
		(first (content x))
))

(defn read-craft-file [file]
	(xml-seq (parse (java.io.File. file))))

(defn parse-annotation 
"returns ( (start end covered) (id mention-id covered))"
[body] 
  (let [ id (:id (:attrs (second (:content body))))
         mention-id (:attrs (first (:content body)))
         start (:start (:attrs (nth (:content body) 2)))
         end   (:end (:attrs (nth (:content body) 2)))
         covered (:content (nth (:content body) 3))]
    (list (list start end covered) 
          (list id mention-id covered))))

(defn parse-mention 
"returns (id (ontology-id text)) from a class mention entity"
[body] 
      { (:id (:attrs body))
            (list (:id (:attrs (first (:content body)))) ;; the real chebi id
                  (first (:content    (first (:content body)))))} ;; the name or covered text
)

(defn load-annotations-from-xml
"Works through a file and creates a map for annotations, returns a map.
The key is a triple: start, end, text.
The value is a pair: annotation id, and then a map with :id and text
 key: (\"33070\" \"33079\" [\"pigmented\"]) 
 value: (\"chebi_Instance_90000\" {:id \"chebi_Instance_70395\"} [\"pigmented\"])"
[file]
	(let [in-data (:content (first (read-craft-file (str (str base-data-dir "/" "chebi") "/" file))))
              fname (:textSource in-data)]
              (loop [item (first in-data)
                     data (rest in-data)
                     annotations {} ]
                (println (count data))
                  (cond (= (:tag item) :annotation)
                        (let [[key value] (parse-annotation item)]
                          (cond (not (empty? data))
                                (recur (first data) (rest data) (assoc annotations key  value))
                                :t annotations))
                        :t 
                          (cond (not (empty? data))
                                (recur (first data) (rest data) annotations)
                                :t annotations)))))



(defn load-mentions-from-xml 
"Works through a file and creates a map for mentions.
The key is the annotation id. The value is a pair of
ontology id and text value.
key: \"chebi_Instance_20000\" 
value: (\"CHEBI:35186\" \"terpenes\") "
[file]
	(let [in-data (:content (first (read-craft-file (str (str base-data-dir "/" "chebi") "/" file))))
              fname (:textSource in-data)]
              (loop [item (first in-data)
                     data (rest in-data)
                     mentions {} ]
                (cond (= (:tag item) :classMention)
                      (let [myMap (parse-mention item)] 
                           (cond (not (empty? data)) 
                                 (recur (first data) (rest data) (merge mentions myMap))
                                 :t
                                 mentions) )
                      :t    
                      (recur (first data) (rest data) mentions)))))
 



