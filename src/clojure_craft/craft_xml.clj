(ns clojure-craft.craft-xml)
(use 'clojure.xml)

(def data-dirs (list 'chebi 'cl 'entrezgene 'go_bpmf 'go_cc 'ncbitaxon 'pr 'so))
;(def base-data-dir "/home/croeder/git/craft/craft-1.0/xml")
;(def file "11532192.txt.annotations.xml")
;(def test-file (str base-data-dir "/chebi/" file))


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
    (let [mention-id (:id (:attrs body))
          ontology-id (:id (:attrs (first (:content body)))) ]
      (cond (re-matches #"[PR|CHEBI|CL|GO].*" ontology-id)
            { mention-id ontology-id }
            :t 
            nil)))


;; CHEBI, PR, CL, GO (go_bpmf, go_cc)
;  <classMention id="PRO_Instance_30002">
;    <mentionClass id="PR:000001875">(PR) leptin receptor </mentionClass>
;  </classMention>

;; entrezgene (not implemented)
;  <classMention id="Entrez_Gene_Instance_48607">
;    <mentionClass id="Entrez Gene sequence">Entrez Gene sequence</mentionClass>
;    <hasSlotMention id="Entrez_Gene_Instance_48603" />
;  </classMention>
;  <integerSlotMention id="Entrez_Gene_Instance_48603">
;    <mentionSlot id="has Entrez Gene ID" />
;    <integerSlotMentionValue value="16847" /> ; <-------------------
;  </integerSlotMention>

;; ncbi taxon (not implemented)
;  <classMention id="organism_Instance_20927">
;    <mentionClass id="organism">organism</mentionClass>
;    <hasSlotMention id="organism_Instance_20928" />
;    <hasSlotMention id="organism_Instance_20929" />
;    <hasSlotMention id="organism_Instance_80246" />
;  </classMention>
;  <stringSlotMention id="organism_Instance_20928">
;    <mentionSlot id="common name" />
;    <stringSlotMentionValue value="[murine, mouse, mice, Nannomy, Mus, Nannomys]" />
;  </stringSlotMention>
;  <integerSlotMention id="organism_Instance_20929">
;    <mentionSlot id="taxonomy ID" />
;    <integerSlotMentionValue value="10088" /> ; <---------------------
;  </integerSlotMention>
;  <booleanSlotMention id="organism_Instance_80246">
;    <mentionSlot id="taxon ambiguity" />
;    <booleanSlotMentionValue value="false" />
;  </booleanSlotMention>



(defn- load-mentions-from-xml 
"Works through a file and creates a map from  mention-ids to ontolgy ids
key: \"chebi_Instance_20000\" 
value: \"CHEBI:35186\" "
[file]
(let [in-data (:content (first (read-craft-file file)))
      fname (:textSource in-data)]
  (reduce (fn [collector item]
          ;  (cond (and (= (:tag item) :classMention)  (parse-mention item))
            (cond (= (:tag item) :classMention)
                  (merge collector  (parse-mention item))
                  :t
                  collector))
          {}
          in-data)))



(defn load-annotations 
"given an xml file of annotations from the CRAFT xml directory,
produce a map from keys (pmid, start, end) to value  <ontology>:<id>"
[file pmid]
(let [annotations (load-annotations-from-xml file)
      mentions-map (load-mentions-from-xml file)]
  (reduce (fn [collector annotation] 
            (let [new-key (list pmid (Integer. (first annotation)) (Integer. (second annotation)))
                  ontology-id (mentions-map (nth annotation 3)) 
                  text-span (nth annotation 2)]
           (merge collector { new-key ontology-id })))
          {}
          annotations )))
  
   

