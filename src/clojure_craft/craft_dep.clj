(ns clojure-craft.craft-dep)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])
;;;; TODO: need to find start/end for the literals

;(def base-data-dir "/home/croeder/git/craft/craft-1.0/dependency")
;(def dep-file "11532192.dep")
;(def dep-file "short.dep")
;(def sample-file (str base-data-dir "/" dep-file))

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

;                     -1             0          1         2          3    5       6
(defstruct Dependency :sentence-num  :token-num :literal :normalized :pos :dep-id :dep-type )

(defn- parse-dependency-item
"parses a single line of the dependency file"
[item-string sentence-num]
  (let [parts (split item-string #"\s+")]
    (struct Dependency 
            sentence-num
            (Integer. (nth parts 0))
            (nth parts 1)
            (nth parts 2)
            (nth parts 3)
            (Integer. (nth parts 5))
            (nth parts 6)))
)


(defn- create-sos-from-paragraphs
"given a file of lines grouped into paragraphs with blank lines,
create a sequence-of-sequences representing a sequence of paragraphs
each holding sentences as sequences of tokens. There is potential for
confusion because the jenia file is one line per token of the source
text. Lines in jenia file represent tokens in the source. Paragraphs
in the jenia file represent lines in the source. Paragraphs in the source
are not distinguished."
[dep-file]
(with-open [buffered-reader (java.io.BufferedReader. (java.io.FileReader. dep-file))]
	         (loop [line (.readLine buffered-reader)
                        sos [] 
                        paragraph []]
                   (cond 
                    (not line)  (conj sos paragraph)
                    (re-matches #"^\d+.*"  line) (recur (.readLine buffered-reader) 
                                                         sos (conj paragraph line))
                    :t  (recur (.readLine buffered-reader) 
                               (conj sos paragraph) []) ))))

(defn- parse-line
" parse a jenia paragraph that represents a source line/sentence"
[jenia-paragraph sentence-number]
(map (fn [x] 
       (parse-dependency-item x sentence-number))
     jenia-paragraph))


(defn- parse-sos 
"parse a sequence of jenia paragraphs, numbering them"
[sos]
(map (fn [x sentence-number]
       (parse-line x sentence-number))
     sos (iterate inc 1)))

(defn load-dependencies [file]
  (parse-sos (create-sos-from-paragraphs file)))
