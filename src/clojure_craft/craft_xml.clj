(ns clojure-craft.craft-xml)
(use 'clojure.xml)

(def data-dirs (list 'chebi 'cl 'entrezgene 'go_bpmf 'go_cc 'ncbitaxon 'pr 'so))
(def base-data-dir "/home/croeder/git/craft/craft-1.0/xml")
(def file "11532192.txt.annotations.xml")
(def test-file (str base-data-dir "/chebi/" file))


(defn- read-craft-file [file]
  (xml-seq (parse (java.io.File. file))))

(defn- parse-annotation 
"returns  (start end covered mention-id )"
[body] 
  (let [ id (:id (:attrs (second (:content body))))
         mention-id (:attrs (first (:content body)))
         start (:start (:attrs (nth (:content body) 2)))
         end   (:end (:attrs (nth (:content body) 2)))
         covered (:content (nth (:content body) 3))]
    (list start end covered (:id mention-id))))

(defn- load-annotations-from-xml
"Works through a file and returns a list of sequences:
(start, end, text, mention-id)
(\"33070\" \"33079\" [\"pigmented\"]  \"chebi_Instance_70395\")"
[file]
(let [in-data (:content (first (read-craft-file file)))
      fname (:textSource in-data)]
  (reduce (fn [collector item]
            (cond (= (:tag item) :annotation)
                  (conj collector (parse-annotation item))
                  :t 
                  collector))
          []
          in-data)))

(defn- parse-mention 
"returns {mention-id ontology-id} from a class mention entity"
[body] 
      { (:id (:attrs body))
        (:id (:attrs (first (:content body))))} )

(defn- load-mentions-from-xml 
"Works through a file and creates a map from  mention-ids to ontolgy ids
key: \"chebi_Instance_20000\" 
value: \"CHEBI:35186\" "
[file]
(let [in-data (:content (first (read-craft-file (str (str base-data-dir "/" "chebi") "/" file))))
      fname (:textSource in-data)]
  (reduce (fn [collector item]
            (cond (= (:tag item) :classMention)
                  (merge collector  (parse-mention item))
                  :t
                  collector))
          {}
          in-data)))


; annotations: (start, end, text, mention-id)
(defn load-annotations 
"given an xml file of annotations from the CRAFT xml directory,
produce a map from keys (file, start, end) to value  <ontology>:<id>"
[file]
(let [annotations (load-annotations-from-xml file)
      mentions-map (load-mentions-from-xml file)]
  (reduce (fn [collector annotation] 
            (let [new-key (list file (Integer. (first annotation)) (Integer. (second annotation)))
                  ontology-id (mentions-map (nth annotation 3)) 
                  text-span (nth annotation 2)]
           (merge collector { new-key ontology-id })))
          {}
          annotations )))
  
   

