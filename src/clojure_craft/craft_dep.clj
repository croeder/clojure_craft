(ns clojure-craft.craft-dep)
(use '[clojure.string :only (join split)])


(def base-data-dir "/home/croeder/git/craft/craft-1.0/dependency")
(def file "11532192.dep")

(def sample-sentence (list
"1 Intraocular intraocular JJ  _ 2 NMOD"
"2 pressure pressure NN _ 0 ROOT"
"3 in in IN _ 2 NMOD"
"4 genetically genetically RB _ 5 AMOD"
"5 distinct  distinct  JJ  _ 6 NMOD"
"6 mice  mouse NNS _ 3 PMOD"
"7 : : : _ 2 P"
"8 an  an  DT  _ 9 NMOD"
"9 update  update  NN  _ 2 DEP"
"10  and and CC  _ 9 COORD"
"11  strain  strain  NN  _ 12  NMOD"
"12  survey  survey  NN  _ 10  CONJ"))

(def sample-data (list
"1 Intraocular intraocular JJ _ 2 NMOD"
"2 pressure pressure NN _ 0 ROOT"
"3 in in IN  _ 2 NMOD"
"4 genetically genetically RB _ 5 AMOD"
"5 distinct distinct JJ  _ 6 NMOD"
"6 mice mouse NNS _ 3 PMOD"
"7 : : : _ 2 P"
"8 an  an DT  _ 9 NMOD"
"9 update update  NN  _ 2 DEP"
"10  and and CC  _ 9 COORD"
"11  strain  strain  NN  _ 12  NMOD"
"12  survey  survey  NN  _ 10  CONJ"
""
"1 Abstract  abstract  NN  _ 0 ROOT"
""
"1 Background  background  NN  _ 0 ROOT"
""
"1 Little  little  JJ  _ 2 SBJ"
"2 is  be  VBZ _ 0 ROOT"
"3 known know  VBN _ 2 VC"
"4 about about IN  _ 3 ADV"
"5 genetic genetic JJ  _ 6 NMOD"
"6 factors factor  NNS _ 4 PMOD"))

;; number literal normalized pos blank ref-number dep-code
;; dep-code: ROOT NMOD PMOD DEP COORD CONJ
;; pos: JJ NN IN RB JJ NNS DT CC

(defstruct dep  :id :literal :normalized :pos :dep-id :dep-type :start :end)
(defn parse-dependency-file
""
[file]

)

(defn parse-dependency-sentence
"parses a paragraph of a dep file representing a sentence,
returns a map keyed by id of the items. 
key: \"11\" 
value: {:id \"11\", :literal \"strain\", :normalized \"strain\", :pos \"NN\", :dep-id \"12\", :dep-type \"NMOD\"}
"
[item-list]
  (loop [my-list (rest item-list)
         item (first item-list)
         parts-map {} ]
    (cond (not (empty? my-list))
          (let [parsed-item (parse-dependency-item item)]
            (recur (rest my-list) (first my-list) 
                   (assoc parts-map (:id parsed-item) parsed-item)))
      :t parts-map)))


(defn parse-dependency-item
"parses a single line of the dependency file"
[item-string]
  (let [parts (split item-string #"\s+")]
    (struct dep (nth parts 0)
            (nth parts 1)
            (nth parts 2)
            (nth parts 3)
            (nth parts 5)
            (nth parts 6)))
)